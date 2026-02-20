# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Perst Code Quality Improvement

## Objective
Clean up deprecation warnings and improve code quality for long-term maintainability with OpenJDK 25+.

## Current State
- Java Version: 25 ✓
- Lucene Version: 9.11.0 ✓
- Tests: 288 passing ✓
- Deprecations: 25 warnings
- Unchecked warnings: 78

## Goals
1. Fix all deprecation warnings related to primitive wrapper constructors (~94 instances)
2. Fix finalize()/runFinalization() deprecations
3. Add missing @Deprecated annotations in Database.java
4. Enable strict compilation with -Xlint:all -Werror

## Current Issues Found

### Issue #1: Primitive Wrapper Constructors (94 instances)
**Severity:** High
**Locations:** Aggregator.java, RndBtree.java, QueryImpl.java, StorageImpl.java, AltBtree.java, Btree.java, etc.

Solution: Replace `new Integer/Long/Double/Byte/Short()` with `valueOf()`

### Issue #2: finalize() method
**Severity:** High
**Location:** Persistent.java:16

Solution: Already marked @Deprecated(forRemoval=true), needs alternative implementation

### Issue #3: System.runFinalization() (6 instances)
**Severity:** Medium
**Locations:** WeakHashTable.java, LruObjectCache.java

Solution: Replace with explicit resource management or Cleaner API

### Issue #4: Deprecated items without @Deprecated annotation
**Severity:** Medium
**Locations:** Database.java (4 methods)

Solution: Add @Deprecated annotations

---

## Risks & Rollback Plan

| Task | Risk | Rollback |
|------|------|----------|
| Replace primitive constructors | Runtime issues if done incorrectly | git revert |
| Fix finalize() | Memory leak if not replaced properly | Keep old code, test thoroughly |
| Enable -Werror | Build failures on warnings | Revert pom.xml changes |

---

## Dependencies
- All tasks depend on having working tests (288 tests currently passing)
- Run `mvn test` after each change per workingRules

## Environment
- Java: OpenJDK 25
- Build: Maven
- Platform: Linux/Windows

## Success Criteria
- [ ] All 288 tests pass
- [ ] Zero deprecation warnings from primitive wrappers
- [ ] finalize() replaced with modern alternative
- [ ] -Xlint:all -Werror enabled and passing

---

## Related Documentation
- `md/todo_improve.md` - Detailed task checklist
- `md/progress_improve.md` - Progress tracking
