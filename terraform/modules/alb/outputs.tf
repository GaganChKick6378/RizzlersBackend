output "alb_dns_name" {
  description = "DNS name of the load balancer"
  value       = aws_lb.app_lb.dns_name
}

output "alb_arn" {
  description = "ARN of the application load balancer"
  value       = aws_lb.app_lb.arn
}

output "dev_target_group_arn" {
  description = "ARN of the dev target group"
  value       = aws_lb_target_group.dev_tg.arn
}

output "qa_target_group_arn" {
  description = "ARN of the qa target group"
  value       = aws_lb_target_group.qa_tg.arn
}

output "target_group_arn" {
  description = "ARN of the default target group (dev)"
  value       = aws_lb_target_group.dev_tg.arn
}

output "http_listener_arn" {
  description = "ARN of the HTTP listener"
  value       = aws_lb_listener.http.arn
} 