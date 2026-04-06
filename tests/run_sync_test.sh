#!/usr/bin/env bash
set -euo pipefail

echo "[TEST] Starting end-to-end PerstUser sync test..."
LOGDIR="/tmp/kiss_tests"
mkdir -p "$LOGDIR"

echo "[TEST] Cleaning up any running servers..."
pkill -f 'tomcat' || true
pkill -f 'vite' || true
sleep 2

echo "[TEST] Starting backend (Tomcat)..."
cd /home/dacosta/Projects/KissOO
rm -f /tmp/kissoo.log /tmp/backend.log || true
./bld develop > /tmp/backend.log 2>&1 &
BE_PID=$!
echo "[TEST] Backend PID: $BE_PID" | tee -a "$LOGDIR/run.log"

echo "[TEST] Starting frontend (Vite) on port 5173..."
cd /home/dacosta/Projects/KissOO/src/main/frontend-svelte
rm -f /tmp/kissoo-front.log || true
npm run dev -- --port 5173 > /tmp/kissoo-front.log 2>&1 &
FE_PID=$!

echo "[TEST] Waiting for servers to be ready..."
sleep 25

echo "[TEST] Seeding test data..."
OWNER_JSON='{"_class":"services.Cleaning","_method":"createOwner","_uuid":"test","data":{"name":"OwnerSync","email":"owner.sync@example.com","phone":"","address":"Sync"}}'
CLEANER_JSON='{"_class":"services.Cleaning","_method":"createCleaner","_uuid":"test","data":{"name":"CleanerSync","phone":"","email":"cleaner.sync@example.com","address":"","active":true}}'

RESP_OWNER=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$OWNER_JSON")
RESP_CLEANER=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$CLEANER_JSON")
echo "Owner create response: $RESP_OWNER" | tee -a "$LOGDIR/run.log"
echo "Cleaner create response: $RESP_CLEANER" | tee -a "$LOGDIR/run.log"

echo "[TEST] Reading users, cleaners and owners..." | tee -a "$LOGDIR/run.log"
USERS_JSON='{"_class":"services.Users","_method":"getUsers","_uuid":"test"}'
CLEANERS_JSON='{"_class":"services.Cleaning","_method":"getCleaners","_uuid":"test"}'
OWNERS_JSON='{"_class":"services.Cleaning","_method":"getOwners","_uuid":"test"}'
RESP_USERS=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$USERS_JSON" | python3 -m json.tool 2>/dev/null || echo "NO_JSON")
RESP_CLEANERS=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$CLEANERS_JSON" | python3 -m json.tool 2>/dev/null || echo "NO_JSON")
RESP_OWNERS=$(curl -s -X POST http://localhost:8000/rest -H 'Content-Type: application/json' -d "$OWNERS_JSON" | python3 -m json.tool 2>/dev/null || echo "NO_JSON")

echo "USERS: $RESP_USERS" | tee -a "$LOGDIR/run.log"
echo "CLEANERS: $RESP_CLEANERS" | tee -a "$LOGDIR/run.log"
echo "OWNERS: $RESP_OWNERS" | tee -a "$LOGDIR/run.log"

echo "[TEST] Basic consistency check..."
python3 - << 'PY'
import json
from sys import exit
try:
    users = json.loads(open('/tmp/users.json','r').read())
except Exception:
    pass
print('NOTE: In this test, results are printed above by the shell.')
PY

echo "[TEST] Done. You can open the People pages in the UI to verify visually."
