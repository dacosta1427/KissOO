# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Fix DatabaseTest Compilation Error

## Objective
Fix the compilation error in DatabaseTest.java caused by the removal of the deprecated `createTable` method.

## Background
In commit 75e2681 ("Deprecated code removed and replaced certain amount of warnings also removed"), the `createTable(Class table)` method in Database.java was deprecated and later made private (`createTableInternal`). The DatabaseTest.java still uses the public method which no longer exists.

## Root Cause
- The `createTable` method was deprecated in Perst 2.75 (no longer needed as tables are auto-created)
- The method was made private in a cleanup commit
- The test file was not updated to reflect this change

## Options

### Option 1: Restore the deprecated public method
- Restore `createTable` as a public deprecated method
- This maintains backward compatibility
- **Pros:** Minimal change, maintains API
- **Cons:** Keeps deprecated code in the codebase

### Option 2: Update tests to not use createTable
- Modify DatabaseTest.java to not call createTable
- Tables are auto-created when objects are inserted
- **Pros:** Removes need for deprecated method
- **Cons:** Changes test behavior

### Option 3: Remove the failing tests
- Remove the tests that rely on createTable
- **Pros:** Quick fix
- **Cons:** Loses test coverage

## Recommended Approach
**Option 1** - Restore the deprecated method with `@Deprecated` annotation. This is the safest approach that maintains backward compatibility while clearly indicating the method should not be used in new code.

## Dependencies
- Maven build tool
- JUnit 5

## Success Criteria
- [ ] DatabaseTest.java compiles successfully
- [ ] All existing tests still pass
- [ ] No new warnings introduced

## Rollback Plan
- **Checkpoint:** Git commit before changes
- **Revert command:** `git reset --hard HEAD`
- **Verification:** Run `mvn test-compile` and confirm error returns
- **Impact:** No impact on other modules

## Supporting Documents
- Todolist: `md/todo_fix_database_test.md`
- Progress: `md/progress_fix_database_test.md`
