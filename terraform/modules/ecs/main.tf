resource "aws_ecs_cluster" "app_cluster" {
<<<<<<< HEAD
  name = "${var.project_name}-cluster"
=======
  name = "rizzlers-cluster"
>>>>>>> 6cb266d (pushing for QA env)
  
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  
  tags = merge(
    var.tags,
    {
<<<<<<< HEAD
      Name = "${var.project_name}-ECS-Cluster"
=======
      Name = "Rizzlers-ECS-Cluster"
>>>>>>> 6cb266d (pushing for QA env)
    }
  )
}

# CloudWatch Log Group for ECS Dev
resource "aws_cloudwatch_log_group" "ecs_logs_dev" {
  name              = "/ecs/${var.name_prefix}-dev"
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-Logs-Dev"
    }
  )
}

# CloudWatch Log Group for ECS QA
resource "aws_cloudwatch_log_group" "ecs_logs_qa" {
  name              = "/ecs/${var.name_prefix}-qa"
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-Logs-QA"
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
      Name = "Rizzlers-ECS-ExecutionRole"
    }
  )
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Add ECR access policy
resource "aws_iam_policy" "ecr_access_policy" {
  name        = "${var.name_prefix}-ecr-access-policy"
  description = "Policy that allows ECR access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_ecr_policy_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.ecr_access_policy.arn
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
      Name = "Rizzlers-ECS-TaskRole"
    }
  )
}

# Dev Task Definition
resource "aws_ecs_task_definition" "dev_task" {
  family                   = "${var.name_prefix}-dev-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "${var.name_prefix}-dev-container"
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
<<<<<<< HEAD
          value = var.environment == "dev" ? "Development" : "Testing"
        },
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
=======
          value = "Development"
>>>>>>> 6cb266d (pushing for QA env)
        }
      ]
      
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs_dev.name
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
      Name = "Rizzlers-ECS-TaskDef-Dev"
    }
  )
}

# QA Task Definition
resource "aws_ecs_task_definition" "qa_task" {
  family                   = "${var.name_prefix}-qa-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "${var.name_prefix}-qa-container"
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
          value = "Testing"
        }
      ]
      
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs_qa.name
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
      Name = "Rizzlers-ECS-TaskDef-QA"
    }
  )
}

# ECS Service for Dev
resource "aws_ecs_service" "dev_service" {
  name             = "${var.name_prefix}-dev-service"
  cluster          = aws_ecs_cluster.app_cluster.id
  task_definition  = aws_ecs_task_definition.dev_task.arn
  launch_type      = "FARGATE"
  platform_version = "LATEST"
  desired_count    = 1
  
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
    container_name   = "${var.name_prefix}-dev-container"
    container_port   = var.container_port
  }
  
  deployment_controller {
    type = "ECS"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-Service-Dev"
    }
  )
  
  lifecycle {
    ignore_changes = [desired_count]
  }
  
  # Ensure that the service waits for the ALB to be ready
  depends_on = [var.load_balancer_listener_arn]
}

# ECS Service for QA
resource "aws_ecs_service" "qa_service" {
  name             = "${var.name_prefix}-qa-service"
  cluster          = aws_ecs_cluster.app_cluster.id
  task_definition  = aws_ecs_task_definition.qa_task.arn
  launch_type      = "FARGATE"
  platform_version = "LATEST"
  desired_count    = 1
  
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
    target_group_arn = var.qa_target_group_arn != "" ? var.qa_target_group_arn : var.target_group_arn
    container_name   = "${var.name_prefix}-qa-container"
    container_port   = var.container_port
  }
  
  deployment_controller {
    type = "ECS"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-Service-QA"
    }
  )
  
  lifecycle {
    ignore_changes = [desired_count]
  }
  
  # Ensure that the service waits for the ALB to be ready
  depends_on = [var.load_balancer_listener_arn]
}

# Auto Scaling for Dev
resource "aws_appautoscaling_target" "dev_ecs_target" {
  max_capacity       = 4
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.app_cluster.name}/${aws_ecs_service.dev_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "dev_ecs_policy_cpu" {
  name               = "${var.name_prefix}-dev-cpu-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.dev_ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.dev_ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.dev_ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
}

resource "aws_appautoscaling_policy" "dev_ecs_policy_memory" {
  name               = "${var.name_prefix}-dev-memory-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.dev_ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.dev_ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.dev_ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
}

# Auto Scaling for QA
resource "aws_appautoscaling_target" "qa_ecs_target" {
  max_capacity       = 4
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.app_cluster.name}/${aws_ecs_service.qa_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "qa_ecs_policy_cpu" {
  name               = "${var.name_prefix}-qa-cpu-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.qa_ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.qa_ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.qa_ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
}

resource "aws_appautoscaling_policy" "qa_ecs_policy_memory" {
  name               = "${var.name_prefix}-qa-memory-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.qa_ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.qa_ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.qa_ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
} 