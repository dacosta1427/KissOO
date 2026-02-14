# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: Perst Deprecated API Fixes

## Tasks

### Priority #1: Class.newInstance() Replacement

- [ ] Task 1.1: Create utility class for object instantiation in impl/ReflectionHelper.java
- [ ] Task 1.2: Fix ClassDescriptor.java - newInstance() method (lines 164, 169, 318)
- [ ] Task 1.3: Fix StorageImpl.java - newInstance() calls (lines 3433, 3456, 3788, 4012, 4264)
- [ ] Task 1.4: Fix AltBtreeMultiFieldIndex.java - Array.newInstance() calls
- [ ] Task 1.5: Fix AltBtreeFieldIndex.java - Array.newInstance() calls
- [ ] Task 1.6: Fix Btree.java - Array.newInstance() calls
- [ ] Task 1.7: Fix BtreeFieldIndex.java - Array.newInstance() calls
- [ ] Task 1.8: Fix BtreeMultiFieldIndex.java - Array.newInstance() calls
- [ ] Task 1.9: Fix RndBtree.java - Array.newInstance() calls
- [ ] Task 1.10: Fix RndBtreeFieldIndex.java - Array.newInstance() calls
- [ ] Task 1.11: Fix RndBtreeMultiFieldIndex.java - Array.newInstance() calls
- [ ] Task 1.12: Fix ReflectionMultidimensionalComparator.java - newInstance() call
- [ ] Task 1.13: Fix TimeSeriesImpl.java - newInstance() call
- [ ] Task 1.14: Fix other impl files with Array.newInstance()

### Priority #2: finalize() Replacement

- [ ] Task 2.1: Add java.lang.ref.Cleaner import to Persistent.java
- [ ] Task 2.2: Create Cleaner-based cleanup mechanism in Persistent.java
- [ ] Task 2.3: Remove deprecated finalize() method
- [ ] Task 2.4: Update subclasses if needed for cleanup registration

### Priority #3: runFinalization() Replacement

- [ ] Task 3.1: Fix WeakHashTable.java - remove runFinalization() calls (lines 85, 117, 139)
- [ ] Task 3.2: Fix LruObjectCache.java - remove runFinalization() calls (lines 127, 169, 218)
- [ ] Task 3.3: Verify cleanup is handled by reference management instead

### Priority #4: Verification

- [ ] Task 4.1: Run mvn compile and verify no deprecation warnings
- [ ] Task 4.2: Run all JUnit tests (139 tests)
- [ ] Task 4.3: Run non-JUnit tests to verify functionality
- [ ] Task 4.4: Document any remaining issues

## Success Criteria

- [ ] All 139 JUnit tests pass
- [ ] No deprecation warnings in compilation output
- [ ] No use of removed Java APIs (Class.newInstance, finalize, runFinalization)
- [ ] Code compiles cleanly with Java 25

## Rollback Plan

1. **Checkpoint:** Git commit before starting implementation
2. **Revert command:** `git revert HEAD` or `git reset --hard HEAD~1`
3. **Verification:** Run `mvn test` - expect 139 tests to pass
4. **Impact:** No impact on functionality, only deprecated API replacement
