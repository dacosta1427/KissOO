# KissOO Project — Intermediate Verdict

> **Status:** Active Development | **Branch:** `feature/cdatabase-lucene-fix`
> **Date:** 2026-03-21

---

## Executive Summary

KissOO is a well-architected fork of the KISS Web Framework with Perst OODBMS integration, a "Manager at the Gate" security pattern, and a modern Svelte 5 frontend in early development. The project has strong fundamentals but contains **critical issues that threaten production viability**, primarily around missing source code and thread safety.

---

## 1. Project Overview

| Aspect | Details |
|--------|---------|
| **Name** | KissOO (fork of KISS Web Application Framework) |
| **Upstream** | github.com/blakemcbride/Kiss |
| **Language** | Java 16+ (source), Groovy 4, ABCL Lisp |
| **Framework** | KISS Web Application Framework (custom JSON-RPC servlet engine) |
| **Server** | Apache Tomcat 11.0.12 (embedded, bundled) |
| **Build** | Custom `bld` bash script + Tasks.java; Maven/Ant/Gradle for IDE |
| **OODB** | Perst DCG 4.0.1 (CDatabase + Lucene 9.11.0) |
| **SQL DB** | PostgreSQL, SQLite, MySQL, MS SQL, Oracle (via C3P0 pool) |
| **Frontend (legacy)** | Vanilla JS + jQuery + custom Web Components (~10 screens, production-ready) |
| **Frontend (new)** | SvelteKit 2 / Svelte 5 / TypeScript / TailwindCSS (~15% complete) |
| **Testing** | JUnit 5 + Mockito |
| **Auth** | UUID session tokens, Argon2id password hashing |
| **License** | MIT |

---

## 2. Architecture

```
HTTP POST /rest
       │
   CorsFilter
       │
   MainServlet (async)
       │
   QueueManager (30-thread pool)
       │
   ProcessServlet (per-request)
       │
   Service Dispatch (by _class/_method)
       │
   ┌────┴────┐
   │         │
GroovyService  JavaService  LispService
   │         │         │
   └────┬────┘
        │
  Manager Layer (Manager at the Gate)
  BaseManager → ActorManager, PerstUserManager, PhoneManager
        │
        │
  ┌─────┴─────┐
  │           │
PerstStorageManager   PerstHelper   PerstContext
  │           │           │
  └─────┬─────┘
        │
   CDatabase (versioning, @Indexable, @FullTextSearchable)
        │
   Storage (Perst OODBMS)
        │
   Lucene (full-text index)
```

---

## 3. Strengths

### 3.1 Clean Core Architecture
- Single entry point (`POST /rest`) with convention-over-configuration dispatch
- Hot-reloading for Groovy/Lisp services — no restart needed during development
- Multi-language backend — Java, Groovy, and ABCL Lisp as first-class service languages
- Thread-per-request with async servlet support for high concurrency
- Microservice-style class caching with TTL eviction (600s inactivity, 60s check interval)

### 3.2 Strong Security Model
- "Manager at the Gate" pattern — every DB operation goes through a Manager with permission checks
- Type-safe `EndpointMethod` + `Agreement` authorization (grants by class/method)
- `Actor` entity with UUID, name, type, and active status
- `Group` entity for group-based permissions
- Argon2id password hashing via Password4j (configurable memory=15360, iterations=2)
- UUID session tokens with auto-purge (inactive after 120s re-validation, purged after 900s)
- `RequireAuthentication` flag for Perst-only mode

### 3.3 Perst Integration — Well Designed
- CDatabase versioning via `CVersion` base class
- `@Indexable` annotation for indexed fields
- `@FullTextSearchable` annotation for Lucene full-text search
- Clean separation from KISS core — no modifications to framework files
- `CDatabaseRoot` maintains FieldIndex collections for all entity types
- Configuration-driven via `application.ini`

### 3.4 Backend Protocol — Frontend Agnostic
- Both frontends use identical REST protocol (`Server.call(cls, meth, json)`)
- `ActorService.java` and `Users.groovy` provide REST endpoints
- Frontends are fully interchangeable from the backend's perspective
- CORS configured for development (localhost:8000, localhost:63342)

### 3.5 Legacy Frontend — Production Ready
- 15 custom Web Components (TextInput, DropDown, CheckBox, Popup, etc.)
- ~10 application screens (Login, Users, CRUD, Framework, SQL Access, etc.)
- AG-Grid and CKEditor bundled
- Mobile-responsive pages in `mobile/` directory
- Fully integrated into the `bld` build system

---

## 4. Weaknesses

### 4.1 CRITICAL — Missing Source Code for Manager Layer

**9 classes exist only as compiled `.class` files with NO `.java` source in the repository:**

| Class | Package | Risk |
|-------|---------|------|
| `BaseManager<T>` | `mycompany.database` | Cannot modify permission checks or CRUD logic |
| `ActorManager` | `mycompany.database` | Cannot change Actor CRUD behavior |
| `PerstUserManager` | `mycompany.database` | Cannot modify authentication or user management |
| `PhoneManager` | `mycompany.database` | Cannot modify phone entity CRUD |
| `BenchmarkDataManager` | `mycompany.database` | Cannot modify benchmark operations |
| `PerstHelper` | `mycompany.database` | Cannot modify Perst facade operations |
| `CDatabaseRoot` | `mycompany.domain` | Cannot modify Perst root object with FieldIndexes |
| `PerstContext` | `oodb` | Cannot modify Perst context (only backup exists) |
| `PerstConnection` | `mycompany.database` | Cannot modify DB connection (only backup exists) |

The `mycompany/database/` package has **zero source files**. This is the entire business logic layer.

**Impact:** You cannot maintain, debug, understand, or extend your own application code. Any bug fix or feature addition requires reverse-engineering from bytecode.

### 4.2 CRITICAL — Non-Thread-Safe Static Indexes

`Actor.java:43-44` and `PerstUser.java:59-61`:

```java
// Actor.java — NOT THREAD SAFE
private static Map<Integer, Actor> userIdIndex = new HashMap<>();
private static Map<String, Actor> uuidIndex = new HashMap<>();

// PerstUser.java — NOT THREAD SAFE
private static Map<Integer, PerstUser> idIndex = new HashMap<>();
private static Map<String, PerstUser> usernameIndex = new HashMap<>();
```

Tomcat runs multiple request threads simultaneously. Concurrent `HashMap` access causes:
- `ConcurrentModificationException`
- Infinite loops in JDK 8 `HashMap.get()` (bucket rehash attack via hash collision)
- Data corruption and lost entries

**Impact:** Data corruption under concurrent load. The server becomes unreliable with multiple simultaneous users.

### 4.3 CRITICAL — Inconsistent Transaction Wrapping

| Manager | `create()` | `update()` | `delete()` |
|---------|-----------|-----------|-----------|
| `PerstUserManager` | ✅ Explicit transaction | ❌ No transaction | ✅ Explicit transaction |
| `PhoneManager` | ✅ Explicit transaction | ✅ Explicit transaction | ✅ Explicit transaction |
| `ActorManager` | ❌ No transaction | ❌ No transaction | ❌ No transaction |
| `BenchmarkDataManager` | ✅ Explicit transaction | N/A | N/A |

```java
// ActorManager.create() — NO transaction wrapping
public static Actor create(Object... args) {
    // ...
    PerstStorageManager.save(actor);  // Direct save, no begin/commit
    return actor;
}
```

```java
// PerstUserManager.create() — CORRECT pattern
public static PerstUser create(Object... args) {
    // ...
    PerstStorageManager.beginTransaction();
    try {
        PerstStorageManager.saveInTransaction(perstUser);
        PerstStorageManager.commitTransaction();
    } catch (Exception e) {
        PerstStorageManager.rollbackTransaction();
        throw e;
    }
}
```

**Impact:** Partial writes, data corruption, inconsistent state under concurrent access.

### 4.4 CRITICAL — Source vs Compiled Mismatch (PerstStorageManager)

The source file at `src/main/precompiled/oodb/PerstStorageManager.java` is **severely outdated** compared to the compiled class:

| Method | In Source? | In Compiled? |
|--------|-----------|-------------|
| `getStorage()` | ❌ | ✅ |
| `getRoot()` | ❌ | ✅ |
| `isAvailable()` | ❌ | ✅ |
| `isCDatabaseEnabled()` | ❌ | ✅ |
| `beginTransaction()` | ❌ | ✅ |
| `commitTransaction()` | ❌ | ✅ |
| `rollbackTransaction()` | ❌ | ✅ |
| `save(Object)` | ❌ | ✅ |
| `saveInTransaction(Object)` | ❌ | ✅ |
| `delete(Object)` | ❌ | ✅ |
| `deleteInTransaction(Object)` | ❌ | ✅ |
| `getAll(Class)` | ❌ | ✅ |
| `cleanOldDatabaseFiles()` | ❌ | ✅ |

**Impact:** Developers reading the source will have a completely wrong understanding of the API. Any changes to the source file will be overwritten by the compiled class at runtime.

### 4.5 HIGH — `perst.file.noflush = true` in Production

```java
// PerstStorageManager.java:63
storage.setProperty("perst.file.noflush", Boolean.TRUE);
```

This disables synchronous disk writes for performance. On JVM crash or power failure, all un-flushed data is lost.

### 4.6 HIGH — No Optimistic Locking or Conflict Detection

The system uses "last writer wins" with no conflict detection. Two users can:
1. User A reads Actor X (version 1)
2. User B reads Actor X (version 1)
3. User A modifies and saves (version becomes 2)
4. User B modifies and saves (silently overwrites A's changes)

CDatabase's `CVersion` provides version history tracking, but the application layer does NOT check for concurrent modifications.

### 4.7 HIGH — Svelte Frontend Cannot Build

The `src/main/frontend-svelte/` project is incomplete:

| Missing Item | Severity |
|-------------|----------|
| `svelte.config.js` | CRITICAL — SvelteKit won't work |
| `vite.config.ts` | CRITICAL — Vite won't work |
| `postcss.config.js` | HIGH — Tailwind CSS won't work |
| `tailwind.config.js` | HIGH — Tailwind CSS won't work |
| Login screen | MEDIUM |
| Framework shell/sidebar | MEDIUM |
| Component library port | MEDIUM |
| Auth/stores | MEDIUM |
| Svelte 5 syntax consistency | HIGH — Mixed Svelte 4/5 syntax |

**Additionally:**
- `App.svelte` and `users/+page.svelte` use Svelte 4 syntax (`on:click`, `onMount`, `let`)
- `+layout.svelte` uses Svelte 5 syntax (`$props()`, `{@render}`)
- `app.ts` uses Svelte 4 instantiation (`new App({ target })`) instead of Svelte 5's `mount()`
- Entry point confusion: both `index.html` and `app.html` exist with conflicting content
- **No build integration** with the main `bld` script
- `Server.ts` has `handleSessionError` that just throws — no auth redirect

### 4.8 MEDIUM — No Lucene Optimization Scheduling

`optimizeFullTextIndex()` is only called in `CDatabaseVersioningTest.java`. Not scheduled in production. Lucene segment explosion will cause performance degradation over time.

### 4.9 MEDIUM — CORS Wide Open

```xml
<!-- web.xml -->
<param-value>*</param-value>  <!-- Allows any origin -->
```

Fine for development, dangerous for production.

### 4.10 MEDIUM — `PerstStorageManager.beginTransaction()` Not Synchronized

Unlike `initialize()` and `close()`, the `beginTransaction()` method is NOT synchronized. Two threads could call it simultaneously, causing race conditions in transaction state management.

---

## 5. Frontend Comparison

| Aspect | Legacy Kiss | Svelte 5 |
|--------|------------|----------|
| **Location** | `src/main/frontend/` | `src/main/frontend-svelte/` |
| **Status** | Production-ready | Early prototype (~15%) |
| **Language** | Vanilla JS | TypeScript + Svelte 5 |
| **Build** | None (raw files) | Vite + SvelteKit |
| **CSS** | Custom + normalize.css | Tailwind CSS |
| **Components** | 15 custom Web Components | 1 (App.svelte) |
| **Screens** | ~10 complete | 2 partial |
| **Routing** | Custom SPA | SvelteKit |
| **AG-Grid** | Bundled (old) | npm v33 |
| **Auth** | Full login flow | Stub only |
| **Build integration** | ✅ Full | ❌ None |

---

## 6. Perst/CDatabase Multi-User Assessment

### What Works
- Perst Storage is thread-safe internally
- CDatabase transactions provide isolation
- FieldIndex collections (`CDatabaseRoot`) are managed by Perst
- UserCache uses `Hashtable` (inherently synchronized)

### What Doesn't Work
- Static `HashMap` indexes in domain classes (data corruption)
- Actor's `userIdIndex` and `uuidIndex` accessed without synchronization
- PerstUser's `idIndex`, `usernameIndex`, `tokenIndex` same issue
- No optimistic locking to detect concurrent modifications
- Transaction wrapping inconsistent across managers
- `beginTransaction()` not synchronized in PerstStorageManager
- Sessions lost on server restart (in-memory only)

### Perst-Specific Observations
- Uses `Storage.beginThreadTransaction(0)` (mode 0 = SHARED_TRANSACTION) when CDatabase is disabled
- Uses `CDatabase.beginTransaction()/commitTransaction()` when CDatabase is enabled
- Lucene index stored at `data/oodb.idx` (adjacent to database file)
- 4.0.1 adds History/Lex APIs (`flushHistory()`, `setHistoryBufferSize()`, etc.) not yet used

---

## 7. Recommended Priority

| Priority | Issue | Effort |
|----------|-------|--------|
| P0 | Extract source code for Manager classes | Medium |
| P0 | Fix thread-unsafe HashMap indexes | Low |
| P1 | Sync PerstStorageManager source with compiled class | Medium |
| P1 | Make transaction wrapping consistent | Medium |
| P1 | Fix Svelte frontend (config files + syntax) | Medium |
| P2 | Add optimistic locking | High |
| P2 | Disable `noflush` or document risk | Low |
| P2 | Schedule Lucene optimization | Low |
| P3 | Tighten CORS for production | Low |

---

## 8. Files Reference

### Key Source Files
- `src/main/precompiled/oodb/PerstStorageManager.java` — outdated source
- `src/main/precompiled/oodb/PerstConfig.java` — config reader
- `src/main/precompiled/mycompany/domain/Actor.java` — non-thread-safe indexes
- `src/main/precompiled/mycompany/domain/PerstUser.java` — non-thread-safe indexes
- `src/main/backend/services/ActorService.java` — REST endpoints
- `src/main/backend/Login.groovy` — authentication
- `src/main/backend/application.ini` — main config

### Missing Source (Compiled Only)
- `mycompany.database.BaseManager` — abstract base manager
- `mycompany.database.ActorManager` — Actor CRUD
- `mycompany.database.PerstUserManager` — user CRUD + auth
- `mycompany.database.PhoneManager` — phone CRUD
- `mycompany.database.BenchmarkDataManager` — benchmark ops
- `mycompany.database.PerstHelper` — Perst facade
- `mycompany.database.PerstConnection` — DB connection
- `mycompany.domain.CDatabaseRoot` — root object with FieldIndexes
- `oodb.PerstContext` — Perst context

### Backups
- `Perst_backup/PerstContext.java`
- `Perst_backup/PerstConnection.java`

### Documentation
- `docs/PerstIntegration.md`
- `docs/PerstCDatabaseFindings.md`
- `docs/PerstDeveloperGuide.md`
- `workMEMORY.md`
- `perstLuceneIssue.md`
- `plan_svelte5.md`

---

*Generated: 2026-03-21*
