# KISS Framework Enhancement: RequireAuthentication Flag

## Overview
This document describes an enhancement to the KISS framework to enable
authentication without requiring a SQL database, using Perst OODB for
user storage and authentication.

## Problem
When no SQL database is configured (DatabaseType/DatabaseName empty), 
the KISS framework bypasses all authentication - any user can access 
any service without credentials.

This is problematic when using Perst OODB for data storage, where user
authentication should still be enforced.

## Solution
Add a configuration flag `RequireAuthentication` that forces authentication
even when no SQL database is configured. When enabled, the framework 
delegates authentication to the existing Login.groovy service, which 
already supports Perst-based authentication.

## Files Modified

### 1. application.ini
**Location:** `src/main/backend/application.ini`

**Change:** Added new configuration option:
```ini
# Require authentication even when no SQL database is configured
# Set to true when using Perst for user authentication
RequireAuthentication = true
```

---

### 2. GroovyClass.java
**Location:** `src/main/core/org/kissweb/restServer/GroovyClass.java`

**Change:** Fixed null parameter handling in method invocation:

```java
// Before (line ~86):
argTypes[i] = args[i].getClass();

// After:
argTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
```

**Reason:** GroovyClass.invoke() was failing when null was passed as a parameter.
This fix allows passing null values to Groovy methods. This is needed because
when using Perst-only authentication, the SQL DB connection is null but
Login.groovy doesn't use it anyway.

---

### 3. MainServlet.java
**Location:** `src/main/core/org/kissweb/restServer/MainServlet.java`

**Change 1:** Added new method `hasSqlDatabase()`:

```java
/**
 * Checks if a SQL database is actually configured.
 *
 * @return true if SQL database is configured
 */
public static boolean hasSqlDatabase() {
    return hasDatabase;
}
```

**Change 2:** Modified `hasDatabase()` method to include RequireAuthentication flag:

```java
/**
 * Checks if a database is configured or authentication is required.
 * Returns true if SQL database is configured OR RequireAuthentication is set.
 *
 * @return true if database is configured or authentication is required
 */
public static boolean hasDatabase() {
    if (hasDatabase)
        return true;
    String requireAuth = (String) environment.get("RequireAuthentication");
    return "true".equalsIgnoreCase(requireAuth);
}
```

**Change 3:** Added log message at startup:
```java
if (!hasDatabase)
    logger.info("* * * No database configured; bypassing login requirements");
```

---

### 4. ProcessServlet.java
**Location:** `src/main/core/org/kissweb/restServer/ProcessServlet.java`

**Change:** Modified 3 locations to use `hasSqlDatabase()` for DB connections:

1. **newDatabaseConnection()** - Uses `hasSqlDatabase()`:
```java
if (!MainServlet.hasSqlDatabase())
    return;
```

2. **login()** and **checkLogin()** - Added comments explaining null DB is handled:
```java
// DB may be null when using Perst-only auth - GroovyClass now handles null params
```

**Reason:** When RequireAuthentication is set but no SQL DB exists:
- `hasDatabase()` returns true (auth is required)
- `hasSqlDatabase()` returns false (no SQL DB)
- `newDatabaseConnection()` skips SQL connection
- Login.groovy receives null DB but uses Perst instead (which works)

---

## Why This Works

The key insight is that `hasDatabase()` now serves two purposes:
1. **For authentication** - returns true if auth is needed (SQL DB OR RequireAuthentication flag)
2. **For DB connection** - we now use `hasSqlDatabase()` which returns true only if actual SQL DB exists

This allows:
- Perst-only authentication without SQL DB
- Groovy methods can receive null parameters (fixed in GroovyClass.java)
- Login.groovy ignores the null DB and uses PerstUserManager instead

---

## Configuration

### application.ini Options

| Setting | Value | Behavior |
|---------|-------|----------|
| DatabaseType | (empty) | No SQL DB |
| DatabaseName | (empty) | No SQL DB |
| RequireAuthentication | true | Force Perst/SQL auth |
| RequireAuthentication | false | Original bypass mode |
| (not set) | - | Original bypass mode |

### Complete Example
```ini
[main]

# No SQL database
DatabaseType = 
DatabaseName = 

# Authentication settings
MaxWorkerThreads = 30
UserInactiveSeconds = 900
RequireAuthentication = true

# Perst settings
PerstEnabled = true
PerstDatabasePath = ../../../data/oodb
```

## Behavior Matrix

| SQL DB | RequireAuthentication | Result |
|--------|----------------------|--------|
| No | false/not set | No authentication (bypass) |
| No | true | Perst authentication via Login.groovy |
| Yes | false/not set | SQL authentication (standard) |
| Yes | true | SQL authentication (standard) |

## Dependencies

1. **Login.groovy** must exist in backend services and handle Perst authentication:
   - Call `PerstUserManager.authenticate(username, password)`
   - Return UserData with user ID on success
   - Return null on failure

2. **PerstUserManager** must be available with authenticate() method

3. **PerstStorageManager** must be initialized before login (handled by KissInit.groovy)

## Impact Assessment

### Pros
- Minimal changes to core framework (4 files, ~15 lines total)
- Backward compatible - existing SQL setups unaffected
- No changes to session management (_uuid, UserCache)
- Works with existing Perst integration
- Fixes a long-standing limitation in GroovyClass (null parameters)

### Alternative Considered
Could have used reflection to call Java Login directly, but the one-line
GroovyClass fix is much cleaner and benefits all Groovy service calls.

## Testing Recommendations

1. Test with Perst enabled, RequireAuthentication=true
   - Login should work with Perst users
   - Invalid credentials should fail
   
2. Test with Perst enabled, RequireAuthentication=false
   - No authentication required (bypass mode)
   
3. Test with SQL database configured
   - Standard SQL authentication still works
   
4. Test session timeout
   - After 120 seconds, checkLogin should re-validate against Perst

---

*Document generated for KISS framework maintainers to assess impact.*
