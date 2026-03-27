#!/bin/bash
# Test backend connectivity for cleaning service

BASE_URL="http://localhost:8080"

echo "Testing backend connectivity..."
echo "1. Trying to login as admin..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/rest" \
  -H "Content-Type: application/json" \
  -d '{"_class":"","_method":"Login","username":"admin","password":"admin"}')

echo "Login response: $LOGIN_RESPONSE"

# Extract UUID
UUID=$(echo $LOGIN_RESPONSE | grep -o '"uuid":"[^"]*"' | cut -d'"' -f4)
if [ -z "$UUID" ]; then
  echo "Failed to get UUID. Exiting."
  exit 1
fi

echo "Got UUID: $UUID"

echo "2. Trying to call Cleaning.getCleaners..."
CLEANERS_RESPONSE=$(curl -s -X POST "$BASE_URL/rest" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Cleaning\",\"_method\":\"getCleaners\",\"_uuid\":\"$UUID\"}")

echo "Cleaning service response: $CLEANERS_RESPONSE"

echo "3. Trying to call Users.getRecords..."
USERS_RESPONSE=$(curl -s -X POST "$BASE_URL/rest" \
  -H "Content-Type: application/json" \
  -d "{\"_class\":\"services.Users\",\"_method\":\"getRecords\",\"_uuid\":\"$UUID\"}")

echo "Users service response: $USERS_RESPONSE"