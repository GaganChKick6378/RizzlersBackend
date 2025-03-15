variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name (dev or qa)"
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "subnets" {
  description = "List of subnet IDs for NLB"
  type        = list(string)
}

variable "alb_arn" {
  description = "ARN of the ALB to attach to the NLB target group"
  type        = string
}

variable "target_port" {
  description = "Port for the target group"
  type        = number
  default     = 80
}

variable "listener_port" {
  description = "Port for the NLB listener"
  type        = number
  default     = 80
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

variable "load_balancer_listener_arn" {
  description = "ARN of the load balancer listener"
  type        = string
} 