# Manager at the Gate Pattern

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

---

## Overview

The "Manager at the Gate" pattern ensures ALL access to domain objects goes through:

1. **EndpointMethod** - Type-safe endpoint identifiers (service methods)
2. **Agreement** - Defines what an Actor is allowed to do
3. **Manager** - Performs the actual operation with validation

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      HTTP Request                               │
│            { _class, _method, _uuid, ...params }              │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   EndpointMethod                                │
│   - Type-safe endpoint identifier                              │
│   - Can be external (REST) or internal only                     │
│   - execute() calls Agreement.canExecute()                     │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Agreement                                   │
│   - CRUD permissions: "Actor:create"                          │
│   - EndpointMethod permissions (type-safe!)                    │
│   - Group permissions                                          │
│   - Validity (active, date range)                             │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       Actor                                     │
│   - HAS an Agreement (mandatory!)                              │
│   - CAN belong to Groups                                       │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       Manager                                   │
│   - CRUD operations                                            │
│   - Validation                                                 │
│   - Business logic                                             │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PerstHelper                                  │
│              (Thread-local Perst)                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Core Classes

### 1. EndpointMethod

Represents a callable method in a Service. Two types:

```java
// External endpoint - callable via REST API
public static final EndpointMethod GET_ACTOR = 
    new EndpointMethod("services.ActorService.getActor") {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, 
                                   Connection db, ProcessServlet servlet) {
            // Implementation
            return true;
        }
    };

// Internal only - NOT callable via REST
public static final EndpointMethod DO_INTERNAL = 
    new EndpointMethod("services.ActorService.doInternal", false) {
        @Override
        protected boolean doExecute(...) { return true; }
    };
```

### 2. Agreement

Defines what an Actor can do. Three permission types:

```java
Agreement agreement = new Agreement("ADMIN");

// CRUD permissions
agreement.grant("Actor", "create");
agreement.grant("Actor", "read");
agreement.grant("Actor", "update");
agreement.grant("Actor", "delete");

// EndpointMethod permissions (type-safe!)
agreement.grant(ActorService.GET_ACTOR);
agreement.grant(ActorService.CREATE_ACTOR);

// Group permissions
Group admins = new Group("admins");
admins.grant(ActorService.DELETE_ACTOR);
agreement.addGroup(admins);

// Validity
agreement.setActive(true);
agreement.setValidFrom(date);
agreement.setValidTo(date);  // null = infinite
```

### 3. Group

UNIX-like group permissions:

```java
Group developers = new Group("developers");
developers.grant(ActorService.GET_ACTOR);
developers.grantCrud("Actor", "read");  // CRUD shorthand

actor.addToGroup(developers);
```

### 4. Actor

MUST have an Agreement:

```java
// Create Actor with Agreement
Agreement agreement = new Agreement("USER");
agreement.grant("Actor", "read");
agreement.grant("Actor", "create");

Actor actor = new Actor("John", "RETAIL", agreement);

// Or add later
actor.setAgreement(agreement);
actor.addToGroup(admins);
```

---

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

---

## Usage in Services

### Option 1: EndpointMethod Pattern (Recommended)

Each endpoint is an `EndpointMethod` that checks its own authorization:

```java
public class ActorService {
    
    public static final EndpointMethod GET_ACTOR = 
        new EndpointMethod("services.ActorService.getActor") {
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
                Actor actor = ActorManager.getByUuid(uuid);
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

### Option 2: Manager Pattern

Use Manager methods with Actor parameter:

```java
public void getAllActors(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
    Actor caller = getAuthenticatedActor(servlet);
    if (caller == null) {
        out.put("error", "Not authenticated");
        return;
    }
    
    // Manager checks Agreement automatically
    Collection<Actor> actors = ActorManager.getAll(caller);
    if (actors == null) {
        out.put("error", "Not authorized");
        return;
    }
    
    out.put("actors", actors);
}
```

---

## Creating a New Service

### Step 1: Define EndpointMethods

```java
public class MyService {
    
    // External endpoints (REST accessible)
    public static final EndpointMethod GET_DATA = 
        new EndpointMethod("services.MyService.getData") {
            @Override
            protected boolean doExecute(JSONObject in, JSONObject out, 
                                       Connection db, ProcessServlet servlet) {
                // Implementation
                return true;
            }
        };
    
    // Internal only
    public static final EndpointMethod DO_CALC = 
        new EndpointMethod("services.MyService.doCalc", false) {  // false = internal
            @Override
            protected boolean doExecute(...) { return true; }
        };
}
```

### Step 2: Grant Permissions

```java
// Create agreement for a user
Agreement agreement = new Agreement("USER");
agreement.grant(MyService.GET_DATA);  // Type-safe!

// Or via CRUD
agreement.grant("MyEntity", "read");
agreement.grant("MyEntity", "create");

// Or via group
Group managers = new Group("managers");
managers.grant(MyService.GET_DATA);
agreement.addGroup(managers);
```

---

## Benefits

| Feature | Description |
|---------|-------------|
| **Type-safe** | EndpointMethod grants prevent string typos |
| **Three permission types** | CRUD, EndpointMethod, Group |
| **Defense in depth** | Multiple authorization layers |
| **Internal/External** | Control which methods are REST-accessible |
| **Default deny** | Everything disallowed unless explicitly granted |
| **Valid periods** | Time-based agreements (valid from/to) |

---

## Current Classes

| Class | Purpose |
|-------|---------|
| `EndpointMethod` | Abstract base for endpoints |
| `Agreement` | Permissions container |
| `Group` | Group-based permissions |
| `Actor` | HAS an Agreement |
| `ActorManager` | CRUD operations |
| `PerstUserManager` | User management |
