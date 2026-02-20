# Working Rules (must be included)

---
Environment:
- Runs on Linux (*.sh scripts) and in Windows (*.cmd scripts)
- Java: OpenJDK 25
- Maven as a build tool, NO Gradle and NO script based compilation
- Maven should always work with the latest library versions
- Enable -Xlint:all -Werror (or at least during CI) to catch deprecations and preview API shifts.

Standard procedure:
After a change has been made to the core Perst code all the junit tests have to be run prior to stating the task as done.

### NEVER comment out or remove a line because that would make the code work. If you want to do this there has to be approval.
### NEVER remove the md dir

## Core Principles

1. **All MD documentation lives in the `md/` directory**

2. **Every plan MUST be accompanied by:**
   - A **todolist** (`md/todo_*.md`) containing all items/steps to implement the plan
   - A **progress doc** (`md/progress_*.md`) containing all tasks with their current state

3. **Working rules at top of every plan(ning), progress and todolist document** - This ensures they are never forgotten

---

# Plan: Fix Warnings in StorageImpl.java

## Overview

This plan addresses the compiler warnings produced by `StorageImpl.java`. The goal is to eliminate all warnings to achieve a clean compile.

## Warning Inventory

### Summary

| # | Warning Type | Location | Description | Severity |
|---|-------------|----------|-------------|----------|
| 1 | Auxiliary class access | StorageImpl.java:3596 | Auxiliary class `ArrayPos` in Bytes.java should not be accessed from outside its own source file | Medium |

### Detailed Analysis

#### Warning #1: Auxiliary Class Access

**File:** `src/main/java/org/garret/perst/impl/StorageImpl.java`  
**Line:** 3596  
**Warning Message:**
```
auxiliary class org.garret.perst.impl.ArrayPos in /home/dacosta/Projects/oodb-testcoverage/src/main/java/org/garret/perst/impl/Bytes.java should not be accessed from outside its own source file
```

**Root Cause:**
The `ArrayPos` class is defined as a package-private class in `Bytes.java` (lines 5-14). It is being used in `StorageImpl.java` at multiple locations:
- Line ~2131: `ArrayPos pos = new ArrayPos(buf, offs);` in `ByteArrayObjectInputStream.readString()`
- Line ~2142: `ArrayPos pos = new ArrayPos(buf, offs);` in `ByteArrayObjectInputStream.readObject()`
- Line 3596: `final Object unswizzle(ArrayPos obj, ...)` method parameter
- Multiple other usages in `unswizzle()` method and related methods

**Impact:**
- This is a design issue where a helper class is defined in one file but used in another
- While it compiles, it violates Java best practices
- Could cause issues with IDE code navigation and refactoring tools
- **Future Java compatibility risk:** While the Java Language Specification allows multiple top-level classes per file, this pattern is discouraged. Future Java versions could:
  - Deprecate or restrict this pattern further
  - Tools and IDEs may not support auxiliary classes properly
  - The warning indicates Oracle/Maven considers this problematic
- If Java ever enforces stricter single-class-per-file rules, this code would break
- The `ArrayPos` class being package-private means it's part of the internal API contract - if it's used across files, it should have its own file to make this explicit

**Proposed Fix:**
Move the `ArrayPos` class to its own source file `src/main/java/org/garret/perst/impl/ArrayPos.java` and make it package-private (default access).

**Affected Files:**
1. `src/main/java/org/garret/perst/impl/Bytes.java` - Remove `ArrayPos` class definition
2. `src/main/java/org/garret/perst/impl/StorageImpl.java` - Add import for `ArrayPos` (if needed, though package-private access should work automatically)
3. `src/main/java/org/garret/perst/impl/ArrayPos.java` - New file containing the `ArrayPos` class

## Implementation Steps

### Step 1: Create ArrayPos.java
- Create new file `src/main/java/org/garret/perst/impl/ArrayPos.java`
- Move the `ArrayPos` class definition from `Bytes.java` to this new file
- Keep the class package-private (no public modifier)

### Step 2: Update Bytes.java
- Remove the `ArrayPos` class definition from `Bytes.java`
- No import needed as both classes are in the same package

### Step 3: Verify StorageImpl.java
- No changes needed to StorageImpl.java
- Package-private access allows usage within the same package
- Verify compilation succeeds without warnings

### Step 4: Run Tests
- Run `mvn test` to ensure no regressions
- All tests should pass

## Risk Assessment

| Risk Level | Description |
|------------|-------------|
| **Low** | This is a simple refactoring - moving a class to its own file. The functionality remains unchanged. |

## Verification

After implementing the fix:
1. Run `mvn compile` - should show no warnings related to StorageImpl.java
2. Run `mvn test` - all tests should pass
3. Verify the project builds cleanly

## Related Issues

This is part of a larger effort to clean up compiler warnings in the Perst codebase. Other files with similar issues:
- `CodeGeneratorImpl.java` - accesses auxiliary classes from `QueryImpl.java` (Node, BinOpNode)

## Success Criteria

- [ ] `mvn compile` produces no warnings for StorageImpl.java
- [ ] All existing tests pass
- [ ] No functional changes to the codebase