variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "rizzlers"
}

variable "environment" {
  description = "Environment name (dev or qa)"
  type        = string
  default     = "dev"
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Path for health check"
  type        = string
  default     = "/api/health"
}

variable "database_url" {
  description = "Database URL"
  type        = string
  default     = "jdbc:postgresql://ibe2025-kdu25rdsinstance61f66da9-8harocvoxzt8.c3ysg6m2290x.ap-south-1.rds.amazonaws.com:5432/Database_10_dev"
}

variable "database_username" {
  description = "Database username"
  type        = string
  default     = "Team_10"
}

variable "database_password" {
  description = "Database password"
  type        = string
  default     = "Password10"
  sensitive   = true
}

variable "vpc_name" {
  description = "Name of the VPC to use"
  type        = string
  default     = "KDU-25-VPC"
}

variable "availability_zones" {
  description = "List of availability zones to use"
  type        = list(string)
  default     = ["ap-south-1a", "ap-south-1b", "ap-south-1c"]
}

variable "use_existing_resources" {
  description = "Whether to use existing resources or create new ones"
  type        = bool
  default     = false
}

variable "resource_name_prefix" {
  description = "Prefix to use for resource names to avoid conflicts"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Common tags to apply to all resources"
  type        = map(string)
  default     = {
    Name    = "Rizzlers-Backend"
    Creator = "RizzlersTeam"
    Purpose = "IBE"
  }
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs in ap-south-1a, ap-south-1b, and ap-south-1c"
  type        = list(string)
  # These should be replaced with actual subnet IDs during deployment
  default     = []
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs in ap-south-1a, ap-south-1b, and ap-south-1c"
  type        = list(string)
  # These should be replaced with actual subnet IDs during deployment
  default     = []
} 