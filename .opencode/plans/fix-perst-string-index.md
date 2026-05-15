# Fix Plan: Perst String Index Bug

## Problem Summary
Login fails with "Invalid type" error in Perst Btree code. The issue is in the `searchPosition` method in `Btree.java`.

## Root Cause
The `searchPosition` method uses `BtreePage.compare()` directly for all key types, but `tpString` requires `BtreePage.compareStr()`. The `compare()` function doesn't have a case for `tpString`, causing it to fall through to `Assert.failed("Invalid type")`.

## Fix Location
File: `../oodbGTxQ/src/main/java/org/garret/perst/impl/Btree.java`
Method: `BtreeSelectionIterator.searchPosition(Key key)`
Lines: 586-631

## Changes Required

### In `Btree.java` lines 598-604 and 617-623:
Replace:
```java
if (BtreePage.compare(key, pg, i) >= key.inclusion) {
```

With:
```java
int cmp = type == ClassDescriptor.tpString 
    ? BtreePage.compareStr(key, pg, i) 
    : BtreePage.compare(key, pg, i);
if (cmp >= key.inclusion) {
```

## Testing
1. Clear the database
2. Restart server
3. Test login with admin/admin credentials
4. Verify the PerstAuth logs show successful authentication

## Alternative Approach
If the Perst fix is not desired, an alternative is to use `select()` instead of `find()` in `StorageManager.find()` - this iterates all objects and filters in memory, avoiding the index lookup issue entirely.