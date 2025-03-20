resource "aws_ecr_repository" "app_repo" {
  name                 = var.name
  image_tag_mutability = "MUTABLE"
  
  image_scanning_configuration {
    scan_on_push = true
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECR-${var.name}"
    }
  )
  
  # Prevent destruction of the ECR repository if it already exists
  lifecycle {
    prevent_destroy = true
    # Ignore changes to these attributes to prevent unnecessary updates
    ignore_changes = [
      image_tag_mutability,
      image_scanning_configuration,
      tags
    ]
  }
}

resource "aws_ecr_lifecycle_policy" "app_repo_policy" {
  repository = aws_ecr_repository.app_repo.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 5 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 5
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
  
  # Ignore changes to the lifecycle policy to prevent unnecessary updates
  lifecycle {
    ignore_changes = [policy]
  }
} 