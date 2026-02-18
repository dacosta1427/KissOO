# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

## Core Principles

1. **All MD documentation lives in the `md/` directory**

2. **Every plan MUST be accompanied by:**
   - A **todolist** (`md/todo_*.md`) containing all items/steps to implement the plan
   - A **progress doc** (`md/progress_*.md`) containing all tasks with their current state

3. **Working rules at top** - This ensures they are never forgotten

---

## Task States

| State | Description |
|-------|-------------|
| `pending` | Not yet started |
| `in_progress` | Currently being worked on |
| `completed` | Successfully finished |
| `failed` | Failed with reason documented |

---

## Failed Task Retry Policy

When a task **fails**:

1. **Document the failure in BOTH places with FULL details:**
   
   | Location | What to Record |
   |----------|----------------|
   | **Todolist** | Task remains visible with: attempt count, failure reason (brief), current status |
   | **Progress doc** | Full failure details: exact error, stack trace, root cause, what was learned |

2. **Retry up to 5 times**
3. **Each retry must include:**
   - The original failure reason
   - What was learned from previous attempts
   - New approach being tried
4. **Only mark as failed (permanent) after 5 unsuccessful attempts**

> **Example todolist entry after failure:**
> ```markdown
> - [ ] Task: Migrate to Maven (attempt 2/5) - FAILED: Dependency conflict with Lucene
> ```

> **Example progress entry after failure:**
> ```markdown
> ## Task: Migrate to Maven
> - **Attempt:** 2/5
> - **Status:** FAILED
> - **Failure reason:** `Cannot resolve org.apache.lucene:lucene-core:4.7.2`
> - **Root cause:** Old Lucene version not available in Maven Central
> - **Solution for next attempt:** Upgrade to Lucene 9.x and update code
> ```

> **Key point:** The task stays in the todolist across all retries with the failure reason visible. Each failure gets full documentation in progress.
>
> **IMPORTANT:** When re-executing a failed task, ALWAYS check the progress doc for full failure details, root cause, and learnings from previous attempts.

---

## Enhanced Task Tracking

| Field | Description |
|-------|-------------|
| **Timestamp** | When task started/ended |
| **Effort estimate** | Time or complexity (S/M/L) |
| **Priority** | High/Medium/Low |
| **Dependencies** | Tasks that must complete first |
| **Owner** | Person responsible |
| **Success criteria** | What "done" means |
| **Status** | Current state |

---

## Workflow Template

```
Plan → [Todolist + Progress Doc] → Execution → Update Progress
```

1. Create/modify plan in `md/`
2. Create todolist (`md/todo_*.md`)
3. Create progress doc (`md/progress_*.md`)
4. Execute tasks, update progress after each
5. If failed: retry with learned knowledge (up to 5x)

---

## Additional Guidelines

### Risk Assessment
- Identify potential blockers early in planning
- Document what could go wrong before starting
- Consider dependencies and downstream effects

### Rollback Plan
Before starting any task, define how to revert if it fails:

| Rollback Element | Description |
|------------------|--------------|
| **Checkpoint** | What state to save before starting (git commit, backup) |
| **Revert command** | Exact command to restore previous state |
| **Verification** | How to confirm rollback succeeded |
| **Impact assessment** | What gets affected by rollback |

> **Example rollback plan:**
> ```markdown
> ## Task: Migrate build system to Maven
> - **Checkpoint:** Git commit before changes
> - **Revert command:** `git revert HEAD` or `git reset --hard HEAD`
> - **Verification:** Run `make` and confirm builds
> - **Impact:** All developers will need to switch to Maven
> ```

**Rollback Best Practices:**
1. **Always commit/save state before risky changes**
2. **Test rollback procedure beforehand** if possible
3. **Keep rollback simple** - complex rollbacks are error-prone
4. **Document time to rollback** - know how long recovery takes
5. **Communicate rollback** - notify team if rollback affects others

### Code Review
- Require review before marking tasks complete
- Use peer review for critical changes

### Verification
- Test/verify after each task completion
- Run existing tests to ensure no regressions

### Blocker Tracking
- Note what's preventing task progress
- Escalate if blocker persists

---

# Plan: JUnit Test Conversion

## Objective
Convert shell-based demo tests in `tst/` directory to JUnit 5 tests in `junit_tests/src/` directory, following the existing pattern.

## Current State
- **JUnit Tests Location:** `junit_tests/src/org/garret/perst/`
- **Already Converted:** TestList (6 tests), TestIndex (7 tests), TestMap (6 tests) = 19 tests
- **Demo Tests Location:** `tst/` - 58 shell-based test files with main() methods
- **Maven Config:** pom.xml with JUnit 5.11.3 configured
- **Build System:** Maven with Java 25

## Goals
1. Convert high-value demo tests to JUnit 5
2. Ensure all tests pass with `mvn test`
3. Maintain test coverage for core Perst functionality
4. Follow existing JUnit test patterns

## Test Conversion Pattern

Existing JUnit tests follow this pattern:
```java
package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class TestList {
    private Storage storage;
    private static final String TEST_DB = "testlist.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test description")
    void testMethod() {
        // Test implementation
    }
}
```

## Conversion Priority

### Priority #1: Core Functionality Tests (High Value)
- TestAgg - Aggregation functions
- TestBackup - Backup/restore functionality
- TestBlob - BLOB storage
- TestGC - Garbage collection

### Priority #2: Index and Query Tests (Medium Value)
- TestJSQL - JSQL query language
- TestIndex2 - B-tree alternatives
- TestSet - Set operations

### Priority #3: Advanced Features (Lower Priority)
- TestFullTextIndex - Lucene integration
- TestCodeGenerator - Code generation
- TestDynamicObjects - Dynamic classes
- TestAutoIndices - Auto-indexing

### Priority #4: Remaining Tests
- Convert remaining demo tests as time permits

## Risks & Rollback Plan

| Task | Risk | Rollback |
|------|------|----------|
| Test conversion | Test failures after conversion | Revert individual test file changes |
| Database cleanup | Leftover .dbs files | Run cleanup manually |
| Performance | Slower tests with in-memory | Use temp files if needed |

---

## Dependencies
- Maven build must succeed: `mvn compile -Dcheckstyle.skip=true -Dspotbugs.skip=true`
- JUnit 5.11.3 configured in pom.xml
- Existing tests in junit_tests/src/ must continue to pass

## Verification
- Run `mvn test` after each test conversion
- Verify database cleanup works properly
- Ensure no regressions in existing tests
