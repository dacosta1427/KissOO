# Working Rules (must be included)

---

# Plan: Investigate TestCompoundIndex_altbtree Failure

## Error Details
```
Exception in thread "main" org.garret.perst.StorageError: Object access violation: 
Failed to build descriptor for class org.garret.perst.impl.AltBtreeMultiFieldIndex$CompoundKey: 
java.lang.NoSuchMethodException: org.garret.perst.impl.AltBtreeMultiFieldIndex$CompoundKey.<init>()

Caused by: java.lang.NoSuchMethodException: org.garret.perst.impl.AltBtreeMultiFieldIndex$CompoundKey.<init>()
```

## Root Cause Analysis

### Initial Assessment
The error indicates that the AltBtreeMultiFieldIndex.CompoundKey class is missing a no-arg constructor. This is a Perst library issue where:
1. The library is trying to dynamically instantiate a class
2. The class doesn't have a no-arg constructor
3. This is needed for object serialization/deserialization

### Test Expected Behavior
TestCompoundIndex with "altbtree" parameter tests compound indexes using the alternative B-tree implementation.

## Investigation Plan

### Step 1: Examine TestCompoundIndex Source
- Read tst/TestCompoundIndex.java
- Understand how altbtree mode is invoked
- Check if this is a known issue

### Step 2: Check Perst Library Version
- Verify which version of perst.jar is being used
- Check if this is a known bug in the library
- Look for patches or workarounds

### Step 3: Determine if Library Issue
- This may be a bug in the Perst library itself
- Check if newer version fixes this
- Consider excluding this test variant if library issue

## Success Criteria
- [ ] Identify root cause (library bug vs test issue)
- [ ] Document workaround or exclusion
- [ ] If fixable, implement solution

## Dependencies
- None - independent investigation

## Effort Estimate
M (Medium) - May require library version investigation
