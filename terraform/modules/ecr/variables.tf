variable "name" {
  description = "Name of the ECR repository"
  type        = string
}

variable "tags" {
  description = "Tags to apply to the ECR repository"
  type        = map(string)
  default     = {}
}

variable "prevent_destroy" {
  description = "Flag to prevent destroying the repository even if it would normally be destroyed"
  type        = bool
  default     = false
}

variable "force_delete" {
  description = "Flag to force deletion of the repository even if it contains images"
  type        = bool
  default     = false
} 