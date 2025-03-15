resource "aws_lb" "nlb" {
  name               = "${var.name_prefix}-nlb-${var.environment}"
  internal           = false
  load_balancer_type = "network"
  subnets            = var.subnets

  enable_deletion_protection = false

  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-NLB-${var.environment}"
    }
  )
}

# Target group for ALB - Update to use HTTP protocol for health checks
resource "aws_lb_target_group" "alb_target_group" {
  name        = "${var.name_prefix}-alb-tg-${var.environment}"
  port        = var.target_port
  protocol    = "TCP"
  vpc_id      = var.vpc_id
  target_type = "alb"

  health_check {
    enabled             = true
    interval            = 30
    port                = "traffic-port"
    protocol            = "HTTP"
    path                = "/"
    healthy_threshold   = 3
    unhealthy_threshold = 3
    timeout             = 5
    matcher             = "200-399"
  }

  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-NLB-TG-${var.environment}"
    }
  )
}

# Only try to attach the ALB to the target group after we're sure the ALB has a listener
# This is to prevent the "must have at least one listener" error
resource "aws_lb_target_group_attachment" "alb_attachment" {
  target_group_arn = aws_lb_target_group.alb_target_group.arn
  target_id        = var.alb_arn
  port             = var.target_port

  # Add depends_on to ensure the ALB listener exists before attempting the attachment
  depends_on = [var.load_balancer_listener_arn]

  # Add lifecycle ignore_changes to prevent Terraform from trying to recreate the attachment
  lifecycle {
    ignore_changes = [target_id]
  }
}

# Listener for the NLB
resource "aws_lb_listener" "nlb_listener" {
  load_balancer_arn = aws_lb.nlb.arn
  port              = var.listener_port
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.alb_target_group.arn
  }
} 