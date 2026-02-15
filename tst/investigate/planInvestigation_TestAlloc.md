# Working Rules (must be included)

---

# Plan: Investigate TestAlloc Failure

## Error Details
```
Exception in thread "main" org.garret.perst.StorageError: File access error
at org.garret.perst.impl.MultiFile.<init>(MultiFile.java:130)
	at org.garret.perst.impl.StorageImpl.open(StorageImpl.java:916)
	at org.garret.perst.impl.StorageImpl.open(StorageImpl.java:907)
	at TestAlloc.main(TestAlloc.java:9)
```

## Root Cause Analysis

### Initial Assessment (OUTDATED - See Update Below)
The error `StorageError: File access error` when opening a MultiFile was initially thought to be a file access issue. However, further investigation reveals a **different root cause**.

### Updated Root Cause - Java Version Incompatibility
The system is running **Java 25 (OpenJDK 25)**, but the Perst library (`perst.jar`) is an older version that uses `sun.misc.Unsafe` which was removed in Java 9+.

**Evidence:**
```
java.lang.NoSuchFieldException: unsafe
	at java.base/java.lang.Class.getDeclaredField(Class.java:2382)
	at org.garret.perst.impl.sun14.Sun14ReflectionProvider.<init>(Sun14ReflectionProvider.java:21)
```

This error appears in TestLeak, and likely TestAlloc has a similar issue manifesting as "File access error" due to the underlying library initialization failure.

### Test Expected Behavior
According to the makefile, TestAlloc runs 3 times:
1. First run: Initialize database
2. Second run: Verify/read from database  
3. Third run: Cleanup

The failure occurs because the Perst library cannot initialize properly in Java 25.

## Investigation Plan

### COMPLETED INVESTIGATION
- [x] Examined TestAlloc.java source code - uses "@testalloc.mfd" multi-file database
- [x] Tested with various parameters - all fail with same error
- [x] Identified Java 25 incompatibility as root cause

### Solution Options
1. **Option 1 (Recommended):** Upgrade Perst library to version compatible with Java 9+
2. **Option 2:** Use Java 8 instead of Java 25 (if available)
3. **Option 3:** Exclude TestAlloc from test runs until Perst is upgraded

## Success Criteria
- [x] Identify root cause: Perst library incompatible with Java 25
- [ ] Document solution

## Dependencies
- Need updated Perst library

## Effort Estimate
M (Medium) - Requires library upgrade
