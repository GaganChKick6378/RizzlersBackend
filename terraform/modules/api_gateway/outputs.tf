output "api_url" {
  description = "The URL of the API Gateway"
  value       = "${aws_api_gateway_rest_api.api.id}.execute-api.${var.aws_region}.amazonaws.com"
}

output "rest_api_id" {
  description = "ID of the REST API"
  value       = aws_api_gateway_rest_api.api.id
}

output "dev_stage_name" {
  description = "Name of the dev stage"
  value       = aws_api_gateway_stage.dev.stage_name
}

output "qa_stage_name" {
  description = "Name of the qa stage"
  value       = aws_api_gateway_stage.qa.stage_name
} 