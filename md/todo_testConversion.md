# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Todolist: JUnit Test Conversion

## Tasks

### Priority #1: Core Functionality Tests (High Value)
- [ ] 1. Convert TestAgg - Aggregation functions
- [x] 2. Convert TestBackup - Backup/restore functionality (3 tests)
- [x] 3. Convert TestBlob - BLOB storage (6 tests)
- [x] 4. Convert TestGC - Garbage collection (5 tests)

### Priority #2: Index and Query Tests (Medium Value)
- [x] 5. Convert TestJSQL - JSQL query language (7 tests)
- [x] 6. Convert TestIndex2 - B-tree alternatives (5 tests)
- [x] 7. Convert TestSet - Set operations (5 tests)

### Priority #3: Advanced Features (Lower Priority)
- [x] 8. Convert TestFullTextIndex - Lucene integration (16 tests)
- [x] 9. Convert TestCodeGenerator - Code generation (8 tests)
- [x] 10. Convert TestDynamicObjects - Dynamic classes (7 tests)
- [x] 11. Convert TestAutoIndices - Auto-indexing (9 tests)

## Success Criteria
- [x] All converted tests pass with `mvn test`
- [x] Test count increases from 19 to 148 tests
- [x] No regressions in existing functionality
- [x] Database cleanup works properly

## Rollback Plan
1. Revert individual test file changes if a specific test fails
2. Keep original demo tests in tst/ as backup
3. Run existing tests to verify no regressions
