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
  description = "URL of the API Gateway"
  value       = "${module.api_gateway.api_url}"
}

output "api_gateway_dev_url" {
  description = "URL of the API Gateway dev stage"
  value       = "${module.api_gateway.api_url}/dev"
}

output "api_gateway_qa_url" {
  description = "URL of the API Gateway qa stage"
  value       = "${module.api_gateway.api_url}/qa"
}

output "alb_dns_name" {
  description = "DNS name of the ALB"
  value       = module.alb.alb_dns_name
}

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group for ECS logs"
  value       = module.ecs.cloudwatch_log_group
} 