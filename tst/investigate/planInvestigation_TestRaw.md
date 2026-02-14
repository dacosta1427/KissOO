# Working Rules (must be included)

---

# Plan: Investigate TestRaw Failure

## Error Details
```
Exception in thread "main" org.garret.perst.StorageError: Object access violation: 
Failed to build descriptor for class ListItem: java.lang.NoSuchMethodException: ListItem.<init>()

Caused by: java.lang.NoSuchMethodException: ListItem.<init>()
```

## Root Cause Analysis

### Initial Assessment
The error indicates that the ListItem class is missing a no-arg constructor. This is needed for Perst to dynamically instantiate the class during serialization/deserialization.

### Test Expected Behavior
TestRaw tests raw object storage capabilities. The ListItem class appears to be a helper class used by the test.

## Investigation Plan

### Step 1: Examine TestRaw Source Code
- Read tst/TestRaw.java to understand what it does
- Find the ListItem class definition
- Check if ListItem has a no-arg constructor

### Step 2: Check Class Definition
- Look at how ListItem is defined
- Identify why no-arg constructor is missing
- Determine if this is intentional or a bug

### Step 3: Fix or Document
- If fixable, add no-arg constructor to ListItem
- If intentional design, document the requirement

## Success Criteria
- [ ] Identify why ListItem lacks no-arg constructor
- [ ] Implement fix if possible
- [ ] Document findings

## Dependencies
- None - independent investigation

## Effort Estimate
S (Small) - Likely a simple constructor addition
