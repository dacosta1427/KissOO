# Agent Protocol (AP.md)

## Entry Point

**Read this first.** Then read `KISSOO_DEVELOPMENT_PROTOCOL.md` for complete rules.

---

## Quick Reference

### 1. Core Principles

| Principle | What It Means |
|-----------|---------------|
| **Memory Primary** | Read .memory/ docs before acting, write after |
| **Pure OO Navigation** | Never filter by ID, ask objects directly: `cleaner.getSchedules()` |
| **Manager at Gate** | All data access via Managers, never directly to Perst |
| **Session Auth** | Frontend sends ONLY `_uuid`, backend derives actor from session |

### 2. Critical Patterns

```groovy
// Get actor from session (NEVER from request params!)
PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
Actor actor = pu.getActor()

// Pure OO navigation (NEVER iterate + filter)
Cleaner cleaner = (Cleaner) actor
return cleaner.getSchedules()  // NOT: getAll().findAll{...}
```

```typescript
// Frontend sends ONLY _uuid
Server.call('services.Cleaning', 'getSchedules', {})
// NOT: { _uuid, _ownerId, _cleanerId, _isAdmin }
```

### 3. Memory System

| When | Action |
|------|--------|
| **Start** | Read AP.md + KISSOO_DEVELOPMENT_PROTOCOL.md + .memory/EXPERIENCE_LOG.md |
| **Before task** | Check .memory/PITFALL_REGISTRY.md, BEST_PRACTICES.md |
| **After task** | Update EXPERIENCE_LOG.md |
| **On failure** | Update PITFALL_REGISTRY.md |
| **On new pattern** | Update BEST_PRACTICES.md |

### 4. Git Workflow

```bash
git checkout -b feat/my-feature
git add . && git commit -m "feat: description"
git push -u origin feat/my-feature
git tag -a v1.0-YYYYMMDD -m "message"
git push origin v1.0-YYYYMMDD
```

---

## Escalation Triggers

Ask human when:
- Decision contradicts BEST_PRACTICES.md
- Pitfall unresolved after 3 attempts
- Public API changes
- Technical debt severity HIGH

---

## Files

| File | Purpose |
|------|---------|
| `KISSOO_DEVELOPMENT_PROTOCOL.md` | Master rules, patterns, anti-patterns |
| `.memory/EXPERIENCE_LOG.md` | Current state, iteration history |
| `.memory/PITFALL_REGISTRY.md` | Known failures with remedies |
| `.memory/BEST_PRACTICES.md` | Reusable patterns |
| `.memory/PROJECT_STATE.md` | Architecture, class diagram |

---

**Remember:** Your intelligence compounds through memory. Consult your past self through the documents.