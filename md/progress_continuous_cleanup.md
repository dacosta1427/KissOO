# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

> See [workingRules.md](./workingRules.md) for core operating principles

---

# Progress: Continuous Directory Code Cleanup

## Project Status
- **Start Date:** 2026-02-17
- **Current Phase:** Analyzing issues
- **Overall Progress:** 0% complete

## Task Progress

### Priority #1: Fix Lucene API Errors

#### Task 1.1: CDatabase.java
- **Description:** Fix 9 compile errors due to Lucene 9.x API changes - queryParser and analysis packages removed/reorganized
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** -
- **Success criteria:** CDatabase.java compiles without errors
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Needs Lucene 9.x query parser update

#### Task 1.2: FullTextSearchIterator.java
- **Description:** Fix 4 compile errors due to Lucene 9.x search API changes
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** -
- **Success criteria:** FullTextSearchIterator.java compiles without errors
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** 

#### Task 1.3: PerstDirectory.java
- **Description:** Fix 3 compile errors and 3 warnings - Lucene 9.x Directory API changes
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** -
- **Success criteria:** PerstDirectory.java compiles without errors
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** 

#### Task 1.4: TableDescriptor.java
- **Description:** Fix 5 compile errors and 3 warnings - Lucene 9.x API changes
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** -
- **Success criteria:** TableDescriptor.java compiles without errors
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** 

### Priority #2: Fix Type Warnings

#### Task 2.1: CVersion.java
- **Description:** Fix 2 unchecked conversion warnings by adding proper generic type parameters
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** -
- **Success criteria:** CVersion.java compiles without warnings
- **Timestamp:** -
- **Effort estimate:** S
- **Notes:** 

#### Task 2.2: CVersionHistory.java
- **Description:** Fix 2 unchecked cast warnings by adding proper generic type parameters
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** -
- **Success criteria:** CVersionHistory.java compiles without warnings
- **Timestamp:** -
- **Effort estimate:** S
- **Notes:** 

#### Task 2.3: ExtentIterator.java
- **Description:** Fix 1 unchecked cast warning by adding proper generic type parameters
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** -
- **Success criteria:** ExtentIterator.java compiles without warnings
- **Timestamp:** -
- **Effort estimate:** S
- **Notes:** 

#### Task 2.4: IndexIterator.java
- **Description:** Fix 1 unchecked cast warning by adding proper generic type parameters
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** -
- **Success criteria:** IndexIterator.java compiles without warnings
- **Timestamp:** -
- **Effort estimate:** S
- **Notes:** 

### Priority #3: Verification

#### Task 3.1: Run compile
- **Description:** Run mvn compile to verify all errors are fixed
- **Status:** pending
- **Priority:** High
- **Dependencies:** Tasks 1.1-1.4
- **Owner:** -
- **Success criteria:** mvn compile succeeds with no errors
- **Timestamp:** -
- **Effort estimate:** S
- **Notes:** 

#### Task 3.2: Run tests
- **Description:** Run all JUnit tests to ensure no regressions
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 3.1
- **Owner:** -
- **Success criteria:** All JUnit tests pass
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** 

## Risk Assessment
- **High Risk:** Lucene API changes are significant between 4.x and 9.x - may require significant refactoring
- **Medium Risk:** Some deprecated APIs may have no direct replacement in Lucene 9.x
- **Low Risk:** Type warnings are straightforward to fix

## Current Blockers
- None - analysis complete

## Next Steps
1. Start fixing CDatabase.java Lucene errors
2. Update to Lucene 9.x compatible APIs
3. Fix type warnings
