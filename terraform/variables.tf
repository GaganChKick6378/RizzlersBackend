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
  default     = "jdbc:postgresql://database-kdu.czpwqpnfk9dp.ap-south-1.rds.amazonaws.com:5432/Database_10_dev"
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

variable "tags" {
  description = "Common tags to apply to all resources"
  type        = map(string)
  default     = {
    Name    = "Rizzlers-Backend"
    Creator = "RizzlersTeam"
    Purpose = "IBE"
  }
} 