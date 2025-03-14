resource "aws_cloudwatch_log_group" "app_logs" {
  name              = "/app/${var.name_prefix}"
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-App-Logs-${var.environment}"
    }
  )
}

# Dashboard to monitor the application
resource "aws_cloudwatch_dashboard" "app_dashboard" {
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
            ["AWS/ECS", "CPUUtilization", "ServiceName", "${var.name_prefix}-service", "ClusterName", "${var.project_name}-cluster"]
          ]
          period = 300
          stat   = "Average"
          region = "ap-south-1"
          title  = "ECS CPU Utilization"
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
            ["AWS/ECS", "MemoryUtilization", "ServiceName", "${var.name_prefix}-service", "ClusterName", "${var.project_name}-cluster"]
          ]
          period = 300
          stat   = "Average"
          region = "ap-south-1"
          title  = "ECS Memory Utilization"
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
            ["AWS/ApplicationELB", "RequestCount", "LoadBalancer", "${var.name_prefix}-alb"]
          ]
          period = 300
          stat   = "Sum"
          region = "ap-south-1"
          title  = "ALB Request Count"
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
            ["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", "${var.name_prefix}-alb"]
          ]
          period = 300
          stat   = "Average"
          region = "ap-south-1"
          title  = "ALB Response Time"
        }
      }
    ]
  })
} 