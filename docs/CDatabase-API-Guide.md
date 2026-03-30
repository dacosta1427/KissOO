# CDatabase API Guide

## Overview

CDatabase is the unified API for Perst, providing version control, Lucene full-text search, optimistic locking, and smart index auto-detection in a single class.

**Architecture:**
```
Application → CDatabase → Storage
                ↓
        Lin (current) + Lex (history)
                ↓
           Lucene Index
```

---

## Quick Start

```java
// Initialize
Storage storage = StorageFactory.getInstance().createStorage();
storage.open("mydb.dbs");
CDatabase cdb = CDatabase.instance;
cdb.open(storage, "mydb_index");

// Insert an object
cdb.beginTransaction();
cdb.insert(myObject);
long transId = cdb.commitTransaction();

// Store multiple objects atomically
TransactionContainer tc = cdb.createContainer();
tc.addInsert(newObject);
tc.addUpdate(existingObject);
StoreResult result = cdb.store(tc);

if (result.isSuccess()) {
    // All objects stored
}
```

---

## API Reference

### Initialization

```java
void open(Storage storage, String luceneIndexPath)
void close()
```

### Transaction Control

```java
void beginTransaction()
long commitTransaction()
void rollbackTransaction()
boolean isInTransaction()
```

---

### Object Retrieval

#### getByOid - Get object by OID
```java
<T extends CVersion> T getByOid(long oid)
```
Returns the raw object from storage (may not be latest version after updates).

```java
MyObject obj = cdb.getByOid(12345);
if (obj != null) {
    System.out.println(obj.getName());
}
```

#### getLatestByOid - Get latest version by OID
```java
<T extends CVersion> T getLatestByOid(long oid)
```
Returns the current (latest) version from the version history. Use after updates.

```java
MyObject latest = cdb.getLatestByOid(oid);
// Always returns the most recent version
```

#### getByUuid - Get object by UUID
```java
<T extends CVersion> T getByUuid(String uuid)
```
Uses full-text search to find object by its UUID.

```java
MyObject obj = cdb.getByUuid("abc-123-def");
```

---

### Version Helpers

#### getCurrent - Get current version from history
```java
<T extends CVersion> T getCurrent(CVersionHistory<T> history)
```
Returns the current version from a version history.

```java
CVersionHistory<MyObject> history = obj.getVersionHistory();
MyObject current = cdb.getCurrent(history);
```

#### getVersionForUpdate - Get working copy for update
```java
<T extends CVersion> T getVersionForUpdate(CVersionHistory<T> history)
```
Returns a working copy (clone) ready for modification. Auto-starts transaction if needed.

```java
CVersionHistory<MyObject> history = original.getVersionHistory();
MyObject workingCopy = cdb.getVersionForUpdate(history);
workingCopy.setName("new name");
cdb.commitTransaction();
```

#### getUpdateObject - Get working copy by OID
```java
<T extends CVersion> T getUpdateObject(long oid)
```
Convenience method - gets working copy directly from OID.

```java
MyObject workingCopy = cdb.getUpdateObject(oid);
workingCopy.setValue(42);
cdb.commitTransaction();
```

---

### TransactionContainer - Batch Operations

#### createContainer / createSyncContainer
```java
TransactionContainer createContainer()           // Async Lin writes
TransactionContainer createSyncContainer()       // Sync Lin writes (critical data)
```

#### Automatic OID Assignment
After a successful `store()` call, **all objects in the TransactionContainer automatically have their OIDs assigned**.

```java
// Create container and add new objects (no OIDs yet)
TransactionContainer tc = cdb.createContainer();
Company c1 = new Company("Acme Corp", address1);  // getOid() == 0
Company c2 = new Company("Beta Inc", address2);    // getOid() == 0

tc.addInsert(c1);
tc.addInsert(c2);

StoreResult result = cdb.store(tc);
if (result.isSuccess()) {
    // Objects NOW have OIDs - use them directly!
    c1.getOid();  // e.g., 12345
    c2.getOid();  // e.g., 12346
}
```

**This is critical for the Manager pattern:**

```java
// Manager creates objects, submits, and gets back with OIDs
TransactionContainer tc = cdb.createContainer();
for (String name : namesToCreate) {
    tc.addInsert(new Company(name, address));
}

cdb.store(tc);  // All objects now have OIDs!

// Manager can immediately use the objects - OIDs are set
for (Company c : insertedCompanies) {
    myCache.put(c.getOid(), c);
}
```

**Key points:**
- New objects → OIDs assigned during `store()`
- Updated objects → Working copies already have OIDs
- No need to call `getOid()` on stored objects separately

#### store - Store with early optimistic lock validation
```java
StoreResult store(TransactionContainer container)
```
Performs pre-validation BEFORE acquiring the exclusive lock (fail-fast).

```java
TransactionContainer tc = cdb.createContainer();
tc.addInsert(newObject);
tc.addUpdate(existingObject);
tc.addDelete(oldObject);

StoreResult result = cdb.store(tc);
if (result.isSuccess()) {
    // All stored
} else if (result.isConflict()) {
    // Optimistic lock conflict - re-fetch and retry
    long oid = result.getOid();
    long expected = result.getExpectedVersion();
    long actual = result.getActualVersion();
}
```

**Validation Flow:**
```
store(tc):
  1. For each UPDATE: compare expected vs current version
  2. If any mismatch → return StoreResult.conflict() (no lock acquired)
  3. If all OK → acquire lock, commit, return StoreResult.success()
```

#### update - Convenience single-object update
```java
<T extends CVersion> StoreResult update(long oid, Consumer<T> updater)
```
Handles beginTransaction, working copy, commit automatically.

```java
StoreResult result = cdb.update(oid, obj -> {
    obj.setName("newName");
    obj.setValue(42);
});

if (result.isConflict()) {
    // Handle conflict
}
```

---

### Query Operations

#### find - Query by indexed field
```java
<T extends CVersion> IterableIterator<T> find(Class<T> table, String field, Key key)
```

```java
IterableIterator<MyObject> results = cdb.find(MyObject.class, "name", new Key("John"));
while (results.hasNext()) {
    MyObject obj = results.next();
}
```

#### getRecords - Get all records
```java
<T extends CVersion> IterableIterator<T> getRecords(Class<T> table)
```

```java
IterableIterator<MyObject> all = cdb.getRecords(MyObject.class);
List<MyObject> list = cdb.toList(all);  // Convert to list
```

#### fullTextSearch - Lucene full-text search
```java
FullTextSearchResult[] fullTextSearch(String query, int limit)
```

**Basic usage:**
```java
FullTextSearchResult[] results = cdb.fullTextSearch("laptop", 100);
for (FullTextSearchResult r : results) {
    MyProduct obj = (MyProduct) r.getVersion();
    float score = r.getScore();  // 0.0 - 1.0 relevance
    System.out.println(obj.getName() + " (score: " + score + ")");
}
```

**Query syntax (Lucene):**
```java
// Simple term
cdb.fullTextSearch("laptop", 100);

// AND/OR operators
cdb.fullTextSearch("laptop AND wireless", 100);
cdb.fullTextSearch("phone OR tablet", 100);

// Field-specific (if @FullTextSearchable fields indexed separately)
cdb.fullTextSearch("description:laptop AND name:pro", 100);

// Wildcards
cdb.fullTextSearch("lap*", 100);  // Starts with "lap"
cdb.fullTextSearch("l?ptop", 100); // Single char wildcard

// Phrase search
cdb.fullTextSearch('"wireless keyboard"', 100);

// NOT operator
cdb.fullTextSearch("laptop -cheap", 100);  // Laptop but not cheap
```

**Filtering results by score:**
```java
FullTextSearchResult[] results = cdb.fullTextSearch("laptop", 100);
for (FullTextSearchResult r : results) {
    if (r.getScore() > 0.5) {  // Only relevant matches
        MyProduct obj = (MyProduct) r.getVersion();
        System.out.println(obj.getName());
    }
}
```

---

### Smart Index Management

#### analyzeAndSuggest - Analyze class for index recommendations
```java
<T extends CVersion> Recommendations analyzeAndSuggest(Class<T> clazz)
```

```java
Recommendations recs = cdb.analyzeAndSuggest(MyObject.class);
for (ActionItem action : recs.getActions()) {
    System.out.println(action.getDescription());
    System.out.println(action.getCodeSnippet());
}
```

#### getOptimizationSuggestions - Get dynamic suggestions
```java
List<ActionItem> getOptimizationSuggestions()
```
Returns suggestions based on accumulated query patterns.

#### Query Logging
```java
void setQueryLoggingEnabled(boolean enabled)
boolean isQueryLoggingEnabled()
QueryLogger getQueryLogger()
```

Query logging is automatically enabled and records patterns from `find()`, `getRecords()`, and `fullTextSearch()`.

---

### Memory Management

```java
void registerCollection(Object owner, String fieldName, int estimatedSize)
boolean shouldLazyLoad(String fieldName, int estimatedSize)
void setLargeCollectionThreshold(int threshold)
MemoryStats getMemoryStats()
```

```java
// Register large collection for lazy loading decisions
cdb.registerCollection(myObject, "items", 5000);

// Check if should lazy load
if (cdb.shouldLazyLoad("items", 5000)) {
    // Load on demand
}

// Get memory stats
MemoryStats stats = cdb.getMemoryStats();
System.out.println("Collections: " + stats.getTotalCollections());
```

---

### History/Lin/Lex Management

```java
void flushHistoryBuffer()
int getHistoryBufferSize()
void setHistoryBufferSize(int threshold)
void setHistoryFlushInterval(int seconds)
```

---

## Key Concepts

### Optimistic Locking

Every object update uses optimistic locking. The `store()` method validates versions BEFORE acquiring the lock:

```java
// Inside store():
for (CVersion obj : container.getUpdateList()) {
    expectedVersion = container.getExpectedVersion(obj);
    currentVersion = obj.getVersionHistory().getCurrent().getId();
    if (expectedVersion != currentVersion) {
        return StoreResult.conflict(oid, expectedVersion, currentVersion);
    }
}
// All validated - safe to commit
```

### Working Copies

**NEVER modify retrieved objects directly.** Use working copies:

```java
// WRONG - modifies history directly
MyObject obj = cdb.getByOid(oid);
obj.setName("new");  // Direct modification

// CORRECT - use working copy
cdb.beginTransaction();
MyObject wc = cdb.update(obj);
wc.setName("new");
cdb.commitTransaction();

// OR use convenience method
cdb.update(oid, obj -> obj.setName("new"));
```

### StoreResult

| Method | Description |
|--------|-------------|
| `isSuccess()` | Operation completed |
| `isConflict()` | Optimistic lock conflict |
| `isError()` | Error occurred |
| `getOid()` | Object that had conflict |
| `getExpectedVersion()` | Version you expected |
| `getActualVersion()` | Current version in DB |

---

## Migration from UnifiedDBManager

If upgrading from code that used `UnifiedDBManager`:

| UDBM | CDatabase |
|------|-----------|
| `dbManager.open(storage, index)` | `cdb.open(storage, index)` |
| `dbManager.store(tc)` | `cdb.store(tc)` |
| `dbManager.getByOid(oid)` | `cdb.getByOid(oid)` |
| `dbManager.update(oid, fn)` | `cdb.update(oid, fn)` |
| `dbManager.find(...)` | `cdb.find(...)` |
| `dbManager.searchFullText(q)` | `cdb.fullTextSearch(q, limit)` |
| `dbManager.createContainer()` | `cdb.createContainer()` |
| `dbManager.isInTransaction()` | `cdb.isInTransaction()` |

The main difference: `searchFullText()` → `fullTextSearch(query, limit)` (takes limit parameter).

---

## Thread Safety

- Each thread has its own transaction context (ThreadLocal)
- Multiple threads can use CDatabase simultaneously
- `store()` acquires exclusive lock only after validation passes
- `historyBuffer` operations are synchronized
