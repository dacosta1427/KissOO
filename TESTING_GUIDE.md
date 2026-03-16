# KissOO Testing Guide

## Architecture Overview

### Package Structure

```
src/main/precompiled/
├── mycompany/
│   ├── domain/           # Domain entities (Perst persistent objects)
│   │   ├── Actor.java
│   │   ├── PerstUser.java
│   │   └── ...
│   │
│   └── database/        # Managers (business logic)
│       ├── ActorManager.java
│       ├── PerstUserManager.java
│       ├── BaseManager.java
│       └── ...
│
└── oodb/                # Perst infrastructure
    ├── PerstConfig.java
    ├── PerstContext.java
    └── PerstStorageManager.java
```

### Key Principles

1. **Managers in precompiled** - All Manager classes go in `src/main/precompiled/mycompany/database/`
2. **Services in backend** - REST services go in `src/main/backend/services/`
3. **Static methods** - Managers use static methods, not singletons
4. **Storage delegation** - Managers delegate storage to `PerstStorageManager`

---

## Writing Manager Tests

### Test Location

Tests go in: `src/test/core/mycompany/database/`

### Example: ActorManagerTest

```java
package mycompany.database;

import mycompany.domain.Actor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActorManager.
 * Tests business logic methods only - storage is delegated to PerstStorageManager.
 */
public class ActorManagerTest {

    @Test
    public void testValidateRejectsNullActor() {
        assertFalse(ActorManager.validate(null));
    }

    @Test
    public void testValidateAcceptsValidActor() {
        Actor actor = new Actor("testActor", "RETAIL");
        assertTrue(ActorManager.validate(actor));
    }
    
    // ... more tests
}
```

### What to Test in Managers

✅ **DO test:**
- Validation logic (`validate()`)
- Business rules
- Authorization checks
- Error handling for invalid input

❌ **DON'T test:**
- Perst storage operations (tested in PerstStorageManager tests)
- Direct database access
- Internal storage delegation

---

## Using Perst in Managers

### Storage Layer: PerstStorageManager

All storage operations go through `PerstStorageManager`:

```java
import oodb.PerstStorageManager;

// Get all entities
Collection<Object> results = PerstStorageManager.getAll(MyEntity.class);

// Get by UUID
Object entity = PerstStorageManager.getByUuid(uuid);

// Save
PerstStorageManager.save(entity);

// Delete
PerstStorageManager.delete(entity);
```

### Manager Pattern

```java
package mycompany.database;

import mycompany.domain.MyEntity;
import java.util.*;

public class MyEntityManager extends BaseManager<MyEntity> {
    
    // ========== Authorization-Aware Methods ==========
    // These check permissions before performing operations
    
    public static Collection<MyEntity> getAll(MyEntity actor) {
        if (!checkPermission(actor, ACTION_READ, MyEntity.class)) {
            return null;
        }
        return getAll();
    }
    
    // ========== CRUD Methods (delegate to PSM) ==========
    
    public static Collection<MyEntity> getAll() {
        Collection<MyEntity> result = new ArrayList<>();
        for (Object obj : PerstStorageManager.getAll(MyEntity.class)) {
            result.add((MyEntity) obj);
        }
        return result;
    }
    
    public static MyEntity getByUuid(String uuid) {
        return (MyEntity) PerstStorageManager.getByUuid(uuid);
    }
    
    public static boolean create(MyEntity entity) {
        if (!validate(entity)) return false;
        PerstStorageManager.save(entity);
        return true;
    }
    
    // ========== Business Logic ==========
    
    public static boolean validate(MyEntity entity) {
        if (entity == null) return false;
        // validation logic
        return true;
    }
}
```

### Authorization Pattern

Managers use the "Manager at the Gate" pattern:

```java
// In any Manager method:
public static Result doSomething(Actor actor, Object... params) {
    // 1. Check permission using Actor's Agreement
    if (!checkPermission(actor, ACTION_READ, MyEntity.class)) {
        return null;  // or throw exception
    }
    
    // 2. Perform operation
    return performOperation(params);
}
```

---

## PerstStorageManager API

### Initialization

```java
// Called once at startup (in KissInit.groovy)
PerstStorageManager.initialize();
```

### Query Methods

```java
// Get all entities of a class
Collection<Object> getAll(Class<?> clazz)

// Get entity by UUID
Object getByUuid(String uuid)
```

### CRUD Operations

```java
// Save (insert or update)
void save(Object obj)

// Delete
void delete(Object obj)
```

### Transactions

```java
void beginTransaction();
void commitTransaction();
void rollbackTransaction();
```

---

## Configuration

### application.ini

```ini
[main]
RequireAuthentication = true

# Perst OODBMS Settings
PerstEnabled = true
PerstUseCDatabase = false
PerstDatabasePath = ../../../data/oodb
PerstPagePoolSize = 536870912
```

---

## Common Patterns

### Check Perst Availability

```java
if (!PerstStorageManager.isAvailable()) {
    // Handle unavailable state
    return Collections.emptyList();
}
```

### Handle Null DB Parameter (for Login.groovy)

The framework now handles null DB parameters automatically via GroovyClass method resolution. Your Login.groovy can receive null for the Connection parameter:

```groovy
public static UserData login(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet) {
    // db may be null when Perst-only mode is active
    // Use PerstUserManager for authentication instead
}
```

---

## Test Naming Conventions

- `test[MethodName]` - Test a specific method
- `test[MethodName]With[Condition]` - Test with specific condition
- `test[MethodName]Throws[Exception]` - Test exception handling
- `test[MethodName]Returns[Expected]` - Test return values

## Running Tests

```bash
./bld -v test
```

> **Note:** When running tests outside Tomcat (standalone), some tests may fail due to missing Perst and Jakarta Servlet jars in the test classpath. These tests work correctly when running in the full application context (Tomcat). Tests affected:
> - Tests that reference Perst domain classes (Actor, PerstUser, etc.)
> - Tests that use PerstConfig (requires HttpServlet)
> 
> The failures are infrastructure-related, not logic errors.

---

*Last updated: 2026-03-16*
