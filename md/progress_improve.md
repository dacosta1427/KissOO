# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Progress: Perst Deprecated API Fixes

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** Completed
- **Overall Progress:** 100% complete

## Task Progress

### Priority #1: Class.newInstance() Replacement

#### Task 1.1: Fix ClassDescriptor.java line 318
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Replace Class.newInstance() with Constructor.newInstance()
- **Timestamp:** 2026-02-14
- **Effort estimate:** Small
- **Notes:** Changed `c.newInstance()` to `c.getDeclaredConstructor().newInstance()`

### Priority #2: finalize() Replacement

#### Task 2.1: Add @SuppressWarnings to Persistent.java
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Suppress deprecation warning for finalize()
- **Timestamp:** 2026-02-14
- **Effort estimate:** Small
- **Notes:** Added @SuppressWarnings("deprecation") to finalize() method

### Priority #3: runFinalization() Replacement

#### Task 3.1-3.3: Add @SuppressWarnings to methods in WeakHashTable.java and LruObjectCache.java
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Suppress deprecation warnings for runFinalization() calls
- **Timestamp:** 2026-02-14
- **Effort estimate:** Small
- **Notes:** Added @SuppressWarnings("deprecation") to get(), flush(), and invalidate() methods

### Priority #4: Verification

#### Task 4.1-4.3: Test and verify changes
- **Status:** completed
- **Priority:** High
- **Dependencies:** Tasks 1.x, 2.x, 3.x
- **Owner:** Cline
- **Success criteria:** All tests pass
- **Timestamp:** 2026-02-14
- **Effort estimate:** Medium
- **Notes:** All 160 JUnit tests pass

## Summary of Changes

| File | Change |
|------|--------|
| ClassDescriptor.java | Replaced Class.newInstance() with Constructor.newInstance() |
| Persistent.java | Added @SuppressWarnings("deprecation") to finalize() |
| WeakHashTable.java | Added @SuppressWarnings("deprecation") to get(), flush(), invalidate() |
| LruObjectCache.java | Added @SuppressWarnings("deprecation") to get(), flush(), invalidate() |

## Risk Assessment
- **High Risk:** None - changes are backward compatible
- **Medium Risk:** None - minimal code changes
- **Low Risk:** None - tests verify functionality

## Current Blockers
- None - all tasks completed

## Next Steps
1. Consider future migration to Cleaner API when Java removes finalize() completely
2. No further action required at this time
