> See [workingRules.md](./workingRules.md) for core operating principles

---

# Plan: Remove Deprecated Database API

## Objective
Remove deprecated methods from Database.java to achieve a cleaner API. Since Perst 2.75, tables and indices are created automatically when objects are inserted, making these methods unnecessary.

## Current State
- Java Version: 25 ✓
- Lucene Version: 9.11.0 ✓
- Tests: 288 passing ✓
- Deprecated methods in Database.java: 5

## Deprecated Methods to Remove

1. `createTable(Class table)` - No longer needed, auto-creates on insert
2. `createIndex(Class, String, int)` - No longer needed
3. `createIndex(Class, String, boolean)` - No longer needed
4. `createIndex(Class, String, boolean, boolean, boolean)` - No longer needed
5. `createIndex(Class, String, boolean, boolean, boolean, boolean)` - No longer needed

## Modern Replacement
Use `@Indexable` annotation on persistent class fields for automatic index creation.

---

## Dependencies
- All tasks depend on tests passing
- Run `mvn test` after each change per workingRules

## Environment
- Java: OpenJDK 25
- Build: Maven

## Success Criteria
- [ ] All 5 deprecated methods removed from Database.java
- [ ] All test files updated
- [ ] All 288 tests pass
- [ ] Clean compilation with -Xlint:all -Werror (or minimal warnings)

## Rollback Plan
1. Git revert to previous branch state
2. Run `mvn test` to verify

## Risks
- Breaking change: Code using removed methods will fail to compile
- Tests in DatabaseTest.java need updates
