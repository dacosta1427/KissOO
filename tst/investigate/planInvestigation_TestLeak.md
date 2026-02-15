# Working Rules (must be included)

---

# Plan: Investigate TestLeak Failure

## Status
This investigation is related to TestAlloc - both fail due to Java 25 incompatibility with Perst library.

## Error Details
```
java.lang.NoSuchFieldException: unsafe
	at java.base/java.lang.Class.getDeclaredField(Class.java:2382)
	at org.garret.perst.impl.sun14.Sun14ReflectionProvider.<init>(Sun14ReflectionProvider.java:21)
java.lang.Error: Failed to initialize reflection provider
```

## Root Cause
**Java 25 Incompatibility**: The Perst library uses `sun.misc.Unsafe` which was removed in Java 9+.

## Solution
See planInvestigation_TestAlloc.md for details - need to upgrade Perst library or use Java 8.

## Success Criteria
- [x] Identify root cause: Java 25 incompatibility with Perst library
- [x] Document solution: Requires Perst library upgrade

## Dependencies
- Perst library upgrade needed

## Effort Estimate
M (Medium) - Requires library upgrade
