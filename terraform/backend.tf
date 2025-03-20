terraform {
  backend "s3" {
    bucket = "rizzlers-ibe-dev-tfstate"
    key    = "env/${terraform.workspace}/backend/terraform.tfstate"
    region = "ap-south-1"
  }
} 