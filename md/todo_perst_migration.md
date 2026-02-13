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

# Perst OpenJDK 25 Migration - Todo List

## Overview
This document contains all tasks required to migrate Perst from Java 1.6 to OpenJDK 25, modernize the build system, and update dependencies.

## Task Dependencies
```
Priority #1 (Build) → Priority #2 (Java) → Priority #3 (Dependencies) → Priority #4 (Deprecated APIs) → Priority #5 (Testing)
```

## Tasks

### Priority #1: Build System Modernization
- [ ] Task 1.1: Analyze current build system (Ant/makefile)
- [ ] Task 1.2: Create Maven build configuration
- [ ] Task 1.3: Verify Maven builds work correctly
- [ ] Task 1.4: Update CI/CD configuration for Maven

### Priority #2: Java Version Upgrade
- [ ] Task 2.1: Update Java compiler target to 25
- [ ] Task 2.2: Fix compilation errors from Java version upgrade
- [ ] Task 2.3: Update source code for Java 25 compatibility
- [ ] Task 2.4: Verify all tests pass with new Java version

### Priority #3: Dependency Updates
- [ ] Task 3.1: Update Lucene from 4.7.2 to latest version
- [ ] Task 3.2: Update Javassist from 3.18.1 to latest version
- [ ] Task 3.3: Update AspectJ from 1.7.4 to latest version
- [ ] Task 3.4: Fix compatibility issues with new dependencies
- [ ] Task 3.5: Update dependency management in build files

### Priority #4: Deprecated API Replacement
- [ ] Task 4.1: Identify deprecated APIs in codebase
- [ ] Task 4.2: Replace deprecated Lucene APIs
- [ ] Task 4.3: Replace deprecated Javassist APIs
- [ ] Task 4.4: Replace deprecated Java standard library APIs
- [ ] Task 4.5: Update code to use modern alternatives

### Priority #5: Modern Testing & CI
- [ ] Task 5.1: Set up JUnit 5 testing framework
- [ ] Task 5.2: Migrate existing tests to JUnit 5
- [ ] Task 5.3: Add code coverage reporting
- [ ] Task 5.4: Set up GitHub Actions CI/CD pipeline
- [ ] Task 5.5: Add static analysis tools (SpotBugs, Checkstyle)
- [ ] Task 5.6: Configure automated dependency updates

### Risk Mitigation & Rollback
- [ ] Task R1: Create comprehensive backup strategy
- [ ] Task R2: Document rollback procedures for each priority
- [ ] Task R3: Test rollback procedures
- [ ] Task R4: Create migration validation checklist

### Documentation & Communication
- [ ] Task D1: Update README with new build instructions
- [ ] Task D2: Create migration guide for developers
- [ ] Task D3: Update API documentation
- [ ] Task D4: Create release notes template

## Success Criteria
- [ ] All tests pass with OpenJDK 25
- [ ] Maven builds successfully
- [ ] All dependencies updated to current versions
- [ ] No deprecated API warnings
- [ ] CI/CD pipeline working
- [ ] Code coverage maintained or improved
- [ ] Build time optimized
- [ ] Documentation updated

## Rollback Plan
Each priority has its own rollback procedure:
1. **Build System**: Keep makefile as backup, revert to Ant if Maven fails
2. **Java Version**: Revert compiler settings to 1.6 if compilation fails
3. **Dependencies**: Revert pom.xml to old versions if compatibility issues
4. **APIs**: Keep old implementations as fallback if new APIs fail
5. **Testing**: Keep old test framework as backup during migration

## Monitoring & Validation
- [ ] Pre-migration baseline measurements
- [ ] Post-migration performance validation
- [ ] Continuous monitoring of build success
- [ ] Regular dependency vulnerability scans
- [ ] Code quality metrics tracking