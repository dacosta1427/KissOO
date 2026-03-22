# Plan: Option A — PerstStorageManager Facade with TransactionContainer

## Core Principle

**TransactionContainer is THE entry point for ALL storage operations.**

Every insert/update/delete — even single objects — goes through explicit TC:

```java
TransactionContainer tc = PerstStorageManager.createContainer();
tc.addInsert(actor);
boolean success = PerstStorageManager.store(tc);
```

**No convenience shortcuts.** Clarity over brevity.

---

## Phase 1: Fix PerstStorageManager Facade

### 1.1 Remove broken methods
- [ ] Delete `getDatabase()` — returns null
- [ ] Delete `insert()`, `update()`, `delete()` — force explicit TC

### 1.2 Add missing transaction control methods
- [ ] `beginTransaction()`
- [ ] `commitTransaction()` throws Exception
- [ ] `rollbackTransaction()`
- [ ] `isInTransaction()`

### 1.3 Add missing retrieval methods
- [ ] `getByUuid(Class<T>, String uuid)`
- [ ] `searchFullText(String query)`
- [ ] `setHistoryBufferSize(int)`
- [ ] `setHistoryFlushInterval(int)`

### 1.4 Add documentation
- [ ] Class-level javadoc explaining TC pattern

---

## Phase 2: Update ActorManager — Explicit TC

### 2.1 `create()` — explicit TC
### 2.2 `update()` — explicit TC
### 2.3 `delete()` — explicit TC
### 2.4 Static finders → facade
- `getByUuid()` → `PerstStorageManager.find(Actor.class, "uuid", uuid)`
- `getByName()` → `PerstStorageManager.find(Actor.class, "name", name)`
- `getByUserId()` → `PerstStorageManager.find(Actor.class, "userId", id)`
- `getAll()` → `PerstStorageManager.getAll(Actor.class)`

---

## Phase 3: Update PerstUserManager — Explicit TC

### 3.1 `create()` — explicit TC
### 3.2 `update()` — explicit TC
### 3.3 `delete()` — explicit TC
### 3.4 Auth operations — all with TC
- `authenticate()`, `changePassword()`, `resetPassword()`, `activate()`, `deactivate()`

### 3.5 Static finders → facade
- `getByKey()` → facade calls
- `getAll()` → `PerstStorageManager.getAll(PerstUser.class)`
- `getActiveUsers()` → filter from getAll()

---

## Phase 4: Rewrite PerstHelper with TC

### 4.1 Actor operations
- [ ] `storeActor(Actor)` — TC
- [ ] `updateActor(Actor)` — TC
- [ ] `removeActor(Actor)` — TC

### 4.2 User operations
- [ ] `storeUser(PerstUser)` — TC
- [ ] `updateUser(PerstUser)` — TC
- [ ] `removeUser(PerstUser)` — TC

### 4.3 Agreement operations
- [ ] `storeAgreement(Agreement)` — TC
- [ ] `updateAgreement(Agreement)` — TC

### 4.4 Group operations
- [ ] `storeGroup(Group)` — TC
- [ ] `updateGroup(Group)` — TC

### 4.5 Generic operations
- [ ] `storeNewObject(Object)` — TC
- [ ] `storeModifiedObject(Object)` — TC
- [ ] `removeObject(Object)` — TC

### 4.6 Remove dead methods
- [ ] `getContext()` — remove or stub
- [ ] `getVersionHistory()` — remove
- [ ] `getCurrentVersion()` — remove
- [ ] `retrieveObject()` — remove
- [ ] `retrieveAllObjects()` — rewrite using facade

---

## Phase 5: Clean Domain Classes

### 5.1 Actor.java — REMOVE
- [ ] `index()` method
- [ ] `removeIndex()` method
- [ ] static `userIdIndex`
- [ ] static `uuidIndex`
- [ ] static `findByUuid()`
- [ ] static `findByName()`
- [ ] static `findByUserId()`
- [ ] static `findAll()`
- [ ] `getAll()`

### 5.2 PerstUser.java — REMOVE
- [ ] `index()` method
- [ ] `removeIndex()` method
- [ ] static maps
- [ ] static `get()`
- [ ] static `getByUsername()`
- [ ] static `getAll()`
- [ ] static `getActiveUsers()`
- [ ] `getAll()`

### 5.3 Agreement.java — CLEAN (already good)
### 5.4 Group.java — CLEAN (already good)
### 5.5 Phone.java — CLEAN (already good)
### 5.6 BenchmarkData.java — CLEAN (already good)

---

## Phase 6: Update Callers of Static Finders

### 6.1 Find all direct domain class calls
```bash
grep -rn "Actor\." --include="*.java" --include="*.groovy"
grep -rn "PerstUser\." --include="*.java" --include="*.groovy"
```

### 6.2 Update each caller to use Manager
- [ ] `ActorService.java`
- [ ] `Login.groovy`
- [ ] `PerstInit.java`
- [ ] Others from grep results

---

## Phase 7: PerstContext Cleanup

### 7.1 Check usage
```bash
grep -rn "PerstContext" --include="*.java" --include="*.groovy"
```

### 7.2 Decision
- If unused → remove
- If used → check if still needed

---

## Files Summary

| File | Phase | Action |
|------|-------|--------|
| `PerstStorageManager.java` | 1 | Rewrite facade |
| `ActorManager.java` | 2 | All CRUD → explicit TC |
| `PerstUserManager.java` | 3 | All CRUD + auth → explicit TC |
| `PerstHelper.java` | 4 | Rewrite with TC, remove dead code |
| `Actor.java` | 5 | Remove index/removeIndex/static maps/finders |
| `PerstUser.java` | 5 | Same cleanup |
| `ActorService.java` | 6 | Update finder calls |
| `Login.groovy` | 6 | Update finder calls |
| `PerstInit.java` | 6 | Update calls |
| Other callers | 6 | From grep results |
| `PerstContext.java` | 7 | Check usage, likely remove |

---

## Execution Order

1. **Phase 1** — PerstStorageManager (foundation)
2. **Phase 2** — ActorManager
3. **Phase 3** — PerstUserManager
4. **Phase 4** — PerstHelper
5. **Phase 5** — Actor, PerstUser cleanup
6. **Phase 6** — Update callers
7. **Phase 7** — PerstContext cleanup

---

## Decisions Made

1. **PerstHelper** — Keep and rewrite with TC
2. **Other domain classes** — Only Actor and PerstUser need cleanup (Agreement, Group, Phone, BenchmarkData already clean)
3. **Convenience methods** — Dropped. Force explicit TC everywhere.
