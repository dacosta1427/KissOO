# Working Rules (must be included)

---

# Todolist: Clean Compiler Warnings

## Tasks - ALL COMPLETED

### Priority #1: Fix Deprecated API Usage (Quick Wins) - DONE
- [x] Task 1.1: Fix AssocDB.java - Replace Integer(int) with Integer.valueOf()
- [x] Task 1.2: Fix Item.java - Replace Double(double) with Double.valueOf()
- [x] Task 1.3: Fix ReadWriteTransaction.java - Replace Integer(int) with Integer.valueOf()
- [x] Task 1.4: Fix TestBackup.java - Replace Long(long) with Long.valueOf()

### Priority #2: Fix Main Source Files - Core Database Classes - DONE
- [x] Task 2.1: Fix StorageImpl.java - Add proper type parameters
- [x] Task 2.2: Fix AltBtree.java - Add type parameters to Link, ArrayList
- [x] Task 2.3: Fix AltBtreeFieldIndex.java - Fix unchecked casts
- [x] Task 2.4: Fix AltBtreeMultiFieldIndex.java - Fix unchecked casts and toArray
- [x] Task 2.5: Fix AltBtreeCompoundIndex.java - Fix unchecked Comparable.compareTo
- [x] Task 2.6: Fix AltPersistentSet.java - Fix unchecked array casts

### Priority #3: Fix Main Source Files - Utility Classes - DONE
- [x] Task 3.1: Fix Aggregator.java - Add type parameters
- [x] Task 3.2: Fix Projection.java - Fix unchecked From/To casts
- [x] Task 3.3: Fix SmallMap.java - Fix unchecked array conversions
- [x] Task 3.4: Fix L2List.java - Fix unchecked Query.select call
- [x] Task 3.5: Fix Version.java - Fix unchecked List.add, raw type assignment
- [x] Task 3.6: Fix VersionHistory.java - Fix unchecked casts

### Priority #4: Fix Main Source Files - Association & Full-Text - DONE
- [x] Task 4.1: Fix ReadOnlyTransaction.java - Fix unchecked casts and raw type
- [x] Task 4.2: Fix ReadWriteTransaction.java - Fix remaining unchecked casts
- [x] Task 4.3: Fix FullTextSearchHelper.java - Fix unchecked collections
- [x] Task 4.4: Fix FullTextSearchResult.java - Fix unchecked Arrays.sort

### Priority #5: Fix Test Source Files - DONE
- [x] Task 5.1: Fix DatabaseTest.java - Add @SuppressWarnings or update deprecated calls
- [x] Task 5.2: Fix PersistentSetTest.java - Fix unchecked Set.add calls
- [x] Task 5.3: Fix QueryTest.java - Fix unchecked Vector.add, Query.select calls
- [x] Task 5.4: Fix StorageTest.java - Fix unchecked Set.add, Query.select calls
- [x] Task 5.5: Fix TestAlloc.java - Fix unchecked Index.put calls
- [x] Task 5.6: Fix TestBackup.java - Fix unchecked Index.put calls
- [x] Task 5.7: Fix TestFullTextIndex.java - Fix unchecked FieldIndex.put calls
- [x] Task 5.8: Fix TestRaw.java - Fix deprecated API usage

### Priority #6: Verification - DONE
- [x] Task 6.1: Run mvn clean compile and verify zero warnings
- [x] Task 6.2: Run full test suite and verify all tests pass

## Success Criteria - ACHIEVED
- [x] Build completes with zero compiler warnings
- [x] All JUnit tests pass (355 tests, 0 failures)
- [x] No functionality regressions

## Rollback Plan
Not needed - task completed successfully.
