# Working Rules (must be included)

---
Environment:
- Runs on Linux (*.sh scripts) and in Windows (*.cmd scripts)
- Java: OpenJDK 25
- Maven as a build tool, NO Gradle and NO script based compilation
- Maven should always work with the latest library versions
- Enable -Xlint:all -Werror (or at least during CI) to catch deprecations and preview API shifts.

Standard procedure:
After a change has been made to the core Perst code all the junit tests have to be run prior to stating the task as done.

### NEVER comment out or remove a line because that would make the code work. If you want to do this there has to be approval.
### NEVER remove the md dir

## Core Principles

1. **All MD documentation lives in the `md/` directory**

2. **Every plan MUST be accompanied by:**
   - A **todolist** (`md/todo_*.md`) containing all items/steps to implement the plan
   - A **progress doc** (`md/progress_*.md`) containing all tasks with their current state

3. **Working rules at top of every plan(ning), progress and todolist document** - This ensures they are never forgotten

---

# Todolist: Fix Warnings in StorageImpl.java

## Tasks

### Priority #1: Fix Auxiliary Class Warning

- [x] Task 1.1: Create new file `ArrayPos.java` with the class definition moved from `Bytes.java`
- [x] Task 1.2: Remove `ArrayPos` class definition from `Bytes.java`
- [x] Task 1.3: Compile to verify no warnings for StorageImpl.java
- [x] Task 1.4: Run all junit tests to verify no regressions

## Success Criteria

- [x] `mvn compile` produces no warnings related to StorageImpl.java
- [x] All existing junit tests pass (Note: `DecimalTest.testDiv` is a pre-existing failure unrelated to this change)
- [x] No functional changes to the codebase

## Rollback Plan

1. Git commit before changes
2. Revert command: `git checkout HEAD -- src/main/java/org/garret/perst/impl/Bytes.java src/main/java/org/garret/perst/impl/ArrayPos.java`
3. Delete ArrayPos.java if it exists: `rm src/main/java/org/garret/perst/impl/ArrayPos.java`
4. Verify with `mvn compile`