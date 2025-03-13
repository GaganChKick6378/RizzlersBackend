resource "aws_cloudwatch_log_group" "app_logs" {
  name              = "/aws/${var.name_prefix}/application"
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-App-Logs-${var.environment}"
    }
  )
}

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.name_prefix}-dashboard"
  
  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-service", "ClusterName", "${var.name_prefix}-cluster", { "stat" = "Average" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-south-1"
          title   = "ECS CPU Utilization"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ECS", "MemoryUtilization", "ServiceName", "${var.name_prefix}-service", "ClusterName", "${var.name_prefix}-cluster", { "stat" = "Average" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-south-1"
          title   = "ECS Memory Utilization"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ApplicationELB", "RequestCount", "LoadBalancer", "app/${var.name_prefix}-alb/*", { "stat" = "Sum" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-south-1"
          title   = "ALB Request Count"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", "app/${var.name_prefix}-alb/*", { "stat" = "Average" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-south-1"
          title   = "ALB Response Time"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ApiGateway", "Count", "ApiName", "${var.name_prefix}-api", { "stat" = "Sum" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-south-1"
          title   = "API Gateway Request Count"
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 12
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ApiGateway", "Latency", "ApiName", "${var.name_prefix}-api", { "stat" = "Average" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-south-1"
          title   = "API Gateway Latency"
          period  = 300
        }
      }
    ]
  })
}

# CloudWatch Alarm for API Gateway 4xx errors
resource "aws_cloudwatch_metric_alarm" "api_gateway_4xx_errors" {
  alarm_name          = "${var.name_prefix}-api-4xx-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "4XXError"
  namespace           = "AWS/ApiGateway"
  period              = 60
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "This alarm monitors for high 4XX errors on API Gateway"
  treat_missing_data  = "notBreaching"
  
  dimensions = {
    ApiName = "${var.name_prefix}-api"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-4xxAlarm-${var.environment}"
    }
  )
}

# CloudWatch Alarm for API Gateway 5xx errors
resource "aws_cloudwatch_metric_alarm" "api_gateway_5xx_errors" {
  alarm_name          = "${var.name_prefix}-api-5xx-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "5XXError"
  namespace           = "AWS/ApiGateway"
  period              = 60
  statistic           = "Sum"
  threshold           = 5
  alarm_description   = "This alarm monitors for high 5XX errors on API Gateway"
  treat_missing_data  = "notBreaching"
  
  dimensions = {
    ApiName = "${var.name_prefix}-api"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-5xxAlarm-${var.environment}"
    }
  )
}

# CloudWatch Alarm for high CPU on ECS
resource "aws_cloudwatch_metric_alarm" "ecs_high_cpu" {
  alarm_name          = "${var.name_prefix}-ecs-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "This alarm monitors for high CPU usage on ECS"
  treat_missing_data  = "notBreaching"
  
  dimensions = {
    ClusterName = "${var.name_prefix}-cluster"
    ServiceName = "${var.name_prefix}-service"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECS-CpuAlarm-${var.environment}"
    }
  )
} 