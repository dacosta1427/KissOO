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

# Compilation Warning Report

Generated: 2026-02-20

## Summary

| Warning Type | Count | Status |
|-------------|-------|--------|
| Auxiliary Class | 5 | Planning complete |
| Raw Type | 94 | Not addressed |
| Deprecated API | 1 | Not addressed |
| **Total** | **100** | |

---

## 1. Auxiliary Class Warnings (5)

All from `CodeGeneratorImpl.java` accessing classes from `QueryImpl.java`:

| File | Line | Class Accessed |
|------|------|----------------|
| CodeGeneratorImpl.java | 634 | Node |
| CodeGeneratorImpl.java | 634 | BinOpNode |
| CodeGeneratorImpl.java | 634 | Node |
| CodeGeneratorImpl.java | 727 | Node |
| CodeGeneratorImpl.java | 781 | Node |

**Status:** Planning documentation created in `md/plan_fixCodeGeneratorImpl.md`

---

## 2. Raw Type Warnings (94)

### By File

| File | Count |
|------|-------|
| Aggregator.java | 65 |
| Storage.java | 28 |
| ITable.java | 1 |

### By Missing Generic Type

| Raw Type | Count | Should Be |
|----------|-------|-----------|
| `org.garret.perst.Aggregator.Aggregate` | 33 | `Aggregate<T>` |
| `java.lang.Class` | 22 | `Class<T>` |
| `java.lang.Comparable` | 21 | `Comparable<T>` |
| `java.util.Iterator` | 5 | `Iterator<E>` |
| `java.util.Set` | 1 | `Set<E>` |
| `java.util.HashSet` | 1 | `HashSet<E>` |

### Detailed Raw Type Issues

#### Aggregator.java (65 warnings)
- Uses raw `Aggregate` type instead of `Aggregate<T>`
- Uses raw `Comparable` instead of `Comparable<T>`

#### Storage.java (28 warnings)
- Uses raw `Class` instead of `Class<T>`
- Uses raw `Iterator` instead of `Iterator<E>`

#### ITable.java (1 warning)
- Uses raw `HashSet` instead of `HashSet<E>`

---

## 3. Deprecated API Warnings (1)

| File | Description |
|------|-------------|
| StorageImpl.java | Uses or overrides a deprecated API |

Note: The specific deprecated API was not detailed in the compile output.

---

## 4. External Warnings (Not Our Code)

Maven/Guava internal warnings (4 warnings about `sun.misc.Unsafe`):
- These come from Maven's Guava library
- Cannot be fixed by us
- Will be resolved when Maven/Guava updates

---

## Recommendations

### Priority 1: Auxiliary Class Warnings
- Already planned in `md/plan_fixCodeGeneratorImpl.md`
- Requires extracting Node classes from QueryImpl.java

### Priority 2: Raw Type Warnings (Optional)
- Fixing would improve type safety
- Most impactful fixes:
  1. `Aggregator.java` - Add generic parameter to `Aggregate` usage
  2. `Storage.java` - Add `<T>` to `Class` parameters
- Note: This is a significant refactoring task

### Priority 3: Deprecated API (Optional)
- Run `mvn compile -Xlint:deprecation` to identify the specific deprecated API
- Evaluate if the deprecated API has a modern replacement