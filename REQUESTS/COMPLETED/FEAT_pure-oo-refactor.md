# Plan: Pure OO Refactoring - Remove SQL-like Load-All-Then-Filter

## Problem

Backend services use SQL-thinking pattern:
```groovy
// BAD: Load ALL + filter  
Collection<Booking> allBookings = PerstStorageManager.getAll(Booking.class)
Collection<Booking> bookings = allBookings.findAll { it.getHouseOid() == houseId }
```

This loads ALL records then filters in memory - works for admin but violates Pure OO principle when a specific Owner/Cleaner is logged in.

## Expected Behavior

- **Owner logged in** → `owner.getHouses()` returns only their houses (via OO navigation)
- **Cleaner logged in** → `cleaner.getSchedules()` returns only their schedules
- **Admin** → Load all + filter is valid (admin needs visibility)

## Current State

### Existing OO References (Good)
- House.owner (Owner reference) ✓
- Booking.house (House reference) ✓
- Schedule.cleaner (Cleaner reference) ✓
- Schedule.booking (Booking reference) ✓

### Missing Collection Methods (Bad)
- Owner.getHouses() - NO collection on Owner
- Owner.getBookings() - NO collection on Owner  
- Cleaner.getSchedules() - NO collection on Cleaner

## Tasks

### Phase 1: Add OO Collection Methods to Domain Classes
- [ ] Add Owner.getHouses() - iterate all houses, filter by this Owner
- [ ] Add Owner.getBookings() - from houses → bookings
- [ ] Add Cleaner.getSchedules() - iterate all schedules, filter by this Cleaner

### Phase 2: Refactor Backend Services (Cleaning.groovy)
- [ ] Refactor getBookings() - use owner.getBookings() for Owner, keep load-all for Admin
- [ ] Refactor getHouses() - use owner.getHouses() for Owner, keep load-all for Admin
- [ ] Refactor getSchedules() - use cleaner.getSchedules() for Cleaner, keep load-all for Admin
- [ ] Refactor getBookingsByHouse() - verify house ownership first
- [ ] Refactor getSchedulesByCleaner() - verify cleaner ownership first

### Phase 3: Testing
- [ ] Verify Owner login sees only their houses
- [ ] Verify Cleaner login sees only their schedules
- [ ] Verify Admin still sees all

## Files to Modify

1. `src/main/precompiled/mycompany/domain/Owner.java`
2. `src/main/precompiled/mycompany/domain/Cleaner.java`
3. `src/main/backend/services/Cleaning.groovy`

## Notes

- Admin path kept as-is (load all + filter is appropriate)
- Services check user role first, then decide which method to use
- Domain classes use lazy iteration (not stored collections) for simplicity