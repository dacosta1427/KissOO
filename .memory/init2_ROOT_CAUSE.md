# init2() Not Executing - Root Cause Analysis

## Summary

The `KissInit.init2()` method is never executed due to a **classloader mismatch** between the Groovy service framework and the PerstConnection class.

## The Problem

### Step 1: Method Invocation

```java
// MainServlet.java line 237
(new GroovyService()).internalGroovy(null, "KissInit", "init2", (Connection) db);
```

### Step 2: Argument Type Resolution

```java
// GroovyService.java line 277
ca[i] = args[i].getClass();  // Returns: class koo.core.PerstConnection
```

### Step 3: Method Lookup

```java
// GroovyService.java line 281
Method methp = ci.gclass.getMethod(_method, ca);
// Looking for: init2(koo.core.PerstConnection)
// But KissInit.groovy has: init2(org.kissweb.database.Connection)
```

### Step 4: Silent Failure

```java
// GroovyService.java line 296-297
catch (Exception e) {
    return ProcessServlet.ExecutionReturn.Error;  // No logging!
}
```

## Why It Fails

1. **PerstConnection loaded by webapp classloader** (`koo.core.PerstConnection`)
2. **Connection (KISS framework) loaded by system classloader** (`org.kissweb.database.Connection`)
3. **Method lookup uses runtime class** - finds `init2(PerstConnection)` but Groovy compiled with `init2(Connection)`
4. **Even though PerstConnection extends Connection**, the method signatures don't match exactly due to classloader differences

## Log Evidence

```
2026-04-17 19:18:46 [INFO] DEBUG init2: Calling init2 with db=koo.core.PerstConnection@236b3b99
# NOTE: "[KissInit] init2() CALLED" NEVER appears - method not executed
```

## Solutions Considered

### Option 1: Fix GroovyService (Recommended)
Modify `internalGroovy()` to use the declared parameter types, not runtime types:

```java
// Instead of:
ca[i] = args[i].getClass();

// Use explicit type when Connection is detected:
if (args[i] instanceof org.kissweb.database.Connection) {
    ca[i] = org.kissweb.database.Connection.class;
}
```

### Option 2: Fix MainServlet Call
Pass the Connection class explicitly:

```java
(new GroovyService()).internalGroovy(null, "KissInit", "init2", 
    org.kissweb.database.Connection.class, db);
```

### Option 3: Add Debug Logging
Add logging to GroovyService method lookup to catch these failures:

```java
catch (Exception e) {
    logger.error("Method " + _method + " lookup failed: " + e.getMessage());
    return ProcessServlet.ExecutionReturn.Error;
}
```

## Current Workaround

The project currently works around this by:
1. Putting `PerstConnection` in MainServlet environment in `init()`
2. Services access it via `servlet.getUserData()` or environment
3. Skipping `init2()` user initialization

## Files Involved

| File | Issue |
|------|-------|
| `GroovyService.java:277` | Uses `getClass()` instead of declared type |
| `GroovyService.java:281` | Method lookup fails silently |
| `MainServlet.java:237` | Passes PerstConnection without Connection class hint |
| `KissInit.groovy:129` | init2(Connection) never gets called |

## References

- Java ClassLoader documentation: Class identity requires same classloader
- Groovy method invocation relies on exact type matching
- Kiss framework uses this pattern for all static method calls