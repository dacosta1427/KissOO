# Working Rules (must be included)

---

# Todolist: Investigate TestSOD

## Tasks

### Priority #1: Investigation
- [x] Task 1.1: Examine TestSOD.java source code
- [x] Task 1.2: Check if there's a non-interactive/batch mode
- [x] Task 1.3: Determine if test should be excluded from automated runs
- [x] Task 1.4: Document findings and implement solution

## Success Criteria
- [x] Document if TestSOD can run in automated mode
- [x] Decide whether to include or exclude from test suite

### Priority #2: Fix Java 25 Compatibility
- [x] Task 2.1: Investigate Java 25 compatibility issue (sun.misc.unsafe removal)
- [x] Task 2.2: Implement fix without touching Perst core code (if possible)
- [x] Task 2.3: Verify TestSOD runs correctly

## Notes
- TestSOD is an interactive menu-driven test (not designed for automation)
- No batch/automated mode exists in the source code
- Java 25 FIXED: Rebuilt Perst from source using Maven (no more sun.misc.unsafe)
- Test now runs on Java 25 - can be executed with piped input
- **FIX: Run `mvn compile` to rebuild lib/perst.jar**

## Rollback Plan
1. N/A - no changes expected to test files
