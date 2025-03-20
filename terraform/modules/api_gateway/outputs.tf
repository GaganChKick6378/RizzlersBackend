output "api_id" {
  description = "ID of the API Gateway REST API"
  value       = aws_api_gateway_rest_api.api.id
}

output "api_endpoint" {
  description = "Endpoint URL of the API Gateway"
  value       = aws_api_gateway_rest_api.api.execution_arn
}

output "stage_name" {
  description = "Name of the API Gateway stage"
  value       = aws_api_gateway_stage.env_stage.stage_name
}

output "invoke_url" {
  description = "URL to invoke the API endpoint"
  value       = "${aws_api_gateway_rest_api.api.execution_arn}/${aws_api_gateway_stage.env_stage.stage_name}"
}

output "api_url" {
  description = "Base URL of the API Gateway"
  value       = "${aws_api_gateway_rest_api.api.id}.execute-api.${var.aws_region}.amazonaws.com"
} 