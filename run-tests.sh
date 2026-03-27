#!/bin/bash
# Quick test script for KissOO backend
# Run this after starting the backend server

echo "=== KissOO Quick Test ==="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/rest"

# Test 1: Login as admin
echo -e "${YELLOW}Test 1: Admin Login${NC}"
LOGIN_RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"_class":"","_method":"Login","username":"admin","password":"admin"}')

if echo "$LOGIN_RESULT" | grep -q '"_Success":true'; then
    echo -e "${GREEN}✓ Admin login successful${NC}"
    UUID=$(echo "$LOGIN_RESULT" | grep -o '"uuid":"[^"]*"' | cut -d'"' -f4)
    echo "  UUID: $UUID"
else
    echo -e "${RED}✗ Admin login failed${NC}"
    echo "  Response: $LOGIN_RESULT"
    exit 1
fi

# Test 2: Get Houses
echo ""
echo -e "${YELLOW}Test 2: Get Houses${NC}"
HOUSES_RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Cleaning\",\"_method\":\"getHouses\",\"_uuid\":\"$UUID\"}")

if echo "$HOUSES_RESULT" | grep -q '"_Success":true'; then
    HOUSE_COUNT=$(echo "$HOUSES_RESULT" | grep -o '"id":' | wc -l)
    echo -e "${GREEN}✓ Get Houses successful${NC}"
    echo "  Found $HOUSE_COUNT houses"
else
    echo -e "${RED}✗ Get Houses failed${NC}"
    echo "  Response: $HOUSES_RESULT"
fi

# Test 3: Get Cost Profiles
echo ""
echo -e "${YELLOW}Test 3: Get Cost Profiles${NC}"
COST_RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Cleaning\",\"_method\":\"getCostProfiles\",\"_uuid\":\"$UUID\"}")

if echo "$COST_RESULT" | grep -q '"_Success":true'; then
    PROFILE_COUNT=$(echo "$COST_RESULT" | grep -o '"id":' | wc -l)
    echo -e "${GREEN}✓ Get Cost Profiles successful${NC}"
    echo "  Found $PROFILE_COUNT cost profiles"
else
    echo -e "${RED}✗ Get Cost Profiles failed${NC}"
    echo "  Response: $COST_RESULT"
fi

# Test 4: Get Standard Cost Profile
echo ""
echo -e "${YELLOW}Test 4: Get Standard Cost Profile${NC}"
STANDARD_RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Cleaning\",\"_method\":\"getStandardCostProfile\",\"_uuid\":\"$UUID\"}")

if echo "$STANDARD_RESULT" | grep -q '"_Success":true'; then
    echo -e "${GREEN}✓ Get Standard Cost Profile successful${NC}"
    echo "  Response: $STANDARD_RESULT" | head -c 200
    echo "..."
else
    echo -e "${RED}✗ Get Standard Cost Profile failed${NC}"
fi

# Test 5: Get Owners
echo ""
echo -e "${YELLOW}Test 5: Get Owners${NC}"
OWNERS_RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Cleaning\",\"_method\":\"getOwners\",\"_uuid\":\"$UUID\"}")

if echo "$OWNERS_RESULT" | grep -q '"_Success":true'; then
    OWNER_COUNT=$(echo "$OWNERS_RESULT" | grep -o '"id":' | wc -l)
    echo -e "${GREEN}✓ Get Owners successful${NC}"
    echo "  Found $OWNER_COUNT owners"
else
    echo -e "${RED}✗ Get Owners failed${NC}"
fi

# Test 6: Get Cleaners
echo ""
echo -e "${YELLOW}Test 6: Get Cleaners${NC}"
CLEANERS_RESULT=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Cleaning\",\"_method\":\"getCleaners\",\"_uuid\":\"$UUID\"}")

if echo "$CLEANERS_RESULT" | grep -q '"_Success":true'; then
    CLEANER_COUNT=$(echo "$CLEANERS_RESULT" | grep -o '"id":' | wc -l)
    echo -e "${GREEN}✓ Get Cleaners successful${NC}"
    echo "  Found $CLEANER_COUNT cleaners"
else
    echo -e "${RED}✗ Get Cleaners failed${NC}"
fi

# Test 7: Calculate Cost (if house exists)
echo ""
echo -e "${YELLOW}Test 7: Calculate Cost${NC}"
if [ "$HOUSE_COUNT" -gt 0 ]; then
    # Get first house ID
    HOUSE_ID=$(echo "$HOUSES_RESULT" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "  Using house ID: $HOUSE_ID"
    
    COST_CALC=$(curl -s -X POST "$BASE_URL" \
      -H "Content-Type: application/json" \
      -d "{\"_class\":\"services.Cleaning\",\"_method\":\"calculateCost\",\"_uuid\":\"$UUID\",\"house_id\":$HOUSE_ID}")
    
    if echo "$COST_CALC" | grep -q '"_Success":true'; then
        echo -e "${GREEN}✓ Calculate Cost successful${NC}"
        TOTAL=$(echo "$COST_CALC" | grep -o '"total":[0-9.]*' | cut -d':' -f2)
        echo "  Estimated cost: €$TOTAL"
    else
        echo -e "${RED}✗ Calculate Cost failed${NC}"
    fi
else
    echo -e "${YELLOW}⊘ No houses to test cost calculation${NC}"
fi

echo ""
echo -e "${GREEN}=== Test Complete ===${NC}"
echo ""
echo "Next steps:"
echo "1. Open browser to http://localhost:5173"
echo "2. Login as admin/admin"
echo "3. Click 'Load Test Data' if needed"
echo "4. Test each page in the TEST_PLAN.md"
