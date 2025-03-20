provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Creator = "RizzlersTeam"
      Purpose = "IBE"
    }
  }
}

# Get current AWS account ID
data "aws_caller_identity" "current" {}

# Data source for VPC (using existing KDU-25-VPC)
data "aws_vpc" "kdu_vpc" {
  filter {
    name   = "tag:Name"
    values = [var.vpc_name]
  }
}

# Use hardcoded subnets instead of dynamic discovery
locals {
  # These are placeholder values - you'll need to replace these with the actual subnet IDs from your VPC
  # Make sure to include at least one subnet in each of ap-south-1a, ap-south-1b, and ap-south-1c
  public_subnet_ids = var.public_subnet_ids
  private_subnet_ids = var.private_subnet_ids
  resource_name_prefix = var.resource_name_prefix != "" ? var.resource_name_prefix : "${var.project_name}-${var.environment}"
}

# Security Groups
module "security_groups" {
  source      = "./modules/security"
  vpc_id      = data.aws_vpc.kdu_vpc.id
  environment = var.environment
  name_prefix = local.resource_name_prefix
  tags        = var.tags
}

# ECS Service and Task Definition (using existing cluster)
module "ecs" {
  source          = "./modules/ecs"
  project_name    = var.project_name
  environment     = var.environment
  vpc_id          = data.aws_vpc.kdu_vpc.id
  subnets         = local.private_subnet_ids
  security_group  = module.security_groups.ecs_sg_id
  ecr_repository  = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/rizzlers-tf-qa"
  container_port  = var.container_port
  health_check_path = var.health_check_path
  tags            = var.tags
  database_url    = var.database_url
  qa_database_url = var.qa_database_url
  database_username = var.database_username
  database_password = var.database_password
  name_prefix     = local.resource_name_prefix
  target_group_arn = module.alb.target_group_arn
  load_balancer_listener_arn = module.alb.http_listener_arn
}

# Application Load Balancer
module "alb" {
  source        = "./modules/alb"
  project_name  = var.project_name
  environment   = var.environment
  vpc_id        = data.aws_vpc.kdu_vpc.id
  subnets       = local.public_subnet_ids
  security_group = module.security_groups.alb_sg_id
  health_check_path = var.health_check_path
  tags          = var.tags
  name_prefix   = local.resource_name_prefix
}

# Network Load Balancer for API Gateway
module "nlb" {
  source       = "./modules/nlb"
  project_name = var.project_name
  environment  = var.environment
  vpc_id       = data.aws_vpc.kdu_vpc.id
  subnets      = local.public_subnet_ids
  alb_arn      = module.alb.alb_arn
  target_port  = 80
  listener_port = 80
  tags         = var.tags
  name_prefix  = local.resource_name_prefix
}

# API Gateway - using existing API Gateway with both dev and qa stages
module "api_gateway" {
  source       = "./modules/api_gateway"
  project_name = var.project_name
  environment  = "qa"  # Only targeting QA environment
  aws_region   = var.aws_region
  load_balancer_dns = module.alb.alb_dns_name
  load_balancer_listener_arn = module.alb.http_listener_arn
  nlb_arn      = module.nlb.nlb_arn
  vpc_id       = data.aws_vpc.kdu_vpc.id
  vpc_link_subnets = local.private_subnet_ids
  tags         = var.tags
  name_prefix  = "${var.project_name}"  # Remove environment suffix to make it shared
}

# CloudWatch Logs
module "cloudwatch" {
  source       = "./modules/cloudwatch"
  project_name = var.project_name
  environment  = var.environment
  tags         = var.tags
  name_prefix  = local.resource_name_prefix
} 