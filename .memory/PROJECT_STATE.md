# Project State

## Package Structure

```
src/main/
├── precompiled/
│   ├── mycompany/domain/     # Domain entities (Actor, PerstUser, House, Booking, etc.)
│   ├── mycompany/database/  # Manager classes (PerstUserManager, ActorManager, etc.)
│   └── oodb/                 # Perst configuration (PerstConfig, PerstStorageManager)
├── backend/
│   └── services/             # REST services (Groovy/Java)
└── frontend-svelte/
    └── src/
        ├── lib/
        │   ├── api/         # API modules (Auth.ts, Cleaning.ts, etc.)
        │   ├── state/       # Svelte stores (session.svelte.ts)
        │   └── services/    # Server.ts
        └── routes/          # SvelteKit routes
```

## Key Responsibilities

| Class/Module | Responsibility | Collaborators |
|--------------|---------------|---------------|
| `PerstStorageManager` | Singleton storage access, transactions | Managers, Services |
| `PerstUserManager` | User CRUD, authentication | Login service |
| `ActorManager` | Actor CRUD, authorization | Services |
| `Session Store` | Reactive session state (Svelte 5) | Server.ts, Components |
| `Server.ts` | Backend communication, error handling | API modules |

## Technical Debt

| Item | Severity | Planned Fix | Iteration |
|------|----------|-------------|-----------|
| <item> | HIGH/MED/LOW | <fix> | <N> |

## Architecture Decisions

| ID | Decision | Rationale | Date |
|----|----------|-----------|------|
| OO-001 | Pure OO navigation | No SQL patterns, objects reference directly | 2026-04 |
| OO-002 | Session-based auth | Frontend sends only UUID | 2026-04 |

## Dependencies

```
KissOO
├── Perst 5.1.0 (CDatabase)
│   ├── CVersion (versioning)
│   ├── @Indexable (B-tree index)
│   └── @FullTextSearchable (Lucene)
├── Svelte 5 (runes)
└── Tailwind CSS
```

## API Boundaries

### Public API (Frontend → Backend)
```typescript
// Only _uuid required
Server.call(className, methodName, payload)
```

### Backend Response Format
```typescript
{ _Success: boolean, data?: any, _ErrorMessage?: string, _ErrorCode?: number }
```

## Cross-References

- Related Pitfalls: See PITFALL_REGISTRY.md
- Related Patterns: See BEST_PRACTICES.md