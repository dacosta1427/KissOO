# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: Fix All Compiler Warnings

## Tasks

### Priority #1: Simple Individual Issues

- [ ] Task 1.1: Add serialVersionUID to AssertionFailed.java
- [ ] Task 1.2: Fix ITable.java - add Class<?> type parameter

### Priority #2: Aggregator.java (65 warnings)

- [ ] Task 2.1: Analyze Aggregator.java structure
- [ ] Task 2.2: Fix Comparable raw types (lines 163-271)
- [ ] Task 2.3: Fix Aggregate raw types (lines 87-687)
- [ ] Task 2.4: Fix Set/HashSet raw types (lines 454-465)

### Priority #3: Storage.java (27 warnings)

- [ ] Task 3.1: Apply @SuppressWarnings("rawtypes") to Storage.java

### Priority #4: Auxiliary Class Access (6 warnings)

- [ ] Task 4.1: Fix QueryImpl.java auxiliary class access (line 634)
- [ ] Task 4.2: Fix Bytes.java auxiliary class access (line 3596)

### Priority #5: Verification

- [ ] Task 5.1: Run mvn clean compile - verify 0 warnings
- [ ] Task 5.2: Run mvn test - verify all tests pass

## Success Criteria
- [ ] 0 compiler warnings with mvn clean compile
- [ ] All tests pass with mvn test

## Rollback Plan
1. Git revert to previous state
2. Run mvn clean compile to verify
3. Run tests to ensure no regressions
