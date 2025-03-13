provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Creator = "RizzlersTeam"
      Purpose = "IBE"
    }
  }
}

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

# ECR Repository for container images
module "ecr" {
  source = "./modules/ecr"
  name   = local.resource_name_prefix
  tags   = var.tags
}

# Security Groups
module "security_groups" {
  source      = "./modules/security"
  vpc_id      = data.aws_vpc.kdu_vpc.id
  environment = var.environment
  name_prefix = local.resource_name_prefix
  tags        = var.tags
}

# ECS Cluster, Service, and Task Definition
module "ecs" {
  source          = "./modules/ecs"
  project_name    = var.project_name
  environment     = var.environment
  vpc_id          = data.aws_vpc.kdu_vpc.id
  subnets         = local.private_subnet_ids
  security_group  = module.security_groups.ecs_sg_id
  ecr_repository  = module.ecr.repository_url
  container_port  = var.container_port
  health_check_path = var.health_check_path
  tags            = var.tags
  database_url    = var.database_url
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

# API Gateway
module "api_gateway" {
  source       = "./modules/api_gateway"
  project_name = var.project_name
  environment  = var.environment
  load_balancer_dns = module.alb.alb_dns_name
  load_balancer_listener_arn = module.alb.http_listener_arn
  vpc_id       = data.aws_vpc.kdu_vpc.id
  vpc_link_subnets = local.private_subnet_ids
  tags         = var.tags
  name_prefix  = local.resource_name_prefix
}

# CloudWatch Logs
module "cloudwatch" {
  source       = "./modules/cloudwatch"
  project_name = var.project_name
  environment  = var.environment
  tags         = var.tags
  name_prefix  = local.resource_name_prefix
} 