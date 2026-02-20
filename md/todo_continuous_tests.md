# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: Convert Continuous Examples to JUnit 5 Tests

## Tasks

### Priority #1: Setup
- [ ] Task 1.1: Add continuous/tst as test source directory in pom.xml - Configure Maven to compile and run tests from continuous/tst

### Priority #2: SimpleRelation Tests
- [ ] Task 2.1: Create TestSimpleRelation.java - Convert SimpleRelation to JUnit 5 test class
- [ ] Task 2.2: Add testInsertCompany test method - Test inserting companies
- [ ] Task 2.3: Add testInsertEmployee test method - Test inserting employees with company relation
- [ ] Task 2.4: Add testFindByQuery test method - Test JSQL queries
- [ ] Task 2.5: Add testFullTextSearch test method - Test Lucene full-text search

### Priority #3: Bank Tests
- [ ] Task 3.1: Create TestBank.java - Convert Bank to JUnit 5 test class
- [ ] Task 3.2: Add testInitializeAccounts test method - Test account creation
- [ ] Task 3.3: Add testConcurrentTransfers test method - Test multi-threaded transfers
- [ ] Task 3.4: Add testBalanceConsistency test method - Test total balance remains correct

### Priority #4: Verification
- [ ] Task 4.1: Run mvn test to verify all tests pass
- [ ] Task 4.2: Verify database files are cleaned up

## Success Criteria
- [ ] All tests compile and pass
- [ ] Continuous module functionality verified

## Rollback Plan
1. Git reset to revert changes
2. Verify tests are removed
