#!/bin/bash

# This script manages Terraform state to prevent destruction of existing resources
# and properly import them as data sources instead.

echo "Starting state management for QA environment..."

# First, check if we still have the resources in state
HAS_ECS_CLUSTER=$(terraform state list module.ecs.aws_ecs_cluster.app_cluster 2>/dev/null)
HAS_API_GATEWAY=$(terraform state list module.api_gateway.aws_api_gateway_rest_api.api 2>/dev/null) 

# Remove resources from state if they exist (but don't destroy them)
if [ -n "$HAS_ECS_CLUSTER" ]; then
  echo "Removing ECS cluster from state (to prevent destruction)..."
  terraform state rm module.ecs.aws_ecs_cluster.app_cluster || echo "ECS cluster not in state"
fi

if [ -n "$HAS_API_GATEWAY" ]; then
  echo "Removing API Gateway from state (to prevent destruction)..."
  terraform state rm module.api_gateway.aws_api_gateway_rest_api.api || echo "API Gateway not in state"
fi

# Check if data sources are already in state
HAS_ECS_DATA=$(terraform state list module.ecs.data.aws_ecs_cluster.existing_cluster 2>/dev/null)
HAS_API_DATA=$(terraform state list module.api_gateway.data.aws_api_gateway_rest_api.existing_api 2>/dev/null)

# Import the data sources if they're not already in state
if [ -z "$HAS_ECS_DATA" ]; then
  echo "Importing existing ECS cluster as data source..."
  terraform import module.ecs.data.aws_ecs_cluster.existing_cluster rizzlers-cluster || echo "Failed to import ECS cluster - it may not exist yet"
fi

if [ -z "$HAS_API_DATA" ]; then
  echo "Importing existing API Gateway as data source..."
  terraform import module.api_gateway.data.aws_api_gateway_rest_api.existing_api rizzlers-api || echo "Failed to import API Gateway - it may not exist yet"
fi

echo "State management complete. You can now run terraform plan and apply safely."
echo "IMPORTANT: Verify with 'terraform plan' that no resources will be destroyed!" 