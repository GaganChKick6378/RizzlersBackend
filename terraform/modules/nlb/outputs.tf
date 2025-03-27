output "nlb_arn" {
  description = "ARN of the NLB"
  value       = aws_lb.nlb.arn
}

output "nlb_dns_name" {
  description = "DNS name of the NLB"
  value       = aws_lb.nlb.dns_name
}

output "target_group_arn" {
  description = "ARN of the NLB target group"
  value       = aws_lb_target_group.alb_target_group.arn
} 