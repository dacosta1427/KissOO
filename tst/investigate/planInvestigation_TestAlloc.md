# Working Rules (must be included)

---

# Plan: Investigate TestAlloc Failure

## Error Details
```
Exception in thread "main" org.garret.perst.StorageError: File access error
at org.garret.perst.impl.MultiFile.<init>(MultiFile.java:130)
	at org.garret.perst.impl.StorageImpl.open(StorageImpl.java:916)
	at org.garret.perst.impl.StorageImpl.open(StorageImpl.java:907)
	at TestAlloc.main(TestAlloc.java:9)
```

## Root Cause Analysis

### Initial Assessment
The error `StorageError: File access error` when opening a MultiFile indicates one of:
1. Missing required database file that the test expects to exist
2. Permission issues on the database file
3. Incorrect file path/location
4. Database file is locked by another process

### Test Expected Behavior
According to the makefile, TestAlloc runs 3 times:
1. First run: Initialize database
2. Second run: Verify/read from database  
3. Third run: Cleanup

The failure occurs on first run when trying to open, suggesting it expects an existing database file.

## Investigation Plan

### Step 1: Examine TestAlloc Source Code
- Read tst/TestAlloc.java to understand expected file names and parameters
- Check what arguments it accepts (if any)
- Identify expected database file structure

### Step 2: Check Existing Database Files
- Look for testalloc.* files in tst/ directory
- Verify if database files exist that should be removed/created

### Step 3: Check File Permissions
- Verify read/write permissions on tst/ directory
- Check if any lock files exist

### Step 4: Test with Correct Sequence
- Run makefile test sequence to see if it works in proper order
- Compare with how other tests handle initialization

## Success Criteria
- [ ] Identify why file access error occurs
- [ ] Determine if test needs modification or just correct execution order
- [ ] Document fix or workaround

## Dependencies
- None - this is an independent investigation

## Effort Estimate
S (Small) - Should be quick to diagnose once source is examined
