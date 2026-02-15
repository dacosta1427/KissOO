# Working Rules (must be included)

---

# Progress: Investigate TestAlloc

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** Completed
- **Overall Progress:** 100% complete

## Task Progress

### Task 1.1: Examine TestAlloc.java source code
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Developer
- **Success criteria:** Understand what TestAlloc does and what files it expects
- **Timestamp:** 2026-02-14
- **Effort estimate:** S
- **Notes:** Test stores .java files as Blobs in multi-file database

### Task 1.2: Check what arguments TestAlloc accepts
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Developer
- **Success criteria:** Know all command-line arguments
- **Timestamp:** 2026-02-14
- **Effort estimate:** S
- **Notes:** No arguments needed - runs in sequence (init, verify, cleanup)

### Task 1.3: Run makefile test sequence
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** Developer
- **Success criteria:** See if TestAlloc works when run as makefile specifies
- **Timestamp:** 2026-02-14
- **Effort estimate:** S
- **Notes:** Run 3 times: init, verify, cleanup

### Task 1.4: Identify root cause
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Developer
- **Success criteria:** Document exact cause of file access error
- **Timestamp:** 2026-02-14
- **Effort estimate:** S
- **Notes:** Two issues: 1) Missing testalloc.mfd file, 2) Interface handling in Java 25

### Task 1.5: Implement fix
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.4
- **Owner:** Developer
- **Success criteria:** TestAlloc passes with proper fix
- **Timestamp:** 2026-02-14
- **Effort estimate:** M
- **Notes:** Created testalloc.mfd with correct multi-file format, fixed Perst library

## Root Cause Analysis

### Issue 1: Missing testalloc.mfd database file
- **Problem:** testalloc.mfd file was missing from tst directory
- **Solution:** Created file with proper format: `"testalloc1.dbs" 10000`

### Issue 2: Interface constructor lookup fails in Java 25
- **Problem:** When calling `registerCustomAllocator(Blob.class, ...)`, Perst internally creates a ClassDescriptor for Blob. In the ClassDescriptor constructor, it calls `locateConstructor()` which tries to find a constructor for the class. For interfaces (like Blob), this fails because:
  1. Interfaces don't have constructors
  2. `cls.getDeclaredConstructor(null)` throws NoSuchMethodException
  3. This is now properly thrown as StorageError in Java 25

- **Root Cause Location:** `ClassDescriptor.locateConstructor()` method in src/org/garret/perst/impl/ClassDescriptor.java

- **Solution:** Added interface check at the beginning of locateConstructor():
  ```java
  private void locateConstructor() { 
      // Interfaces cannot be instantiated, so skip constructor lookup
      if (cls.isInterface()) {
          return;
      }
      // ... rest of method
  }
  ```

- **Why this fix is correct:** 
  - Custom allocators only make sense for concrete classes that can be instantiated
  - Interfaces cannot be instantiated directly, so there's no need to find a constructor
  - This is a proper fix in the Perst library, not a workaround in the test

## Test Results

- **TestAlloc Run 1:** "Database is initialized" ✓
- **TestAlloc Run 2:** "Verification completed" + "Cleanup completed" ✓
- **JUnit Tests:** All 77 tests pass ✓

## Files Modified

1. `src/org/garret/perst/impl/ClassDescriptor.java` - Added interface check in locateConstructor()
2. `tst/testalloc.mfd` - Created multi-file database descriptor

## Risk Assessment
- **High Risk:** None
- **Medium Risk:** None - fix is in core library, properly handles edge case
- **Low Risk:** Minimal change, only adds early return for interfaces

## Current Blockers
- None

## Next Steps
1. Test is fully functional with proper fix
2. Fix should be submitted to Perst project maintainers
