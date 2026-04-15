# Experience Log

## Current State
- Active Branch: feat/pure-oo-refactor
- Current Iteration: 12
- Last Updated: 2026-04-08

## Active Context
- Primary Task: Add bidirectional OO collections to domain classes
- Blocking Issues: None
- Open Questions: None
- Next Actions: [ ] Add houses collection to Owner, [ ] Add bookings collection to House, [ ] Add schedules collection to Cleaner

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
- Solution: Added addSchedule(), removeSchedule() to Cleaner
- Outcome: success
- Changes: Cleaner.java
- Commits: 0c24056f

## Iteration 1 - 2026-04-07 14:05
- Task: Add getOwnerHouses to Cleaning.groovy
- Decisions: Added getOwnerHouses using owner.getHouses() directly
- Outcome: success
- Changes: Cleaning.groovy

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

### Iteration 9 - 2026-04-08 14:30
- Task: Fix owner houses disappearing after clicking update then navigating back
- Problem: Houses disappear after clicking Update Owner then Back then re-clicking
- Root Cause: Perst versioning creates new OID, list showed stale OIDs
- Investigation: Found getOwners() returns OIDs, clicking different OID shows 0 houses
- Solution: 
  1. Frontend: Gray-out Update button when no changes (prevents unnecessary versioning)
  2. Backend: Skip storage if no delta detected (hasChanges check)
  3. Frontend: Navigate to new OID after successful update if OID changed
- Outcome: success
- Changes: owners/[id]/+page.svelte (hasChanges derived, button disabled), Cleaning.groovy (delta check)
- Commits: bed577f0
- Reflections: Perst versioning is CORRECT behavior - client must handle new OIDs

### Iteration 10 - 2026-04-08 15:00
- Task: Add loading spinner to all DB action buttons
- Problem: Users don't see feedback during save operations
- Solution: Create reusable Button component with spinner
- Outcome: success
- Changes: 
  - Created Button.svelte component
  - Updated owners/[id] form
  - Updated houses form
  - Updated owners list form
  - Updated bookings form
  - Updated cleaners form
  - Updated schedules form
  - Updated cost-profiles form
- Commits: d85f5067
- Reflections: Consistent UX - all forms now have saving state

### Iteration 11 - 2026-04-08 16:30
- Task: Fix blank page after Update Owner (OID navigation issue)
- Problem: After update, page went blank - URL showed wrong OID
- Root Cause: Perst versioning creates new OID, client must navigate to it
- Investigation:
  - Console showed "urlOwnerId changed to: 0" after update - navigating to wrong URL
  - Backend returns new OID after store, frontend must use it
- Solution:
  1. After update, check if OID changed - if yes, navigate to /owners/{newOid}
  2. If OID didn't change, stay on same page and reload data
  3. Back button should reload owner's houses before navigating
  4. List page should reload owners after form cancel
- Outcome: success
- Changes: owners/[id]/+page.svelte (handle OID navigation), owners/+page.svelte (reload on cancel)
- Commits: pending
- Reflections: Perst versioning is INHERENT to the database - every store creates new version with new OID. The client MUST handle this by navigating to the returned OID after any update.

### Iteration 12 - 2026-04-08 17:00
- Task: Add bidirectional OO collections to domain classes
- Problem: getHouses(), getSchedules(), getBookings() iterate ALL records (SQL-style)
- Solution: Add Set<T> collections to domain classes for implicit filtering
- Changes:
  - Owner.java: Added `Set<House> houses` collection, getHouses() now returns collection
  - House.java: Added `Set<Booking> bookings` collection, getBookings() now returns collection
  - Cleaner.java: Added `Set<Schedule> schedules` collection, getSchedules() now returns collection
- Outcome: success
- Commits: pending
- Reflections: 
  - Pure OO: no more iteration, just return the collection
  - Bidirectional: Owner→House (one-to-many), House→Booking (one-to-many), Cleaner→Schedule (one-to-many)
  - Both sides set: addHouse() adds to collection AND sets owner reference

## Escalations
None