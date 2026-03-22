# Svelte 5 Simplified Architecture Plan

## Implementation Instructions

> **IMPORTANT**: After each successfully completed task/todo item in this plan:
> 1. Update this document to mark the task as completed (change `[ ]` to `[x]`)
> 2. Commit the changes to git with a meaningful commit message
> 3. Update the "Last Updated" date at the bottom of this document

---

## Architecture Overview

```
Components ($state, $derived) 
    ↓ function call
API Modules (Auth.ts, Users.ts) - plain TypeScript
    ↓ Server.call()
Kiss Backend
```

## File Structure

```
src/lib/
├── api/
│   ├── Auth.ts              # NEW: login(), logout(), signup()
│   └── Users.ts             # NEW: getUsers(), addUser(), deleteUser()
├── state/
│   └── session.ts           # NEW: Configurable $state session
└── services/
    └── Server.ts            # KEEP: Existing

DELETE: lib/remote/, lib/server/, lib/state/session.svelte.ts
```

## Tasks

### Phase 1: Create Core API Modules

- [ ] **Task 1.1**: Create `lib/api/Auth.ts` - Simple functions using Server.call()
- [ ] **Task 1.2**: Create `lib/api/Users.ts` - CRUD functions using Server.call()
- [ ] **Task 1.3**: Create `lib/state/session.ts` - Configurable $state session

### Phase 2: Update Pages to Use Svelte 5 Runes

- [ ] **Task 2.1**: Update `routes/login/+page.svelte` - Use $state, $derived, Auth.login()
- [ ] **Task 2.2**: Update `routes/signup/+page.svelte` - Use $state, Auth.signup()
- [ ] **Task 2.3**: Update `routes/users/+page.svelte` - Use $state, Users module
- [ ] **Task 2.4**: Update `routes/+page.svelte` - Use $state, Auth.logout()
- [ ] **Task 2.5**: Update `routes/+layout.svelte` - Add session.restore()

### Phase 3: Cleanup Old Files

- [ ] **Task 3.1**: Delete `lib/remote/auth.remote.ts`
- [ ] **Task 3.2**: Delete `lib/remote/users.remote.ts`
- [ ] **Task 3.3**: Delete `lib/server/kiss-bridge.ts`
- [ ] **Task 3.4**: Delete `lib/state/session.svelte.ts`
- [ ] **Task 3.5**: Update `svelte.config.js` - Remove $remote alias

### Phase 4: Testing & Verification

- [ ] **Task 4.1**: Run `npm install` and `npm run build`
- [ ] **Task 4.2**: Test login with backend (admin/admin)
- [ ] **Task 4.3**: Commit changes

---

## Key Code Patterns

### Auth.ts
```typescript
import { Server } from '$lib/services/Server';
import { session } from '$lib/state/session';

export async function login(username: string, password: string) {
  const res = await Server.call('', 'Login', { 
    username: username.toLowerCase(), 
    password 
  });
  if (res._Success && res.uuid) {
    session.setUUID(res.uuid);
  }
  return res;
}

export async function logout() {
  try { await Server.call('', 'Logout', {}); } catch (e) {}
  session.clear();
}

export async function signup(username: string, password: string) {
  const res = await Server.call('services.Users', 'addRecord', {
    userName: username.toLowerCase(),
    userPassword: password,
    userActive: 'Y'
  });
  if (res._Success || res.success) {
    return login(username, password);
  }
  return res;
}
```

### session.ts
```typescript
let uuid = $state('');
let persistToStorage = true;

export const session = {
  get uuid() { return uuid; },
  get isAuthenticated() { return uuid.length > 0; },
  
  setUUID(newUuid: string, persist?: boolean) {
    uuid = newUuid;
    if (persist ?? persistToStorage) {
      localStorage.setItem('kiss_uuid', newUuid);
    }
  },
  
  clear() {
    uuid = '';
    localStorage.removeItem('kiss_uuid');
  },
  
  restore() {
    const saved = localStorage.getItem('kiss_uuid');
    if (saved) uuid = saved;
  },
  
  setPersistence(enabled: boolean) {
    persistToStorage = enabled;
  }
};
```

### Component with Runes
```svelte
<script lang="ts">
  import { login } from '$lib/api/Auth';
  
  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  let isValid = $derived(username.length > 0 && password.length > 0);

  async function handleLogin() {
    loading = true;
    error = '';
    const res = await login(username, password);
    loading = false;
    if (res._Success) {
      goto('/');
    } else {
      error = res._ErrorMessage || 'Login failed';
    }
  }
</script>

<form on:submit|preventDefault={handleLogin}>
  <input bind:value={username} />
  <input bind:value={password} />
  <button type="submit" disabled={!isValid || loading}>
    {loading ? 'Logging in...' : 'Login'}
  </button>
</form>
```

---

*Last Updated: 2026-03-22*
*Plan Version: 1.0*
*Target Svelte Version: 5.54.1*
