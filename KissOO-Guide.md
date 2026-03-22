# KissOO Comprehensive Guide

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Perst OODB Integration](#perst-oodb-integration)
4. [Manager at the Gate Authorization](#manager-at-the-gate-authorization)
5. [Backend Services](#backend-services)
6. [REST API](#rest-api)
7. [Authentication System](#authentication-system)
8. [Configuration Reference](#configuration-reference)
9. [Build System](#build-system)
10. [Development Guide](#development-guide)

---

## Overview

KissOO is a fork of the Kiss web framework with **Perst OODBMS** integration. It's a Java-based full-stack web application framework that replaces traditional SQL databases with object-oriented persistence.

### Key Features
- **Perst OODBMS** - Object-oriented database (no SQL)
- **Manager at the Gate** - Three-layer authorization pattern
- **Multi-language Services** - Java, Groovy, Lisp support
- **Hot Reload** - Services compile on change
- **Svelte 5 Frontend** - Modern reactive UI

### Technology Stack
| Layer | Technology |
|-------|------------|
| **Language** | Java 17+, Groovy |
| **Database** | Perst OODBMS with Lucene search |
| **Frontend** | Svelte 5, Tailwind CSS |
| **Server** | Tomcat 11 (Jakarta EE 11) |
| **Build** | Custom `bld` tool |

---

## Architecture

### Directory Structure

```
KissOO/
├── src/main/
│   ├── core/                    # Kiss framework (don't modify)
│   │   └── org/kissweb/
│   │       ├── restServer/      # ProcessServlet, MainServlet
│   │       └── database/        # SQL abstractions (not used with Perst)
│   ├── precompiled/             # Infrequently changing code
│   │   ├── mycompany/
│   │   │   ├── domain/          # Domain entities (Actor, PerstUser, etc.)
│   │   │   └── database/        # Manager classes
│   │   └── oodb/                # Perst configuration classes
│   ├── backend/
│   │   ├── services/            # REST services (frequently changing)
│   │   ├── KissInit.groovy      # Application initialization
│   │   └── application.ini      # Configuration file
│   └── frontend-svelte/         # Svelte 5 frontend
├── data/
│   └── oodb                     # Perst database file
├── libs/                        # JAR dependencies
└── tomcat/                      # Embedded Tomcat server
```

### Request Flow

```
┌─────────┐    JSON     ┌────────────────┐    Service    ┌─────────────┐
│ Browser │ ──────────> │ MainServlet    │ ───────────> │ Groovy/Java │
│         │             │ /rest endpoint │              │ Services    │
└─────────┘             └────────────────┘              └─────────────┘
                              │                                │
                              │ Session Check                  │ Manager Call
                              ▼                                ▼
                        ┌──────────┐                   ┌──────────────┐
                        │UserCache │                   │ PerstStorage │
                        │ (UUIDs)  │                   │  Manager     │
                        └──────────┘                   └──────────────┘
                                                           │
                                                           ▼
                                                     ┌──────────┐
                                                     │ Perst OODB│
                                                     │  (data/oodb)│
                                                     └──────────┘
```

---

## Perst OODB Integration

### What is Perst?

Perst is a **pure Java object-oriented database** that stores objects directly without SQL translation. It provides:
- Direct object storage (no ORM needed)
- Automatic versioning with CDatabase
- Lucene full-text search integration
- ACID transactions
- Compact storage

### Configuration

**`src/main/backend/application.ini`**:
```ini
[main]
PerstEnabled = true
PerstUseCDatabase = true
PerstDatabasePath = /home/dacosta/Projects/KissOO/data/oodb
PerstPagePoolSize = 536870912
PerstNoflush = false
PerstOptimizeInterval = 86400
```

| Setting | Description | Default |
|---------|-------------|---------|
| `PerstEnabled` | Enable Perst OODBMS | `true` |
| `PerstUseCDatabase` | Use CDatabase for versioning | `true` |
| `PerstDatabasePath` | Path to database file | `data/oodb` |
| `PerstPagePoolSize` | Page cache size in bytes | `536870912` (512MB) |
| `PerstOptimizeInterval` | Lucene optimize interval (seconds) | `86400` (24h) |

### Key Classes

#### PerstStorageManager
Single entry point for all Perst operations:

```java
// src/main/precompiled/oodb/PerstStorageManager.java

public class PerstStorageManager {
    private static Storage storage;
    private static UnifiedDBManager dbManager;
    
    public static void initialize() {
        // Create storage with configured path and page pool
        storage = StorageFactory.createStorage();
        storage.open(PerstConfig.getDatabasePath(), PerstConfig.getPagePoolSize());
        
        // Initialize database manager
        dbManager = new UnifiedDBManager(storage);
        dbManager.open();
        
        // Store in MainServlet environment (no KISS core modifications)
        MainServlet.putEnvironment("perstDBManager", dbManager);
    }
    
    // CRUD Operations
    public static <T extends CVersion> Collection<T> getAll(Class<T> clazz) {
        return dbManager.getAll(clazz);
    }
    
    public static <T extends CVersion> T getByOid(long oid, Class<T> clazz) {
        return dbManager.getByOid(oid, clazz);
    }
    
    // Transaction support
    public static TransactionContainer createContainer() {
        return new TransactionContainer(storage);
    }
    
    public static boolean store(TransactionContainer tc) {
        return tc.commit();
    }
}
```

#### PerstConfig
Configuration manager (singleton):

```java
// src/main/precompiled/oodb/PerstConfig.java

public class PerstConfig {
    private static PerstConfig instance;
    
    private boolean perstEnabled;
    private boolean useCDatabase;
    private String databasePath;
    private long pagePoolSize;
    
    public static void initialize() {
        if (instance == null) {
            instance = new PerstConfig();
            instance.load();
        }
    }
    
    private void load() {
        Properties props = MainServlet.readIniFile("application.ini", "main");
        perstEnabled = Boolean.parseBoolean(props.getProperty("PerstEnabled", "false"));
        useCDatabase = Boolean.parseBoolean(props.getProperty("PerstUseCDatabase", "true"));
        databasePath = props.getProperty("PerstDatabasePath", "data/oodb");
        pagePoolSize = Long.parseLong(props.getProperty("PerstPagePoolSize", "536870912"));
        
        // Convert relative paths to absolute
        if (!new File(databasePath).isAbsolute()) {
            String appPath = MainServlet.getApplicationPath();
            databasePath = appPath + File.separator + databasePath;
        }
    }
}
```

### Domain Entities

All domain entities extend `CVersion` for automatic versioning:

```java
// src/main/precompiled/mycompany/domain/Actor.java

@Indexable            // Enable Perst indexing
@FullTextSearchable   // Enable Lucene full-text search
public class Actor extends CVersion implements Serializable {
    
    @Indexable
    private String uuid;
    
    @Indexable
    private String name;
    
    private String type;
    private boolean active;
    private Date createdDate;
    private String userId;
    private Agreement agreement;
    
    // Default constructor required by Perst
    public Actor() {
    }
    
    // Getters and setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // ... more getters/setters
}
```

**Key Rules:**
1. Must extend `CVersion` (for versioning) or `Persistent` (simple)
2. Must have a default no-arg constructor
3. All fields are automatically persisted
4. Use `@Indexable` for indexed fields
5. Use `@FullTextSearchable` for text search

### Manager Pattern

Manager classes provide CRUD operations with permission checking:

```java
// src/main/precompiled/mycompany/database/ActorManager.java

public class ActorManager extends BaseManager<Actor> {
    
    /**
     * Get all actors
     */
    public static Collection<Actor> getAll() {
        return PerstStorageManager.getAll(Actor.class);
    }
    
    /**
     * Find actor by name
     */
    public static Actor getByName(String name) {
        return PerstStorageManager.find(Actor.class, "name", name);
    }
    
    /**
     * Find actor by UUID
     */
    public static Actor getByUuid(String uuid) {
        return PerstStorageManager.find(Actor.class, "uuid", uuid);
    }
    
    /**
     * Create new actor (uses TransactionContainer for atomicity)
     */
    public static Actor create(String name, String type, Agreement agreement) {
        Actor actor = new Actor();
        actor.setUuid(UUID.randomUUID().toString());
        actor.setName(name);
        actor.setType(type);
        actor.setActive(true);
        actor.setCreatedDate(new Date());
        actor.setAgreement(agreement);
        
        // Atomic operation
        TransactionContainer tc = PerstStorageManager.createContainer();
        tc.addInsert(actor);
        if (!PerstStorageManager.store(tc)) {
            throw new RuntimeException("Failed to create actor");
        }
        
        return actor;
    }
    
    /**
     * Update actor
     */
    public static void update(Actor actor) {
        actor.setLastModifiedDate(new Date());
        
        TransactionContainer tc = PerstStorageManager.createContainer();
        tc.addUpdate(actor);
        PerstStorageManager.store(tc);
    }
    
    /**
     * Delete actor
     */
    public static void delete(Actor actor) {
        TransactionContainer tc = PerstStorageManager.createContainer();
        tc.addDelete(actor);
        PerstStorageManager.store(tc);
    }
}
```

### User Management

**PerstUserManager** handles authentication:

```java
// src/main/precompiled/mycompany/database/PerstUserManager.java

public class PerstUserManager extends BaseManager<PerstUser> {
    
    /**
     * Authenticate user
     */
    public static PerstUser authenticate(String username, String password) {
        PerstUser user = findByUsername(username);
        if (user == null) {
            return null;
        }
        
        String hashedPassword = hashPassword(password, user.getSalt());
        if (!hashedPassword.equals(user.getPasswordHash())) {
            return null;
        }
        
        if (!user.canLogin()) {
            return null;  // Inactive or email not verified
        }
        
        // Update last login
        user.setLastLoginDate(new Date());
        PerstStorageManager.update(user);
        
        return user;
    }
    
    /**
     * Create new user
     */
    public static PerstUser create(String username, String password, long userId) {
        PerstUser user = new PerstUser();
        user.setUsername(username.toLowerCase());
        user.setSalt(generateSalt());
        user.setPasswordHash(hashPassword(password, user.getSalt()));
        user.setActive(true);
        user.setEmailVerified(true);  // Required for canLogin()
        user.setUserId(userId);
        
        TransactionContainer tc = PerstStorageManager.createContainer();
        tc.addInsert(user);
        PerstStorageManager.store(tc);
        
        return user;
    }
    
    /**
     * Find user by username
     */
    public static PerstUser findByUsername(String username) {
        return PerstStorageManager.find(PerstUser.class, "username", username.toLowerCase());
    }
    
    /**
     * Hash password with salt
     */
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashed = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### TransactionContainer Pattern

For atomic batch operations:

```java
// Create container
TransactionContainer tc = PerstStorageManager.createContainer();

// Add operations
tc.addInsert(newEntity);      // Insert
tc.addUpdate(existingEntity); // Update
tc.addDelete(oldEntity);      // Delete

// Commit all atomically
boolean success = PerstStorageManager.store(tc);

if (!success) {
    // Handle conflict/failure
}
```

**Benefits:**
- All-or-nothing execution
- Automatic conflict detection
- Version history tracking
- Crash recovery support

---

## Manager at the Gate Authorization

Three-layer authorization pattern ensuring every endpoint is protected.

### Core Classes

#### EndpointMethod
Type-safe endpoint identifier:

```java
// src/main/precompiled/mycompany/domain/EndpointMethod.java

public class EndpointMethod extends Persistent {
    private String service;    // e.g., "services.Users"
    private String method;     // e.g., "getRecords"
    
    public EndpointMethod() {}
    
    public EndpointMethod(String service, String method) {
        this.service = service;
        this.method = method;
    }
    
    public boolean matches(String service, String method) {
        return this.service.equals(service) && this.method.equals(method);
    }
}
```

#### Agreement
Permission contract linking an Actor to allowed operations:

```java
// src/main/precompiled/mycompany/domain/Agreement.java

public class Agreement extends CVersion {
    private Actor actor;
    private Set<EndpointMethod> allowedMethods;
    private Map<String, Set<String>> crudPermissions; // className -> CRUD
    
    public boolean hasEndpointPermission(String service, String method) {
        return allowedMethods.stream()
            .anyMatch(em -> em.matches(service, method));
    }
    
    public boolean hasCrudPermission(String className, String operation) {
        Set<String> perms = crudPermissions.get(className);
        return perms != null && perms.contains(operation);
    }
}
```

#### Group
Grouping mechanism for permissions:

```java
// src/main/precompiled/mycompany/domain/Group.java

public class Group extends CVersion {
    private String name;
    private Set<Actor> members;
    private Agreement agreement;  // Shared permissions
}
```

#### Actor
Entity that can own/perform actions:

```java
// src/main/precompiled/mycompany/domain/Actor.java

public class Actor extends CVersion {
    private String uuid;
    private String name;
    private String type;
    private boolean active;
    private Agreement agreement;
    
    public Agreement getAgreement() {
        return agreement;
    }
}
```

### Authorization Flow

```
┌─────────────┐
│ Frontend    │
│ Request     │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. ENDPOINT LEVEL                                           │
│    ProcessServlet.checkEndpointPermission(service, method)  │
│    - Uses Actor.getAgreement().hasEndpointPermission()      │
│    - If no agreement → DENY                                 │
└──────┬──────────────────────────────────────────────────────┘
       │ ALLOW
       ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. MANAGER LEVEL                                            │
│    BaseManager.checkPermission(actor, crud, Class)          │
│    - Uses Actor.getAgreement().hasCrudPermission()          │
│    - If not allowed → DENY                                  │
└──────┬──────────────────────────────────────────────────────┘
       │ ALLOW
       ▼
┌─────────────┐
│ Execute     │
│ Service     │
└─────────────┘
```

### Creating Protected Services

```java
// src/main/backend/services/ActorService.java

public class ActorService {
    
    // Define endpoint methods (type-safe)
    private static final EndpointMethod GET_ALL = 
        new EndpointMethod("services.ActorService", "getAll");
    private static final EndpointMethod CREATE = 
        new EndpointMethod("services.ActorService", "create");
    
    /**
     * Get all actors - requires GET permission on Actor class
     */
    public void getAll(JSONObject injson, JSONObject outjson, 
                       Connection db, ProcessServlet servlet) throws Exception {
        
        // 1. Get authenticated actor (set by ProcessServlet)
        Actor actor = (Actor) servlet.getAttribute("currentActor");
        
        // 2. Manager-level permission check
        BaseManager.checkPermission(actor, "GET", Actor.class);
        
        // 3. Execute operation
        Collection<Actor> actors = ActorManager.getAll();
        
        // 4. Build response
        outjson.put("_Success", true);
        JSONArray arr = new JSONArray();
        for (Actor a : actors) {
            arr.put(a.toJSON());
        }
        outjson.put("actors", arr);
    }
    
    /**
     * Create actor - requires POST permission on Actor class
     */
    public void create(JSONObject injson, JSONObject outjson,
                       Connection db, ProcessServlet servlet) throws Exception {
        
        Actor actor = (Actor) servlet.getAttribute("currentActor");
        
        // Manager-level check
        BaseManager.checkPermission(actor, "POST", Actor.class);
        
        // Extract parameters
        String name = injson.getString("name");
        String type = injson.getString("type");
        
        // Create
        Actor newActor = ActorManager.create(name, type, actor.getAgreement());
        
        outjson.put("_Success", true);
        outjson.put("id", newActor.getId());
    }
}
```

---

## Backend Services

### Service Structure

All services follow this signature:

```java
public void methodName(JSONObject injson, JSONObject outjson, 
                       Connection db, ProcessServlet servlet) throws Exception
```

| Parameter | Description |
|-----------|-------------|
| `injson` | JSON data from frontend |
| `outjson` | JSON data to return |
| `db` | Database connection (null if no SQL) |
| `servlet` | HTTP request/response access |

### Groovy Service Example

```groovy
// src/main/backend/services/Users.groovy

class Users {
    
    void getRecords(JSONObject injson, JSONObject outjson, 
                    Connection db, ProcessServlet servlet) {
        try {
            Collection<PerstUser> users = PerstUserManager.getAll()
            outjson.put("_Success", true)
            outjson.put("rows", users.collect { it.toJSON() })
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void addRecord(JSONObject injson, JSONObject outjson,
                   Connection db, ProcessServlet servlet) {
        String userName = injson.getString("userName")
        String userPassword = injson.getString("userPassword")
        long userId = injson.optLong("userId", System.currentTimeMillis())
        
        PerstUser user = PerstUserManager.create(userName, userPassword, userId)
        
        outjson.put("_Success", true)
        outjson.put("id", user.getId())
    }
    
    void deleteRecord(JSONObject injson, JSONObject outjson,
                      Connection db, ProcessServlet servlet) {
        long id = injson.getLong("id")
        PerstUser user = PerstUserManager.getByOid(id, PerstUser.class)
        
        if (user != null) {
            PerstUserManager.delete(user)
            outjson.put("_Success", true)
        } else {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", "User not found")
        }
    }
}
```

### Java Service Example

```java
// src/main/backend/services/ActorService.java

public class ActorService {
    
    public void getAll(JSONObject injson, JSONObject outjson,
                      Connection db, ProcessServlet servlet) throws Exception {
        try {
            Collection<Actor> actors = ActorManager.getAll();
            
            outjson.put("_Success", true);
            JSONArray arr = new JSONArray();
            for (Actor actor : actors) {
                arr.put(actor.toJSON());
            }
            outjson.put("rows", arr);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }
}
```

### Service Discovery Order

1. **GroovyService** - Checks for `.groovy` files, compiles if needed
2. **JavaService** - Checks for `.java` files, compiles if needed
3. **LispService** - (Disabled) Would check for `.lisp` files
4. **CompiledJavaService** - (Not implemented) Pre-compiled classes

### Dynamic Loading

Services are automatically loaded and cached:

```java
// GroovyService caching logic
Class<?> loadGroovyClass(String fileName) {
    File file = new File(fileName);
    Long lastModified = classCache.get(fileName);
    
    if (lastModified == null || file.lastModified() > lastModified) {
        // Re-compile if file changed
        Class<?> clazz = groovyClassLoader.parseClass(file);
        classCache.put(fileName, file.lastModified());
        return clazz;
    }
    
    return classCache.get(fileName);
}
```

---

## REST API

### Single Endpoint Architecture

All requests go to **`/rest`**:

```
POST /rest
Content-Type: application/json

{
    "_class": "services.Users",     // Service class
    "_method": "getRecords",        // Method name
    "_uuid": "abc-123-def",         // Session UUID
    "param1": "value1"              // Additional params
}
```

### Request Format

| Field | Description | Required |
|-------|-------------|----------|
| `_class` | Service class path (empty for core methods) | Yes |
| `_method` | Method name to call | Yes |
| `_uuid` | Session UUID (omitted for login) | For authenticated requests |
| Other | Any additional parameters | As needed |

### Response Format

**Success:**
```json
{
    "_Success": true,
    "data": "...",
    "rows": [...]
}
```

**Error:**
```json
{
    "_Success": false,
    "_ErrorMessage": "Description of error",
    "_ErrorCode": 1
}
```

**Error Codes:**
| Code | Description |
|------|-------------|
| 1 | General error |
| 2 | Session expired/invalid |

### Built-in Core Methods

When `_class` is empty:

| Method | Description |
|--------|-------------|
| `Login` | Authenticate user, returns UUID |
| `Logout` | Invalidate session |
| `LoginRequired` | Returns if authentication required |
| `checkLogin` | Verify session is valid |

### Frontend Integration

```typescript
// src/lib/services/Server.ts

export class Server {
    private static url = '';
    private static uuid = '';
    
    static async call(cls: string, method: string, params: any = {}): Promise<any> {
        const payload = {
            ...params,
            _uuid: this.uuid,
            _method: method,
            _class: cls
        };
        
        const response = await fetch(`${this.url}/rest`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        const result = await response.json();
        
        if (!result._Success && result._ErrorCode === 2) {
            // Session expired
            this.uuid = '';
            throw new Error('Session expired');
        }
        
        return result;
    }
}
```

---

## Authentication System

### Login Flow

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ Frontend    │         │ ProcessServlet│         │ PerstUser    │
│             │         │              │         │ Manager      │
└──────┬──────┘         └──────┬───────┘         └──────┬───────┘
       │                       │                        │
       │ POST /rest            │                        │
       │ {Login, username, pw} │                        │
       ├──────────────────────>│                        │
       │                       │                        │
       │                       │ authenticate()         │
       │                       ├───────────────────────>│
       │                       │                        │
       │                       │  PerstUser or null     │
       │                       │<───────────────────────┤
       │                       │                        │
       │                       │ UserCache.newUser()    │
       │                       │ (creates UUID)         │
       │                       │                        │
       │ {_Success, uuid}      │                        │
       │<──────────────────────┤                        │
       │                       │                        │
```

### Session Management (UserCache)

```java
// org.kissweb.restServer.UserCache

public class UserCache {
    private static Hashtable<String, UserData> uuidTable = new Hashtable<>();
    private static int userInactiveSeconds = 1800; // 30 minutes
    
    /**
     * Create new session
     */
    public static UserData newUser(String username, String password, long userId) {
        UserData ud = new UserData();
        ud.setUsername(username);
        ud.setPassword(password);
        ud.setUserId(userId);
        ud.setUUID(UUID.randomUUID().toString());
        ud.setLastAccess(new Date());
        
        uuidTable.put(ud.getUUID(), ud);
        return ud;
    }
    
    /**
     * Find session by UUID
     */
    public static UserData findUser(String uuid) {
        UserData ud = uuidTable.get(uuid);
        if (ud != null) {
            if (System.currentTimeMillis() - ud.getLastAccess().getTime() > 
                userInactiveSeconds * 1000L) {
                // Session expired
                uuidTable.remove(uuid);
                return null;
            }
            ud.setLastAccess(new Date());
        }
        return ud;
    }
    
    /**
     * Remove session (logout)
     */
    public static void removeUser(String uuid) {
        uuidTable.remove(uuid);
    }
}
```

### Password Security

**PerstUserManager** handles password hashing:

```java
public class PerstUserManager {
    
    private static final int SALT_LENGTH = 16;
    
    /**
     * Generate random salt
     */
    private static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash password with SHA-256
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * Create user with hashed password
     */
    public static PerstUser create(String username, String password, long userId) {
        PerstUser user = new PerstUser();
        user.setUsername(username.toLowerCase().trim());
        user.setSalt(generateSalt());
        user.setPasswordHash(hashPassword(password, user.getSalt()));
        user.setActive(true);
        user.setEmailVerified(true);  // Required for canLogin()
        user.setUserId(userId);
        user.setCreatedDate(new Date());
        
        TransactionContainer tc = PerstStorageManager.createContainer();
        tc.addInsert(user);
        if (!PerstStorageManager.store(tc)) {
            throw new RuntimeException("Failed to create user");
        }
        
        return user;
    }
}
```

---

## Configuration Reference

### application.ini

```ini
[main]

# === General Settings ===
MaxWorkerThreads = 100
UserInactiveSeconds = 1800
RequireAuthentication = true

# === Perst OODB Settings ===
PerstEnabled = true
PerstUseCDatabase = true
PerstDatabasePath = /home/user/KissOO/data/oodb
PerstPagePoolSize = 536870912
PerstNoflush = false
PerstOptimizeInterval = 86400

# === SQL Settings (optional, not used with Perst-only) ===
# SQLDriver = org.postgresql.Driver
# SQLUrl = jdbc:postgresql://localhost:5432/mydb
# SQLUser = postgres
# SQLPassword = secret

# === Logging ===
# Log4j2 config can be specified here
```

### Configuration Classes

**PerstConfig** - Reads Perst settings:
```java
public class PerstConfig {
    private static boolean perstEnabled;
    private static boolean useCDatabase;
    private static String databasePath;
    private static long pagePoolSize;
    private static int optimizeInterval;
    
    // Getters for all settings
}
```

---

## Build System

### Custom `bld` Tool

```bash
# Development mode (run with hot reload)
./bld develop

# Build only
./bld build

# Build for unit tests
./bld unit-tests

# Run tests
java -jar work/KissUnitTest.jar

# Clean build
./bld clean
```

### Directory Structure for Build

```
src/main/
├── precompiled/          # Compiled once, rarely changes
│   ├── mycompany/
│   │   ├── domain/       # Domain entities
│   │   └── database/     # Manager classes
│   └── oodb/             # Perst configuration
│
├── backend/              # Compiled on each change
│   ├── services/         # REST services
│   ├── KissInit.groovy   # Initialization
│   └── application.ini   # Configuration
│
└── core/                 # Framework (don't modify)
    └── org/kissweb/
```

### Adding New Domain Class

1. **Create entity** in `src/main/precompiled/mycompany/domain/`:
```java
public class MyEntity extends CVersion {
    private String name;
    // getters/setters
}
```

2. **Create manager** in `src/main/precompiled/mycompany/database/`:
```java
public class MyEntityManager extends BaseManager<MyEntity> {
    public static Collection<MyEntity> getAll() {
        return PerstStorageManager.getAll(MyEntity.class);
    }
    // ... other CRUD methods
}
```

3. **Create service** in `src/main/backend/services/`:
```groovy
class MyEntityService {
    void getRecords(JSONObject injson, JSONObject outjson, 
                    Connection db, ProcessServlet servlet) {
        // Implementation
    }
}
```

4. **Allow endpoints without auth** (if needed) in `KissInit.groovy`:
```groovy
MainServlet.allowWithoutAuthentication("services.MyEntityService", "getRecords")
```

### Testing

```bash
# Run all tests
java -jar work/KissUnitTest.jar

# Run specific package tests
java -jar work/KissUnitTest.jar --select-package=oodb

# Run with coverage
java -jar work/KissUnitTest.jar --select-package=org.kissweb
```

---

## Development Guide

### Quick Start

1. **Clone and build**:
```bash
git clone <repo>
cd KissOO
./bld develop
```

2. **Configure Perst** in `src/main/backend/application.ini`:
```ini
PerstEnabled = true
PerstDatabasePath = data/oodb
```

3. **Start Tomcat**:
```bash
./startBackend.sh
```

4. **Run frontend**:
```bash
cd src/main/frontend-svelte
npm run dev
```

### Development Workflow

1. **Modify service** in `src/main/backend/services/`
2. **Auto-compilation** - changes picked up automatically
3. **Test** via browser or curl:
```bash
curl -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"services.Users","_method":"getRecords","_uuid":"..."}'
```

### Common Patterns

#### Find or Create
```java
public static Actor findOrCreate(String name, String type, Agreement agreement) {
    Actor actor = getByName(name);
    if (actor == null) {
        actor = create(name, type, agreement);
    }
    return actor;
}
```

#### Batch Operations
```java
public static void batchUpdate(List<Actor> actors) {
    TransactionContainer tc = PerstStorageManager.createContainer();
    for (Actor actor : actors) {
        actor.setLastModifiedDate(new Date());
        tc.addUpdate(actor);
    }
    PerstStorageManager.store(tc);
}
```

#### Full-Text Search
```java
public static Collection<Actor> search(String query) {
    return PerstStorageManager.searchFullText(Actor.class, query);
}
```

### Debugging Tips

1. **Enable Perst logging** - Check `data/oodb.idx/` for Lucene errors
2. **Check transactions** - Use `TransactionContainer` for debugging
3. **Verify indexes** - Ensure `@Indexable` annotations are correct
4. **Session debugging** - Check `UserCache` for active sessions

---

## Quick Reference

### Key Classes

| Class | Location | Purpose |
|-------|----------|---------|
| `PerstStorageManager` | `precompiled/oodb/` | Perst operations |
| `PerstConfig` | `precompiled/oodb/` | Configuration |
| `BaseManager<T>` | `precompiled/mycompany/database/` | CRUD base class |
| `Actor` | `precompiled/mycompany/domain/` | Main entity |
| `PerstUser` | `precompiled/mycompany/domain/` | User entity |
| `ProcessServlet` | `core/org/kissweb/restServer/` | Request handler |
| `UserCache` | `core/org/kissweb/restServer/` | Session storage |

### Common Operations

```java
// Get all
Collection<Actor> all = ActorManager.getAll();

// Find by field
Actor actor = ActorManager.getByName("John");

// Create
Actor newActor = ActorManager.create("John", "USER", agreement);

// Update
ActorManager.update(actor);

// Delete
ActorManager.delete(actor);

// Search
Collection<Actor> results = ActorManager.search("search term");
```

### Configuration Summary

```ini
# Required
PerstEnabled = true
PerstDatabasePath = data/oodb

# Recommended
PerstUseCDatabase = true
PerstPagePoolSize = 536870912
RequireAuthentication = true
```

---

## See Also

- **sv5guide.md** - Svelte 5 frontend guide
- **PERST_USAGE.md** - Detailed Perst usage
- **MANAGER_AT_THE_GATE.md** - Authorization pattern
- **AI/KnowledgeBase.md** - Framework knowledge base

---

*Last Updated: 2026-03-22*
