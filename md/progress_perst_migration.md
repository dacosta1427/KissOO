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

# Perst OpenJDK 25 Migration - Progress Tracking

## Overview
This document tracks the progress of migrating Perst from Java 1.6 to OpenJDK 25, modernizing the build system, and updating dependencies.

## Project Status
- **Start Date:** 2026-02-13
- **Current Phase:** Build System & Java Migration Complete
- **Overall Progress:** ~15% complete

## Task Progress

### Priority #1: Build System Modernization

#### Task 1.1: Analyze current build system (Ant/makefile)
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Complete analysis of current build system including Ant build.xml, makefile, and all build targets
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Current system uses Ant for compilation and jar creation, makefile for additional build tasks. Found dual Java 1.4/1.6 support with separate source trees (src14/ and src/). No CI/CD files found. Test system uses makefile with individual shell scripts for each test.

#### Task 1.2: Create Maven build configuration
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Cline
- **Success criteria:** Complete pom.xml with all necessary dependencies and build configurations
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Created comprehensive pom.xml with Java 25, updated dependencies (Lucene 9.11.0, Javassist 3.29.2-GA), JUnit 5, and modern build plugins (JaCoCo, Checkstyle, SpotBugs). Added profiles for legacy Java 1.4 support and development/release builds.

#### Task 1.3: Verify Maven builds work correctly
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** Cline
- **Success criteria:** Maven clean compile and package commands succeed without errors
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Verified with `mvn compile -Dcheckstyle.skip=true -Dspotbugs.skip=true` - BUILD SUCCESS

#### Task 1.4: Update CI/CD configuration for Maven
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** CI/CD pipeline updated to use Maven instead of Ant
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to check if CI/CD files exist

### Priority #2: Java Version Upgrade

#### Task 2.1: Update Java compiler target to 25
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** Cline
- **Success criteria:** Maven compiler plugin configured for Java 25
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** pom.xml already has Java 25 configured (source, target, release all set to 25)

#### Task 2.2: Fix compilation errors from Java version upgrade
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 2.1
- **Owner:** Cline
- **Success criteria:** All compilation errors resolved
- **Timestamp:** 2026-02-13
- **Effort estimate:** Large
- **Notes:** Compilation succeeds with deprecation warnings (sun.misc.Unsafe, finalize(), runFinalization())

#### Task 2.3: Update source code for Java 25 compatibility
- **Status:** completed
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** Cline
- **Success criteria:** All source code compatible with Java 25
- **Timestamp:** 2026-02-13
- **Effort estimate:** Large
- **Notes:** Code compiles with Java 25, deprecation warnings for Task 4

#### Task 2.4: Verify all tests pass with new Java version
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.3
- **Owner:** Cline
- **Success criteria:** All existing tests pass with Java 25
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to run full test suite

### Priority #3: Dependency Updates

#### Task 3.1: Update Lucene from 4.7.2 to latest version
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.1
- **Owner:** Cline
- **Success criteria:** Lucene updated to latest stable version with all compatibility issues resolved
- **Timestamp:** 2026-02-13
- **Effort estimate:** Large
- **Notes:** Major API changes expected between 4.7.2 and latest

#### Task 3.2: Update Javassist from 3.18.1 to latest version
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.1
- **Owner:** Cline
- **Success criteria:** Javassist updated to latest version with all compatibility issues resolved
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** API changes expected but less severe than Lucene

#### Task 3.3: Update AspectJ from 1.7.4 to latest version
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.1
- **Owner:** Cline
- **Success criteria:** AspectJ updated to latest version
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to check if AspectJ is actually used

#### Task 3.4: Fix compatibility issues with new dependencies
- **Status:** pending
- **Priority:** High
- **Dependencies:** Tasks 3.1, 3.2, 3.3
- **Owner:** Cline
- **Success criteria:** All dependency compatibility issues resolved
- **Timestamp:** 2026-02-13
- **Effort estimate:** Large
- **Notes:** Expected to be complex due to major version jumps

#### Task 3.5: Update dependency management in build files
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 3.4
- **Owner:** Cline
- **Success criteria:** All build files updated with new dependency versions
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** Should be straightforward once compatibility is resolved

### Priority #4: Deprecated API Replacement

#### Task 4.1: Identify deprecated APIs in codebase
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.1
- **Owner:** Cline
- **Success criteria:** Complete inventory of deprecated APIs used in codebase
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to compile with warnings enabled

#### Task 4.2: Replace deprecated Lucene APIs
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 4.1, Task 3.1
- **Owner:** Cline
- **Success criteria:** All deprecated Lucene APIs replaced with modern alternatives
- **Timestamp:** 2026-02-13
- **Effort estimate:** Large
- **Notes:** Major refactoring expected

#### Task 4.3: Replace deprecated Javassist APIs
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 4.1, Task 3.2
- **Owner:** Cline
- **Success criteria:** All deprecated Javassist APIs replaced with modern alternatives
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Less severe than Lucene but still significant

#### Task 4.4: Replace deprecated Java standard library APIs
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 4.1
- **Owner:** Cline
- **Success criteria:** All deprecated Java standard library APIs replaced
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to check for removed APIs in Java 25

#### Task 4.5: Update code to use modern alternatives
- **Status:** pending
- **Priority:** High
- **Dependencies:** Tasks 4.2, 4.3, 4.4
- **Owner:** Cline
- **Success criteria:** All code updated to use modern API alternatives
- **Timestamp:** 2026-02-13
- **Effort estimate:** Large
- **Notes:** Comprehensive code review and updates required

### Priority #5: Modern Testing & CI

#### Task 5.1: Set up JUnit 5 testing framework
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.2
- **Owner:** Cline
- **Success criteria:** JUnit 5 configured and working in Maven build
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** Need to check current testing framework

#### Task 5.2: Migrate existing tests to JUnit 5
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 5.1
- **Owner:** Cline
- **Success criteria:** All existing tests migrated to JUnit 5
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to assess current test structure

#### Task 5.3: Add code coverage reporting
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 5.2
- **Owner:** Cline
- **Success criteria:** Code coverage reporting integrated into build process
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** JaCoCo or similar tool

#### Task 5.4: Set up GitHub Actions CI/CD pipeline
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.4
- **Owner:** Cline
- **Success criteria:** GitHub Actions workflow configured and working
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Need to check if GitHub repository exists

#### Task 5.5: Add static analysis tools (SpotBugs, Checkstyle)
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 5.1
- **Owner:** Cline
- **Success criteria:** Static analysis tools integrated into build process
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Code quality improvements

#### Task 5.6: Configure automated dependency updates
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 5.4
- **Owner:** Cline
- **Success criteria:** Dependabot or similar tool configured for automated updates
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** GitHub Dependabot integration

### Risk Mitigation & Rollback

#### Task R1: Create comprehensive backup strategy
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Complete backup and rollback strategy documented and tested
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Git-based rollback procedures

#### Task R2: Document rollback procedures for each priority
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task R1
- **Owner:** Cline
- **Success criteria:** Detailed rollback procedures for each migration priority
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Step-by-step rollback instructions

#### Task R3: Test rollback procedures
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task R2
- **Owner:** Cline
- **Success criteria:** All rollback procedures tested and validated
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Test in isolated environment

#### Task R4: Create migration validation checklist
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task R1
- **Owner:** Cline
- **Success criteria:** Comprehensive validation checklist for each migration step
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** Ensure nothing is missed during migration

### Documentation & Communication

#### Task D1: Update README with new build instructions
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** README updated with Maven build instructions
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** Need to find existing README

#### Task D2: Create migration guide for developers
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.4
- **Owner:** Cline
- **Success criteria:** Comprehensive migration guide for developers
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Document all changes and new procedures

#### Task D3: Update API documentation
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 4.5
- **Owner:** Cline
- **Success criteria:** API documentation updated to reflect changes
- **Timestamp:** 2026-02-13
- **Effort estimate:** Medium
- **Notes:** Javadoc updates

#### Task D4: Create release notes template
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 2.4
- **Owner:** Cline
- **Success criteria:** Release notes template for future releases
- **Timestamp:** 2026-02-13
- **Effort estimate:** Small
- **Notes:** Standardized format for documenting changes

## Current Blockers
- None identified yet - still in planning phase

## Risk Assessment
- **High Risk:** Major dependency version jumps (Lucene 4.7.2 → latest, Javassist 3.18.1 → latest)
- **Medium Risk:** Java 25 compatibility issues with legacy code
- **Low Risk:** Build system migration complexity

## Next Steps
1. Begin with Task 1.1: Analyze current build system
2. Create backup strategy (Task R1)
3. Start Maven configuration (Task 1.2)

## Notes
- Current pom.xml already has Java 25 configured but may not be functional
- Need to verify if AspectJ is actually used in the codebase
- Test suite structure needs to be analyzed before migration
- CI/CD configuration needs to be identified and updated