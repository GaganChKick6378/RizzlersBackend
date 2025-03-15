output "api_url" {
  description = "The URL of the API Gateway"
  value       = local.api_exists ? "${local.rest_api_id}.execute-api.${var.aws_region}.amazonaws.com" : ""
}

output "rest_api_id" {
  description = "ID of the REST API"
  value       = local.rest_api_id
}

output "stage_name" {
  description = "Name of the environment stage"
  value       = local.stage_exists ? data.aws_api_gateway_stage.existing_stage[0].stage_name : length(aws_api_gateway_stage.env_stage) > 0 ? aws_api_gateway_stage.env_stage[0].stage_name : var.environment
}

output "stage_url" {
  description = "URL of the environment stage"
  value       = local.api_exists ? "${local.rest_api_id}.execute-api.${var.aws_region}.amazonaws.com/${var.environment}" : ""
} 