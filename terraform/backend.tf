terraform {
  backend "s3" {
    bucket = "rizzlers-ibe-tfstate"
    key    = "terraform.tfstate"
    region = "ap-south-1"
    # The workspace name will be used to create separate state files for each environment
    # For example:
    # - dev workspace state will be saved in env:/dev/terraform.tfstate
    # - qa workspace state will be saved in env:/qa/terraform.tfstate
  }
} 