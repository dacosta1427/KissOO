# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: Perst Code Quality Improvement

## Tasks

### Priority #1: Fix Primitive Wrapper Constructors (94 instances)
- [x] Task 1.1: Fix Aggregator.java - Replace new Integer/Long/Double() with valueOf()
- [x] Task 1.2: Fix RndBtree.java - Replace new Integer/Long/Double/Byte/Short() with valueOf()
- [x] Task 1.3: Fix QueryImpl.java - Replace new Integer/Long/Double() with valueOf()
- [x] Task 1.4: Fix StorageImpl.java - Replace new Integer/Long() with valueOf()
- [x] Task 1.5: Fix AltBtree.java - Replace new Integer/Long/Double/Byte/Short() with valueOf()
- [x] Task 1.6: Fix Btree.java - Replace new Integer/Long/Double/Byte/Short() with valueOf()
- [x] Task 1.7: Fix BtreeCompoundIndex.java - Replace new Integer/Long/Double/Byte/Short() with valueOf()
- [x] Task 1.8: Fix BtreeMultiFieldIndex.java - Replace new Integer/Long/Double/Byte/Short() with valueOf()
- [x] Task 1.9: Fix AltBtreeCompoundIndex.java - Replace new Integer/Byte() with valueOf()

### Priority #2: Fix finalize() and runFinalization()
- [x] Task 2.1: Persistent.java - Already has @Deprecated(forRemoval=true)
- [x] Task 2.2: WeakHashTable.java - Already has @SuppressWarnings
- [x] Task 2.3: LruObjectCache.java - Already has @SuppressWarnings

### Priority #3: Add @Deprecated Annotations
- [ ] Task 3.1: Database.java - Add @Deprecated to 4 deprecated methods

### Priority #4: Enable Strict Compilation
- [ ] Task 4.1: Enable -Xlint:all in pom.xml
- [ ] Task 4.2: Run tests and fix any issues

### Priority #5: Final Verification
- [ ] Task 5.1: Run all 288 JUnit tests
- [ ] Task 5.2: Verify zero deprecation warnings (or minimal)

## Success Criteria
- [ ] All 288 tests pass
- [ ] Zero deprecation warnings from primitive wrappers
- [ ] All deprecated methods properly annotated
- [ ] Clean build with -Xlint:all

## Rollback Plan
1. Git revert individual files if issues arise
2. Run `mvn test` to verify
3. Revert pom.xml if -Xlint causes failures
