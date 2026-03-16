# Using Perst in KissOO - Developer Guide

## Overview

KissOO extends the KISS framework with Perst OODBMS support. This guide explains how to use Perst for data storage in your application.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Your Service (Groovy/Java)                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Manager at the Gate                            │
│  - PerstUserManager (for users)                                 │
│  - ActorManager (for actors)                                    │
│  - [YourCustomManager]                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   PerstStorageManager                            │
│  - Transaction management                                      │
│  - Root access                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              MainServlet Environment (perstStorage)             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Perst Database                                │
│  - userIndex (PerstUser)                                       │
│  - actorIndex (Actor)                                           │
│  - agreementIndex (Agreement)                                   │
│  - groupIndex (Group)                                           │
└─────────────────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Configuration

In `application.ini`:

```ini
[main]

# Perst settings
PerstEnabled = true
PerstUseCDatabase = false
PerstDatabasePath = ../../../data/oodb
PerstPagePoolSize = 536870912

# No SQL database configured - Perst-only mode
# Leave DatabaseType and DatabaseName commented out
```

### 2. Initialization

Perst is initialized in `KissInit.init()` (NOT init2 - init2 is not called when no database is configured):

```groovy
static void init() {
    MainServlet.readIniFile "application.ini", "main"
    
    // Initialize Perst HERE - before init2() which might not be called
    if (PerstConfig.getInstance().isPerstEnabled()) {
        PerstStorageManager.initialize()
    }
    
    // Other initialization...
}
```

**Important:** Perst must be initialized in `init()`, not `init2()`. When no SQL database is configured (DatabaseType commented out), the KISS framework does NOT call `init2()`. So all initialization including Perst must happen in `init()`.

### 3. Creating a Domain Object

First, extend your domain class from Perst's `Persistent`:

```java
package mycompany.domain;

import org.garret.perst.Persistent;

public class MyEntity extends Persistent {
    private String name;
    private String type;
    private int value;
    
    // Required no-arg constructor
    public MyEntity() {}
    
    public MyEntity(String name, String type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    // ... etc
}
```

### 4. Adding Index to CDatabaseRoot

Update `CDatabaseRoot.java` to add your index:

```java
public class CDatabaseRoot extends Root {
    public FieldIndex<PerstUser> userIndex;
    public FieldIndex<Actor> actorIndex;
    public FieldIndex<Agreement> agreementIndex;
    public FieldIndex<Group> groupIndex;
    
    // Add your index
    public FieldIndex<MyEntity> myEntityIndex;
    
    public void setCollections(Storage storage) {
        userIndex = storage.createFieldIndex(PerstUser.class, "username", true);
        actorIndex = storage.createFieldIndex(Actor.class, "name", true);
        agreementIndex = storage.createFieldIndex(Agreement.class, "name", true);
        groupIndex = storage.createFieldIndex(Group.class, "name", true);
        
        // Add your index
        myEntityIndex = storage.createFieldIndex(MyEntity.class, "name", true);
    }
}
```

### 5. Creating a Manager

Create a Manager class following the "Manager at the Gate" pattern:

```java
package mycompany.database;

import mycompany.domain.MyEntity;
import mycompany.domain.CDatabaseRoot;
import oodb.PerstStorageManager;
import java.util.*;

/**
 * MyEntityManager - Manages MyEntity domain objects.
 * 
 * ALL access to MyEntity MUST go through this class.
 */
public class MyEntityManager {
    
    private MyEntityManager() {}  // Prevent instantiation
    
    // ========== CRUD Operations ==========
    
    public static Collection<MyEntity> getAll() {
        if (!PerstStorageManager.isAvailable()) {
            return new ArrayList<>();
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<MyEntity> result = new ArrayList<>();
        for (MyEntity e : root.myEntityIndex) {
            result.add(e);
        }
        return result;
    }
    
    public static MyEntity getByName(String name) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        return root.myEntityIndex.get(name);
    }
    
    public static MyEntity create(Object... params) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        
        String name = (String) params[0];
        if (getByName(name) != null) {
            throw new IllegalArgumentException("Entity already exists: " + name);
        }
        
        MyEntity entity = new MyEntity(
            name,
            (String) params[1],
            (Integer) params[2]
        );
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.myEntityIndex.put(entity);
            PerstStorageManager.commitTransaction();
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            throw new RuntimeException("Failed to create entity: " + e.getMessage(), e);
        }
        
        return entity;
    }
    
    public static boolean update(MyEntity entity) {
        if (!PerstStorageManager.isAvailable() || entity == null) {
            return false;
        }
        
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.myEntityIndex.put(entity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean delete(MyEntity entity) {
        if (!PerstStorageManager.isAvailable() || entity == null) {
            return false;
        }
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.myEntityIndex.remove(entity);
            PerstStorageManager.commitTransaction();
            return true;
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            return false;
        }
    }
}
```

### 6. Using in Services

```groovy
class MyService {
    
    static void doSomething(JSONObject in, JSONObject out, Connection db) {
        // Create
        def entity = MyEntityManager.create("test", "type1", 100)
        
        // Read
        def found = MyEntityManager.getByName("test")
        
        // Update
        found.setValue(200)
        MyEntityManager.update(found)
        
        // Delete
        MyEntityManager.delete(found)
    }
}
```

## Transaction Management

### Automatic Transactions

Managers handle transactions automatically for create/update/delete operations:

```java
// Inside Manager
public static MyEntity create(Object... params) {
    PerstStorageManager.beginTransaction();
    try {
        // Perform operations
        root.myEntityIndex.put(entity);
        
        PerstStorageManager.commitTransaction();
    } catch (Exception e) {
        PerstStorageManager.rollbackTransaction();
        throw e;
    }
}
```

### Manual Transactions

For complex operations spanning multiple entities:

```groovy
class ComplexService {
    
    static void transfer(JSONObject in, JSONObject out, Connection db) {
        // Start manual transaction
        PerstStorageManager.beginTransaction()
        
        try {
            def from = ActorManager.getByName(in.getString("from"))
            def to = ActorManager.getByName(in.getString("to"))
            int amount = in.getInt("amount")
            
            from.setBalance(from.getBalance() - amount)
            to.setBalance(to.getBalance() + amount)
            
            ActorManager.update(from)
            ActorManager.update(to)
            
            // Commit
            PerstStorageManager.commitTransaction()
            
            out.put("success", true)
        } catch (Exception e) {
            // Rollback on error
            PerstStorageManager.rollbackTransaction()
            out.put("error", e.message)
        }
    }
}
```

## Existing Managers

### PerstUserManager

For user authentication and management:

```java
// Authenticate
PerstUser user = PerstUserManager.authenticate(username, password);

// Create user
PerstUser newUser = PerstUserManager.create("john", "password123", 1);

// Update
user.setEmail("john@example.com");
PerstUserManager.update(user);

// Delete
PerstUserManager.delete(user);
```

### ActorManager

For actor/entity management:

```java
// Create actor
Actor actor = ActorManager.create("John Doe", "person", userId);

// Find by name
Actor found = ActorManager.getByName("John Doe");

// Find by UUID
Actor byUuid = ActorManager.getByUuid(actor.getUuid());

// Update
actor.setType("admin");
ActorManager.update(actor);

// Delete
ActorManager.delete(actor);
```

## Best Practices

1. **Always use Managers** - Never access Perst directly from services
2. **Let Managers handle transactions** - Don't wrap Manager calls in manual transactions
3. **Use transactions for multi-entity operations** - When updating multiple related entities
4. **Validate before saving** - Managers should validate entities before persisting
5. **Check availability** - Use `PerstStorageManager.isAvailable()` before operations

## Troubleshooting

### Perst not available

If you get "Perst not available" errors:
- Check `PerstEnabled = true` in `application.ini`
- Verify database path exists and is writable
- Check server logs for initialization errors

### Transaction conflicts

If you see transaction conflicts:
- Ensure you're using `beginTransaction()`/`commitTransaction()` pairs
- Avoid long-running transactions
- Consider using `SHARED_TRANSACTION` for read-only operations

### Index not found

If you get index errors:
- Verify your index is defined in `CDatabaseRoot.setCollections()`
- Rebuild the database if you changed the schema
