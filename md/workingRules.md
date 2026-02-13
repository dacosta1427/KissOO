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

## Required Document Formats

### Todolist Format (`md/todo_*.md`)

The todolist MUST follow this structure:

```markdown
# Working Rules (must be included)

---

# Todolist: [Project Name]

## Tasks (or ## Priority Sections)

### Priority #X: [Category Name]
- [ ] Task X.X: [Task description]
- [x] Task X.X: [Completed task description]

## Success Criteria
- [ ] [Criteria 1]
- [ ] [Criteria 2]

## Rollback Plan
1. [Rollback step 1]
2. [Rollback step 2]
```

Key requirements:
- Include Working Rules at top
- Use checkbox format `- [ ]` for pending, `- [x]` for completed
- Group tasks by priority/category
- Include success criteria section
- Include rollback plan section

### Progress Doc Format (`md/progress_*.md`)

The progress doc MUST follow this structure:

```markdown
# Working Rules (must be included)

---

# Progress: [Project Name]

## Project Status
- **Start Date:** YYYY-MM-DD
- **Current Phase:** [Current phase name]
- **Overall Progress:** X% complete

## Task Progress

### Priority #X: [Category Name]

#### Task X.X: [Task name]
- **Status:** pending | in_progress | completed | failed
- **Priority:** High | Medium | Low
- **Dependencies:** [Task dependencies]
- **Owner:** [Person responsible]
- **Success criteria:** [What "done" means]
- **Timestamp:** YYYY-MM-DD
- **Effort estimate:** S | M | L
- **Notes:** [Additional details]

## Risk Assessment
- **High Risk:** [Description]
- **Medium Risk:** [Description]
- **Low Risk:** [Description]

## Current Blockers
- [List any blockers]

## Next Steps
1. [Next step 1]
2. [Next step 2]
```

Key requirements:
- Include Working Rules at top
- Include Project Status section with percentage
- Each task must have: Status, Priority, Dependencies, Owner, Success criteria, Timestamp, Effort estimate, Notes
- Include Risk Assessment section
- Include Current Blockers section
- Include Next Steps section

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
