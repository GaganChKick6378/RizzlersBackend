output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/rizzlers-tf-qa"
}

output "ecs_cluster_name" {
  description = "Name of the shared ECS cluster"
  value       = "rizzlers-cluster"
}

output "ecs_service_name" {
  description = "Name of the ECS service for the current environment"
  value       = module.ecs.service_name
}

output "api_gateway_url" {
  description = "URL of the API Gateway"
  value       = "${var.aws_region}.execute-api.amazonaws.com/${module.api_gateway.rest_api_id}"
}

output "api_gateway_qa_url" {
  description = "URL of the API Gateway qa stage"
  value       = "${var.aws_region}.execute-api.amazonaws.com/${module.api_gateway.rest_api_id}/qa"
}

output "alb_dns_name" {
  description = "DNS name of the ALB"
  value       = module.alb.alb_dns_name
}

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group for ECS logs"
  value       = module.ecs.cloudwatch_log_group
} 