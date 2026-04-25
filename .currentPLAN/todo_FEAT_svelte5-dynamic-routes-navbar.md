# Todo: Svelte 5 Dynamic Routes & Navbar Fixes

## Plan Overview
Fix navbar (remove debug, use goto) + implement dynamic routes for all entity types.

**Target Branch:** `feat/sv5-dynamic-routes`  
**Priority:** HIGH  
**Related:** PIT-001, SV5-001

---

## Phase 0: Navbar Fixes [START HERE] ⚠️

### Critical Issues
- [ ] **P0**: Remove DEBUG span from Navbar.svelte (visible in production)
- [ ] **P0**: Convert all nav links from `<a href>` to `goto()` (prevents page reloads)

### Tasks
- [ ] Remove debug span (lines ~50-53 in Navbar.svelte)
- [ ] Import `goto` from `$app/navigation`
- [ ] Create `navigateTo(path)` helper function
- [ ] Replace ALL anchor tags with buttons using `onclick={() => navigateTo(path)}`
- [ ] Apply to: Home, Houses, Owners, Cleaners, Bookings, Schedules, Users, Login, Signup, Logout
- [ ] Close mobile menu after navigation
- [ ] Test role-based navbar logic (Admin, Owner, Cleaner, Logged out)
- [ ] Verify no page reload (Network tab in devtools)

---

## Phase 1: Route Structure

### Houses
- [ ] Create `src/main/frontend-svelte/src/routes/houses/[houseId]/+page.svelte`
- [ ] Move detail logic from `houses/+page.svelte` to detail page
- [ ] Use `$page.params.houseId` to read ID from URL
- [ ] Add `$effect` for reactive data loading

### Bookings  
- [ ] Create `src/main/frontend-svelte/src/routes/bookings/[bookingId]/+page.svelte`
- [ ] Move detail logic from `bookings/+page.svelte`
- [ ] Use `$page.params.bookingId`

### Cleaners
- [ ] Create `src/main/frontend-svelte/src/routes/cleaners/[cleanerId]/+page.svelte`
- [ ] Move detail logic from `cleaners/+page.svelte`
- [ ] Use `$page.params.cleanerId`

### Schedules
- [ ] Create `src/main/frontend-svelte/src/routes/schedules/[scheduleId]/+page.svelte`
- [ ] Move detail logic from `schedules/+page.svelte`
- [ ] Use `$page.params.scheduleId`

---

## Phase 2: Backend Services

### HouseService
- [ ] Create `getById()` method
- [ ] Load house via `PerstStorageManager.getByOid()`
- [ ] Authorize via `actor.getHouses().contains(house)`
- [ ] Return 404 if not found, 403 if unauthorized

### BookingService
- [ ] Create `getById()` method
- [ ] Authorize via `actor.getBookings().contains(booking)`

### CleanerService
- [ ] Create `getById()` method
- [ ] Authorize via `actor.getCleaners()` or role check

### ScheduleService
- [ ] Create `getById()` method
- [ ] Authorize via `actor.getSchedules().contains(schedule)`

---

## Phase 3: List Page Updates

All list pages need to navigate via URL instead of state:

- [ ] `houses/+page.svelte` - Replace `openDetail()` with `goto('/houses/' + id)`
- [ ] `bookings/+page.svelte` - Replace with `goto()`
- [ ] `cleaners/+page.svelte` - Replace with `goto()`
- [ ] `schedules/+page.svelte` - Replace with `goto()`
- [ ] `owners/+page.svelte` - Verify (should already work)

**Remove from list pages:**
- `editingX` state variables
- `showDetail` / `showEditForm` state variables
- Detail form components (moved to detail pages)

---

## Phase 4: Testing

### Browser Navigation
- [ ] Direct URL access: `/houses/123` loads house 123
- [ ] Navigation: Click house → URL changes, back/forward work
- [ ] Navbar: No page reload when clicking links

### Navbar Role Logic
- [ ] System Admin sees all links
- [ ] Owner sees houses, bookings, schedules only
- [ ] Cleaner sees schedules only
- [ ] Logged out sees login/signup only

### Build Quality
- [ ] `npm run check` passes (0 errors)
- [ ] `npm run build` succeeds
- [ ] No debug console.log statements

---

## Phase 5: Cleanup

- [ ] Remove all debug code and console.logs
- [ ] Update `.memory/EXPERIENCE_LOG.md`
- [ ] Update `.memory/BEST_PRACTICES.md`
- [ ] Commit with descriptive message
- [ ] Tag: `v1.2-YYYYMMDD`
- [ ] Push to remote

---

## Priority Order

1. **URGENT**: Remove debug span from navbar
2. **URGENT**: Convert navbar to use `goto()`
3. High: Create dynamic route folders
4. High: Create backend getById services
5. Medium: Update list pages
6. Medium: Testing

---