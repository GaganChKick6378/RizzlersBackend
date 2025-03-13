#!/bin/bash

echo "Testing API endpoints..."

echo -e "\n1. Testing root endpoint:"
curl -s https://38cl2iacig.execute-api.ap-south-1.amazonaws.com/dev/

echo -e "\n2. Testing students endpoint (no context path):"
curl -s https://38cl2iacig.execute-api.ap-south-1.amazonaws.com/dev/students

echo -e "\n3. Testing students endpoint (with context path):"
curl -s https://38cl2iacig.execute-api.ap-south-1.amazonaws.com/dev/api/students

echo -e "\n4. Testing health endpoint:"
curl -s https://38cl2iacig.execute-api.ap-south-1.amazonaws.com/dev/api/health

echo -e "\n5. Testing health endpoint (no context path):"
curl -s https://38cl2iacig.execute-api.ap-south-1.amazonaws.com/dev/health

echo -e "\nTesting complete!" 