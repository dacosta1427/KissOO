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

### Lombok Usage
All domain classes in `src/main/precompiled/mycompany/domain/` use Lombok `@Getter @Setter` annotations.
- Getters/setters are auto-generated — do NOT write manual getters/setters for fields
- Use `@Getter` only for classes with controlled-mutation fields (Agreement, Group, BenchmarkData, Phone)
- Business methods (e.g., `checkPassword()`, `canLogin()`, `generateVerificationToken()`) are written manually
- Lombok is included via `lombok.jar` in `libs/` and is processed at compile time

## Pure OO Navigation (No ID Fields)
KissOO uses a pure object-oriented approach with Perst OODBMS:
- **No ID/foreign key fields** — objects reference each other directly
- **Actor → PerstUser**: `actor.getPerstUser()` (transient, cached)
- **PerstUser → Actor**: `perstUser.getActor()` (persistent reference)
- **Login flow**: Find PerstUser by username → `pu.getActor()` → cast to Owner/Cleaner
- **Session**: PerstUser is stored directly in `UserData` via `ud.putUserData("perstUser", perstUser)`
- **Navigation example**: `((PerstUser) ud.getUserData("perstUser")).getActor()`

### Actor/PerstUser Relationship
- Every Actor (Owner, Cleaner) creates its own deactivated PerstUser in the constructor
- The PerstUser has a persistent `actor` reference back to its Actor
- To create an Owner/Cleaner: just `new Owner(...)` — PerstUser is auto-created
- Store both together: `tc.addInsert(owner); tc.addInsert(owner.getPerstUser()); PerstStorageManager.store(tc)`

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

### Mistakes to Avoid (Recent Lessons)
1. **Assuming dropdown issues are only frontend**: Always test backend API independently (curl) before modifying frontend.
2. **Not checking unique constraints**: When creating new records, always ensure indexed fields (like `userId`) are unique.
3. **Overlooking response format**: Backend responses must include `_Success` field; frontend expects it for error handling.
4. **Incomplete testing**: Test both success and failure paths; verify data persistence in database.
5. **Assuming hot reload works**: After modifying Groovy services, verify they are actually reloaded (check logs).

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