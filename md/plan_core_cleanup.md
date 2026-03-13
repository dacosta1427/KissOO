# Working Rules (must be included)

---

# Plan: Perst Core Code Cleanup

## Objective
Identify and fix compile warnings and errors in the Perst core code (src directory) to achieve clean compilation with `-Xlint:all`.

## Files with Issues (Identified by `mvn clean compile`)

### 1. Aggregator.java (12 warnings)
- **Issue:** Unchecked generic calls (initialize, accumulate, merge, compareTo, add, addAll)
- **Solution:** Add proper type parameters or use @SuppressWarnings where type safety is guaranteed

### 2. Persistent.java (1 warning)
- **Issue:** finalize() method uses deprecated API marked for removal in Java 25
- **Solution:** Remove finalize() method or deprecate it with @Deprecated annotation

### 3. FullTextSearchHelper.java (3 warnings)
- **Issue:** Unchecked calls to ArrayList.add() and toArray()
- **Solution:** Add proper type parameters

### 4. FullTextSearchResult.java (2 warnings)
- **Issue:** Unchecked method invocation in Arrays.sort() with anonymous Comparator
- **Solution:** Use properly typed Comparator

### 5. Database.java (~28 warnings)
- **Issue:** Multiple unchecked calls to Index.put(), FieldIndex.put(), ArrayList.add(), HashMap.put(), Query.execute(), etc.
- **Solution:** Add proper type parameters throughout the class

### 6. L2List.java (1 warning)
- **Issue:** Unchecked call to Query.select()
- **Solution:** Add proper type parameters

### 7. Projection.java (4 warnings)
- **Issue:** Unchecked cast operations
- **Solution:** Add @SuppressWarnings("unchecked") with justification or refactor to avoid casts

### 8. SmallMap.java (6 warnings)
- **Issue:** Unchecked conversions and casts for Pair arrays
- **Solution:** Use proper generic array types or suppress warnings appropriately

### 9. Version.java (2 warnings)
- **Issue:** Unchecked calls to List.add() and raw type assignment
- **Solution:** Add proper type parameters

### 10. VersionHistory.java (1 warning)
- **Issue:** Unchecked cast from Object to Version
- **Solution:** Add @SuppressWarnings or refactor

### 11. AltBtree.java (~58 warnings)
- **Issue:** Extensive unchecked calls to Link.setObject(), ArrayList.add(), Comparable.compareTo(), and casts
- **Solution:** Add type parameters or suppress warnings with proper justification

### 12. AltBtreeCompoundIndex.java (1 warning)
- **Issue:** Unchecked call to Comparable.compareTo()
- **Solution:** Add proper type parameter

### 13. AltBtreeFieldIndex.java (6 warnings)
- **Issue:** Unchecked casts and conversions for arrays and ArrayLists
- **Solution:** Add proper type parameters

### 14. AltBtreeMultiFieldIndex.java (7 warnings)
- **Issue:** Unchecked calls to Comparable.compareTo(), ArrayList.toArray(), and casts
- **Solution:** Add proper type parameters

### 15. AltPersistentSet.java (3 warnings)
- **Issue:** Unchecked casts for generic arrays
- **Solution:** Add proper type parameters

### 16. StorageImpl.java (2 warnings)
- **Issue:** Unchecked return type conversions in getMemoryDump() and getRoot()
- **Solution:** Add proper type parameters or adjust interface signatures

## Success Criteria
- All files compile without warnings using `mvn clean compile`
- No new warnings introduced
- All existing tests pass

## Rollback Plan
1. Git revert to previous state
2. Run `mvn clean compile` to verify
3. Run tests to ensure no regressions
