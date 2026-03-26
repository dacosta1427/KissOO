# ChangeNote: PerstConnection Integration

## Date
2026-03-26

## Summary
Integrated Perst OODBMS operations into KISS via a new `PerstConnection` class that extends `org.kissweb.database.Connection`. Registered as `NonSqlConnection` in MainServlet environment, enabling services to use Perst operations through the standard `db` parameter.

## Problem Statement

Previously, services received a `Connection db` parameter that was always `null` when using Perst-only mode (no SQL database configured). This led to:

1. **Wasted parameter**: The `db` parameter served no purpose in Perst-only mode
2. **Inconsistent patterns**: Services had to import and call static `PerstStorageManager` methods
3. **Two abstraction layers**: Managers called PerstStorageManager which called UnifiedDBManager

## Solution

Created `PerstConnection` class that:
1. Extends `org.kissweb.database.Connection` (using SQLite in-memory as dummy SQL connection)
2. Adds Perst-specific operations (getAll, find, getByOid, transaction management)
3. Registered as `NonSqlConnection` in MainServlet environment - KISS framework automatically passes it to services when no SQL database is configured

## Files Changed

### New Files
- `src/main/precompiled/oodb/PerstConnection.java` - Connection subclass with Perst methods

### Modified Files
- `src/main/backend/KissInit.groovy` - Register PerstConnection as NonSqlConnection after Perst initialization

## Architecture

### Before
```
Service.method(injson, outjson, db, servlet)
    │
    ├── db = null (unused)
    │
    └── HouseManager.getAll() 
           └── PerstStorageManager.getAll() 
```

### After
```
Service.method(injson, outjson, db, servlet)
    │
    ├── db = PerstConnection (registered as NonSqlConnection)
    │
    └── if (db instanceof PerstConnection) {
            Collection<House> houses = ((PerstConnection) db).getAll(House.class);
        }
```

## PerstConnection API

### Check Availability
```java
if (db instanceof PerstConnection) {
    PerstConnection perst = (PerstConnection) db;
    if (perst.isPerstAvailable()) { ... }
}
```

### Transaction Management
```java
perst.perstBeginTransaction()
perst.perstCommitTransaction()
perst.perstRollbackTransaction()
perst.perstIsInTransaction()
```

### Transaction Container (batch operations)
```java
TransactionContainer tc = perst.perstCreateContainer();
tc.addInsert(obj);  // or addUpdate, addDelete
perst.perstStore(tc);
```

### Retrieve Operations
```java
List<T> list = perst.getAll(Class<T> clazz);
T obj = perst.find(Class<T> clazz, String field, String value);
T obj = perst.find(Class<T> clazz, String field, int value);
T obj = perst.find(Class<T> clazz, String field, long value);
T obj = perst.getByOid(Class<T> clazz, long oid);
T obj = perst.getByUuid(Class<T> clazz, String uuid);
```

## Testing

1. PerstConnection is registered at startup (check logs for "PerstConnection registered as NonSqlConnection")
2. Services receive PerstConnection as `db` parameter when Perst is enabled
3. Test with: `db instanceof PerstConnection` check

## Notes

- Uses SQLite in-memory connection as dummy SQL connection (satisfies Connection superclass)
- Registered both as "NonSqlConnection" (for KISS framework) and "PerstConnection" (for direct access)
- Manager classes still work - this is an additive change
- Services can gradually migrate from static calls to using `db`
