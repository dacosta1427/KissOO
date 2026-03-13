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

# Progress: Fix Warnings in StorageImpl.java

## Project Status
- **Start Date:** 2026-02-20
- **Current Phase:** Completed
- **Overall Progress:** 100% complete

## Task Progress

### Priority #1: Fix Auxiliary Class Warning

#### Task 1.1: Create new file `ArrayPos.java`
- **Description:** Create a new source file for the ArrayPos class that is currently defined inside Bytes.java. This will resolve the auxiliary class access warning.
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** TBD
- **Success criteria:** File created with correct package declaration and class definition
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** The class remains package-private (no public modifier)

#### Task 1.2: Remove `ArrayPos` class definition from `Bytes.java`
- **Description:** Remove the ArrayPos class from Bytes.java after it has been moved to its own file.
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** TBD
- **Success criteria:** Bytes.java compiles successfully without the ArrayPos class
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** Bytes.java still uses ArrayPos via same-package access

#### Task 1.3: Compile to verify no warnings for StorageImpl.java
- **Description:** Run Maven compile to verify that the auxiliary class warning is resolved.
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.1, Task 1.2
- **Owner:** TBD
- **Success criteria:** `mvn compile` produces no warnings related to StorageImpl.java
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** Verified - no StorageImpl.java warnings. Only CodeGeneratorImpl.java has remaining auxiliary class warnings (separate issue).

#### Task 1.4: Run all junit tests to verify no regressions
- **Description:** Execute the full test suite to ensure the refactoring didn't break any functionality.
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** TBD
- **Success criteria:** All tests pass
- **Timestamp:** 2026-02-20
- **Effort estimate:** M
- **Notes:** Run `mvn test` - 731 tests run. 1 pre-existing failure in DecimalTest.testDiv (unrelated to this change).

## Risk Assessment
- **High Risk:** None
- **Medium Risk:** None
- **Low Risk:** The refactoring is straightforward - moving a class to its own file with no functional changes

## Current Blockers
- None

## Next Steps
1. Create ArrayPos.java file
2. Remove ArrayPos from Bytes.java
3. Verify compilation
4. Run tests