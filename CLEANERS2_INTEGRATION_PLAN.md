# Cleaners2 Integration Plan

**Date:** 2026-03-23  
**Author:** opencode  
**Purpose:** Plan for integrating KissOO master frontend-svelte with cleaners branch to create a solid foundation (KissOO master) and a domain-specific extension (cleaners2 branch).

## ⚠️ CRITICAL REQUIREMENTS ⚠️
1. **SVELTE 5 ONLY** - No Svelte 4 patterns allowed. Use Svelte 5 runes ($state, $derived, $effect, $props).
2. **PROTOCOL MUST BE HONORED** - After each completed, tested, and committed task:
   - Read the 2 guides (KissOO-Guide.md and sv5guide.md)
   - Read this plan document
   - Update this plan with progress
   - Commit and push changes
   This prevents regression and maintains context.

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Phase 0: Design Decisions](#phase-0-design-decisions)
4. [Phase 1: KissOO Master Foundation](#phase-1-kissoo-master-foundation)
5. [Phase 2: Manager Pattern Decision](#phase-2-manager-pattern-decision)
6. [Phase 3: Cleaners2 Branch Creation](#phase-3-cleaners2-branch-creation)
7. [Phase 4: Implementation Roadmap](#phase-4-implementation-roadmap)
8. [Technical Specifications](#technical-specifications)
9. [Risks & Mitigations](#risks--mitigations)
10. [Success Criteria](#success-criteria)
11. [Next Steps](#next-steps)

---

## Executive Summary

Create a **master2** branch (copy of current master) and a **cleaners2** branch built on master2, combining the best of both codebases:

### Goals
1. **KissOO Master2 Branch** = Copy of current master, then enrich & improve
   - **Start**: Direct copy of current master (preserves original)
   - **Then enhance**: Add session management, reusable components, etc.
   - **Do not modify original master branch**

2. **Cleaners2 Branch** = Cleaning scheduler application built ON improved master2
   - All cleaning scheduler functionality
   - Domain-specific components and logic
   - Shares same improved foundation as master2

### Key Principles
- **Minimal layers and wrappers** - Use static methods where practical
- **Svelte 5 only** - No Svelte 4 patterns
- **"Underwater login"** - Store encrypted credentials for silent re-authentication
- **Backend-driven session timeout** - Frontend sends UUID with every request, backend validates

---

## 📊 Progress Tracker

| Phase | Task | Status | Commit | Notes |
|-------|------|--------|--------|-------|
| **Phase 1: KissOO Master2** | | | | |
| 1.0 | Create master2 branch | ✅ Completed | - | Already created as copy of master |
| 1.1 | Enhanced Session Management | ✅ Completed | c2e1f836 | Encrypted credentials, silent re-auth |
| 1.2 | Component Integration | ✅ Completed | bd27ef69 | Form.svelte, validation.ts, NotificationToast.svelte integrated |
| 1.3 | State Management Pattern | ✅ Completed | bd27ef69 | Notification store using cleaners reactive pattern |
| 1.4 | API Layer Standardization | ✅ Completed | bd27ef69 | No changes needed (already standard) |
| **Phase 2: Manager Pattern** | | | | |
| 2.0 | Design Decision | ✅ Completed | - | Keep PerstStorageManager (minimal layers) |
| **Phase 3: Cleaners2 Branch** | | | | |
| 3.1 | Create Branch | ✅ Completed | b9f81cf3 | Created cleaners2 branch from master2 |
| 3.2 | Copy Cleaning Scheduler | ✅ Completed | b9f81cf3 | Copied pages and components from cleaners branch |
| 3.3 | API Layer Integration | ✅ Completed | d3f3a3f1 | Frontend API adapter (kiss-client.js) uses Server.call() |
| 3.4 | Domain Logic Integration | ✅ Completed | d3f3a3f1 + 4a60c57e | Backend service created, Schedule conflict fixed, build passes |
| **Phase 4: Implementation** | | | | |
| 4.0 | Week 1: Master2 Foundation | 🔄 In Progress | - | Days 1-7 (UI Refinements) |  
| 4.0.1 | Users Page Refinement | ✅ Completed | f21e388b | Form.svelte integration, toast notifications |  
| 4.0.2 | CRUD Page Refinement | ✅ Completed | 135854c6 | Form.svelte integration, toast notifications |  
| 4.0.3 | Cleaning Scheduler Alignment | ✅ Completed | f897fe65 | Frontend-backend field mapping, address field added |  
| 4.0.4 | Runtime Testing | 🔄 In Progress | - | Testing with backend server (server started) |  
| 4.0.5 | Bug Fix: GlobalModal Binding | ✅ Completed | 151ad7cd | Fixed Svelte 5 binding error in modal component |  
| 4.0.6 | Backend Authentication Fix | ✅ Completed | 9f327899 | Fixed admin user emailVerified flag for login |  
| 4.0.7 | Frontend Auth UUID Fix | ✅ Completed | 4cf6a316 | Added Server.setUUID in Auth.ts, debug logging |
| 4.0.8 | Cleaners API Consolidation | ✅ Completed | 99efcc78 | Consolidated cleaning API in Cleaning.ts, fixed imports, added TypeScript types |
| 4.1 | Week 2: Cleaners2 Branch | 🔄 Pending | - | Days 1-7 |

**Legend**: ✅ Completed | 🔄 In Progress/Not Started | ❌ Blocked/Failed

---

## Current State Analysis

### KissOO Master Frontend-Svelte
**Strengths:**
- Complete session management with localStorage persistence
- TypeScript throughout
- Tailwind CSS styling
- Demo pages (users, CRUD, benchmarks, etc.)
- Proper Svelte 5 runes usage

**Weaknesses:**
- No session expiration handling
- Inline forms and validation (not reusable)
- Alert/confirm dialogs (poor UX)
- No notification system

### Cleaners Branch Frontend-Svelte
**Strengths:**
- Advanced Svelte 5 patterns ($state, $derived, $effect)
- Reusable Form.svelte component
- Centralized validation.ts
- NotificationToast.svelte for better UX
- Reactive store patterns

**Weaknesses:**
- Domain-specific (cleaning scheduler)
- Uses different API endpoint format
- No session persistence
- Less mature error handling

### Backend Session Management
- **Timeout**: 30 minutes (`userInactiveSeconds = 1800`)
- **Mechanism**: Server checks `lastAccess` on each request
- **Frontend sends**: UUID with every API call
- **Problem**: No client-side awareness of timeout

### Manager Patterns
| Approach | KissOO Master | Manager-at-the-Gate Branch |
|----------|---------------|----------------------------|
| **Entry Point** | `oodb.PerstStorageManager` | `PerstContext` → `PerstHelper` |
| **Manager Style** | Static methods | Static methods (via helper) |
| **Layers** | 2 (StorageManager → UnifiedDBManager) | 3 (PerstHelper → PerstContext → Storage) |
| **Permission** | Integrated in BaseManager | Not implemented |

---

## Phase 0: Design Decisions

### 1. Session Management Approach
**Decision**: Store encrypted credentials for "underwater login" (silent re-authentication).

**Rationale**:
- User wants minimal disruption when session expires
- Backend has 30-minute timeout
- Credentials stored encrypted in localStorage (client-side only)
- Only used when session expires (401 response)

**Implementation**:
- Use Web Crypto API for AES-GCM encryption
- Derive key from user password (not stored)
- Opt-in with "Remember me" checkbox
- Fallback to login page if decryption fails

### 2. Manager Pattern Approach
**Decision**: Keep current PerstStorageManager pattern (minimal layers).

**Rationale**:
- User wants "few layers and wrappers"
- Current pattern already works
- Static methods are acceptable (GetInstance() adds complexity)
- Single entry point (PerstStorageManager) already acts as "librarian"

**Implementation**:
- Keep `oodb.PerstStorageManager` as main entry point
- Add optional `PerstHelper` only for convenience methods
- No major refactoring needed

### 3. Component Integration Priority
**Decision**: Integrate Form.svelte, validation.ts, NotificationToast.svelte.

**Rationale**:
- Most advanced Svelte 5 components
- Solve real UX problems (forms, validation, notifications)
- Generic enough for any application
- Medium integration effort

**Not integrating now**:
- `stores.svelte.js` (different paradigm, high integration effort)
- `Table.svelte` (already have ag-grid for complex tables)
- `ScheduleBoard.svelte` (domain-specific)

### 4. Demo Pages Inclusion
**Decision**: Keep demo pages in KissOO master.

**Rationale**:
- Demonstrate framework capabilities
- Provide examples for developers
- Already implemented and working

---

## Phase 1: KissOO Master2 Foundation (Create enriched & improved master branch)

### Task 1.0: Create master2 Branch (Already Done)
**Goal**: Create a new branch for improvements without modifying original master.

**Status**: ✅ Already completed - master2 branch created as copy of master.

```bash
# Already executed:
git checkout -b master2
git push -u origin master2
```

**Important**: All improvements will be made on `master2` branch, not on `master`. This preserves the original master branch.

### Task 1.1: Enhanced Session Management ✅ COMPLETED
**Goal**: Add "underwater login" with encrypted credential storage.

**Status**: ✅ Implemented and tested on master2 branch (commit c2e1f836).

**Files modified**:
- `src/lib/state/session.svelte.ts` - Added encrypted credential storage using Web Crypto API (AES-GCM)
- `src/lib/services/Server.ts` - Added silent re-authentication on session expiration (401 response)
- `src/routes/login/+page.svelte` - Added "Remember me" checkbox, pre-fill username from stored credentials
- `src/lib/api/Auth.ts` - Added `session.clearCredentials()` on logout
- `src/lib/utils/Utils.ts` - Fixed TypeScript errors (unused parameter)

**Features implemented**:
- Encrypted credential storage with Web Crypto API (AES-GCM)
- Key derived from user password (not stored)
- "Remember me" checkbox for opt-in storage
- Silent re-authentication when session expires (401 response)
- Automatic retry of original API call with new session
- Fallback to login page if decryption or re-auth fails
- Credential clearing on logout

**Protocol followed**:
- ✅ Read KissOO-Guide.md and sv5guide.md after commit
- ✅ Updated plan with progress
- ✅ Committed and pushed changes

### Task 1.2: Component Integration from Cleaners
**Goal**: Add Form.svelte, validation.ts, NotificationToast.svelte.

**Files to create**:
- `src/lib/components/Form.svelte` (adapted from cleaners)
- `src/lib/utils/validation.ts` (copied from cleaners)
- `src/lib/components/NotificationToast.svelte` (adapted from cleaners)

**Files to modify**:
- `src/lib/utils/Utils.ts` - Integrate notification system
- `src/routes/users/+page.svelte` - Refactor to use Form component
- `src/routes/crud/+page.svelte` - Add toast notifications
- `src/routes/+layout.svelte` - Add NotificationToast component

**Adaptation needed**:
- Convert CSS to Tailwind classes
- Integrate with existing modal store
- Add TypeScript types
- Ensure Svelte 5 runes compatibility

### Task 1.3: State Management Pattern Adoption
**Goal**: Use cleaners' reactive object pattern for new state.

**Implementation**:
```javascript
// New notification store (following cleaners pattern)
export const notificationState = $state({ value: [] });
export const notificationActions = {
  success: (message) => { /* add notification */ },
  error: (message) => { /* add error notification */ },
  clear: () => { notificationState.value = []; }
};

// Keep session store as-is (encapsulated pattern)
```

### Task 1.4: API Layer Standardization
**Goal**: Ensure consistent API communication.

**Current state**: Server.ts already handles Kiss backend format.
**No changes needed** - keep existing pattern.

---

## Phase 2: Manager Pattern Decision

### Recommended Approach: Minimal Wrapper
**Keep** `oodb.PerstStorageManager` as the single entry point (librarian).

**Optional additions** (only if they reduce boilerplate):
```java
// In PerstStorageManager - add convenience methods only
public static Actor findActorByUuid(String uuid) {
  return find(Actor.class, "uuid", uuid);
}

public static Collection<Actor> getAllActiveActors() {
  return getAll(Actor.class).stream()
    .filter(Actor::isActive)
    .collect(Collectors.toList());
}
```

**No new layers** - work directly with PerstStorageManager.

### Integration with Frontend
**No frontend changes needed** - backend manager pattern is transparent to frontend.

---

## Phase 3: Cleaners2 Branch Creation

### Task 3.1: Create Branch
```bash
# Ensure we're on master2 branch with all Phase 1 enhancements
git checkout master2
git pull origin master2

# Create cleaners2 branch from master2
git checkout -b cleaners2

# Push to remote
git push -u origin cleaners2
```

### Task 3.2: Copy Cleaning Scheduler Code
**Copy from cleaners branch**:
- `src/routes/cleaners/` - Cleaner management pages
- `src/routes/bookings/` - Booking management pages  
- `src/routes/schedules/` - Schedule management pages
- `src/routes/houses/` - House management pages
- `src/lib/components/ScheduleBoard.svelte` - Domain-specific component

**Adapt to KissOO patterns**:
- Use KissOO's session management
- Use KissOO's Server.ts for API calls
- Use integrated Form/Validation components

### Task 3.3: API Layer Integration
**Cleaners uses**: `/api/kiss` endpoint with `{service, method, args}` format  
**KissOO uses**: `/rest` endpoint with `{...injson, _uuid, _method, _class}` format

**Solution**: Create adapter:
```typescript
// adapters/kiss-adapter.ts
export async function callCleaningService(method: string, args: any) {
  // Convert to KissOO format
  return Server.call('services.Cleaning', method, args);
}

// Or adapt each service call individually
export const cleaningApi = {
  getCleaners: () => Server.call('services.Cleaning', 'getCleaners', {}),
  createBooking: (data) => Server.call('services.Cleaning', 'createBooking', data)
};
```

### Task 3.4: Domain Logic Integration
**Keep cleaning scheduler business logic**:
- Cleaner scheduling algorithms
- Booking management
- House information tracking
- Schedule coordination

**Adapt to KissOO backend services**:
- Create `Cleaning.groovy` service in backend
- Implement required methods
- Connect to Perst database via PerstStorageManager

---

## Phase 4: Implementation Roadmap

### Week 1: KissOO Master2 Foundation
**Days 1-2**: Session enhancements
- Implement encrypted credential storage
- Add silent re-authentication
- Test session expiration handling

**Days 3-4**: Component integration
- Copy and adapt Form.svelte
- Copy validation.ts
- Copy and adapt NotificationToast.svelte
- Update Utils.ts with notification system

**Days 5-6**: Page updates
- Refactor users page to use Form component
- Add toast notifications to CRUD operations
- Update other pages as needed

**Day 7**: Testing and refinement
- Test all enhanced functionality
- Fix any integration issues
- Update documentation

### Week 2: Cleaners2 Branch
**Day 1**: Branch creation and setup
- Create cleaners2 branch
- Copy cleaning scheduler code
- Set up project structure

**Days 2-3**: API integration
- Create API adapter layer
- Connect to KissOO backend services
- Test API calls

**Days 4-5**: UI integration
- Integrate with KissOO session management
- Use Form/Validation components
- Add toast notifications

**Days 6-7**: Testing and polish
- End-to-end testing
- Domain-specific adjustments
- Performance optimization

---

## Technical Specifications

### 1. Encrypted Credential Storage
**Algorithm**: AES-GCM with Web Crypto API  
**Key derivation**: PBKDF2 with user password  
**Storage**: localStorage (client-side only)  
**Security**:
- Credentials never sent to server in plain text after initial login
- Key derived from password (not stored)
- Only encrypted blob stored in localStorage

**Implementation**:
```typescript
async function encryptCredentials(username: string, password: string): Promise<string> {
  const key = await deriveKey(password);
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const data = new TextEncoder().encode(JSON.stringify({ username, password }));
  const encrypted = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, data);
  return JSON.stringify({ iv: Array.from(iv), data: Array.from(new Uint8Array(encrypted)) });
}
```

### 2. Session Expiration Flow
```
1. User makes API call (Server.call)
2. Server returns 401 (_ErrorCode === 2)
3. Server.ts catches error
4. Check for stored credentials
5. Attempt silent re-auth (session.silentLogin)
6. If success: retry original call with new UUID
7. If failure: redirect to /login?expired=true
```

### 3. Component Integration Details

#### Form.svelte Adaptation
```svelte
<!-- Original from cleaners -->
<Form fields={[...]} data={formData} errors={formErrors} onSubmit={handleSubmit} />

<!-- Adapted for KissOO (with Tailwind) -->
<Form 
  fields={[
    { name: 'username', label: 'Username', type: 'text', required: true },
    { name: 'password', label: 'Password', type: 'password', required: true }
  ]}
  data={loginData}
  errors={loginErrors}
  onSubmit={handleLogin}
  class="tailwind-classes"
/>
```

#### Validation.ts Integration
```typescript
// Add to existing Utils.ts
import { validators } from './validation';

export const Utils = {
  // ... existing methods
  validate: {
    required: validators.required,
    email: validators.email,
    phone: validators.phone
  }
};
```

#### NotificationToast Integration
- Replace `alert()` calls with `notificationActions.error()`
- Replace `confirm()` with modal system (keep existing)
- Add toasts for success/warning/info messages

### 4. API Adapter Pattern
```typescript
// Cleaners2 API adapter
export const cleaningApi = {
  // Convert cleaners branch calls to KissOO format
  getCleaners: () => Server.call('services.Cleaning', 'getCleaners', {}),
  createBooking: (data: Partial<Booking>) => 
    Server.call('services.Cleaning', 'createBooking', data),
  // ... other methods
};
```

---

## Risks & Mitigations

### Risk 1: Security of Stored Credentials
**Impact**: High (credential theft)  
**Probability**: Low (with proper encryption)  
**Mitigation**:
- Use Web Crypto API (browser-native)
- Derive key from password (not stored)
- Add "Remember me" checkbox (opt-in only)
- Clear credentials on logout
- Timeout encrypted credentials (e.g., 7 days)

### Risk 2: Breaking Existing Functionality
**Impact**: Medium (user disruption)  
**Probability**: Medium (large changes)  
**Mitigation**:
- Feature flags for new components
- Gradual migration (one page at a time)
- Comprehensive testing
- Rollback plan

### Risk 3: Performance Impact
**Impact**: Low (user experience)  
**Probability**: Low  
**Mitigation**:
- Lazy load new components
- Cache decrypted credentials in memory
- Minimal re-renders with Svelte 5 runes
- Profile before/after

### Risk 4: Svelte 5 Compatibility
**Impact**: High (application breaks)  
**Probability**: Low (tested patterns)  
**Mitigation**:
- Use only Svelte 5 runes ($state, $derived, $effect, $props)
- Test with Svelte 5 compiler
- Avoid Svelte 4 patterns

---

## Success Criteria

### KissOO Master2 (Enriched & Improved Generic Skeleton)
- [ ] **Session Management**: Silent re-authentication with encrypted credentials
- [ ] **Components**: Form.svelte, validation.ts, NotificationToast.svelte integrated
- [ ] **Demo Pages**: All existing pages working with enhanced UX
- [ ] **Type Safety**: Full TypeScript coverage
- [ ] **Documentation**: Updated guides and examples

### Cleaners2 (Domain Extension)
- [ ] **Functionality**: Full cleaning scheduler (cleaners, bookings, schedules, houses)
- [ ] **Foundation**: Built on enhanced KissOO master2
- [ ] **Integration**: Uses KissOO session management and components
- [ ] **Domain Logic**: All cleaning scheduler business logic preserved
- [ ] **API**: Connected to KissOO backend services

### Code Quality
- [ ] **Svelte 5**: No Svelte 4 patterns
- [ ] **TypeScript**: Full type coverage
- [ ] **Styling**: Consistent Tailwind CSS
- [ ] **Error Handling**: Comprehensive error handling
- [ ] **Testing**: Unit and integration tests

---

## Next Steps

### Immediate Actions
1. **Phase 3 Started** - Branch created, cleaning scheduler code copied
2. **Complete API Integration** - Ensure kiss-client.js works with Server.call()
3. **Create Backend Service** - Implement Cleaning.groovy service in backend
4. **Test Integration** - Verify cleaning pages can communicate with backend

### Current Status
- ✅ **master2 branch** created and pushed
- ✅ **Phase 1 tasks** completed (session, components, state, API)
- ✅ **cleaners2 branch** created from master2
- ✅ **Cleaning scheduler code** copied (pages, components, API adapter)
- 🔄 **API integration** in progress (kiss-client.js adapted to Server.call)
- 🔄 **Backend service** not yet created (Cleaning.groovy needed)

### Bug Fixes and Investigation Results (2026-03-24)

**Issue 1: Cleaners dropdown not showing anything in schedules page**
- **Root Cause**: `scheduleFields` array in `schedules/+page.svelte` is a `const` array, not reactive. Mutating its properties doesn't trigger Svelte 5 reactivity.
- **Solution**: Convert `scheduleFields` to `$state` variable and ensure proper reactivity after data fetch.
- **Files modified**: `src/routes/schedules/+page.svelte`
- **Additional**: Added empty state warning when no cleaners found; added error display for API failures.

**Issue 2: Login failure for user `a@b.c:asd`**
- **Root Cause**: `PerstUser.canLogin()` requires `emailVerified == true`. New users created via signup have `emailVerified = false` by default.
- **Solution**: 
  1. Immediate: Manually set `emailVerified = true` for existing user.
  2. Long-term: Modify signup flow to auto-verify or add email verification workflow.
- **Files to investigate**: `PerstUser.java:141-143`, `KissInit.groovy` (default user creation), `Auth.ts` (signup)
- **Note**: Backend authentication returns generic error; frontend doesn't distinguish between wrong credentials and unverified email.

**Issue 3: Backend Cleaning.groovy API bugs**
- **Root Cause**: `Cleaning.groovy` used `optString` and `optBoolean` methods that don't exist in `org.kissweb.json.JSONObject`. Service calls failed with "No signature of method" errors.
- **Solution**: Changed all `optString(key, default)` to `getString(key, default)` and `optBoolean(key, default)` to `getBoolean(key, default)`.
- **Files modified**: `src/main/backend/services/Cleaning.groovy`
- **Result**: Added sample cleaners via API; dropdown now has data.

**Issue 4: User creation fails due to duplicate userId**
- **Root Cause**: `Users.groovy` called `PerstUserManager.create` with `userId = 0` for all new users, violating unique constraint on `userId` field.
- **Solution**: Generate unique `userId` by finding max existing userId + 1.
- **Files modified**: `src/main/backend/services/Users.groovy`
- **Note**: Also fixed response to include `_Success` field for proper error handling.

**Protocol Enhancement**: Added to AGENTS.md - when investigating UI bugs, first check Svelte 5 reactivity patterns; for auth issues, check `emailVerified` flag; for backend API errors, verify method signatures in JSON library; for data creation failures, check unique constraints on indexed fields.

### Questions for Clarification
1. **Encryption duration**: Should encrypted credentials expire (7 days, 30 days, never)?
2. **"Remember me" default**: Should it be checked by default?
3. **Component migration**: Should we migrate ALL existing forms to Form.svelte or only new ones?
4. **Backend changes**: Do we need to create `Cleaning.groovy` service or adapt existing services?
5. **Email verification**: Should new users be auto-verified or require email verification workflow?

### Long-term Considerations
1. **Manager pattern**: Evaluate if PerstHelper adds value over direct PerstStorageManager calls
2. **State management**: Consider adopting cleaners' reactive pattern for more stores
3. **Component library**: Build reusable component library for future projects
4. **Documentation**: Create comprehensive developer guide for both codebases

---

**Document Status**: Phase 4 In Progress  
**Last Updated**: 2026-03-23 (Fixed frontend Auth.ts to set Server.uuid after login)  
**Next Review**: After testing SV5 frontend login with debug logs