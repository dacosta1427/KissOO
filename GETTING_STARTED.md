# Getting Started with KissOO (Perst Integration)

> This fork adds Perst OODBMS integration to the Kiss framework with the "Manager at the Gate" pattern for authorization.

## What is KissOO?

KissOO is a fork of [Kiss](https://github.com/blakemcbride/Kiss) that adds:
- **Perst OODBMS** - Object-oriented database for high-performance object storage
- **Manager at the Gate** - Authorization pattern for all database operations
- **Dual Database Support** - Use Perst, PostgreSQL, or both

## Quick Start

### Prerequisites

- Java 17+ (tested with Java 25)
- Git

### Setup

```bash
# Clone this fork
git clone https://github.com/dacosta1427/KissOO.git
cd KissOO

# Build and run
./bld develop   # Linux/macOS
bld develop     # Windows
```

### Enable Perst

Edit `backend/application.ini`:

```ini
PerstEnabled = true
PerstDatabasePath = oodb
```

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Request                            │
│            { _class, _method, _uuid, ... }                 │
└─────────────────────────┬───────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                   Service Layer                            │
│         (ActorService, UserService, etc.)                  │
│                                                             │
│   MUST obtain Actor from UserData and pass to Managers     │
└─────────────────────────┬───────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                   Manager Layer                            │
│   ┌─────────────┐  ┌──────────────┐  ┌─────────────┐      │
│   │ActorManager │  │UserManager   │  │ XXXManager  │      │
│   └──────┬──────┘  └──────┬───────┘  └──────┬──────┘      │
│          │                 │                  │              │
│   ┌──────┴─────────────────┴──────────────────┴──────┐     │
│   │     ✓ Authorization Check                        │     │
│   │     ✓ Validation                                  │     │
│   │     ✓ Business Logic                              │     │
│   └──────────────────┬─────────────────────────────────┘     │
└──────────────────────┼──────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                  PerstHelper (Data Access)                 │
│         Thread-safe, per-session isolation                 │
└──────────────────────┬──────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                  Perst OODBMS                               │
│            (CVersion Objects)                               │
└─────────────────────────────────────────────────────────────┘
```

## ⚠️ CRITICAL: Authorization is MANDATORY

**Every service call MUST go through authorization. This is enforced at two levels:**

1. **Endpoint level** - Via `EndpointMethod` in Agreement
2. **Manager level** - Via `checkPermission()` in BaseManager

```java
// ✅ CORRECT: Authorization is automatic
UserData ud = servlet.getUserData();
Actor caller = ActorManager.getByUserId((int) ud.getUserId());
Actor actor = ActorManager.getByUuid(caller, uuid);  // Agreement checked automatically
```

## The Manager at the Gate Pattern

**CRITICAL: All database operations MUST go through Managers.**

### Why?

1. **Authorization** - Every operation checks if the Actor has permission
2. **Validation** - Business rules enforced consistently
3. **Audit** - All operations can be logged
4. **Testability** - Easy to mock and test

### How It Works

1. Service receives HTTP request
2. Service gets `UserData` from session via `servlet.getUserData()`
3. Service gets `Actor` from `UserData.getUserId()`
4. Service calls Manager methods, passing the `Actor`
5. Manager checks `checkPermission(actor, action, resource)`
6. If authorized → execute; if not → return error

### Example Service

```java
public class MyService {
    
    public void getData(JSONObject injson, JSONObject outjson, 
                        Connection db, ProcessServlet servlet) {
        
        // 1. Get current user
        UserData ud = servlet.getUserData();
        if (ud == null) {
            outjson.put("error", "Not authenticated");
            return;
        }
        
        // 2. Get Actor for authorization
        Actor actor = ActorManager.getByUserId((int) ud.getUserId());
        if (actor == null) {
            outjson.put("error", "No actor linked to user");
            return;
        }
        
        // 3. Use Manager with authorization
        Collection<MyEntity> data = MyManager.getAll(actor);
        if (data == null) {
            outjson.put("error", "Not authorized");
            return;
        }
        
        outjson.put("data", data);
    }
}
```

### Authorization Actions

| Action | Description |
|--------|-------------|
| `ACTION_CREATE` | Creating new entities |
| `ACTION_READ` | Reading/listing entities |
| `ACTION_UPDATE` | Modifying entities |
| `ACTION_DELETE` | Removing entities |
| `ACTION_EXECUTE` | Executing endpoints |

## Core Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `PerstConfig` | `oodb` | Reads Perst settings from application.ini |
| `PerstContext` | `oodb` | Perst database operations (thread-local) |
| `Actor` | `mycompany.domain` | Domain entity linked to users |
| `Agreement` | `mycompany.domain` | Authorization permissions |
| `Group` | `mycompany.domain` | Group-based permissions |
| `PerstUser` | `mycompany.domain` | User entity for authentication |
| `PerstHelper` | `mycompany.database` | Static helper for Perst operations |
| `BaseManager` | `mycompany.database` | Abstract base with authorization |
| `ActorManager` | `mycompany.database` | Manages Actor entities |
| `PerstUserManager` | `mycompany.database` | Manages users + authentication |

## Authorization Flow

```
Request → EndpointMethod.execute()
             ↓
        Agreement.grants(endpoint, resource, action)
             ↓
        Check in order:
             1. CRUD permissions ("Actor:create")
             2. EndpointMethod permissions (type-safe)
             3. Group permissions (via groups)
             ↓
        Everything denied by default!
             ↓
        Execute or Deny
```

## EndpointMethod Pattern (Recommended)

Each endpoint is an `EndpointMethod` that checks its own authorization:

```java
public class ActorService {
    
    public static final EndpointMethod GET_ACTOR = 
        new EndpointMethod("services.ActorService.getActor", Actor.class) {
            @Override
            protected boolean doExecute(JSONObject in, JSONObject out, 
                                       Connection db, ProcessServlet servlet) {
                // 1. Authenticate
                Actor caller = getAuthenticatedActor(servlet);
                if (caller == null) {
                    out.put("error", "Not authenticated");
                    return false;
                }
                
                // 2. Authorize via Agreement (automatic)
                if (!caller.canExecute(GET_ACTOR)) {
                    out.put("error", "Not authorized");
                    return false;
                }
                
                // 3. Execute
                String uuid = in.getString("uuid");
                Actor actor = ActorManager.getByUuid(caller, uuid);
                out.put("actor", actor.toJSON());
                return true;
            }
        };
    
    // Legacy method - delegates to EndpointMethod
    public void getActor(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
        GET_ACTOR.execute(in, out, db, servlet);
    }
}
```

## Creating a New Domain Manager

### Step 1: Create the Manager

```java
package domain.database;

import domain.Actor;
import domain.MyEntity;
import java.util.Collection;
import java.util.ArrayList;

public class MyManager extends BaseManager<MyEntity> {
    
    private static MyManager instance;
    
    private MyManager() {}
    
    public static synchronized MyManager getInstance() {
        if (instance == null) instance = new MyManager();
        return instance;
    }
    
    @Override
    protected String getResourceName() {
        return "MyEntity";
    }
    
    @Override
    public Collection<MyEntity> getAll() {
        if (!isPerstAvailable()) return new ArrayList<>();
        return PerstHelper.retrieveAllObjects(MyEntity.class);
    }
    
    @Override
    public MyEntity getByKey(String key) {
        if (!isPerstAvailable()) return null;
        return PerstHelper.retrieveObject(MyEntity.class, "name", key);
    }
    
    @Override
    public MyEntity create(Object... params) {
        // Validation
        if (params.length < 1) {
            throw new IllegalArgumentException("name required");
        }
        
        MyEntity entity = new MyEntity((String) params[0]);
        if (!validate(entity)) {
            throw new IllegalArgumentException("Validation failed");
        }
        
        PerstHelper.storeNewObject(entity);
        return entity;
    }
    
    @Override
    public boolean update(MyEntity entity) {
        if (!isPerstAvailable() || entity == null) return false;
        if (!validate(entity)) return false;
        PerstHelper.storeModifiedObject(entity);
        return true;
    }
    
    @Override
    public boolean delete(MyEntity entity) {
        if (!isPerstAvailable() || entity == null) return false;
        PerstHelper.removeObject(entity);
        return true;
    }
    
    @Override
    protected boolean validate(MyEntity entity) {
        return entity != null && entity.getName() != null;
    }
}
```

### Step 2: Use in Service

```java
public void doSomething(JSONObject injson, JSONObject outjson,
                        Connection db, ProcessServlet servlet) {
    
    UserData ud = servlet.getUserData();
    Actor actor = ActorManager.getByUserId((int) ud.getUserId());
    
    // Use authorization-enabled method
    MyEntity entity = MyManager.create(actor, "name");
    if (entity == null) {
        outjson.put("error", "Not authorized");
        return;
    }
    
    outjson.put("entity", entity.toJSON());
}
```

## Thread Safety

PerstHelper uses `PerstContext` which maintains thread-local Storage instances. Each HTTP request runs in its own thread with its own Perst session, ensuring complete isolation between concurrent requests.

## Benefits

| Feature | Description |
|---------|-------------|
| **Type-safe** | EndpointMethod grants prevent string typos |
| **Three permission types** | CRUD, EndpointMethod, Group |
| **Defense in depth** | Multiple authorization layers |
| **Internal/External** | Control which methods are REST-accessible |
| **Default deny** | Everything disallowed unless explicitly granted |
| **Valid periods** | Time-based agreements (valid from/to) |

## Documentation

- **Perst Usage**: [PERST_USAGE.md](PERST_USAGE.md)
- **Testing**: [docs/TestingGuide.md](docs/TestingGuide.md)
- **Architecture**: [docs/PerstIntegration.md](docs/PerstIntegration.md)

## Differences from Upstream Kiss

| Feature | Upstream | This Fork |
|---------|----------|-----------|
| Database | PostgreSQL only | Perst + PostgreSQL |
| Auth | PostgreSQL users | PerstUser + Actor |
| DB Access | Direct | Through Managers |
| Authorization | Session-based | Actor-based |
