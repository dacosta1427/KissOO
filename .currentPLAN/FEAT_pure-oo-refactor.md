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

## Completed Phases
- ✅ Phase 1: Add OO Collection Methods to Domain Classes
- ✅ Phase 2: Refactor Backend Services
- ✅ Phase 3: Testing
- ✅ Phase 4: OID Navigation (handle new OIDs after Perst versioning)
- ✅ Phase 5: Email Required (make email mandatory across all forms)
- ✅ Phase 6: Translation Fix (add missing common.name/email/phone/address)
- ✅ Phase 7: Button Component (reusable Button with spinner)
- ✅ Phase 8: Add Bidirectional Collections (Set<T> for implicit filtering)

## Completed Tasks

### Task: Add Loading Spinners to All DB Action Buttons
- [x] Create reusable Button.svelte component
- [x] Update owners/[id] form (edit owner)
- [x] Update houses form (add/edit house)
- [x] Update owners list form (create/edit owner)
- [x] Update bookings form (create/edit booking)
- [x] Update cleaners form (create/edit cleaner)
- [x] Update schedules form (create/edit schedule)
- [x] Update cost-profiles form (create/edit profile)

### Task: Bidirectional OO Collections
- [x] Owner.java - Added `Set<House> houses` collection
- [x] House.java - Added `Set<Booking> bookings` collection
- [x] Cleaner.java - Added `Set<Schedule> schedules` collection

## Notes

- Each form needs `saving` state and `finally` block to reset it
- Use Button component with `loading` prop for spinners
- Gray-out disabled buttons with btn-disabled class
- Collections enable implicit filtering - no more iteration!