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
- JAR: `ooGTxQ-1.0.0.jar` in `libs/`
- Source: `../oodbGTQ/` project
- Update: Run `./bld perst-update` to fetch latest from Nassyn Maven repository
- The `perstUpdate()` method in `Tasks.java` handles Maven download from `https://repo.nasyn.io/repository/maven-public/`

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

### PerstStorageManager Usage

#### Retrieval: Use `select()` NOT `getRecords()`
```groovy
// CORRECT: select() iterates the class extent directly
Collection<Owner> owners = PerstStorageManager.getAll(Owner.class)

// WRONG: getRecords() uses Lucene full-text search (wrong tool for simple retrieval)
```

**Why:** `getRecords()` uses Lucene indexing which is designed for text search, not object retrieval. The `select()` method iterates the class extent directly via `ExtentIterator`.

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

### Classloader Mismatch Fix
Groovy services are loaded by a different classloader than precompiled classes. When `getTable(GroovyClass)` is called, the Class object differs from the precompiled one in `typeMap`.

**Fix applied to ooGTxQ:**
- `lookupTable(Class type)` — falls back to class name string matching when `typeMap.get(type)` returns null
- `getTable(Class type)` — same fallback to prevent duplicate table descriptors
- Both cache the new Class reference in `typeMap` for future fast lookups

See `../oodbGTQ/changeRequests/` for details.

### Known ooGTxQ Limitations
- `linQueue` (async Lucene indexing) is never initialized by `CDatabase.open()` — use sync containers
- `getTableDescriptors()` returns package-private `TableDescriptor` objects — requires reflection to inspect
- The `initLinQueue()` method exists but is never called

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