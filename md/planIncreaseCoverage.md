# Increase Test Coverage

## Overview

This document tracks the effort to increase code coverage for the Perst database project from the current baseline to a target of 70%+ instruction coverage.

## Progress Update (2026-02-15)

### Current Coverage Status

| Metric | Current | Target |
|--------|---------|--------|
| Instruction | **40%** | 70% |
| Branch | 31% | 50% |
| Line | **39%** | 70% |
| Method | 62% | 80% |
| Class | 67% | 85% |

### Coverage by Package

| Package | Coverage | Classes | Missed Instructions |
|---------|----------|---------|---------------------|
| org.garret.perst | 27% | 98 | 10,848 |
| org.garret.perst.impl | **41%** | 284 | 61,985 |
| org.garret.perst.fulltext | 64% | 11 | (low priority) |

### Total
- **Total Instructions:** 122,679
- **Covered Instructions:** 49,500 (40%)
- **Missed Instructions:** 73,179
- **Total Classes:** 393

### Improvement Summary
- **Previous Coverage:** 39% (74,168 missed)
- **Current Coverage:** 40% (73,179 missed)
- **Net Improvement:** +989 instructions covered (+1%)
- **Tests Added:** 4 new tests (TestBtreeCompoundIndex)
- **Total Tests:** 292 (all passing)

## High Priority Uncovered Classes

### org.garret.perst.impl Package (41% coverage)

The following classes have 0% or very low coverage:

1. **ReplicationMasterFile** - 0% (1,362 instructions)
2. **PersistentMapImpl** - 0% (1,250 instructions)
3. **BtreeCompoundIndex** - ~~0%~~ **32%** ✓ (improved!)
4. **RtreeRnPage** - 0% (938 instructions)
5. **BitmapCustomAllocator** - 0% (892 instructions)
6. **ReplicationSlaveStorageImpl** - 0% (852 instructions)
7. **WeakHashTable** - 0% (605 instructions)
8. **PersistentListImpl.ListIntermediatePage** - 0% (589 instructions)
9. **PinWeakHashTable** - 0% (536 instructions)
10. **AltBtreeMultiFieldIndex** - 0% (476 instructions)
11. **RndBtreeMultiFieldIndex** - 0% (475 instructions)
12. **MultiFile** - 0% (417 instructions)
13. **AsyncReplicationMasterFile** - 0% (406 instructions)
14. **InvokeAnyNode** - 0% (322 instructions)
15. **RndBtreeCompoundIndex** - 0% (259 instructions)
16. **AltBtreeCompoundIndex** - 0% (319 instructions)
17. **RtreeRn** - 0% (314 instructions)
18. **SubList** - 0% (304 instructions)
19. **GetAtNode** - 0% (300 instructions)
20. **Rc4File** - 0% (291 instructions)
21. **PersistentMapImpl.SubMap** - 0% (285 instructions)
22. **ScalableList** - 0% (268 instructions)

### Low Coverage Classes (>0% but <20%)

1. **CodeGeneratorImpl** - 13% (2,392 missed)
2. **BinOpNode** - 10% (1,367 missed)
3. **ContainsNode** - 11% (707 missed)
4. **UnaryOpNode** - 4% (382 missed)
5. **AltBtreeFieldIndex** - 15% (484 missed)
6. **RndBtreeFieldIndex** - 14% (494 missed)
7. **ThickCaseInsensitiveFieldIndex** - 9% (380 missed)

## Strategy for Coverage Improvement

### Phase 1: Analyze and Prioritize

- [x] Run initial coverage analysis with JaCoCo
- [x] Identify classes with 0% coverage
- [x] Document uncovered packages and classes
- [ ] Review each high-priority class for testability

### Phase 2: Add Targeted Tests

#### Priority 1: Core Storage (org.garret.perst.impl)
- StorageImpl - 51% coverage (already high)
- QueryImpl - 43% coverage (already moderate)
- BtreePage - 52% coverage (already moderate)

#### Priority 2: Uncovered Index Implementations
- BtreeCompoundIndex - 0%
- AltBtreeMultiFieldIndex - 0%
- RndBtreeMultiFieldIndex - 0%

#### Priority 3: Uncovered Collections
- PersistentMapImpl - 0%
- PersistentListImpl.ListIntermediatePage - 0%
- SubList - 0%

#### Priority 4: Uncovered Replication
- ReplicationMasterFile - 0%
- ReplicationSlaveStorageImpl - 0%
- AsyncReplicationMasterFile - 0%

### Phase 3: Edge Cases

- Error conditions
- Boundary cases
- Exception handling

## Testing Infrastructure

The project uses:
- **JUnit 5** for test framework
- **JaCoCo** for code coverage analysis
- **Maven** for build and test execution

### Running Tests with Coverage

```bash
# Navigate to project directory
cd /home/dacosta/Projects/oodb-testcoverage

# Run tests and generate coverage
mvn clean test

# View coverage report
# Open target/site/jacoco/index.html
```

## Notes

- All 288 tests pass currently
- Some uncovered code may be deprecated or platform-specific
- Replication tests may require network setup
- Some internal classes may be difficult to test in isolation

## Related Files

- `../oodb-testcoverage/md/todo_increaseCoverage.md` - Detailed task list
- `../oodb-testcoverage/md/progress_increaseCoverage.md` - Progress tracking
- `../oodb-testcoverage/md/planIncreaseCoverage.md` - Detailed plan
