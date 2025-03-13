resource "aws_apigatewayv2_api" "api" {
  name          = "${var.project_name}-${var.environment}-api"
  protocol_type = "HTTP"
  
  cors_configuration {
    allow_headers     = ["content-type", "x-amz-date", "authorization", "x-api-key", "x-amz-security-token"]
    allow_methods     = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_origins     = ["http://localhost:3000", "https://drld61kimwwfp.cloudfront.net"]
    expose_headers    = ["content-type", "content-length"]
    max_age           = 3600
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-${var.environment}"
    }
  )
}

# VPC Link for private integration
resource "aws_apigatewayv2_vpc_link" "link" {
  name               = "${var.project_name}-${var.environment}-vpce-link"
  security_group_ids = [aws_security_group.vpce_sg.id]
  subnet_ids         = var.vpc_link_subnets
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-VpcLink-${var.environment}"
    }
  )
}

# Security group for VPC Link
resource "aws_security_group" "vpce_sg" {
  name        = "${var.project_name}-${var.environment}-vpce-sg"
  description = "Security group for API Gateway VPC Link"
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

# Integration with ALB
resource "aws_apigatewayv2_integration" "alb_integration" {
  api_id           = aws_apigatewayv2_api.api.id
  integration_type = "HTTP_PROXY"
  
  integration_uri    = "https://${var.load_balancer_dns}"
  integration_method = "ANY"
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.link.id
  
  # Timeout configurations
  timeout_milliseconds = 29000
}

# Route configuration
resource "aws_apigatewayv2_route" "default_route" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "ANY /{proxy+}"
  
  target = "integrations/${aws_apigatewayv2_integration.alb_integration.id}"
}

# Stages
resource "aws_apigatewayv2_stage" "dev" {
  api_id      = aws_apigatewayv2_api.api.id
  name        = "dev"
  auto_deploy = true
  
  default_route_settings {
    throttling_burst_limit = 100
    throttling_rate_limit  = 50
    detailed_metrics_enabled = true
  }
  
  # Enable access logging
  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_logs.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      resourcePath   = "$context.resourcePath"
      status         = "$context.status"
      protocol       = "$context.protocol"
      responseLength = "$context.responseLength"
      integrationLatency = "$context.integrationLatency"
      responseLatency = "$context.responseLatency"
    })
  }
  
  # Enable API caching
  stage_variables = {
    "env" = "dev"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-DevStage"
    }
  )
}

resource "aws_apigatewayv2_stage" "qa" {
  api_id      = aws_apigatewayv2_api.api.id
  name        = "qa"
  auto_deploy = true
  
  default_route_settings {
    throttling_burst_limit = 100
    throttling_rate_limit  = 50
    detailed_metrics_enabled = true
  }
  
  # Enable access logging
  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_logs.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      resourcePath   = "$context.resourcePath"
      status         = "$context.status"
      protocol       = "$context.protocol"
      responseLength = "$context.responseLength"
      integrationLatency = "$context.integrationLatency"
      responseLatency = "$context.responseLatency"
    })
  }
  
  # Enable API caching
  stage_variables = {
    "env" = "qa"
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-QaStage"
    }
  )
}

# CloudWatch Log Group for API Gateway
resource "aws_cloudwatch_log_group" "api_logs" {
  name              = "/aws/apigateway/${var.project_name}-${var.environment}-api"
  retention_in_days = 30
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-Logs-${var.environment}"
    }
  )
}

# Usage plan with throttling
resource "aws_api_gateway_usage_plan" "usage_plan" {
  name        = "${var.project_name}-${var.environment}-usage-plan"
  description = "Usage plan with throttling"
  
  api_stages {
    api_id = aws_apigatewayv2_api.api.id
    stage  = aws_apigatewayv2_stage.dev.name
  }
  
  api_stages {
    api_id = aws_apigatewayv2_api.api.id
    stage  = aws_apigatewayv2_stage.qa.name
  }
  
  quota_settings {
    limit  = 100000
    period = "MONTH"
  }
  
  throttle_settings {
    burst_limit = 100
    rate_limit  = 50
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ApiGateway-UsagePlan-${var.environment}"
    }
  )
} 