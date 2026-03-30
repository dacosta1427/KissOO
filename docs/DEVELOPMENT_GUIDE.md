# Perst Development Guide

## For Future Agents & Developers

This document captures all major changes made to Perst as of March 2026. Use this to understand the current architecture and avoid reinventing work.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [What We Changed](#what-we-changed)
3. [Smart Index Deep Dive](#smart-index-deep-dive)
4. [CDatabase API Reference](#cdatabase-api-reference)
5. [Production Hardening](#production-hardening)
6. [Testing Patterns](#testing-patterns)
7. [Common Pitfalls](#common-pitfalls)

---

## Architecture Overview

### Before (March 2026)

```
Application → UnifiedDBManager → CDatabase → Storage
```

### After (Current)

```
Application → CDatabase → Storage
             ↓
        Lin (current) + Lex (history)
             ↓
        Lucene Index
```

**Key Design Decisions:**
- **Single API**: CDatabase is the only entry point for persistence
- **Thread Safety**: ThreadLocal-based transaction isolation (no synchronization needed)
- **Early Validation**: `store()` validates optimistic locks BEFORE acquiring lock
- **Smart Index**: Automatic field analysis + query pattern tracking

---

## What We Changed

### 1. Removed UnifiedDBManager (v4.1.0)

**Why:** UDBM was a thin wrapper adding minimal value. CDatabase already had ThreadLocal isolation.

**Migration:**
```java
// OLD
UnifiedDBManager dbm = new UnifiedDBManagerImpl();
dbm.open(storage, index);
dbm.store(tc);

// NEW
CDatabase cdb = CDatabase.instance;
cdb.open(storage, index);
cdb.store(tc);
```

**Deleted Files:**
- `src/main/java/org/garret/perst/dbmanager/UnifiedDBManager.java`
- `src/main/java/org/garret/perst/dbmanager/UnifiedDBManagerImpl.java`
- `src/main/java/org/garret/perst/dbmanager/StoreResult.java`
- `src/main/java/org/garret/perst/dbmanager/RetrieveResult.java`
- `src/main/java/org/garret/perst/dbmanager/VersionedObject.java`
- `src/main/java/org/garret/perst/dbmanager/CollectionInfo.java`
- `src/main/java/org/garret/perst/dbmanager/MemoryStats.java`

### 2. Enhanced CDatabase

**New Methods Added:**

| Category | Methods |
|----------|---------|
| OID Lookup | `getByOid()`, `getLatestByOid()`, `getByUuid()` |
| Version | `getCurrent()`, `getUpdateObject()` |
| Transaction | `createContainer()`, `createSyncContainer()`, `isInTransaction()` |
| Store | `store()` (with early validation), `update(oid, Consumer)` |
| Smart Index | `analyzeAndSuggest()`, `autoCreateIndexes()`, `getQueryLogger()` |
| State | `getState()`, `getResourceMetrics()` |
| Memory | `registerCollection()`, `shouldLazyLoad()`, `getMemoryStats()` |

### 3. Production Hardening

**State Machine:**
```java
public enum State { OPEN, CLOSING, CLOSED }
```

**Resource Metrics:**
```java
ResourceMetrics metrics = cdb.getResourceMetrics();
metrics.getActiveTransactions();  // Currently open transactions
metrics.getOpenIterators();       // Currently open iterators
metrics.hasPotentialLeaks();      // Quick leak check
```

### 4. New Result Types (in `continuous` package)

- `StoreResult<T>` - Result of `store()` operations with conflict details
- `MemoryStats` - Memory usage statistics

---

## Smart Index Deep Dive

### How It Works

Smart Index combines **static analysis** (field types, annotations) with **dynamic analysis** (actual query patterns) to recommend indexes.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Smart Index System                          │
│                                                                 │
│  ┌─────────────────────┐     ┌─────────────────────┐           │
│  │ SmartIndexAnalyzer  │     │     QueryLogger      │           │
│  │ (Static Analysis)   │     │ (Dynamic Analysis)   │           │
│  │                     │     │                      │           │
│  │ - Field types       │     │ - find() queries     │           │
│  │ - Annotations       │     │ - fullTextSearch()   │           │
│  │ - Value sampling    │     │ - getRecords()       │           │
│  └──────────┬──────────┘     └──────────┬───────────┘           │
│             │                           │                        │
│             └───────────┬───────────────┘                        │
│                         ▼                                        │
│            ┌────────────────────────┐                           │
│            │ IndexRecommendationEngine │                        │
│            │ (Combines both)           │                        │
│            └───────────┬──────────────┘                         │
│                        ▼                                         │
│              Recommendations                                     │
│              - FieldAnalysis[]                                   │
│              - ActionItem[]                                      │
│              - QueryOptimization[]                               │
└─────────────────────────────────────────────────────────────────┘
```

### Static Analysis (SmartIndexAnalyzer)

Analyzes CVersion classes using:

1. **Annotations:**
   - `@Indexable` → BTree index recommended
   - `@Indexable(unique=true)` → BTree with unique constraint
   - `@Indexable(thick=true)` → ThickIndex for low cardinality
   - `@Indexable(regex=true)` → RegexIndex
   - `@FullTextSearchable` → Lucene index

2. **Field Types:**
   - `String` → SHORT_STRING or LONG_STRING (based on length)
   - `int/long/float/double` → NUMERIC
   - `boolean` → BOOLEAN
   - `Enum` → ENUM
   - `CVersion` → REFERENCE

3. **Value Sampling:**
   - Samples existing objects to detect actual data patterns
   - Identifies sparse vs dense values

### Dynamic Analysis (QueryLogger)

Records all queries made through CDatabase:

```java
// Automatically called by:
cdb.find(class, field, key);           // → Logs EXACT/RANGE/PREFIX/PATTERN
cdb.getRecords(class);                 // → Logs ALL_RECORDS
cdb.fullTextSearch(query, limit);      // → Logs FULLTEXT
```

**QueryStats tracked per field:**
- Total query count
- Average duration
- Query type distribution
- Index presence detection

**Triggers for recommendations:**
- > 100 queries on unindexed field
- Slow query detection (> 100ms average)
- Frequent full-table scans

### Recommendation Types

| Action | When |
|--------|------|
| ADD_INDEX | Field has no index but should |
| CHANGE_INDEX | Current index type is suboptimal |
| ADD_LUCENE | Text field needs full-text search |
| REMOVE_INDEX | Index exists but never used |
| REVIEW | Needs human decision |

### Confidence Levels

| Level | Meaning | Auto-create? |
|-------|---------|--------------|
| HIGH | Strong signal, safe to auto-create | Yes |
| MEDIUM | Good recommendation, review first | No (default) |
| LOW | Weak signal, needs verification | No |

### Auto-Create Safety Features

```java
// Dry run - preview only (DEFAULT)
cdb.autoCreateIndexes(MyClass.class, true);

// Auto-create HIGH confidence only (DEFAULT)
cdb.autoCreateIndexes(MyClass.class, false);

// Auto-create with custom threshold
cdb.autoCreateIndexes(MyClass.class, false, Confidence.MEDIUM);
```

**What it does NOT auto-create:**
- LUCENE indexes (requires `@FullTextSearchable`)
- SPATIAL/MULTIDIMENSIONAL (manual creation)
- Fields already indexed
- Low confidence recommendations

### Query Type Classification

```java
Key key = new Key("value");
// Exact: "value"
// Prefix: "value*"
// Pattern: "val*e?"
// Range: new Key(min, max)
```

---

## CDatabase API Reference

### Initialization

```java
CDatabase cdb = CDatabase.instance;
cdb.open(Storage storage, String luceneIndexPath);
cdb.close();
```

### Transaction Control

```java
cdb.beginTransaction();
long transId = cdb.commitTransaction();
cdb.rollbackTransaction();
boolean inTx = cdb.isInTransaction();
```

### Batch Operations (Recommended)

```java
TransactionContainer tc = cdb.createContainer();
tc.addInsert(newObj);
tc.addUpdate(existingObj);
tc.delete(oldObj);

StoreResult result = cdb.store(tc);  // Early validation
if (result.isConflict()) {
    // Handle conflict with result.getExpectedVersion(), result.getActualVersion()
}
```

### Single Object Operations

```java
// Get object
T obj = cdb.getByOid(oid);
T latest = cdb.getLatestByOid(oid);
T byUuid = cdb.getByUuid(uuid);

// Update with convenience
StoreResult result = cdb.update(oid, obj -> {
    obj.setName("newName");
});
```

### Queries

```java
// Index-based
IterableIterator<T> results = cdb.find(Class, "fieldName", key);

// Full table
IterableIterator<T> all = cdb.getRecords(Class);

// Full-text
FullTextSearchResult[] hits = cdb.fullTextSearch("query", limit);
```

### Smart Index

```java
// Analyze and get recommendations
Recommendations recs = cdb.analyzeAndSuggest(MyClass.class);
recs.getHighPriority();     // HIGH confidence actions
recs.getSummary();          // Human-readable summary

// Auto-create indexes
AutoCreateResult result = cdb.autoCreateIndexes(MyClass.class, true);  // dry run
AutoCreateResult result = cdb.autoCreateIndexes(MyClass.class, false); // actual

// Query logging
cdb.setQueryLoggingEnabled(true);
QueryLogger logger = cdb.getQueryLogger();
```

### State & Monitoring

```java
State state = cdb.getState();  // OPEN, CLOSING, CLOSED
ResourceMetrics metrics = cdb.getResourceMetrics();
MemoryStats memStats = cdb.getMemoryStats();
```

---

## Production Hardening

### State Machine

```
OPEN → CLOSING → CLOSED
```

| State | beginTransaction | store | close |
|-------|-----------------|-------|-------|
| OPEN | OK | OK | Sets to CLOSING |
| CLOSING | No | No | Sets to CLOSED |
| CLOSED | No | No | No-op |

### Resource Leak Detection

```java
// On close, warning is printed if:
// - Active transactions > 0
// - Open iterators > 0

CDatabase.ResourceMetrics metrics = cdb.getResourceMetrics();
if (metrics.hasPotentialLeaks()) {
    System.err.println("WARNING: Closing with leaks: " + metrics);
}
```

### Lin Error Recovery

| Transaction Type | Lin Error Behavior |
|------------------|-------------------|
| `syncToLin=true` (IMNORMAL) | Fatal - throws IOError |
| `syncToLin=false` (default) | Logged, non-fatal, recoverable via .trloca |

---

## Testing Patterns

### Base Test Class

```java
public class MyTest extends AbstractCDatabaseTest {
    // Provides:
    // - cdb: CDatabase instance (fresh per test)
    // - storage: Storage instance
    // - dbFile, indexDir: Unique paths per test
    // - Automatic cleanup in tearDownBase()
}
```

### Test File Location

- `src/test/java/org/garret/perst/continuous/` - CDatabase tests
- `src/test/java/org/garret/perst/smart/` - Smart Index tests
- `src/test/java/org/garret/perst/` - General tests

### Running Tests

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=org.garret.perst.continuous.TestCDatabaseConvenienceMethods

# Pattern match
mvn test -Dtest="org.garret.perst.continuous.TestCDatabase*,org.garret.perst.smart.*"
```

---

## Common Pitfalls

### 1. Modifying Retrieved Objects Directly

**WRONG:**
```java
MyObj obj = cdb.getByOid(oid);
obj.setName("new");  // Modifies history directly!
```

**CORRECT:**
```java
cdb.update(oid, obj -> obj.setName("new"));
// OR
cdb.beginTransaction();
MyObj wc = cdb.getUpdateObject(oid);
wc.setName("new");
cdb.commitTransaction();
```

### 2. Double-Close on Singleton

```java
// Safe - idempotent
cdb.close();
cdb.close();  // No-op
```

### 3. getLatestByOid vs getByOid

```java
// getByOid - returns whatever is at that OID storage location
// (may be stale after updates)
T obj = cdb.getByOid(oid);

// getLatestByOid - always returns current version from history
T latest = cdb.getLatestByOid(oid);  // Usually what you want
```

### 4. TransactionContainer Auto-Capture

```java
TransactionContainer tc = cdb.createContainer();

// This auto-captures the expected version:
tc.addUpdate(obj);  

// This uses explicit version:
tc.addUpdate(obj, expectedVersion);

// The auto-capture uses obj.getVersionHistory().getCurrent()
// at the time addUpdate() is called
```

### 5. Smart Index Dry Run

```java
// Always dry-run first!
AutoCreateResult preview = cdb.autoCreateIndexes(MyClass.class, true);
System.out.println(preview.getSummary());  // Review before actual creation

// Only then actually create
if (preview.getIndexesCreated() > 0) {
    cdb.autoCreateIndexes(MyClass.class, false);
}
```

### 6. Iterator Leak

```java
// WRONG - iterator may not be fully consumed
IterableIterator<T> iter = cdb.getRecords(MyClass.class);
while (iter.hasNext()) {
    if (someCondition) break;  // Iterator not fully consumed
}

// CORRECT - ensure completion
IterableIterator<T> iter = cdb.getRecords(MyClass.class);
try {
    while (iter.hasNext()) {
        T obj = iter.next();
        if (someCondition) break;
    }
} finally {
    // Iterator auto-tracked via TrackedIterator
}
```

---

## Version History

### 4.1.0 (March 2026)

**Breaking Changes:**
- UnifiedDBManager removed
- `searchFullText()` → `fullTextSearch(query, limit)`

**New Features:**
- Smart Index Auto-Detection
- Auto-create indexes
- State machine (OPEN/CLOSING/CLOSED)
- Resource metrics
- Early optimistic validation
- Production hardening

**Bug Fixes:**
- Optimistic lock false conflicts (TransactionContainer.addUpdate)
- Double-close safety
- History buffer null check

---

## File Structure

```
src/main/java/org/garret/perst/
├── CDatabase.java              # Main API (2000+ lines)
├── CVersion.java               # Base class for versioned objects
├── CVersionHistory.java        # Version chain management
├── TransactionContainer.java   # Batch operations
├── Key.java                    # Index key types
├── IterableIterator.java       # Base iterator class
├── continuous/
│   ├── CDatabase.java          # (same as above)
│   ├── StoreResult.java        # Store operation results
│   └── MemoryStats.java        # Memory statistics
└── smart/
    ├── IndexRecommendationEngine.java  # Combines static + dynamic
    ├── SmartIndexAnalyzer.java         # Static field analysis
    ├── QueryLogger.java                # Dynamic query tracking
    ├── Recommendations.java            # Analysis output
    ├── ActionItem.java                 # Specific action
    ├── FieldAnalysis.java              # Field analysis result
    ├── QueryStats.java                 # Query statistics
    ├── QueryOptimization.java          # Query-based recommendation
    ├── Confidence.java                 # Confidence enum
    ├── IndexType.java                  # Index type enum
    ├── FieldType.java                  # Field type enum
    ├── ImpactEstimate.java             # Impact estimation
    └── QueryType.java                  # Query type enum
```

---

## Quick Reference for Future Agents

When working on Perst:

1. **Always use CDatabase** - never bypass it for storage operations
2. **Respect transaction isolation** - ThreadLocal, one transaction per thread
3. **Use working copies** - never modify objects retrieved via getByOid()
4. **Test with AbstractCDatabaseTest** - automatic setup/teardown
5. **Smart Index is opt-in** - auto-create is off by default
6. **Check FUTURE_EVOLUTION.md** - for planned work and priorities
7. **Tags available** - `fallback-pre-optimization` for rollback point

---

*Last updated: March 2026*
*Author: Development team (AI-assisted)*
