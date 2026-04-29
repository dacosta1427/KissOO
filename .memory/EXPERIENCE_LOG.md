
### Iteration 17 - 2026-04-29
- Task: Fix service signatures and finalize login implementation
- Problem: Java Login.java had wrong signature - used `(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet)`
- Fix: Changed signature to correct framework pattern: `(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)`
- Changes made:
  1. ✅ Fixed `src/main/precompiled/koo/services/Login.java` - now uses correct signature, extracts username/password from injson
  2. ✅ Updated `src/main/backend/Login.groovy` - added checkLogin() method, improved documentation
  3. ✅ All Pure-OO authorization in place - getHouse, getBooking, getSchedule use `actor.getHouses().contains(item)` pattern
  4. ✅ Manager classes implemented - all domain objects have *Manager classes
  5. ✅ OO collection methods - Owner.getHouses(), Owner.getBookings(), Cleaner.getSchedules() all working
- Status: FIXED - Login service now follows Kiss framework pattern
- Next: Continue with dynamic route implementation (edit-in-place instead of separate detail pages)

### Iteration 16 - 2026-04-29
- Task: Fix service signatures (Java Login.java needs to match Groovy pattern)
- Problem: Java Login.java has wrong signature - uses `(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet)`
  instead of correct framework pattern: `(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)`
- Root Cause: The Groovy pattern IS the leading pattern for all services (see CleaningService.groovy). 
  Java version was incorrectly implemented with separate String parameters instead of extracting from injson.
- Solution (planned):
  1. Fix Java Login.java: change signature to `(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)`
  2. Extract username/password from injson: `injson.getString("username")`
  3. Remove separate user/password String parameters
  4. Fix/delete Login.groovy which also has wrong signature
  5. CleaningService.groovy already correct (signatures + instanceof authorization + Manager refactoring)
- Documents created:
  - `.currentPLAN/PLAN_fix-service-signatures.md` (plan with corrected code)
  - `.currentPLAN/todo_fix-service-signatures.md` (task list)
  - `.currentPLAN/progress_fix-service-signatures.md` (progress tracker)
- Status: FIXED - see Iteration 17
- Next: Execute tasks from todo list after approval

### Iteration 15 - 2026-04-26
- Task: Execute Phase 0-0.5 - Navbar fixes + Pure OO authorization
- Problem: Navbar uses <a href> causing page reloads, DEBUG span in production, backend detail endpoints lack OO authorization
- Solution:
  1. Navbar: Removed DEBUG span, converted all <a href> to goto() navigation
  2. Backend: Added Pure OO authorization to getHouse, getBooking, getSchedule, getOwner
     - Uses actor.getHouses().contains(house) pattern
     - Admin bypass via role check
     - Returns proper error codes (403 unauthorized, 404 not found)
- Changes:
  - src/main/frontend-svelte/src/lib/components/Navbar.svelte
  - src/main/backend/services/CleaningService.groovy (4 detail methods)
  - Added Actor import
- Outcome: Navbar SPA behavior fixed, Pure OO authorization enforced
- Commits: dd2ddb93, b0ab81dd
- Tags: v1.1.1-20260425, v1.2-20260425-sv5-routes
- Next: Continue with remaining phases for complete dynamic route implementation
