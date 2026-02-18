# Progress: 80% Coverage Goal

## Current Status (2026-02-18)
- **Tests**: 490 (all passing, 0 failures, 0 errors)
- **Coverage**: 44.4% instruction coverage
  - `org.garret.perst`: 46.5% (6977/15005)
  - `org.garret.perst.fulltext`: 90.9% (879/967)
  - `org.garret.perst.impl`: 43.7% (46661/106710)

## Phases Completed

### Phase 1 (prior work)
- TestFullTextIndex.java - fulltext index coverage → 90.9% fulltext
- GeometryTest.java (19 tests) - SpatialIndexR2 coverage

### Phase 2A (prior work)  
- GeometryTest extended to cover RectangleR2 utilities

### Phase 2B
- L2ListTest.java (26 tests) - L2List coverage
- SmallMapTest.java (24 tests) - SmallMap coverage

### Phase 2C
- QueryProfilerTest.java - query profiler coverage
- TestAgg.java extended (+5 tests, edge cases)

### Phase 2D
- KeyTest.java (33 tests) - Key class coverage
- DatabaseTest.java extended (+9 tests)

### Phase 2E (current)
- ScalableListTest.java (14 tests) - PersistentListImpl coverage
- SpatialIndexRnTest.java (12 tests) - RtreeRn/RtreeRnPage coverage
- AltBtreeTest.java (9 tests) - AltBtreeMultiFieldIndex coverage
- Fixed: Added no-arg constructor to RectangleRn for IValue deserialization

## Coverage Gap Analysis

### To reach 80% (target: 98,146/122,682 instructions):
- Currently covered: 54,517
- Gap: ~43,600 more instructions needed

### Major uncovered areas in `impl`:
| Class | Instructions | Coverage |
|-------|-------------|----------|
| ReplicationMasterFile | 1362 | 0% |
| ReplicationSlaveStorageImpl | 852 | 0% |
| BitmapCustomAllocator | 892 | 0% |
| RtreeRnPage | 938 | 0% |
| PersistentListImpl.ListIntermediatePage | 589 | 0% |
| MultiFile | 417 | 0% |
| ScalableList | 268 | 0% |
| LinkImpl.SubList | 228 | 0% |
| GetAtNode | 300 | 0% |
| InvokeAnyNode | 322 | 0% |
| AsyncReplicationMasterFile | 406 | 0% |
| AltBtreeCompoundIndex | 319 | 0% |
| RndBtreeCompoundIndex | 259 | 0% |
| RndBtreeMultiFieldIndex | 475 | 0% |
| PinWeakHashTable | 536 | 0% |
| StorageImpl (main class) | ~10000+ | ~42% |

### Assessment
The 80% target requires covering a massive amount of impl code, including:
1. Replication features (ReplicationMasterFile, ReplicationSlaveStorageImpl) - complex network code
2. BitmapCustomAllocator - requires specific Storage configuration
3. RtreeRnPage - N-dimensional R-tree pages (requires RectangleRn serialization fixed)
4. Deeper StorageImpl branches (error recovery, XML import/export, etc.)

## Next Steps
- Phase 2F: BitmapCustomAllocator tests (via createBitmapAllocator API)
- Phase 2G: MultiFile/PinWeakHashTable tests
- Phase 2H: ScalableList fix (create via createList, not createScalableList for coverage)
- Phase 3: StorageImpl deeper coverage (XML, error paths, etc.)
