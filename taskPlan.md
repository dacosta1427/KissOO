# KissOO — Task Plan: Critical Issues

> **Verdict:** `intermediateVerdict.md`
> **Last Updated:** 2026-03-21

---

## Priority 0 — Immediate (Production Risk)

### 0.1 Extract Source Code for Compiled-Only Classes

**Issue:** 9 classes exist only as compiled `.class` files — no `.java` source in the repository. The entire `mycompany/database/` package is missing source.

**Goal:** Recover source code so all application logic can be maintained, debugged, and extended.

**Approach:** Use `javap -p -c` to extract method signatures, then recreate `.java` source files matching the compiled API. Compare against backups in `Perst_backup/` where available.

**Files to recover:**

| Class | Difficulty | Notes |
|-------|-----------|-------|
| `mycompany.database.BaseManager` | High | Abstract generic class with 15+ methods |
| `mycompany.database.ActorManager` | Medium | Extends BaseManager<Actor> |
| `mycompany.database.PerstUserManager` | Medium | Auth + CRUD, 17 methods |
| `mycompany.database.PhoneManager` | Low | 8 methods, simple CRUD |
| `mycompany.database.BenchmarkDataManager` | Medium | 12 methods with aggregation |
| `mycompany.database.PerstHelper` | High | 25+ methods, Perst facade |
| `mycompany.database.PerstConnection` | Low | Wrapper extending Connection |
| `mycompany.domain.CDatabaseRoot` | Medium | Root with FieldIndex fields |
| `oodb.PerstContext` | Medium | Has backup at `Perst_backup/` |

**Deliverable:** `.java` source files in correct package directories:
- `src/main/precompiled/mycompany/database/` (7 files)
- `src/main/precompiled/mycompany/domain/CDatabaseRoot.java`
- `src/main/precompiled/oodb/PerstContext.java`

---

### 0.2 Fix Non-Thread-Safe HashMap Indexes

**Issue:** `Actor.java` and `PerstUser.java` use plain `HashMap` for static in-memory indexes, causing data corruption under concurrent load.

**Files to modify:**
- `src/main/precompiled/mycompany/domain/Actor.java` (lines 43-44)
- `src/main/precompiled/mycompany/domain/PerstUser.java` (lines 59-61)

**Change all static HashMap to ConcurrentHashMap:**
```java
// BEFORE
private static Map<Integer, Actor> userIdIndex = new HashMap<>();
private static Map<String, Actor> uuidIndex = new HashMap<>();

// AFTER
private static Map<Integer, Actor> userIdIndex = new ConcurrentHashMap<>();
private static Map<String, Actor> uuidIndex = new ConcurrentHashMap<>();
```

**Also update:**
- `index()` method calls — `ConcurrentHashMap.put()` instead of `HashMap.put()`
- `removeFromIndex()` method calls — `ConcurrentHashMap.remove()` instead of `HashMap.remove()`
- `getFromIndex()` method calls — `ConcurrentHashMap.get()` instead of `HashMap.get()`

---

### 0.3 Sync PerstStorageManager Source with Compiled Class

**Issue:** Source file `src/main/precompiled/oodb/PerstStorageManager.java` is outdated — missing 12+ methods present in the compiled class.

**Methods to add to source:**
- `getStorage()` — return shared Storage instance
- `getRoot()` — return CDatabaseRoot
- `isAvailable()` — check if Perst is enabled
- `isCDatabaseEnabled()` — check if CDatabase mode is on
- `beginTransaction()` — dual-mode (CDatabase or Storage thread transaction)
- `commitTransaction()` / `rollbackTransaction()`
- `save(Object)` / `saveInTransaction(Object)` — dual-mode insert
- `delete(Object)` / `deleteInTransaction(Object)` — dual-mode delete
- `getAll(Class)` — iterate records via CDatabase.getRecords()
- `cleanOldDatabaseFiles()` / `deleteDirectory()` — migration utilities

**Approach:** Use `javap -p -s -c` on the compiled class to extract method signatures and bytecode patterns, then write matching source code.

---

## Priority 1 — High (Production Quality)

### 1.1 Make Transaction Wrapping Consistent

**Issue:** Inconsistent transaction usage across managers. Some operations wrap in begin/commit/rollback, others don't.

**Goal:** All write operations (create, update, delete) in all managers should use explicit transaction boundaries.

**Files to modify:**
- `mycompany.database.ActorManager` (after source recovery) — add transactions to `create()`, `update()`, `delete()`
- `mycompany.database.PerstUserManager` (after source recovery) — fix `update()` to use transaction
- `PerstHelper.storeNewObject()` / `storeModifiedObject()` / `removeObject()` — all need transaction wrapping

**Pattern to enforce:**
```java
public static boolean update(Actor actor) {
    if (!isAvailable()) return false;
    beginTransaction();
    try {
        getStorage().getRoot().getActorIndex().put(actor.getName(), actor);
        commitTransaction();
        return true;
    } catch (Exception e) {
        rollbackTransaction();
        throw e;
    }
}
```

**Decision needed:** Should transactions be at the Manager level (per-operation) or at the Service level (per-HTTP-request)? Current pattern is mixed. Recommend: Manager level with service-level fallback.

---

### 1.2 Fix Svelte Frontend Build Issues

**Issue:** Svelte frontend cannot build or run due to missing config files and mixed Svelte 4/5 syntax.

**Step 1: Create missing config files**

`src/main/frontend-svelte/svelte.config.js`:
```javascript
import adapter from '@sveltejs/adapter-auto';

export default {
    kit: {
        adapter: adapter()
    }
};
```

`src/main/frontend-svelte/vite.config.ts`:
```typescript
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
    plugins: [sveltekit()]
});
```

`src/main/frontend-svelte/postcss.config.js`:
```javascript
export default {
    plugins: {
        tailwindcss: {},
        autoprefixer: {}
    }
};
```

`src/main/frontend-svelte/tailwind.config.js`:
```javascript
export default {
    content: ['./src/**/*.{html,js,svelte,ts}'],
    theme: { extend: {} },
    plugins: []
};
```

**Step 2: Standardize on Svelte 5 syntax**

Migrate all `.svelte` files:
- `on:click` → `onclick=`
- `let` reactive variables → `$state()` rune
- `onMount` → `$effect()` rune or `onMount()` from `$svelte`
- `new App({ target })` → `mount(App, { target })` from `svelte`
- Slots → `{@render children()}`
- `$:` reactive statements → `$derived()` or `$effect()`

**Step 3: Fix entry point conflict**

Decide: use `app.html` (SvelteKit SSR) or `index.html` (Vite SPA). Recommend `app.html` for SvelteKit consistency.

**Step 4: Fix Server.ts session error handling**

Implement actual auth redirect instead of throwing in `handleSessionError`.

**Step 5: Integrate with build system**

Add `frontend-svelte` build step to `Tasks.java` and/or `bld` script.

---

### 1.3 Add Optimistic Locking

**Issue:** No conflict detection — concurrent edits result in silent data loss (last writer wins).

**Goal:** Detect concurrent modifications and throw conflict exception.

**Approach:** Use `CVersion.id` (version counter) that increments on each update. Before committing, check that the version hasn't changed since read.

```java
// In PerstUserManager.update()
public static boolean update(PerstUser user) {
    int expectedVersion = user.getId();  // Or use a separate version field
    beginTransaction();
    try {
        // Re-fetch and check version
        PerstUser existing = getByOid(user.getOid());
        if (existing.getId() != expectedVersion) {
            rollbackTransaction();
            throw new ConflictException("Concurrent modification detected");
        }
        commitTransaction();
        return true;
    } catch (Exception e) {
        rollbackTransaction();
        throw e;
    }
}
```

**Note:** `CVersion` already has `transId` (transaction ID) and `id` (version ID). The application layer should use these to detect conflicts.

---

## Priority 2 — Medium (Production Hardening)

### 2.1 Disable `perst.file.noflush` or Document Risk

**Issue:** `perst.file.noflush = true` causes data loss on crash.

**Options:**

**Option A — Disable noflush:**
```java
// Remove this line from PerstStorageManager.java
// storage.setProperty("perst.file.noflush", Boolean.TRUE);
```

**Option B — Document the risk** in `docs/PerstIntegration.md` and `PERST_USAGE.md` with a warning.

**Option C — Make configurable:**
Add `PerstNoflush = true/false` in `application.ini`:
```ini
PerstNoflush = false
```

Recommend **Option C** — configurable with default `false` (safe).

---

### 2.2 Schedule Lucene Index Optimization

**Issue:** Lucene segment explosion causes performance degradation over time. `optimizeFullTextIndex()` only called in tests.

**Goal:** Schedule periodic optimization via Cron.

**Approach:** Create a `CronTasks/OptimizeLucene.groovy`:
```groovy
import oodb.PerstStorageManager;

class OptimizeLucene {
    static void run() {
        def db = PerstStorageManager.getDatabase();
        if (db != null) {
            db.optimizeFullTextIndex();
        }
    }
}

OptimizeLucene.run();
```

Add to `CronTasks/crontab`:
```
# Optimize Lucene full-text index daily at 3 AM
0 3 * * * OptimizeLucene
```

---

### 2.3 Synchronize `beginTransaction()`

**Issue:** `PerstStorageManager.beginTransaction()` is not synchronized, unlike `initialize()` and `close()`. Race condition between multiple threads calling it simultaneously.

**Fix:** Add `synchronized` keyword:
```java
public static synchronized void beginTransaction() {
    // ...
}
```

---

### 2.4 Tighten CORS Configuration

**Issue:** `web.xml` allows `*` origins — any website can call the API.

**Fix:** Add environment-based CORS configuration in `application.ini`:
```ini
# Comma-separated list of allowed origins, or * for all
CorsAllowedOrigins = https://yourdomain.com,https://app.yourdomain.com
```

Modify `MainServlet` or `web.xml` to read this and set allowed origins dynamically.

---

## Priority 3 — Low (Nice to Have)

### 3.1 Make Sessions Persistent

**Issue:** All sessions are in-memory. Server restart logs out all users.

**Goal:** Persist sessions to Perst or at least make them recoverable.

**Approach:** Store `UserData` sessions in Perst via CDatabase. On startup, restore active sessions.

**Note:** This is a significant change — requires careful consideration of session expiry and cleanup.

---

### 3.2 Add Connection/Transaction Leak Detection

**Issue:** If a service throws before calling `commitTransaction()` or `rollbackTransaction()`, the thread transaction is never ended.

**Fix:** Use `try-finally` in service layer, or add a `ThreadLocal<Boolean>` flag that's checked in a servlet filter to detect unclosed transactions.

---

### 3.3 Add Perst Health Check Endpoint

**Goal:** Expose a REST endpoint that checks Perst health (storage open, CDatabase initialized, Lucene index readable, etc.).

**Example:** `GET /rest?_class=System&_method=healthCheck`

---

## Task Summary Table

| ID | Task | Priority | Effort | Files |
|----|------|----------|--------|-------|
| 0.1 | Extract Manager source code | P0 | Medium | 9 new .java files |
| 0.2 | Fix HashMap → ConcurrentHashMap | P0 | Low | 2 files |
| 0.3 | Sync PerstStorageManager source | P0 | Medium | 1 file |
| 1.1 | Consistent transaction wrapping | P1 | Medium | 4 files |
| 1.2 | Fix Svelte frontend | P1 | Medium | 5 files + build |
| 1.3 | Add optimistic locking | P1 | High | 4 files |
| 2.1 | Make noflush configurable | P2 | Low | 2 files |
| 2.2 | Schedule Lucene optimization | P2 | Low | 2 files |
| 2.3 | Synchronize beginTransaction() | P2 | Low | 1 file |
| 2.4 | Tighten CORS | P2 | Low | 2 files |
| 3.1 | Persistent sessions | P3 | High | 3 files |
| 3.2 | Transaction leak detection | P3 | Medium | 2 files |
| 3.3 | Perst health check endpoint | P3 | Low | 1 file |

---

## Suggested Execution Order

```
Phase 1 (Critical): Safety & Correctness
├── 0.2 Fix HashMap → ConcurrentHashMap       (1-2 hours, immediate safety)
├── 0.3 Sync PerstStorageManager source       (half day)
└── 0.1 Extract Manager source code           (1-2 days)

Phase 2 (High): Production Quality
├── 1.1 Consistent transaction wrapping         (half day, after 0.1)
├── 2.3 Synchronize beginTransaction()         (10 minutes, after 0.3)
├── 2.1 Make noflush configurable             (1 hour)
└── 1.2 Fix Svelte frontend                  (1-2 days)

Phase 3 (Medium): Polish
├── 1.3 Add optimistic locking                (1 day)
├── 2.2 Schedule Lucene optimization          (1 hour)
├── 2.4 Tighten CORS                          (1 hour)
└── 3.3 Perst health check                   (1 hour)

Phase 4 (Future)
├── 3.1 Persistent sessions                   (1-2 days)
└── 3.2 Transaction leak detection           (half day)
```
