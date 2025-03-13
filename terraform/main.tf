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

# Get availability zones
data "aws_availability_zones" "available" {
  state = "available"
}

# Get public subnets
data "aws_subnet_ids" "public" {
  vpc_id = data.aws_vpc.kdu_vpc.id
  filter {
    name   = "tag:Type"
    values = ["Public"]
  }
}

data "aws_subnet" "public_subnets" {
  for_each = data.aws_subnet_ids.public.ids
  id       = each.value
}

# Get private subnets
data "aws_subnet_ids" "private" {
  vpc_id = data.aws_vpc.kdu_vpc.id
  filter {
    name   = "tag:Type"
    values = ["Private"]
  }
}

data "aws_subnet" "private_subnets" {
  for_each = data.aws_subnet_ids.private.ids
  id       = each.value
}

locals {
  public_subnet_ids  = [for s in data.aws_subnet.public_subnets : s.id]
  private_subnet_ids = [for s in data.aws_subnet.private_subnets : s.id]
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