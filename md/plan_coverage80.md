# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---
Environment:
- Runs on Linux (*.sh scripts) and in Windows (*.cmd scripts)
- Java: OpenJDK 25
- Maven as a build tool, NO Gradle and NO script based compilation
- Maven should always work with the latest library versions
- Enable -Xlint:all -Werror (or at least during CI) to catch deprecations and preview API shifts.

Standard procedure:
After a change has been made to the core Perst code all the junit tests have to be run prior to stating the task as done.

### NEVER comment out or remove a line because that would make the code work. If you want to do this there has to be approval.

---

# Plan: Increase Per-Package Test Coverage to 80%+

## Objective
Bring instruction coverage of every package to ≥ 80% using only JUnit 5 tests in `junit_tests/src/`.

## Baseline (2026-02-18, 660 tests, all passing)

| Package | Instr. | Branch | Classes | Lines |
|---|---|---|---|---|
| `org.garret.perst` | 27% | 24% | 98 | ~27% |
| `org.garret.perst.impl` | 41% | 31% | 284 | ~39% |
| `org.garret.perst.fulltext` | 64% | 63% | 11 | ~65% |
| **Total** | **40%** | **31%** | **393** | **~39%** |

**Target**: every package ≥ 80% instruction coverage.

---

## Package 1 — `org.garret.perst.fulltext` (64% → 80%)

Gap to close: **~16 percentage points**.  
This is the smallest and easiest package (11 classes, ~200 covered lines already).

### Root-cause analysis

| Class | Instr. | Issue |
|---|---|---|
| `FullTextSearchResult` | 6% | Result wrapper almost entirely untested |
| `FullTextQueryUnaryOp` | 0% | NOT operator never exercised |
| `FullTextQueryBinaryOp` | 58% | AND/OR combinations incomplete |
| `FullTextQueryMatchOp` | 61% | Phrase/match variants missing |
| `FullTextSearchHelper` | 77% | A few edge-case paths remain |
| `FullTextSearchHit` | 89% | Minor gaps |
| `FullTextQueryUnaryOp` | 0% | Completely untested |

### Action items

1. **Extend `TestFullTextIndex.java`**
   - Add tests that use the `NOT` operator → exercises `FullTextQueryUnaryOp`
   - Add tests that iterate / sort `FullTextSearchResult` (hits count, rank ordering, `merge()`)
   - Add combined `AND NOT`, `OR NOT` query expressions → `FullTextQueryBinaryOp`
   - Add explicit phrase-match with accents / special chars → `FullTextQueryMatchOp`
   - Add edge cases: empty result, query with no matches, single-word query

---

## Package 2 — `org.garret.perst` (27% → 80%)

Gap to close: **~53 percentage points**.  
The package contains 98 classes. Many are at 0% because they are never instantiated by the existing tests.

### Class groups and actions

#### Group A — Geometry classes (Sphere.*, Rectangle*, RectangleRn, Point3D)

All at 0% or < 30%.  
**New file**: `GeometryTest.java`

| Class | Current |
|---|---|
| `Sphere.Point` | 0% |
| `Sphere.Line` | 0% |
| `Sphere.Circle` | 0% |
| `Sphere.Ellipse` | 0% |
| `Sphere.Box` | 0% |
| `Sphere.Polygon` | 0% |
| `RectangleRn` | 0% |
| `Point3D` | 0% |
| `Rectangle` | 28% |
| `RectangleR2` | 48% |

Tests needed:
- Construct each geometry type with typical values
- Call all `area()`, `distance()`, `contains()`, `intersects()`, `join()` methods
- Test boundary conditions (zero-area, touching, overlapping)
- Test `RectangleRn` N-dimensional distance / containment

#### Group B — Doubly-linked list (L2List, L2ListElem)

| Class | Current |
|---|---|
| `L2List` | 0% |
| `L2List.L2ListIterator` | 0% |

**New file**: `L2ListTest.java`

Tests needed:
- `prepend()`, `append()`, `remove()`, `clear()`
- Forward and reverse iteration
- `size()` after mutations
- `deallocateMembers()` (with persistent elements)
- Empty-list edge cases

#### Group C — SmallMap

| Class | Current |
|---|---|
| `SmallMap` | 22% |
| `SmallMap.Pair` | 29% |
| `SmallMap.EntrySet` | 22% |
| `SmallMap.ArrayIterator` | 60% |

**New file**: `SmallMapTest.java`

Tests needed:
- `put()`, `get()`, `remove()`, `containsKey()`, `containsValue()`
- `keySet()`, `values()`, `entrySet()` iteration
- Replace existing entry (key collision)
- `clear()` and `isEmpty()`
- `putAll()` from another map

#### Group D — QueryProfiler

| Class | Current |
|---|---|
| `QueryProfiler` | 0% |
| `QueryProfiler.QueryInfo` | 0% |

**Extend `QueryTest.java`** or create `QueryProfilerTest.java`

Tests needed:
- Register `QueryProfiler` as storage listener
- Run queries so `queryExecution()` is called
- Call `dump()` and verify sorting by total time
- Verify `QueryInfo.compareTo()` ordering
- Multiple queries, verify aggregated `totalTime` and `count`

#### Group E — Database (53% → 80%)

| Class | Current |
|---|---|
| `Database` | 53% |
| `Database.Table` | 57% |
| `Database.Metadata` | 40% |

**Extend `DatabaseTest.java`**

Tests needed:
- Test all Table operations: `select`, `update`, `delete`, `getRecordCount()`
- Test metadata access: `getMetaTable()`, table list, field list
- Test transactions: `beginTransaction()`, `commit()`, `rollback()`
- Test error paths: duplicate key insert, null field access
- Test index creation on existing table
- Test `getVersion()`, `open()` / `close()` lifecycle

#### Group F — File implementations — **PARTIALLY SKIPPED**

| Class | Current | Status |
|---|---|---|
| `CompressedReadWriteFile` | 0% | To be tested |
| `CompressedFile` | 0% | **SKIPPED** - read-only format, requires external utility |
| `CompressedReadWriteFile.PageMap` | 0% | To be tested |
| `CompressedReadWriteFile.PageMap.PageMapIterator` | 0% | To be tested |
| `MappedFile` | 0% | To be tested |
| `IFileOutputStream` | 0% | To be tested |
| `CompressDatabase` | 0% | To be tested |

> **Decision**: `CompressedFile` is SKIPPED - it's a read-only compressed file format that requires an external utility workflow to create. Testing would need pre-compressed database files which is outside the scope of unit tests.

**New file**: `FileImplementationTest.java`

Tests needed:
- Open a `Storage` with `CompressedReadWriteFile` as the underlying file (via `StorageFactory`)
- Perform typical CRUD, verify data is readable after reopen
- Open a `Storage` with `MappedFile`
- Test `IFileOutputStream` write/flush
- Run `CompressDatabase.compress()` on an existing database file

#### Group G — Aggregator (0–74%)

| Class | Current |
|---|---|
| `Aggregator.TopAggregate` | 0% |
| `Aggregator.CompoundAggregate` | 0% |
| `Aggregator.RepeatCountAggregate` | 44% |
| `Aggregator.ApproxDistinctCountAggregate` | 74% |
| `Aggregator` | 54% |

**Extend `TestAgg.java`**

Tests needed:
- `Aggregator.top(n)` — TopAggregate: add elements, verify top-N results
- `Aggregator.compound(a, b)` — CompoundAggregate: combine two aggregates, verify both update
- `RepeatCountAggregate`: add duplicate values, verify repeat count
- `ApproxDistinctCountAggregate`: add many values, verify approximate distinct count

#### Group H — Collections / Iterators (low coverage)

| Class | Current |
|---|---|
| `PersistentCollection` | 14% |
| `IterableIterator` | 23% |
| `ThreadSafeIterator` | 0% |
| `ClassFilterIterator` | 84% |
| `Projection` | 0% |
| `IteratorWrapper` | — |

**New file**: `CollectionUtilTest.java`

Tests needed:
- `PersistentCollection` — subclass it, verify `contains()`, `containsAll()`, `toArray()`
- `IterableIterator` — wrap a list iterator, call `iterator()` on itself
- `ThreadSafeIterator` — wrap an iterator, use from two threads simultaneously
- `Projection` — project a typed collection through a field path
- `ClassFilterIterator` — filter mixed persistent objects by class

#### Group I — Key, PatriciaTrieKey, VersionHistory, Version

| Class | Current |
|---|---|
| `Key` | 48% |
| `PatriciaTrieKey` | 18% |
| `VersionHistory` | 16% |
| `Version` | 54% |

**Extend `TestPatricia.java`**: exercise all `PatriciaTrieKey` factory methods (from bytes, string, inet address).  
**Extend `TestVersion.java`**: exercise `VersionHistory` — `getLatestVersion()`, `checkout()`, history traversal.  
**Extend existing Key tests**: constructor variants for all field types (boolean, short, int, long, float, double, date, String, composite).

#### Group J — Utility / Math (all 0%)

| Class | Current |
|---|---|
| `Euler` | 0% |
| `FP` | 0% |
| `PinnedPersistent` | 77% |
| `PersistentResource` | 45% |

**New file**: `MathUtilTest.java`

Tests needed:
- `Euler` gamma / factorial methods
- `FP` floating-point comparison utilities
- `PinnedPersistent` — pin, unpin, verify pin count behaviour
- `PersistentResource` — shared lock / exclusive lock, upgrade/downgrade

---

## Package 3 — `org.garret.perst.impl` (41% → 80%)

Gap to close: **~39 percentage points**.  
This is the largest package with 284 classes. Many are B-tree / R-tree implementation variants.

### Class groups and actions

#### Group A — JSQL Query Nodes (very low coverage) — **SKIPPED**

| Class | Current |
|---|---|
| `UnaryOpNode` | 4% |
| `BinOpNode` | 10% |
| `ContainsNode` | 11% |
| `InvokeNode` | 20% |
| `InvokeAnyNode` | 0% |
| `LoadAnyNode` | 22% |
| `OrderNode` | 22% |
| `CompareNode` | 25% |
| `GetAtNode` | 0% |

> **Decision**: JSQL deeper tests skipped per user request. Current coverage is sufficient for now.

~~**Extend `TestJSQL.java`, `TestJSQLContains.java`, `TestJsqlJoin.java`**~~

~~Tests needed:~~
~~- Complex compound WHERE with `NOT`, `AND`, `OR`, nested parentheses~~
~~- Arithmetic expressions (`+`, `-`, `*`, `/`, `%`) in WHERE clause~~
~~- `CONTAINS` with dynamic list~~
~~- Method invocation in query (e.g., `name.startsWith("A")`)~~
~~- `ORDER BY` multi-field, ascending and descending~~
~~- `LIKE`, `MATCHES` string predicates~~
~~- Null field handling~~

#### Group B — B-tree variants (incomplete coverage)

| Class | Current |
|---|---|
| `BtreeFieldIndex` | 28% |
| `AltBtreeFieldIndex` | 15% |
| `AltBtreeMultiFieldIndex` | 0% |
| `BtreeCompoundIndex` | 32% |
| `BtreeMultiFieldIndex` | 31% |
| `RndBtree` | 28% |
| `RndBtreeFieldIndex` | 14% |
| `RndBtreeMultiFieldIndex` | 0% |
| `RndBtreeCompoundIndex` | 0% |
| `AltBtreeCompoundIndex` | 0% |
| `RndBtree.BtreeSelectionIterator` | 17% |
| `AltBtree` | 45% |
| `Btree` | 46% |

**Extend `TestCompoundIndex.java`, `TestRndIndex.java`, `TestIndex.java`, `TestIndex2.java`**  
**Extend `TestBtreeCompoundIndex.java`**

Tests needed:
- `AltBtreeMultiFieldIndex`: create, insert, range scan, remove
- `RndBtreeMultiFieldIndex` / `RndBtreeCompoundIndex`: create, insert, random access by key, iterate
- `AltBtreeCompoundIndex`: compound key insert, range query
- Full iterator coverage: ascending, descending, range from/to
- Duplicate key handling across all variants
- Large dataset (trigger page splits)

#### Group C — R-tree spatial index (low / 0%)

| Class | Current |
|---|---|
| `RtreePage` | 23% |
| `RtreeR2Page` | 18% |
| `RtreeRnPage` | 0% |
| `RtreeRn` | 0% |
| `RtreeRn.RtreeIterator` | 0% |
| `RtreeRn.NeighborIterator` | 0% |
| `Rtree.NeighborIterator` | 18% |
| `RtreeR2.NeighborIterator` | 74% |

**Extend `TestRtree.java`**  
**New sections**: `RtreeRn` (N-dimensional) tests, neighbour-iterator tests

Tests needed:
- `RtreeRn` insert / search for N=3 and N=4 dimensions
- Neighbour search (nearest-K) for `Rtree`, `RtreeR2`, `RtreeRn`
- Search with overlapping, containing, and disjoint rectangles
- Deletion and rebalancing under `RtreePage` / `RtreeR2Page`

#### Group D — XML Import / Export (32–36%)

| Class | Current |
|---|---|
| `XMLImporter` | 32% |
| `XMLExporter` | 36% |
| `XMLImporter.XMLScanner` | 53% |
| `XMLImporter.XMLElement` | 77% |

**Extend `TestXML.java`**

Tests needed:
- Round-trip: export a complex graph to XML, wipe the DB, import, verify identity
- Import XML with all supported field types (int, long, float, String, Date, byte[])
- Malformed XML — verify exception is thrown
- Very large XML (stress page scanner)

#### Group E — Persistent collections (low coverage)

| Class | Current |
|---|---|
| `PersistentListImpl` | 30% |
| `PersistentMapImpl` | 39% |
| `PersistentMapImpl.SubMap` | 11% |
| `LinkImpl` | 41% |
| `LinkImpl.SubList` | 0% |
| `ScalableList` | 0% |
| `ScalableSet` | 36% |
| `SubList` | 0% |

**Extend `TestList.java`, `TestPersistentMap.java`, `TestLink.java`**

Tests needed:
- `PersistentListImpl`: `add()`, `set()`, `remove()`, `subList()`, `listIterator()` (forward and backward)
- `PersistentMapImpl.SubMap`: `headMap()`, `tailMap()`, `subMap()`, entry iteration
- `LinkImpl.SubList`: `subList(from, to)`, `size()`, `iterator()`
- `ScalableList`: large insertions, `get()`, `remove()`, `subList()`
- `ScalableSet`: `add()`, `contains()`, `remove()`, `iterator()`

#### Group F — WeakHashTable / PinWeakHashTable (0%)

> **Note**: `TestWeakHashTable.java` exists and ran 5 tests, but `impl.WeakHashTable` still shows 0%.  
> This is because Perst uses `StrongHashTable` or `OidHashTable` internally by default, not `WeakHashTable`.  
> The same applies to `PinWeakHashTable`.

**New file or extend `TestWeakHashTable.java`**

Tests needed:
- Force the storage to use `WeakHashTable` via `StorageFactory` / storage property
- Test that entries are GC'd after strong references are released
- `PinWeakHashTable`: pin an entry, confirm it survives GC pressure

#### Group G — Replication classes (0%) — **DEFERRED**

| Class | Reason |
|---|---|
| `ReplicationMasterFile` | Requires live TCP sockets |
| `AsyncReplicationMasterFile` | Same |
| `ReplicationSlaveStorageImpl` | Requires slave startup, network |
| `ReplicationMasterStorageImpl` | Same |
| `ReplicationDynamicSlaveStorageImpl` | Same |
| `ReplicationStaticSlaveStorageImpl` | Same |

**Decision**: Defer replication tests to a separate plan. These classes require a live master/slave network setup which is out of scope for unit tests without a mock framework. Mocking `Socket` / `ServerSocket` could be a future plan.

#### Group H — Special file implementations (0%)

| Class | Current |
|---|---|
| `MultiFile` | 0% |
| `Rc4File` | 0% |

**New file**: `FileImplTest.java`

Tests needed:
- `MultiFile`: open a storage split across two files, insert data, verify reads after reopen
- `Rc4File`: open an encrypted storage with a key, insert data, reopen with correct key, verify; try incorrect key

#### Group I — Bitmap / Allocator

| Class | Current |
|---|---|
| `BitmapCustomAllocator` | 0% |
| `BitmapAllocator` | 93% (branch 0% — branch edge cases) |

**Extend `TestAlloc.java`**

Tests needed:
- `BitmapCustomAllocator`: create with custom segment, allocate objects, verify allocations are in custom range
- `BitmapAllocator` branch edges: allocate until full segment, verify wrap-around / next segment

#### Group J — StorageImpl (51% → 80%) — **PRIORITY: Do before Ttree**

**Extend `StorageTest.java`**

Tests needed:
- Backup and restore
- Transaction rollback across multiple operations
- Multi-open (open already-open db → exception)
- `setGcThreshold()`, `gc()` triggered GC
- `getStatistics()` — verify object counts after inserts
- Error paths: open non-existent file, corrupt header

#### Group K — CodeGeneratorImpl (13%)

**Extend `TestCodeGenerator.java`**

Tests needed:
- Generate code for more field types
- Verify generated class compiles and can be stored
- Error case: field with unsupported type

#### Group L — ThickIndex, ThickFieldIndex (32%, 18%)

**Extend `TestThickIndex.java`**

Tests needed:
- `ThickFieldIndex`: create, insert duplicates, query duplicates, remove all, verify empty
- Large duplicate set (triggers internal structure growth)

#### Group M — Ttree, TtreePage (49%, 28%)

**Extend `TestTtree.java`**

Tests needed:
- Insert until page splits occur
- Range queries across page boundaries
- Delete causing underflow / merge

---

## Classes Excluded from 80% Target

The following classes are excluded because they are **untestable without external infrastructure or are implementation internals**:

| Class | Reason |
|---|---|
| `ReplicationMasterFile` | Requires network |
| `AsyncReplicationMasterFile` | Requires network |
| `ReplicationSlaveStorageImpl` | Requires network |
| `ReplicationMasterStorageImpl` | Requires network |
| `ReplicationDynamicSlaveStorageImpl` | Requires network |
| `ReplicationStaticSlaveStorageImpl` | Requires network |
| `PersistentListImpl.ListIntermediatePage` | Internal page struct |
| `PersistentMapImpl.SubMap.new AbstractSet(){…}` | Anonymous inner class |
| `InvokeAnyNode` | Requires reflective invocation path not triggered by normal JSQL |
| `GetAtNode` | Internal array-access node |
| `CompressedFile` | Read-only compressed file format - requires external utility workflow |

---

## Execution Order (by risk / value)

> **Note**: JSQL deeper tests (3A) are SKIPPED per user request. StorageImpl (3I) should be done BEFORE Ttree.

| Phase | Package | Focus | New Files | Extended Files |
|---|---|---|---|---|
| 1 | `fulltext` | FullTextQuery*, SearchResult | — | `TestFullTextIndex.java` |
| 2A | `perst` | Geometry classes | `GeometryTest.java` | — |
| 2B | `perst` | L2List, SmallMap | `L2ListTest.java`, `SmallMapTest.java` | — |
| 2C | `perst` | QueryProfiler, Aggregators | `QueryProfilerTest.java` | `TestAgg.java` |
| 2D | `perst` | File impls | `FileImplementationTest.java` | — |
| 2E | `perst` | Database, Key, Version | — | `DatabaseTest.java`, `TestVersion.java`, `TestPatricia.java` |
| 2F | `perst` | Utilities, Collections | `CollectionUtilTest.java`, `MathUtilTest.java` | — |
| ~~3A~~ | ~~`impl`~~ | ~~JSQL nodes~~ | — | ~~SKIPPED~~ |
| 3B | `impl` | B-tree variants | — | `TestCompoundIndex.java`, `TestRndIndex.java`, `TestIndex.java`, `TestBtreeCompoundIndex.java` |
| 3C | `impl` | R-tree, Neighbour | — | `TestRtree.java` |
| 3D | `impl` | XML | — | `TestXML.java` |
| 3E | `impl` | Collections | — | `TestList.java`, `TestPersistentMap.java`, `TestLink.java` |
| 3F | `impl` | Weak/Pin tables | `FileImplTest.java` | `TestWeakHashTable.java` |
| 3G | `impl` | File impls (Rc4, Multi) | `FileImplTest.java` | — |
| 3H | `impl` | BitmapCustomAllocator | — | `TestAlloc.java` |
| 3I | `impl` | **StorageImpl** (priority) | — | `StorageTest.java` |
| 3J | `impl` | ThickIndex | — | `TestThickIndex.java` |
| 3K | `impl` | Ttree, TtreePage | — | `TestTtree.java` |
| 3L | `impl` | CodeGeneratorImpl | — | `TestCodeGenerator.java` |

---

## Quick-Start Commands

```bash
# Baseline run (junit tests only)
cd /home/dacosta/Projects/oodb-testcoverage
mvn test -Dtest="org.garret.perst.*Test*,org.garret.perst.All" -Dsurefire.failIfNoSpecifiedTests=false

# View coverage report
xdg-open target/site/jacoco/index.html

# Run single test class
mvn test -Dtest="GeometryTest"
```

---

## Rollback Plan

| Element | Detail |
|---|---|
| Checkpoint | `git commit` before starting each phase |
| Revert | `git reset --hard HEAD` |
| Verify | `mvn test -Dtest="org.garret.perst.*Test*,org.garret.perst.All" -Dsurefire.failIfNoSpecifiedTests=false` |
| Impact | Only `junit_tests/src/` files are changed; source code is untouched |

---

## Success Criteria

- [ ] `org.garret.perst.fulltext` instruction coverage ≥ 80%
- [ ] `org.garret.perst` instruction coverage ≥ 80%
- [ ] `org.garret.perst.impl` instruction coverage ≥ 80%
- [ ] All existing 660 tests continue to pass
- [ ] No source code changes (tests only)
