# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Progress: Perst Code Quality Improvement

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** In Progress
- **Overall Progress:** ~70% complete

## Current Analysis (Feb 2026)
- Tests: 288 passing ✓
- Deprecation warnings: 25
- Unchecked warnings: 78
- Major issue: ~94 instances of primitive wrapper constructors (new Integer/Long/Double/etc.)

## Task Progress

### Priority #1: Fix Primitive Wrapper Constructors (~94 instances)

All tasks completed - replaced ~94 instances of `new Integer/Long/Double/Byte/Short()` with `valueOf()`:

| File | Instances Fixed |
|------|----------------|
| Aggregator.java | 11 |
| RndBtree.java | 5 |
| QueryImpl.java | 27 |
| StorageImpl.java | 8 |
| AltBtree.java | 5 |
| Btree.java | 5 |
| BtreeCompoundIndex.java | 5 |
| BtreeMultiFieldIndex.java | 5 |
| AltBtreeCompoundIndex.java | 2 |
| FullTextIndexImpl.java | 1 |
| OSFile.java | 2 |

**Status:** completed
- **Deprecation warnings reduced from 25 to 10**
- All 288 tests passing

### Priority #2: finalize() and runFinalization()
- **Status:** completed (previous work)
- **Notes:** Already has @Deprecated/@SuppressWarnings

### Priority #3: Add @Deprecated Annotations
- **Status:** pending
- **Priority:** Medium
- **Effort estimate:** S

### Priority #4: Enable Strict Compilation
- **Status:** pending
- **Priority:** Medium
- **Effort estimate:** M

## Risk Assessment
- **High Risk:** None - valueOf() is drop-in replacement
- **Medium Risk:** None - minimal changes per file
- **Low Risk:** Tests verify functionality

## Current Blockers
- None

## Next Steps
1. Start Task 1.1: Fix Aggregator.java
2. Run tests after each file fix
3. Progress through remaining files
4. Enable strict compilation last
