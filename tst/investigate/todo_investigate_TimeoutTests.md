# Working Rules (must be included)

---

# Todolist: Investigate Timeout Tests (TestBitmap, TestMaxOid, TestR2, TestRtree)

## Tasks

### Priority #1: Investigation
- [ ] Task 1.1: Determine appropriate timeout for TestBitmap (>120s likely needed)
- [ ] Task 1.2: Determine appropriate timeout for TestMaxOid (>180s likely needed)
- [ ] Task 1.3: Determine appropriate timeout for TestR2 (>60s likely needed)
- [ ] Task 1.4: Determine appropriate timeout for TestRtree (>60s likely needed)
- [ ] Task 1.5: Update test scripts with correct timeouts
- [ ] Task 1.6: Document as long-running tests

## Success Criteria
- [ ] All timeout tests have appropriate timeouts
- [ ] Tests run to completion

## Rollback Plan
1. Revert timeout changes in test scripts
