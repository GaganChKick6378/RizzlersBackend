variable "aws_region" {
  description = "AWS Region"
  type        = string
}

variable "project_name" {
  description = "The name of the project, used for shared resources across environments"
  type        = string
}

variable "team_name" {
  description = "The name of the team maintaining this project"
  type        = string
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
}

variable "health_check_path" {
  description = "Path for health check"
  type        = string
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
  sensitive   = true
}

variable "vpc_name" {
  description = "Name of the VPC to use"
  type        = string
}

variable "tags" {
  description = "Common tags to apply to all resources"
  type        = map(string)
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs in ap-south-1a, ap-south-1b, and ap-south-1c"
  type        = list(string)
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs in ap-south-1a, ap-south-1b, and ap-south-1c"
  type        = list(string)
}

variable "graphql_endpoint" {
  description = "GraphQL API endpoint URL"
  type        = string
}

variable "graphql_api_key" {
  description = "API key for GraphQL API"
  type        = string
  sensitive   = true
} 