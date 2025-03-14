output "cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.app_cluster.name
}

<<<<<<< HEAD
output "cluster_id" {
  description = "ID of the ECS cluster"
  value       = aws_ecs_cluster.app_cluster.id
}

output "service_name" {
  description = "Name of the ECS service"
  value       = aws_ecs_service.app_service.name
=======
output "dev_service_name" {
  description = "Name of the dev ECS service"
  value       = aws_ecs_service.dev_service.name
>>>>>>> 6cb266d (pushing for QA env)
}

output "qa_service_name" {
  description = "Name of the QA ECS service"
  value       = aws_ecs_service.qa_service.name
}

output "dev_task_definition_arn" {
  description = "ARN of the dev task definition"
  value       = aws_ecs_task_definition.dev_task.arn
}

output "qa_task_definition_arn" {
  description = "ARN of the QA task definition"
  value       = aws_ecs_task_definition.qa_task.arn
}

output "task_definition_family" {
  description = "Family of the task definition"
  value       = aws_ecs_task_definition.app_task.family
}

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group for ECS logs"
  value       = aws_cloudwatch_log_group.ecs_logs.name
}

output "target_group_arn" {
  description = "ARN of the target group"
  value       = var.target_group_arn
} 