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
  description = "Subnets for the ECS service"
  type        = list(string)
}

variable "security_group" {
  description = "Security group for the ECS service"
  type        = string
}

variable "ecr_repository" {
  description = "ECR repository URL"
  type        = string
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
}

variable "health_check_path" {
  description = "Path for health checks"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}

variable "database_url" {
  description = "Database URL"
  type        = string
}

variable "database_username" {
  description = "Database username"
  type        = string
}

variable "database_password" {
  description = "Database password"
  type        = string
} 