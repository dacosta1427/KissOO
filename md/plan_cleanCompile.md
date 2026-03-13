# Working Rules (must be included)

---

# Plan: Clean Compiler Warnings

## Goal
Eliminate all compiler warnings from the Perst project by fixing unchecked casts, raw type usage, and deprecated API calls.

## Background
The build currently generates ~200+ compiler warnings, primarily:
- **Unchecked casts** - Raw type to generic type conversions
- **Unchecked calls** - Using raw types with generic methods
- **Deprecated API usage** - Using `Integer(int)`, `Long(long)`, `Double(double)` constructors

These warnings should be fixed to:
1. Improve code quality and type safety
2. Prepare for `-Werror` enforcement in CI
3. Ensure compatibility with future Java versions

## Affected Files Summary

### Main Source (src/main/java)
| Package | Files | Warning Types |
|---------|-------|----------------|
| org.garret.perst | 6 | Unchecked casts, raw types |
| org.garret.perst.impl | 7 | Unchecked casts, raw types |
| org.garret.perst.assoc | 4 | Deprecated API, unchecked casts |
| org.garret.perst.fulltext | 2 | Unchecked casts |

### Test Source (src/test/java)
| Package | Files | Warning Types |
|---------|-------|----------------|
| org.garret.perst | 7 | Unchecked casts, raw types, deprecated |
| org.garret.perst.assoc | 1 | Unchecked casts |

## Approach

### 1. Deprecated API Fixes
- Replace `Integer(int)`, `Long(long)`, `Double(double)` with `Integer.valueOf()`, `Long.valueOf()`, `Double.valueOf()`
- These are in: AssocDB.java, Item.java, ReadWriteTransaction.java, TestBackup.java

### 2. Unchecked Casts Fixes
- Add proper type parameters where possible
- Use `@SuppressWarnings("unchecked")` with clear justification where runtime types prevent clean solutions
- Focus on critical classes first (StorageImpl, AltBtree, Transaction classes)

### 3. Raw Type Fixes
- Replace raw type usage with parameterized versions
- Common patterns: `List` → `List<T>`, `Set` → `Set<T>`, `Map` → `Map<K,V>`

## Implementation Strategy
1. Fix deprecated API usages (quick wins)
2. Fix main source files (high impact)
3. Fix test source files
4. Run full test suite to verify no regressions
5. Verify zero warnings in build

## Rollback Plan
- **Checkpoint:** Git commit before changes
- **Revert command:** `git reset --hard HEAD`
- **Verification:** Run `mvn clean compile` and check for warnings
- **Impact:** Code changes only, no structural modifications

---

## Files Detailed Analysis

### Main Source Files Requiring Changes

#### org.garret.perst
1. **Aggregator.java** - Unchecked calls to Aggregate<T> methods, Comparable<T>.compareTo
2. **L2List.java** - Unchecked call to Query.select
3. **Projection.java** - Unchecked casts (From/To type parameters)
4. **SmallMap.java** - Unchecked array conversions, unchecked cast
5. **Version.java** - Unchecked call to List.add, raw type assignment
6. **VersionHistory.java** - Unchecked cast

#### org.garret.perst.impl
1. **AltBtree.java** - Heavy unchecked usage with ArrayList, Link, Comparable
2. **AltBtreeCompoundIndex.java** - Unchecked Comparable.compareTo
3. **AltBtreeFieldIndex.java** - Unchecked casts and conversions
4. **AltBtreeMultiFieldIndex.java** - Unchecked casts and toArray calls
5. **AltPersistentSet.java** - Unchecked array casts
6. **StorageImpl.java** - Unchecked ArrayList.add, HashMap.put, Class.isAssignableFrom

#### org.garret.perst.assoc
1. **AssocDB.java** - Deprecated Integer(int) constructor
2. **Item.java** - Deprecated Double(double) constructor
3. **ReadOnlyTransaction.java** - Unchecked casts, raw type Comparable.compareTo
4. **ReadWriteTransaction.java** - Unchecked casts, deprecated Integer(int)

#### org.garret.perst.fulltext
1. **FullTextSearchHelper.java** - Unchecked ArrayList.add, HashSet.add, toArray
2. **FullTextSearchResult.java** - Unchecked Arrays.sort with raw Comparator

### Test Source Files Requiring Changes

1. **DatabaseTest.java** - Deprecated createTable method calls
2. **PersistentSetTest.java** - Unchecked Set.add calls
3. **QueryTest.java** - Unchecked Vector.add, Query.select calls
4. **StorageTest.java** - Unchecked Set.add, Query.select calls
5. **TestAlloc.java** - Unchecked Index.put calls
6. **TestBackup.java** - Unchecked Index.put, deprecated Long(long)
7. **TestFullTextIndex.java** - Unchecked FieldIndex.put calls
8. **TestRaw.java** - Deprecated API usage
