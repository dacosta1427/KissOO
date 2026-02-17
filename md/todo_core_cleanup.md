# Working Rules (must be included)

---

# Todolist: Perst Core Code Cleanup

## Tasks

### Priority #1: High Impact Files
- [ ] Task 1.1: Fix Persistent.java - Remove or annotate deprecated finalize() method
- [ ] Task 1.2: Fix Database.java - Add proper type parameters to resolve 28 unchecked warnings
- [ ] Task 1.3: Fix StorageImpl.java - Fix unchecked return type conversions

### Priority #2: Heavy Warning Files
- [ ] Task 2.1: Fix AltBtree.java - Address ~58 unchecked calls to Link.setObject() and ArrayList operations
- [ ] Task 2.2: Fix Aggregator.java - Fix 12 unchecked generic method calls
- [ ] Task 2.3: Fix AltBtreeFieldIndex.java - Fix 6 unchecked array casts
- [ ] Task 2.4: Fix AltBtreeMultiFieldIndex.java - Fix 7 unchecked calls and casts

### Priority #3: Moderate Warning Files
- [ ] Task 3.1: Fix SmallMap.java - Fix 6 unchecked conversions/casts for Pair arrays
- [ ] Task 3.2: Fix Projection.java - Fix 4 unchecked cast operations
- [ ] Task 3.3: Fix FullTextSearchHelper.java - Fix 3 unchecked ArrayList operations
- [ ] Task 3.4: Fix FullTextSearchResult.java - Fix 2 unchecked Comparator issues

### Priority #4: Minor Warning Files
- [ ] Task 4.1: Fix Version.java - Fix 2 unchecked List operations
- [ ] Task 4.2: Fix VersionHistory.java - Fix 1 unchecked cast
- [ ] Task 4.3: Fix L2List.java - Fix 1 unchecked Query.select() call
- [ ] Task 4.4: Fix AltBtreeCompoundIndex.java - Fix 1 unchecked compareTo call
- [ ] Task 4.5: Fix AltPersistentSet.java - Fix 3 unchecked array casts

## Success Criteria
- [ ] All 16 files compile without warnings
- [ ] All existing tests pass
- [ ] Build completes with "BUILD SUCCESS" and no warnings

## Rollback Plan
1. Git revert to previous state
2. Run `mvn clean compile` to verify
3. Run tests to verify no regressions
