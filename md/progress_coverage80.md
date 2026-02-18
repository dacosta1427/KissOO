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

# Progress: Coverage 80% Per Package

## Project Status
- **Start Date:** 2026-02-18
- **Current Phase:** Planning complete — not yet started
- **Overall Progress:** 0% complete

## Baseline Measurements (2026-02-18)

| Package | Instr. Cov. | Branch Cov. | Classes | Tests |
|---|---|---|---|---|
| `org.garret.perst` | 27% | 24% | 98 | — |
| `org.garret.perst.impl` | 41% | 31% | 284 | — |
| `org.garret.perst.fulltext` | 64% | 63% | 11 | — |
| **Total** | **40%** | **31%** | **393** | **660** |

---

## Task Progress

---

### Phase 1 — `org.garret.perst.fulltext` (64% → 80%)

#### Task 1.1: Extend TestFullTextIndex — NOT operator
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `FullTextQueryUnaryOp` instruction coverage > 0%, fulltext package moves toward 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Add query using `NOT` keyword to fulltext query string

#### Task 1.2: Extend TestFullTextIndex — FullTextSearchResult
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `FullTextSearchResult` coverage rises from 6% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Test `getHits()`, iteration, rank ordering, `merge()` method

#### Task 1.3: Extend TestFullTextIndex — AND NOT / OR NOT
- **Status:** pending
- **Priority:** High
- **Dependencies:** 1.1
- **Owner:** —
- **Success criteria:** `FullTextQueryBinaryOp` rises from 58% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Combine NOT with AND/OR compound queries

#### Task 1.4: Extend TestFullTextIndex — phrase/special chars
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `FullTextQueryMatchOp` rises from 61% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Use accented characters and multi-word phrases

#### Task 1.5: Extend TestFullTextIndex — edge cases
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `FullTextSearchHelper` rises from 77% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Empty result, single-word query, query with no matches

#### Task 1.6: Verify fulltext coverage ≥ 80%
- **Status:** pending
- **Priority:** High
- **Dependencies:** 1.1–1.5
- **Owner:** —
- **Success criteria:** `mvn test` passes; JaCoCo reports fulltext ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Run `mvn test`, check `target/site/jacoco/org.garret.perst.fulltext/index.html`

---

### Phase 2A — Geometry classes

#### Task 2A.1–2A.5: Create GeometryTest.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** All Sphere.* and RectangleRn at > 80%; Rectangle / RectangleR2 at > 80%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** Sphere.java is 806 lines — large but self-contained pure Java geometry

#### Task 2A.6: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 2A.1–2A.5
- **Owner:** —
- **Success criteria:** `mvn test` passes with 0 failures
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 2B — L2List and SmallMap

#### Task 2B.1–2B.3: Create L2ListTest.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `L2List` and `L2List.L2ListIterator` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** L2List is a doubly-linked list backed by persistent elements; needs a Storage to run deallocateMembers test

#### Task 2B.4–2B.6: Create SmallMapTest.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `SmallMap`, `SmallMap.Pair`, `SmallMap.EntrySet`, `SmallMap.ArrayIterator` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** SmallMap is pure Java (no persistence layer needed for most tests)

#### Task 2B.7: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 2B.1–2B.6
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 2C — QueryProfiler and Aggregators

#### Task 2C.1–2C.2: Create QueryProfilerTest.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `QueryProfiler` and `QueryProfiler.QueryInfo` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Register profiler as StorageListener before executing queries

#### Task 2C.3–2C.6: Extend TestAgg.java — missing aggregators
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `Aggregator.TopAggregate` and `Aggregator.CompoundAggregate` coverage ≥ 80%; RepeatCount and ApproxDistinct edge cases covered
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** Aggregator.java is 689 lines; many static inner aggregator classes

#### Task 2C.7: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 2C.1–2C.6
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 2D — File implementations

#### Task 2D.1–2D.4: Create FileImplementationTest.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `CompressedReadWriteFile`, `CompressedFile`, `MappedFile`, `IFileOutputStream`, `CompressDatabase` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** CompressedReadWriteFile is 608 lines. StorageFactory may need a custom IFile parameter to instantiate these. Check StorageFactory API.

#### Task 2D.5: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 2D.1–2D.4
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 2E — Database, Key, PatriciaTrieKey, Version

#### Task 2E.1–2E.5: Extend DatabaseTest.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `Database`, `Database.Table`, `Database.Metadata` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** L
- **Notes:** Database.java is 1321 lines. Focus on Table.select/update/delete and metadata access paths.

#### Task 2E.6: Extend TestPatricia.java — PatriciaTrieKey
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `PatriciaTrieKey` coverage rises from 18% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** PatriciaTrieKey has factory methods for different key types (String, bytes, InetAddress)

#### Task 2E.7: Extend TestVersion.java — VersionHistory
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `VersionHistory` coverage rises from 16% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** VersionHistory tracks multiple Version objects; requires checkout/history API calls

#### Task 2E.8: Create KeyTest.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `Key` coverage rises from 48% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Key has many constructor overloads; test each primitive type and String variant

#### Task 2E.9: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 2E.1–2E.8
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 2F — Utility / Math / Iterator classes

#### Task 2F.1–2F.5: Create CollectionUtilTest.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `PersistentCollection`, `IterableIterator`, `ThreadSafeIterator`, `ClassFilterIterator`, `Projection` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** PersistentCollection is abstract — must subclass; ThreadSafeIterator needs concurrency test

#### Task 2F.6–2F.7: Create MathUtilTest.java
- **Status:** pending
- **Priority:** Low
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `Euler` and `FP` coverage ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Pure math utility classes — no Storage needed

#### Task 2F.8–2F.9: PinnedPersistent / PersistentResource
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `PinnedPersistent` rises from 77% to ≥ 80%; `PersistentResource` rises from 45% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** PersistentResource implements IResource — test shared/exclusive lock acquire/release

#### Task 2F.10: Verify and check org.garret.perst ≥ 80%
- **Status:** pending
- **Priority:** High
- **Dependencies:** all Phase 2 tasks
- **Owner:** —
- **Success criteria:** JaCoCo reports `org.garret.perst` instruction ≥ 80%
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3A — JSQL Query Nodes

#### Task 3A.1–3A.7: Extend JSQL tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `UnaryOpNode`, `BinOpNode`, `ContainsNode`, `InvokeNode`, `LoadAnyNode`, `OrderNode`, `CompareNode` coverage ≥ 40% (intermediate target; full 80% requires deep JSQL parsing paths)
- **Timestamp:** —
- **Effort estimate:** L
- **Notes:** JSQL nodes are exercised indirectly through query strings; need complex queries to reach deeper paths. `InvokeAnyNode` and `GetAtNode` are excluded from 80% target.

#### Task 3A.8: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3A.1–3A.7
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3B — B-tree Variants

#### Task 3B.1–3B.7: Extend B-tree tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `AltBtreeMultiFieldIndex`, `RndBtreeMultiFieldIndex`, `RndBtreeCompoundIndex`, `AltBtreeCompoundIndex` coverage > 0%; all variants reach ≥ 60% (large classes — full 80% may need many test rounds)
- **Timestamp:** —
- **Effort estimate:** L
- **Notes:** B-tree pages are covered indirectly by driving enough inserts to trigger splits

#### Task 3B.8: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3B.1–3B.7
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3C — R-tree Spatial Index

#### Task 3C.1–3C.4: Extend TestRtree.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `RtreeRn`, `RtreeRnPage`, `RtreeRn.RtreeIterator`, `RtreeRn.NeighborIterator` coverage > 0%; all Rtree classes ≥ 60%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** RtreeRn is N-dimensional — use 3D/4D Rectangle arrays via `RectangleRn`

#### Task 3C.5: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3C.1–3C.4
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3D — XML Import/Export

#### Task 3D.1–3D.4: Extend TestXML.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `XMLImporter` and `XMLExporter` coverage from 32%/36% to ≥ 70%; XMLScanner from 53% to ≥ 80%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** Round-trip test is most effective; malformed XML triggers scanner error paths

#### Task 3D.5: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3D.1–3D.4
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3E — Persistent Collections

#### Task 3E.1–3E.5: Extend collection tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `PersistentListImpl`, `PersistentMapImpl.SubMap`, `LinkImpl.SubList`, `ScalableList`, `ScalableSet` coverage ≥ 60% (intermediate); SubList / SubMap inner classes to ≥ 50%
- **Timestamp:** —
- **Effort estimate:** L
- **Notes:** ScalableList at 0% is high priority — likely a large class. SubMap at 11% needs headMap/tailMap tests.

#### Task 3E.6: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3E.1–3E.5
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3F — WeakHashTable / PinWeakHashTable

#### Task 3F.1: Investigate WeakHashTable 0% coverage
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** Root cause identified — either storage property key found or alternative mechanism
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** Perst defaults to OidHashTable. Check StorageFactory.setProperty("perst.object.cache.kind", "weak") or equivalent.

#### Task 3F.2–3F.4: Add WeakHashTable / PinWeakHashTable tests
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** 3F.1
- **Owner:** —
- **Success criteria:** `impl.WeakHashTable` and `impl.PinWeakHashTable` coverage > 0%, ideally ≥ 60%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** GC-dependent tests are inherently flaky — use `System.gc()` with a retry loop

#### Task 3F.5: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3F.1–3F.4
- **Owner:** —
- **Success criteria:** `mvn test` passes (including GC-sensitive tests)
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3G — Special File Implementations

#### Task 3G.1–3G.2: Create FileImplTest.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `MultiFile` and `Rc4File` coverage > 0%, ideally ≥ 70%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** MultiFile requires multiple file paths; Rc4File requires a cipher key parameter to StorageFactory

#### Task 3G.3: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3G.1–3G.2
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3H — BitmapCustomAllocator

#### Task 3H.1–3H.2: Extend TestAlloc.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `BitmapCustomAllocator` coverage from 0% to ≥ 70%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** CustomAllocator API requires creating a storage with a registered custom allocator

#### Task 3H.3: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3H.1–3H.2
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3I — StorageImpl, Ttree, ThickIndex

#### Task 3I.1–3I.4: Extend StorageTest.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `StorageImpl` coverage from 51% to ≥ 70% (stepping stone to 80%)
- **Timestamp:** —
- **Effort estimate:** L
- **Notes:** StorageImpl is the largest impl class; backup/restore and GC paths have high instruction counts

#### Task 3I.5–3I.6: Extend TestThickIndex.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `ThickFieldIndex` from 18% to ≥ 70%; `ThickIndex` from 32% to ≥ 70%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** ThickIndex wraps a normal index with a count per key — duplicates are the key test input

#### Task 3I.7–3I.9: Extend TestTtree.java
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `TtreePage` from 28% to ≥ 70%; `Ttree` from 49% to ≥ 70%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** T-tree page split/merge is triggered by large datasets with deletions

#### Task 3I.10: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3I.1–3I.9
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Phase 3J — CodeGeneratorImpl

#### Task 3J.1–3J.3: Extend TestCodeGenerator.java
- **Status:** pending
- **Priority:** Low
- **Dependencies:** none
- **Owner:** —
- **Success criteria:** `CodeGeneratorImpl` from 13% to ≥ 60%
- **Timestamp:** —
- **Effort estimate:** M
- **Notes:** CodeGenerator uses javassist for bytecode generation; verify javassist.jar is on test classpath

#### Task 3J.4: Verify tests pass
- **Status:** pending
- **Priority:** High
- **Dependencies:** 3J.1–3J.3
- **Owner:** —
- **Success criteria:** `mvn test` passes
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

### Final Verification

#### Task F.1–F.5: Full suite run and commit
- **Status:** pending
- **Priority:** High
- **Dependencies:** all phases complete
- **Owner:** —
- **Success criteria:** All three packages ≥ 80% instruction coverage; all tests pass
- **Timestamp:** —
- **Effort estimate:** S
- **Notes:** —

---

## Risk Assessment

- **High Risk:** `org.garret.perst` 27% → 80% is a 53pp gap — very large. The Sphere / RectangleRn classes and file implementation classes account for most of the gap. If CompressedReadWriteFile / MappedFile are hard to integrate, coverage may stall at ~65%.
- **High Risk:** `org.garret.perst.impl` 41% → 80% requires reaching many B-tree / R-tree page internals that are only exercised under specific data volume / split conditions.
- **Medium Risk:** Replication classes (6 classes, ~4,400 instructions combined) are permanently excluded — this limits `impl` package ceiling. If excluded instructions are counted, the real achievable ceiling may be ~75–78% for `impl`.
- **Medium Risk:** WeakHashTable / PinWeakHashTable GC-dependent tests may be flaky in CI.
- **Low Risk:** `org.garret.perst.fulltext` gap is small (16pp) and achievable with a few new test methods.

## Current Blockers
- None at planning stage

## Next Steps
1. Start Phase 1 (fulltext) — lowest risk, fastest win
2. Then Phase 2A (geometry) — pure Java, no storage dependency
3. Then Phase 2B (L2List, SmallMap)
4. Continue with remaining phases in priority order per plan
