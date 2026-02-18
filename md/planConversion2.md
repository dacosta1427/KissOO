rki# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: JUnit Test Conversion - Phase 2

## Objective
Convert the remaining 38 demo tests from `tst/` directory to JUnit 5 format.

## Background
- 23 tests have already been converted in Phase 1
- Current test count: 148 tests passing
- 38 tests remain to be converted from the tst/ directory

## Scope
### Included
- All remaining test files in tst/ directory (excluding demo apps)
- Tests that can run in isolation without external dependencies (except TestAgg which requires data.csv)

### Excluded
- Demo applications (Simple.java, Guess.java, Benchmark.java, etc.)
- Tests requiring external servers or complex setup

---

## Approach
1. Read each original test file from tst/
2. Analyze test logic and assertions
3. Convert to JUnit 5 with @Test, @BeforeEach, @AfterEach annotations
4. Scale down data sizes for faster test execution
5. Run `mvn test` after each conversion to verify

## Implementation

### Phase 1: Core Tests (3 tests)
- TestAgg - requires data.csv
- TestCompoundIndex - Compound indices
- TestIndexIterator - Index iterator

### Phase 2: Data Types (5 tests)
- TestBit - Bit operations
- TestBitmap - Bitmap index
- TestDecimal - Decimal types
- TestLink - Linked lists
- TestRaw - Raw data access

### Phase 3: Advanced Indexing (8 tests)
- TestRtree - Spatial indexing
- TestKDTree - K-D trees
- TestKDTree2 - K-D tree variant
- TestPatricia - Patricia tries
- TestTtree - T-tree
- TestThickIndex - Thick indexing
- TestRndIndex - Random indexing
- TestDerivedIndex - Derived indices

### Phase 4: Query & Search (3 tests)
- TestJSQLContains - CONTAINS
- TestJsqlJoin - JOINs
- TestRegex - Regex

### Phase 5: Transactions (3 tests)
- TestRollback - Rollback
- TestRecovery - Recovery
- TestConcur - Concurrency

### Phase 6: Replication & Server (4 tests)
- TestReplic - Replication
- TestReplic2 - Replication 2
- TestDbServer - DB Server
- TestServer - Server

### Phase 7: Other Features (12 tests)
- TestAlloc - Memory allocation
- TestLeak - Memory leaks
- TestLoad - Load testing
- TestMaxOid - Max OID
- TestMod - Modifications
- TestPerf - Performance
- TestRandomBlob - Random BLOBs
- TestSSD - SSD features
- TestSOD - SOD features
- TestTimeSeries - Time series
- TestVersion - Versioning
- TestXML - XML features

---

## Expected Outcomes
- All 38 tests converted to JUnit 5
- Test count increases from 148 to ~250+ tests
- All tests pass with `mvn test`
- No regressions in existing functionality

## Risks
- Some tests may require external dependencies or complex setup
- Tests with threading may be difficult to convert reliably
- Server-based tests may need special handling

## Rollback Plan
- Keep original tst/ tests as backup
- Revert individual test file changes if failures occur
