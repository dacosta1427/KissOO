# Bitmap-Based Authorization System

## Overview

The KissOO system uses a bitmap-based authorization system for managing endpoint permissions. Each endpoint is assigned a unique bit position, and permissions are stored as BigInteger bitmaps in Perst OODBMS.

## Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────────┐
│                     EndpointRegistry                            │
│  (Persisted in Perst)                                           │
│  Maps: endpoint name → bit position                            │
│  - registerEndpoint(name) → assigns next available bit         │
│  - getEndpointBit(name) → returns bit for endpoint            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     RolePermissions                             │
│  (Persisted in Perst)                                           │
│  Maps: Role → default permission bitmap                        │
│  - getDefaultPermissions(role) → BigInteger                    │
│  - grantEndpointToRole(role, endpoint)                        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        Agreement                                │
│  (Perst entity attached to each AActor)                        │
│  Fields:                                                        │
│  - role: Role (MEMBER, CLEANER, OWNER, ADMIN, SUPER_ADMIN)     │
│  - endpointPermissions: BigInteger (bitmap)                   │
│  - crudPermissions: Set<String>                                │
│  - groups: Set<Group>                                          │
└─────────────────────────────────────────────────────────────────┘
```

## Key Classes

### 1. Endpoint.java (Annotation)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpoint {
    String name();              // Unique name: "services.ClassName.methodName"
    String description() default "";
    boolean external() default true;
    Class<?> resource() default Object.class;
}
```

Usage in Groovy services:
```groovy
@Endpoint(name = "services.CleaningService.getCleaners",
          description = "Get all cleaners",
          resource = Cleaner.class)
def getCleaners(JSONObject injson, JSONObject outjson, ...) {
    // ...
}
```

### 2. EndpointRegistry.java

Manages endpoint-to-bit mapping. Persisted in Perst.

**Key Methods:**
```java
// Register a new endpoint (auto-assigns bit)
public static BigInteger registerEndpoint(String endpointName)

// Get bit for endpoint
public static BigInteger getEndpointBit(String endpointName)

// Check if registered
public static boolean isRegistered(String endpointName)

// Get all endpoints
public static Map<String, BigInteger> getAllEndpoints()
```

**Example:**
```java
BigInteger bit = EndpointRegistry.registerEndpoint("services.CleaningService.getCleaners");
// bit = 2 (binary: 10)
```

### 3. RolePermissions.java

Manages default permission bitmaps for each role. Persisted in Perst.

**Key Methods:**
```java
// Get default permissions for a role
public static BigInteger getDefaultPermissions(Role role)

// Set default permissions
public static void setDefaultPermissions(Role role, BigInteger permissions)

// Grant endpoint to role
public static void grantEndpointToRole(Role role, String endpointName)

// Revoke endpoint from role
public static void revokeEndpointFromRole(Role role, String endpointName)

// Check if role has endpoint
public static boolean roleHasEndpoint(Role role, String endpointName)
```

### 4. Agreement.java

Perst entity attached to each AActor. Contains permission data.

**Key Methods:**
```java
// Endpoint permissions (bitmap)
public BigInteger getEndpointPermissions()
public void grantEndpoint(String endpointName)
public void revokeEndpoint(String endpointName)
public boolean hasEndpointPermission(String endpointName)

// Role
public Role getRole()
public void setRole(Role role)

// CRUD permissions (legacy)
public void grant(Class<?> resource, String action)
public boolean hasCrudPermission(Class<?> resource, String action)

// Groups
public void addGroup(Group group)
public boolean hasGroup(String groupName)
```

### 5. PermissionService.groovy

REST API for managing permissions.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/permissions` | List all registered endpoints |
| GET | `/permissions/role/{role}` | Get permissions for a role |
| POST | `/permissions/role/{role}/grant` | Grant endpoint to role |
| POST | `/permissions/role/{role}/revoke` | Revoke endpoint from role |
| GET | `/permissions/actor/{actorOid}` | Get effective permissions for actor |
| POST | `/permissions/actor/{actorOid}/grant` | Grant endpoint to specific actor |
| POST | `/permissions/actor/{actorOid}/revoke` | Revoke endpoint from specific actor |
| GET | `/permissions/roles` | List all role permission summaries |
| POST | `/permissions/register` | Manually register endpoint |

## Permission Flow

### 1. Authorization Check

When a request comes in:

```
1. GroovyService loads .groovy file
2. Finds @Endpoint annotated method
3. Gets endpoint name from annotation
4. Gets bit from EndpointRegistry
5. Checks: (actor.permissions & endpointBit) != 0
6. If true → execute, else → deny
```

### 2. Effective Permissions Calculation

An actor's effective permissions = explicit permissions OR role default permissions

```java
BigInteger explicit = agreement.getEndpointPermissions();
BigInteger rolePerms = RolePermissions.getDefaultPermissions(agreement.getRole());

// If no explicit, inherit from role
BigInteger effective = (explicit.signum() == 0) ? rolePerms : explicit.or(rolePerms);
```

### 3. Granting Permissions

**To a Role (affects all actors with that role):**
```json
POST /permissions/role/ADMIN/grant
{
  "roleName": "ADMIN",
  "endpointName": "services.CleaningService.getCleaners"
}
```

**To a Specific Actor (overrides role):**
```json
POST /permissions/actor/12345/grant
{
  "actorOid": 12345,
  "endpointName": "services.CleaningService.getCleaners"
}
```

## Roles

Defined in `Role.java`:
- `SUPER_ADMIN` - Full system access
- `ADMIN` - Content management
- `OWNER` - House owner
- `CLEANER` - Cleaning staff
- `MEMBER` - Basic member (default)

## Configuration

In `application.ini`:
```ini
# Default role for new actors
DefaultRole = MEMBER
```

## Example Usage

### 1. Register endpoints automatically

When a Groovy service with @Endpoint annotations is first accessed, endpoints are automatically registered:

```groovy
// CleaningService.groovy
class CleaningService {
    
    @Endpoint(name = "services.CleaningService.getCleaners")
    def getCleaners(...) { ... }
    
    @Endpoint(name = "services.CleaningService.createCleaner")
    def createCleaner(...) { ... }
}
```

On first call to `getCleaners`, the endpoint is registered with bit 1.

### 2. Grant permissions to role

```json
POST /services.PermissionService.grantRoleEndpoint
{
  "roleName": "ADMIN",
  "endpointName": "services.CleaningService.getCleaners"
}
```

All actors with role ADMIN now have access to getCleaners.

### 3. Check actor permissions

```json
GET /services.PermissionService.getActorPermissions
{
  "actorOid": 12345
}
```

Returns:
```json
{
  "actorOid": 12345,
  "actorName": "John",
  "role": "ADMIN",
  "explicitPermissions": "0",
  "rolePermissions": "2",
  "effectivePermissions": "2",
  "endpointCount": 1,
  "endpoints": ["services.CleaningService.getCleaners"]
}
```

## Persistence

Both `EndpointRegistry` and `RolePermissions` are persisted in Perst:
- Loaded on first access
- Saved when modified
- Survives server restarts
- Endpoint bits remain stable across restarts

## Migration from Old System

The old system used `Set<EndpointMethod>` in Agreement. The new system uses BigInteger bitmap:

| Old (Set) | New (Bitmap) |
|----------|--------------|
| `grant(EndpointMethod)` | `grantEndpoint(String name)` |
| `revoke(EndpointMethod)` | `revokeEndpoint(String name)` |
| `canExecute(EndpointMethod)` | `hasEndpointPermission(String name)` |
| `Set<EndpointMethod>` | `BigInteger` |

The CRUD permissions (`crudPermissions`) remain unchanged and continue to work alongside the new bitmap system.