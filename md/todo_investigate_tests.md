# Working Rules (must be included)

---

# Todolist: Investigate Failed Tests

## Tasks

### Priority #1: Investigation Plans (COMPLETED)
- [x] Task 1.1: Create investigate directory in tst/
- [x] Task 1.2: Analyze error logs for each failed test
- [x] Task 1.3: Create planInvestigation_TestAlloc.md
- [x] Task 1.4: Create planInvestigation_TestLeak.md
- [x] Task 1.5: Create planInvestigation_TestSOD.md
- [x] Task 1.6: Create planInvestigation_TestCompoundIndex_altbtree.md
- [x] Task 1.7: Create planInvestigation_DynamicObjects.md
- [x] Task 1.8: Create planInvestigation_TestBlob_zip.md
- [x] Task 1.9: Create planInvestigation_TestRaw.md
- [x] Task 1.10: Create planInvestigation_TimeoutTests.md (TestBitmap, TestMaxOid, TestR2, TestRtree)

## Summary of Created Plans

| Test | Issue Type | Likely Solution |
|------|------------|-----------------|
| TestAlloc | Sequencing | Run in proper order (makefile style) |
| TestLeak | Timeout | Increase timeout (long-running test) |
| TestSOD | Interactive | Exclude from automated runs |
| TestCompoundIndex altbtree | Library Bug | Check Perst version / exclude |
| DynamicObjects | Script Error | Fix typo in test script |
| TestBlob zip | Sequencing | Run CompressDatabase.sh first |
| TestRaw | Missing Constructor | Add no-arg constructor to ListItem |
| TestBitmap | Timeout | Increase timeout |
| TestMaxOid | Timeout | Increase timeout |
| TestR2 | Timeout | Increase timeout |
| TestRtree | Timeout | Increase timeout |

## Success Criteria
- [x] All failed tests have investigation plans
- [x] Root cause identified for each failure
- [ ] Execute investigation plans
- [ ] Implement fixes where possible

## Rollback Plan
1. Revert any changes to test source files
2. Restore original tracking_tests.md if needed
