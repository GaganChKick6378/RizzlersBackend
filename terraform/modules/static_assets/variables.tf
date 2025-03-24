variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name (dev or qa)"
  type        = string
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

variable "cors_allowed_origins" {
  description = "List of allowed origins for CORS"
  type        = list(string)
  default     = ["*"]
}

variable "cache_ttl" {
  description = "Default CloudFront cache TTL in seconds"
  type        = number
  default     = 86400 # 1 day
}

variable "price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_All" # Use all edge locations
} 