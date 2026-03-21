# KissOO — Task Plan

> **Last Updated:** 2026-03-21

---

## ✅ COMPLETED

- [x] **0.1** Extract Manager source code
- [x] **0.2** Fix HashMap → ConcurrentHashMap (Thread-safe static indexes)
- [x] **0.3** Sync PerstStorageManager source with compiled class
- [x] **1.1** Consistent transaction wrapping (via TransactionContainer migration)
- [x] **1.3** Add optimistic locking (UDBM handles via TransactionContainer)
- [x] **2.3** Synchronize beginTransaction() (UDBM handles thread isolation via ThreadLocal)

---

## NEXT: Priority 2 — Production Hardening

### 2.1 Make noflush configurable

**Issue:** `perst.file.noflush = true` hardcoded — data loss risk on crash.

**Goal:** Add `PerstNoflush = true/false` in application.ini with safe default.

- [ ] **PerstConfig.java** — Add `perstNoflush` field (default `false`)
- [ ] **PerstConfig.java** — Add `isPerstNoflush()` getter
- [ ] **PerstStorageManager.java** — Read config, conditionally set property
- [ ] **application.ini** — Add `PerstNoflush = false`

**Effort:** Low (1-2 hours)

---

### 2.2 Lucene Index Optimization Scheduling

**Issue:** `optimizeFullTextIndex()` never called in production — Lucene segment explosion over time.

**Goal:** CDatabase internally schedules optimization based on config.

- [ ] **PerstConfig.java** — Add `perstOptimizeInterval` field (seconds, default 86400 = 24h)
- [ ] **PerstConfig.java** — Add `getPerstOptimizeInterval()` getter
- [ ] **PerstStorageManager.java** — Start `ScheduledExecutorService` on initialize
- [ ] **PerstStorageManager.java** — Call `cdb.optimizeFullTextIndex()` on schedule
- [ ] **PerstStorageManager.java** — Shutdown executor on `close()`
- [ ] **application.ini** — Add `PerstOptimizeInterval = 86400`

**Effort:** Medium (half day)

**Note for Perst agent:** Add internal timer to CDatabase/UnifiedDBManager. Suggestion:
```java
private ScheduledExecutorService scheduler;

public void setOptimizeInterval(int seconds) {
    if (scheduler != null) {
        scheduler.shutdown();
    }
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
        try {
            optimizeFullTextIndex();
        } catch (Exception e) {
            System.err.println("[CDatabase] Optimization failed: " + e.getMessage());
        }
    }, seconds, seconds, TimeUnit.SECONDS);
}
```

---

## FUTURE WORK (Backend)

### 3.1 Perst Health Check Endpoint

**Goal:** Expose REST endpoint for Perst health status.

- [ ] Create health check in PerstStorageManager or PerstDBHealth class
- [ ] Expose via REST: `GET /rest?_class=System&_method=healthCheck`

**Effort:** Low

---

## FUTURE WORK (Frontend — After Backend Complete)

### 1.2 Fix Svelte Frontend Build Issues

**Issue:** Missing config files, mixed Svelte 4/5 syntax.

- [ ] **src/main/frontend-svelte/svelte.config.js** — Create with adapter-auto
- [ ] **src/main/frontend-svelte/vite.config.ts** — Create with SvelteKit plugin
- [ ] **src/main/frontend-svelte/postcss.config.js** — Create with Tailwind/autoprefixer
- [ ] **src/main/frontend-svelte/tailwind.config.js** — Create with content paths
- [ ] Migrate `.svelte` files from Svelte 4 to 5 syntax (`$state`, `$effect`, `$derived`)
- [ ] Fix entry point (app.html vs index.html)
- [ ] Fix Server.ts session error handling
- [ ] Integrate with build system (Tasks.java / bld)

**Effort:** Medium-High (1-2 days)

---

### 2.4 Tighten CORS Configuration

**Issue:** `cors.allowed.origins = *` in web.xml — security risk in production.

- [ ] Update web.xml / web-secure.xml with specific origins per environment

**Effort:** Low (user will tackle later)

---

## LOWER PRIORITY (Nice to Have)

### 3.2 Make Sessions Persistent

- [ ] Store UserData sessions in Perst via CDatabase
- [ ] Restore active sessions on startup

**Effort:** High (1-2 days)

---

### 3.3 Transaction Leak Detection

- [ ] Add `ThreadLocal<Boolean>` flag for unclosed transactions
- [ ] Servlet filter to detect and warn about leaks

**Effort:** Medium

---

## Task Summary

| ID | Task | Status | Effort |
|----|------|--------|--------|
| 0.1 | Extract Manager source | ✅ Done | Medium |
| 0.2 | HashMap → ConcurrentHashMap | ✅ Done | Low |
| 0.3 | Sync PerstStorageManager | ✅ Done | Medium |
| 1.1 | Transaction wrapping | ✅ Done | Medium |
| 1.3 | Optimistic locking | ✅ Done | — |
| 2.3 | Sync beginTransaction() | ✅ Done | — |
| **2.1** | **Make noflush configurable** | **NEXT** | **Low** |
| **2.2** | **Lucene optimization** | **NEXT** | **Medium** |
| 3.1 | Health check endpoint | Future | Low |
| 1.2 | Fix Svelte frontend | Future | High |
| 2.4 | Tighten CORS | Future | Low |
| 3.2 | Persistent sessions | Future | High |
| 3.3 | Transaction leak detection | Future | Medium |

---

## Current Execution Order

```
1. 2.1 Make noflush configurable    (Low effort, quick win)
2. 2.2 Lucene optimization         (Medium effort, production safety)
3. 3.1 Health check endpoint       (Low effort, visibility)
4. 1.2 Fix Svelte frontend         (High effort, after backend complete)
5. 2.4 Tighten CORS                (Low effort, security)
6. 3.2 Persistent sessions         (High effort, future)
7. 3.3 Transaction leak detection  (Medium effort, future)
```
