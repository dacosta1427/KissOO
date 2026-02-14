> See [workingRules.md](./workingRules.md) for core operating principles

---

# Todolist: Increase JUnit Test Coverage

## Tasks

### Priority #1: Core List and Index Tests
- [x] 1. Convert TestList - IPersistentList operations
- [x] 2. Convert TestIndex - B-tree index functionality
- [x] 3. Convert TestMap - FieldIndex operations

### Priority #2: Query and Data Tests
- [ ] 3. Convert TestMap - FieldIndex operations
- [ ] 4. Convert TestJSQL - JSQL query language
- [ ] 5. Convert TestAgg - Aggregation functions

### Priority #3: Storage and Recovery Tests
- [ ] 6. Convert TestBackup - Backup/restore
- [ ] 7. Convert TestBlob - BLOB storage

### Priority #4: Advanced Features
- [ ] 8. Convert TestFullTextIndex - Lucene integration
- [ ] 9. Convert TestCodeGenerator - Code generation
- [ ] 10. Convert TestDynamicObjects - Dynamic class support

## Success Criteria
- [ ] All converted tests pass with `mvn test`
- [ ] Test count increases from 58 to ~100+ tests
- [ ] No regressions in existing functionality

## Rollback Plan
1. Revert individual test file changes if a specific test fails
2. Keep original demo tests in tst/ as backup
