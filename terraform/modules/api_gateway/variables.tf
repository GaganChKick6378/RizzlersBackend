variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name (dev or qa)"
  type        = string
}

variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "ap-south-1"
}

variable "load_balancer_dns" {
  description = "DNS name of the load balancer"
  type        = string
}

variable "load_balancer_listener_arn" {
  description = "ARN of the load balancer listener"
  type        = string
}

variable "nlb_arn" {
  description = "ARN of the NLB"
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "vpc_link_subnets" {
  description = "List of subnet IDs for VPC Link"
  type        = list(string)
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}

variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "use_existing_resources" {
  description = "Whether to use existing resources instead of creating new ones"
  type        = bool
  default     = false
} 