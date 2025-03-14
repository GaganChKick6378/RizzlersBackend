output "api_gateway_url" {
  description = "URL of the API Gateway"
  value       = "${aws_api_gateway_rest_api.api.id}.execute-api.${var.aws_region}.amazonaws.com"
}

output "dev_stage_url" {
  description = "URL of the dev stage"
  value       = "${aws_api_gateway_stage.dev.invoke_url}"
}

output "qa_stage_url" {
  description = "URL of the qa stage"
  value       = "${aws_api_gateway_stage.qa.invoke_url}"
} 