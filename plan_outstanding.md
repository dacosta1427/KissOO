# KissOO Perst Integration Fix - Execution Plan

**Status:** ❌ BROKEN - 18 compilation errors  
**Last Build:** `./bld.cmd -v develop` - FAILED  
**Analysis Date:** 2026-03-15

---

## Executive Summary

The KissOO framework **cannot start** due to 18 compilation errors in the Perst integration code. The errors stem from API mismatches between Managers, BaseManager, and domain entities.

---

## Root Cause Analysis

### Issue #1: `checkPermission` Type Mismatch
**Location:** `ActorManager.java` lines 43, 53, 63, 73, 98, 108 and `PerstUserManager.java` lines 42, 52, 62, 72, 82

**Problem:** `BaseManager.checkPermission()` expects `Class<?>` as 3rd parameter, but callers pass `String`:
```java
// BaseManager.java line 61 - CORRECT signature
protected static boolean checkPermission(Actor actor, String action, Class<?> resourceClass)

// ActorManager.java line 43 - WRONG (passing String)
if (!checkPermission(actor, ACTION_READ, "Actor"))  // Should be: Actor.class
```

**Fix:** Change all `checkPermission` calls to pass `Class<?>` instead of `String`.

---

### Issue #2: `retrieveObject` Return Type
**Location:** `ActorManager.java` lines 133, 143 and `PerstUserManager.java` line 107

**Problem:** 
- Line 133: Returns `Object` but assigns to `Actor`
- Line 143: Wrong method signature - `retrieveObject(Class, String)` doesn't exist

```java
// ActorManager.java:133 - WRONG
return PerstHelper.retrieveObject(Actor.class, "name", key);  // Returns Object, not Actor

// ActorManager.java:143 - WRONG (method doesn't exist)
return PerstHelper.retrieveObject(Actor.class, uuid);
```

**Fix:** Use correct `PerstHelper` method: `retrieveActorByUuid(String uuid)` or cast return value.

---

### Issue #3: Missing `setUserId(Integer)` on Actor
**Location:** `ActorManager.java` line 180

**Problem:** Actor has no `setUserId(Integer)` method:
```java
actor.setUserId((Integer) params[3]);  // Method doesn't exist
```

**Fix:** Either add `userId` field + setter to Actor, or remove this line.

---

### Issue #4: Missing `getPassword()` on PerstUser
**Location:** `PerstUserManager.java` lines 125, 212

**Problem:** PerstUser has `getPasswordHash()` not `getPassword()`:
```java
// PerstUserManager.java:125 - WRONG
String storedPassword = user.getPassword();  // Should be getPasswordHash()

// PerstUserManager.java:212 - WRONG
if (user.getPassword() == null || user.getPassword().isEmpty())  // Same issue
```

**Fix:** Change to `getPasswordHash()` or add `getPassword()` wrapper method.

---

## Detailed Fix Tasks

### PHASE 1: Fix ActorManager.java (11 errors)

#### Task 1.1: Fix checkPermission calls (6 occurrences)
**File:** `src/main/precompiled/mycompany/database/ActorManager.java`

| Line | Current | Change To |
|------|---------|-----------|
| 43 | `checkPermission(actor, ACTION_READ, "Actor")` | `checkPermission(actor, ACTION_READ, Actor.class)` |
| 53 | `checkPermission(actor, ACTION_READ, "Actor")` | `checkPermission(actor, ACTION_READ, Actor.class)` |
| 63 | `checkPermission(actor, ACTION_READ, "Actor")` | `checkPermission(actor, ACTION_READ, Actor.class)` |
| 73 | `checkPermission(actor, ACTION_CREATE, "Actor")` | `checkPermission(actor, ACTION_CREATE, Actor.class)` |
| 98 | `checkPermission(actor, ACTION_UPDATE, "Actor")` | `checkPermission(actor, ACTION_UPDATE, Actor.class)` |
| 108 | `checkPermission(actor, ACTION_DELETE, "Actor")` | `checkPermission(actor, ACTION_DELETE, Actor.class)` |

#### Task 1.2: Fix retrieveObject calls (2 occurrences)
**File:** `src/main/precompiled/mycompany/database/ActorManager.java`

| Line | Current | Change To |
|------|---------|-----------|
| 133 | `return PerstHelper.retrieveObject(Actor.class, "name", key);` | `return (Actor) PerstHelper.retrieveObject(Actor.class, "name", key);` |
| 143 | `return PerstHelper.retrieveObject(Actor.class, uuid);` | `return PerstHelper.retrieveActorByUuid(uuid);` |

#### Task 1.3: Fix setUserId call (1 occurrence)
**File:** `src/main/precompiled/mycompany/database/ActorManager.java`

**Option A:** Remove the line (if userId linking is handled elsewhere)
**Option B:** Add userId field + setter to Actor class

```java
// If Option B - Add to Actor.java:
// private int userId = 0;
// public int getUserId() { return userId; }
// public void setUserId(int userId) { this.userId = userId; }
```

---

### PHASE 2: Fix PerstUserManager.java (7 errors)

#### Task 2.1: Fix checkPermission calls (5 occurrences)
**File:** `src/main/precompiled/mycompany/database/PerstUserManager.java`

| Line | Current | Change To |
|------|---------|-----------|
| 42 | `checkPermission(actor, ACTION_READ, "PerstUser")` | `checkPermission(actor, ACTION_READ, PerstUser.class)` |
| 52 | `checkPermission(actor, ACTION_READ, "PerstUser")` | `checkPermission(actor, ACTION_READ, PerstUser.class)` |
| 62 | `checkPermission(actor, ACTION_CREATE, "PerstUser")` | `checkPermission(actor, ACTION_CREATE, PerstUser.class)` |
| 72 | `checkPermission(actor, ACTION_UPDATE, "PerstUser")` | `checkPermission(actor, ACTION_UPDATE, PerstUser.class)` |
| 82 | `checkPermission(actor, ACTION_DELETE, "PerstUser")` | `checkPermission(actor, ACTION_DELETE, PerstUser.class)` |

#### Task 2.2: Fix retrieveObject return type (1 occurrence)
**File:** `src/main/precompiled/mycompany/database/PerstUserManager.java`

| Line | Current | Change To |
|------|---------|-----------|
| 107 | `return PerstHelper.retrieveObject(PerstUser.class, "username", key);` | `return (PerstUser) PerstHelper.retrieveObject(PerstUser.class, "username", key);` |

#### Task 2.3: Fix getPassword calls (1 occurrence - actually 2 lines)
**File:** `src/main/precompiled/mycompany/database/PerstUserManager.java`

| Line | Current | Change To |
|------|---------|-----------|
| 125 | `user.getPassword()` | `user.getPasswordHash()` |
| 212 | `user.getPassword()` | `user.getPasswordHash()` |

---

### PHASE 3: Add Missing Actor Methods (if needed)

#### Task 3.1: Add userId field to Actor
**File:** `src/main/precompiled/mycompany/domain/Actor.java`

Add after line 21:
```java
private int userId = 0;
```

Add getters/setters:
```java
public int getUserId() { return userId; }
public void setUserId(int userId) { this.userId = userId; }
```

---

### PHASE 4: Verify Build

#### Task 4.1: Run build
```bash
./bld.cmd -v develop
```

#### Task 4.2: Verify startup
- Check no compilation errors
- Check server starts on http://localhost:8000

---

## Implementation Order

1. **ActorManager.java** - Fix all 6 checkPermission calls
2. **ActorManager.java** - Fix 2 retrieveObject calls  
3. **ActorManager.java** - Fix setUserId (or decide to remove)
4. **PerstUserManager.java** - Fix all 5 checkPermission calls
5. **PerstUserManager.java** - Fix retrieveObject return type
6. **PerstUserManager.java** - Fix 2 getPasswordHash calls
7. **Actor.java** - Add userId field if needed
8. **Build and Test** - Run `./bld.cmd -v develop`

---

## Files to Modify

| File | Changes | Errors Fixed |
|------|---------|--------------|
| `src/main/precompiled/mycompany/database/ActorManager.java` | 9 changes | 11 |
| `src/main/precompiled/mycompany/database/PerstUserManager.java` | 8 changes | 7 |
| `src/main/precompiled/mycompany/domain/Actor.java` | Add userId (optional) | 1 |

---

## After Fix - Next Steps

Once build succeeds:
1. Run tests to verify Perst integration works
2. Test login flow
3. Verify Actor/PerstUser CRUD operations
4. Check that deleted test files are either restored or formally removed from git

---

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Changing String to Class may break runtime | Medium | Verify each checkPermission call is correct |
| Casting retrieveObject may cause ClassCastException | Medium | Ensure proper type is returned |
| Adding userId field to Actor | Low | If not used, consider removing line instead |

---

*Plan Created: 2026-03-15*
*Author: Claude (minimax-m2.5-free)*
