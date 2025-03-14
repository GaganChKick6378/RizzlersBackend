resource "aws_lb" "nlb" {
  name               = "${var.name_prefix}-nlb"
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

# Target group for ALB
resource "aws_lb_target_group" "alb_target_group" {
  name        = "${var.name_prefix}-alb-tg"
  port        = var.target_port
  protocol    = "TCP"
  vpc_id      = var.vpc_id
  target_type = "alb"

  health_check {
    enabled             = true
    interval            = 30
    port                = "traffic-port"
    protocol            = "TCP"
    healthy_threshold   = 3
    unhealthy_threshold = 3
  }

  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-NLB-TG-${var.environment}"
    }
  )
}

# Attach the ALB to the target group
resource "aws_lb_target_group_attachment" "alb_attachment" {
  target_group_arn = aws_lb_target_group.alb_target_group.arn
  target_id        = var.alb_arn
  port             = var.target_port
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