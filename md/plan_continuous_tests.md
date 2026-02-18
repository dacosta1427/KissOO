# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Convert Continuous Examples to JUnit 5 Tests

## Objective
Convert the two example programs in `continuous/tst/` to JUnit 5 tests to verify the continuous module functionality.

## Background
The `continuous/tst/` directory contains two example applications:
1. **SimpleRelation.java** - Interactive Company-Employee database with full-text search
2. **Bank.java** - Concurrent banking simulation testing multi-threaded transactions

Both are currently interactive command-line applications that need to be converted to automated test cases.

## Analysis

### SimpleRelation.java
- Models: Address, Company (extends CVersion), Employee (extends CVersion)
- Features tested:
  - Insert/select of versioned objects
  - Full-text search with Lucene
  - Version history access
  - JSQL queries
  - Snapshot/time-slice queries

### Bank.java
- Models: Account (extends CVersion), Transfer (extends CVersion)
- Features tested:
  - Concurrent transactions (multi-threaded)
  - Balance consistency
  - Conflict detection and handling
  - Transaction rollback

## Implementation Approach

### Option 1: Add continuous/tst as test source (Recommended)
- Add `continuous/tst` as test source directory in pom.xml
- Convert examples to JUnit 5 test classes
- This keeps continuous tests separate from main tests

### Option 2: Add to existing test directory
- Add tests to junit_tests/src/org/garret/perst/continuous/
- Requires moving/renaming test files

**Recommended: Option 1** - Keep continuous tests in their own directory

## Tasks

### Task 1: Add continuous/tst as test source
- Modify pom.xml to include continuous/tst as test source

### Task 2: Convert SimpleRelation to JUnit 5
- Create TestSimpleRelation.java with:
  - @BeforeEach setup (create storage, open database)
  - @AfterEach teardown (close, delete files)
  - Test methods for insert, find, select, full-text search
  - Use assertions to verify results

### Task 3: Convert Bank to JUnit 5
- Create TestBank.java with:
  - @BeforeEach/@AfterEach setup/teardown
  - Test concurrent transactions
  - Test balance consistency
  - Test conflict handling

## Dependencies
- Maven
- JUnit 5
- Continuous module compiled

## Success Criteria
- [ ] Tests compile successfully
- [ ] Tests run and pass
- [ ] Database files cleaned up after tests

## Rollback Plan
- **Checkpoint:** Git commit before changes
- **Revert command:** git reset --hard HEAD
- **Impact:** No impact on other tests

## Supporting Documents
- Todolist: `md/todo_continuous_tests.md`
- Progress: `md/progress_continuous_tests.md`
