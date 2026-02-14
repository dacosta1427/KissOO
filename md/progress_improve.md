# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Progress: Perst Deprecated API Fixes

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** Planning Complete
- **Overall Progress:** 0% complete

## Task Progress

### Priority #1: Class.newInstance() Replacement

#### Task 1.1: Create utility class for object instantiation
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Utility class created with newInstance() method that uses Constructor.newInstance()
- **Timestamp:** -
- **Effort estimate:** Small
- **Notes:** Will wrap checked exceptions and provide consistent API

#### Task 1.2-1.14: Fix newInstance() calls throughout codebase
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Cline
- **Success criteria:** All Class.newInstance() and Array.newInstance() calls updated
- **Timestamp:** -
- **Effort estimate:** Large
- **Notes:** 40+ occurrences across multiple files

### Priority #2: finalize() Replacement

#### Task 2.1-2.4: Replace finalize() with Cleaner
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Cline
- **Success criteria:** finalize() removed, Cleaner-based cleanup implemented
- **Timestamp:** -
- **Effort estimate:** Medium
- **Notes:** Persistent.java is central class - changes may affect subclasses

### Priority #3: runFinalization() Replacement

#### Task 3.1-3.3: Remove runFinalization() calls
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.1
- **Owner:** Cline
- **Success criteria:** All 6 runFinalization() calls removed
- **Timestamp:** -
- **Effort estimate:** Small
- **Notes:** WeakHashTable and LruObjectCache - cleanup handled by reference management

### Priority #4: Verification

#### Task 4.1-4.4: Test and verify changes
- **Status:** pending
- **Priority:** High
- **Dependencies:** Tasks 1.x, 2.x, 3.x
- **Owner:** Cline
- **Success criteria:** All tests pass, no deprecation warnings
- **Timestamp:** -
- **Effort estimate:** Medium
- **Notes:** Must run full test suite to verify no regressions

## Risk Assessment
- **High Risk:** Changes to Persistent.java may affect all persistent classes
- **Medium Risk:** Exception handling changes may alter behavior
- **Low Risk:** Utility wrapper provides consistent API

## Current Blockers
- None - ready to start implementation

## Next Steps
1. Start with Task 1.1: Create utility class for object instantiation
2. Fix ClassDescriptor.java newInstance() method
3. Proceed through remaining files systematically
4. Run tests after each major change
