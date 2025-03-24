output "bucket_name" {
  description = "The name of the S3 bucket"
  value       = aws_s3_bucket.static_assets.bucket
}

output "bucket_arn" {
  description = "The ARN of the S3 bucket"
  value       = aws_s3_bucket.static_assets.arn
}

output "bucket_regional_domain_name" {
  description = "The regional domain name of the S3 bucket"
  value       = aws_s3_bucket.static_assets.bucket_regional_domain_name
}

output "cloudfront_distribution_id" {
  description = "The ID of the CloudFront distribution"
  value       = aws_cloudfront_distribution.static_assets.id
}

output "cloudfront_domain_name" {
  description = "The domain name of the CloudFront distribution"
  value       = aws_cloudfront_distribution.static_assets.domain_name
}

output "cloudfront_arn" {
  description = "The ARN of the CloudFront distribution"
  value       = aws_cloudfront_distribution.static_assets.arn
} 