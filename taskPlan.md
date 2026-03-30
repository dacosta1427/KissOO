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

- [x] **PerstConfig.java** — Add `perstNoflush` field (default `false`)
- [x] **PerstConfig.java** — Add `isPerstNoflush()` getter
- [x] **PerstStorageManager.java** — Read config, conditionally set property
- [x] **application.ini** — Add `PerstNoflush = false`

**Effort:** Low (1-2 hours) — **COMPLETED**

---

### 2.2 Lucene Index Optimization Scheduling

**Issue:** `optimizeFullTextIndex()` never called in production — Lucene segment explosion over time.

**Goal:** CDatabase internally schedules optimization based on config.

- [x] **PerstConfig.java** — Add `perstOptimizeInterval` field (seconds, default 86400 = 24h)
- [x] **PerstConfig.java** — Add `getPerstOptimizeInterval()` getter
- [x] **PerstStorageManager.java** — Start `ScheduledExecutorService` on initialize
- [x] **PerstStorageManager.java** — Call `flushHistoryBuffer()` on schedule
- [x] **PerstStorageManager.java** — Shutdown executor on `close()`
- [x] **application.ini** — Add `PerstOptimizeInterval = 86400`

**Effort:** Medium (half day) — **COMPLETED**

---

## ✅ 3.1 Perst Health Check Endpoint — COMPLETE

**Added REST endpoints for Perst health and stats:**

- `PerstStorageManager.healthCheck()` — Basic status (initialized, available, inTransaction, etc.)
- `PerstStorageManager.getStats()` — Memory stats (usedMemory, maxMemory, usagePercent, etc.)
- `PerstService.java` — REST service exposing both endpoints

**Usage:**
```
GET /rest?service=PerstService&method=healthCheck
GET /rest?service=PerstService&method=getStats
```

**Effort:** Low — **COMPLETED**

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

**Status:** No longer applicable - using CDatabase directly (Perst 5.1.0)

**Effort:** N/A - handled by Perst

---

## Task Summary

| ID | Task | Status | Effort |
|----|------|--------|--------|
| 0.1 | Extract Manager source | ✅ Done | Medium |
| 0.2 | HashMap → ConcurrentHashMap | ✅ Done | Low |
| 0.3 | Sync PerstStorageManager | ✅ Done | Medium |
| 1.1 | Transaction wrapping | ✅ Done | Medium |
| 1.3 | Optimistic locking | ✅ Done | — |
| 2.1 | Make noflush configurable | ✅ Done | Low |
| 2.2 | Lucene optimization | ✅ Done | Medium |
| 2.3 | Sync beginTransaction() | ✅ Done | — |
| 3.1 | Health check endpoint | ✅ Done | Low |
| 1.2 | Fix Svelte frontend | Future | High |
| 2.4 | Tighten CORS | Future | Low |
| 3.2 | Persistent sessions | Future | High |
| 3.3 | Transaction leak detection | Future (Perst agent) | Medium |

---

## Current Execution Order

```
1. 2.1 Make noflush configurable    ✅ Done
2. 2.2 Lucene optimization         ✅ Done
3. 3.1 Health check endpoint      ✅ Done
4. 1.2 Fix Svelte frontend        (Future - after backend complete)
5. 2.4 Tighten CORS               (Future - user will tackle)
6. 3.2 Persistent sessions         (Future)
7. 3.3 Transaction leak detection  (Future)
```
1. 2.1 Make noflush configurable    (Low effort, quick win)
2. 2.2 Lucene optimization         (Medium effort, production safety)
3. 3.1 Health check endpoint       (Low effort, visibility)
4. 1.2 Fix Svelte frontend         (High effort, after backend complete)
5. 2.4 Tighten CORS                (Low effort, security)
6. 3.2 Persistent sessions         (High effort, future)
7. 3.3 Transaction leak detection  (Medium effort, future)
```
