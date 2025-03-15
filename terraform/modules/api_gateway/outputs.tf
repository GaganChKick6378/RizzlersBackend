output "api_url" {
  description = "The URL of the API Gateway"
  value       = "${data.aws_api_gateway_rest_api.existing_api.id}.execute-api.${var.aws_region}.amazonaws.com"
}

output "rest_api_id" {
  description = "ID of the REST API"
  value       = data.aws_api_gateway_rest_api.existing_api.id
}

output "qa_stage_name" {
  description = "Name of the qa stage"
  value       = aws_api_gateway_stage.qa.stage_name
} 