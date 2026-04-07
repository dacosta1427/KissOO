# Todo: Pure OO Refactoring

## Tasks

### Phase 1: Add OO Collection Methods to Domain Classes
- [x] Task 1.1: Add Owner.getHouses() - iterate all houses, filter by this Owner
- [x] Task 1.2: Add Owner.getBookings() - from houses → bookings
- [x] Task 1.3: Add Cleaner.getSchedules() - iterate all schedules, filter by this Cleaner

### Phase 2: Refactor Backend Services (Cleaning.groovy)
- [x] Task 2.1: Refactor getBookings() - use owner.getBookings() for Owner, keep load-all for Admin
- [x] Task 2.2: Refactor getHouses() - use owner.getHouses() for Owner, keep load-all for Admin
- [x] Task 2.3: Refactor getSchedules() - use cleaner.getSchedules() for Cleaner, keep load-all for Admin
- [x] Task 2.4: Refactor getBookingsByHouse() - verify house ownership first
- [x] Task 2.5: Refactor getSchedulesByCleaner() - verify cleaner ownership first

### Phase 3: Testing
- [ ] Task 3.1: Verify Owner login sees only their houses
- [ ] Task 3.2: Verify Cleaner login sees only their schedules
- [ ] Task 3.3: Verify Admin still sees all