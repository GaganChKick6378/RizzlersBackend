variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment (dev or qa)"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnets" {
  description = "Subnets for the NLB"
  type        = list(string)
}

variable "alb_arn" {
  description = "ARN of the ALB to forward traffic to"
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

variable "name_prefix" {
  description = "Prefix to use for resource names"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
} 