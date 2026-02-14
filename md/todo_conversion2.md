# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: JUnit Test Conversion - Phase 2

## Tasks

### Priority #1: Core Tests
- [ ] 1. Convert TestAgg - Aggregation functions (requires data.csv)
- [x] 2. Convert TestCompoundIndex - Compound indices
- [x] 3. Convert TestIndexIterator - Index iterator (skipped - already covered in TestIndex)

### Priority #2: Data Types
- [x] 4. Convert TestBit - Bit operations
- [x] 5. Convert TestBitmap - Bitmap index
- [x] 6. Convert TestDecimal - Decimal types
- [x] 7. Convert TestLink - Linked lists (skipped - demo app)
- [x] 8. Convert TestRaw - Raw data access

### Priority #3: Advanced Indexing
- [x] 9. Convert TestRtree - Spatial indexing
- [x] 10. Convert TestKDTree - K-D trees
- [ ] 11. Convert TestKDTree2 - K-D tree variant
- [ ] 12. Convert TestPatricia - Patricia tries
- [ ] 13. Convert TestTtree - T-tree
- [ ] 14. Convert TestThickIndex - Thick indexing
- [ ] 15. Convert TestRndIndex - Random indexing
- [ ] 16. Convert TestDerivedIndex - Derived indices

### Priority #4: Query & Search
- [ ] 17. Convert TestJSQLContains - CONTAINS operator
- [ ] 18. Convert TestJsqlJoin - JOIN queries
- [ ] 19. Convert TestRegex - Regular expression search

### Priority #5: Transactions
- [ ] 20. Convert TestRollback - Transaction rollback
- [ ] 21. Convert TestRecovery - Database recovery
- [ ] 22. Convert TestConcur - Concurrency testing

### Priority #6: Replication & Server
- [ ] 23. Convert TestReplic - Database replication
- [ ] 24. Convert TestReplic2 - Replication variant
- [ ] 25. Convert TestDbServer - Database server
- [ ] 26. Convert TestServer - Server functionality

### Priority #7: Other Features
- [ ] 27. Convert TestAlloc - Memory allocation
- [ ] 28. Convert TestLeak - Memory leak detection
- [ ] 29. Convert TestLoad - Load testing
- [ ] 30. Convert TestMaxOid - Max OID handling
- [ ] 31. Convert TestMod - Modification tracking
- [ ] 32. Convert TestPerf - Performance testing
- [ ] 33. Convert TestRandomBlob - Random BLOB access
- [ ] 34. Convert TestSSD - SSD optimization
- [ ] 35. Convert TestSOD - SOD features
- [ ] 36. Convert TestTimeSeries - Time series data
- [ ] 37. Convert TestVersion - Version control
- [ ] 38. Convert TestXML - XML storage

## Success Criteria
- [x] All converted tests pass with `mvn test`
- [x] Test count increases from 148 to 162+ tests
- [ ] No regressions in existing functionality
- [x] Database cleanup works properly

## Rollback Plan
1. Revert individual test file changes if a specific test fails
2. Keep original demo tests in tst/ as backup
3. Run existing tests to verify no regressions
