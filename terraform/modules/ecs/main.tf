# Create a new ECS cluster using the name_prefix instead of using an existing one
resource "aws_ecs_cluster" "cluster" {
  name = "${var.name_prefix}-cluster"
  
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  
  tags = var.tags
}

# CloudWatch Log Group for ECS
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/${var.name_prefix}"
  retention_in_days = 30
  
  tags = var.tags
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
  
  tags = var.tags
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
  
  tags = var.tags
}

# Task Definition
resource "aws_ecs_task_definition" "app_task" {
  family                   = "${var.name_prefix}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "${var.name_prefix}-container"
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
        # Database Configuration
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
          name  = "SPRING_DATASOURCE_DRIVER_CLASS_NAME"
          value = var.database_driver
        },
        {
          name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
          value = var.jpa_hibernate_ddl_auto
        },
        
        # Application Configuration
        {
          name  = "SPRING_APPLICATION_NAME"
          value = var.application_name
        },
        {
          name  = "SERVER_PORT"
          value = tostring(var.container_port)
        },
        {
          name  = "CONTEXT_PATH"
          value = var.context_path
        },
        {
          name  = "APPLICATION_ENVIRONMENT"
          value = var.environment
        },
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        },
        {
          name  = "SPRING_MAIN_ALLOW_BEAN_DEFINITION_OVERRIDING"
          value = tostring(var.allow_bean_definition_overriding)
        },
        
        # Initialization Mode
        {
          name  = "SQL_INIT_MODE"
          value = var.sql_init_mode
        },
        {
          name  = "ENV"
          value = var.environment
        },
        
        # Flyway Configuration
        {
          name  = "SPRING_FLYWAY_ENABLED"
          value = tostring(var.flyway_enabled)
        },
        {
          name  = "SPRING_FLYWAY_BASELINE_ON_MIGRATE"
          value = tostring(var.flyway_baseline_on_migrate)
        },
        
        # CORS Configuration
        {
          name  = "APPLICATION_CORS_ALLOWED_ORIGINS"
          value = var.cors_allowed_origins
        },
        {
          name  = "APPLICATION_CORS_ALLOWED_METHODS"
          value = var.cors_allowed_methods
        },
        {
          name  = "APPLICATION_CORS_ALLOWED_HEADERS"
          value = var.cors_allowed_headers
        },
        {
          name  = "APPLICATION_CORS_MAX_AGE"
          value = tostring(var.cors_max_age)
        },
        
        # Actuator Configuration
        {
          name  = "MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE"
          value = var.management_endpoints_web_exposure
        },
        {
          name  = "MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS"
          value = var.management_endpoint_health_show_details
        },
        {
          name  = "MANAGEMENT_ENDPOINTS_WEB_BASE_PATH"
          value = var.management_endpoints_web_base_path
        },
        {
          name  = "MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED"
          value = tostring(var.management_health_probes_enabled)
        },
        {
          name  = "MANAGEMENT_HEALTH_LIVENESSSTATE_ENABLED"
          value = tostring(var.management_health_livenessState_enabled)
        },
        {
          name  = "MANAGEMENT_HEALTH_READINESSSTATE_ENABLED"
          value = tostring(var.management_health_readinessState_enabled)
        },
        
        # Basic Configuration
        {
          name  = "CONTAINER_PORT"
          value = tostring(var.container_port)
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
        command     = ["CMD-SHELL", "wget -q --spider http://localhost:${var.container_port}/ || wget -q --spider http://localhost:${var.container_port}/ping || wget -q --spider http://localhost:${var.container_port}/health || wget -q --spider http://localhost:${var.container_port}/api/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 120
      }
    }
  ])
  
  tags = var.tags
}

# ECS Service
resource "aws_ecs_service" "app_service" {
  name             = "${var.name_prefix}-service"
  cluster          = aws_ecs_cluster.cluster.id
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
    container_name   = "${var.name_prefix}-container"
    container_port   = var.container_port
  }
  
  deployment_controller {
    type = "ECS"
  }
  
  tags = var.tags
  
  
  # Ensure that the service waits for the ALB to be ready
  depends_on = [var.load_balancer_listener_arn]
}

# Auto Scaling
resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = 4
  min_capacity       = 3
  resource_id        = "service/${aws_ecs_cluster.cluster.name}/${aws_ecs_service.app_service.name}"
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