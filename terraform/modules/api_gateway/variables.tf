variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment (dev or qa)"
  type        = string
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
  description = "ARN of the Network Load Balancer for VPC Link"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "vpc_link_subnets" {
  description = "Subnets for the VPC Link"
  type        = list(string)
}

variable "name_prefix" {
  description = "Prefix to use for resource names"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}

variable "aws_region" {
  description = "AWS region for the API Gateway"
  type        = string
  default     = "ap-south-1"
} 