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

# Application configuration variables
variable "application_name" {
  description = "Name of the Spring Boot application"
  type        = string
  default     = "rizzlers-backend"
}

variable "server_port" {
  description = "Port that the Spring Boot application runs on"
  type        = number
}

variable "context_path" {
  description = "Context path for the application"
  type        = string
}

variable "spring_profiles_active" {
  description = "Active Spring profiles"
  type        = string
}

variable "application_environment" {
  description = "Application environment indicator"
  type        = string
}

variable "allow_bean_definition_overriding" {
  description = "Allow Spring bean definition overriding"
  type        = bool
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

# GraphQL Configuration
variable "graphql_endpoint" {
  description = "GraphQL endpoint URL"
  type        = string
}

variable "graphql_api_key" {
  description = "GraphQL API key"
  type        = string
  sensitive   = true
}

variable "graphql_api_key_header" {
  description = "GraphQL API key header name"
  type        = string
}

variable "graphql_timeout" {
  description = "GraphQL timeout in milliseconds"
  type        = number
}

variable "graphql_graphiql_enabled" {
  description = "Enable GraphiQL UI"
  type        = bool
}

variable "graphql_servlet_mapping" {
  description = "GraphQL servlet mapping path"
  type        = string
}

variable "graphql_graphiql_path" {
  description = "GraphiQL UI path"
  type        = string
}

variable "graphql_playground_enabled" {
  description = "Enable GraphQL Playground"
  type        = bool
}

# Logging Configuration
variable "logging_level_webclient" {
  description = "Logging level for WebClient"
  type        = string
}

variable "logging_level_netty" {
  description = "Logging level for Netty HTTP client"
  type        = string
}

variable "logging_level_webclient_response" {
  description = "Logging level for WebClient responses"
  type        = string
}

variable "logging_level_graphql_service" {
  description = "Logging level for GraphQL service"
  type        = string
}

variable "logging_level_tenant_service" {
  description = "Logging level for tenant service"
  type        = string
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