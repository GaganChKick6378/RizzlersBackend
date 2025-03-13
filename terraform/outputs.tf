output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = module.ecr.repository_url
}

output "alb_dns_name" {
  description = "DNS name of the load balancer"
  value       = module.alb.alb_dns_name
}

output "api_gateway_url" {
  description = "URL of the API Gateway"
  value       = module.api_gateway.api_gateway_url
}

output "api_gateway_dev_stage_url" {
  description = "URL of the API Gateway dev stage"
  value       = module.api_gateway.dev_stage_url
}

output "api_gateway_qa_stage_url" {
  description = "URL of the API Gateway qa stage"
  value       = module.api_gateway.qa_stage_url
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs.service_name
}

output "cloudwatch_log_group" {
  description = "CloudWatch log group for the application"
  value       = module.cloudwatch.log_group_name
} 