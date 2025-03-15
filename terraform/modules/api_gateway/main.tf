# Hard-code the existing API Gateway ID for QA environment
locals {
  # Use this ID for the existing API Gateway 
  existing_api_id = "3red8g1idh"
  
  # Calculate rest_api_id based on environment and use_existing_resources
  rest_api_id = var.use_existing_resources ? local.existing_api_id : length(aws_api_gateway_rest_api.api) > 0 ? aws_api_gateway_rest_api.api[0].id : ""
  
  # Only proceed if we have a valid API ID
  api_exists = local.rest_api_id != ""
  
  # Create a unique name for the security group
  sg_name = "${var.name_prefix}-vpce-sg-${var.environment}"
  
  # Create unique name for log group
  log_group_name = "/aws/apigateway/${var.name_prefix}-api-${var.environment}"
}

# Only create a new API Gateway if use_existing_resources is false
resource "aws_api_gateway_rest_api" "api" {
  count       = var.use_existing_resources ? 0 : 1
  name        = "${var.name_prefix}-api"
  description = "Rizzlers REST API Gateway"
  
  endpoint_configuration {
    types = ["REGIONAL"]
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway"
    }
  )
}

# Get the root resource ID for the API Gateway
# This is necessary when working with existing API Gateways
data "aws_api_gateway_resource" "root_resource" {
  count       = var.use_existing_resources && local.api_exists ? 1 : 0
  rest_api_id = local.rest_api_id
  path        = "/"
}

# Get the proxy resource if it exists
data "aws_api_gateway_resource" "proxy_resource" {
  count       = var.use_existing_resources && local.api_exists ? 1 : 0
  rest_api_id = local.rest_api_id
  path        = "/{proxy+}"
}

locals {
  # Get the root resource ID for the API Gateway
  # For a new API Gateway, this is the same as the rest_api_id
  # For an existing API Gateway, we need to fetch it with the data source
  root_resource_id = var.use_existing_resources && local.api_exists && length(data.aws_api_gateway_resource.root_resource) > 0 ? data.aws_api_gateway_resource.root_resource[0].id : length(aws_api_gateway_rest_api.api) > 0 ? aws_api_gateway_rest_api.api[0].root_resource_id : ""
  
  # Calculate proxy resource ID
  proxy_resource_id = var.use_existing_resources && local.api_exists && length(data.aws_api_gateway_resource.proxy_resource) > 0 ? data.aws_api_gateway_resource.proxy_resource[0].id : length(aws_api_gateway_resource.proxy) > 0 ? aws_api_gateway_resource.proxy[0].id : ""
  
  # Determine if proxy resource exists
  proxy_exists = local.proxy_resource_id != ""
}

# Security group for VPC Link - with environment in name
resource "aws_security_group" "vpce_sg" {
  name        = local.sg_name
  description = "Security group for API Gateway VPC Link - ${var.environment}"
  vpc_id      = var.vpc_id
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-SG-${var.environment}"
    }
  )
}

resource "aws_security_group_rule" "vpce_egress" {
  security_group_id = aws_security_group.vpce_sg.id
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

# Create a VPC Link for integrating with private resources - adding environment to name
resource "aws_api_gateway_vpc_link" "link" {
  count       = local.api_exists ? 1 : 0
  name        = "${var.name_prefix}-vpce-link-${var.environment}"
  target_arns = [var.nlb_arn]
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-VpcLink-${var.environment}"
    }
  )
}

# API resource for the proxy integration - only create if it doesn't exist and we're not using existing resources
resource "aws_api_gateway_resource" "proxy" {
  count       = (!var.use_existing_resources) && local.api_exists && local.root_resource_id != "" ? 1 : 0
  rest_api_id = local.rest_api_id
  parent_id   = local.root_resource_id
  path_part   = "{proxy+}"
}

# Setup a method for the proxy resource with ANY HTTP method
resource "aws_api_gateway_method" "proxy_method" {
  count         = (!var.use_existing_resources) && local.api_exists && local.proxy_exists ? 1 : 0
  rest_api_id   = local.rest_api_id
  resource_id   = local.proxy_resource_id
  http_method   = "ANY"
  authorization = "NONE" # No authorization for now as per requirement
  
  # Define the request parameters that need to be passed
  request_parameters = {
    "method.request.path.proxy" = true
  }
}

# Integration with Load Balancer
resource "aws_api_gateway_integration" "lb_integration" {
  count                   = local.api_exists && local.proxy_exists ? 1 : 0
  rest_api_id             = local.rest_api_id
  resource_id             = local.proxy_resource_id
  http_method             = "ANY"  # We know this method exists
  
  # HTTP_PROXY maintains original HTTP method
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  
  # Path parameter must match exactly what's in the request_parameters mapping
  uri                     = "http://${var.load_balancer_dns}/{proxy}"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.link[0].id
  
  # Pass all request parameters to the backend
  passthrough_behavior    = "WHEN_NO_MATCH"
  
  # Cache configuration to improve performance
  cache_key_parameters    = ["method.request.path.proxy"]
  
  # Map the request path parameter to the integration URI path parameter
  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

# Root path method and integration - only create if not using existing resources
resource "aws_api_gateway_method" "root_method" {
  count         = (!var.use_existing_resources) && local.api_exists && local.root_resource_id != "" ? 1 : 0
  rest_api_id   = local.rest_api_id
  resource_id   = local.root_resource_id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "root_integration" {
  count         = local.api_exists && local.root_resource_id != "" ? 1 : 0
  rest_api_id   = local.rest_api_id
  resource_id   = local.root_resource_id
  http_method   = "ANY"  # We know this method exists
  
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${var.load_balancer_dns}/"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.link[0].id
  
  # Ensure cache configuration is consistent
  cache_key_parameters = []
}

# CORS is handled separately as it may not already exist
# We'll only set this up for the proxy resource if it exists
resource "aws_api_gateway_method" "proxy_options" {
  count         = local.api_exists && local.proxy_exists ? 1 : 0
  rest_api_id   = local.rest_api_id
  resource_id   = local.proxy_resource_id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "proxy_options_response" {
  count       = local.api_exists && length(aws_api_gateway_method.proxy_options) > 0 ? 1 : 0
  rest_api_id = local.rest_api_id
  resource_id = local.proxy_resource_id
  http_method = aws_api_gateway_method.proxy_options[0].http_method
  status_code = "200"
  
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Origin"  = true
  }
}

resource "aws_api_gateway_integration" "proxy_options_integration" {
  count       = local.api_exists && length(aws_api_gateway_method.proxy_options) > 0 ? 1 : 0
  rest_api_id = local.rest_api_id
  resource_id = local.proxy_resource_id
  http_method = aws_api_gateway_method.proxy_options[0].http_method
  type        = "MOCK"
  
  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }
}

resource "aws_api_gateway_integration_response" "proxy_options_integration_response" {
  count       = local.api_exists && length(aws_api_gateway_integration.proxy_options_integration) > 0 && length(aws_api_gateway_method_response.proxy_options_response) > 0 ? 1 : 0
  rest_api_id = local.rest_api_id
  resource_id = local.proxy_resource_id
  http_method = aws_api_gateway_method.proxy_options[0].http_method
  status_code = aws_api_gateway_method_response.proxy_options_response[0].status_code
  
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
    "method.response.header.Access-Control-Allow-Methods" = "'GET,POST,PUT,DELETE,OPTIONS'"
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
  }
}

# Deployment and Stages
resource "aws_api_gateway_deployment" "deployment" {
  count       = local.api_exists && local.proxy_exists && length(aws_api_gateway_integration.lb_integration) > 0 && length(aws_api_gateway_integration.root_integration) > 0 ? 1 : 0
  rest_api_id = local.rest_api_id
  
  depends_on = [
    aws_api_gateway_integration.lb_integration,
    aws_api_gateway_integration.root_integration
  ]
  
  # Use a timestamp to force redeployment when needed
  triggers = {
    # Add timestamp to ensure deployment happens on every apply
    redeployment = sha1(jsonencode([
      local.proxy_resource_id,
      local.root_resource_id,
      length(aws_api_gateway_integration.lb_integration) > 0 ? aws_api_gateway_integration.lb_integration[0].id : "",
      length(aws_api_gateway_integration.root_integration) > 0 ? aws_api_gateway_integration.root_integration[0].id : "",
      timestamp()
    ]))
  }
  
  lifecycle {
    create_before_destroy = true
  }
}

# Only create the stage for the current environment
resource "aws_api_gateway_stage" "env_stage" {
  count        = local.api_exists && length(aws_api_gateway_deployment.deployment) > 0 ? 1 : 0
  deployment_id = aws_api_gateway_deployment.deployment[0].id
  rest_api_id   = local.rest_api_id
  stage_name    = var.environment
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-${var.environment}-Stage"
    }
  )
}

# CloudWatch Log Group for API Gateway
resource "aws_cloudwatch_log_group" "api_logs" {
  name              = local.log_group_name
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-Logs-${var.environment}"
    }
  )
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