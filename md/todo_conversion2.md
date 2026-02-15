# JUnit Test Conversion - Phase 2 - Todo List

## Completed (33 tests)
- [x] TestCompoundIndex
- [x] TestBit
- [x] TestBitmap
- [x] TestDecimal
- [x] TestRaw
- [x] TestRtree
- [x] TestKDTree
- [x] TestPatricia
- [x] TestTtree
- [x] TestThickIndex
- [x] TestRndIndex
- [x] TestDerivedIndex
- [x] TestJsqlJoin
- [x] TestRegex
- [x] TestAlloc
- [x] TestKDTree2
- [x] TestJSQLContains
- [x] TestRollback
- [x] TestMaxOid
- [x] TestMod
- [x] TestXML
- [x] TestLoad
- [x] TestRecovery
- [x] TestConcur
- [x] TestLeak
- [x] TestTimeSeries
- [x] TestPerf
- [x] TestRandomBlob
- [x] TestVersion (converted today)
- [x] TestServer (converted today)

## Skipped (2 tests)
- [x] TestIndexIterator (already in TestIndex.java)
- [x] TestLink (interactive demo app)

## Pending - Core Tests (1 test)
- [ ] TestAgg

## Pending - Replication & Server (3 tests)
- [ ] TestReplic (complex - requires master/slave network setup)
- [ ] TestReplic2 (complex - requires master/slave network setup)
- [ ] TestDbServer (complex - requires Database server)

## Pending - Other Features (2 tests)
- [ ] TestSSD (interactive menu-driven app)
- [ ] TestSOD (interactive menu-driven app)

## Today's Goal
Convert at least 3 more tests from the pending list.

## Notes
- TestServer converted with 2 test methods: testServerMultiThreaded and testServerIndexOperations
- TestSSD and TestSOD are interactive menu-driven applications - difficult to convert to automated tests
- TestReplic, TestReplic2 require complex network setup (master/slave replication)
- TestDbServer requires Database server setup
