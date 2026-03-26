# PerstConnection Usage Guide

## Overview

`PerstConnection` provides a unified interface for Perst OODBMS operations in KissOO services. It is registered as `NonSqlConnection` in the MainServlet environment when Perst is enabled (and no SQL database is configured).

## Getting Started

### Getting PerstConnection in Services

```groovy
import org.kissweb.restServer.MainServlet
import oodb.PerstConnection

class MyService {
    private PerstConnection getPerst() {
        return (PerstConnection) MainServlet.getEnvironment("PerstConnection")
    }
    
    void myMethod(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        PerstConnection perst = getPerst()
        if (perst == null || !perst.isAvailable()) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", "Perst not available")
            return
        }
        // Use perst for database operations
    }
}
```

## API Reference

### Checking Availability

```groovy
boolean isAvailable()
```
Returns `true` if Perst is initialized and ready for use.

---

### Retrieve Operations

#### Get All Objects
```groovy
List<T> getAll(Class<T> clazz)
```
Returns all objects of the specified class.

**Example:**
```groovy
Collection<House> houses = perst.getAll(House)
Collection<PerstUser> users = perst.getAll(PerstUser)
```

#### Find by Field (String)
```groovy
T find(Class<T> clazz, String field, String value)
```
Finds a single object by field value.

**Example:**
```groovy
PerstUser user = perst.find(PerstUser, "username", "admin")
House house = perst.find(House, "name", "My House")
```

#### Find by Field (int)
```groovy
T find(Class<T> clazz, String field, int value)
```

**Example:**
```groovy
Booking booking = perst.find(Booking, "houseId", 123)
```

#### Find by Field (long)
```groovy
T find(Class<T> clazz, String field, long value)
```

**Example:**
```groovy
PerstUser user = perst.find(PerstUser, "ownerId", 456L)
```

#### Get by OID
```groovy
T getByOid(Class<T> clazz, long oid)
```
Retrieves an object by its Perst OID.

**Example:**
```groovy
House house = perst.getByOid(House, 4205L)
```

#### Get by UUID
```groovy
T getByUuid(Class<T> clazz, String uuid)
```
Retrieves an object by its UUID.

**Example:**
```groovy
PerstUser user = perst.getByUuid(PerstUser, "550e8400-e29b-41d4-a716-446655440000")
```

---

### Transaction Container (Batch Operations)

Transaction containers allow atomic batch operations.

#### Create Container
```groovy
TransactionContainer perstCreateContainer()
```
Creates a new transaction container for batch operations.

#### Store Container
```groovy
boolean perstStore(TransactionContainer tc)
```
Stores all operations in the container atomically. Returns `true` if successful.

**Example - Insert:**
```groovy
House house = new House("My House", "123 Main St", "Description", 0, true, "16:00", "10:00")
def tc = perst.perstCreateContainer()
tc.addInsert(house)
if (perst.perstStore(tc)) {
    // Success - house.getOid() now contains the OID
}
```

**Example - Update:**
```groovy
House house = perst.getByOid(House, oid)
house.setName("Updated Name")
def tc = perst.perstCreateContainer()
tc.addUpdate(house)
perst.perstStore(tc)
```

**Example - Delete:**
```groovy
House house = perst.getByOid(House, oid)
def tc = perst.perstCreateContainer()
tc.addDelete(house)
perst.perstStore(tc)
```

**Example - Multiple Operations:**
```groovy
def tc = perst.perstCreateContainer()
tc.addInsert(new House(...))
tc.addInsert(new Booking(...))
tc.addUpdate(existingHouse)
tc.addDelete(oldSchedule)
perst.perstStore(tc)  // All succeed or all fail
```

---

### Transaction Management

#### Begin Transaction
```groovy
void perstBeginTransaction()
```

#### Commit Transaction
```groovy
void perstCommitTransaction()
```

#### Rollback Transaction
```groovy
void perstRollbackTransaction()
```

#### Check Transaction Status
```groovy
boolean perstIsInTransaction()
```

**Example:**
```groovy
perst.perstBeginTransaction()
try {
    // Do multiple operations
    perst.perstCommitTransaction()
} catch (Exception e) {
    perst.perstRollbackTransaction()
    throw e
}
```

---

### History/Lucene

#### Flush History Buffer
```groovy
void perstFlushHistory()
```
Flushes the Lucene full-text index buffer.

---

## Complete Service Example

```groovy
package services

import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.MainServlet
import oodb.PerstConnection

class MyService {
    
    private PerstConnection getPerst() {
        return (PerstConnection) MainServlet.getEnvironment("PerstConnection")
    }
    
    void getItems(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            Collection<MyItem> items = perst.getAll(MyItem)
            // ... build response
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createItem(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            JSONObject data = injson.getJSONObject("data")
            
            MyItem item = new MyItem(data.getString("name"))
            def tc = perst.perstCreateContainer()
            tc.addInsert(item)
            
            if (!perst.perstStore(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create")
                return
            }
            
            outjson.put("data", [id: item.getOid(), name: item.getName()])
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateItem(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            
            MyItem item = perst.getByOid(MyItem, oid)
            if (item == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Not found")
                return
            }
            
            if (data.has("name")) item.setName(data.getString("name"))
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(item)
            perst.perstStore(tc)
            
            outjson.put("data", [id: item.getOid(), name: item.getName()])
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteItem(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            
            MyItem item = perst.getByOid(MyItem, oid)
            if (item == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Not found")
                return
            }
            
            def tc = perst.perstCreateContainer()
            tc.addDelete(item)
            perst.perstStore(tc)
            
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
}
```

## Migration Guide

When migrating from Manager classes to PerstConnection:

### Before (using Managers)
```groovy
Collection<House> houses = HouseManager.getAll()
House house = HouseManager.getByOid(oid)
HouseManager.create(name, address, ...)
HouseManager.update(house)
HouseManager.delete(house)
```

### After (using PerstConnection)
```groovy
PerstConnection perst = getPerst()
Collection<House> houses = perst.getAll(House)
House house = perst.getByOid(House, oid)

// Create
House newHouse = new House(name, address, ...)
def tc = perst.perstCreateContainer()
tc.addInsert(newHouse)
perst.perstStore(tc)

// Update
house.setName(newName)
def tc = perst.perstCreateContainer()
tc.addUpdate(house)
perst.perstStore(tc)

// Delete
def tc = perst.perstCreateContainer()
tc.addDelete(house)
perst.perstStore(tc)
```

## Notes

- PerstConnection is a singleton registered at startup
- The same instance is shared across all requests (Perst is thread-safe)
- Always check `isAvailable()` before using Perst methods
- Use transaction containers for atomic operations
- The `db` parameter passed to services is null in Perst-only mode - use `getPerst()` instead
