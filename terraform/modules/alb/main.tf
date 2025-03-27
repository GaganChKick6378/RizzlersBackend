resource "aws_lb" "app_lb" {
  name               = "${var.name_prefix}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [var.security_group]
  subnets            = var.subnets
  
  enable_deletion_protection = false
  
  # Removing access logs to simplify deployment
  # access_logs {
  #   bucket  = aws_s3_bucket.alb_logs.bucket
  #   prefix  = "${var.project_name}-${var.environment}"
  #   enabled = true
  # }
  
  tags = var.tags
}

# Comment out S3 bucket for logs to simplify deployment
# resource "aws_s3_bucket" "alb_logs" {
#   bucket = "${var.name_prefix}-alb-logs"
#   
#   tags = var.tags
# }
# 
# resource "aws_s3_bucket_server_side_encryption_configuration" "alb_logs_encryption" {
#   bucket = aws_s3_bucket.alb_logs.id
# 
#   rule {
#     apply_server_side_encryption_by_default {
#       sse_algorithm = "AES256"
#     }
#   }
# }
# 
# resource "aws_s3_bucket_lifecycle_configuration" "alb_logs_lifecycle" {
#   bucket = aws_s3_bucket.alb_logs.id
# 
#   rule {
#     id     = "log-expiration"
#     status = "Enabled"
# 
#     expiration {
#       days = 90
#     }
#   }
# }
# 
# resource "aws_s3_bucket_policy" "alb_logs_policy" {
#   bucket = aws_s3_bucket.alb_logs.id
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Principal = {
#           AWS = "arn:aws:iam::718504428378:root"  # AWS Account ID for ALB logs in ap-south-1
#         }
#         Action   = "s3:PutObject"
#         Resource = "${aws_s3_bucket.alb_logs.arn}/${var.project_name}-${var.environment}/AWSLogs/*"
#       }
#     ]
#   })
# }

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app_lb.arn
  port              = 80
  protocol          = "HTTP"
  
  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.app_tg.arn
  }
}

# Using HTTP only for simplicity and to avoid ACM certificate validation timing out
# resource "aws_lb_listener" "https" {
#   load_balancer_arn = aws_lb.app_lb.arn
#   port              = 443
#   protocol          = "HTTPS"
#   ssl_policy        = "ELBSecurityPolicy-2016-08"
#   certificate_arn   = aws_acm_certificate.cert.arn
#   
#   default_action {
#     type             = "forward"
#     target_group_arn = aws_lb_target_group.app_tg.arn
#   }
# }

resource "aws_lb_target_group" "app_tg" {
  name        = "${var.name_prefix}-tg"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"
  
  health_check {
    healthy_threshold   = 2
    interval            = 120
    matcher             = "200-299"
    path                = var.health_check_path
    port                = "traffic-port"
    timeout             = 60
    unhealthy_threshold = 10
  }
  
  tags = var.tags
  
  # Adding deregistration delay to allow in-flight requests
  deregistration_delay = 300
}

# Comment out ACM certificate to avoid validation timeout
# resource "aws_acm_certificate" "cert" {
#   domain_name       = "${var.project_name}-${var.environment}.example.com"
#   validation_method = "DNS"
#   
#   tags = var.tags
#  
# } 