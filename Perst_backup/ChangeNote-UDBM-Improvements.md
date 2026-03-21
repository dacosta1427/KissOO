# ChangeNote: UnifiedDBManager Improvements

**Date:** 2026-03-21  
**Priority:** Transaction Leak = High, Others = Medium  
**File:** `org.garret.perst.dbmanager.UnifiedDBManagerImpl`  
**Related:** `org.garret.perst.continuous.CDatabase`

---

## Executive Summary

This ChangeNote documents issues and recommended fixes for `UnifiedDBManager` discovered during KissOO integration. The primary issue is a **transaction state leak** that can cause `isInTransaction()` to incorrectly return `true`. Secondary issues include incomplete implementations and inconsistent error handling.

---

## Issue 1: Transaction State Leak (HIGH PRIORITY)

### Problem Description

The `ThreadLocal<Boolean> inTransaction` flag can become "stuck" on `true` if an exception occurs during `commitTransaction()` or `rollbackTransaction()`. This causes `isInTransaction()` to return `true` even when no transaction is active.

### Affected Code

#### A. `store()` method (lines 128-178)

```java
@Override
public StoreResult store(TransactionContainer container) {
    if (cdb == null) {
        return StoreResult.error("DBManager not initialized");
    }
    
    // Validation phase (outside transaction)
    for (CVersion obj : container.getUpdateList()) {
        long expectedVersion = container.getExpectedVersion(obj);
        CVersionHistory<?> history = obj.getVersionHistory();
        if (history != null) {
            CVersion current = history.getCurrent();
            if (current != null && current.getId() != expectedVersion) {
                return StoreResult.conflict(current, current.getId());
            }
        }
    }
    
    // Transaction phase
    CDatabase.syncToLinFlag.set(container.isSyncToLin());
    try {
        cdb.beginTransaction();
        try {
            // Process inserts
            for (CVersion obj : container.getInsertList()) {
                cdb.insert(obj);
            }
            
            // Process updates
            for (CVersion obj : container.getUpdateList()) {
                cdb.update(obj);
            }
            
            // Process deletes
            for (CVersion obj : container.getDeleteList()) {
                cdb.delete(obj);
            }
            
            cdb.commitTransaction();
            return StoreResult.success(null, 0);
            
        } catch (Exception e) {
            cdb.rollbackTransaction();
            return StoreResult.error(e.getMessage());
        }
    } finally {
        CDatabase.syncToLinFlag.remove();
    }
}
```

**Problems:**
1. `inTransaction` is never set to `true` when entering the transaction
2. If `cdb.commitTransaction()` throws, the exception propagates and `inTransaction` state becomes inconsistent
3. No guarantee that transaction state is synchronized with actual database state

#### B. Direct transaction methods (lines 283-309)

```java
@Override
public void beginTransaction() {
    if (cdb != null) {
        cdb.beginTransaction();
        inTransaction.set(true);
    }
}

@Override
public void commitTransaction() {
    if (cdb != null) {
        cdb.commitTransaction();        // ← If THROWS, exception propagates
        inTransaction.set(false);       // ← Never executes!
    }
}

@Override
public void rollbackTransaction() {
    if (cdb != null) {
        cdb.rollbackTransaction();      // ← If THROWS, exception propagates
        inTransaction.set(false);       // ← Never executes!
    }
}
```

### Recommended Fix

#### Fix 1: Update `commitTransaction()`

```java
@Override
public void commitTransaction() {
    if (cdb != null) {
        try {
            cdb.commitTransaction();
        } finally {
            inTransaction.set(false);
        }
    }
}
```

#### Fix 2: Update `rollbackTransaction()`

```java
@Override
public void rollbackTransaction() {
    if (cdb != null) {
        try {
            cdb.rollbackTransaction();
        } finally {
            inTransaction.set(false);
        }
    }
}
```

#### Fix 3: Update `store()` method

```java
@Override
public StoreResult store(TransactionContainer container) {
    if (cdb == null) {
        return StoreResult.error("DBManager not initialized");
    }
    
    // Validation phase (outside transaction)
    for (CVersion obj : container.getUpdateList()) {
        long expectedVersion = container.getExpectedVersion(obj);
        CVersionHistory<?> history = obj.getVersionHistory();
        if (history != null) {
            CVersion current = history.getCurrent();
            if (current != null && current.getId() != expectedVersion) {
                return StoreResult.conflict(current, current.getId());
            }
        }
    }
    
    // Transaction phase
    CDatabase.syncToLinFlag.set(container.isSyncToLin());
    inTransaction.set(true);  // Set flag when beginning
    try {
        cdb.beginTransaction();
        try {
            // Process inserts
            for (CVersion obj : container.getInsertList()) {
                cdb.insert(obj);
            }
            
            // Process updates
            for (CVersion obj : container.getUpdateList()) {
                cdb.update(obj);
            }
            
            // Process deletes
            for (CVersion obj : container.getDeleteList()) {
                cdb.delete(obj);
            }
            
            cdb.commitTransaction();
            return StoreResult.success(null, 0);
            
        } catch (Exception e) {
            cdb.rollbackTransaction();
            return StoreResult.error(e.getMessage());
        }
    } finally {
        CDatabase.syncToLinFlag.remove();
        inTransaction.set(false);  // Always clear flag
    }
}
```

### Alternative (Safety Net)

If modifying the methods is undesirable, add a safety net to `isInTransaction()`:

```java
@Override
public boolean isInTransaction() {
    // Safety net: sync with CDatabase if out of sync
    if (inTransaction.get() && cdb != null && !cdb.isInTransaction()) {
        inTransaction.set(false);
    }
    return inTransaction.get();
}
```

**Recommendation:** Use the try-finally approach (Fixes 1-3) as it's cleaner and ensures proper state management.

### Impact if Not Fixed

1. `isInTransaction()` returns `true` when no transaction is active
2. Code that checks transaction state may behave incorrectly
3. Thread pool contamination - leaked state persists across requests on same thread
4. Difficult to diagnose production issues

---

## Issue 2: Incomplete getMemoryStats() (MEDIUM PRIORITY)

### Problem Description

The `getMemoryStats()` method returns hardcoded zeros for `loadedObjects` and `lazyLoadedCollections`, despite maintaining a `monitoredCollections` map that isn't used.

### Current Code (lines 268-273)

```java
@Override
public MemoryStats getMemoryStats() {
    Runtime rt = Runtime.getRuntime();
    long used = rt.totalMemory() - rt.freeMemory();
    long max = rt.maxMemory();
    return new MemoryStats(used, max, 0, 0);  // hardcoded zeros!
}
```

### Recommended Fix

```java
@Override
public MemoryStats getMemoryStats() {
    Runtime rt = Runtime.getRuntime();
    long used = rt.totalMemory() - rt.freeMemory();
    long max = rt.maxMemory();
    
    int loadedObjects = 0;
    int lazyLoadedCollections = 0;
    
    for (CollectionInfo info : monitoredCollections.values()) {
        if (info.isLarge()) {
            lazyLoadedCollections++;
        }
        loadedObjects += info.getEstimatedSize();
    }
    
    return new MemoryStats(used, max, loadedObjects, lazyLoadedCollections);
}
```

**Note:** The `CollectionInfo` class needs `getEstimatedSize()` and `isLarge()` accessors, or we need to track counts separately.

### Alternative (Simpler)

```java
@Override
public MemoryStats getMemoryStats() {
    Runtime rt = Runtime.getRuntime();
    long used = rt.totalMemory() - rt.freeMemory();
    long max = rt.maxMemory();
    
    return new MemoryStats(
        used, 
        max, 
        monitoredCollections.size(),  // count of monitored collections
        0  // lazy loaded count requires additional tracking
    );
}
```

---

## Issue 3: Hardcoded searchFullText() Limit (MEDIUM PRIORITY)

### Problem Description

The `searchFullText()` method hardcodes the result limit to 1000, with no way for callers to configure it.

### Current Code (line 221-232)

```java
@Override
public IterableIterator<CVersion> searchFullText(String query) {
    try {
        FullTextSearchResult[] results = cdb.fullTextSearch(query, 1000);  // hardcoded!
        List<CVersion> list = new ArrayList<>();
        for (FullTextSearchResult r : results) {
            list.add(r.getVersion());
        }
        return new ArrayIterator(list);
    } catch (Exception e) {
        throw new RuntimeException("Search failed: " + e.getMessage(), e);
    }
}
```

### Recommended Fix

#### Option A: Add parameter to method

```java
@Override
public IterableIterator<CVersion> searchFullText(String query) {
    return searchFullText(query, 1000);
}

public IterableIterator<CVersion> searchFullText(String query, int maxResults) {
    try {
        FullTextSearchResult[] results = cdb.fullTextSearch(query, maxResults);
        List<CVersion> list = new ArrayList<>();
        for (FullTextSearchResult r : results) {
            list.add(r.getVersion());
        }
        return new ArrayIterator(list);
    } catch (Exception e) {
        throw new RuntimeException("Search failed: " + e.getMessage(), e);
    }
}
```

#### Option B: Add field with setter

```java
private int defaultSearchLimit = 1000;

public void setDefaultSearchLimit(int limit) {
    this.defaultSearchLimit = limit;
}

@Override
public IterableIterator<CVersion> searchFullText(String query) {
    try {
        FullTextSearchResult[] results = cdb.fullTextSearch(query, defaultSearchLimit);
        // ...
    }
}
```

---

## Issue 4: Inconsistent Exception Handling (LOW PRIORITY)

### Problem Description

Methods have inconsistent exception handling strategies:
- Some return `null` on error
- Some throw `RuntimeException`
- No clear convention

### Examples

```java
// Returns null (line 44-54)
public <T extends CVersion> T getByUuid(String uuid) {
    try {
        // ...
    } catch (Exception e) {
        throw new RuntimeException("Failed to get by UUID: " + e.getMessage(), e);
    }
}

// Returns null (line 57-81)
public <T> RetrieveResult<T> getByOid(long oid) {
    try {
        // ...
    } catch (Exception e) {
        return null;  // Silent failure!
    }
}

// Throws (line 221-232)
public IterableIterator<CVersion> searchFullText(String query) {
    try {
        // ...
    } catch (Exception e) {
        throw new RuntimeException("Search failed: " + e.getMessage(), e);
    }
}
```

### Recommended Fix

Define a clear error handling strategy. Recommendation:

1. **Retrieval methods:** Return `null` for not-found, throw for system errors
2. **Store methods:** Return `StoreResult` with error details
3. **Never silently swallow exceptions** without logging

```java
// Example: getByUuid should be
public <T extends CVersion> T getByUuid(String uuid) {
    try {
        FullTextSearchResult[] results = cdb.fullTextSearch("_UUID:" + uuid, 1);
        if (results != null && results.length > 0) {
            return (T) results[0].getVersion();
        }
        return null;  // Not found - return null
    } catch (Exception e) {
        throw new RuntimeException("Failed to get by UUID: " + e.getMessage(), e);
    }
}
```

---

## Issue 5: Inconsistent Null Handling in getVersionForUpdate() (LOW PRIORITY)

### Problem Description

The `getVersionForUpdate()` method has inconsistent null handling.

### Current Code (lines 94-111)

```java
@Override
public <T extends CVersion> VersionedObject<T> getVersionForUpdate(CVersionHistory<T> versionHistory) {
    if (versionHistory == null) {
        return null;  // Silent null return
    }
    
    T current = versionHistory.getCurrent();
    if (current == null) {
        throw new IllegalArgumentException("Version history cannot be null");  // Wrong message!
    }
    
    // ...
}
```

### Recommended Fix

```java
@Override
public <T extends CVersion> VersionedObject<T> getVersionForUpdate(CVersionHistory<T> versionHistory) {
    if (versionHistory == null) {
        throw new IllegalArgumentException("Version history cannot be null");
    }
    
    T current = versionHistory.getCurrent();
    if (current == null) {
        throw new IllegalStateException("Version history has no current version");
    }
    
    // ...
}
```

---

## Issue 6: CDatabase Singleton Usage (INFO)

### Current Code (line 28)

```java
public void open(Storage storage, String luceneIndexPath) {
    this.storage = storage;
    this.cdb = CDatabase.instance;  // Singleton access
    this.cdb.open(storage, luceneIndexPath);
}
```

### Note

The singleton pattern limits flexibility if multiple databases are needed. Consider:
- Passing CDatabase as a constructor parameter
- Using dependency injection

**Lower priority** - only relevant if multi-database support is needed.

---

## Summary of Recommended Changes

| Issue | Priority | Files | Change |
|-------|----------|-------|--------|
| Transaction State Leak | **HIGH** | UnifiedDBManagerImpl | Fix 1-3 (try-finally) |
| Incomplete getMemoryStats() | Medium | UnifiedDBManagerImpl | Track and return actual stats |
| Hardcoded searchFullText limit | Medium | UnifiedDBManagerImpl | Add parameter or setter |
| Inconsistent exception handling | Low | UnifiedDBManagerImpl | Standardize error strategy |
| Inconsistent null handling | Low | UnifiedDBManagerImpl | Fix messages, be consistent |
| CDatabase singleton | Info | UnifiedDBManagerImpl | Consider DI (future) |

---

## Testing Recommendations

After implementing fixes:

1. **Transaction Leak Fix:**
   ```java
   // Test 1: Simulate commit exception
   @Test
   public void testCommitExceptionClearsFlag() {
       dbm.beginTransaction();
       assertTrue(dbm.isInTransaction());
       // Simulate exception in commit...
       dbm.rollbackTransaction();
       assertFalse(dbm.isInTransaction());
   }
   
   // Test 2: Concurrent threads
   @Test
   public void testThreadIsolation() throws Exception {
       // Thread 1 starts transaction
       // Thread 2 should see isInTransaction() = false
   }
   ```

2. **MemoryStats:**
   - Register collections
   - Verify counts are accurate

3. **searchFullText:**
   - Test with various limits
   - Verify results are capped correctly

---

## Files to Modify

| File | Changes |
|------|---------|
| `UnifiedDBManagerImpl.java` | All fixes (1-6) |

---

## Contact

For questions or clarifications, contact the KissOO integration team.
