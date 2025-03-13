output "api_gateway_url" {
  description = "URL of the API Gateway"
  value       = aws_apigatewayv2_api.api.api_endpoint
}

output "dev_stage_url" {
  description = "URL of the dev stage"
  value       = "${aws_apigatewayv2_api.api.api_endpoint}/dev"
}

output "qa_stage_url" {
  description = "URL of the qa stage"
  value       = "${aws_apigatewayv2_api.api.api_endpoint}/qa"
} 