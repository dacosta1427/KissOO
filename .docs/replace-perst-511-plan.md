# Plan: Replace ooGTxQ 64-bit with Perst 5.1.1 32-bit

**Date:** 2026-04-28  
**Task ID:** REPLACE_OODB_64BIT_WITH_PERST_511_32BIT  
**Status:** PLANNING  

## Goal

Replace the current ooGTxQ 64-bit OODBMS layer (which has incomplete 64-bit support) with Perst 5.1.1 32-bit, **changing ONLY the database layer** with ZERO business logic changes.

## Business Constraint

- **ZERO changes to business logic** (services, managers, domain entities)
- **ONLY the db layer changes** (StorageManager, PerstConnection, config)
- All existing API contracts remain unchanged
- Current code uses Perst Link for collections - this remains valid in Perst 5.1.1

## Current State

### Database Layer (to be replaced)
- **Current:** ooGTxQ-1.0.0.jar (incomplete 64-bit, still uses 32-bit int internally)
- **Location:** libs/ooGTxQ-1.0.0.jar
- **Issues:** Casts long→int, 7 int→long fixes needed (see AGENTS.md)

### Business Layer (stays unchanged)
- **Domain:** domain.actor.*, domain.oov.* (all use Perst Link)
- **Managers:** *Manager classes (all use StorageManager)
- **Services:** backend/services/*.groovy (all use StorageManager)
- **Core:** koo.core.database.* (StorageManager, PerstConnection)

## Replacement Target

- **Target:** Perst 5.1.1 32-bit
- **Package:** org.garret.perst (same as current Perst API)
- **Key Interfaces:** Storage, Database (32-bit OIDs)

## Why This Works

1. **API Compatibility**: Perst 5.1.1 uses same org.garret.perst package
2. **Link Collections**: Perst 5.1.1 supports org.garret.perst.Link (unchanged)
3. **Storage Interface**: Same getObjectByOID(int oid) signature
4. **CVersion**: Same base class for versioned objects
5. **Indexable**: Same annotation for B-tree indexes
6. **FullTextSearchable**: Same annotation for Lucene
7. **TransactionContainer**: Same batch operation pattern

## What Changes (DB Layer Only)

### Files to Replace
1. `libs/ooGTxQ-1.0.0.jar` → `perst-5.1.1.jar`
2. `src/main/precompiled/koo/core/database/StorageManager.java`
3. `src/main/precompiled/koo/core/database/PerstConnection.java`
4. `src/main/precompiled/koo/config/PerstConfig.java`

### Files to Modify (DB Layer Only)

#### 1. StorageManager.java
**Current:** Delegates to CDatabase (ooGTxQ)
**New:** Delegates to Perst 5.1.1 Storage/Database

```java
// Current (ooGTxQ)
CDatabase cdb = CDatabase.instance;
cdb.open(storage, indexPath);
cdb.commit(container);

// New (Perst 5.1.1)
Database db = Database.create(storage);
db.beginTransaction();
db.commit();
```

Changes needed:
- Remove CDatabase dependency (ooGTxQ-specific)
- Use Perst 5.1.1 Database class (32-bit, native OID int)
- Keep same public API (createContainer, store, getAll, getByOid)
- No cast needed: getObjectByOID takes int, we pass int
- Transaction handling via Perst native transactions

#### 2. PerstConnection.java
**Current:** Wraps CDatabase, provides no-op commit/rollback/close
**New:** Can be simplified or removed

Options:
- Remove PerstConnection entirely (services use StorageManager directly)
- Keep as thin wrapper for framework compatibility
- No-op methods can be removed (Perst handles transactions)

#### 3. PerstConfig.java
**Current:** CDatabase-specific settings
**New:** Perst 5.1.1 settings

Changes:
- Remove CDatabase options
- Keep database path, cache size settings
- Add Perst 5.1.1 specific config if needed

#### 4. PerstInit.java (optional)
**Current:** May have CDatabase-specific init
**New:** Use Perst 5.1.1 init

Check if any CDatabase-specific code exists.

## What Stays Exactly the Same (Business Layer)

### Domain Classes (NO CHANGES)
- `domain.actor.owner.Owner` (uses Link houses)
- `domain.actor.cleaner.Cleaner` (uses Link schedules)
- `domain.oov.house.House` (uses Link bookings)
- `domain.oov.house.Booking` (object references)
- `domain.actor.cleaner.CostProfile` (standalone)
- `domain.actor.cleaner.Schedule` (object references)
- All use `org.garret.perst.Link` ← **Valid in Perst 5.1.1**

### Managers (NO CHANGES)
- `PerstUserManager` (calls StorageManager)
- `ActorManager` (calls StorageManager)
- `OwnerManager` (inherits)
- `CleanerManager` (inherits)
- `HouseManager` (inherits)
- `BookingManager` (inherits)
- `ScheduleManager` (inherits)
- `PhoneManager` (calls StorageManager)

All use `StorageManager.*` ← **API unchanged**

### Services (NO CHANGES)
- `CleaningService.groovy`
- `Users.groovy`
- `PermissionService.groovy`
- `PerstService.java`
- `PerstInit.groovy`
- `LoadTestdata.groovy`
- `Crud.groovy`

All use `StorageManager.*` ← **API unchanged**

### Core Framework (PARTIAL - StorageManager only)
- `BaseManager` (NO CHANGE - uses StorageManager)
- `StorageManager` (CHANGE - db layer implementation)
- `PerstConnection` (CHANGE - db layer implementation)

## Step-by-Step Implementation Plan

### Phase 1: Setup and Analysis (1 day)
1. Download Perst 5.1.1 JAR
2. Analyze Perst 5.1.1 API vs current usage
3. Verify Link, CVersion, annotations are compatible
4. Document differences in storage API

### Phase 2: StorageManager Rewrite (2 days)
1. Backup current StorageManager.java
2. Rewrite to use Perst 5.1.1 Database class
3. Implement same public methods:
   - `createContainer()` → Perst transaction
   - `store(tc)` → Perst commit
   - `getAll(Clazz)` → Perst extent iteration
   - `getByOid(Clazz, oid)` → Perst getObjectByOID
   - `find(Clazz, field, val)` → Perst index lookup
4. Ensure TransactionContainer works with Perst
5. Test: StorageManagerTest

### Phase 3: PerstConnection Update (0.5 day)
1. Option A: Remove entirely (simplest)
2. Option B: Update to Perst 5.1.1 wrapper
3. Update service method signatures if removing

### Phase 4: PerstConfig Update (0.5 day)
1. Remove CDatabase-specific config
2. Keep database path, cache settings
3. Add Perst 5.1.1 config options

### Phase 5: Integration Testing (1 day)
1. Replace JAR in libs/
2. Deploy to test environment
3. Run all service tests
4. Verify data persistence
5. Verify queries work
6. Verify transactions work

### Phase 6: Validation (1 day)
1. Create test data (owners, houses, bookings)
2. Test CRUD operations
3. Test batch operations
4. Test queries (by OID, by field)
5. Test relationships (Link collections)
6. Verify no business logic breakage

## Test Plan

### Unit Tests (StorageManager)
```java
@Test
public void testStorageManagerCreateContainer() {
    TransactionContainer tc = StorageManager.createContainer();
    assertNotNull(tc);
}

@Test
public void testStorageManagerInsertAndRetrieve() {
    House house = new House(...);
    TransactionContainer tc = StorageManager.createContainer();
    tc.addInsert(house);
    boolean success = StorageManager.store(tc);
    assertTrue(success);
    House retrieved = StorageManager.getByOid(House.class, house.getOid());
    assertNotNull(retrieved);
    assertEquals(house.getName(), retrieved.getName());
}

@Test
public void testStorageManagerGetAll() {
    Collection<House> houses = StorageManager.getAll(House.class);
    assertNotNull(houses);
}

@Test
public void testStorageManagerUpdate() {
    TransactionContainer tc = StorageManager.createContainer();
    tc.addUpdate(obj);
    boolean success = StorageManager.store(tc);
    assertTrue(success);
}

@Test
public void testStorageManagerDelete() {
    TransactionContainer tc = StorageManager.createContainer();
    tc.addDelete(obj);
    boolean success = StorageManager.store(tc);
    assertTrue(success);
}
```

### Integration Tests (Services)
- Test CleaningService still works
- Test Users service CRUD
- Test batch operations via PerstService

### Smoke Tests (Manual)
1. Start application
2. Login
3. Navigate to Owners list
4. Create new Owner
5. Create House for Owner
6. Create Booking
7. Verify all data persists
8. Restart application
9. Verify data still exists

## Files Changed Summary

### Replaced Files
- `libs/ooGTxQ-1.0.0.jar` → `libs/perst-5.1.1.jar`

### Modified Files (DB Layer Only)
1. `src/main/precompiled/koo/core/database/StorageManager.java`
   - Complete rewrite to use Perst 5.1.1
   - Keep same public API
2. `src/main/precompiled/koo/core/database/PerstConnection.java`
   - Remove or update for Perst 5.1.1
3. `src/main/precompiled/koo/config/PerstConfig.java`
   - Remove CDatabase options
   - Keep database path, cache settings

### Deleted Files (Optional)
- `docs/CDatabase-API-Guide.md` (CDatabase-specific documentation)

### Unchanged Files (Business Layer)
✓ All domain classes (Owner, Cleaner, House, Booking, etc.)  
✓ All managers (PerstUserManager, ActorManager, etc.)  
✓ All services (CleaningService, Users, etc.)  
✓ BaseManager  
✓ Application configuration  
✓ Frontend  
✓ Tests

## Risk Assessment

### Low Risk
✅ **Perst Link compatibility**: Perst 5.1.1 supports Link (tried and true)  
✅ **CVersion compatibility**: Same base class  
✅ **Annotations**: @Indexable, @FullTextSearchable exist in Perst 5.1.1  
✅ **API stability**: org.garret.perst package is stable

### Medium Risk
⚠️ **Transaction handling**: Need to verify TransactionContainer works with Perst native transactions  
⚠️ **Index lookup**: Verify find() method works with Perst 5.1.1 indexes  
⚠️ **Performance**: Perst 5.1.1 may have different performance characteristics

### High Risk
🔴 **Breaking changes**: If Perst 5.1.1 removed/changed key APIs

**Mitigation:**
- Download Perst 5.1.1, verify API compatibility before coding
- Keep old JAR until new one is fully tested
- Maintain StorageManager public API (services won't change)

## Verification Checklist

### Pre-Implementation
- [ ] Download Perst 5.1.1 JAR
- [ ] Verify org.garret.perst.Storage interface exists
- [ ] Verify org.garret.perst.Link class exists
- [ ] Verify org.garret.perst.CVersion exists
- [ ] Verify @Indexable, @FullTextSearchable annotations exist
- [ ] Verify Database class has create() method

### Post-Implementation
- [ ] StorageManager compiles
- [ ] All tests pass
- [ ] Application starts
- [ ] Can create Owner
- [ ] Can create House
- [ ] Can create Booking
- [ ] Can query by OID
- [ ] Can query by field
- [ ] Transactions work (atomic batch)
- [ ] Data persists after restart

### Business Validation (Zero Changes Expected)
- [ ] Owners page loads
- [ ] Can create new Owner
- [ ] Can create House for Owner
- [ ] Can create Booking
- [ ] Can assign Schedule to Cleaner
- [ ] All existing functionality works

## Rollback Plan

If issues arise:
1. Restore `libs/ooGTxQ-1.0.0.jar`
2. Restore `StorageManager.java` backup
3. Restore `PerstConnection.java` backup
4. Restart application
5. System returns to previous state

## Success Criteria

**Definition of Done:**
1. ✅ Perst 5.1.1 JAR deployed
2. ✅ StorageManager uses Perst 5.1.1 Database (no CDatabase)
3. ✅ All business services unchanged
4. ✅ All tests pass
5. ✅ Manual smoke tests pass
6. ✅ Data persistence verified
7. ✅ No business logic changes (confirmed by git diff)

## Timeline

**Total: 6 days**
- Phase 1: Setup & Analysis (1 day)
- Phase 2: StorageManager Rewrite (2 days)
- Phase 3: PerstConnection Update (0.5 day)
- Phase 4: PerstConfig Update (0.5 day)
- Phase 5: Integration Testing (1 day)
- Phase 6: Validation (1 day)

## Open Questions

1. **Perst 5.1.1 availability**: Do we have the JAR file?
2. **Transaction semantics**: Does Perst 5.1.1 handle nested transactions like CDatabase?
3. **Index lookups**: Does Perst 5.1.1 find() work the same as CDatabase.find()?

**Action:** Download Perst 5.1.1, review API docs, verify compatibility.

---

## Summary

This plan replaces **ONLY the database layer** (StorageManager, PerstConnection, config) from ooGTxQ 64-bit to Perst 5.1.1 32-bit. 

**Business layer remains 100% unchanged:**
- No service modifications
- No manager modifications
- No domain class modifications
- No API changes

The key is that both ooGTxQ and Perst 5.1.1 use the same `org.garret.perst` package, same `Link` class for collections, same `CVersion` base class, and same annotations. StorageManager serves as the adapter layer — we rewrite its internals to use Perst 5.1.1 instead of CDatabase, while keeping its public API identical.