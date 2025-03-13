# Rizzlers Backend Deployment Guide

## Prerequisites
- AWS CLI configured with proper credentials
- Terraform installed
- Access to KDU-25-VPC

## Deployment Instructions

### 1. Identify Subnet IDs
The deployment requires subnet IDs from different availability zones in ap-south-1 region.

Run the helper script to identify the subnets:
```bash
cd terraform
./get_subnet_ids.sh
```

This will output a list of subnet IDs along with their availability zones. You need:
- At least 2 public subnets (from different AZs: ap-south-1a, ap-south-1b, ap-south-1c)
- At least 2 private subnets (from different AZs: ap-south-1a, ap-south-1b, ap-south-1c)

### 2. Configure Terraform Variables

Create `terraform.tfvars` file based on the example:
```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` and fill in the subnet IDs you identified:
```
public_subnet_ids = [
  "subnet-xxxxxxxx1", # ap-south-1a
  "subnet-xxxxxxxx2", # ap-south-1b
  "subnet-xxxxxxxx3"  # ap-south-1c
]

private_subnet_ids = [
  "subnet-xxxxxxxx4", # ap-south-1a
  "subnet-xxxxxxxx5", # ap-south-1b
  "subnet-xxxxxxxx6"  # ap-south-1c
]
```

Make sure to include subnets from at least two different availability zones.

> **IMPORTANT**: The `terraform.tfvars` file contains sensitive configuration information and should **NOT** be committed to GitHub. It has been added to `.gitignore` to prevent accidental commits. For CI/CD deployments, these variables are supplied through environment variables in the GitHub Actions workflow.

### 3. Deploy with Terraform

Initialize Terraform:
```bash
terraform init
```

Validate configuration:
```bash
terraform validate
```

Plan the deployment:
```bash
terraform plan
```

Apply the configuration:
```bash
terraform apply
```

## Troubleshooting

### Common Errors

1. **Multiple Availability Zones Error**
   ```
   Error: creating ELBv2 application Load Balancer: ValidationError: At least two subnets in two different Availability Zones must be specified
   ```
   **Solution**: Ensure your `public_subnet_ids` includes subnets from at least two different availability zones.

2. **VPC Link Subnet Error**
   ```
   Error: creating API Gateway v2 VPC Link: BadRequestException: SubnetIds for a vpc link cannot be empty
   ```
   **Solution**: Ensure your `private_subnet_ids` is populated correctly with valid subnet IDs.

3. **Load Balancer Association Error**
   ```
   Error: creating ECS Service: InvalidParameterException: The target group does not have an associated load balancer
   ```
   **Solution**: This is often a deployment sequence issue. Try running `terraform apply` again after the first run completes.

## Database Connection

The deployment is configured to use the RDS instance:
- Host: ibe2025-kdu25rdsinstance61f66da9-8harocvoxzt8.c3ysg6m2290x.ap-south-1.rds.amazonaws.com
- Database: Database_1_dev (Development) or Database_1_qa (Testing)
- User: Team_1
- Default Password: Password1

You should update the password immediately after accessing the database.

## CI/CD Pipeline

The project includes a GitHub Actions workflow for CI/CD. The workflow:
1. Provisions infrastructure using Terraform
2. Builds and deploys the application to ECS
3. Uses environment variables to configure Terraform

The subnet IDs and other configuration parameters are specified as environment variables in the workflow file, so there's no need to commit sensitive files like `terraform.tfvars`.

## Architecture Diagram

The deployment consists of:
- ECS Fargate for container hosting
- Application Load Balancer for distribution
- API Gateway for API management
- CloudWatch for monitoring
- ECR for container registry

All resources are deployed in the existing KDU-25-VPC.