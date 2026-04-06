#!/usr/bin/env bash
set -euo pipefail

LOGDIR="/tmp/kiss_tests2"
mkdir -p "$LOGDIR"

echo "[TEST] Starting end-to-end PerstUser sync test (robust)" | tee -a "$LOGDIR/run.log"

wait_for_port() {
  local host="${1:-localhost}"
  local port="${2}"
  local timeout_ms="${3:-120000}"
  local waited=0
  local interval=1000
  while ! socat - TCP:localhost:${port} >/dev/null 2>&1; do
    if (( waited >= timeout_ms )); then
      echo "[TEST] timeout waiting for ${host}:${port}" | tee -a "$LOGDIR/run.log"; return 1
    fi
    sleep 1
    waited=$((waited + interval))
  done
  return 0
}

echo "[TEST] Starting back-end (Tomcat) and front-end..."
pkill -f 'tomcat' || true
pkill -f 'vite' || true
sleep 2

cd /home/dacosta/Projects/KissOO
./bld develop > /tmp/backend.log 2>&1 &
BE_PID=$!
echo "[TEST] Backend PID ${BE_PID}" | tee -a "$LOGDIR/run.log"

cd /home/dacosta/Projects/KissOO/src/main/frontend-svelte
pkill -f 'vite' || true
npm run dev -- --port 5173 > /tmp/frontend.log 2>&1 &
FE_PID=$!
echo "[TEST] Frontend PID ${FE_PID}" | tee -a "$LOGDIR/run.log"

echo "[TEST] Waiting for backend/frontend to boot..." | tee -a "$LOGDIR/run.log"
sleep 25

echo "[TEST] Seed Owner and Cleaner" | tee -a "$LOGDIR/run.log"
OWNER_JSON='{"_class":"services.Cleaning","_method":"createOwner","_uuid":"test","data":{"name":"OwnerSync","email":"owner.sync@example.com","phone":"","address":"Sync"}}'
RESP_OWNER=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$OWNER_JSON")

CLEANER_JSON='{"_class":"services.Cleaning","_method":"createCleaner","_uuid":"test","data":{"name":"CleanerSync","phone":"","email":"cleaner.sync@example.com","address":"","active":true}}'
RESP_CLEANER=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$CLEANER_JSON")

echo "Owner seed: $RESP_OWNER" | tee -a "$LOGDIR/run.log"
echo "Cleaner seed: $RESP_CLEANER" | tee -a "$LOGDIR/run.log"

echo "[TEST] Pulling data..." | tee -a "$LOGDIR/run.log"
USERS_JSON='{"_class":"services.Users","_method":"getUsers","_uuid":"test"}'
RESP_USERS=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$USERS_JSON")
echo "Users: $RESP_USERS" | tee -a "$LOGDIR/run.log"

CLEANERS_JSON='{"_class":"services.Cleaning","_method":"getCleaners","_uuid":"test"}'
RESP_CLEANERS=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$CLEANERS_JSON")
echo "Cleaners: $RESP_CLEANERS" | tee -a "$LOGDIR/run.log"

OWNERS_JSON='{"_class":"services.Cleaning","_method":"getOwners","_uuid":"test"}'
RESP_OWNERS=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$OWNERS_JSON")
echo "Owners: $RESP_OWNERS" | tee -a "$LOGDIR/run.log"

echo "[TEST] End-to-end data check complete." | tee -a "$LOGDIR/run.log"
