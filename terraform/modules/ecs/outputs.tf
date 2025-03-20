output "cluster_name" {
  description = "Name of the ECS cluster"
  value       = data.aws_ecs_cluster.existing_cluster.cluster_name
}

output "cluster_id" {
  description = "ID of the ECS cluster"
  value       = data.aws_ecs_cluster.existing_cluster.id
}

output "service_name" {
  description = "Name of the ECS service"
  value       = aws_ecs_service.app_service.name
}

output "task_definition_arn" {
  description = "ARN of the task definition"
  value       = aws_ecs_task_definition.app_task.arn
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