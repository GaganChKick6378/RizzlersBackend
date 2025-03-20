# Terraform Infrastructure for Rizzlers Backend

This Terraform configuration sets up the infrastructure for the Rizzlers Backend application using AWS services like ECS, ECR, API Gateway, and more.

## Workspace-based Environment Separation

This configuration uses Terraform workspaces to manage different environments (dev, qa, etc.). Each workspace creates resources with a consistent naming convention based on the workspace name.

## Configuration Files

- `terraform.tfvars`: Contains common configuration values for all environments
- `dev.tfvars`: Contains dev-specific configuration values
- `qa.tfvars`: Contains qa-specific configuration values

## Manual Usage

### Initialize Terraform
```bash
terraform init
```

### Create and Select Workspaces
```bash
# Create dev workspace (if it doesn't exist)
terraform workspace new dev

# Create QA workspace (if it doesn't exist)
terraform workspace new qa

# Switch between workspaces
terraform workspace select dev
terraform workspace select qa
```

### Plan and Apply Configuration
For development environment:
```bash
# Select dev workspace
terraform workspace select dev

# Plan with appropriate tfvars files
terraform plan -var-file=terraform.tfvars -var-file=dev.tfvars

# Apply with appropriate tfvars files
terraform apply -var-file=terraform.tfvars -var-file=dev.tfvars
```

For QA environment:
```bash
# Select qa workspace
terraform workspace select qa

# Plan with appropriate tfvars files
terraform plan -var-file=terraform.tfvars -var-file=qa.tfvars

# Apply with appropriate tfvars files
terraform apply -var-file=terraform.tfvars -var-file=qa.tfvars
```

## Automated Deployment with GitHub Actions

This project includes a GitHub Actions workflow that automates the deployment process. The workflow:

1. Sets up the infrastructure using Terraform
2. Builds the backend application Docker image
3. Pushes the image to Amazon ECR
4. Deploys the image to Amazon ECS

### GitHub Secrets Required

The following secrets must be set in your GitHub repository:

| Secret Name | Description |
|-------------|-------------|
| `AWS_ACCESS_KEY_ID` | AWS Access Key with appropriate permissions |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key paired with the access key |
| `DB_PASSWORD` | Database password |
| `DB_URL_DEV` | Database URL for the dev environment |
| `DB_USERNAME` | Database username |
| `GRAPHQL_API_KEY` | API key for GraphQL endpoint |
| `GRAPHQL_ENDPOINT` | GraphQL endpoint URL |
| `QA_DATABASE_URL` | Database URL for the QA environment |
| `TF_VAR_PRIVATE_SUBNET_IDS` | JSON array of private subnet IDs (e.g., `["subnet-1234", "subnet-5678"]`) |
| `TF_VAR_PUBLIC_SUBNET_IDS` | JSON array of public subnet IDs (e.g., `["subnet-abcd", "subnet-efgh"]`) |

### Workflow Triggers

The workflow is triggered automatically on:

- Push to the `dev` branch (deploys to dev environment)
- Push to the `qa` branch (deploys to qa environment)

## Resource Naming

All resources are created with a consistent naming convention:
```
${project_name}-${team_name}-${workspace_name}
```

For example, in the 'dev' workspace, resources would be named: `IBE-rizzlers-dev-*`

## Required Variables

Before applying, make sure to set actual values for the following variables in the appropriate tfvars files:

- `public_subnet_ids`: List of public subnet IDs
- `private_subnet_ids`: List of private subnet IDs

These should be actual subnet IDs from your VPC in the specified AWS region. 