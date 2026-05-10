
# KissOO Database Access Architecture Schema

Generated: 2026-04-28_142412

## Overview

This schema shows how services and managers interact with the database through
the unified CDatabase access layer (replacement for legacy UniDbMgr).

```

╔══════════════════════════════════════════════════════════════════════════╗
║                          LAYER 1: SVELTE 5 FRONTEND                       ║
╚══════════════════════════════════════════════════════════════════════════╝
                                    │
                                    │ JSON calls with _uuid
                                    │ (no IDs, no _ownerId, _cleanerId, etc.)
                                    ▼

╔══════════════════════════════════════════════════════════════════════════╗
║                          LAYER 2: BACKEND SERVICES                       ║
║                            (Groovy / Java)                               ║
╚══════════════════════════════════════════════════════════════════════════╝
║                                                                            ║
║  CleaningService.groovy    Users.groovy    PermissionService.groovy      ║
║  PerstInit.groovy          LoadTestdata.groovy   PerstService.java       ║
║  Crud.groovy               FileUpload.groovy   MyGroovyService.groovy    ║
║                                                                            ║
║  ┌─────────────────────────────────────────────────────────────────────┐  ║
║  │  void getCleaners(JSONObject injson, JSONObject outjson,             │  ║
║  │                    Connection db, ProcessServlet servlet) {          │  ║
║  │    // db parameter UNUSED for Perst operations!                      │  ║
║  │    Collection<Cleaner> cleaners =                                    │  ║
║  │        StorageManager.getAll(Cleaner.class);  // ← STORAGEMANAGER   │  ║
║  │  }                                                                   │  ║
║  └─────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
║  Pattern:                                                                ║
║    - Get PerstUser from session: servlet.getUserData("perstUser")        ║
║    - Get Actor: pu.getActor()                                            ║
║    - Check permissions via Actor/Agreement                               ║
║    - Use StorageManager for ALL Perst operations                         ║
║    - NEVER use db parameter for Perst (it's for SQL compatibility)        ║
║                                                                            ║
╚══════════════════════════════════════════════════════════════════════════╝
                                    │
                                    │ StorageManager static calls
                                    │ (createContainer, store, getAll, etc.)
                                    ▼

╔══════════════════════════════════════════════════════════════════════════╗
║                    LAYER 3: MANAGER LAYER                                 ║
╚══════════════════════════════════════════════════════════════════════════╝
║                                                                            ║
║  ┌─────────────────────────────────────────────────────────────────────┐  ║
║  │  StorageManager (Singleton)                                           │  ║
║  │  - createContainer()     → TransactionContainer                       │  ║
║  │  - store(tc)             → CDatabase.commit(tc)                      │  ║
║  │  - getAll(Clazz)         → CDatabase.select(Clazz, "true")           │  ║
║  │  - getByOid(Clazz, oid) → CDatabase.find(Clazz, "oid", oid)          │  ║
║  │  - getByUuid(Clazz, uuid)→ CDatabase.find(Clazz, "_uuid", uuid)      │  ║
║  │  - find(Clazz, field, val) → CDatabase.find(Clazz, field, val)       │  ║
║  └─────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
║  PerstUserManager            ActorManager           PhoneManager           ║
║  (authenticate)               (getAll)                (getAll)            ║
║  (create)                     (getByName)             (getByOid)          ║
║  (getByKey)                   (getByUuid)             (create)            ║
║  (getByOid)                   (update)                (update)            ║
║  (update)                     (delete)                (delete)            ║
║                                                                            ║
║  OwnerManager    CleanerManager    ScheduleManager    HouseManager        ║
║  (inherits)      (inherits)       (inherits)         (inherits)          ║
║                                                                            ║
║  BookingManager  CostProfileManager                                        ║
║  (inherits)      (inherits)                                                ║
║                                                                            ║
║  PATTERN: All managers use StorageManager + TransactionContainer          ║
║                                                                            ║
║  Example:                                                                 ║
║  ┌─────────────────────────────────────────────────────────────────────┐  ║
║  │  public static boolean update(PerstUser user) {                     │  ║
║  │    if (user == null) return false;                                 │  ║
║  │    TransactionContainer tc = StorageManager.createContainer();      │  ║
║  │    tc.addUpdate(user);      // Add to batch                        │  ║
║  │    return StorageManager.store(tc);  // Atomic commit              │  ║
║  │  }                                                                  │  ║
║  └─────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
╚══════════════════════════════════════════════════════════════════════════╝
                                    │
                                    │ CDatabase methods
                                    │ (commit, select, find, insert, update, delete)
                                    ▼

╔══════════════════════════════════════════════════════════════════════════╗
║                      LAYER 4: CDATABASE (ooGTxQ)                         ║
║               Unified Database Access Layer (Replaces UniDbMgr)          ║
╚══════════════════════════════════════════════════════════════════════════╝
║                                                                            ║
║  CDatabase.instance  (Singleton)                                           ║
║                                                                            ║
║  Methods:                                                                  ║
║  ─────────────────────────────────────────────────────────────────────────║
║                                                                            ║
║  TRANSACTION CONTROL:                                                     ║
║    beginTransaction()       - Start transaction                           ║
║    commitTransaction()      - Commit current transaction                  ║
║    rollbackTransaction()    - Rollback current transaction                ║
║    isInTransaction()        - Check transaction state                     ║
║                                                                            ║
║  STORE (TransactionContainer):                                            ║
║    commit(tc)            - Atomic batch insert/update/delete              ║
║                            Returns version number                        ║
║                                                                            ║
║  RETRIEVE:                                                                 ║
║    select(Clazz, "true")     - Get all objects of class (Extent)      ║
║    find(Clazz, field, val)   - Index lookup (B-tree)                  ║
║    find(Clazz, field, val)   - Lucene full-text search                ║
║    getByUuid(Clazz, uuid)    - Find by UUID (via Lucene)              ║
║                                                                            ║
║  CRUD (Direct object operations):                                         ║
║    insert(obj)             - Insert new object                           ║
║    update(obj)             - Update existing object                      ║
║    delete(obj)             - Delete object                               ║
║                                                                            ║
║  VERSIONING:                                                               ║
║    getLatestByOid(oid)      - Get latest version from history             ║
║    getCurrent(history)      - Get current from version history            ║
║    getVersionForUpdate(hist) - Get working copy for update                ║
║                                                                            ║
║  FULL-TEXT SEARCH (Lucene):                                               ║
║    fullTextSearch(query, limit)  - Search indexed fields                  ║
║    optimizeFullTextIndex()       - Optimize Lucene index                  ║
║    restoreFullTextIndex()        - Rebuild Lucene index                   ║
║                                                                            ║
║  UNDERLYING STORAGE:                                                      ║
║    ┌─────────────────────────────────────────────────────────────────┐    ║
║    │  Perst CDatabase                                                 │    ║
║    │  ├── Lin (Current)     - Current version timeline              │    ║
║    │  ├── Lex (History)     - Historical versions                   │    ║
║    │  └── Lucene Index      - Full-text search                     │    ║
║    └─────────────────────────────────────────────────────────────────┘    ║
║                                                                            ║
║  PERSISTENT STORAGE:                                                       ║
║    ┌─────────────────────────────────────────────────────────────────┐    ║
║    │  Perst Storage (ooGTxQ)                                          │    ║
║    │  ├── Objects stored as CVersion instances                      │    ║
║    │  ├── @Indexable fields → B-tree indexes                         │    ║
║    │  ├── @FullTextSearchable → Lucene indexes                      │    ║
║    │  └── OIDs (64-bit) - unique object identifiers                  │    ║
║    └─────────────────────────────────────────────────────────────────┘    ║
║                                                                            ║
╚══════════════════════════════════════════════════════════════════════════╝
```

## Key Data Flow Examples

### Example 1: Get All Cleaners (Read Operation)

```
Frontend (Svelte 5)
    │
    │ Server.call("services.domain.CleaningService", "getCleaners", {})
    ▼
CleaningService.getCleaners(injson, outjson, Connection db, servlet)
    │
    │ // db parameter NOT used!
    │ StorageManager.getAll(Cleaner.class)
    ▼
StorageManager.getAll(Cleaner.class)
    │
    │ CDatabase.select(Cleaner.class, "true")
    ▼
CDatabase.select()
    │
    │ Iterate class extent via ExtentIterator
    ▼
Perst Storage (returns Cleaner objects)
    │
    ▼
Back up through layers → JSON response → Frontend
```

### Example 2: Create New Owner (Write Operation)

```
Frontend (Svelte 5)
    │
    │ Server.call("services.Users", "createUser", {username, password, ...})
    ▼
Users.createUser(injson, outjson, Connection db, servlet)
    │
    │ // Get actor from session
    │ PerstUser pu = (PerstUser)servlet.getUserData("perstUser")
    │ Owner owner = new Owner(...)
    │ PerstUser user = new PerstUser(username, password, owner)
    │
    │ TransactionContainer tc = StorageManager.createContainer()
    │ tc.addInsert(owner)
    │ tc.addInsert(user)
    │ StorageManager.store(tc)
    ▼
StorageManager.store(tc)
    │
    │ CDatabase.commit(tc)
    ▼
CDatabase.commit()
    │
    │ 1. Validate optimistic locks
    │ 2. Assign OIDs if new
    │ 3. Write to Lin (current)
    │ 4. Update indexes
    │ 5. Write to transaction log
    ▼
Perst Storage (persisted)
    │
    ▼
Success → Return to frontend
```

### Example 3: Update Booking (Atomic Batch)

```
Booking booking = StorageManager.getByOid(Booking.class, oid)
booking.setStatus("confirmed")

Schedule schedule = new Schedule(...)
schedule.setBooking(booking)

TransactionContainer tc = StorageManager.createContainer()
tc.addUpdate(booking)      // Update existing
tc.addInsert(schedule)     // Create new
tc.addDelete(oldSchedule)  // Delete old

StorageManager.store(tc)   // ALL succeed or ALL fail
    │
    ▼
CDatabase.commit(tc)
    │
    ├─ Bookings index updated
    ├─ Schedules index updated
    ├─ Lucene indexes updated
    └─ Version history (Lex) recorded
```

## Critical Design Decisions

### 1. Why Connection db Parameter is Unused

**Question:** Why do services receive `Connection db` if it's never used?

**Answer:** 
- Maintains API compatibility with SQL-based services
- Framework (ProcessServlet) injects it automatically
- Services can use it for SQL queries if needed (hybrid mode)
- In pure-OO mode, it's simply ignored
- Keeps method signature consistent across all services

### 2. Why StorageManager is Static

**Question:** Why is StorageManager static instead of injectable?

**Answer:**
- Matches Perst's singleton CDatabase pattern
- Simpler - no DI framework needed
- Thread-safe by design (CDatabase handles concurrency)
- Perst's CDatabase is itself a singleton

### 3. Why TransactionContainer Pattern

**Question:** Why wrap everything in TransactionContainer?

**Answer:**
- Atomicity: All-or-nothing batch operations
- Optimistic locking: Automatic conflict detection
- Performance: Single commit for multiple operations
- Versioning: All changes get sequential version numbers
- History: Lin/Lex track all changes for audit/recovery

### 4. UniDbMgr → CDatabase Evolution

**Legacy (pre-v4.1.0):**
```
Application → UnifiedDBManager → CDatabase → Storage
  (wrapper)       (thin layer)                 (added complexity)
```

**Current (v4.1.0+):**
```
Application → CDatabase → Storage
  (direct)       (rich API)      (simplified)
```

**Benefits:**
- Removed indirection layer
- CDatabase has all needed methods
- Fewer classes to maintain
- Clearer ownership
- ThreadLocal transactions in CDatabase

## Service-to-DB Access Matrix

| Service | StorageManager Calls | Direct DB Access | OK? |
|---------|---------------------|------------------|-----|
| CleaningService | getAll, getByOid, createContainer, store | NONE | ✅ |
| Users | getAll, find, createContainer, store | NONE | ✅ |
| PermissionService | getAll, getByOid, find, createContainer, store | NONE | ✅ |
| PerstService | healthCheck, getStats | NONE | ✅ |
| PerstInit | getAll, createContainer, store | NONE | ✅ |
| LoadTestdata | getAll, getByOid, createContainer, store | NONE | ✅ |
| Crud | via PhoneManager | NONE | ✅ |
| ActorService | (if used) | NONE | ✅ |

## Manager Access Matrix

| Manager | StorageManager Methods Used | Pattern Followed? |
|---------|---------------------------|-------------------|
| PerstUserManager | getAll, find, getByOid, createContainer, store | ✅ |
| ActorManager | getAll, find, createContainer, store | ✅ |
| OwnerManager | (inherits from ActorManager) | ✅ |
| CleanerManager | (inherits from ActorManager) | ✅ |
| ScheduleManager | (inherits from BaseManager) | ✅ |
| HouseManager | (inherits from BaseManager) | ✅ |
| BookingManager | (inherits from BaseManager) | ✅ |
| CostProfileManager | (inherits from BaseManager) | ✅ |
| PhoneManager | getAll, getByOid, createContainer, store | ✅ |

## Data Flow Diagram (ASCII)

```
┌─────────────────┐
│  Svelte 5       │
│  Frontend       │
│  (Browser)      │
└────────┬────────┘
         │ JSON + _uuid
         │ (no IDs in params)
         ▼
┌─────────────────┐    ┌────────────────────┐
│   Backend       │    │  Session Cache     │
│   Services      │    │  (UserData)        │
│   (Groovy)      │◄───┤  PerstUser         │
│                 │    │  Actor             │
└────────┬────────┘    └────────────────────┘
         │
         │ StorageManager.method()
         │ (never use Connection db!)
         ▼
┌─────────────────┐
│ StorageManager  │  ← Singleton, static
│ (static)        │     ─ createContainer()
│                 │     ─ store(tc)
│                 │     ─ getAll(clazz)
│                 │     ─ getByOid(clazz, oid)
│                 │     ─ getByUuid(clazz, uuid)
└────────┬────────┘
         │
         │ CDatabase.commit() / .select() / .find()
         ▼
┌─────────────────┐
│   CDatabase     │  ← Singleton (ooGTxQ)
│   (UniDbMgr)    │      ─ commit(tc)
│                 │      ─ select(...)
│                 │      ─ find(...)
│                 │      ─ insert(obj)
│                 │      ─ update(obj)
│                 │      ─ delete(obj)
└────────┬────────┘
         │
         │ Lin (current) + Lex (history)
         │ Lucene indexes
         ▼
┌─────────────────┐
│   Perst Storage │  ← OODBMS (64-bit OIDs)
│   (ooGTxQ)      │      ─ CVersion objects
│                 │      ─ @Indexable fields
│                 │      ─ @FullTextSearchable
└─────────────────┘
```

## Migration Guide (if adding new service)

### DO:
```groovy
import koo.core.database.StorageManager

class MyNewService {
    void myMethod(JSONObject injson, JSONObject outjson, 
                  Connection db, ProcessServlet servlet) {
        
        // 1. Get PerstUser from session
        PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
        
        // 2. Get Actor for authorization
        Actor actor = pu.getActor()
        
        // 3. Use StorageManager for Perst operations
        Collection<MyObject> items = 
            StorageManager.getAll(MyObject.class)
        
        // 4. For writes, use TransactionContainer
        MyObject obj = new MyObject(...)
        TransactionContainer tc = StorageManager.createContainer()
        tc.addInsert(obj)
        StorageManager.store(tc)
        
        // 5. DO NOT use db parameter for Perst!
        //    (It's for SQL compatibility only)
    }
}
```

### DON'T:
```groovy
class BadService {
    void badMethod(JSONObject injson, JSONObject outjson,
                   Connection db, ProcessServlet servlet) {
        
        // WRONG: Using db for Perst (won't work!)
        db.insert(obj);  // NO! db is for SQL only
        
        // WRONG: Direct storage access
        storage.insert(obj);  // NO! Use StorageManager
        
        // WRONG: SQL-style filtering
        def all = StorageManager.getAll(House.class)
        def mine = all.findAll { it.ownerId == myId }  // NO! Use OO navigation
        def mineCorrect = actor.getHouses()  // YES! Pure OO
    }
}
```

## Query Cheat Sheet

### Get All Objects
```groovy
Collection<Item> items = StorageManager.getAll(Item.class)
```

### Get by OID
```groovy
Item item = StorageManager.getByOid(Item.class, oid)
```

### Get by Field
```groovy
Item item = StorageManager.find(Item.class, "name", "value")
```

### Insert
```groovy
TransactionContainer tc = StorageManager.createContainer()
tc.addInsert(obj)
StorageManager.store(tc)
```

### Update
```groovy
TransactionContainer tc = StorageManager.createContainer()
tc.addUpdate(obj)
StorageManager.store(tc)
```

### Delete
```groovy
TransactionContainer tc = StorageManager.createContainer()
tc.addDelete(obj)
StorageManager.store(tc)
```

### Batch Operations
```groovy
TransactionContainer tc = StorageManager.createContainer()
tc.addInsert(newObj)
tc.addUpdate(existingObj)
tc.addDelete(oldObj)
boolean success = StorageManager.store(tc)  // Atomic!
```

## Summary

- **UniDbMgr (legacy)** → **CDatabase (current)** → unified access layer
- **All services** use **StorageManager** (static) for Perst operations
- **All managers** use **StorageManager** + **TransactionContainer**
- **Zero direct storage access** in application code
- **Connection db** parameter is unused for Perst (kept for SQL compatibility)
- **100% pattern consistency** across the codebase

This architecture ensures:
- Clean separation of concerns
- Atomic batch operations
- Optimistic locking
- Version history (Lin/Lex)
- Full-text search (Lucene)
- Pure OO navigation (no SQL-style ID filtering)

