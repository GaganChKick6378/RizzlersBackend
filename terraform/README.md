# Rizzlers Backend Infrastructure

This directory contains the Terraform code for deploying the Rizzlers backend infrastructure on AWS.

## Architecture

The infrastructure consists of the following components:

- **API Gateway**: HTTP API Gateway with dev and qa stages, throttling, and caching
- **Load Balancer**: Application Load Balancer for routing traffic to ECS
- **ECS**: Elastic Container Service for running the containerized application
- **ECR**: Elastic Container Registry for storing Docker images
- **CloudWatch**: Logs, metrics, and alarms for monitoring

## Prerequisites

- AWS CLI configured with appropriate credentials
- Terraform 1.5.0 or later
- S3 bucket for Terraform state (rizzlers-ibe-dev-tfstate)

## Deployment

### Manual Deployment

1. Initialize Terraform:
   ```
   terraform init
   ```

2. Plan the deployment:
   ```
   terraform plan -var="environment=dev"
   ```

3. Apply the changes:
   ```
   terraform apply -var="environment=dev"
   ```

### CI/CD Deployment

The infrastructure is automatically deployed via GitHub Actions when changes are pushed to the `dev` or `qa` branches.

## Environment Variables

The following environment variables are used in the ECS task definition:

- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `APPLICATION_ENVIRONMENT`: Environment name (Development or Testing)

## Modules

- **ECR**: Elastic Container Registry for Docker images
- **ECS**: Elastic Container Service for running containers
- **ALB**: Application Load Balancer for routing traffic
- **API Gateway**: HTTP API Gateway for API management
- **Security**: Security groups for controlling access
- **CloudWatch**: Logs, metrics, and alarms for monitoring

## Outputs

After deployment, the following outputs are available:

- `ecr_repository_url`: URL of the ECR repository
- `alb_dns_name`: DNS name of the load balancer
- `api_gateway_url`: URL of the API Gateway
- `api_gateway_dev_stage_url`: URL of the API Gateway dev stage
- `api_gateway_qa_stage_url`: URL of the API Gateway qa stage
- `ecs_cluster_name`: Name of the ECS cluster
- `ecs_service_name`: Name of the ECS service
- `cloudwatch_log_group`: CloudWatch log group for the application 