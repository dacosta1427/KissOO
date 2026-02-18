# Progress: 80% Coverage Goal

## Current Status (2026-02-18)
- **Tests**: 510 (all passing, 0 failures, 0 errors)
- **Coverage**: 45.8% instruction coverage
  - `org.garret.perst`: 46.4% (6964/14998)
  - `org.garret.perst.fulltext`: 90.8% (872/960)
  - `org.garret.perst.impl`: 45.3% (47606/105076)

## Phases Completed

### Phase 1-2A (prior work)
- TestFullTextIndex.java - fulltext index coverage → 90.8% fulltext
- GeometryTest.java (19 tests) - SpatialIndexR2 coverage

### Phase 2B
- L2ListTest.java (26 tests) - L2List coverage
- SmallMapTest.java (24 tests) - SmallMap coverage

### Phase 2C
- QueryProfilerTest.java - query profiler coverage
- TestAgg.java extended (+5 tests, edge cases)

### Phase 2D
- KeyTest.java (33 tests) - Key class coverage
- DatabaseTest.java extended (+9 tests)

### Phase 2E
- ScalableListTest.java (14 tests) - PersistentListImpl coverage
- SpatialIndexRnTest.java (12 tests) - RtreeRn/RtreeRnPage coverage
- AltBtreeTest.java (9 tests) - AltBtreeMultiFieldIndex + AltBtreeFieldIndex coverage
- Fixed: Added no-arg constructor to RectangleRn for IValue deserialization

### Phase 2F (current)
- CompoundIndexTest.java (14 tests) - AltBtreeCompoundIndex, RndBtreeCompoundIndex, RndBtreeMultiFieldIndex
- PersistentListPageTest.java (11 tests) - PersistentListImpl.ListIntermediatePage, ListItr
- CacheKindTest.java (8 tests) - PinWeakHashTable, StrongHashTable, WeakHashTable, SoftHashTable, exportXML/importXML
- Fixed: Added no-arg constructors to AltBtreeCompoundIndex.CompoundKey, RndBtreeCompoundIndex.CompoundKey, RndBtreeMultiFieldIndex.CompoundKey
- Fixed: int.class → Integer.class in compound key type arrays

## Coverage Gap Analysis

### To reach 80% (target: ~96,827/121,034 instructions):
- Currently covered: 55,442
- Gap: ~41,400 more instructions needed

### Skipped (by user request):
| Class | Instructions | Reason |
|-------|-------------|--------|
| ReplicationMasterFile | 1362 | Complex network replication code |
| ReplicationSlaveStorageImpl | 852 | Complex network replication code |
| AsyncReplicationMasterFile | 406 | Complex network replication code |
| BitmapCustomAllocator | 892 | Requires specific storage config |
| RtreeRnPage | 938 | N-dimensional R-tree pages |

### Major remaining uncovered areas in `impl`:
| Class | Instructions | Coverage |
|-------|-------------|----------|
| MultiFile | 417 | 0% |
| LinkImpl.SubList | 228 | 0% |
| GetAtNode | 300 | 0% |
| InvokeAnyNode | 322 | 0% |
| ScalableList | 268 | 0% |
| StorageImpl (main class) | ~8000+ | ~44% |
| ThickFieldIndex | 400+ | partial |
| MultidimensionalIndex (KDTree) | 800+ | 0% |
| SortedCollectionImpl | 500+ | partial |
| PatriciaImpl | 400+ | partial |

## Next Steps (Phase 2G onwards)
1. **MultiFileStorageTest** - test `@path` file format for MultiFile storage
2. **LinkSubListTest** - test `link.subList(from, to)` for LinkImpl.SubList
3. **KDTreeTest** - test `createMultidimensionalIndex` for KDTree coverage
4. **JsqlArrayAccessTest** - test array indexing in JSQL for GetAtNode/InvokeAnyNode
5. **ThickFieldIndexTest** - test `createThickFieldIndex` API
6. **PatriciaTrieTest** - test Patricia trie index
7. **StorageImpl deeper paths** - XML import/export (partially done), error recovery
