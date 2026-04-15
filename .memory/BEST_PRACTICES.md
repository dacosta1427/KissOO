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
| OO-004 | Handle OID changes after versioning - navigate to new OID | Frontend detail pages | 2026-04-08 |
| OO-005 | Always reload list pages after form changes to get fresh OIDs | List pages | 2026-04-08 |
| OO-006 | Use Set<T> collections in domain classes for implicit filtering | Domain classes | 2026-04-08 |
| SV5-001 | Use [id] folder notation for detail routes | Frontend routes | 2026-04-07 |
| SV5-002 | Use goto() from $app/navigation for routing | Frontend routes | 2026-04-07 |
| JSON-001 | Validate JSON after manual editing | i18n files | 2026-04-07 |
| UI-001 | Gray-out update buttons when no changes to prevent unnecessary versioning | Forms | 2026-04-08 |

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

### Anti-Pattern: Manual JSON Editing
- **Symptom:** Translation keys missing, JSON parse errors
- **Root Cause:** Manual edits replace object keys instead of merging
- **Alternative:** Use JSON validator after edit, copy from working branch
- **Prevention:** `python3 -m json.tool file.json > /dev/null` validates
- **Related Pitfalls:** PIT-003 (JSON corruption)

### Anti-Pattern: Not Handling Perst Versioning
- **Symptom:** After update, clicking same entity shows no data (e.g., houses disappear)
- **Root Cause:** Perst creates new version with new OID, frontend uses stale OID
- **Alternative:** 
  1. Gray-out update button when no changes (prevents unnecessary versioning)
  2. Backend delta check skips storage when no changes
  3. Frontend navigates to new OID after successful update if OID changed
- **Prevention:** Use hasChanges state to disable button, handle OID changes in update response
- **Related Pitfalls:** PIT-004 (OID shift after update)

## Svelte 5 Routing Patterns

### Dynamic Detail Routes
```bash
# Folder structure
/routes/owners/+page.svelte      # list
/routes/owners/[id]/+page.svelte  # detail

# Navigation in list page
import { goto } from '$app/navigation';

function openDetail(item: Item) {
    goto('/owners/' + item.id);  // URL: /owners/123
}

# Reading ID in detail page
import { page } from '$app/stores';
let id = $derived(parseInt($page.params.id));
```

### Why [id] Notation
- URL reflects state → bookmarkable, sharable
- Browser back/forward works correctly  
- Debugging easier (visible in URL bar)

## Common Patterns Quick Reference

| Pattern | When to Use | Example |
|---------|-------------|---------|
| Manager | Storage access | `PerstUserManager.getByUsername()` |
| OO Navigation | Related objects | `cleaner.getSchedules()` |
| Session Auth | Authorization | `servlet.getUserData("perstUser")` |
| TransactionContainer | Batch operations | `tc.addInsert(obj); store(tc)` |
| Svelte 5 goto() | Navigation | `goto('/route/' + id)` |

## Automated Checks

### JSON Validation (Required after manual edits)
```bash
# Validate any JSON file before commit
python3 -m json.tool src/main/frontend-svelte/src/lib/i18n/messages/en.json > /dev/null && echo "Valid" || echo "Invalid"

# Or use node
node -e "JSON.parse(require('fs').readFileSync('file.json'))" && echo "Valid"
```

### Frontend (SvelteKit)
```bash
# Type check - catches import errors, type mismatches
cd src/main/frontend-svelte
npm run check

# Build - full compilation
npm run build
```

### Why These Matter
- **Import errors** - `session` vs `session.svelte` - check catches this
- **Type mismatches** - Svelte 5 runes ($state, $derived) need proper typing
- **Missing translations** - can add custom lint rule

### Before Every Commit (Required)
```bash
cd src/main/frontend-svelte
npm run check
# If clean:
npm run build
# Then commit
```

### Backend
```bash
# Java/Groovy - build handles compilation
./bld build
```

## Protocol Adherence Checklist

- [ ] Run `npm run check` before frontend commits
- [ ] Run `./bld build` before backend commits  
- [ ] Test changes in browser after build
- [ ] Update .memory/ with any new findings