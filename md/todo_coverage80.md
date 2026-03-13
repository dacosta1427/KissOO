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

# Todolist: Coverage 80% Per Package

## Phase 1 — `org.garret.perst.fulltext` (64% → 80%)

### Priority #1: FullText coverage gap

- [ ] Task 1.1: Extend `TestFullTextIndex.java` — add NOT operator test (`FullTextQueryUnaryOp`)
- [ ] Task 1.2: Extend `TestFullTextIndex.java` — add `FullTextSearchResult` iteration, sorting, and `merge()` tests
- [ ] Task 1.3: Extend `TestFullTextIndex.java` — add AND NOT / OR NOT compound query tests (`FullTextQueryBinaryOp`)
- [ ] Task 1.4: Extend `TestFullTextIndex.java` — add phrase-match with special chars tests (`FullTextQueryMatchOp`)
- [ ] Task 1.5: Extend `TestFullTextIndex.java` — add edge-case tests: empty result, single word, no match
- [ ] Task 1.6: Run `mvn test` → verify all tests pass and fulltext coverage ≥ 80%

---

## Phase 2 — `org.garret.perst` (27% → 80%)

### Priority #2A: Geometry classes

- [ ] Task 2A.1: Create `GeometryTest.java` — test `Sphere.Point`, `Sphere.Line`, `Sphere.Circle`, `Sphere.Ellipse`
- [ ] Task 2A.2: Create `GeometryTest.java` — test `Sphere.Box`, `Sphere.Polygon` (area, contains, intersects)
- [ ] Task 2A.3: Create `GeometryTest.java` — test `RectangleRn` (N-dim distance, containment, join)
- [ ] Task 2A.4: Create `GeometryTest.java` — test `Point3D` (distance, add, subtract)
- [ ] Task 2A.5: Extend existing geometry in `GeometryTest.java` — test `Rectangle` and `RectangleR2` remaining methods (boundary, overlap, zero-area)
- [ ] Task 2A.6: Run `mvn test` → verify all tests pass

### Priority #2B: L2List and SmallMap

- [ ] Task 2B.1: Create `L2ListTest.java` — test `prepend()`, `append()`, `remove()`, `clear()`, `size()`
- [ ] Task 2B.2: Create `L2ListTest.java` — test forward/reverse iteration, `L2ListIterator`
- [ ] Task 2B.3: Create `L2ListTest.java` — test empty-list edge cases and `deallocateMembers()`
- [ ] Task 2B.4: Create `SmallMapTest.java` — test `put()`, `get()`, `remove()`, `containsKey()`, `containsValue()`
- [ ] Task 2B.5: Create `SmallMapTest.java` — test `keySet()`, `values()`, `entrySet()`, `putAll()`, `clear()`
- [ ] Task 2B.6: Create `SmallMapTest.java` — test key collision (update existing entry), `isEmpty()`
- [ ] Task 2B.7: Run `mvn test` → verify all tests pass

### Priority #2C: QueryProfiler and Aggregators

- [ ] Task 2C.1: Create `QueryProfilerTest.java` — register profiler as storage listener, run queries, verify `queryExecution()` accumulates stats
- [ ] Task 2C.2: Create `QueryProfilerTest.java` — test `QueryInfo.compareTo()` ordering and `dump()` output
- [ ] Task 2C.3: Extend `TestAgg.java` — test `Aggregator.top(n)` / `TopAggregate` (add elements, verify top-N)
- [ ] Task 2C.4: Extend `TestAgg.java` — test `Aggregator.compound(a, b)` / `CompoundAggregate`
- [ ] Task 2C.5: Extend `TestAgg.java` — test `RepeatCountAggregate` with duplicates
- [ ] Task 2C.6: Extend `TestAgg.java` — test `ApproxDistinctCountAggregate` edge cases
- [ ] Task 2C.7: Run `mvn test` → verify all tests pass

### Priority #2D: File implementations

- [ ] Task 2D.1: Create `FileImplementationTest.java` — open Storage with `CompressedReadWriteFile`, CRUD, reopen and verify
- [ ] Task 2D.2: Create `FileImplementationTest.java` — open Storage with `MappedFile`, CRUD, verify
- [ ] Task 2D.3: Create `FileImplementationTest.java` — test `IFileOutputStream` write/flush
- [ ] Task 2D.4: Create `FileImplementationTest.java` — test `CompressDatabase.compress()` on an existing db file
- [ ] Task 2D.5: Run `mvn test` → verify all tests pass

### Priority #2E: Database, Key, PatriciaTrieKey, Version

- [ ] Task 2E.1: Extend `DatabaseTest.java` — test all Table operations (`select`, `update`, `delete`, `getRecordCount()`)
- [ ] Task 2E.2: Extend `DatabaseTest.java` — test metadata access (`getMetaTable()`, table/field lists)
- [ ] Task 2E.3: Extend `DatabaseTest.java` — test transaction `rollback()` with pending changes
- [ ] Task 2E.4: Extend `DatabaseTest.java` — test error paths: duplicate key, null required field
- [ ] Task 2E.5: Extend `DatabaseTest.java` — test index creation on an existing populated table
- [ ] Task 2E.6: Extend `TestPatricia.java` — add `PatriciaTrieKey` factory method tests (bytes, string, inet address)
- [ ] Task 2E.7: Extend `TestVersion.java` — add `VersionHistory` tests (`getLatestVersion()`, `checkout()`, history traversal)
- [ ] Task 2E.8: Add `KeyTest.java` — test all `Key` constructor variants (boolean, short, int, long, float, double, Date, String, composite)
- [ ] Task 2E.9: Run `mvn test` → verify all tests pass

### Priority #2F: Utility / Math / Iterator classes

- [ ] Task 2F.1: Create `CollectionUtilTest.java` — test `PersistentCollection` (`contains()`, `containsAll()`, `toArray()`)
- [ ] Task 2F.2: Create `CollectionUtilTest.java` — test `IterableIterator` (wrapping, `iterator()` returns self)
- [ ] Task 2F.3: Create `CollectionUtilTest.java` — test `ThreadSafeIterator` (concurrent access from two threads)
- [ ] Task 2F.4: Create `CollectionUtilTest.java` — test `ClassFilterIterator` (filter mixed objects by type)
- [ ] Task 2F.5: Create `CollectionUtilTest.java` — test `Projection` (project collection through field path)
- [ ] Task 2F.6: Create `MathUtilTest.java` — test `Euler` methods (gamma, factorial, etc.)
- [ ] Task 2F.7: Create `MathUtilTest.java` — test `FP` floating-point comparison utilities
- [ ] Task 2F.8: Extend `PinnedPersistentTest.java` (or existing test) — test `PinnedPersistent` pin/unpin, pin count
- [ ] Task 2F.9: Extend existing test — test `PersistentResource` shared/exclusive lock, upgrade/downgrade
- [ ] Task 2F.10: Run `mvn test` → verify all tests pass and `org.garret.perst` coverage ≥ 80%

---

## Phase 3 — `org.garret.perst.impl` (41% → 80%)

### ~~Priority #3A: JSQL query nodes~~ — **SKIPPED**

> **Decision**: JSQL deeper tests skipped per user request. Current coverage is sufficient for now.

~~- [ ] Task 3A.1: Extend `TestJSQL.java` — add NOT, nested AND/OR, parentheses grouping tests~~
~~- [ ] Task 3A.2: Extend `TestJSQL.java` — add arithmetic expression tests (`+`, `-`, `*`, `/`, `%` in WHERE)~~
~~- [ ] Task 3A.3: Extend `TestJSQL.java` — add `ORDER BY` multi-field ascending/descending~~
~~- [ ] Task 3A.4: Extend `TestJSQL.java` — add `LIKE`, `MATCHES` string predicates~~
~~- [ ] Task 3A.5: Extend `TestJSQL.java` — add null-field handling~~
~~- [ ] Task 3A.6: Extend `TestJSQLContains.java` — add dynamic list `CONTAINS` tests~~
~~- [ ] Task 3A.7: Extend `TestJsqlJoin.java` — add method invocation in query (e.g., `name.startsWith("A")`)~~
~~- [ ] Task 3A.8: Run `mvn test` → verify all tests pass~~

### Priority #3B: B-tree variants

- [ ] Task 3B.1: Extend `TestCompoundIndex.java` — add `AltBtreeMultiFieldIndex` create/insert/scan/remove tests
- [ ] Task 3B.2: Extend `TestCompoundIndex.java` — add `AltBtreeCompoundIndex` compound key and range tests
- [ ] Task 3B.3: Extend `TestRndIndex.java` — add `RndBtreeMultiFieldIndex` tests
- [ ] Task 3B.4: Extend `TestRndIndex.java` — add `RndBtreeCompoundIndex` tests
- [ ] Task 3B.5: Extend `TestRndIndex.java` — add `RndBtreeFieldIndex` tests covering iterator paths
- [ ] Task 3B.6: Extend `TestIndex.java` / `TestIndex2.java` — add ascending/descending full-scan iterators for AltBtree variants
- [ ] Task 3B.7: Extend `TestBtreeCompoundIndex.java` — add duplicate key handling, large dataset (trigger page splits)
- [ ] Task 3B.8: Run `mvn test` → verify all tests pass

### Priority #3C: R-tree / Spatial index

- [ ] Task 3C.1: Extend `TestRtree.java` — add `RtreeRn` (N-dimensional, N=3 and N=4) insert/search/delete tests
- [ ] Task 3C.2: Extend `TestRtree.java` — add nearest-neighbour (NeighborIterator) for `Rtree`, `RtreeR2`, `RtreeRn`
- [ ] Task 3C.3: Extend `TestRtree.java` — add overlapping/containing/disjoint rectangle search tests
- [ ] Task 3C.4: Extend `TestRtree.java` — add deletion rebalancing tests for `RtreePage` / `RtreeR2Page`
- [ ] Task 3C.5: Run `mvn test` → verify all tests pass

### Priority #3D: XML import/export

- [ ] Task 3D.1: Extend `TestXML.java` — add round-trip test with complex object graph
- [ ] Task 3D.2: Extend `TestXML.java` — add tests for all field types (int, long, float, String, Date, byte[])
- [ ] Task 3D.3: Extend `TestXML.java` — add malformed XML import test (verify exception)
- [ ] Task 3D.4: Extend `TestXML.java` — add large XML stress test (many objects)
- [ ] Task 3D.5: Run `mvn test` → verify all tests pass

### Priority #3E: Persistent collections

- [ ] Task 3E.1: Extend `TestList.java` — add `PersistentListImpl` `subList()` and bidirectional `listIterator()` tests
- [ ] Task 3E.2: Extend `TestList.java` — add `ScalableList` large-insert, `get()`, `remove()`, `subList()` tests
- [ ] Task 3E.3: Extend `TestPersistentMap.java` — add `SubMap` `headMap()`, `tailMap()`, `subMap()`, entry iteration
- [ ] Task 3E.4: Extend `TestLink.java` — add `LinkImpl.SubList` `subList(from,to)`, `size()`, `iterator()` tests
- [ ] Task 3E.5: Extend `TestSet.java` — add `ScalableSet` `add()`, `contains()`, `remove()`, `iterator()` tests
- [ ] Task 3E.6: Run `mvn test` → verify all tests pass

### Priority #3F: WeakHashTable / PinWeakHashTable

- [ ] Task 3F.1: Investigate why `impl.WeakHashTable` shows 0% despite `TestWeakHashTable` running 5 tests
- [ ] Task 3F.2: Extend `TestWeakHashTable.java` — force storage to use `WeakHashTable` via storage property
- [ ] Task 3F.3: Add test for GC pressure removing unpinned entries
- [ ] Task 3F.4: Add `PinWeakHashTable` test — pin entry, apply GC pressure, verify it survives
- [ ] Task 3F.5: Run `mvn test` → verify all tests pass

### Priority #3G: Special file implementations

- [ ] Task 3G.1: Create `FileImplTest.java` — test `MultiFile` (split storage across two files, CRUD, reopen)
- [ ] Task 3G.2: Create `FileImplTest.java` — test `Rc4File` (encrypted storage: correct key reopens, wrong key fails gracefully)
- [ ] Task 3G.3: Run `mvn test` → verify all tests pass

### Priority #3H: BitmapCustomAllocator

- [ ] Task 3H.1: Extend `TestAlloc.java` — create `BitmapCustomAllocator` with custom segment, allocate objects, verify address range
- [ ] Task 3H.2: Extend `TestAlloc.java` — test `BitmapAllocator` branch edges: fill segment, verify next-segment wrap
- [ ] Task 3H.3: Run `mvn test` → verify all tests pass

### Priority #3I: StorageImpl — **Do before Ttree**

- [ ] Task 3I.1: Extend `StorageTest.java` — add backup/restore round-trip test
- [ ] Task 3I.2: Extend `StorageTest.java` — add multi-operation rollback test
- [ ] Task 3I.3: Extend `StorageTest.java` — add `gc()` triggered garbage collection test
- [ ] Task 3I.4: Extend `StorageTest.java` — add `getStatistics()` object count verification
- [ ] Task 3I.5: Run `mvn test` → verify all tests pass

### Priority #3J: ThickIndex

- [ ] Task 3J.1: Extend `TestThickIndex.java` — add `ThickFieldIndex` duplicate-heavy insert/query/remove test
- [ ] Task 3J.2: Extend `TestThickIndex.java` — add large duplicate set test (trigger internal growth)
- [ ] Task 3J.3: Run `mvn test` → verify all tests pass

### Priority #3K: Ttree, TtreePage

- [ ] Task 3K.1: Extend `TestTtree.java` — add insert-until-split test
- [ ] Task 3K.2: Extend `TestTtree.java` — add range query across page boundary test
- [ ] Task 3K.3: Extend `TestTtree.java` — add delete-causing-underflow/merge test
- [ ] Task 3K.4: Run `mvn test` → verify all tests pass

### Priority #3L: CodeGeneratorImpl

- [ ] Task 3L.1: Extend `TestCodeGenerator.java` — add tests for more field types (all primitives + String + Date)
- [ ] Task 3L.2: Extend `TestCodeGenerator.java` — test generated class can be stored and retrieved
- [ ] Task 3L.3: Extend `TestCodeGenerator.java` — test unsupported-type error case
- [ ] Task 3L.4: Run `mvn test` → verify all tests pass

---

## Final Verification

- [ ] Task F.1: Run full suite `mvn test -Dtest="org.garret.perst.*Test*,org.garret.perst.All" -Dsurefire.failIfNoSpecifiedTests=false`
- [ ] Task F.2: Confirm `org.garret.perst.fulltext` ≥ 80% instruction coverage
- [ ] Task F.3: Confirm `org.garret.perst` ≥ 80% instruction coverage
- [ ] Task F.4: Confirm `org.garret.perst.impl` ≥ 80% instruction coverage
- [ ] Task F.5: Commit all new and modified test files

---

## Success Criteria

- [ ] All 660+ tests pass (0 failures, 0 errors)
- [ ] `org.garret.perst.fulltext` instruction coverage ≥ 80%
- [ ] `org.garret.perst` instruction coverage ≥ 80%
- [ ] `org.garret.perst.impl` instruction coverage ≥ 80%
- [ ] No production source code (`src/`) modified

## Rollback Plan

1. Each phase starts with a `git commit` checkpoint
2. To revert a failed phase: `git reset --hard HEAD`
3. Verification: `mvn test -Dtest="org.garret.perst.*Test*,org.garret.perst.All" -Dsurefire.failIfNoSpecifiedTests=false`
4. Impact: only `junit_tests/src/` files are affected; `src/` is never touched
