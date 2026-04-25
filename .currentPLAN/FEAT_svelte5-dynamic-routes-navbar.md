# FEAT: Svelte 5 Dynamic Routes & Navbar Navigation

## Overview
Implement proper Svelte 5 dynamic route segments (`[id]`) for all entity detail views AND fix navbar to use proper client-side `goto()` navigation instead of page reloads.

**Status:** Planning  
**Priority:** HIGH (PIT-001 + Navbar Issues)  
**Target Branch:** `feat/sv5-dynamic-routes`

---

## Problem Statement

### 1. PIT-001: Missing Svelte 5 Dynamic Route Segments

**Current State:**
- Routes are FLAT (except owners): `/houses`, `/bookings`, `/cleaners`, `/schedules`
- Detail views use INTERNAL STATE instead of URL params
- Clicking navbar links when on detail page → goes to same URL, no navigation occurs
- Browser back/forward buttons don't work correctly
- URLs are not bookmarkable or shareable

**Root Cause:**
- Svelte 5 `[id]` folder notation NOT used (except `owners/[id]`)
- Developers assumed state-based navigation was sufficient

**Affected Entities:**
1. ✅ `owners/[id]` — Dynamic route EXISTS (reference implementation)
2. ❌ `houses/[houseId]` — MISSING (flat list only)
3. ❌ `bookings/[bookingId]` — MISSING (flat list only)
4. ❌ `cleaners/[cleanerId]` — MISSING (flat list only)
5. ❌ `schedules/[scheduleId]` — MISSING (flat list only)

---

### 2. Navbar Issue 1: DEBUG Span Visible in Production

**File:** `Navbar.svelte`
**Problem:**
```html
<!-- DEBUG - Should NEVER be in production -->
<span class="text-xs text-red-500 mr-2">
  isAdmin={session.isAdmin} ownerOid={session.ownerOid} cleanerOid={session.cleanerOid}
</span>
```
**Impact:** Exposes internal session state to end users
**Fix:** Remove this debug span entirely

---

### 3. Navbar Issue 2: Uses `<a href>` Instead of `goto()`

**Problem:** Navbar uses traditional anchor tags:
```html
<a href="/houses" class="...">{tt('nav.houses')}</a>
```

**Impact:**
- ❌ Full page reload on every navigation
- ❌ Poor SPA experience (flash of reload)
- ❌ State gets reset unnecessarily
- ❌ Slower transitions between pages

**Fix:**
```typescript
import { goto } from '$app/navigation';

function navigateTo(path: string) {
  goto(path);
  mobileMenuOpen = false;  // Close mobile menu
}

<button onclick={() => navigateTo('/houses')}>
  {tt('nav.houses')}
</button>
```

**Benefits:**
- ✅ No page reload (proper SPA behavior)
- ✅ Faster navigation
- ✅ State preserved across pages
- ✅ Better UX overall

---

## Solution Architecture

### Reference Implementation: `owners/[id]` (ALREADY WORKING)

Study this pattern for all other entities:
```
src/main/frontend-svelte/src/routes/
└── owners/
    ├── +page.svelte                  # List view
    └── [id]/
        └── +page.svelte              # Detail view (reads $page.params.id)
```

The owners pattern is the gold standard — replicate for houses, bookings, cleaners, schedules.

---

## Implementation Plan

### Phase 0: Navbar Fixes (Day 0.5) — **FIRST PRIORITY**

#### Task 0.1: Remove Debug Span
**File:** `src/main/frontend-svelte/src/lib/components/Navbar.svelte`

```typescript
// REMOVE THIS BLOCK (lines ~50-53)
<!-- DEBUG -->
<span class="text-xs text-red-500 mr-2">
  isAdmin={session.isAdmin} ownerOid={session.ownerOid} cleanerOid={session.cleanerOid}
</span>
```

**Validation:**
- [ ] Debug span removed from desktop navbar
- [ ] Debug span removed from mobile navbar  
- [ ] Navbar still displays username (the `session.username` span below should remain)

---

#### Task 0.2: Convert Nav Links to `goto()`
**File:** `Navbar.svelte`

**Current (broken):**
```html
<a href="/houses" class="text-gray-600 hover:text-gray-900 font-medium">
  {tt('nav.houses')}
</a>
```

**Fixed:**
```typescript
<script lang="ts">
  import { goto } from '$app/navigation';  // ADD THIS IMPORT
  // ... existing code
  
  function navigateTo(path: string) {
    goto(path);
    mobileMenuOpen = false;  // Close mobile menu on navigation
  }
</script>

<!-- Desktop: Replace with button -->
<button onclick={() => navigateTo('/houses')}
        class="text-gray-600 hover:text-gray-900 font-medium">
  {tt('nav.houses')}
</button>

<!-- Mobile: Replace with button -->
<button onclick={() => navigateTo('/houses')}
        class="text-gray-600 hover:text-gray-900 font-medium text-left">
  {tt('nav.houses')}
</button>
```

**Apply to ALL nav links:**
- Home (both logged in/out)
- Houses
- Owners
- Cleaners
- Bookings
- Schedules
- Users
- Login/Signup (logged out)
- Logout button
- LanguageSwitcher (keep as-is, it's already a component)

**Validation:**
- [ ] No `<a href>` tags remain in navbar (except maybe external links)
- [ ] All navigation uses `goto()` or `navigateTo()`
- [ ] Mobile menu closes after navigation
- [ ] No page reload when clicking navbar
- [ ] Role-based menu still works correctly

---

#### Task 0.3: Test Navbar Role Logic
**Verify each role sees correct links:**

| Role | Should See | Should NOT See |
|------|-----------|----------------|
| System Admin | All links (houses, owners, cleaners, bookings, schedules, users) | Nothing |
| Content Admin | All links (same as system admin) | Nothing |
| Owner | Houses, Bookings, Schedules | Owners, Cleaners, Users |
| Cleaner | Schedules only | Everything else |
| Logged Out | Login, Signup | Everything else |

**Test Cases:**
```typescript
// Login as each role, verify navbar shows only correct links
// Desktop view: check all links
// Mobile view: open menu, check all links
// Navigate between pages, verify no reload
// Browser back/forward, works correctly
```

---

### Phase 1: Route Structure Setup (Day 1)

#### Task 1.1: Create Dynamic Route Folders

**For houses:**
```bash
src/main/frontend-svelte/src/routes/
└── houses/
    ├── +page.svelte          # List (keep existing)
    └── [houseId]/            # NEW
        └── +page.svelte      # Detail (NEW)
```

**For bookings:**
```bash
src/main/frontend-svelte/src/routes/
└── bookings/
    ├── +page.svelte          # List (keep existing)
    └── [bookingId]/          # NEW
        └── +page.svelte      # Detail (NEW)
```

**For cleaners:**
```bash
src/main/frontend-svelte/src/routes/
└── cleaners/
    ├── +page.svelte          # List (keep existing)
    └── [cleanerId]/          # NEW
        └── +page.svelte      # Detail (NEW)
```

**For schedules:**
```bash
src/main/frontend-svelte/src/routes/
└── schedules/
    ├── +page.svelte          # List (keep existing)
    └── [scheduleId]/         # NEW
        └── +page.svelte      # Detail (NEW)
```

**Reference:** Copy pattern from `owners/[id]/+page.svelte`

---

#### Task 1.2: Extract Detail Pages from List Pages

**Current (broken):**
`houses/+page.svelte` contains BOTH list AND detail in one file using state variables.

**Fixed:**
- `houses/+page.svelte` → List only (show all houses)
- `houses/[houseId]/+page.svelte` → Detail only (show one house)

**Pattern (from owners):**
```typescript
// Detail page: houses/[houseId]/+page.svelte
<script lang="ts">
  import { page } from '$app/stores';
  import { onMount } from 'svelte';
  
  // Read ID from URL
  let houseId = $derived(parseInt($page.params.houseId));
  let house = $state<House | null>(null);
  let loading = $state(true);
  
  // Load when ID changes
  $effect(() => {
    if (houseId) {
      loadHouse(houseId);
    }
  });
  
  async function loadHouse(id: number) {
    loading = true;
    const res = await Server.call('services.HouseService', 'getById', { houseOid: id });
    house = res.data;
    loading = false;
  }
</script>

{#if loading}
  <div>Loading...</div>
{:else if house}
  <!-- Detail view -->
  <h1>{house.name}</h1>
  <!-- ... -->
{/if}
```

**Validation:**
- [ ] Detail logic moved from `+page.svelte` to `[id]/+page.svelte`
- [ ] List page (`+page.svelte`) contains only list
- [ ] `$page.params.*` used to read URL param
- [ ] `$effect` used to load data when ID changes

---

### Phase 2: Backend Service Methods (Day 2)

#### Task 2.1: Create `HouseService.getById()`

**File:** `src/main/backend/services/koo/HouseService.groovy` (or .java)

```groovy
void getById(JSONObject injson, JSONObject outjson, 
             Connection db, ProcessServlet servlet) {
    
    long houseOid = injson.getLong("houseOid");
    
    // Get actor from session (NOT from request params!)
    PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
    if (pu == null) {
        outjson.put("_Success", false);
        outjson.put("_ErrorMessage", "Not authenticated");
        outjson.put("_ErrorCode", 2);
        return;
    }
    
    Actor actor = pu.getActor();
    
    // Load house from database
    House house = PerstStorageManager.getByOid(House.class, houseOid);
    
    if (house == null) {
        outjson.put("_Success", false);
        outjson.put("_ErrorMessage", "House not found");
        outjson.put("_ErrorCode", 4);
        return;
    }
    
    // Authorization: owner can see their own, admin sees all
    boolean isAdmin = actor?.getAgreement()?.getRole() in ["admin", "superAdmin"];
    
    if (!isAdmin) {
        // Use PURE OO NAVIGATION - no ID filtering!
        if (!actor.getHouses().contains(house)) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", "Not authorized");
            outjson.put("_ErrorCode", 3);
            return;
        }
    }
    
    // Success
    outjson.put("_Success", true);
    outjson.put("data", house);
}
```

**Key Points:**
- ✅ Uses `PerstStorageManager.getByOid()` to load
- ✅ Gets actor from `servlet.getUserData("perstUser")` (session)
- ✅ Uses `actor.getHouses()` (Pure OO - NO ID filtering!)
- ✅ Returns proper error codes

---

#### Task 2.2: Create `BookingService.getById()`

Same pattern, but check `actor.getBookings()`.

#### Task 2.3: Create `CleanerService.getById()`

Same pattern, but check `actor.getCleaners()` (if this relationship exists) or allow all admins.

#### Task 2.4: Create `ScheduleService.getById()`

Same pattern, but check `actor.getSchedules()`.

---

### Phase 3: List Page Updates (Day 2)

#### Task 3.1: Update List Pages to Use URL Navigation

**Current (broken - state-based):**
```typescript
// houses/+page.svelte
let editingHouse = $state<House | null>(null);
let showDetail = $state(false);

function openDetail(house: House) {
  editingHouse.set(house);
  showDetail.set(true);
}

<!-- In template -->
<button onclick={() => openDetail(house)}>
  Edit {house.name}
</button>
```

**Fixed (URL-based):**
```typescript
// houses/+page.svelte
import { goto } from '$app/navigation';

function openDetail(house: House) {
  goto(`/houses/${house.oid}`);  // URL: /houses/123
}

// Click house in list
<button onclick={() => openDetail(house)}>
  Edit {house.name}
</button>
```

**Remove from list page:**
- `editingX` state variables
- `showDetail` / `showEditForm` state variables
- `<EditForm>` components (moved to detail pages)

**Keep in list page:**
- Data loading (list of houses)
- "Create New" button
- List display

---

#### Task 3.2: Update All List Pages

Apply to:
- `houses/+page.svelte`
- `bookings/+page.svelte`
- `cleaners/+page.svelte`
- `schedules/+page.svelte`
- `owners/+page.svelte` (verify it already works)

---

### Phase 4: Backward Compatibility (Day 3)

#### Task 4.1: Handle 404 for Invalid IDs

In each detail page (`[id]/+page.svelte`):
```typescript
{#if house === null && !loading}
  <div class="text-center py-12">
    <h2 class="text-2xl font-bold text-gray-900 mb-4">House Not Found</h2>
    <p class="text-gray-600 mb-6">The house you're looking for doesn't exist.</p>
    <button onclick={() => goto('/houses')} 
            class="text-blue-600 hover:text-blue-800">
      ← Back to houses list
    </button>
  </div>
{/if}
```

#### Task 4.2: Redirect Old State-Based URLs (Optional)

If external links exist to old state-based URLs, create redirects.

---

### Phase 5: Testing & Validation (Day 3)

#### Test 1: Browser Navigation
```typescript
// Test 1: Direct URL access
// 1. Open /houses/123 directly
// 2. Should load house 123 data

// Test 2: Navigation flow
// 1. Go to /houses
// 2. Click house → URL changes to /houses/123
// 3. Click another house → URL changes to /houses/456
// 4. Browser back → URL to /houses/123
// 5. Browser forward → URL to /houses/456

// Test 3: Navbar
// 1. On /houses/123
// 2. Click "Owners" in navbar → URL to /owners
// 3. Click "Houses" in navbar → URL to /houses (list)
// 4. This is CORRECT - list shows all houses
```

#### Test 2: Role-Based Navbar
```typescript
// System Admin
// - Should see: houses, owners, cleaners, bookings, schedules, users

// Owner
// - Should see: houses, bookings, schedules
// - Should NOT see: owners, cleaners, users

// Cleaner  
// - Should see: schedules
// - Should NOT see: houses, owners, cleaners, bookings, users

// Logged Out
// - Should see: login, signup
// - Should NOT see: houses, owners, etc.
```

#### Test 3: No Page Reload
```typescript
// 1. Open browser dev tools → Network tab
// 2. Click navbar links
// 3. Should see: NO full page reload (no document request)
// 4. Only XHR/fetch requests for data
```

#### Test 4: TypeScript & Build
```bash
cd src/main/frontend-svelte
npm run check  # Must pass with 0 errors
npm run build  # Must build successfully
```

#### Test 5: Backend Services
```typescript
// Each getById service must:
// - Return 404 for non-existent IDs
// - Return 403 for unauthorized access
// - Return 200 with data for authorized access
// - Use Pure OO navigation (no ID filtering)
```

---

### Phase 6: Cleanup & Documentation (Day 3)

#### Task 6.1: Remove Debug Code
**Files:**
- All debug `console.log()` statements
- Debug span in Navbar.svelte (already done)
- Commented-out debug code

#### Task 6.2: Update Experience Log
**File:** `.memory/EXPERIENCE_LOG.md`

```markdown
### Iteration 12 - 2026-04-26
- Task: Implement Svelte 5 dynamic routes + navbar fixes
- Problem: Flat routes, state-based navigation, debug code in production
- Solution: Dynamic routes for all entities + goto()-based navbar
- Changes: 
  - Created [houseId]/, [bookingId]/, [cleanerId]/, [scheduleId]/ pages
  - Fixed navbar to use goto() instead of <a href>
  - Removed debug span
- Outcome: success
- Commits: <hash>
- Reflections: Navbar fix critical for SPA behavior
```

#### Task 6.3: Update Best Practices
**File:** `.memory/BEST_PRACTICES.md`

```markdown
### PAT-<XXX>: Use goto() for Navigation
**When:** Navigating between pages  
**Action:** Use `goto('/path')` not `<a href="/path">`  
**Rationale:** No page reload, preserves state, better UX
```

#### Task 6.4: Git Operations
```bash
git add -A
git commit -m "feat: implement Svelte 5 dynamic routes + navbar goto() navigation

- Create dynamic routes: houses/[id], bookings/[id], cleaners/[id], schedules/[id]
- Fix navbar: remove debug span, use goto() instead of <a href>
- Backend: add getById() services with Pure OO navigation
- No page reload on navigation, proper SPA behavior"

git push -u origin feat/sv5-dynamic-routes

git tag -a v1.2-YYYYMMDD -m "Svelte 5 dynamic routes + navbar fixes"
git push origin v1.2-YYYYMMDD
```

---

## Acceptance Criteria

### Dynamic Routes
- [ ] All 5 entities have working `[id]` dynamic routes
- [ ] Browser navigation (back/forward) works
- [ ] URLs are bookmarkable
- [ ] Direct URL access loads correct data

### Navbar
- [ ] Debug span removed from production
- [ ] No page reload when clicking navbar links
- [ ] Role-based menus work correctly
- [ ] Mobile menu closes after navigation

### Backend
- [ ] `getById()` methods exist for all entities
- [ ] Authorization enforced on detail pages
- [ ] Pure OO navigation used (no ID filtering)
- [ ] Proper error codes returned

### Quality
- [ ] `npm run check` passes (0 errors)
- [ ] `npm run build` succeeds
- [ ] No debug code in production
- [ ] No regressions in existing functionality

---

## First Point of Order

**IMMEDIATE ACTION REQUIRED:**

1. **Remove debug span from Navbar.svelte** (critical - visible in production)
2. **Convert navbar to use `goto()`** (critical - causes page reloads)
3. Test navbar role logic
4. Begin Phase 1: Create dynamic routes

**These navbar fixes are PRIORITY #1** - they affect every page load and expose internal state to users.

---

## References

- **PIT-001**: Missing Svelte 5 Dynamic Route Segments
- **SV5-001**: Use [id] folder notation (BEST_PRACTICES.md)
- **Owner Example**: `owners/[id]/+page.svelte` (working reference)
- **Navbar**: `src/main/frontend-svelte/src/lib/components/Navbar.svelte`

---

*Created per KISSOO_DEVELOPMENT_PROTOCOL.md*  
*Next: Create feature branch and begin Phase 0*
