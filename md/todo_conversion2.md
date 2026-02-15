# JUnit Test Conversion - Phase 2 - Todo List

## Completed (34 tests)
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
- [x] TestVersion
- [x] TestServer (converted today)
- [x] TestDbServer (converted today - full-text search!)

## Skipped (2 tests)
- [x] TestIndexIterator (already in TestIndex.java)
- [x] TestLink (interactive demo app)

## Pending - Core Tests (1 test)
- [ ] TestAgg

## Pending - Replication & Server (2 tests)
- [ ] TestReplic (complex - requires master/slave network setup)
- [ ] TestReplic2 (complex - requires master/slave network setup)

## Pending - Other Features (2 tests)
- [ ] TestSSD (interactive menu-driven app)
- [ ] TestSOD (interactive menu-driven app)

## Notes
- TestServer: Multi-threaded server operations with FieldIndex locking
- TestDbServer: Full-text search and Database API testing (3 tests added)
- TestSSD and TestSOD are interactive menu-driven applications - difficult to convert
- TestReplic, TestReplic2 require complex network setup
