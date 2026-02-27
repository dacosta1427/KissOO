# Perst Usage Guide

## Overview

This guide covers how to use Perst within the kissweb framework for building data-driven applications.

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                   kissweb Framework                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  PerstHelper ──────► PerstContext ──────► Perst        │
│  (CRUD API)       (Management)         (Database)       │
│                                                         │
│  PerstConfig ──────► Reads application.ini              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### When to Use Each

| Component | Use When |
|-----------|----------|
| **PerstHelper** | ✅ Recommended - Standard CRUD operations |
| **PerstContext** | Advanced operations, transactions, custom queries |
| **PerstConfig** | Reading configuration (automatic) |

---

## 2. How to Add a New Domain Entity

### Step 1: Create the Entity Class

Create `src/main/backend/domain/YourEntity.java`:

```java
package domain;

import org.garret.perst.continuous.CVersion;

/**
 * YourEntity - Description of what this entity represents.
 * 
 * Extends CVersion for automatic versioning.
 */
public class YourEntity extends CVersion {
    
    // Fields - these are persisted automatically
    private String name;
    private String description;
    private int value;
    private boolean active;
    private long createdDate;
    
    /** Required: Default constructor for Perst */
    public YourEntity() {
        this.createdDate = System.currentTimeMillis();
        this.active = true;
    }
    
    /** Convenience constructor */
    public YourEntity(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public long getCreatedDate() { return createdDate; }
    
    /** Convert to JSON for API responses */
    public java.util.Map<String, Object> toJSON() {
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        json.put("name", name);
        json.put("description", description);
        json.put("value", value);
        json.put("active", active);
        json.put("createdDate", createdDate);
        return json;
    }
}
```

### Step 2: ⚠️ UPDATE PerstDBRoot

**Critical:** Add the index for your new entity.

Edit `src/main/backend/domain/PerstDBRoot.java`:

```java
package domain;

import org.garret.perst.FieldIndex;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

public class PerstDBRoot extends Persistent {
    
    // EXISTING INDEXES
    public FieldIndex<PerstUser> userIndex;
    public FieldIndex<Actor> actorIndex;
    
    // ADD YOUR NEW INDEX
    public FieldIndex<YourEntity> yourEntityIndex;
    
    public PerstDBRoot() {
        super();
    }
    
    public void setCollections(Storage db) {
        // EXISTING
        userIndex = db.createFieldIndex(PerstUser.class, "username", true);
        actorIndex = db.createFieldIndex(Actor.class, "username", true);
        
        // ADD YOUR NEW INDEX
        // Syntax: db.createFieldIndex(EntityClass.class, "fieldName", unique)
        yourEntityIndex = db.createFieldIndex(YourEntity.class, "name", true);
    }
    
    public void onLoad() {
        // Indexes are automatically restored
    }
}
```

### Step 3: Build

```bash
cd kissweb
./bld build
```

---

## 3. CRUD Operations via PerstHelper

Use `PerstHelper` for all standard database operations.

### Create (Insert)

```java
import domain.database.PerstHelper;
import domain.YourEntity;

// Create new entity
YourEntity entity = new YourEntity("My Name", "Description");
entity.setValue(100);

// Store in database
PerstHelper.storeNewObject(entity);
```

### Read (Retrieve)

```java
// By indexed field (fast)
YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "name", "My Name");

// By UUID (if you have one)
YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "uuid");

// Get all entities of a type
java.util.Collection<YourEntity> all = PerstHelper.retrieveAllObjects(YourEntity.class);
```

### Update

```java
// Modify existing entity
YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "name", "My Name");
entity.setValue(200);

// Save changes - Perst automatically versions this
PerstHelper.storeModifiedObject(entity);
```

### Delete

```java
YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "name", "My Name");
PerstHelper.removeObject(entity);
```

---

## 4. CVersion vs Persistent

### When to Use CVersion

Use `extends CVersion` when you need:
- ✅ Automatic versioning/audit trail
- ✅ Time-travel queries (see data as of any point)
- ✅ Change history tracking
- ✅ Optimistic locking

```java
// USE THIS for versioning
public class User extends CVersion {
    private String name;
    private String email;
    // Every change is automatically versioned
}
```

### When to Use Persistent

Use `extends Persistent` when:
- ✅ Simple persistence (no versioning needed)
- ✅ High-volume data that doesn't need history
- ✅ Performance-critical operations

```java
// For simple persistence without versioning
public class LogEntry extends Persistent {
    private String message;
    private long timestamp;
    // No version history needed
}
```

---

## 5. Best Practices for Indexing

### Index Selection

| Field Type | Index Type | Example |
|------------|-----------|---------|
| Unique identifier | FieldIndex (unique=true) | `userIndex` by `username` |
| Name/title | FieldIndex (unique=false) | `actorIndex` by `name` |
| Date | FieldIndex | `logIndex` by `timestamp` |
| Category | FieldIndex | `productIndex` by `category` |

### Index Rules

1. **Index frequently queried fields** - Fields used in `retrieveObject()` should be indexed
2. **Keep indexes unique when appropriate** - Prevents duplicates, faster lookups
3. **Don't over-index** - Each index uses memory

```java
// Good: Index the field you search by
userIndex = db.createFieldIndex(PerstUser.class, "email", true);

// Avoid: Indexing fields you never search
// (wastes memory)
```

---

## 6. Common Patterns

### Pattern 1: Find or Create

```java
public YourEntity findOrCreate(String name) {
    YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "name", name);
    if (entity == null) {
        entity = new YourEntity(name, "");
        PerstHelper.storeNewObject(entity);
    }
    return entity;
}
```

### Pattern 2: Update with Version Check

```java
public boolean updateIfChanged(YourEntity entity, int expectedVersion) {
    if (entity.getVersion() != expectedVersion) {
        return false; // Someone else modified it
    }
    PerstHelper.storeModifiedObject(entity);
    return true;
}
```

### Pattern 3: Batch Operations

```java
public void importBatch(java.util.List<YourEntity> entities) {
    for (YourEntity entity : entities) {
        PerstHelper.storeNewObject(entity);
    }
    // Perst batches the commits internally
}
```

### Pattern 4: Search with Version History

```java
import org.garret.perst.continuous.CVersionHistory;

// Get version history
YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "name", "My Name");
CVersionHistory<YourEntity> history = entity.getVersionHistory();

// How many versions?
int count = history.getNumberOfVersions();

// Get specific version (0 = oldest, getNumberOfVersions()-1 = newest)
YourEntity oldVersion = history.getVersion(0);

// Get current
YourEntity current = history.getCurrent();
```

---

## 7. PerstHelper vs PerstContext

### Use PerstHelper (Recommended for Most Cases)

```java
import domain.database.PerstHelper;

// Simple CRUD operations
PerstHelper.storeNewObject(entity);
PerstHelper.retrieveObject(YourEntity.class, "name", "value");
PerstHelper.storeModifiedObject(entity);
PerstHelper.removeObject(entity);
PerstHelper.retrieveAllObjects(YourEntity.class);
```

**Benefits:**
- ✅ Simpler API
- ✅ Handles null checks
- ✅ Consistent error handling
- ✅ Recommended for all standard operations

---

### Use PerstContext (Advanced Cases)

```java
import domain.kissweb.PerstContext;

PerstContext ctx = PerstContext.getInstance();

// Custom queries
IterableIterator<YourEntity> iter = ctx.getDatabase()
    .find(YourEntity.class, "name", new Key("value"));

// Complex transactions
ctx.startTransaction();
try {
    // Multiple operations
    ctx.commitTransaction();
} catch (Exception e) {
    ctx.rollbackTransaction();
}
```

**When to use PerstContext directly:**
- Custom query builders
- Complex multi-object transactions
- Direct access to CDatabase API
- Low-level Perst operations

**For 95% of use cases, use PerstHelper.**

---

## 8. Configuration

Settings in `application.ini`:

```ini
# Enable Perst
PerstEnabled = true

# Database file location
PerstDatabasePath = oodb

# Memory cache size (512MB default)
PerstPagePoolSize = 536870912
```

---

## 9. Troubleshooting

### "Perst not available"

```java
// Check if Perst is enabled
if (!PerstHelper.isAvailable()) {
    // Handle gracefully
}
```

### "Object not found"

```java
YourEntity entity = PerstHelper.retrieveObject(YourEntity.class, "name", "value");
if (entity == null) {
    // Handle not found
}
```

### Performance Issues

- Ensure fields you're searching are indexed in PerstDBRoot
- Use appropriate memory pool size in application.ini
- Consider using CVersion only when needed ( Persistent is lighter)

---

## Summary

| Task | Use |
|------|-----|
| Add new entity | Create class + update PerstDBRoot |
| CRUD operations | PerstHelper |
| Versioning | Extend CVersion |
| Indexing | Add to PerstDBRoot.setCollections() |
| Advanced queries | PerstContext (rarely needed) |
