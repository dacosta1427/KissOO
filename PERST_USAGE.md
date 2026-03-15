# Perst Usage Guide

## Overview

This guide covers how to use Perst within the KissOO framework for building data-driven applications.

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                   KissOO Framework                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  mycompany.database.PerstHelper ───┐                    │
│  (CRUD API)                        │                    │
│                                    ▼                    │
│                                oodb.PerstContext ───► Perst DB
│  mycompany.domain.Actor,          (Management)         │
│  Agreement, Group, PerstUser ──────────────────────────┘
│                                                         │
│  oodb.PerstConfig ──────► Reads application.ini         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Directory Structure

```
src/main/
├── precompiled/
│   ├── mycompany/domain/     # Domain entities (Actor, Agreement, Group, PerstUser)
│   ├── mycompany/database/   # Manager classes (PerstHelper, ActorManager)
│   └── oodb/                 # Perst configuration (PerstConfig, PerstContext)
└── backend/
    └── services/             # REST services (ActorService, etc.)
```

---

## 2. Quick Start

### Enable Perst

Edit `backend/application.ini`:

```ini
PerstEnabled = true
PerstDatabasePath = oodb
```

### Basic CRUD Example

```java
// In your service
import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.database.PerstHelper;

// Create
Agreement agreement = new Agreement("USER");
agreement.grant(Actor.class, "read");
agreement.grant(Actor.class, "create");

Actor actor = new Actor("John", "USER", agreement);
PerstHelper.storeNewObject(actor);

// Retrieve
Actor found = PerstHelper.retrieveObject(Actor.class, "name", "John");

// Update
found.setName("John Doe");
PerstHelper.storeModifiedObject(found);

// Delete
PerstHelper.removeObject(found);
```

---

## 3. Domain Entity Structure

All domain entities are in `precompiled/mycompany/domain/`:

```java
package mycompany.domain;

import org.garret.perst.continuous.CVersion;

public class YourEntity extends CVersion {
    private String name;
    
    public YourEntity() { }
    
    public YourEntity(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

### Entity Rules
- ✅ Extend `CVersion` for versioning/audit trail
- ✅ Extend `Persistent` for simple storage (no versioning)
- ✅ Default constructor required
- ✅ Fields are automatically persisted

---

## 4. CRUD Operations via PerstHelper

| Operation | Method |
|-----------|--------|
| Create | `PerstHelper.storeNewObject(entity)` |
| Read | `PerstHelper.retrieveObject(Class, field, value)` |
| Update | `PerstHelper.storeModifiedObject(entity)` |
| Delete | `PerstHelper.removeObject(entity)` |
| Get All | `PerstHelper.retrieveAllObjects(Class)` |

### Example

```java
// Create
User user = new User("alice", "alice@example.com");
PerstHelper.storeNewObject(user);

// Read by indexed field
User found = PerstHelper.retrieveObject(User.class, "username", "alice");

// Update
found.setEmail("newemail@example.com");
PerstHelper.storeModifiedObject(found);

// Delete
PerstHelper.removeObject(found);
```

---

## 5. Configuration

Settings in `backend/application.ini`:

```ini
# Enable/disable Perst
PerstEnabled = true

# Database file location (relative to working directory)
PerstDatabasePath = oodb

# Memory cache size in bytes (512MB default)
PerstPagePoolSize = 536870912

# Use CDatabase for versioning (true/false)
PerstUseCDatabase = true
```

---

## 6. Transaction Handling (CDatabase Only)

When using CDatabase versioning, wrap writes in transactions:

```java
import oodb.PerstContext;

PerstContext context = PerstContext.getInstance();
context.beginTransaction();

try {
    User user = new User("bob", "bob@example.com");
    PerstHelper.storeNewObject(user);
    context.commitTransaction();
} catch (Exception e) {
    context.rollbackTransaction();
}
```

---

## 7. Common Patterns

### Find or Create

```java
User findOrCreate(String username) {
    User user = PerstHelper.retrieveObject(User.class, "username", username);
    if (user == null) {
        user = new User(username, "");
        PerstHelper.storeNewObject(user);
    }
    return user;
}
```

### Version History

```java
// Get all versions of an entity
List<YourEntity> history = PerstHelper.getVersionHistory(
    YourEntity.class, "name", "MyEntity"
);

// Get current version
YourEntity current = PerstHelper.getCurrentVersion(
    YourEntity.class, "name", "MyEntity"
);
```

---

## 8. Troubleshooting

### "Perst not available"
```java
if (!PerstHelper.isAvailable()) {
    // Perst is disabled or not initialized
}
```

### "Object not found"
```java
User user = PerstHelper.retrieveObject(User.class, "username", "unknown");
if (user == null) {
    // Handle not found
}
```

### Performance Tips
- Index fields you search by (add to FieldIndex in CDatabaseRoot)
- Use `Persistent` instead of `CVersion` when versioning isn't needed
- Set appropriate `PerstPagePoolSize` in application.ini

---

## 9. See Also

- **Testing**: See [docs/TestingGuide.md](docs/TestingGuide.md)
- **Architecture**: See [docs/PerstIntegration.md](docs/PerstIntegration.md)
- **Authorization**: See [MANAGER_AT_THE_GATE.md](MANAGER_AT_THE_GATE.md)
