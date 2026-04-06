# Agent Protocol (AP.md)

## Project Context

**Generic Protocol:** 100% Object-Oriented development with persistent memory

**Project-Specific:** See `.memory/PROJECT_DESCRIPTION_*.md` for project details.

This protocol guides AI agents in KissOO projects. Project-specific context (what, why, goals) 
is maintained in `.memory/PROJECT_DESCRIPTION_KissOO.md`.

---

## Entry Point

**Read this first.** Then read `KISSOO_DEVELOPMENT_PROTOCOL.md` for complete rules.

---

## What Is Expected of You

1. **Before ANY action** → READ AP.md + KISSOO_DEVELOPMENT_PROTOCOL.md + .memory/ protocol docs
2. **Check for current plan** → Look in `.currentPLAN/` for active work
3. **After ANY meaningful change** → UPDATE .memory/ docs
4. **NEVER commit directly to main**
5. **ALWAYS push after commit**
6. **When stuck** → consult PITFALL_REGISTRY.md before asking human

---

## How To Do It

1. Read AP.md (this file)
2. Read `KISSOO_DEVELOPMENT_PROTOCOL.md` for complete rules
3. Check current state in `.memory/EXPERIENCE_LOG.md`
4. Check `.currentPLAN/` for active tasks
5. Create feature branch
6. Execute: READ → ACT → WRITE loop
7. Commit, push, tag

## Memory System

| Doc | Purpose | When Read | When Write |
|-----|---------|-----------|------------|
| `KISSOO_DEVELOPMENT_PROTOCOL.md` | Master rules, patterns | Start | Never (unless protocol change) |
| `PROJECT_DESCRIPTION_*.md` | What/why/goal | Start | Core identity changes |
| `EXPERIENCE_LOG.md` | Current state | Always | Every iteration |
| `PITFALL_REGISTRY.md` | Failures | When stuck | After failure |
| `BEST_PRACTICES.md` | Patterns | When designing | New pattern discovered |
| `PROJECT_STATE.md` | Architecture | When modifying | Architecture changes |
| `DECISION_LOG.md` | Rationale | When deciding | Decision made |

**Extended Docs:** ACTION_AUDIT.md, ARCHIVED_PITFALLS.md, PROJECT_EVOLUTION.md

---

## Critical Rules

- **Memory is primary, code is secondary**
- **Simple > Complex, Elegant > Clever, Readable > Clever**
- **Manager at the Gate** - go through subsystem Managers
- **Pure OO Navigation** - never use SQL patterns
- **Session-Based Auth** - frontend sends only _uuid
- **Branch → Commit → Push → Tag**
- **Every decision traceable, every failure captured**

## Git Workflow

```
feat/xxx     # New feature
fix/xxx      # Bug fix
test/xxx     # Test development

git checkout -b feat/my-feature
git add . && git commit -m "feat: description"
git push -u origin feat/my-feature
# ... work ...
git tag -a v1.0-YYYYMMDD -m "message"
git push origin v1.0-YYYYMMDD
```

## Current Plan Execution

`.currentPLAN/` contains the active plan to execute. Supported prefixes:
- `FEAT_` - Feature implementation
- `CHG_` - Change request
- `PLAN_` - General plan

### Execution Flow

```
1. Read AP.md (this file)
2. Read KISSOO_DEVELOPMENT_PROTOCOL.md
3. Read all .memory/ protocol docs
4. Check .currentPLAN/ for active plan
5. If plan found → Execute tasks from plan
   - Create todo_*.md from plan tasks
   - Track progress in progress_*.md
   - Update EXPERIENCE_LOG.md with current task
6. If NO plan → Ask human: "What should I work on?"
```

### While Executing

- Work through tasks sequentially
- Update progress after each task
- Mark tasks complete: `- [x]`
- If blocked → escalate to human
- When plan complete → move to REQUESTS/ as COMPLETED

### Plan Templates

See `REQUESTS/templates/PLAN_template.md` for plan format.
See `REQUESTS/templates/todo_template.md` for todo format.
See `REQUESTS/templates/progress_template.md` for progress format.

### Plan Execution Checklist

When a plan is placed in `.currentPLAN/` (with prefix FEAT_, CHG_, PLAN_):

```
□ Read the plan in .currentPLAN/
□ Create todo_<plan-name>.md from REQUESTS/templates/todo_template.md
□ Create progress_<plan-name>.md from REQUESTS/templates/progress_template.md
□ Execute tasks one by one
□ Update progress after each task completion
□ When complete: move plan to REQUESTS/ as COMPLETED
```

### Todo Template

```markdown
# Todo: <Plan Name>

## Tasks

### Phase 1: <Name>
- [ ] Task 1.1: <description>
- [ ] Task 1.2: <description>

### Phase 2: <Name>
- [ ] Task 2.1: <description>
```

### Progress Template

```markdown
# Progress: <Plan Name>

## Status
- **Start Date:** YYYY-MM-DD
- **Overall Progress:** 0% complete

## Task Progress

### Task 1.1: <Name>
- **Status:** pending | in_progress | completed | failed
- **Started:** YYYY-MM-DD
- **Completed:** YYYY-MM-DD
- **Notes:** <details>
```

## Request System

**REQUESTS/** - Central location for bugs, features, and change requests  
**.currentPLAN/** - Active plan to execute NOW

| Type | Path | Prefix |
|------|------|--------|
| Bugs | `REQUESTS/bugs/` | BUG- |
| Features | `REQUESTS/features/` | FEAT- |
| Changes | `REQUESTS/changes/` | CHG- |
| Plans | `.currentPLAN/` | FEAT_ / CHG_ / PLAN_ |

## Escalation Triggers

Ask human when:
- Decision contradicts BEST_PRACTICES.md
- Pitfall unresolved after 3 attempts
- Public API changes
- Technical debt severity HIGH
- New pattern affects >3 existing classes

## First Action Checklist

- [ ] Read AP.md (this file)
- [ ] Read `KISSOO_DEVELOPMENT_PROTOCOL.md`
- [ ] Read `.memory/PROJECT_DESCRIPTION_*.md`
- [ ] Read `.memory/EXPERIENCE_LOG.md`
- [ ] Read `.memory/PITFALL_REGISTRY.md`
- [ ] Read `.memory/BEST_PRACTICES.md`
- [ ] Read `.memory/PROJECT_STATE.md`
- [ ] Check `.currentPLAN/` for active tasks
- [ ] Create feature branch

---

## Files Reference

| File | Purpose |
|------|---------|
| `KISSOO_DEVELOPMENT_PROTOCOL.md` | Master rules, patterns, anti-patterns |
| `.memory/EXPERIENCE_LOG.md` | Current state, iteration history |
| `.memory/PITFALL_REGISTRY.md` | Known failures with remedies |
| `.memory/BEST_PRACTICES.md` | Reusable patterns |
| `.memory/PROJECT_STATE.md` | Architecture, class diagram |
| `.memory/DECISION_LOG.md` | Architectural decisions |
| `.currentPLAN/` | Active plan to execute |

---

**Remember:** Your intelligence compounds through memory. Consult your past self through the documents.