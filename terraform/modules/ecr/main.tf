resource "aws_ecr_repository" "app_repo" {
  name                 = var.name
  image_tag_mutability = "MUTABLE"
  force_delete         = var.force_delete
  
  image_scanning_configuration {
    scan_on_push = true
  }
  
  tags = merge(
    var.tags,
    {
      Name = "Rizzlers-ECR-${var.name}"
    }
  )

  # Use dynamic blocks to conditionally add lifecycle configuration
  dynamic "lifecycle" {
    for_each = var.prevent_destroy ? [1] : []
    content {
      prevent_destroy = true
    }
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
} 