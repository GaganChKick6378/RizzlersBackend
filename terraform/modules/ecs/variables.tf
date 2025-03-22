variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment (dev or qa)"
  type        = string
}

variable "application_name" {
  description = "Name of the Spring Boot application"
  type        = string
  default     = "rizzlers-backend"
}

variable "context_path" {
  description = "Context path for the application"
  type        = string
}

variable "allow_bean_definition_overriding" {
  description = "Allow Spring bean definition overriding"
  type        = bool
  default     = true
}

variable "database_driver" {
  description = "Database driver class name"
  type        = string
}

variable "jpa_hibernate_ddl_auto" {
  description = "Hibernate DDL auto (none, update, create, etc.)"
  type        = string
}

variable "sql_init_mode" {
  description = "SQL initialization mode"
  type        = string
}

variable "flyway_enabled" {
  description = "Enable Flyway database migrations"
  type        = bool
}

variable "flyway_baseline_on_migrate" {
  description = "Set Flyway baseline on migrate"
  type        = bool
}

# CORS Configuration
variable "cors_allowed_origins" {
  description = "CORS allowed origins"
  type        = string
}

variable "cors_allowed_methods" {
  description = "CORS allowed methods"
  type        = string
}

variable "cors_allowed_headers" {
  description = "CORS allowed headers"
  type        = string
}

variable "cors_max_age" {
  description = "CORS max age in seconds"
  type        = number
}

# Actuator Configuration
variable "management_endpoints_web_exposure" {
  description = "Spring Boot Actuator endpoints to expose"
  type        = string
}

variable "management_endpoint_health_show_details" {
  description = "Show details in health endpoint"
  type        = string
}

variable "management_endpoints_web_base_path" {
  description = "Base path for actuator endpoints"
  type        = string
}

variable "management_health_probes_enabled" {
  description = "Enable Spring Boot health probes"
  type        = bool
}

variable "management_health_livenessState_enabled" {
  description = "Enable liveness state in health checks"
  type        = bool
}

variable "management_health_readinessState_enabled" {
  description = "Enable readiness state in health checks"
  type        = bool
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

variable "name_prefix" {
  description = "Prefix to use for resource names"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
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

variable "target_group_arn" {
  description = "ARN of the target group to register tasks with"
  type        = string
}

variable "load_balancer_listener_arn" {
  description = "ARN of the load balancer listener"
  type        = string
} 