# KissOO Development Notes

## Build Requirements

### Required JAR Files
The following JARs must be present in `libs/`:
- `abcl.jar` - ABCL Lisp implementation (required for LispService)
  - If missing, Lisp services will fail to compile
  - Workaround: See "Disabling Lisp" below
- `jakarta.mail-2.0.3.jar` - Jakarta Mail for email sending
- `angus-activation-2.0.2.jar` - Jakarta Activation (required by Jakarta Mail)
- `jakarta.activation-api-2.1.3.jar` - Jakarta Activation API

### ooGTxQ (64-bit Perst OODBMS)
KissOO uses **ooGTxQ** — a 64-bit fork of Perst with full OID support (>2³¹ objects).
- JAR: `oodbGTxQ-1.7.1.jar` in `libs/` (included in distribution)
- Source: `../oodbGTxQ/` project
- The `buildLocalDependencies()` method in `Tasks.java` includes the JAR in the build

### External Database Path

### Lombok Usage
All domain classes in `src/main/precompiled/mycompany/domain/` use Lombok `@Getter @Setter` annotations.
- Getters/setters are auto-generated — do NOT write manual getters/setters for fields
- Use `@Getter` only for classes with controlled-mutation fields (Agreement, Group, BenchmarkData, Phone)
- Business methods (e.g., `checkPassword()`, `canLogin()`, `generateVerificationToken()`) are written manually
- Lombok is included via `lombok.jar` in `libs/` and is processed at compile time

## Pure OO Navigation (No ID Fields)
KissOO uses a pure object-oriented approach with Perst OODBMS:
- **No ID/foreign key fields** — objects reference each other directly
- **Actor → PerstUser**: `actor.getPerstUser()` (persisted - NATURAL actors have PU, CORPORATE don't)
- **PerstUser → Actor**: `perstUser.getActor()` (persistent reference)
- **Login flow**: Find PerstUser by username → `pu.getActor()` → cast to Owner/Cleaner
- **Session**: PerstUser is stored directly in `UserData` via `ud.putUserData("perstUser", perstUser)`
- **Navigation example**: `((PerstUser) ud.getUserData("perstUser")).getActor()`

### Actor/PerstUser Relationship
- Every Actor has an ActorType: NATURAL (default) or CORPORATE
- **NATURAL actors**: Have a persisted PerstUser (created in Actor constructor, deactivated by default)
- **CORPORATE actors**: Container only, no PerstUser
- The PerstUser has a persistent `actor` reference back to its Actor
- PerstUser is indexed by username for fast `find(username)` lookup
- To create an Owner/Cleaner: just `new Owner(...)` — PerstUser is auto-created
- Store both together: `tc.addInsert(owner); tc.addInsert(owner.getPerstUser()); PerstStorageManager.store(tc)`

### Object Deletion Protocol
All domain objects inherit from Perst's `CVersion` → `Persistent` → `PinnedPersistent`, which provides built-in deletion support:

**Deletion pattern (always use this):**
```java
TransactionContainer tc = PerstStorageManager.createContainer();
tc.addDelete(object);  // Just add to delete collection
PerstStorageManager.store(tc);  // Perst handles the rest internally
```

**Checking if an object is deleted:**
```java
// Use inherited isDeleted() from PinnedPersistent
if (user.isDeleted()) {
    // User has been deleted
}
```

**Key points:**
- All domain classes (Actor, PerstUser, House, Booking, Schedule, etc.) use this pattern
- No need for custom `deleted` fields — use inherited `isDeleted()` method
- Perst handles DELETED flag internally when object is added to TC's delete collection
- Works with session cache — objects marked deleted will fail `isDeleted()` check

### OO Convenience Methods for API Serialization
Domain classes provide `getXXXOid()` methods for JSON serialization (not ID fields):
- `House.getOwnerOid()`, `House.getCostProfileOid()`
- `Booking.getHouseOid()`
- `Schedule.getCleanerOid()`, `Schedule.getBookingOid()`

These return the OID of the referenced object for API responses. **Never use `getHouseId()`, `getCleanerId()`, etc.** — those methods don't exist.

## Database Architecture

### UnifiedDBManager (ooGTxQ) - Single Entry Point

KissOO uses **UnifiedDBManager** from ooGTxQ as the single entry point for all database operations. This ensures consistency between data storage and retrieval.

**Key Points:**
- `StorageManager` and `PerstConnection` delegate to `UnifiedDBManager`
- `UnifiedDBManager.getRecords(Class)` retrieves all records of a class (not Lucene-based)
- `UnifiedDBManager.find(Class, field, Key)` finds by indexed field
- `UnifiedDBManager.createLink()` creates Perst Link collections

### Required JAR Files
The Perst database is stored **outside the WAR** at the path configured in `application.ini`:
```
PerstDatabasePath = /home/dacosta/kissoo-data/oodb
```
This allows the database to persist across deployments and be placed on NAS/storage.

### Clearing the Database
To completely clear the database:
1. **Kill the server first** — `pkill -9 java` (open file handles prevent deletion)
2. **Delete DB files** — `rm -rf /home/dacosta/kissoo-data/oodb*`
3. **Recreate directory** — `mkdir -p /home/dacosta/kissoo-data`
4. **Restart server** — `./bld develop` or `tomcat/bin/startup.sh`

### UnifiedDBManager Usage

#### Retrieval: Use `getObjects()` NOT `getRecords()`
```groovy
// CORRECT: getObjects() retrieves all objects of a class
Collection<Owner> owners = StorageManager.getAll(Owner.class)

// WRONG: getRecords() does NOT exist in UnifiedDBManager
```

**Why:** `UnifiedDBManager.getObjects(Class<T>)` is the correct method for retrieving all objects of a class. The method `getRecords()` doesn't exist and will cause compilation errors.

#### Storage: Use `createContainer()` (returns sync container)
```groovy
def tc = PerstStorageManager.createContainer()
tc.addInsert(owner)
tc.addInsert(owner.getPerstUser())
PerstStorageManager.store(tc)
```

**Why:** `createContainer()` returns `createSyncContainer()` internally because `CDatabase.open()` does NOT initialize the `linQueue` (async Lucene indexing thread pool). Async containers would queue TCs that are never processed. Sync containers force immediate Lucene indexing via `processLinSync()`.

#### TransactionContainer Pattern
- `createContainer()` — creates a new empty container
- `tc.addInsert(obj)` — adds object for insertion
- `tc.addUpdate(obj)` — adds object for update
- `tc.addDelete(obj)` — adds object for deletion
- `PerstStorageManager.store(tc)` — commits all operations atomically

## Unique Constraints

### PerstUser.username is Globally Unique
The `@Indexable(unique=true)` annotation on `PerstUser.username` creates a unique index across **ALL** PerstUsers in the database. This means:
- Owner PerstUser emails must be unique
- Cleaner PerstUser emails must be unique
- **No email can be shared between an Owner and a Cleaner**
- The admin user's username ("admin") must also be unique

When creating test data, ensure all PerstUser usernames (typically emails) are globally unique across all entity types.

### Rollback Behavior
When `store()` fails (e.g., unique constraint violation), the transaction is rolled back. However, the OIDs assigned during `makePersistent()` are NOT reclaimed — the next successful store will use the next available OID.

## ooGTxQ Integration Details

### UnifiedDBManager Architecture
KissOO uses `UnifiedDBManager` from `oodbGTxQ` as the single entry point for all database operations.

**Key Benefits:**
- Single point of control for all database operations
- Transaction lifecycle managed internally (prevents "Transaction already started" errors)
- Optimistic locking support built-in
- Consistent API for all operations

**Architecture:**
```
StorageManager.store(TransactionContainer)
  → UnifiedDBManager.store()
    → CDatabase.beginTransaction()
    → CDatabase.insert/update/delete()
    → CDatabase.commitTransaction() OR rollbackTransaction()
```

**Key Methods:**
- `createContainer()` - Creates a new TransactionContainer
- `store(TransactionContainer)` - Stores all objects atomically
- `getRecords(Class)` - Retrieves all records of a class
- `find(Class, String, Key)` - Finds records by indexed field

### External Database Path
The Perst database is stored **outside the WAR** at the path configured in `application.ini`:
```
PerstDatabasePath = /home/dacosta/kissoo-data/oodb
```
This allows the database to persist across deployments and be placed on NAS/storage.

### Clearing the Database
To completely clear the database:
1. **Kill the server first** — `pkill -9 java` (open file handles prevent deletion)
2. **Delete DB files** — `rm -rf /home/dacosta/kissoo-data/oodb*`
3. **Recreate directory** — `mkdir -p /home/dacosta/kissoo-data`
4. **Restart server** — `./bld develop` or `tomcat/bin/startup.sh`

### StorageManager Integration
`StorageManager.java` provides static methods that delegate to `UnifiedDBManager`:

```java
// All operations go through UnifiedDBManager
TransactionContainer tc = StorageManager.createContainer();
tc.addInsert(object);
StorageManager.store(tc);
```

**Important:** Always use `StorageManager` static methods. The `UnifiedDBManager` instance is managed internally.

### PerstConnection Integration
`PerstConnection.java` extends the KISS `Connection` class with Perst operations:

```java
PerstConnection conn = new PerstConnection();
TransactionContainer tc = conn.perstCreateContainer();
conn.perstStore(tc);
```

### Critical Fixes Applied
1. **StorageManager.java**: Refactored to use `UnifiedDBManager` instead of direct `CDatabase` calls
2. **PerstConnection.java**: Updated to use `UnifiedDBManager` for all operations
3. **Transaction lifecycle**: Now handled internally by `UnifiedDBManager.store()`

### PerstService.java (Java)
Java services can use `StorageManager` static methods directly:
```java
Collection<House> houses = StorageManager.getAll(House.class);
House h = StorageManager.getByOid(House.class, oid);
```

### JSONObject Usage
Services that interact with Perst should use `org.garret.perst.json.JSONObject` for JSON operations.

## Disabling Lisp Services

If `abcl.jar` is not available, disable Lisp to allow building:

1. Delete these files:
   - `src/main/core/org/kissweb/lisp/ABCL.java`
   - `src/main/core/org/kissweb/restServer/LispService.java`

2. In `ProcessServlet.java`, comment out the LispService call (around line 476-480):
   ```java
   // Lisp service disabled - requires abcl.jar
   // res = (new LispService()).tryLisp(this, response, _className, _method, injson, outjson);
   ```

**Note:** When updating from the upstream Kiss framework, re-apply these changes if abcl.jar is not available.

## Frontend (SvelteKit)

The SvelteKit frontend is in `src/main/frontend-svelte/`.

### Running
```bash
cd src/main/frontend-svelte
npm run dev
```

### Routes
- `/` - Home page
- `/login` - Login page
- `/signup` - Signup page
- `/users` - User management

### Build
```bash
cd src/main/frontend-svelte
npm run build
```

Note: `.svelte-kit/` is auto-generated and should not be committed.

## Debugging Protocols

### UI/UX Issues (Svelte 5)
1. **Dropdown/Select not populating**: Check if data array is empty, then check reactivity:
   - Ensure options array uses `$state` for reactivity
   - Verify API call returns data (check `res.data`)
   - Mutating `const` arrays doesn't trigger re-render
2. **Form not updating**: Ensure form data uses `$state` and bindings are correct
3. **Component not re-rendering**: Check if props are reactive (use `$state` in parent)

### Authentication Issues
1. **Login fails with correct credentials**:
   - Check `PerstUser.canLogin()` requirements: `active && emailVerified`
   - Verify `emailVerified` flag in database (default is `false` for new users)
   - Check backend logs for `PerstAuth` messages
2. **Signup succeeds but login fails**: Likely `emailVerified = false`
3. **Session expires prematurely**: Check `userInactiveSeconds` (default 1800 seconds)

### Backend Service Issues
1. **Service not found**: Ensure service class is in `backend/services/` package
2. **Method not found**: Verify method signature matches `void methodName(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)`
3. **Database not available**: Check Perst initialization in `KissInit.groovy`

### Quick Checks
- Run `npm run build` in `src/main/frontend-svelte/` to catch TypeScript errors
- Check browser console for API errors
- Verify backend server is running on port 8080

### Common Pitfalls
1. **Non-reactive arrays in Svelte 5**: Use `$state` for arrays that need reactivity; mutating `const` arrays doesn't trigger re-renders.
2. **Missing `_Success` field**: Ensure backend services set `_Success: true/false` in response JSON.
3. **Unique constraints**: Check `@Indexable(unique=true)` annotations on Perst entities; generate unique IDs for indexed fields.
4. **JSON method signatures**: Verify `JSONObject` methods exist (e.g., `getString` vs `optString`).
5. **Session expiration**: UUIDs expire; re-login if API returns `_ErrorCode: 2`.
6. **Hot reload limitations**: Groovy service changes may require backend restart if not auto-reloaded.
7. **Stale DB after schema changes**: Always clear the DB when changing entity structure (new fields, removed fields, etc.)
8. **`getRecords()` vs `select()`**: Always use `select()` for "get all objects of class X". `getRecords()` uses Lucene which is for text search.
9. **Email uniqueness**: All PerstUser usernames (emails) must be globally unique across ALL entity types.

### Mistakes to Avoid (Recent Lessons)
1. **Assuming dropdown issues are only frontend**: Always test backend API independently (curl) before modifying frontend.
2. **Not checking unique constraints**: When creating new records, always ensure indexed fields (like `username`) are unique.
3. **Overlooking response format**: Backend responses must include `_Success` field; frontend expects it for error handling.
4. **Incomplete testing**: Test both success and failure paths; verify data persistence in database.
5. **Assuming hot reload works**: After modifying Groovy services, verify they are actually reloaded (check logs).
6. **Using `getHouseId()` etc.**: These methods don't exist. Use `getHouseOid()`, `getCleanerOid()`, `getBookingOid()`, `getScheduleOid()`.
7. **Not killing server before DB clear**: Open file handles prevent DB deletion. Always `pkill -9 java` first.

## Internal Package Convention

Services in the `internal` package are **internal-only** by default. This provides a security-by-convention approach where internal-only services are isolated by package.

### Rules
1. **Methods in `internal.` package are internal-only** unless annotated with `@EXTERNAL_CALL`
2. **Methods starting with `_`** are internal-only (e.g., `_resetGroovy`)
3. **Methods in other packages** are external by default unless annotated with `@INTERNAL_CALL`

### Example
```groovy
// src/main/backend/internal/services/Reset.groovy
package internal.services

class Reset {
    // This method is NOT exposed (internal package, no annotation)
    void resetGroovy(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GroovyClass.reset()
        outjson.put("_Success", true)
    }
    
    // This method IS exposed (internal package, @EXTERNAL_CALL annotation)
    @koo.security.EXTERNAL_CALL
    void publicReset(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GroovyClass.reset()
        outjson.put("_Success", true)
    }
}
```

### Annotations
- `@INTERNAL_CALL` - Marks a method as internal-only (external packages)
- `@EXTERNAL_CALL` - Marks a method as exposed (internal packages)

## Perst 5.1.0 NonSqlConnection Integration

### How It Works
When Perst-only mode is enabled (no SQL database), the framework automatically passes a `PerstConnection` instance as the `db` parameter to all services.

**Architecture:**
```
KissInit.groovy:
  PerstStorageManager.initialize()
  PerstConnection = new PerstConnection()
  MainServlet.putEnvironment("NonSqlConnection", PerstConnection)

ProcessServlet.java:
  if (!hasSqlDatabase):
    DB = MainServlet.getEnvironment("NonSqlConnection")

Services receive:
  void method(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)
```

### Using the db Parameter
```groovy
void myService(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
    // db is a PerstConnection when Perst-only mode
    if (db instanceof PerstConnection) {
        Collection<Cleaner> cleaners = db.getAll(Cleaner.class)
        Cleaner c = db.getByOid(Cleaner.class, oid)
        def tc = db.perstCreateContainer()
        tc.addInsert(cleaner)
        db.perstStore(tc)
    }
}
```

### Alternative: Using PerstStorageManager
```groovy
import oodb.PerstStorageManager

void myService(...) {
    if (!PerstStorageManager.isAvailable()) {
        outjson.put("error", "Perst not available")
        return
    }
    Collection<Cleaner> cleaners = PerstStorageManager.getAll(Cleaner.class)
    Cleaner c = PerstStorageManager.getByOid(Cleaner.class, oid)
    def tc = PerstStorageManager.createContainer()
    tc.addInsert(cleaner)
    PerstStorageManager.store(tc)
}
```

### Critical Fixes Applied
1. **KissInit.groovy**: Split initialization into two steps to ensure PerstConnection is registered even if Perst was already initialized
2. **PerstConnection.java**: Override commit(), rollback(), close() as no-ops because PerstConnection is reused across requests
3. **Login.groovy**: Accept PerstConnection as parameter type (not Connection) for proper method matching

### PerstService.java (Java)
Java services can use `PerstStorageManager.isAvailable()` and other static methods directly.

### Login JSON Format
Core Login method (empty class name):
```json
{"_class":"","_method":"Login","username":"admin","password":"admin"}
```

User services:
```json
{"_class":"services.LoadTestdata","_method":"load","_uuid":"session-uuid"}
```

## Svelte 5 Standards

### Mandatory: Svelte 5 Only (No Svelte 4)
All components MUST use Svelte 5 syntax. Svelte 4 syntax is deprecated and not allowed.

### Key Differences

| Svelte 4 | Svelte 5 |
|----------|----------|
| `export let prop` | `let { prop } = $props()` |
| `$: reactive = ...` | `$derived(...)` |
| `createEventDispatcher` | Callback props (`onEvent = () => void`) |
| `on:event` | `onevent={handler}` |
| `$$props` | `$props()` |
| `$state(initial)` | `$state(initial)` (same) |
| `$effect` | `$effect` (new) |

### Props Pattern (Svelte 5)
```svelte
<script lang="ts">
  interface Props {
    title?: string;
    count?: number;
    onSave?: (value: string) => void;
  }

  let { title = 'Default', count = 0, onSave }: Props = $props();
</script>
```

### Reactive Pattern (Svelte 5)
```svelte
<script lang="ts">
  let items = $state(['a', 'b', 'c']);
  let filtered = $derived(items.filter(i => i.startsWith('a')));
</script>
```

### Event Handling (Svelte 5)
```svelte
<!-- Svelte 4: on:click={handler} -->
<!-- Svelte 5: -->
<button onclick={handler}>Click</button>
```

### Snippets (Svelte 5)
```svelte
<script>
  let { children }: Props = $props();
</script>

{#if children}
  {@render children()}
{/if}
```

### Navbar Sticky
Add `sticky top-0 z-50` class to the `<header>` element to make it stick to the top on scroll.

### Check for Svelte 4 Patterns
Run this to find Svelte 4 syntax:
```bash
grep -r "export let\|createEventDispatcher\|\$:\|on:" src/main/frontend-svelte/src/lib/components/
```

### CORS Configuration (Fork Override)
The Kiss framework configures CORS via `web.xml` files in `src/main/core/WEB-INF/`:
- `web.xml` - copied during `buildSystem()` 
- `web-secure.xml` - copied during `war()` build
- `web-unsafe.xml` - copied after war build

These are **Kiss framework core files**. When updating from upstream Kiss framework, CORS settings may be overwritten.

**Current fork override:** All web.xml files have `cors.allowed.origins = *` for development.

**To change CORS:**
1. Edit `src/main/core/WEB-INF/web.xml` (development)
2. Edit `src/main/core/WEB-INF/web-secure.xml` (production)
3. Rebuild with `./bld build`

**Alternative:** Implement a custom CORS filter that reads from `application.ini` (future improvement).

## Session and OO Data Access Pattern

### Why Pass Only UUID, Not IDs?

The session already contains all the information needed. Here's how it works:

**Login flow:**
1. User logs in with username/password
2. Login.groovy creates a PerstUser and stores it in UserData: `ud.putUserData("perstUser", perstUser)`
3. UserData is stored in UserCache, keyed by UUID
4. UUID is sent with every request as `_uuid`

**Service call flow:**
1. Request comes with `_uuid` 
2. ProcessServlet finds the UserData via `UserCache.findUser(uuid)`
3. Service gets the actor via: `((PerstUser) servlet.getUserData("perstUser")).getActor()`

**Key insight:** The UUID uniquely identifies a session, which contains ONE user. There's no need to pass `_ownerId`, `_cleanerId`, `_isAdmin` separately - all that info is already in the PerstUser → Actor relationship.

### No-ID Pattern (Pure OO)

**OLD (SQL-style - WRONG):**
```typescript
// Frontend passes IDs
housesAPI.getByOwner(ownerId)

// Backend receives ID and filters by it
if (injson.has("_ownerId")) { filter by ownerId }
```

**NEW (OO-style - CORRECT):**
```typescript
// Frontend just calls getAll(), backend filters by actor relationship
housesAPI.getAll()

// Backend gets actor from session and uses OO navigation
def actor = pu.getActor()  // PerstUser → Actor
return actor.getHouses()   // Actor → Houses (pure OO!)
```

### Implementation

#### Frontend: Remove redundant ID params
In `Server.ts`, remove these from request:
- `_ownerId`
- `_cleanerId`  
- `_isAdmin`

Keep only `_uuid` - the session UUID is sufficient.

#### Backend: Get actor from session
In services (Cleaning.groovy, Users.groovy, etc.):

```groovy
// OLD (SQL-style):
boolean isAdmin = injson.has("_isAdmin") && injson.getBoolean("_isAdmin")
long ownerId = injson.has("_ownerId") ? injson.getLong("_ownerId") : 0

// NEW (OO-style):
PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
def actor = pu.getActor()
boolean isAdmin = actor?.getAgreement()?.getRole() in ["admin", "superAdmin"]
```

#### Data filtering by role

**Admin sees everything:**
```groovy
if (isAdmin) {
    return PerstStorageManager.getAll(House.class)
}
```

**Owner/Cleaner sees their own:**
```groovy
// Pure OO - no ID parameters needed!
return actor.getHouses()
return actor.getBookings()
return actor.getSchedules()
```

### Field Naming Convention

**Always use OID suffix for object references:**
- `ownerOid` - OID of an Owner
- `cleanerOid` - OID of a Cleaner  
- `userOid` - OID of a PerstUser
- `houseOid` - OID of a House

**NEVER use ID suffix** - that suggests SQL auto-increment IDs which don't exist in Perst.

### Why getUserData("perstUser")?

The `servlet.getUserData("perstUser")` pattern exists because:
1. UserData is per-session (one per UUID)
2. It stores the PerstUser object directly (not just an OID)
3. This avoids repeated database lookups

The PerstUser is stored once at login time and reused for the lifetime of the session.

### Future Enhancement

Could add a convenience method to ProcessServlet:
```groovy
// Current:
def actor = ((PerstUser) servlet.getUserData("perstUser")).getActor()

// Future (cleaner):
def actor = servlet.getActor()  // one-liner
```

For now, keep the explicit pattern as it's clear and self-documenting.

### Recording Knowledge
- All lessons learned and discoveries during development should be documented
- Update AGENTS.md with new findings, fixes, and patterns discovered
- Include root cause analysis when fixing bugs

### LoadTestdata Service Conversion
- **Converted from Groovy to Java** (`LoadTestdata.groovy` → `LoadTestdata.java`)
- Added `@EXTERNAL_CALL` annotation for explicit external endpoint declaration
- Uses `IterableIterator` from ooGTxQ for record iteration
- Uses Manager classes (OwnerManager, HouseManager, etc.) for data access
- Requires system admin (SUPER_ADMIN role) to execute
- **Fixed UnifiedDBManager lookup**: Changed from `servlet.getServletContext().getAttribute()` to `MainServlet.getEnvironment()`
- **Fixed Login.java**: Added UUID to response for session management

### Version Control Protocol
- When changes are approved, commit them promptly
- Always push commits to remote repository
- Never leave approved changes uncommitted

### Uncertainty Protocol
- If unsure about something, ask the user for clarification
- Don't make assumptions that could lead to incorrect implementation

### Translation / i18n Protocol
- ALL user-facing text must use translation keys (tt() function in Svelte)
- NEVER hardcode visible text - always use tt('key.path')
- When adding new text/UI, add translations to ALL language files:
  - `src/main/frontend-svelte/src/lib/i18n/messages/en.json`
  - `src/main/frontend-svelte/src/lib/i18n/messages/nl.json`
  - `src/main/frontend-svelte/src/lib/i18n/messages/de.json`
- Common keys should go under "common" section
- Page-specific keys go under their respective section (e.g., "verify", "auth", "owners")

---

## MCP Ollama Bridge

For local AI with file system and web access capabilities:

- **Location:** `/home/dacosta/Projects/mcpOllama/`
- **Start:** `./start.sh` or `source .venv/bin/activate && ollama-mcp-bridge --port 7777`
- **API:** `http://localhost:7777/api/chat` (Ollama-compatible with tools)
- **Available models:** `qwen3.5:4b`, `qwen3.5:2b`

### MCP Tools Enabled
- **filesystem**: Read/write files in `/home/dacosta/Projects`
- **fetch**: Fetch web content
- **memory**: Knowledge graph (9 tools for persistent context)

### Adding More Servers
Edit `mcp-config.json` to add GitHub, Brave Search, PostgreSQL, etc.

See `../mcpOllama/HOWTO.md` for full documentation.