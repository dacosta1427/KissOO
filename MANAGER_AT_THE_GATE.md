# Manager at the Gate Pattern

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

Instead of direct PerstHelper calls:

```java
// ❌ WRONG: Bypassing the Manager
Actor actor = PerstHelper.retrieveObject(Actor.class, "name", "John");
actor.setName("Jane");
PerstHelper.storeModifiedObject(actor);
```

Use the Manager:

```java
// ✅ CORRECT: Going through the Manager
ActorManager mgr = ActorManager.getInstance();
Actor actor = mgr.getByKey("John");
actor.setName("Jane");
mgr.update(actor);
```

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
4. Register in PerstDBRoot if needed

## Current Managers

| Manager | Purpose |
|---------|---------|
| `ActorManager` | Manages Actor entities |
| `PerstUserManager` | Manages User entities + authentication |
