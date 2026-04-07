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

**PIT-003: JSON Translation File Corruption**
- **Category:** SYNTAX
- **First Seen:** 2026-04-07
- **Last Updated:** 2026-04-07
- **Occurrences:** 1
- **Status:** RESOLVED - en.json restored from cleaners2

**Context:** Navbar shows placeholder keys like "nav.home" instead of "Home"

**Symptom:**
- Translation keys showing as plain text in navbar
- Missing keys: nav.home, nav.houses, nav.owners, nav.cleaners, etc.

**Root Cause (5 Whys):**
1. Why? → en.json missing entire "nav" section
2. Why? → Commit c1d3a507 changed the JSON structure
3. Why? → Developer edited file, replaced "nav": with "common": instead of adding
4. Why? → Edit replacing top-level key instead of merging both sections
5. Why? → No JSON validation after manual edit

**Resolution:** Copied working en.json from cleaners2 branch:
```bash
git show cleaners2:src/main/frontend-svelte/src/lib/i18n/messages/en.json > \
  src/main/frontend-svelte/src/lib/i18n/messages/en.json
```

**Prevention:** ALWAYS validate JSON with `python3 -m json.tool file.json` after editing

**Related Files:** en.json, nl.json, de.json

**Evidence:** git diff showed entire nav object replaced with common

---

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