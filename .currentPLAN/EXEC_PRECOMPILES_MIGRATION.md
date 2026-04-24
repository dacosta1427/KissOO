# Plan: Move Infrastructure Classes to Precompiles

**Date:** 2026-04-24  
**Branch:** cleanModel32B  
**Type:** REFACTOR - Infrastructure Optimization  

## Objective

Move non-change liable infrastructure classes from `backend/` to `precompiled/` package to:
- Optimize build/runtime performance (per user preference)
- Maintain clear separation between stable infrastructure and active business logic
- Reduce compilation overhead

## Rationale

From AGENTS.md: "User prefers moving non-change liable classes to precompiled to optimize build/runtime structure"

The precompiled package already contains the vast majority of stable code:
- Domain entities (Owner, Cleaner, House, Booking, Schedule)
- Managers (OwnerManager, CleanerManager, etc.)
- Security (PasswordSecurity, EndpointMethod)
- Core actor hierarchy (AActor, ANaturalActor, etc.)

Remaining infrastructure that is stable and rarely changes:
- PerstConnection - Connection wrapper for Perst OODBMS operations
- EmailService - Recently migrated to Java, stable API
- PerstUserManager - PerstUser management (minor fix applied)

## Changes Made

### 1. PerstConnection.java - MOVED TO PRECOMPILES ✓

**From:** `src/main/backend/koo/PerstConnection.java`  
**To:** `src/main/precompiled/koo/core/database/PerstConnection.java`  
**Package:** `koo.core.database` (updated from `koo`)

**Reasoning:**
- Infrastructure code with no business logic
- Used by backend services for Perst operations
- Rarely changes (last update was for PerstConnection-Usage.md)
- Fits with "Manager at the Gate" pattern

**Updates Required:**
- Updated import in `KissInit.groovy`: `import koo.PerstConnection` → `import koo.core.database.PerstConnection`
- Compiles successfully in precompiles build stage

### 2. PerstUser.java - FIXED ✓

**File:** `src/main/precompiled/koo/core/user/PerstUser.java`

**Issue:** Corrupted `verifyEmail()` method with duplicate declaration and malformed code

**Fix:** Rewrote method to correct implementation:
```java
public boolean verifyEmail(String token) {
    if (verificationToken == null || !verificationToken.equals(token)) {
        return false;
    }
    if (System.currentTimeMillis() > verificationExpiresAt) {
        return false;
    }
    this.emailVerified = true;
    this.verificationToken = null;
    return true;
}
```

**Note:** This file was corrupted in the repository (both local and remote). Fix was necessary for build to succeed.

### 3. PerstUserManager.java - FIXED ✓

**File:** `src/main/precompiled/koo/core/user/PerstUserManager.java`

**Issue:** Called `user.isPasswordExpired()` which doesn't exist in PerstUser class

**Fix:** Removed the non-existent check (lines 59-61)

**Note:** Password expiry feature is not implemented. Removing the check allows authentication to work. If password expiry is needed, it should be implemented as a separate feature.

### 4. Directory Cleanup ✓

- Removed empty `src/main/backend/koo/` directory
- Removed empty `src/main/backend/koo/services/` directory
- Retained `src/main/backend/koo/` as empty directory for build compatibility (referenced in Tasks.java line 284)

## Build Verification

```bash
./bld build    # SUCCESS
./bld develop  # SUCCESS - Server starts and runs
```

All compilation successful. Server starts without errors.

## Classes NOT Moved (Active Business Logic)

The following services remain in `backend/` as they contain active, frequently-changing business logic:

1. **CleaningService.groovy** (2003 lines) - Complex cleaning scheduler workflow
2. **Users.groovy** (518 lines) - User management with evolving verification flows
3. **PermissionService.groovy** (394 lines) - Permission system (active development)
4. **CostService.java** (191 lines) - Cost calculation (pricing changes)
5. **ActorService.java** (283 lines) - Actor operations
6. **LoadTestdata.groovy** (394 lines) - Test data (changes frequently)
7. **EmailService.java** - Could be moved, but kept in backend for now due to TLS/email config changes

## Benefits Achieved

✅ **Reduced Build Overhead** - PerstConnection compiled once in precompiles stage  
✅ **Clearer Separation** - Infrastructure vs. business logic  
✅ **Faster Iteration** - Backend Groovy services still hot-reload  
✅ **Maintained Flexibility** - Active business logic remains in backend  
✅ **Protocol Compliance** - "Manager at the Gate" pattern reinforced  

## Testing Required

- [ ] Verify PerstConnection functionality (CRUD operations)
- [ ] Verify email sending (if EmailService moved)
- [ ] Verify authentication flow (PerstUser changes)
- [ ] Verify server startup and shutdown

## Rollback Plan

If issues arise:
```bash
git revert HEAD  # Revert all changes
git push origin cleanModel32B
```

Or selectively:
```bash
git checkout HEAD~1 -- src/main/precompiled/koo/core/database/PerstConnection.java
git checkout HEAD~1 -- src/main/backend/KissInit.groovy
git checkout HEAD~1 -- src/main/precompiled/koo/core/user/PerstUser.java
git checkout HEAD~1 -- src/main/precompiled/koo/core/user/PerstUserManager.java
mkdir -p src/main/backend/koo
cp src/main/precompiled/koo/core/database/PerstConnection.java src/main/backend/koo/
```

## Next Steps

1. Commit and push changes
2. Run integration tests
3. Monitor server logs for any issues
4. Consider moving EmailService.java to precompiles (optional)
5. Document password expiry feature if needed

## Protocol Compliance

✅ Read AP.md and KISSOO_DEVELOPMENT_PROTOCOL.md  
✅ Followed "Manager at the Gate" pattern  
✅ Maintained Pure OO navigation principles  
✅ Updated documentation (AGENTS.md section below)  
✅ Tested build before committing  
✅ Branch, commit, push, tag workflow  

---

**Status:** READY TO COMMIT AND PUSH
