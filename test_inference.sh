#!/bin/bash
# Test script for ML Inference Service

echo "================================"
echo "PathoVision ML Service Test"
echo "================================"
echo ""

# Configuration
ML_URL="http://localhost:5000"
BACKEND_URL="http://localhost:3001"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Flask Health Check
echo -e "${YELLOW}Test 1: Flask Health Check${NC}"
curl -s ${ML_URL}/health | python -m json.tool
echo ""

# Test 2: Model Info
echo -e "${YELLOW}Test 2: Model Information${NC}"
curl -s ${ML_URL}/model-info | python -m json.tool
echo ""

# Test 3: Backend Health
echo -e "${YELLOW}Test 3: Backend Health Check${NC}"
curl -s ${BACKEND_URL}/api/ml/health | python -m json.tool
echo ""

# Test 4: Single Image Prediction (if test image exists)
if [ -f "test_sample.png" ]; then
    echo -e "${YELLOW}Test 4: Single Image Prediction (Direct to Flask)${NC}"
    curl -s -X POST -F "image=@test_sample.png" ${ML_URL}/predict | python -m json.tool
    echo ""
    
    echo -e "${YELLOW}Test 5: Single Image Prediction (via Backend)${NC}"
    curl -s -X POST -F "image=@test_sample.png" ${BACKEND_URL}/api/ml/predict | python -m json.tool
    echo ""
else
    echo -e "${RED}✗ test_sample.png not found - skipping prediction tests${NC}"
    echo "  To test predictions, place a histopathology image as 'test_sample.png'"
fi

echo -e "${YELLOW}================================${NC}"
echo "Test completed!"
echo ""
echo -e "${GREEN}Services status:${NC}"
echo "  Flask ML Service: ${ML_URL}"
echo "  Node.js Backend: ${BACKEND_URL}"
