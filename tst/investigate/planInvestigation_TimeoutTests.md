# Working Rules (must be included)

---

# Plan: Investigate Timeout Tests (TestBitmap, TestMaxOid, TestR2, TestRtree)

## Error Details

### TestBitmap
- Output: Inserts 1,000,000 records
- Timeout: 30s not enough (took >2 minutes in testing)

### TestMaxOid
- Output: Inserts 15,000,000 records  
- Timeout: 60s not enough (timed out after 60s)

### TestR2
- Output: Iterates 29,000+ times
- Timeout: 30s not enough (iterations too slow)

### TestRtree
- Output: Iterates 28,000+ times
- Timeout: 30s not enough (iterations too slow)

## Root Cause Analysis

### Initial Assessment
These tests are NOT failing - they just take a long time to run. Each test is designed to stress-test Perst with large datasets:
- TestBitmap: Large bitmap index operations
- TestMaxOid: Maximum OID testing with millions of records
- TestR2: R-tree spatial index testing
- TestRtree: R-tree index operations

### Test Expected Behavior
These are performance/stress tests that intentionally use large datasets to test Perst's scalability. They work correctly but need more time.

## Investigation Plan

### Step 1: Determine Appropriate Timeouts
- TestBitmap: Likely needs 120-180s
- TestMaxOid: Likely needs 180-300s  
- TestR2: Likely needs 60-90s
- TestRtree: Likely needs 60-90s

### Step 2: Update Test Script
- Increase timeouts for these specific tests
- Or mark them as "long-running" tests

### Step 3: Document as Known Long-Running Tests
- Add to documentation that these are intentionally slow
- Consider separating into "quick" and "full" test suites

## Success Criteria
- [ ] Determine appropriate timeout values
- [ ] Update test script with correct timeouts
- [ ] Document these as expected long-running tests

## Dependencies
- None - independent investigation

## Effort Estimate
S (Small) - Just timeout adjustment
