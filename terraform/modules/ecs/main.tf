# Reference the existing ECS cluster - don't create or destroy it
data "aws_ecs_cluster" "existing_cluster" {
  cluster_name = "rizzlers-cluster"
}

# CloudWatch Log Group for ECS
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/${var.name_prefix}"
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-Logs-${var.environment}"
    }
  )
}

# Task Execution Role
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.name_prefix}-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-ExecutionRole-${var.environment}"
    }
  )
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Task Role
resource "aws_iam_role" "ecs_task_role" {
  name = "${var.name_prefix}-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-TaskRole-${var.environment}"
    }
  )
}

# Task Definition
resource "aws_ecs_task_definition" "app_task" {
  family                   = "rizzlers-tf-qa-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "rizzlers-tf-qa-task-container"
      image     = "${var.ecr_repository}:latest"
      essential = true
      
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]
      
      environment = [
        {
          name  = "SPRING_DATASOURCE_URL"
          value = var.database_url
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.database_username
        },
        {
          name  = "SPRING_DATASOURCE_PASSWORD"
          value = var.database_password
        },
        {
          name  = "APPLICATION_ENVIRONMENT"
          value = var.environment == "dev" ? "Development" : "Testing"
        },
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        }
      ]
      
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = "ap-south-1"
          "awslogs-stream-prefix" = "ecs"
        }
      }
      
      healthCheck = {
        command     = ["CMD-SHELL", "wget -q --spider http://localhost:${var.container_port}${var.health_check_path} || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-TaskDef-${var.environment}"
    }
  )
}

# ECS Service
resource "aws_ecs_service" "app_service" {
  name             = "rizzlers-tf-qa-service"
  cluster          = data.aws_ecs_cluster.existing_cluster.id
  task_definition  = aws_ecs_task_definition.app_task.arn
  launch_type      = "FARGATE"
  platform_version = "LATEST"
  desired_count    = 3
  
  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }
  
  network_configuration {
    subnets          = var.subnets
    security_groups  = [var.security_group]
    assign_public_ip = false
  }
  
  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = "rizzlers-tf-qa-task-container"
    container_port   = var.container_port
  }
  
  deployment_controller {
    type = "ECS"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-Service-${var.environment}"
    }
  )
  
  lifecycle {
    ignore_changes = [desired_count]
  }
  
  # Ensure that the service waits for the ALB to be ready
  depends_on = [var.load_balancer_listener_arn]
}

# Auto Scaling
resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = 4
  min_capacity       = 3
  resource_id        = "service/${data.aws_ecs_cluster.existing_cluster.cluster_name}/${aws_ecs_service.app_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "ecs_policy_cpu" {
  name               = "${var.name_prefix}-cpu-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
}

resource "aws_appautoscaling_policy" "ecs_policy_memory" {
  name               = "${var.name_prefix}-memory-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
} 