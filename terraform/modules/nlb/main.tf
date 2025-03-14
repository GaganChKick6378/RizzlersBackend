resource "aws_lb" "nlb" {
  name               = "${var.name_prefix}-nlb"
  internal           = true
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

resource "aws_lb_target_group" "nlb_tg" {
  name     = "${var.name_prefix}-nlb-tg"
  port     = var.target_port
  protocol = "TCP"
  vpc_id   = var.vpc_id
  target_type = "alb"

  health_check {
    enabled             = true
    port                = "traffic-port"
    healthy_threshold   = 3
    unhealthy_threshold = 3
    interval            = 30
  }

  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-NLB-TargetGroup-${var.environment}"
    }
  )
}

resource "aws_lb_listener" "nlb_listener" {
  load_balancer_arn = aws_lb.nlb.arn
  port              = var.listener_port
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.nlb_tg.arn
  }
}

resource "aws_lb_target_group_attachment" "nlb_tg_attachment" {
  target_group_arn = aws_lb_target_group.nlb_tg.arn
  target_id        = var.alb_arn
  port             = 80
} 