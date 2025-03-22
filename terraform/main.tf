provider "aws" {
  region = var.aws_region
  default_tags {
    tags = local.tags
  }
}

# Data source for VPC (using existing KDU-25-VPC)
data "aws_vpc" "kdu_vpc" {
  filter {
    name   = "tag:Name"
    values = [var.vpc_name]
  }
}

# Use the locals block provided by the user for workspace-based environment separation
locals {
  environment = terraform.workspace

  name_prefix = lower("${var.project_name}-${var.team_name}-${local.environment}")

  tags = {
    Creator     = "team-${var.team_name}"
    Purpose     = "${var.project_name}-project"
    Environment = local.environment
  }
  
  # These are placeholder values - you'll need to replace these with the actual subnet IDs from your VPC
  # Make sure to include at least one subnet in each of ap-south-1a, ap-south-1b, and ap-south-1c
  public_subnet_ids = var.public_subnet_ids
  private_subnet_ids = var.private_subnet_ids
}

# ECR Repository for container images
module "ecr" {
  source = "./modules/ecr"
  name   = local.name_prefix
  tags   = local.tags
}

# Security Groups
module "security_groups" {
  source      = "./modules/security"
  vpc_id      = data.aws_vpc.kdu_vpc.id
  environment = local.environment
  name_prefix = local.name_prefix
  tags        = local.tags
}

# ECS Cluster, Service, and Task Definition
module "ecs" {
  source          = "./modules/ecs"
  project_name    = var.project_name
  environment     = local.environment
  vpc_id          = data.aws_vpc.kdu_vpc.id
  subnets         = local.private_subnet_ids
  security_group  = module.security_groups.ecs_sg_id
  ecr_repository  = module.ecr.repository_url
  container_port  = var.container_port
  health_check_path = var.health_check_path
  tags            = local.tags
  database_url    = var.database_url
  database_username = var.database_username
  database_password = var.database_password
  name_prefix     = local.name_prefix
  target_group_arn = module.alb.target_group_arn
  load_balancer_listener_arn = module.alb.http_listener_arn
  
  # Application configuration
  application_name = var.application_name
  context_path = var.context_path
  allow_bean_definition_overriding = var.allow_bean_definition_overriding
  
  # Database configuration
  database_driver = var.database_driver
  jpa_hibernate_ddl_auto = var.jpa_hibernate_ddl_auto
  sql_init_mode = var.sql_init_mode
  
  # Flyway configuration
  flyway_enabled = var.flyway_enabled
  flyway_baseline_on_migrate = var.flyway_baseline_on_migrate
  
  # CORS Configuration
  cors_allowed_origins = var.cors_allowed_origins
  cors_allowed_methods = var.cors_allowed_methods
  cors_allowed_headers = var.cors_allowed_headers
  cors_max_age = var.cors_max_age
  
  # Actuator Configuration
  management_endpoints_web_exposure = var.management_endpoints_web_exposure
  management_endpoint_health_show_details = var.management_endpoint_health_show_details
  management_endpoints_web_base_path = var.management_endpoints_web_base_path
  management_health_probes_enabled = var.management_health_probes_enabled
  management_health_livenessState_enabled = var.management_health_livenessState_enabled
  management_health_readinessState_enabled = var.management_health_readinessState_enabled
}

# Application Load Balancer
module "alb" {
  source        = "./modules/alb"
  project_name  = var.project_name
  environment   = local.environment
  vpc_id        = data.aws_vpc.kdu_vpc.id
  subnets       = local.public_subnet_ids
  security_group = module.security_groups.alb_sg_id
  health_check_path = var.health_check_path
  tags          = local.tags
  name_prefix   = local.name_prefix
}

# Network Load Balancer for API Gateway
module "nlb" {
  source       = "./modules/nlb"
  project_name = var.project_name
  environment  = local.environment
  vpc_id       = data.aws_vpc.kdu_vpc.id
  subnets      = local.public_subnet_ids
  alb_arn      = module.alb.alb_arn
  alb_listener_arn = module.alb.http_listener_arn
  target_port  = 80
  listener_port = 80
  tags         = local.tags
  name_prefix  = local.name_prefix
}

# API Gateway - using a single API Gateway with both dev and qa stages
module "api_gateway" {
  source       = "./modules/api_gateway"
  project_name = var.project_name
  environment  = local.environment  # Just used for naming resources
  aws_region   = var.aws_region
  load_balancer_dns = module.alb.alb_dns_name
  load_balancer_listener_arn = module.alb.http_listener_arn
  nlb_arn      = module.nlb.nlb_arn
  vpc_id       = data.aws_vpc.kdu_vpc.id
  vpc_link_subnets = local.private_subnet_ids
  tags         = local.tags
  name_prefix  = local.name_prefix
}

# CloudWatch Logs
module "cloudwatch" {
  source       = "./modules/cloudwatch"
  project_name = var.project_name
  environment  = local.environment
  tags         = local.tags
  name_prefix  = local.name_prefix
} 