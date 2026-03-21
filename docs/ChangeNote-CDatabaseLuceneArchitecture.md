# Perst CDatabase Integration - Final Architecture

## Core Principle

**⚠️ FUNCTIONALLITY ALWAYS TRUMPS FILES ⚠️**

CDatabase handles ALL storage operations automatically.

## What CDatabase Does

| Feature | How |
|---------|-----|
| Versioning | Via `CVersion` base class |
| Indexing | Via `@Indexable` annotations |
| Full-text search | Via `@FullTextSearchable` |
| History/Lex | **Automatic** - all managed internally |

## Usage

```java
// Initialize
PerstStorageManager.initialize();

// Get database
CDatabase db = PerstStorageManager.getDatabase();

// Store objects - that's it!
db.beginTransaction();
db.insert(myObject);
db.commitTransaction();

// Query
db.find(Actor.class, "uuid", uuid);
db.getRecords(Actor.class);
db.fullTextSearch("query", 100);

// Periodic optimization (fixes segment explosion)
db.optimizeFullTextIndex();
```

## What NOT To Do

- ❌ **NO wrapper classes** around CDatabase
- ❌ **NO separate Lex index** - CDatabase handles history
- ❌ **NO manual Lucene Document creation**
- ❌ **NO HistoryIndexManager** - unnecessary complexity

## The Only "Fix" Needed

The only issue with CDatabase is **segment explosion**. The fix:

```java
// Schedule this to run periodically (e.g., nightly or every N transactions)
db.optimizeFullTextIndex();
```

## Files

| File | Purpose |
|------|---------|
| `PerstStorageManager.java` | Minimal lifecycle management (init, get, close) |
| `PerstConfig.java` | Configuration |

---

*2026-03-19 - Corrected architecture*
