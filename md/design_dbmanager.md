# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# DBManager Design - Optimistic Locking with Lazy Loading

## Overview

This document describes the DBManager class - a central manager for data retrieval and storage in Perst, inspired by a librarian metaphor.

---

## Concept: The Librarian Metaphor

```
┌─────────────────────────────────────────────────────────────────┐
│                         Application                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   User: "I need book X"     User: "Here's my updated book"     │
│          │                            │                         │
│          ▼                            ▼                         │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                    DBManager                            │   │
│   │   (The Librarian)                                        │   │
│   │   • Manages access to the database                      │   │
│   │   • Handles retrieval and storage                        │   │
│   │   • Enforces optimistic locking                         │   │
│   │   • Monitors lazy loading                               │   │
│   └────────────────────────┬────────────────────────────────┘   │
│                            │                                     │
│                            ▼                                     │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                   Perst Storage                          │   │
│   │   (The Library)                                          │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## DBManager Responsibilities

### 1. Data Retrieval
- Get objects by ID
- Get objects by index
- Query objects using JSQL
- Lazy loading with memory monitoring

### 2. Data Storage
- Store new objects
- Update existing objects with optimistic locking
- Handle transactions (commit/rollback)

### 3. Optimistic Locking
- Track object versions
- Detect conflicts in multi-user scenarios
- Return conflict information for recovery

### 4. Memory Management
- Monitor collection loading
- Prevent OOM with large collections
- Support pagination/streaming

---

## API Design

### Core Interface

```java
package org.garret.perst.dbmanager;

/**
 * Result of an optimistic locking operation.
 * Contains the outcome and any conflict information.
 */
public class StoreResult<T> {
    private final Status status;
    private final T object;           // The object that was stored (if successful)
    private final T conflictObject;  // The current object in DB (if conflict)
    private final long version;       // Version that was stored
    
    public enum Status {
        SUCCESS,        // Object stored successfully
        CONFLICT,      // Optimistic lock conflict detected
        ERROR          // Other error occurred
    }
    
    // Getters...
}

/**
 * Result of retrieving an object with lazy-loaded collection info.
 */
public class RetrieveResult<T> {
    private final T object;
    private final long version;
    private final CollectionInfo[] largeCollections;
}

/**
 * Information about a potentially large collection.
 */
public class CollectionInfo {
    private final String fieldName;
    private final int estimatedSize;
    private final boolean shouldLazyLoad;
}
```

### DBManager Interface

```java
package org.garret.perst.dbmanager;

import org.garret.perst.*;
import org.garret.perst.continuous.*;

/**
 * DBManager - Central manager for data retrieval and storage.
 * Like a librarian, it handles all requests to get and store objects.
 * 
 * Features:
 * - Optimistic locking via version tracking
 * - Lazy loading monitoring for large collections
 * - Multi-user concurrent access support
 */
public interface DBManager extends AutoCloseable {
    
    // ========== Configuration ==========
    
    /**
     * Initialize the DBManager with a Perst storage.
     */
    void initialize(Storage storage);
    
    /**
     * Set the maximum collection size before triggering lazy loading warning.
     */
    void setLargeCollectionThreshold(int threshold);
    
    // ========== Retrieval Operations ==========
    
    /**
     * Get an object by its OID.
     * @param oid The object identifier
     * @return RetrieveResult containing the object and metadata
     */
    <T> RetrieveResult<T> get(long oid);
    
    /**
     * Get an object by its OID, with eager loading of specified collections.
     * @param oid The object identifier
     * @param eagerCollections Names of collections to load fully
     * @return RetrieveResult containing the object and metadata
     */
    <T> RetrieveResult<T> get(long oid, String... eagerCollections);
    
    /**
     * Get the current version of a CVersion object.
     * @param versionHistory The version history
     * @return The current version, or null if none
     */
    <T extends CVersion> T getCurrent(CVersionHistory<T> versionHistory);
    
    /**
     * Get the last current object before a specific version.
     * Used for optimistic locking - user gets a copy, modifies it, and stores it.
     * @param versionHistory The version history
     * @return The version that was current when retrieved (for comparison on store)
     */
    <T extends CVersion> VersionedObject<T> getVersionForUpdate(CVersionHistory<T> versionHistory);
    
    // ========== Storage Operations ==========
    
    /**
     * Store a new object.
     * @param object The object to store
     * @return StoreResult with status and any assigned OID
     */
    <T> StoreResult<T> store(T object);
    
    /**
     * Store with optimistic locking.
     * User provides the version they read - if it matches, store succeeds.
     * If not, conflict information is returned.
     * 
     * @param object The object to store
     * @param expectedVersion The version the user had when they retrieved the object
     * @return StoreResult with SUCCESS (if versions matched) or CONFLICT (with current object)
     */
    <T extends CVersion> StoreResult<T> storeWithOptimisticLock(T object, long expectedVersion);
    
    /**
     * Attempt to store with retry on conflict.
     * Automatically retries up to maxAttempts, calling the updater function each time.
     * 
     * @param versionHistory The version history
     * @param maxAttempts Maximum retry attempts
     * @param updater Function that modifies the working copy
     * @return StoreResult with final outcome
     */
    <T extends CVersion> StoreResult<T> storeWithRetry(
        CVersionHistory<T> versionHistory,
        int maxAttempts,
        java.util.function.Function<T, T> updater
    );
    
    // ========== Index Operations ==========
    
    /**
     * Get an index for querying.
     */
    <T> Index<T> getIndex(Class<T> type, String fieldName);
    
    /**
     * Create an index on a field.
     */
    <T> Index<T> createIndex(Class<T> type, String fieldName, boolean unique);
    
    // ========== Transaction Control ==========
    
    /**
     * Begin a transaction.
     */
    void beginTransaction();
    
    /**
     * Commit the current transaction.
     */
    void commit();
    
    /**
     * Rollback the current transaction.
     */
    void rollback();
    
    /**
     * Check if inside a transaction.
     */
    boolean isInTransaction();
    
    // ========== Collection Monitoring ==========
    
    /**
     * Register a collection for lazy loading monitoring.
     */
    void registerCollection(Object owner, String fieldName, int estimatedSize);
    
    /**
     * Check if a collection should use lazy loading.
     */
    boolean shouldLazyLoad(String fieldName, int estimatedSize);
    
    /**
     * Get memory usage statistics.
     */
    MemoryStats getMemoryStats();
    
    // ========== Resource Management ==========
    
    @Override
    void close();
}
```

### Versioned Object Wrapper

```java
package org.garret.perst.dbmanager;

/**
 * Wrapper containing a CVersion object and its version info.
 * Used in the optimistic locking workflow:
 * 1. User receives VersionedObject from getVersionForUpdate()
 * 2. User modifies the object
 * 3. User calls storeWithOptimisticLock() with the version
 * 4. If conflict, user gets new VersionedObject with current version
 */
public class VersionedObject<T> {
    private final T object;
    private final long versionId;
    private final long transactionId;
    private final Date timestamp;
    
    public VersionedObject(T object, long versionId, long transactionId, Date timestamp) {
        this.object = object;
        this.versionId = versionId;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
    }
    
    public T getObject() { return object; }
    public long getVersionId() { return versionId; }
    public long getTransactionId() { return transactionId; }
    public Date getTimestamp() { return timestamp; }
}
```

### Memory Statistics

```java
package org.garret.perst.dbmanager;

/**
 * Memory usage statistics for monitoring.
 */
public class MemoryStats {
    private final long usedMemory;
    private final long maxMemory;
    private final int loadedObjects;
    private final int lazyLoadedCollections;
    
    public MemoryStats(long usedMemory, long maxMemory, int loadedObjects, int lazyCollections) {
        this.usedMemory = usedMemory;
        this.maxMemory = maxMemory;
        this.loadedObjects = loadedObjects;
        this.lazyLoadedCollections = lazyCollections;
    }
    
    public double getUsagePercent() { 
        return (double) usedMemory / maxMemory * 100; 
    }
    
    // Getters...
}
```

---

## Optimistic Locking Workflow

### Success Case

```
User                            DBManager                         Database
  │                                │                                 │
  │  getVersionForUpdate(history) │                                 │
  │ ─────────────────────────────> │                                 │
  │                                │  getCurrent()                  │
  │                                │ ─────────────────────────────> │
  │                                │ <───────────────────────────── │
  │                                │  (returns VersionedObject)     │
  │ <───────────────────────────── │                                 │
  │  (gets copy + version)        │                                 │
  │                                │                                 │
  │  [modify the copy]             │                                 │
  │                                │                                 │
  │  storeWithOptimisticLock(obj, │                                 │
  │                             ver)│                                 │
  │ ─────────────────────────────> │                                 │
  │                                │  [check version matches]       │
  │                                │                                 │
  │                                │  store()                      │
  │                                │ ─────────────────────────────> │
  │                                │                                 │
  │ <───────────────────────────── │                                 │
  │  StoreResult(SUCCESS)         │                                 │
```

### Conflict Case

```
User                            DBManager                         Database
  │                                │                                 │
  │  getVersionForUpdate(history) │                                 │
  │ ─────────────────────────────> │                                 │
  │                                │  getCurrent()                  │
  │                                │ ─────────────────────────────> │
  │                                │ <───────────────────────────── │
  │                                │  (returns v1)                  │
  │ <───────────────────────────── │                                 │
  │  (gets copy of v1)            │                                 │
  │                                │                                 │
  │  [modify the copy]             │                                 │
  │                                │                                 │
  │  storeWithOptimisticLock(obj, │                                 │
  │                              v1)│                                 │
  │ ─────────────────────────────> │                                 │
  │                                │  [check: current is now v2!]   │
  │                                │                                 │
  │                                │  CONFLICT!                     │
  │ <───────────────────────────── │                                 │
  │  StoreResult(CONFLICT,        │                                 │
  │    conflictObject=current)    │                                 │
  │                                │                                 │
  │  [user decides: merge/retry]   │                                 │
```

---

## Lazy Loading Monitoring

### Concept

```
┌─────────────────────────────────────────────────────────────┐
│                    Large Collection                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ items: [■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■] │   │
│  │ Estimated: 10,000 items                              │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Before access:                                             │
│  - Don't load entire collection                            │
│  - Provide iterator or paginated access                    │
│  - Monitor memory usage                                    │
└─────────────────────────────────────────────────────────────┘
```

### Implementation

```java
public class LazyCollectionMonitor {
    private int largeCollectionThreshold = 1000;
    private final Map<String, CollectionInfo> monitoredCollections = new ConcurrentHashMap<>();
    private volatile long totalLoadedItems = 0;
    
    public boolean shouldLazyLoad(String fieldName, int estimatedSize) {
        return estimatedSize > largeCollectionThreshold;
    }
    
    public void notifyItemsLoaded(String fieldName, int count) {
        totalLoadedItems.addAndGet(count);
        checkMemoryPressure();
    }
    
    private void checkMemoryPressure() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();
        
        if (used > max * 0.8) {
            // Trigger lazy loading or warn user
            throw new MemoryPressureException(
                "Memory usage at " + (used * 100 / max) + "%. " +
                "Consider using lazy loading for large collections."
            );
        }
    }
}
```

---

## Multi-User Concurrency

### Thread Safety

```java
public class DBManagerImpl implements DBManager {
    private final Storage storage;
    private final ConcurrentHashMap<Long, TransactionContext> transactions = new ConcurrentHashMap<>();
    private final LazyCollectionMonitor collectionMonitor = new LazyCollectionMonitor();
    
    @Override
    public synchronized void beginTransaction() {
        // Ensure single transaction per thread
        if (transactions.containsKey(Thread.currentThread().getId())) {
            throw new IllegalStateException("Transaction already in progress");
        }
        storage.beginThreadTransaction();
        transactions.put(Thread.currentThread().getId(), 
            new TransactionContext(storage));
    }
    
    @Override
    public synchronized void commit() {
        Long threadId = Thread.currentThread().getId();
        TransactionContext ctx = transactions.remove(threadId);
        if (ctx == null) {
            throw new IllegalStateException("No transaction to commit");
        }
        ctx.commit();
    }
}
```

---

## Usage Examples

### Basic Retrieval and Storage

```java
// Initialize
DBManager dbManager = new DBManagerImpl();
dbManager.initialize(storage);
dbManager.setLargeCollectionThreshold(500);

// Store new object
MyObject obj = new MyObject();
obj.setName("Test");
StoreResult<MyObject> result = dbManager.store(obj);
if (result.getStatus() == StoreResult.Status.SUCCESS) {
    System.out.println("Stored with OID: " + result.getObject().getOid());
}
```

### Optimistic Locking with CVersion

```java
// Get version for update (like checking out a book)
CVersionHistory<MyVersionedObject> history = getHistoryForObject(oid);
VersionedObject<MyVersionedObject> versioned = dbManager.getVersionForUpdate(history);

// User modifies the copy
MyVersionedObject copy = versioned.getObject();
copy.setName("Updated Name");

// Store with optimistic lock
StoreResult<MyVersionedObject> result = dbManager.storeWithOptimisticLock(
    copy, 
    versioned.getVersionId()
);

if (result.getStatus() == StoreResult.Status.SUCCESS) {
    System.out.println("Update successful!");
} else if (result.getStatus() == StoreResult.Status.CONFLICT) {
    // Conflict - another user modified the object
    MyVersionedObject current = result.getConflictObject();
    System.out.println("Conflict! Current version: " + current.getVersionId());
    // User can merge changes or retry
}
```

### Retry Pattern

```java
// Automatic retry on conflict
StoreResult<MyVersionedObject> result = dbManager.storeWithRetry(
    history,
    3,  // max attempts
    (workingCopy) -> {
        // Modify the working copy
        workingCopy.setName("Updated Name");
        workingCopy.incrementCounter();
        return workingCopy;
    }
);

if (result.getStatus() == StoreResult.Status.SUCCESS) {
    System.out.println("Successfully updated after retries");
}
```

### Lazy Loading Large Collections

```java
// Get object with lazy loading info
RetrieveResult<MyObject> result = dbManager.get(oid);

// Check if collections are large
for (CollectionInfo info : result.getLargeCollections()) {
    System.out.println("Collection '" + info.getFieldName() + 
        "' has " + info.getEstimatedSize() + " items");
    if (info.shouldLazyLoad()) {
        // Use iterator instead of loading all
        Iterator<Item> it = object.getItems().iterator();
        while (it.hasNext()) {
            Item item = it.next();
            // Process item
            if (memoryLow()) break;
        }
    }
}
```

---

## Error Handling

| Error | Handling |
|-------|----------|
| Optimistic Lock Conflict | Return conflict object, let user decide |
| Memory Pressure | Throw exception, suggest lazy loading |
| Transaction in Progress | Throw exception, require commit/rollback |
| Object Not Found | Return null or throw exception based on context |

---

## Implementation Notes

1. **Storage Integration**: DBManager wraps Storage, not replaces it
2. **Backward Compatibility**: Works with existing Perst code
3. **Optional Features**: Lazy loading is opt-in via threshold
4. **Thread Safety**: All operations are thread-safe
5. **Transaction Isolation**: Each thread has its own transaction context
