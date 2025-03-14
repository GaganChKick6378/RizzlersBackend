output "alb_sg_id" {
  description = "ID of the security group for the ALB"
  value       = aws_security_group.alb_sg.id
}

output "ecs_sg_id" {
  description = "ID of the security group for ECS tasks"
  value       = aws_security_group.ecs_sg.id
} 