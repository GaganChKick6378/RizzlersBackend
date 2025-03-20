output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = module.ecr.repository_url
}

output "ecs_cluster_name" {
  description = "Name of the shared ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Name of the ECS service for the current environment"
  value       = module.ecs.service_name
}

output "api_gateway_url" {
  description = "Base URL of the API Gateway"
  value       = module.api_gateway.api_url
}

output "api_gateway_environment_url" {
  description = "URL of the API Gateway environment stage"
  value       = "https://${module.api_gateway.api_url}/${module.api_gateway.stage_name}"
}

# Keep these for backward compatibility but they will now reference the workspace-specific URL
output "api_gateway_dev_url" {
  description = "URL of the API Gateway dev stage (DEPRECATED: Use api_gateway_environment_url)"
  value       = terraform.workspace == "dev" ? "https://${module.api_gateway.api_url}/dev" : null
}

output "api_gateway_qa_url" {
  description = "URL of the API Gateway qa stage (DEPRECATED: Use api_gateway_environment_url)"
  value       = terraform.workspace == "qa" ? "https://${module.api_gateway.api_url}/qa" : null
}

output "alb_dns_name" {
  description = "DNS name of the ALB"
  value       = module.alb.alb_dns_name
}

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group for ECS logs"
  value       = module.ecs.cloudwatch_log_group
}

output "environment" {
  description = "Current environment based on workspace"
  value       = terraform.workspace
}

output "name_prefix" {
  description = "Resource naming prefix used for all resources"
  value       = local.name_prefix
} 