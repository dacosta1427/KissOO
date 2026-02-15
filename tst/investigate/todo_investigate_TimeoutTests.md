# Working Rules (must be included)

---

# Todolist: Investigate Timeout Tests (TestBitmap, TestMaxOid, TestR2, TestRtree)

## Tasks

### Priority #1: Investigation
- [x] Task 1.1: Determine appropriate timeout for TestBitmap (~600s for 1M records)
- [x] Task 1.2: Determine appropriate timeout for TestMaxOid (~7+ hours for 1B records)
- [x] Task 1.3: Determine appropriate timeout for TestR2 (~7s, within 30s)
- [x] Task 1.4: Determine appropriate timeout for TestRtree (~6s, within 30s)
- [ ] Task 1.5: Update test scripts with correct timeouts
- [ ] Task 1.6: Document as long-running tests

## Success Criteria
- [ ] All timeout tests have appropriate timeouts
- [ ] Tests run to completion

## Rollback Plan
1. Revert timeout changes in test scripts
