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

  # Use a static lifecycle configuration that's safe for CI/CD
  lifecycle {
    # Since we can't use variables directly here, we'll use a safer static setting
    # This can be overridden when working in local environments through terraform.tfvars
    prevent_destroy = false
    
    # Ignore changes to force_delete to prevent conflicts
    ignore_changes = [
      force_delete
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
} 