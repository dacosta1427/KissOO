# Working Rules (must be included)

---

# Plan: Investigate TestLeak Failure

## Error Details
```
Output: Iteration 0
(Testimed out after 30 seconds - only produced one iteration)
```

## Root Cause Analysis

### Initial Assessment
The test starts but appears to hang or run extremely slowly after the first iteration. This suggests:
1. Memory leak detection takes a very long time
2. The test may require more time to complete its leak detection cycle
3. Possible issue with the garbage collection monitoring

### Test Expected Behavior
TestLeak is designed to detect memory leaks in the Perst database. It likely:
1. Creates objects in a loop
2. Monitors memory usage over time
3. Reports any memory leaks detected

## Investigation Plan

### Step 1: Examine TestLeak Source Code
- Read tst/TestLeak.java to understand what it does
- Check iteration count expectations
- Identify what parameters it accepts

### Step 2: Run with Longer Timeout
- Execute with 120s or 300s timeout instead of 30s
- Observe if it completes successfully with more time

### Step 3: Check Memory Usage
- Monitor Java heap size during test execution
- Check if test is stuck or just slow

### Step 4: Compare with Makefile
- Look at how TestLeak is run in the makefile
- Check if there are any special requirements

## Success Criteria
- [ ] Determine if test actually fails or just needs more time
- [ ] Identify proper timeout value if needed
- [ ] Document findings

## Dependencies
- None - independent investigation

## Effort Estimate
S (Small) - Likely just needs longer timeout
