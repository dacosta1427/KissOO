# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: Increase Test Coverage

## Tasks

### Priority #1: Analyze Current Coverage
- [x] Task 1.1: Run initial coverage analysis (39% baseline)
- [ ] Task 1.2: Identify classes with 0% coverage
- [ ] Task 1.3: Document uncovered packages and classes

### Priority #2: Convert Remaining Tests
- [ ] Task 2.1: Convert TestReplic to JUnit (replication)
- [ ] Task 2.2: Convert TestReplic2 to JUnit (replication)
- [ ] Task 2.3: Review remaining demo apps for conversion

### Priority #3: Add Targeted Tests for org.garret.perst
- [ ] Task 3.1: Add Storage implementation tests
- [ ] Task 3.2: Add Query processing tests
- [ ] Task 3.3: Add Transaction handling tests
- [ ] Task 3.4: Add Lock management tests

### Priority #4: Add Targeted Tests for org.garret.perst.impl
- [ ] Task 4.1: Add B-tree implementation tests
- [ ] Task 4.2: Add Page management tests
- [ ] Task 4.3: Add Cache implementations tests
- [ ] Task 4.4: Add File I/O tests

### Priority #5: Edge Case Coverage
- [ ] Task 5.1: Add error condition tests
- [ ] Task 5.2: Add boundary case tests
- [ ] Task 5.3: Add exception handling tests

## Success Criteria
- [ ] Increase instruction coverage from 39% to 70%+
- [ ] All existing tests continue to pass
- [ ] No regressions introduced
- [ ] Document coverage by package

## Rollback Plan
1. Run `git revert HEAD` to undo last change
2. Run `mvn test` to verify tests still pass
3. Check coverage is back to baseline
