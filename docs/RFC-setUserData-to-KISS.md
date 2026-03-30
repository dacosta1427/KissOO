# RFC: Add setUserData() Method to UserData Class

**From:** KissOO (Perst-enabled fork)  
**To:** KISS Framework Maintainers  
**Date:** 2026-03-30  
**Subject:** Request to add setUserData() method to UserData class

---

## Summary

Request to add a `setUserData(String key, Object value)` method to `org.kissweb.restServer.UserData` class to enable applications to store custom session data.

---

## Problem

When implementing role-based access control in KissOO, we needed to store custom session data (isAdmin, ownerId, cleanerId) per user session. The existing `UserData` class only provides:

- `getUserData(String key)` - getter
- `data` - private Hashtable field

Without a setter, there's no way for application code to store custom session variables. The workaround would require either:
1. Subclassing UserData (not ideal - it's a framework class)
2. Using reflection to access private field (bad practice)
3. Creating parallel session storage (duplication)

---

## Proposed Solution

Add a simple setter method to UserData.java:

```java
/**
 * Set user data by key.
 *
 * @param key the key
 * @param value the value
 */
public void setUserData(String key, Object value) {
    data.put(key, value);
}
```

This is a minimal, non-breaking addition that:
- Uses existing private infrastructure
- Follows existing getter pattern
- Enables applications to extend session management
- Has zero impact on existing functionality

---

## Use Case Example (KissOO)

```java
// In Login.groovy - store role info in session
servlet.getUserData().setUserData("isAdmin", user.isAdmin());
servlet.getUserData().setUserData("ownerId", owner.getOid());
servlet.getUserData().setUserData("cleanerId", cleaner.getOid());

// Later in services - check permissions
boolean isAdmin = (boolean) servlet.getUserData().getUserData("isAdmin");
```

---

## Recommendation

Accept this addition to the framework. It's a small, practical improvement that benefits any application needing custom session data without breaking existing functionality.

---

**KissOO Change Note:** This method was added to `src/main/core/org/kissweb/restServer/UserData.java` in commit `5251b0ff` on 2026-03-30.
