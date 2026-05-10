## Summary

Read and analyzed all key project documentation for KissOO:

### Primary Files Read
1. **AGENTS.md** (549 lines) - Project development notes, build requirements, OO navigation rules, Perst integration, debugging protocols
2. **AP.md** (211 lines) - Agent protocol, entry point, memory system, critical rules, git workflow
3. **.memory/EXPERIENCE_LOG.md** - Current state, iteration history (last: Iteration 15 - 2026-04-26)
4. **.memory/PROJECT_DESCRIPTION_KissOO.md** - Project overview, core features, architecture highlights
5. **.memory/PITFALL_REGISTRY.md** (132 lines) - Known pitfalls (PIT-001 through PIT-004) with root causes and resolutions
6. **.memory/BEST_PRACTICES.md** (157 lines) - Design patterns, heuristics, project-specific rules, anti-patterns
7. **.memory/PROJECT_STATE.md** - Package structure, responsibilities, technical debt, architecture decisions
8. **.currentPLAN/FEAT_svelte5-dynamic-routes-navbar.md** (627 lines) - Active plan for Svelte 5 dynamic routes and navbar fixes

### Critical Points

**Architecture & Paradigm:**
- Pure OO navigation with Perst OODBMS (no SQL-style ID filtering)
- Manager at the Gate pattern - all data access through Manager classes
- Session-based auth - frontend sends only _uuid, backend derives context from PerstUser stored in session
- PerstUser.username globally unique across ALL entity types (Owner, Cleaner, Admin)
- Object deletion via `tc.addDelete(obj)` pattern - use inherited `isDeleted()` method
- getXXXOid() for OID references, NEVER getXXXId()

**Database:**
- Perst DB stored at /home/dacosta/kissoo-data/oodb (external to WAR)
- Use `select()` NOT `getRecords()` for retrieval (getRecords uses Lucene for text search)
- Use `createContainer()` which returns sync container (async linQueue never initialized)
- 64-bit OID support in ooGTxQ, but StorageImpl.reserveIds() truncates indexSize to int at 7 locations - MUST FIX before 2.4B insertion

**Current Active Work:**
- FEAT_svelte5-dynamic-routes-navbar (HIGH priority)
- Missing Svelte 5 dynamic routes for houses, bookings, cleaners, schedules
- Navbar uses <a href> causing page reloads (should use goto())
- DEBUG span visible in production navbar
- PIT-001: Missing [id] folder notation for entity detail views

**Known Pitfalls (Active):**
- PIT-001: Missing Svelte 5 dynamic route segments (5 occurrences)
- PIT-003: JSON translation file corruption (RESOLVED)
- PIT-002: Svelte 5 goto() issues (RESOLVED)
- PIT-004: OID shift after Perst versioning (RESOLVED)

**Best Practices / Rules:**
- OO-001: Use getXxxOid() not getXxxId()
- OO-002: Add collections to domain for bidirectional navigation
- OO-003: Use select() not getRecords()
- OO-004: Handle OID changes after versioning
- SV5-001: Use [id] folder notation for detail routes
- SV5-002: Use goto() from $app/navigation
- JSON-001: Validate JSON after manual editing
- UI-001: Gray-out update buttons when no changes

**Build Requirements:**
- JARs: abcl.jar (Lisp), jakarta.mail, angus-activation, jakarta.activation-api, ooGTxQ-1.0.0.jar, lombok.jar
- Java 21+, Maven, Linux/Windows
- Lombok @Getter/@Setter for domain classes
- Business methods written manually

**Frontend:**
- Svelte 5 ONLY (no Svelte 4)
- Runes: $state, $derived, $props
- NO Svelte 4 syntax (export let, $:, createEventDispatcher, on:event)
- Must run `npm run check` before commits

### References Found
- AGENTS.md → Project conventions, architecture, debugging protocols  
- AP.md → Active protocols, entry point, memory system
- .memory/EXPERIENCE_LOG.md → Current iteration state, change history
- .memory/PITFALL_REGISTRY.md → Known failures with 5 Whys analysis and remedies
- .memory/BEST_PRACTICES.md → Reusable patterns, anti-patterns, validation rules
- .memory/PROJECT_STATE.md → Architecture, package structure, API boundaries
- .currentPLAN/FEAT_svelte5-dynamic-routes-navbar.md → Active HIGH priority task

### Conflicts Detected
None - all documentation is consistent with project memory.

### Next Actions
1. Execute FEAT_svelte5-dynamic-routes-navbar plan (create dynamic routes for houses, bookings, cleaners, schedules)
2. Fix navbar: remove DEBUG span, replace <a href> with goto() buttons
3. Verify PerstStorageManager indexSize truncation issue and implement fix for 2.4B insertion support
4. Continue following AP.md protocol: read KISSOO_DEVELOPMENT_PROTOCOL.md, check .currentPLAN/, update .memory/ after changes