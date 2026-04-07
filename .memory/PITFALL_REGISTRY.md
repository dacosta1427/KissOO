# Pitfall Registry

## Active Pitfalls

### PIT-001: Missing Svelte 5 Dynamic Route Segments
- **Category:** DESIGN
- **First Seen:** 2026-04-07
- **Last Updated:** 2026-04-07
- **Occurrences:** 5 (owners, houses, bookings, cleaners, schedules)

**Context:** Navigation between list and detail views in Svelte 5

**Symptom:** 
- Owner detail uses internal state (`editingOwner`), NOT URL
- Click "Owners" in navbar when on owner detail → goes to same URL, no change
- Error: "No such property: shuffledHouses" in LoadTestdata

**Root Cause (5 Whys):**
1. Why? → Routes are ALL flat (`/owners`, `/houses`) with no dynamic segments
2. Why? → Detail view uses internal state (`openEditForm()`), not URL params
3. Why? → Svelte 5 [id] notation NOT used for any route
4. Why? → Developers assumed state-based navigation
5. Why? → Not following Svelte 5 routing pattern

**Resolution:** Create dynamic routes:
- /owners/[ownerId] - owner detail
- /houses/[houseId] - house detail
- /bookings/[bookingId] - booking detail
- /cleaners/[cleanerId] - cleaner detail

**PIT-002: Svelte 5 goto() Not Working** [RESOLVED]
- **Category:** INTEGRATION
- **First Seen:** 2026-04-07
- **Last Updated:** 2026-04-07
- **Occurrences:** 1
- **Status:** RESOLVED - goto() works correctly

**Context:** Clicking owner in list should navigate to detail page

**Resolution:** Works now - verified:
- openEditForm() calls goto('/owners/' + owner.id)
- URL changes to /owners/123
- Detail page loads correctly

**Prevention:** Standard Svelte 5 goto() for navigation

**Related Classes:** Navbar.svelte, owners/+page.svelte, houses/+page.svelte

**Evidence:** Click owner → URL stays /owners, no ownerId in path

## Archived Pitfalls
<resolved entries moved here after 10 iterations>

## Category Definitions

| Category | Description | Examples |
|----------|-------------|----------|
| SYNTAX | Compilation errors, typos | Missing semicolons, wrong types |
| DESIGN | OO principle violations | God class, tight coupling |
| LOGIC | Incorrect algorithms | Wrong loop bounds, off-by-one |
| INTEGRATION | Component interaction | API mismatches, version conflicts |
| ASSUMPTION | Wrong understanding | Misread docs, assumed behavior |