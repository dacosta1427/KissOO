# Manager at the Gate Pattern

## ⚠️ CRITICAL: Authorization is MANDATORY

**Every service call that modifies data MUST pass the calling Actor to the Manager for authorization check.**

```java
// ❌ WRONG: Bypassing authorization
Actor actor = ActorManager.getInstance().getByUuid(uuid);
actor.setName("Jane");
ActorManager.getInstance().update(actor);  // NO AUTHORIZATION CHECK!

// ✅ CORRECT: Always pass Actor for authorization
UserData ud = servlet.getUserData();
Actor currentActor = ActorManager.getInstance().getByUserId((int) ud.getUserId());
if (currentActor == null) {
    outjson.put("error", "Not authenticated");
    return;
}
Actor actor = ActorManager.getInstance().getByUuid(uuid);
actor.setName("Jane");
if (!ActorManager.getInstance().update(currentActor, actor)) {
    outjson.put("error", "Not authorized");
    return;
}
```

## Overview

The "Manager at the Gate" pattern ensures that ALL access to domain objects goes through a dedicated Manager class. This provides a central point for:

- **Validation** - Business rules before data changes
- **Authorization** - Who can do what
- **Audit Logging** - Track all operations
- **Business Logic** - Encapsulate domain rules

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                         │
│                  (HTTP Controllers)                       │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   Manager Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐                 │
│  │ ActorManager   │  │UserManager    │  ...            │
│  └────────┬────────┘  └────────┬────────┘                 │
└───────────┼─────────────────────┼───────────────────────────┘
            │                     │
            ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│               PerstHelper (Data Access)                   │
└───────────┬─────────────────────┬───────────────────────────┘
            │                     │
            ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Perst Database                         │
│                  (CVersion Objects)                        │
└─────────────────────────────────────────────────────────────┘
```

## BaseManager

All Managers extend `BaseManager`:

```java
public abstract class BaseManager<T> {
    public abstract Collection<T> getAll();
    public abstract T getByKey(String key);
    public abstract T create(Object... params);
    public abstract boolean update(T entity);
    public abstract boolean delete(T entity);
    protected abstract boolean validate(T entity);
}
```

## Example: ActorManager

```java
public class ActorManager extends BaseManager<Actor> {
    
    // Singleton
    private static ActorManager instance;
    public static synchronized ActorManager getInstance() {
        if (instance == null) instance = new ActorManager();
        return instance;
    }
    
    @Override
    public Actor getByKey(String name) {
        return PerstHelper.retrieveObject(Actor.class, "name", name);
    }
    
    @Override
    public boolean validate(Actor actor) {
        return actor != null && 
               actor.getName() != null && 
               !actor.getName().isEmpty();
    }
    
    // Domain-specific methods
    public List<Actor> getByType(String type) { ... }
    public boolean exists(String name) { ... }
}
```

## Usage in Services

All service methods MUST follow this pattern for authorization:

```java
public void myServiceMethod(JSONObject injson, JSONObject outjson, 
                           Connection db, ProcessServlet servlet) {
    // 1. Get current user from session
    UserData ud = servlet.getUserData();
    if (ud == null) {
        outjson.put("error", "Not authenticated");
        return;
    }
    
    // 2. Get the Actor for authorization
    Actor actor = ActorManager.getInstance().getByUserId((int) ud.getUserId());
    if (actor == null) {
        outjson.put("error", "No actor linked to user");
        return;
    }
    
    // 3. Use authorization-enabled Manager methods
    Collection<Actor> actors = ActorManager.getInstance().getAll(actor);
    if (actors == null) {
        outjson.put("error", "Not authorized to read actors");
        return;
    }
    
    // Continue with business logic...
}
```

### Authorization Flow

```
HTTP Request with _uuid
        ↓
kissweb validates session → UserData
        ↓
Service gets UserData via servlet.getUserData()
        ↓
Service gets Actor via ActorManager.getByUserId(userId)
        ↓
Service calls Manager methods with Actor parameter
        ↓
Manager.checkPermission(actor, action, resource)
        ↓
✓ Authorized → Execute operation
✗ Not authorized → Return error
```

### Action Constants

| Constant | Purpose |
|----------|---------|
| `ACTION_CREATE` | Creating new entities |
| `ACTION_READ` | Reading/listing entities |
| `ACTION_UPDATE` | Modifying entities |
| `ACTION_DELETE` | Removing entities |
| `ACTION_ADMIN` | Administrative operations |

## Benefits

| Benefit | Description |
|---------|-------------|
| **Validation** | All creates/updates go through validate() |
| **Centralized Logic** | Business rules in one place |
| **Testability** | Easy to mock Managers |
| **Security** | Authorization checks in one place |
| **Audit** | Can log all operations |

## Adding New Domain Managers

1. Create `domain/database/XXXManager.java` extending `BaseManager<T>`
2. Implement all abstract methods
3. Add domain-specific methods
4. Implement `getResourceName()` - returns the resource name for authorization logging
5. Override `checkPermission(Actor, action, resource)` for custom authorization logic
6. Register in PerstDBRoot if needed

### Authorization Template

```java
public class MyManager extends BaseManager<MyEntity> {
    
    @Override
    protected String getResourceName() {
        return "MyEntity";
    }
    
    @Override
    protected boolean checkPermission(Actor actor, String action, String resource) {
        if (actor == null) {
            return ACTION_READ.equals(action);  // Allow read for anon
        }
        
        // Custom authorization logic
        if (ACTION_ADMIN.equals(action)) {
            return "ADMIN".equals(actor.getType());
        }
        
        // Default: check if actor is active
        return actor.isActive();
    }
}
```

## Current Managers

| Manager | Purpose |
|---------|---------|
| `ActorManager` | Manages Actor entities |
| `PerstUserManager` | Manages User entities + authentication |
