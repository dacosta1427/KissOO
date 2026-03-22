# Bug Report: allowWithoutAuthentication / shouldAllowWithoutAuthentication Inconsistency

## Date
2026-03-22

## Severity
Medium

## Component
`src/main/core/org/kissweb/restServer/MainServlet.java`

## Summary
`shouldAllowWithoutAuthentication()` does not transform dots to slashes like `allowWithoutAuthentication()`, causing authentication bypass to fail for service methods.

## Description

The `allowWithoutAuthentication()` method transforms class names by replacing dots with slashes before storing:

```java
public static void allowWithoutAuthentication(String className, String methodName) {
    className = className.replaceAll("\\.", "/");  // DOT → SLASH
    allowedWithoutAuthentication.add(className + ":" + methodName);
}
```

However, `shouldAllowWithoutAuthentication()` does NOT perform this transformation:

```java
static boolean shouldAllowWithoutAuthentication(String className, String methodName) {
    return allowedWithoutAuthentication.contains(className + ":" + methodName);  // NO TRANSFORM
}
```

This causes a mismatch:
- `KissInit.groovy` calls: `MainServlet.allowWithoutAuthentication("services.Users", "addRecord")`
- This stores: `services/Users:addRecord` (slash)
- `ProcessServlet` calls: `shouldAllowWithoutAuthentication("services.Users", "addRecord")`
- This checks: `services.Users:addRecord` (dot) - **NO MATCH**

## Impact

- Authentication bypass via `allowWithoutAuthentication()` fails for non-empty class names containing dots
- Methods like `services.Users.addRecord` cannot be called without authentication even when explicitly allowed
- Only affects Perst/NonSQL-only mode (where `hasDatabase = false` but `requiresAuthentication = true`)

## Root Cause

Added by Blake McBride on Feb 9, 2022 (commit e8eceb04). The transformation was applied to the setter but not the getter.

## Fix

Apply the same dot-to-slash transformation to `shouldAllowWithoutAuthentication()`:

```java
static boolean shouldAllowWithoutAuthentication(String className, String methodName) {
    className = className.replaceAll("\\.", "/");  // ADD THIS LINE
    return allowedWithoutAuthentication.contains(className + ":" + methodName);
}
```

## Files Affected

- `src/main/core/org/kissweb/restServer/MainServlet.java` (lines 654-656)

## Test Case

1. Start backend with Perst-only mode (no SQL database)
2. Call `MainServlet.allowWithoutAuthentication("services.Users", "addRecord")` in KissInit.groovy
3. Attempt to call `services.Users.addRecord` without authentication
4. **Before fix:** Returns "You have been logged out due to inactivity"
5. **After fix:** Method executes successfully

## Status

Pending fix verification.
