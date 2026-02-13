# Test Coverage Tracking

## Overview
This document tracks which demo code tests are included in the run_tests.sh script and their parameter requirements.

## Current Status
- **Total test files in tst/**: 58
- **Included in run_tests.sh**: 39
- **Missing from run_tests.sh**: 19

## Test Inventory

### Tests INCLUDED in run_tests.sh (39 tests)

| # | Test Name | Parameters | Status |
|---|-----------|------------|--------|
| 1 | Simple | None | Not tested |
| 2 | Benchmark | None | Not tested |
| 3 | TestIndex | None | Not tested |
| 4 | TestIndex2 | None | Not tested |
| 5 | TestAgg | None | Not tested |
| 6 | TestBackup | None | Not tested |
| 7 | TestBit | None | Not tested |
| 8 | TestBitmap | None | Not tested |
| 9 | TestBlob | None | Not tested |
| 10 | TestCompoundIndex | None | Not tested |
| 11 | TestFullTextIndex | None | Not tested |
| 12 | TestGC | None | Not tested |
| 13 | TestJSQL | None | Not tested |
| 14 | TestKDTree | None | Not tested |
| 15 | TestLink | None | Not tested |
| 16 | TestList | None | Not tested |
| 17 | TestMap | None | Not tested |
| 18 | TestMaxOid | None | Not tested |
| 19 | TestMod | None | Not tested |
| 20 | TestPatricia | None | Not tested |
| 21 | TestR2 | None | Not tested |
| 22 | TestRaw | None | Not tested |
| 23 | TestRecovery | None | Not tested |
| 24 | TestRegex | None | Not tested |
| 25 | TestReplic | None | Not tested |
| 26 | TestRndIndex | None | Not tested |
| 27 | TestRollback | None | Not tested |
| 28 | TestRtree | None | Not tested |
| 29 | TestSet | None | Not tested |
| 30 | TestSSD | None | Not tested |
| 31 | TestThickIndex | None | Not tested |
| 32 | TestTimeSeries | None | Not tested |
| 33 | TestTtree | None | Not tested |
| 34 | TestVersion | None | Not tested |
| 35 | TestXML | None | Not tested |
| 36 | SearchEngine | None | Not tested |
| 37 | IpCountry | None | Not tested |
| 38 | Guess | None | Not tested |
| 39 | AstroNet | None | Not tested |

### Tests MISSING from run_tests.sh (19 tests)

| # | Test Name | Parameters (from makefile) | Status |
|---|-----------|------------|--------|
| 1 | TestAlloc | None (or multiple runs) | Not tested |
| 2 | TestAutoIndices | None | Not tested |
| 3 | TestCodeGenerator | None | Not tested |
| 4 | TestConcur | None | Not tested |
| 5 | TestDbServer | None | Not tested |
| 6 | TestDecimal | None | Not tested |
| 7 | TestDerivedIndex | None | Not tested |
| 8 | TestDynamicObjects | None (or populate) | Not tested |
| 9 | TestIndexIterator | None | Not tested |
| 10 | TestJSQLContains | None | Not tested |
| 11 | TestJsqlJoin | None (runs twice in makefile) | Not tested |
| 12 | TestKDTree2 | None | Not tested |
| 13 | TestLeak | None | Not tested |
| 14 | TestLoad | None | Not tested |
| 15 | TestPerf | None (or inmemory) | Not tested |
| 16 | TestRandomBlob | None | Not tested |
| 17 | TestReplic2 | None | Not tested |
| 18 | TestServer | None | Not tested |
| 19 | TestSOD | None | Not tested |

## Tests with Parameters (from makefile)

These tests require specific parameters to be run properly:

### TestIndex (5 variants)
- `TestIndex` - default
- `TestIndex altbtree` - alternative B-tree
- `TestIndex inmemory` - in-memory mode
- `TestIndex map` - map mode
- `TestIndex zip` - compressed
- `TestIndex multifile` - multi-file
- `TestIndex gc` - garbage collection

### TestMap (4 variants)
- `TestMap` - default
- `TestMap populate` - populate database
- `TestMap 100` - with parameter
- `TestMap 100 populate` - with parameter + populate

### TestKDTree (2 variants)
- `TestKDTree` - default
- `TestKDTree populate` - populate database

### TestKDTree2 (2 variants)
- `TestKDTree2` - default
- `TestKDTree2 populate` - populate database

### TestGC (2 variants)
- `TestGC` - default
- `TestGC background` - background GC

### TestCompoundIndex (2 variants)
- `TestCompoundIndex` - default
- `TestCompoundIndex altbtree` - alternative B-tree

### TestIndexIterator (2 variants)
- `TestIndexIterator` - default
- `TestIndexIterator altbtree` - alternative B-tree

### TestDynamicObjects (3 variants)
- `TestDynamicObjects` - default
- `TestDynamicObjects populate` - populate database
- `TestDynamicObjects` - verify (runs twice in makefile)

### TestJsqlJoin (2 variants)
- `TestJsqlJoin` - runs twice in makefile

### TestReplic (2 variants)
- `TestReplic master` - master mode
- `TestReplic slave` - slave mode

### TestBlob (3 variants)
- `TestBlob` - default
- `TestBlob` - runs twice
- `TestBlob zip` - compressed mode

### TestFullTextIndex (3 variants)
- `TestFullTextIndex` - default
- `TestFullTextIndex` - runs twice
- `TestFullTextIndex reload` - reload mode

### TestAlloc (3 variants)
- `TestAlloc` - initialize
- `TestAlloc` - verify
- `TestAlloc` - cleanup (runs 3 times in makefile)

### TestMod (2 variants)
- `TestMod` - default
- `TestMod pinned` - pinned mode

### TestLeak
- `TestLeak` - memory leak test

### TestLoad
- `TestLoad` - load test

### TestPerf (2 variants)
- `TestPerf` - default
- `TestPerf inmemory` - in-memory mode

## Makefile Test Order (complete list)

```
Simple.sh
TestList.sh
TestPerf.sh
TestPerf.sh inmemory
TestRegex.sh
TestAgg.sh
TestPatricia.sh
TestIndex.sh
TestIndex.sh altbtree
TestIndex.sh inmemory
TestIndex.sh map
TestIndex.sh zip
TestIndex.sh multifile
TestIndex.sh gc
TestIndex2.sh
TestRndIndex.sh
TestMap.sh
TestMap.sh populate
TestMap.sh
TestMap.sh 100
TestMap.sh 100 populate
TestCompoundIndex.sh
TestCompoundIndex.sh altbtree
TestMod.sh
TestMod.sh pinned
TestIndexIterator.sh
TestIndexIterator.sh altbtree
TestRtree.sh
TestR2.sh
TestTtree.sh
TestKDTree.sh
TestKDTree2.sh
TestKDTree.sh populate
TestKDTree.sh populate
TestKDTree2.sh populate
TestKDTree2.sh populate
TestRaw.sh
TestRaw.sh
TestGC.sh
TestGC.sh background
TestGC.sh altbtree background
TestConcur.sh
TestConcur.sh
TestServer.sh
TestDbServer.sh
TestXML.sh
TestXML.sh altbtree
TestBackup.sh
TestBlob.sh
TestBlob.sh
CompressDatabase.sh testblob.dbs
TestBlob.sh zip
TestRandomBlob.sh
TestRandomBlob.sh
TestAlloc.sh
TestAlloc.sh
TestAlloc.sh
TestLeak.sh
TestTimeSeries.sh
TestBit.sh
TestThickIndex.sh
TestSet.sh
TestJSQL.sh
TestJSQLContains.sh
TestJsqlJoin.sh
TestJsqlJoin.sh
TestCodeGenerator.sh
TestAutoIndices.sh
TestVersion.sh
TestFullTextIndex.sh
TestFullTextIndex.sh
TestFullTextIndex.sh reload
TestReplic.sh master & ./TestReplic.sh slave
TestDynamicObjects.sh
TestDynamicObjects.sh populate
TestDynamicObjects.sh
TestDecimal.sh
TestRollback.sh
TestLoad.sh
```

## Next Steps

1. Update run_tests.sh to include all missing tests
2. Add parameter support for tests that require it
3. Test each test individually and verify they work
4. Update this tracking document with results
