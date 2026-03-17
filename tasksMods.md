# Tasks.java Modifications

## Overview

This document describes the changes made to `Tasks.java` to fix build issues in the KissOO project.

## Change 1: Build Order (Lines 238-240)

### The Problem

The original build order in `buildSystem()` was:
```java
buildJava("src/main/core", ...);
buildJava("src/test/core", ...);    // Test compiled BEFORE precompiled
buildJava("src/main/precompiled", ...);  // Precompiled compiled LAST
```

This caused test compilation to fail because:
- Tests in `src/test/core/oodb/` reference classes from `mycompany.domain` (e.g., `Actor`, `PerstUser`)
- These domain classes are in `src/main/precompiled/mycompany/domain/`
- Since precompiled was built AFTER tests, the domain classes weren't available

### The Error
```
src/test/core/oodb/CDatabaseVersioningTest.java:6: error: package mycompany.domain does not exist
import mycompany.domain.Actor;
```

### The Fix

Changed the order to compile `precompiled` BEFORE `test`:
```java
buildJava("src/main/core", ...);
buildJava("src/main/precompiled", ...);  // Precompiled FIRST
buildJava("src/test/core", ...);         // Test AFTER precompiled
```

This ensures domain classes are available when tests are compiled.

## Why This Matters

The test code (`CDatabaseVersioningTest.java`) imports and uses domain classes:
- `mycompany.domain.Actor`
- `mycompany.domain.Agreement`

These classes are in the `precompiled` source tree, not `core`. Without this fix, tests cannot reference domain objects.

## Related Issues Fixed

1. **Missing `ActorManager.getByUserId()` method** - Added to `ActorManager.java` because `EndpointMethod.java` referenced a non-existent method

2. **CDatabaseVersioningTest API usage** - Fixed to use correct Perst API:
   - `VersionSelector.CURRENT` (static field) instead of `VersionSelector.Kind.CURRENT`
   - `getTransactionId()` instead of non-existent `getVersion()`
   - Removed reference to non-existent `setDescription()` method

## Notes

- This change is minimal and surgical - only the order of two lines was swapped
- The original order may have worked before when all domain classes were in `core` 
- The KissOO project now has domain classes in `precompiled`, requiring this order change
