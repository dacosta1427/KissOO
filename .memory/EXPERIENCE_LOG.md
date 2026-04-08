# Experience Log

## Current State
- Active Branch: feat/pure-oo-refactor
- Current Iteration: 9
- Last Updated: 2026-04-08

## Active Context
- Primary Task: Fix owner OID shift after update + prevent unnecessary versioning
- Blocking Issues: RESOLVED - now handle OID changes properly
- Open Questions: None
- Next Actions: [ ] Test full flow with real changes

## Iteration 4 - 2026-04-07 14:35
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
- [ ] Owner.getHouses() returns only owner's houses
- [ ] Cleaner.getSchedules() returns only cleaner's schedules
- [ ] Admin still sees all

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