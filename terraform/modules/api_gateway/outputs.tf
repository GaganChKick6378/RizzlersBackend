output "api_url" {
  description = "The URL of the API Gateway"
  value       = "${local.rest_api_id}.execute-api.${var.aws_region}.amazonaws.com"
}

output "rest_api_id" {
  description = "ID of the REST API"
  value       = local.rest_api_id
}

output "stage_name" {
  description = "Name of the environment stage"
  value       = aws_api_gateway_stage.environment_stage.stage_name
} 