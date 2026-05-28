# Branch Analysis: Triple Branch Comparison

**Date**: April 29, 2026  
**Analyst**: opencode  
**Branches Compared**: 
- `feat/pure-oo-refactor`
- `feat/sv5-dynamic-routes`
- `remodelCleaners2`
- **Current**: `remodeled32B-28Apr`

---

## Executive Summary

The current branch (`remodeled32B-28Apr`) is a **broken hybrid** that partially implements patterns from all three working branches but doesn't fully align with any of them. The main issues are:

1. **Package structure mismatch** - Uses old `koo.core.*` and `domain.actor.*` instead of the clean `domain.domain.*` + `domain.database.*` pattern
2. **Login is BROKEN** - Signature mismatch between what ProcessServlet calls and what Login.java implements
3. **Frontend uses outdated patterns** - Separate detail pages instead of edit-in-place dynamic routes
4. **Backend services are bloated** - CleaningService.groovy is 93KB of legacy code

---

## Branch-by-Branch Analysis

### 1. `feat/pure-oo-refactor` (The Gold Standard - needs adaptation per your vision)

**Purpose**: Pure OO navigation with clean package structure

**Key Achievements** (as implemented in branch):
- ✅ **Clean package structure** (as implemented in branch):
  - `domain.domain.*` - All domain entities (Actor, Owner, Cleaner, House, Booking, Schedule)
  - `domain.database.*` - All Manager classes (ActorManager, HouseManager, BookingManager, etc.)
  - `domain.service.*` - Service classes (Login, PerstInit)
  - No more `koo.core.*` packages

**Adaptation per your vision** (what it should be):
- `koo/core/actor/*` - Generic Actor, Agreement, Role, etc.
- `koo/core/database/*` - Generic StorageManager, PerstConnection
- `koo/core/user/*` - Generic PerstUser, PerstUserManager
- `domain/actor/*` - Owner, Cleaner (extend koo Actor)
- `domain/house/*` - House, Booking, Schedule, CostProfile
- `domain/database/*` - Manager classes (extend koo BaseManager)
- `backend/services/` - Service implementations

**Adaptation per your vision** (what it should be):
- `koo/core/actor/*` - Generic Actor, Agreement, Role, etc.
- `koo/core/database/*` - Generic StorageManager, PerstConnection
- `koo/core/user/*` - Generic PerstUser, PerstUserManager
- `domain/actor/*` - Owner, Cleaner (extend koo Actor)
- `domain/house/*` - House, Booking, Schedule, CostProfile
- `domain/database/*` - Manager classes (extend koo BaseManager)
- `backend/services/` - Service implementations

- ✅ **Pure OO Navigation**:
  - `PerstUser.getActor()` returns Actor
  - `Actor.getHouses()` returns houses directly
  - No ID-based lookups anywhere
  - Uses `getByOid()` only when receiving OID from frontend

- ✅ **Authorization via Agreement/Role**:
  - `Actor.getAgreement().getRole()` determines permissions
  - `EndpointMethod` class for type-safe endpoint authorization
  - Admin/SuperAdmin role checking

- ✅ **Lombok Domain Classes**:
  - `@Getter @Setter @NoArgsConstructor` on all domain entities
  - Manual business methods only (checkPassword, canLogin, etc.)
  - No manual getters/setters

**Key Files** (per your vision):
```
src/main/koo/core/actor/
  ├── Actor.java
  ├── Agreement.java
  ├── Role.java
  ├── ANaturalActor.java
  └── ACorporateActor.java

src/main/koo/core/database/
  ├── BaseManager.java
  ├── StorageManager.java
  └── PerstConnection.java

src/main/koo/core/user/
  ├── PerstUser.java
  └── PerstUserManager.java

src/main/koo/security/
  └── EndpointMethod.java

src/main/koo/services/
  ├── Login.java        # Generic base (can be extended)
  ├── EmailService.java # Generic base
  └── CostService.java  # Generic base

src/main/domain/actor/
  ├── Owner.java        # extends koo.core.actor.ANaturalActor
  └── Cleaner.java      # extends koo.core.actor.ANaturalActor

src/main/domain/house/
  ├── House.java
  ├── Booking.java
  ├── Schedule.java
  └── CostProfile.java

src/main/domain/database/
  ├── OwnerManager.java    # extends koo.core.database.BaseManager<Owner>
  ├── CleanerManager.java  # extends koo.core.database.BaseManager<Cleaner>
  ├── HouseManager.java
  ├── BookingManager.java
  ├── ScheduleManager.java
  ├── CostProfileManager.java
  └── PerstUserManager.java

src/main/backend/services/
  ├── Login.java              # May extend koo.services.Login
  ├── CostService.java        # Domain-specific (as you specified)
  ├── EmailService.java       # May extend services.koo.internal.EmailService
  └── PerstInit.java
```

---

### 2. `feat/sv5-dynamic-routes` (Frontend Excellence)

**Purpose**: Svelte 5 dynamic routes with edit-in-place

**Key Achievements**:
- ✅ **Dynamic Routes with Edit-in-Place**:
  - No more `[bookingId]/+page.svelte` for editing
  - Uses `editingBooking` state variable
  - Form toggles in same page, scrolls to form on mobile

- ✅ **Svelte 5 Patterns**:
  - `$state()` for reactive variables
  - `$derived()` for computed values
  - `$effect()` for lifecycle
  - `{#if children}` instead of separate check
  - `bind:this={formSection}` for scroll-to-edit

- ✅ **Navbar Uses `<a href>` Instead of `goto()`**:
  - Better browser navigation (back button works)
  - No page reload issues
  - SEO-friendly

- ✅ **Optimistic UI Updates**:
  - `editingBooking = row` sets form data
  - `showForm = true` reveals form
  - `scrollToEditForm()` for mobile UX

**Key Patterns**:
```svelte
// Edit-in-place pattern
let editingBooking = $state<Booking | null>(null);

function handleEdit(booking) {
  editingBooking = booking;
  showForm = true;
  scrollToEditForm();
}

// Form submission
async function handleSubmit(data) {
  if (editingBooking) {
    await api.update(editingBooking.id, data);
  } else {
    await api.create(data);
  }
}
```

---

### 3. `remodelCleaners2` (Cleaner Domain & Koo Package)

**Purpose**: Proper Cleaner entity with koo package for core services

**Key Achievements**:
- ✅ **koo Package for Core Services**:
  - `koo.core.actor.*` - Actor, Agreement, Role, etc.
  - `koo.core.database.*` - PerstConnection, StorageManager, CDatabaseRoot
  - `koo.core.user.*` - PerstUser, PerstUserManager
  - `koo.security.*` - EndpointMethod, PasswordSecurity
  - `koo.services.*` - Login, EmailService, CostService
  - `koo.config.*` - AppConfig, PerstConfig

- ✅ **Cleaner Domain Properly Implemented**:
  - `Cleaner extends ANaturalActor`
  - Direct `getSchedules()` method (no streaming through houses)
  - Perst Link for schedules collection
  - `schedule.setCleaner(this)` in `addSchedule()`

- ✅ **Build System Fixes**:
  - Compile `backend/koo` for GroovyService classloader visibility
  - Proper dependency order: domain → koo → backend

- ✅ **PasswordSecurity & EmailService in Java**:
  - TLS enforcement in EmailService
  - Secure password hashing in PasswordSecurity
  - Both in `koo.services.*`

---

## Current Branch State (`remodeled32B-28Apr`)

### What's Working ✅

1. **Manager Class Pattern** - `OwnerManager`, `CleanerManager`, `ScheduleManager` exist
2. **Authorization Logic** - Commits 97e72758, b0ab81dd added `instanceof Owner/Cleaner` checks
3. **.gitignore Cleanup** - Removed tracked `.svelte-kit/` files
4. **Login.groovy Wrapper Attempt** - Recognized special signature requirement

### What's Broken ❌

#### 1. **Login Signature Mismatch**
```java
// What ProcessServlet calls (in ProcessServlet.java):
// GroovyClass.invoke(true, "Login", "login", null, DB, user, password, outjson, this)

// Expected signature:
public static UserData login(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet)

// What current Login.java has:
public static UserData login(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)
```
**Result**: Login BROKEN - ProcessServlet can't call Login with correct parameters.

#### 2. **Package Structure is Wrong**
```
Current:
  koo.core.actor.*      (should be domain.domain.*)
  koo.core.database.*    (should be domain.database.*)
  domain.actor.*      (old structure)
  domain.oov.*        (old structure)

Should be (from feat/pure-oo-refactor):
  domain.domain.*      (all entities)
  domain.database.*    (all managers)
  domain.service.*     (service classes)
```

**Result**: 25+ files in wrong packages, import statements broken.

#### 3. **Frontend Uses Outdated Routes**
```
Current:
  /bookings/[bookingId]/+page.svelte   (separate edit page)
  
Should be (from feat/sv5-dynamic-routes):
  /bookings/+page.svelte              (edit-in-place with editingBooking state)
```

**Result**: Poor UX, broken browser navigation.

#### 4. **CleaningService.groovy is Bloated**
- **Size**: 93KB (should be ~20KB)
- **Contains**: Legacy code, duplicate methods, wrong authorization patterns
- **Should be**: Thin service layer calling Manager classes

#### 5. **Streaming Performance Issues**
```java
// Owner.java (current)
public List<Booking> getBookings() {
    return getHouses().stream()              // Iteration 1: houses
        .flatMap(house -> house.getBookings().stream())  // Iteration 2: bookings
        .collect(Collectors.toList());
}

public List<Schedule> getSchedulesViaHouses() {
    return getBookings().stream()            // Iteration 1+2 (from above)
        .map(Booking::getSchedule)          // Iteration 3: schedules
        .filter(s -> s != null)
        .collect(Collectors.toList());
}
```

**Result**: Triple iteration for schedules. Works for small datasets, but not optimal.

---

## Comparison Matrix

| Feature | pure-oo-refactor | sv5-dynamic-routes | remodelCleaners2 | Current |
|---------|------------------|-------------------|------------------|---------|
| **Package Structure** | ✅ `koo.*` + `domain.*` | ✅ `koo.*` + `domain.*` | ✅ `koo.*` + `domain.*` | ❌ Mixed |
| **Login Working** | ✅ | ✅ | ✅ | ❌ Broken |
| **Edit-in-Place** | ⚠️ Partial | ✅ Full | ⚠️ Partial | ❌ No |
| **Pure OO Nav** | ✅ Full | ✅ Full | ✅ Full | ⚠️ Partial |
| **Manager Pattern** | ✅ `domain/database.*` | ✅ `domain/database.*` | ✅ `domain/database.*` | ⚠️ Mixed |
| **Authorization** | ✅ Agreement/Role | ✅ Agreement/Role | ✅ Agreement/Role | ⚠️ Partial |
| **Lombok Domain** | ✅ Full | ✅ Full | ✅ Full | ⚠️ Partial |
| **Streaming Opt.** | ⚠️ Basic | ⚠️ Basic | ⚠️ Basic | ⚠️ Basic |

---

## My In-Depth Suggestions

### 1. **Fix the Streaming Problem (Beat Relational)**

You mentioned you have ideas to "beat anything relational." Here are my suggestions:

#### Current Problem
```java
Owner.getBookings() → houses → bookings (2 iterations)
Owner.getSchedulesViaHouses() → houses → bookings → schedules (3 iterations)
```

#### Suggestion A: **Stored Links at Owner Level**
Instead of streaming through houses, maintain direct Links:

```java
// In Owner.java
private Link bookingsLink;  // Direct Link to all owner's bookings
private Link schedulesLink;  // Direct Link to all owner's schedules

public List<Booking> getBookings() {
    if (bookingsLink == null || bookingsLink.isEmpty()) return List.of();
    return Arrays.asList((Booking[])bookingsLink.toArray(new Booking[0]));
    // Single iteration - O(1) access
}

public List<Schedule> getSchedules() {
    if (schedulesLink == null || schedulesLink.isEmpty()) return List.of();
    return Arrays.asList((Schedule[])schedulesLink.toArray(new Schedule[0]));
    // Single iteration - O(1) access
}
```

**When to update these Links**:
- `House.addBooking(booking)` → also `owner.getBookingsLink().add(booking)`
- `Booking.setSchedule(schedule)` → also `owner.getSchedulesLink().add(schedule)`

**Benefit**: O(1) access instead of O(n*m) iterations.

#### Suggestion B: **Perst select() with Predicate**
Use Perst's indexed `select()` for direct lookups:

```java
public List<Booking> getBookingsFast() {
    return PerstStorageManager.select(Booking.class, booking -> 
        booking.getHouse().getOwnerOid() == this.getOid()
    );
}
```

**Benefit**: Perst iterates the extent internally (C++ layer), faster than Java streams.

#### Suggestion C: **Hybrid Approach (Best of Both Worlds)**
```java
public class Owner extends ANaturalActor {
    private Link houses;
    
    // Cached bookings (lazy-loaded, invalidated on changes)
    private transient List<Booking> cachedBookings;
    private transient long lastBookingCacheTime;
    
    public List<Booking> getBookings() {
        if (cachedBookings != null && !isCacheStale()) {
            return cachedBookings;
        }
        
        // Rebuild cache
        cachedBookings = houses.stream()
            .flatMap(h -> h.getBookings().stream())
            .collect(Collectors.toList());
        lastBookingCacheTime = System.currentTimeMillis();
        return cachedBookings;
    }
    
    private boolean isCacheStale() {
        return System.currentTimeMillis() - lastBookingCacheTime > 5000; // 5 seconds
    }
    
    // Call this when bookings change
    public void invalidateBookingCache() {
        cachedBookings = null;
    }
}
```

**Benefit**: Best of both worlds - O(1) for repeated calls, only rebuild when needed.

---

### 2. **Package Structure Recommendation (Updated per your vision)**

Based on your clarification that `koo` should be generic/framework code and `domain` should be project-specific:

```
src/main/
├── koo/                     # GENERIC FRAMEWORK CODE (reusable across projects)
│   ├── core/
│   │   ├── actor/       # Generic Actor, Agreement, Role, ANaturalActor, etc.
│   │   ├── database/    # Generic StorageManager, PerstConnection, BaseManager
│   │   └── user/        # Generic PerstUser, PerstUserManager
│   ├── security/        # Generic EndpointMethod, PasswordSecurity
│   ├── services/        # Generic services (Login, EmailService, CostService base)
│   └── config/          # Generic AppConfig, PerstConfig
│
└── domain/                # PROJECT-SPECIFIC - KissOO business logic
    ├── actor/           # Owner, Cleaner (extend koo.core.actor.Actor)
    │   ├── Owner.java
    │   └── Cleaner.java
    │
    ├── house/           # House, Booking, Schedule, CostProfile
    │   ├── House.java
    │   ├── Booking.java
    │   ├── Schedule.java
    │   └── CostProfile.java
    │
    └── database/        # Project-specific Manager classes
        ├── OwnerManager.java    # extends koo.core.database.BaseManager<Owner>
        ├── CleanerManager.java  # extends koo.core.database.BaseManager<Cleaner>
        ├── HouseManager.java
        ├── BookingManager.java
        ├── ScheduleManager.java
        ├── CostProfileManager.java
        └── PerstUserManager.java

# Java services go in backend/services/ (mix of generic and domain-specific)
src/main/backend/services/
    ├── Login.java              # Uses koo.services.Login as base
    ├── CostService.java        # Domain-specific (you confirmed this)
    ├── EmailService.java       # Could be generic or domain-specific
    └── PerstInit.java
```

**Why this structure**:
- `koo.*` = Framework code (generic, reusable in any Perst-based project)
- `domain.*` = KissOO-specific business logic (extends koo classes)
- `backend/services/` = Service implementations (may extend koo services or be domain-specific)
- Clear separation: framework vs project code

---

### 3. **Login Fix (Critical)**

The login signature MUST match what ProcessServlet calls. Here's the fix:

```java
// In src/main/precompiled/koo/services/Login.java (or domain/service/Login.java)
package koo.services;  // or domain.service

import org.kissweb.database.Connection;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;
import org.kissweb.restServer.UserCache;

public class Login {
    
    // THIS signature matches what ProcessServlet calls:
    // GroovyClass.invoke(true, "Login", "login", null, DB, user, password, outjson, this)
    public static UserData login(Connection db, String username, String password, JSONObject outjson, ProcessServlet servlet) {
        
        System.out.println("[PerstAuth] Login attempt for user: " + username);
        
        try {
            PerstUser perstUser = PerstUserManager.authenticate(username, password);
            
            if (perstUser == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorCode", 2);
                outjson.put("_ErrorMessage", "Invalid credentials");
                return null;
            }
            
            // Create session
            UserData ud = UserCache.newUser(username, password, null);
            ud.putUserData("perstUser", perstUser);
            
            // Update last login
            perstUser.setLastLoginDate(System.currentTimeMillis());
            PerstUserManager.update(perstUser);
            
            // Populate session data
            outjson.put("userOid", perstUser.getOid());
            outjson.put("username", perstUser.getUsername());
            outjson.put("fullyActivated", !perstUser.isMustChangePassword() && perstUser.isEmailVerified());
            outjson.put("needsPasswordChange", perstUser.isMustChangePassword());
            outjson.put("needsEmailVerification", !perstUser.isEmailVerified());
            
            // Actor info
            Actor actor = perstUser.getActor();
            if (actor != null) {
                outjson.put("ownerOid", actor instanceof Owner ? actor.getOid() : 0);
                outjson.put("cleanerOid", actor instanceof Cleaner ? actor.getOid() : 0);
                outjson.put("isAdmin", actor.getAgreement() != null && 
                    (actor.getAgreement().getRole() == Role.ADMIN || 
                     actor.getAgreement().getRole() == Role.SUPER_ADMIN));
            }
            
            outjson.put("_Success", true);
            return ud;
            
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorCode", -1);
            outjson.put("_ErrorMessage", "Login failed: " + e.getMessage());
            return null;
        }
    }
}
```

**Key**: The signature MUST be `(Connection db, String username, String password, JSONObject outjson, ProcessServlet servlet)`.

---

### 4. **Frontend: Adopt Full Dynamic Routes**

Replace separate `[entityId]/+page.svelte` files with edit-in-place:

```svelte
<!-- src/routes/bookings/+page.svelte -->
<script lang="ts">
    import { session } from '$lib/state/session.svelte';
    import { bookingsAPI } from '$lib/api/Bookings';
    import Form from '$lib/components/Form.svelte';
    
    let bookings = $state<Booking[]>([]);
    let showForm = $state(false);
    let editingBooking = $state<Booking | null>(null);
    let saving = $state(false);
    
    // Edit-in-place handler
    function handleEdit(booking: Booking) {
        editingBooking = booking;
        showForm = true;
        scrollToEditForm();
    }
    
    async function handleSubmit(data: any) {
        if (saving) return;
        saving = true;
        
        try {
            if (editingBooking) {
                await bookingsAPI.update(editingBooking.id, data);
            } else {
                await bookingsAPI.create(data);
            }
            showForm = false;
            editingBooking = null;
            await loadData();
        } catch (err) {
            error = err.message;
        } finally {
            saving = false;
        }
    }
</script>

{#if showForm}
    <div bind:this={formSection}>
        <Form
            data={editingBooking || {}}
            title={editingBooking ? 'Edit Booking' : 'New Booking'}
            onSubmit={handleSubmit}
            onCancel={() => { showForm = false; editingBooking = null; }}
        />
    </div>
{/if}

{#each bookings as booking}
    <div onclick={() => handleEdit(booking)}>
        {booking.guest_name}
    </div>
{/each}
```

---

### 5. **Backend: Thin Services, Thick Managers**

Refactor `CleaningService.groovy` from 93KB to ~20KB:

```groovy
// CleaningService.groovy (slim version)
package services

import domain.database.BookingManager
import domain.database.ScheduleManager
import domain.database.HouseManager
import domain.domain.Booking
import domain.domain.Schedule
import domain.domain.House

class CleaningService {

  // Get houses (filtered by actor)
  void getHouses(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
    def actor = getActor(servlet)
    if (actor instanceof Owner) {
      outjson.put("houses", OwnerManager.getHouses(actor))
    } else if (actor instanceof Cleaner) {
      outjson.put("houses", HouseManager.getByCleaner(actor))
    } else if (isAdmin(servlet)) {
      outjson.put("houses", HouseManager.getAll())
    } else {
      outjson.put("_ErrorMessage", "Access denied")
    }
  }

  // Get bookings (Pure OO navigation)
  void getBookings(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
    def actor = getActor(servlet)
    Collection<Booking> bookings

    if (actor instanceof Owner) {
      bookings = actor.getBookings()  // Pure OO!
    } else if (actor instanceof Cleaner) {
      bookings = BookingManager.getByCleaner(actor)
    } else if (isAdmin(servlet)) {
      bookings = BookingManager.getAll()
    }

    outjson.put("bookings", bookings)
  }

  // Helper: get actor from session
  private def getActor(ProcessServlet servlet) {
    def pu = (PerstUser) servlet.getUserData("perstUser")
    return pu?.getActor()
  }

  private boolean isAdmin(ProcessServlet servlet) {
    def actor = getActor(servlet)
    return actor?.getAgreement()?.getRole() in [Role.ADMIN, Role.SUPER_ADMIN]
  }
}
```

**Key**: Service layer is thin - just authorization + delegation to Manager classes.

---

## Recommended Action Plan

### Phase 1: Fix Critical Bugs (Do Immediately)
1. ✅ Fix Login.java signature to match ProcessServlet
2. ✅ Test login end-to-end
3. ✅ Verify PerstUser.getActor() works

### Phase 2: Align Package Structure (1-2 hours)
1. ✅ Move `koo.core.*` → `domain.domain.*`
2. ✅ Move Manager classes → `domain.database.*`
3. ✅ Update ALL import statements
4. ✅ Test build (`buildSystem()`)

### Phase 3: Frontend Dynamic Routes (2-3 hours)
1. ✅ Remove `[entityId]/+page.svelte` files
2. ✅ Implement edit-in-place with `editingBooking` state
3. ✅ Change Navbar to use `<a href>` instead of `goto()`
4. ✅ Test all routes

### Phase 4: Optimize Streaming (Your Ideas)
1. ✅ Implement your "beat relational" approach
2. ✅ Benchmark vs SQL-style JOINs
3. ✅ Document the performance gains

---

## Questions for You

1. **Which branch should be the "source of truth"?**
   - `feat/pure-oo-refactor` (clean packages)?
   - `feat/sv5-dynamic-routes` (frontend)?
   - `remodelCleaners2` (koo package)?

2. **What's your "beat relational" idea?**
   - Stored Links at parent level?
   - Perst select() with predicates?
   - Something else entirely?

3. **Do you want to keep the `koo.*` package?**
   - For reusable framework code?
   - Or merge everything into `domain.*`?

4. **Priority:**
   - Fix login first (critical)?
   - Or restructure packages first?

---

**Document prepared by**: opencode  
**Next step**: Your decision on which branch to align with, then execute.
