> See [workingRules.md](./workingRules.md) for core operating principles

---

# Todolist: Continuous Directory Code Cleanup

## Tasks

### Priority #1: Fix Lucene API Errors (High Priority)
- [ ] Task 1.1: Fix CDatabase.java - Update Lucene 9.x query parser and analysis imports
- [ ] Task 1.2: Fix FullTextSearchIterator.java - Update Lucene 9.x search API
- [ ] Task 1.3: Fix PerstDirectory.java - Update Lucene 9.x Directory API
- [ ] Task 1.4: Fix TableDescriptor.java - Update Lucene 9.x API

### Priority #2: Fix Type Warnings (Low Priority)
- [ ] Task 2.1: Fix CVersion.java - Add proper generic type parameters
- [ ] Task 2.2: Fix CVersionHistory.java - Add proper generic type parameters
- [ ] Task 2.3: Fix ExtentIterator.java - Add proper generic type parameters
- [ ] Task 2.4: Fix IndexIterator.java - Add proper generic type parameters

### Priority #3: Verification
- [ ] Task 3.1: Run compile and verify no errors
- [ ] Task 3.2: Run tests and verify all pass

## Success Criteria
- [ ] All 21 compile errors fixed
- [ ] All type warnings addressed
- [ ] Build succeeds

## Rollback Plan
1. Git revert changes
2. Run compile to verify
