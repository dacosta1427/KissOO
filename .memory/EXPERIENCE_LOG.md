
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
