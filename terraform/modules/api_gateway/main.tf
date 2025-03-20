# Create API Gateway for dev environment
resource "aws_api_gateway_rest_api" "api" {
  name        = "${var.name_prefix}-api"
  description = "Dev Environment REST API Gateway"
  
  endpoint_configuration {
    types = ["REGIONAL"]
  }
  
  tags = var.tags
}

# Local variable to determine which API ID to use
locals {
  rest_api_id = aws_api_gateway_rest_api.api.id
  root_resource_id = aws_api_gateway_rest_api.api.root_resource_id
}

# Create a VPC Link for integrating with private resources
resource "aws_api_gateway_vpc_link" "link" {
  name        = "${var.name_prefix}-vpce-link"
  target_arns = [var.nlb_arn]
  tags        = var.tags
}

# Security group for VPC Link
resource "aws_security_group" "vpce_sg" {
  name        = "${var.name_prefix}-vpce-sg"
  description = "Security group for API Gateway VPC Link"
  vpc_id      = var.vpc_id
  
  tags = var.tags
}

resource "aws_security_group_rule" "vpce_egress" {
  security_group_id = aws_security_group.vpce_sg.id
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

# API resource for the proxy integration
resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "{proxy+}"
}

# Setup a method for the proxy resource with ANY HTTP method
resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "ANY"
  authorization = "NONE"
  
  request_parameters = {
    "method.request.path.proxy" = true
  }
}

# Integration with Load Balancer
resource "aws_api_gateway_integration" "lb_integration" {
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.proxy.id
  http_method             = aws_api_gateway_method.proxy_method.http_method
  
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${var.load_balancer_dns}/{proxy}"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.link.id
  
  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

# Root path method and integration
resource "aws_api_gateway_method" "root_method" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_rest_api.api.root_resource_id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "root_integration" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_rest_api.api.root_resource_id
  http_method = aws_api_gateway_method.root_method.http_method
  
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${var.load_balancer_dns}/"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.link.id
}

# CORS Configuration
resource "aws_api_gateway_method" "proxy_options" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "proxy_options_response" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.proxy_options.http_method
  status_code = "200"
  
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Origin"  = true
  }
}

resource "aws_api_gateway_integration" "proxy_options_integration" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.proxy_options.http_method
  type        = "MOCK"
  
  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }
}

resource "aws_api_gateway_integration_response" "proxy_options_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.proxy_options.http_method
  status_code = aws_api_gateway_method_response.proxy_options_response.status_code
  
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
    "method.response.header.Access-Control-Allow-Methods" = "'GET,POST,PUT,DELETE,OPTIONS'"
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
  }
}

# Deployment
resource "aws_api_gateway_deployment" "deployment" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  
  depends_on = [
    aws_api_gateway_integration.lb_integration,
    aws_api_gateway_integration.root_integration,
    aws_api_gateway_integration_response.proxy_options_integration_response
  ]

}

# Stage based on workspace
resource "aws_api_gateway_stage" "env_stage" {
  deployment_id = aws_api_gateway_deployment.deployment.id
  rest_api_id  = aws_api_gateway_rest_api.api.id
  stage_name   = terraform.workspace
}

# CloudWatch Log Group for API Gateway
resource "aws_cloudwatch_log_group" "api_logs" {
  name              = "/aws/apigateway/${var.name_prefix}-api"
  retention_in_days = 30
  
  tags = var.tags
}

# Note: Usage plans are not supported for HTTP APIs
# The commented code below would be used for REST APIs instead
# resource "aws_api_gateway_usage_plan" "usage_plan" {
#   name        = "${var.name_prefix}-usage-plan"
#   description = "Usage plan with throttling"
#   
#   quota_settings {
#     limit  = 100000
#     period = "MONTH"
#   }
#   
#   throttle_settings {
#     burst_limit = 100
#     rate_limit  = 50
#   }
#   
#   tags = merge(
#     var.tags,
#     {
#       Name = "Rizzlers-ApiGateway-UsagePlan-${var.environment}"
#     }
#   )
# } 