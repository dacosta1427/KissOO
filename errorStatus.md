# KissOO Perst Integration - Current Status & Issues

## Executive Summary

KissOO is a fork of the KISS framework that adds Perst OODB integration. 

**Decision Made (2026-03-15):** Perst-only mode confirmed.
- SQLite remains for framework initialization only
- SQLite access is BLOCKED for application code
- All services use Perst directly
- Using Java for services (moving away from Groovy)

**Current Status (2026-03-16):**
- ✅ Perst is initialized in KissInit.init() (NOT init2 - critical fix!)
- ✅ Login service works
- ✅ CRUD operations work (add/edit/delete)
- ✅ Perst-only mode implemented (no database configured)
- ✅ JDBC pool skipped for Perst type
- ✅ Transaction handling fixed
- ✅ No core KISS framework modifications needed

---

## IMPORTANT: Architecture Decision (2026-03-15)

### The KISS Framework Creator's Recommendation

The KISS framework creator suggested a cleaner approach:
1. **Don't tell KISS you're using a database** - Don't configure any database in application.ini
2. **Use MainServlet.putEnvironment/getEnvironment** - Store Perst Storage handle there
3. **Keep all Perst code in precompiled** - No core modifications needed

### Implementation (REFACTORING IN PROGRESS)

**Original Approach (MODIFIED KISS CORE - NOT RECOMMENDED):**
- Modified Connection.java, MainServlet.java, ProcessServlet.java
- Hard to update KISS framework - merge conflicts

**New Approach (RECOMMENDED - NO CORE CHANGES):**
- No database configured in application.ini
- PerstStorageManager stores Storage in MainServlet environment
- All Managers use PerstStorageManager for persistence
- Easy to update KISS framework

### Files Changed for Refactor:
- Reverted: Connection.java, MainServlet.java, ProcessServlet.java
- Created: PerstStorageManager.java
- Modified: All Manager classes to use PerstStorageManager

---

## Problems Encountered & Solutions

### Problem 1: Groovy Reflection Issue
**Symptom:** `NoSuchMethodException: Login.login(PerstKissConnection, ...)`

**Cause:** GroovyClass.invoke() uses strict type matching - looks for exact parameter types, not compatible parent types.

**Solution:** 
1. Switched from Groovy Login.groovy to Java Login.java
2. Used reflection to load the Java class dynamically (to avoid build order issues)
3. ProcessServlet uses `Class.forName("mycompany.service.Login")` to load

**Files Changed:**
- Created `src/main/precompiled/mycompany/service/Login.java`
- Modified `ProcessServlet.login()` to use reflection

---

### Problem 2: PerstKissConnection Inheritance Not Working
**Symptom:** Even though `PerstKissConnection extends Connection`, Groovy couldn't find the method.

**Cause:** Groovy's static reflection doesn't support polymorphism like Java does.

**Solution:** Pass plain `Connection` to services, access Perst via `PerstContext.getInstance()` directly.

---

### Problem 3: NullPointerException on rollback()
**Symptom:** `Cannot invoke "java.sql.Connection.rollback()" because "this.conn" is null`

**Cause:** Perst-only mode creates Connection with null JDBC connection. Code tried to call rollback() on null.

**Solution:** Added Perst type checks before JDBC operations:
```java
// Only rollback for JDBC connections, not Perst
if (DB != null && MainServlet.getConnectionType() != Connection.ConnectionType.Perst) {
    DB.rollback();
}
```

**Locations Fixed:**
- ProcessServlet.successReturn() - commit()
- ProcessServlet.loginFailure() - rollback()
- ProcessServlet.run() - rollback() (JSONException case)
- ProcessServlet.run() - rollback() (error case)

---

### Problem 4: NullPointerException on commit()
**Symptom:** Same issue as rollback - commit() called on null JDBC connection.

**Solution:** Same as Problem 3 - added Perst type checks.

---

### Problem 5: Admin User Not Persisting
**Symptom:** Admin user created in KissInit but not found during login.

**Cause:** Transaction handling was incomplete in standard Storage mode. The beginTransaction() method only worked in CDatabase mode, not standard mode.

**Solution Applied:**
1. Fixed `PerstContext.beginTransaction()` to work in standard Storage mode
2. Fixed `PerstContext.commitTransaction()` to call `endThreadTransaction()` in standard mode
3. Fixed `PerstContext.rollbackTransaction()` to work in standard mode
4. Updated `KissInit.initDefaultUser()` to use proper transaction handling with begin/commit
5. Added verification step to confirm user was saved
6. Fixed `PerstInit.groovy` to use `beginTransaction()` instead of non-existent `startTransaction()`

**Files Changed:**
- `PerstContext.java` - Fixed transaction methods for standard Storage mode
- `KissInit.groovy` - Added proper transaction handling and verification
- `PerstInit.groovy` - Fixed method name

---

### Problem 6: Refactoring - Moving Storage Logic to PerstStorageManager

**Date:** 2026-03-15

**Issue:** Initially, storage logic was scattered in Managers (PerstUserManager, ActorManager, etc.). Each Manager had code like:
```java
PerstStorageManager.beginTransaction();
try {
    CDatabaseRoot root = PerstStorageManager.getRoot();
    root.userIndex.put(user);
    PerstStorageManager.commitTransaction();
} catch (Exception e) {
    PerstStorageManager.rollbackTransaction();
}
```

**Solution:** Created centralized storage methods in PerstStorageManager:
- `save(obj)` - Save object (auto-transaction)
- `saveInTransaction(obj)` - Save within existing transaction
- `delete(obj)` - Delete object (auto-transaction)
- `deleteInTransaction(obj)` - Delete within existing transaction

**Managers now just call:**
```java
PerstStorageManager.beginTransaction();
try {
    PerstStorageManager.saveInTransaction(user);
    PerstStorageManager.commitTransaction();
} catch (Exception e) {
    PerstStorageManager.rollbackTransaction();
}
```

**Files Changed:**
- Created `PerstStorageManager.java` with save/delete methods
- Updated `PerstUserManager.java`, `ActorManager.java`, `PhoneManager.java`, `BenchmarkDataManager.java`

---

### Problem 7: Perst Not Available When Services Call Managers

**Date:** 2026-03-15

**Symptom:** `PerstStorageManager.isAvailable()` returns false when services try to create records.

**Root Cause:** Perst must be initialized in `KissInit.init()`, NOT init2. When no database is configured, the KISS framework does NOT call init2().

**Status: DEBUGGING**

Added debug logging to PerstStorageManager to trace initialization:
- initialize() - logs when called and PerstConfig.isPerstEnabled() result
- getStorage() - logs lazy initialization attempts  
- isAvailable() - logs return value

Run the app and check console output to identify where initialization fails.

**Critical: Initialization Flow:**
```
MainServlet.contextInitialized()
    → reads application.ini
    → calls KissInit.init() 
    → opens database (if configured)
    → calls KissInit.init2(db)  ← PerstStorageManager.initialize() called here
    → Server starts
```

**If PerstStorageManager.isAvailable() returns false:**
1. Check that `PerstConfig.getInstance().isPerstEnabled()` returns true
2. Check that `PerstStorageManager.initialize()` is called in `KissInit.init2()`
3. Check application.ini has `PerstEnabled = true`
4. Check database path exists and is writable

**Files to Check:**
- `KissInit.groovy` - Must call `PerstStorageManager.initialize()` in `init()` (NOT init2!)
- `PerstConfig.java` - Must load `PerstEnabled` from application.ini
- `application.ini` - Must have `PerstEnabled = true`

---

### Problem 8: Data Not Saved - index() Not Called

**Date:** 2026-03-15

**Symptom:** Records are created but not visible in subsequent queries.

**Cause:** PerstUser (and other domain objects) need their `index()` method called to add them to the FieldIndex. Without this, they're not findable.

**Solution:** PerstStorageManager.save() now calls index():
```java
public static void save(Object obj) {
    if (obj instanceof PerstUser) {
        ((PerstUser) obj).index();  // IMPORTANT!
        root.userIndex.put((PerstUser) obj);
    }
    // ... other types
}
```

**IMPORTANT:** Every domain class MUST have an index() method that adds it to the appropriate index.

---

### Problem 9: OID vs Custom ID

**Date:** 2026-03-15

**Symptom:** Services can't find records after create.

**Solution:** Use Perst's internal OID (Object ID) instead of custom userId fields:
```java
// In service response
row.put("id", user.getOid());  // NOT user.getUserId()

// When updating/deleting
PerstUser user = PerstUserManager.getByOid(oid);  // Lookup by OID
```

Each Perst Persistent object has a built-in OID that's automatically assigned.

---

## Architecture (Perst-Only Mode)

### New Architecture (Recommended - No Core KISS Changes)

```
┌─────────────────────────────────────────────────────────────┐
│                    MainServlet Environment                   │
│         (stores single Perst Storage instance)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    PerstStorageManager                      │
│  - initialize() → puts Storage in MainServlet environment   │
│  - save()/delete() → handles persistence                   │
│  - begin/commit/rollbackTransaction()                       │
└─────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ PerstUserManager│  │  ActorManager  │  │  PhoneManager  │
│ (Business Logic)│  │ (Business Logic)│  │ (Business Logic)│
└─────────────────┘  └─────────────────┘  └─────────────────┘
          │                   │                   │
          └───────────────────┼───────────────────┘
                              ▼
                    Your Service (Groovy/Java)
```

### Key Principles

1. **Managers at the Gate** - All data access goes through Managers
2. **PerstStorageManager handles storage** - save/delete/getRoot operations
4. **Initialize once at startup** - PerstStorageManager.initialize() in KissInit.init() (NOT init2!)
4. **Use OID for IDs** - Don't use custom ID fields, use Perst's built-in OID
5. **Call index() before save** - Domain objects must be indexed to be findable

### Configuration (application.ini)

```ini
[main]

# NO database configured - KISS won't open any SQL connection
# DatabaseType = 
# DatabaseName = 

# Perst settings
PerstEnabled = true
PerstUseCDatabase = false
PerstDatabasePath = ../../../data/oodb
PerstPagePoolSize = 536870912
```

### Initialization Code (KissInit.groovy)

**IMPORTANT:** Use `init()`, NOT `init2()`. When no database is configured, init2() is not called!

```groovy
static void init() {
    MainServlet.readIniFile "application.ini", "main"
    
    // Initialize Perst in init() - init2() is NOT called when no database configured!
    if (PerstConfig.getInstance().isPerstEnabled()) {
        PerstStorageManager.initialize()
    }
}
```

---

## Implementation Completed

### Database Configuration
- **SQLite**: Required for framework startup only (`src/main/backend/DB.sqlite`)
- **Perst**: Primary database at `data/oodb` (outside source tree)
- **SQLite Access**: BLOCKED for all application code - framework only
- **Config File**: `src/main/backend/application.ini`

### Key Configuration (application.ini)
```ini
# Database type - Perst for application, SQLite for framework only
DatabaseType = Perst

# Perst settings
PerstEnabled = true
PerstUseCDatabase = false        # Using standard Storage
PerstDatabasePath = ../../../data/oodb
PerstPagePoolSize = 536870912

# SQL access is BLOCKED - Perst only
AllowSqlAccess = false
```

### Connection Flow (Perst-Only)
```
Request → ProcessServlet.newDatabaseConnection()
         → DatabaseType = Perst?
         → YES: Create plain Connection(null), no JDBC
         → NO:  Get JDBC connection from pool
         
Service executes with Connection db
         → Services access Perst via PerstContext.getInstance()
         
Response → ProcessServlet.closeSession()
          → Perst: just set DB = null (no JDBC to close)
          → JDBC: return connection to pool
```

---

## Implementation Completed

### Files Modified

| File | Changes |
|------|---------|
| `Connection.java` | Added Perst to ConnectionType enum |
| `MainServlet.java` | Added "perst" case, getConnectionType(), skip JDBC pool |
| `PerstContext.java` | Added ThreadLocal Storage + session methods, fixed transaction handling |
| `PerstKissConnection.java` | Added setPerstSession/getThreadSession methods |
| `ProcessServlet.java` | Modified newDatabaseConnection/closeSession for Perst |
| `application.ini` | Set DatabaseType=Perst, AllowSqlAccess=false |
| `KissInit.groovy` | Added beginTransaction() + commit + verification |
| `PerstInit.groovy` | Fixed beginTransaction() call |

### New Files Created

| File | Purpose |
|------|---------|
| `src/main/precompiled/mycompany/service/Login.java` | Java-based login (replaces Groovy) |
| `src/main/precompiled/mycompany/service/PerstInit.java` | Perst initialization helper |

---

## Things Still to Accomplish

### High Priority

1. **Fix Perst Initialization**
   - Perst must be available when services call Managers
   - Verify KissInit.init2() properly calls PerstStorageManager.initialize()
   - Debug why isAvailable() returns false

2. **Verify CRUD Operations Work**
   - Users service: add, update, delete records
   - Crud service: phone book operations

3. **Verify Login Works**
   - Admin user can login
   - Session is created properly

### Completed (2026-03-16)

✅ Perst initialization in KissInit.init() (NOT init2!)
✅ Perst-only mode works without core KISS framework changes
✅ CRUD operations work (with proper transaction handling)
✅ Managers use PerstStorageManager for all storage operations

---

*Last Updated: 2026-03-15*
*Status: Perst-only mode implementation in progress*
*Branch: proposed*
