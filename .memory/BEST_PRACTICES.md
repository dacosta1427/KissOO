# Best Practices Compendium

## OO Design Patterns

### PAT-<XXX>: <Pattern Name>
- **Intent:** <what problem it solves>
- **Applicability:** <when to use>
- **Structure:** <textual class diagram>
- **Participants:** table of class/role/responsibilities
- **Consequences:** ✅ benefits | ⚠️ trade‑offs | ❌ limitations
- **Usage:** `<Class.method()>` – context
- **Related Patterns:** <links>

## Design Heuristics

### Rule: Pure OO Navigation
- **Condition:** When accessing related objects
- **Action:** Use object references, never filter by ID
- **Rationale:** Maintains OO integrity, leverages Perst relationships

### Rule: Manager at the Gate
- **Condition:** When accessing storage
- **Action:** Use Manager classes, never direct Perst access
- **Rationale:** Centralizes validation, authorization, transactions

### Rule: Session-Based Authorization
- **Condition:** When handling authenticated requests
- **Action:** Get actor from session, never from request params
- **Rationale:** Security, prevents parameter tampering

## Project‑Specific Rules

| ID | Rule | Applied To | Last Verified |
|----|------|------------|---------------|
| OO-001 | Use object.getXxxOid() not getXxxId() | Domain classes | |
| OO-002 | Add collections to domain for bidirectional navigation | Owner, Cleaner, House | |
| OO-003 | Use select() not getRecords() for retrieval | Services | |

## Anti-Patterns

(Created when a tag appears ≥5 times in PITFALL_REGISTRY)

### Anti-Pattern: SQL-Style Filtering
- **Symptom:** Services use `getAll().findAll{ it.getXxx() == id }`
- **Root Cause:** Thinking in SQL terms, not OO
- **Alternative:** Use `actor.getRelatedObjects()` directly
- **Related Pitfalls:** PIT-XXX (iterating to filter)

### Anti-Pattern: Frontend Sends IDs
- **Symptom:** API calls include _ownerId, _cleanerId, _isAdmin
- **Root Cause:** Not leveraging session-based auth
- **Alternative:** Backend derives from session
- **Related Pitfalls:** PIT-XXX (parameter pollution)

## Common Patterns Quick Reference

| Pattern | When to Use | Example |
|---------|-------------|---------|
| Manager | Storage access | `PerstUserManager.getByUsername()` |
| OO Navigation | Related objects | `cleaner.getSchedules()` |
| Session Auth | Authorization | `servlet.getUserData("perstUser")` |
| TransactionContainer | Batch operations | `tc.addInsert(obj); store(tc)` |