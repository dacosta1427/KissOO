# Experience Log

## Current State
- Active Branch: remodelCleaners2
- Current Iteration: 10
- Last Updated: 2026-04-24

## Active Context
|- Primary Task: Verify OO navigation implementation complete
|- Blocking Issues: None
|- Open Questions: None
|- Next Actions: [x] Verify Owner.getHouses()
|- Next Actions: [x] Verify Cleaner.getSchedules()
|- Next Actions: [x] Verify Admin sees all
|- Next Actions: [ ] Run full integration tests

- Task: Fix mobile Navbar shows all links regardless of role
- Decisions: Added role-based menu conditions to mobile menu (same as desktop)
- Outcome: success
- Changes: Navbar.svelte mobile section
- Reflections: Mobile menu was missing role checks

### Iteration 5 - 2026-04-07 16:30
- Task: Fix Schedule missing getBooking, House requires owner
- Decisions: Added getBooking/setBooking to Schedule, constructor validates owner required
- Outcome: success
- Changes: Schedule.java, House.java
- Reflections: House MUST have owner - business rule enforced

### Iteration 6 - 2026-04-07 17:00
- Task: Add collection ownership methods to domain classes
- Problem: Cleaner has getSchedules() but NO addSchedule() - incomplete OO
- Decisions: Add collection management methods to OWN the relationships:
  - Cleaner: addSchedule(Schedule), removeSchedule(Schedule)
  - Owner: addHouse(House), addBooking(Booking)
  - House: addBooking(Booking)
- Rationale: Proper OO encapsulation - container object must manage its own collections
- Outcome: completed
- Changes: Cleaner.java, Owner.java, House.java

### Iteration 7 - 2026-04-07 18:30
- Task: Fix LoadTestdata + analyze Svelte 5 routes
- Problem: LoadTestdata ERROR - "No such property: shuffledHouses"
- Problem: Svelte 5 routes MISSING [id] dynamic segments
- Decisions: 
  1. Fix LoadTestdata (remove broken references)
  2. Add dynamic routes /owners/[id], /houses/[id], etc.
- Rationale: Svelte 5 uses /[id] for object-specific pages - enables URL state
- Outcome: in_progress
- Missing: ALL routes use flat structure with internal state, no URL params

## Iteration History

### Iteration 1 - 2026-04-07 14:03
- Task: Setup protocol structure and create plan
- Decisions: Copied .memory from ~/Documents/KissOO-AI, created .currentPLAN/, REQUESTS/, templates
- Outcome: success
- Changes: Created .memory/, .currentPLAN/, REQUESTS/templates/
- Commits: N/A (not committed yet)
- Reflections: Protocol structure is now in place

### Iteration 2 - 2026-04-07 14:10
- Task: Add OO collection methods to domain classes
- Decisions: Added getHouses(), getBookings(), getSchedules() to Owner, Cleaner, House
- Outcome: success
- Changes: Owner.java, Cleaner.java, House.java
- Commits: 0c24056f
- Reflections: Domain classes use lazy iteration, not stored collections

### Iteration 3 - 2026-04-07 14:20
- Task: Refactor Cleaning.groovy to use OO methods
- Decisions: Use owner.getHouses() instead of load-all + filter
- Outcome: success
- Changes: Cleaning.groovy
- Commits: 0c24056f
- Reflections: Admin still uses load-all (correct), Owner/Cleaner use OO methods

## Pending Validations
- [x] Owner.getHouses() returns only owner's houses
- [x] Cleaner.getSchedules() returns only cleaner's schedules
- [x] Admin still sees all

### Iteration 8 - 2026-04-07 22:30
- Task: Analyze missing navbar translations + broken i18n
- Problem: Navbar shows placeholder keys like "nav.home" instead of "Home"
- Problem: Navbar shows plain text, not styled buttons
- Problem: Browser shows different styling between branches despite identical code
- Root Cause Found: Commit c1d3a507 CORRUPTED en.json - replaced "nav" section with "common" instead of adding both
- Investigation: Compared branches, found c1d3a507 removed entire nav object
- Resolution: Copied working en.json from cleaners2, rebuilt frontend
- Outcome: success
- Changes: en.json restored, pushed to origin
- Reflections: ALWAYS validate JSON after editing - human error replaced instead of merged
- Commits: 3b40a845

### Iteration 10 - 2026-04-24 20:00
|- Task: Verify Pure OO navigation implementation
|- Decisions: Verified all domain classes have OO collection methods
|- Decisions: Verified all service methods use OO navigation for user flows
|- Outcome: success
|- Changes: Verified Owner, Cleaner, House, Booking, Schedule methods
|- Commits: 0c24056f (Pure OO refactor), c47590d4 (precompiles migration)
|- Reflections: Pure OO principle correctly implemented - object.getRelated() not filter-by-ID

## Escalations
None

### Iteration 9 - 2026-04-08 14:30
- Task: Fix owner houses disappearing after clicking update then navigating back
- Problem: Houses disappear after clicking Update Owner then Back then re-clicking
- Root Cause: Perst versioning creates new OID, list showed stale OIDs
- Investigation: Found getOwners() returns OIDs, clicking different OID shows 0 houses
- Solution: 
  1. Frontend: Gray-out Update button when no changes (prevents unnecessary versioning)
  2. Backend: Skip storage if no delta detected (hasChanges check)
  3. Frontend: Navigate to new OID after successful update if OID changed
- Outcome: in progress
- Changes: owners/[id]/+page.svelte (hasChanges derived, button disabled), Cleaning.groovy (delta check)
- Commits: bed577f0
- Reflections: Perst versioning is CORRECT behavior - client must handle new OIDs
### Iteration 11 - 2026-04-25 12:25
- Task: Plan Svelte 5 dynamic routes implementation for all entity types
- Problem: Only `owners/[id]` uses dynamic routes; other entities (houses, bookings, cleaners, schedules) use flat list-only structure with state-based navigation
- Impact: Browser navigation broken, URLs not bookmarkable, navbar doesn't highlight correctly on detail pages
- Decision: Create comprehensive plan per protocol (READ-ACT-WRITE) before implementation
- Rationale: PIT-001 (Missing Svelte 5 Dynamic Route Segments) affects 4/5 entity types
- Action: Created FEAT_svelte5-dynamic-routes.md in .currentPLAN/ with detailed 6-phase plan
- Next: Create feature branch and begin Phase 1 implementation
- Outcome: plan_created
- Changes: .currentPLAN/FEAT_svelte5-dynamic-routes.md, todo_*.md, progress_*.md
- Commits: none (planning phase)
- Reflections: Following protocol - thorough planning prevents rework. Owner pattern serves as proven reference implementation.

### Iteration 12 - 2026-04-25 13:30
- Task: Create detailed plan for Svelte 5 dynamic routes + navbar fixes (PIT-001)
- Problem: Navbar uses <a href> (causes page reloads) + DEBUG span visible in production
- Solution: Convert all navbar links to goto(); remove debug span; create [id] routes for all entities
- Changes: 
  - .currentPLAN/FEAT_svelte5-dynamic-routes-navbar.md (comprehensive plan)
  - .currentPLAN/todo_*.md, progress_*.md tracking files
- Next: Execute Phase 0 (navbar fixes) immediately
- Outcome: plan_created
- Commits: none (planning)
- Reflections: Navbar critical path - must fix before dynamic routes

### Iteration 13 - 2026-04-25 14:00
- Task: Execute Phase 0 - Navbar fixes (remove debug, goto() navigation)
- Problem: Navbar uses <a href> (page reloads) + DEBUG span in production
- Solution: Converted all nav links to use goto(); removed debug span
- Changes: src/main/frontend-svelte/src/lib/components/Navbar.svelte
  - Added import { goto } from '$app/navigation'
  - Added navigateTo(path) helper function
  - Replaced all <a href> with <button onclick={() => navigateTo(path)}>
  - Removed DEBUG span exposing session state
- Outcome: success - Navbar now uses client-side SPA navigation
- Commits: none (in progress)
- Reflections: Major improvement - no page reloads on navigation

### Iteration 14 - 2026-04-25 14:15
- Task: Complete Phase 0 - Navbar fixes, commit and tag
- Problem: Navbar critical issues (debug span, page reloads)
- Solution: 
  - Removed DEBUG span from Navbar.svelte
  - Added goto() navigation, replaced all <a href>
  - Created comprehensive Phase 0-5 implementation plan
- Changes: 
  - src/main/frontend-svelte/src/lib/components/Navbar.svelte (77+ lines modified)
  - .currentPLAN/FEAT_svelte5-dynamic-routes-navbar.md (plan)
  - .currentPLAN/todo_*.md (37 tasks)
  - .currentPLAN/progress_*.md (tracker)
- Commits: dd2ddb93 feat: Svelte 5 dynamic routes + goto() navbar
- Tags: v1.1.1-20260425
- Outcome: Complete - Phase 0 done, ready for Phase 1 (dynamic routes)
- Reflections: Navbar now performs correctly, no page reloads, no debug leaks
