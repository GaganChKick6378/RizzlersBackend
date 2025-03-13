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
    values = ["KDU-25-VPC"]
  }
}

# Get public and private subnets
data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.kdu_vpc.id]
  }
  filter {
    name   = "tag:Type"
    values = ["Public"]
  }
}

data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.kdu_vpc.id]
  }
  filter {
    name   = "tag:Type"
    values = ["Private"]
  }
}

# ECR Repository for container images
module "ecr" {
  source = "./modules/ecr"
  name   = "${var.project_name}-${var.environment}"
  tags   = var.tags
}

# Security Groups
module "security_groups" {
  source      = "./modules/security"
  vpc_id      = data.aws_vpc.kdu_vpc.id
  environment = var.environment
  tags        = var.tags
}

# ECS Cluster, Service, and Task Definition
module "ecs" {
  source          = "./modules/ecs"
  project_name    = var.project_name
  environment     = var.environment
  vpc_id          = data.aws_vpc.kdu_vpc.id
  subnets         = data.aws_subnets.private.ids
  security_group  = module.security_groups.ecs_sg_id
  ecr_repository  = module.ecr.repository_url
  container_port  = var.container_port
  health_check_path = var.health_check_path
  tags            = var.tags
  database_url    = var.database_url
  database_username = var.database_username
  database_password = var.database_password
}

# Application Load Balancer
module "alb" {
  source        = "./modules/alb"
  project_name  = var.project_name
  environment   = var.environment
  vpc_id        = data.aws_vpc.kdu_vpc.id
  subnets       = data.aws_subnets.public.ids
  security_group = module.security_groups.alb_sg_id
  health_check_path = var.health_check_path
  tags          = var.tags
}

# API Gateway
module "api_gateway" {
  source       = "./modules/api_gateway"
  project_name = var.project_name
  environment  = var.environment
  load_balancer_dns = module.alb.alb_dns_name
  vpc_id       = data.aws_vpc.kdu_vpc.id
  vpc_link_subnets = data.aws_subnets.private.ids
  tags         = var.tags
}

# CloudWatch Logs
module "cloudwatch" {
  source       = "./modules/cloudwatch"
  project_name = var.project_name
  environment  = var.environment
  tags         = var.tags
} 