# Perst Integration Refactor Plan

## Current Problem
The current implementation modifies KISS core files:
- `Connection.java` - Added Perst enum value
- `MainServlet.java` - Added Perst detection and null connection handling  
- `ProcessServlet.java` - Added Perst-specific connection/transaction handling

This makes it difficult to accept future KISS framework updates.

## Proposed Solution (Per Creator's Suggestion)
1. **Don't tell KISS you're using a database** - Don't configure any database in application.ini
2. **Use MainServlet.putEnvironment/getEnvironment** - Store Perst Storage handle there
3. **Keep all Perst code in precompiled** - No core modifications needed

---

## Implementation Status

### Phase 1: Revert Core File Changes (Restore Original KISS) - ✅ COMPLETED

| File | Status |
|------|--------|
| `src/main/core/org/kissweb/database/Connection.java` | ✅ Reverted - removed Perst enum, null handling |
| `src/main/core/org/kissweb/restServer/MainServlet.java` | ✅ Reverted - removed Perst detection, wrapping |
| `src/main/core/org/kissweb/restServer/ProcessServlet.java` | ✅ Reverted - removed Perst connection handling |
| `src/main/backend/application.ini` | ✅ Updated - no database configured |

### Phase 2: Create PerstStorageManager - ✅ COMPLETED

**Created:** `src/main/precompiled/oodb/PerstStorageManager.java`

Key features:
- Uses `MainServlet.putEnvironment("perstStorage", storage)` to store Storage
- Uses `MainServlet.getEnvironment("perstStorage")` to retrieve Storage
- Provides `initialize()`, `getStorage()`, `getRoot()`, transaction methods
- No KISS core modifications needed

### Phase 3: Refactor PerstContext - ✅ COMPLETED

**Modified:** `src/main/precompiled/oodb/PerstContext.java`

Changes:
- Now delegates to PerstStorageManager for all storage operations
- Uses MainServlet environment for Storage access
- Simplified implementation

### Phase 4: Update Application Code - ✅ COMPLETED

| File | Status |
|------|--------|
| `KissInit.groovy` | ✅ Updated - uses PerstStorageManager.initialize() |
| `PerstHelper.java` | ✅ Already delegates to PerstContext (which now uses PerstStorageManager) |

### Phase 5: Delete Unnecessary Files - ✅ COMPLETED

Deleted:
- `src/main/precompiled/oodb/PerstKissConnection.java`
- `src/main/core/oodb/PerstKissConnection.java`
- `src/main/precompiled/oodb/PerstConnection.java`

---

## Files Modified/Created

| Action | Files |
|--------|-------|
| Reverted | Connection.java, MainServlet.java, ProcessServlet.java |
| Modified | application.ini, KissInit.groovy, PerstContext.java |
| Created | PerstStorageManager.java |
| Deleted | PerstKissConnection.java, PerstConnection.java |

---

## New Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      MainServlet                            │
│  (no changes - uses putEnvironment/getEnvironment)         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  PerstStorageManager                        │
│  - initialize() → puts Storage in MainServlet environment   │
│  - getStorage() → retrieves from environment               │
│  - Transaction methods (begin/commit/rollback)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      PerstContext                           │
│  - Delegates to PerstStorageManager                        │
│  - Provides convenient CRUD API                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      PerstHelper                            │
│  - Delegates to PerstContext                               │
│  - Used by domain managers                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Benefits Achieved

1. ✅ **No core KISS modifications** - Easy to update KISS framework
2. ✅ **Cleaner architecture** - Perst is fully independent
3. ✅ **Uses built-in KISS features** - putEnvironment/getEnvironment
4. ✅ **Simpler code** - No null Connection handling, no Perst type checks

---

## Testing Checklist

- [ ] Server starts without errors
- [ ] Login with admin/admin works
- [ ] Admin user persists after restart
- [ ] No SQL connections used
- [ ] Services work via Perst

---

*Plan created: 2026-03-15*
*Branch: proposed*
*Status: Implementation complete - testing in progress*
