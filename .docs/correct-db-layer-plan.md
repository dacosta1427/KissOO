# Plan: Database Layer Assessment & Correction

**Date:** 2026-04-28  
**Task ID:** ASSESS-DB-LAYER-CORRECTION  
**Status:** PLANNING  

## CRITICAL CORRECTION: ooGTxQ IS PERST 5.1.1

The plan to "replace ooGTxQ with Perst 5.1.1" is **incorrect**. 

**ooGTxQ-1.0.0.jar IS already Perst 5.1.1** (or based on it), containing `org.garret.perst.continuous.CDatabase`. 

Evidence:
- `~/Projects/KissOO/../oodb/pom.xml` declares `<version>5.1.1</version>`
- `~/Projects/KissOO/libs/ooGTxQ-1.0.0.jar` contains: `org/garret/perst/continuous/CDatabase.class`
- `~/Projects/KissOO/src/main/precompiled/koo/core/database/StorageManager.java` already uses `CDatabase` and `ConflictException`
- `~/Projects/KissOO/../oodb/src/main/java/org/garret/perst/continuous/` contains full Perst 5.1.1 source

## Actual Problem Statement

The codebase **already uses Perst 5.1.1 (with CDatabase)**. No replacement is needed.

The actual work items are:

### 1. 64-bit OID Conversion (INCOMPLETE - Blocker for 2.4B objects)

**Location:** `~/Projects/KissOO/../oodb/src/main/java/org/garret/perst/impl/StorageImpl.java`

**Issue:** Perst `StorageImpl.reserveIds()` truncates `indexSize` to `int` at 7 locations:

```java
// RootPage fields (lines ~5407-5419)
int  indexSize;       // size of object index - STILL INT (32-bit)
int  shadowIndexSize; // size of object index - STILL INT (32-bit)
int  indexUsed;       // used part of the index - STILL INT (32-bit)
```

**Problem:** With 64-bit OIDs, `indexSize` can exceed `Integer.MAX_VALUE` (2^31-1 ≈ 2.1B entries).
At 8 bytes per OID entry, `Integer.MAX_VALUE * 8L = 16GB` index size maximum.

**7 Locations Truncating to int (from AGENTS.md):**
1. Line 226: `int oldIndexSize = header.root[curr].indexSize;`
2. Line 236: `if (currIndexSize >= header.root[curr].indexSize)`
3. Line 240: `header.root[curr].indexSize = newIndexSize;`
4. Line 259: `int oldIndexSize = header.root[curr].indexSize;`
5. Line 277: `header.root[curr].indexSize = newIndexSize;`
6. Line 650: `oldIndexSize = header.root[curr].indexSize;`
7. Line 676: `header.root[curr].indexSize = newIndexSize;`

**Plus:** B-tree conversion completed but `StorageImpl.reserveIds()` truncates indexSize to int at 7 locations.

**Impact:** Cannot insert more than ~2.4 billion objects before `indexSize` overflow.

**Required Fix:** Change `indexSize`, `shadowIndexSize`, `indexUsed` from `int` to `long` in `RootPage` and all related code.

**Cache Constraint:** Must cap cache at 60% heap to prevent OOM (per AGENTS.md).

### 2. TransactionContainer Pattern Already In Place

**Status:** ✓ WORKING - Already implemented correctly

```java
// Current code (CORRECT):
TransactionContainer tc = PerstStorageManager.createContainer();
tc.addInsert(owner);
tc.addInsert(owner.getPerstUser());
PerstStorageManager.store(tc); // Uses CDatabase.commit()
```

**Optimistic Locking:** `CDatabase.commit()` throws `ConflictException` on version mismatch (pre-lock validation).

### 3. CDatabase Already Integrated

**Status:** ✓ WORKING - No changes needed

```java
// StorageManager already delegates to CDatabase
CDatabase cdb = CDatabase.instance;
cdb.open(storage, indexPath);
long version = cdb.commit(container); // Optimistic locking built-in
```

### 4. Pure OO Navigation Already Enforced

**Status:** ✓ WORKING - No changes needed

```java
// CORRECT: Use OO navigation
actor.getHouses()     // Perst Link collection
pu.getActor()         // PerstUser -> Actor
actor.getPerstUser()  // Actor -> PerstUser

// WRONG: SQL-style ID filtering (not used)
// filter(houses, h -> h.getOwnerId() == ownerId)  // Does not exist
```

## Correct Task Definition

Since ooGTxQ **IS** Perst 5.1.1 already, the task is:

### Task A: Fix 64-bit Truncation in StorageImpl.reserveIds()

**Goal:** Enable >2.4B object insertion by fixing int→long truncation

**Files to modify:**
- `~/oodb/src/main/java/org/garret/perst/impl/StorageImpl.java`
  - Change `RootPage.indexSize` from `int` to `long`
  - Change `RootPage.shadowIndexSize` from `int` to `long`  
  - Change `RootPage.indexUsed` from `int` to `long`
  - Update all 7+ locations where these are assigned/read
  - Update `Header.pack()` / `unpack()` to handle long→byte conversion
  - Update cache size calculation to cap at 60% heap

**Verification:**
- [ ] Compile ooGTxQ project with long-based RootPage
- [ ] Test with 2.4B object insertion (batched 500K/batch)
- [ ] Verify no rollback log overflow
- [ ] Verify cache capped at 60% heap

### Task B: Verify Current Implementation (NO CHANGES)

**Goal:** Confirm system works correctly with existing Perst 5.1.1 + CDatabase

**Checklist:**
- [ ] StorageManager uses CDatabase ✓
- [ ] ConflictException caught in store() ✓
- [ ] TransactionContainer pattern used ✓
- [ ] Pure OO navigation (no ID filtering) ✓
- [ ] Perst Link collections used ✓
- [ ] CVersion base class used ✓
- [ ] Indexable, FullTextSearchable annotations used ✓

### Task C: Performance Tuning

**Goal:** Ensure cache doesn't cause OOM

**Changes:**
- `PerstConfig.java` - Add cache size limit (60% heap)
- Monitor cache eviction under load

## What Does NOT Need to Be Changed

### Business Layer (100% unchanged)
- ✓ Domain classes (Owner, Cleaner, House, Booking, Schedule)
- ✓ Managers (all *Manager classes)
- ✓ Services (CleaningService, Users, PermissionService)
- ✓ Frontend (Svelte 5)
- ✓ API contracts

### Database API (Already correct)
- ✓ CDatabase usage
- ✓ TransactionContainer pattern
- ✓ ConflictException handling
- ✓ Optimistic locking

## Risk Assessment

### HIGH RISK (Must Fix)
🔴 **64-bit truncation in StorageImpl.reserveIds()** - Blocks >2.4B objects
- **Mitigation:** Change int→long in RootPage, test thoroughly
- **Impact:** Core database layer, affects all operations
- **Testing needed:** Batched insertion of 2.4B+ objects

### LOW RISK (Already working)
🟢 **CDatabase integration** - Already implemented correctly
🟢 **Optimistic locking** - Already working (ConflictException)
🟢 **Pure OO navigation** - Already enforced
🟢 **Perst Link collections** - Already working

## Timeline

### Option 1: Fix 64-bit Only (Recommended)
- **Day 1-2:** Modify StorageImpl.java (int→long)
- **Day 3:** Test with large insertion batches
- **Day 4:** Performance tuning (cache limits)
- **Day 5:** Integration testing
- **Total: 5 days**

### Option 2: "Replace" (NOT NEEDED)
- **0 days** - Already using Perst 5.1.1

## Verification Steps

### 64-bit Fix Verification
```bash
# Compile ooGTxQ with long-based RootPage
cd ~/oodb
mvn clean install

# Test with batched insertion (500K/batch, 5 batches = 2.5M objects)
./bld develop
# Monitor for:
# - No Integer overflow in reserveIds()
# - Cache stays under 60% heap
# - No rollback log overflow
```

### Current Implementation Verification
```bash
# Start system
./bld develop

# Test endpoints
curl http://localhost:8080/owner/list
curl -X POST http://localhost:8080/owner/create -d {...}

# Verify optimistic locking
# Two concurrent updates to same object -> one gets ConflictException
```

## Open Questions

1. **Is ooGTxQ a custom fork?** If so, what changes were made vs. vanilla Perst 5.1.1?
   - Check: `git log --oneline -- libs/ooGTxQ-1.0.0.jar`
   
2. **Are the 7 int locations the complete list?** Need full audit of StorageImpl for int→long changes

3. **What about perst-update task?** It downloads "ooGTxQ-1.0.1.jar" - is that also Perst 5.1.1?
   - Check: Tasks.java `perstUpdate()` method

## Summary

**Current state:** System already uses Perst 5.1.1 with CDatabase. 
**Actual problem:** 64-bit truncation in `StorageImpl.reserveIds()` prevents >2.4B object insertion.
**Solution:** Change `RootPage.indexSize`, `shadowIndexSize`, `indexUsed` from `int` to `long`.
**No changes needed** to business layer, CDatabase usage, or TransactionContainer pattern.
