# workMEMORY.md - Project Context & Notes

**IMPORTANT:** This file must be updated CONTINUOUSLY during a session.

**Last Updated:** 2026-03-19

---

## Core Principle

**⚠️ FUNCTIONALLITY ALWAYS TRUMPS FILES ⚠️**

CDatabase handles ALL storage operations automatically. **Never create wrappers or extra services.**

---

## Current Project State

**Project:** KissOO - Perst OODB Integration
**Focus:** CDatabase usage

**Status:** ✅ CORRECT

---

## CDatabase Does Everything

| Feature | Managed By |
|---------|------------|
| Versioning | CDatabase (via `CVersion`) |
| Indexing | CDatabase (via `@Indexable`) |
| Full-text search | CDatabase (via `@FullTextSearchable`) |
| History/Lex | **CDatabase** (automatic) |
| Segment management | CDatabase |

---

## What Was Done Today

### ❌ Deleted (violated principle)

1. `ManagedCDatabase.java` - wrapper to intercept insert()
2. `HistoryIndexManager.java` - standalone service for "Lex"
3. All "Lin/Lex Split" complexity

### ✅ Corrected

1. `PerstStorageManager.java` - minimal lifecycle only (init, get, close)
2. `docs/ChangeNote-CDatabaseLuceneArchitecture.md` - simplified

---

## Correct Architecture

```
Application Code
       ↓
PerstStorageManager.getDatabase() → CDatabase
       ↓
CDatabase handles EVERYTHING:
- insert() → versioning
- @Indexable → indexing
- @FullTextSearchable → full-text
- History/Lex → automatic
```

---

## The Only "Fix" Needed

Segment explosion → call periodically:

```java
PerstStorageManager.getDatabase().optimizeFullTextIndex();
```

---

## Files

| File | Status |
|------|--------|
| `PerstStorageManager.java` | ✅ Minimal - just init/get/close |
| `ManagedCDatabase.java` | ❌ Deleted |
| `HistoryIndexManager.java` | ❌ Deleted |
| `docs/ChangeNote-CDatabaseLuceneArchitecture.md` | ✅ Simplified |

---

## References

- `docs/PerstCDatabaseFindings.md` - CDatabase usage patterns
- `docs/PerstIntegration.md` - Integration guide

---

## Session 2026-03-19 - Corrected

**Actions:**
1. Deleted `ManagedCDatabase.java` and `HistoryIndexManager.java`
2. Simplified `PerstStorageManager.java` to minimal lifecycle
3. Updated docs

**Key Lesson:** CDatabase is NOT wrapped or extended. Just use it directly.

---

## Session 2026-03-21 - Perst JAR Upgrade

### Upgrade: perst-dcg-4.0.0.jar → perst-dcg-4.0.1.jar

**Annotations:** Identical in both versions — `FullTextSearchable` and `Indexable` work fine in 4.0.0.

**Real improvements in 4.0.1:**
| Feature | Old (4.0.0) | New (4.0.1) |
|---------|-------------|-------------|
| `FullTextSearchable` | ✅ Works | ✅ Identical |
| `Indexable` | ✅ Works | ✅ Identical |
| `CDatabase.flushHistory()` | ❌ | ✅ |
| `CDatabase.getHistoryIndexPath()` | ❌ | ✅ |
| `CDatabase.setHistoryBufferSize(int)` | ❌ | ✅ |
| `CDatabase.setHistoryFlushInterval(int)` | ❌ | ✅ |
| `CDatabase.flushHistoryBufferImmediately()` | ❌ | ✅ |
| `TransactionContainer` | ❌ | ✅ New class |
| `CVersion.getUuid()` | ❌ | ✅ |

**Current Code Compatibility:** ✅ Already using correct imports
```java
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
```

**Actions:**
1. Inspected both JARs via `javap` — annotations identical
2. Verified 4.0.1 adds History/Lex APIs, TransactionContainer, UUID support
3. Updated build config to use 4.0.1
4. Deployed 4.0.1 to Tomcat and exploded lib

**Lucene Index Path:** Already fixed in `feature/cdatabase-lucene-fix` branch (code issue, not JAR)

---

## Session 2026-03-21 - Thread Safety Fix

**Fixed:** Static HashMap → ConcurrentHashMap in domain classes

**Files modified:**
- `src/main/precompiled/mycompany/domain/Actor.java` - lines 43-44
- `src/main/precompiled/mycompany/domain/PerstUser.java` - lines 59-61

**Changes:**
```java
// BEFORE (not thread-safe)
private static Map<Integer, Actor> userIdIndex = new HashMap<>();
private static Map<String, Actor> uuidIndex = new HashMap<>();

// AFTER (thread-safe)
private static ConcurrentMap<Integer, Actor> userIdIndex = new ConcurrentHashMap<>();
private static ConcurrentMap<String, Actor> uuidIndex = new ConcurrentHashMap<>();
```

**Reason:** Tomcat runs multiple request threads simultaneously. Concurrent HashMap access causes:
- ConcurrentModificationException
- Data corruption under load

---

## Session 2026-03-21 - Source Recovery

**Extracted** 7 source files from compiled `.class` files using `javap -p`:

| File | Package | Methods |
|------|---------|---------|
| `BaseManager.java` | mycompany.database | 15 - abstract CRUD with permission checks |
| `ActorManager.java` | mycompany.database | 14 - Actor CRUD operations |
| `PerstUserManager.java` | mycompany.database | 19 - User CRUD + auth |
| `PhoneManager.java` | mycompany.database | 6 - Phone CRUD |
| `BenchmarkDataManager.java` | mycompany.database | 12 - Benchmark operations |
| `PerstHelper.java` | mycompany.database | 26 - Perst facade |
| `PerstConnection.java` | mycompany.database | 1 - Connection wrapper |

**NOT created (unnecessary):**
- `CDatabaseRoot.java` - Perst 4.0.1 CDatabase handles indexing via `@Indexable` automatically
- `PerstContext.java` - Same, CDatabase manages everything

**Next:** Task 3 - Migrate to UnifiedDBManager API

---

## Session 2026-03-21 - UnifiedDBManager Migration

**Migrated** `PerstStorageManager` to use UnifiedDBManager from Perst 4.0.1.

**New convenience methods for Managers:**

| Method | Purpose |
|--------|---------|
| `find(Class, field, String)` | Find by indexed string field |
| `find(Class, field, int)` | Find by indexed int field |
| `getAll(Class)` | Get all records |
| `getByOid(Class, long)` | Get by internal OID |
| `createContainer()` | Create TransactionContainer |
| `createSyncContainer()` | Create sync container (critical ops) |
| `store(TransactionContainer)` | Atomic store with optimistic locking |
| `insert(obj)` | Simple insert |
| `update(obj)` | Simple update with optimistic locking |
| `delete(obj)` | Simple delete |
| `flushHistory()` | Flush history buffer |
| `isAvailable()` | Check if Perst is ready |

**Benefits:**
- Optimistic locking built-in (version checking on updates)
- Atomic batch operations via TransactionContainer
- Automatic versioning via CVersion
- Lin/Lex history management
- Thread-safe

**Architecture:**
```
Manager → PerstStorageManager.find/update/insert → UnifiedDBManager → CDatabase
```
