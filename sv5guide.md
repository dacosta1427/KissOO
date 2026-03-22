# Svelte 5 Guide - KissOO Implementation

## Table of Contents
1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Svelte 5 Runes](#svelte-5-runes)
4. [API Architecture](#api-architecture)
5. [Session Management](#session-management)
6. [Navigation](#navigation)
7. [Components](#components)
8. [Layout & Navbar](#layout--navbar)
9. [Forms & Validation](#forms--validation)
10. [Best Practices](#best-practices)

---

## Overview

KissOO uses **Svelte 5** with a clean, simple architecture that avoids experimental features. The frontend communicates with the Kiss Java/Groovy backend via `Server.call()` wrapped in TypeScript API modules.

### Key Principles
- **Simple API modules** - Plain TypeScript functions wrapping `Server.call()`
- **Svelte 5 Runes** - Reactive state management with `$state` and `$derived`
- **Configurable persistence** - Session can use memory or localStorage
- **Auth-aware UI** - Components react to authentication state

---

## Project Structure

```
src/main/frontend-svelte/src/
├── lib/
│   ├── api/
│   │   ├── Auth.ts         # Authentication (login, logout, signup)
│   │   └── Users.ts        # User management (CRUD operations)
│   ├── components/
│   │   └── Navbar.svelte   # Global navigation bar
│   ├── services/
│   │   └── Server.ts       # Backend communication
│   └── state/
│       └── session.svelte.ts  # Session state with runes
├── routes/
│   ├── +layout.svelte      # Root layout (includes Navbar)
│   ├── +page.svelte        # Home page
│   ├── login/+page.svelte  # Login page
│   ├── signup/+page.svelte # Signup page
│   └── users/+page.svelte  # User management page
└── app.css                 # Tailwind CSS imports
```

---

## Svelte 5 Runes

Runes are Svelte 5's new reactivity system. They replace stores and reactive declarations.

### $state

Creates reactive variables:

```svelte
<script lang="ts">
  let count = $state(0);
  let username = $state('');
  let loading = $state(false);
</script>

<button onclick={() => count++}>
  Count: {count}
</button>
```

### $derived

Creates computed values that automatically update:

```svelte
<script lang="ts">
  let password = $state('');
  let confirmPassword = $state('');
  
  // Automatically updates when password or confirmPassword changes
  let passwordsMatch = $derived(password === confirmPassword);
  
  let isValid = $derived(
    password.length >= 3 && 
    password === confirmPassword
  );
</script>

<button disabled={!isValid}>Submit</button>
```

### Real Examples from Codebase

**Login Page** (`src/routes/login/+page.svelte`):
```svelte
<script lang="ts">
  import { login, initBackend } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';

  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  
  // Form is valid only when both fields have values
  let isValid = $derived(username.length > 0 && password.length > 0);

  async function handleLogin() {
    if (!isValid) return;
    loading = true;
    error = '';
    
    const res = await login(username, password);
    if (res._Success) {
      goto(resolve('/'));
    } else {
      error = res._ErrorMessage || 'Login failed';
    }
    loading = false;
  }
</script>
```

**Users Page** (`src/routes/users/+page.svelte`):
```svelte
<script lang="ts">
  let users = $state<User[]>([]);
  let loading = $state(false);
  let newUserName = $state('');
  let newUserPassword = $state('');
  
  // Derived state for form validation
  let canAddUser = $derived(
    newUserName.length >= 3 && 
    newUserPassword.length >= 3
  );
</script>
```

---

## API Architecture

API modules are **plain TypeScript functions** that wrap `Server.call()`. They run on the client and communicate directly with the Kiss backend.

### Auth.ts

```typescript
// src/lib/api/Auth.ts
import { Server } from '$lib/services/Server';
import { session } from '$lib/state/session';

export async function login(username: string, password: string) {
  const res = await Server.call('', 'Login', {
    username: username.toLowerCase(),
    password: password
  });
  
  if (res._Success && res.uuid) {
    session.setUUID(res.uuid);
  }
  return res;
}

export async function logout() {
  try {
    await Server.call('', 'Logout', {});
  } catch (e) {}
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

export function isAuthenticated(): boolean {
  return session.isAuthenticated;
}
```

### Users.ts

```typescript
// src/lib/api/Users.ts
import { Server } from '$lib/services/Server';

export interface User {
  id: number;
  userName: string;
  userPassword: string;
  userActive: 'Y' | 'N';
}

export async function getUsers(): Promise<User[]> {
  const res = await Server.call('services.Users', 'getRecords', {});
  return res.rows || [];
}

export async function addUser(userName: string, userPassword: string) {
  const res = await Server.call('services.Users', 'addRecord', {
    userName: userName.toLowerCase(),
    userPassword,
    userActive: 'Y'
  });
  return {
    success: res._Success || res.success,
    error: res._ErrorMessage || res.error,
    id: res.id
  };
}

export async function deleteUser(id: number) {
  const res = await Server.call('services.Users', 'deleteRecord', { id });
  return {
    success: res._Success || res.success,
    error: res._ErrorMessage || res.error
  };
}
```

### Usage in Components

```svelte
<script lang="ts">
  import { login, isAuthenticated } from '$lib/api/Auth';
  import { getUsers, addUser } from '$lib/api/Users';
  
  // Check auth status
  if (isAuthenticated()) {
    // Load users
    const users = await getUsers();
  }
  
  // Login
  const res = await login(username, password);
</script>
```

---

## Session Management

Session state uses Svelte 5 runes for reactivity and supports configurable persistence.

### session.svelte.ts

```typescript
// src/lib/state/session.svelte.ts

// Reactive state with $state rune
let uuid = $state('');
let persistToStorage = true;
const STORAGE_KEY = 'kiss_session_uuid';

export const session = {
  // Get current UUID (reactive)
  get uuid(): string {
    return uuid;
  },
  
  // Check if authenticated (reactive)
  get isAuthenticated(): boolean {
    return uuid.length > 0;
  },
  
  // Set UUID (persists to localStorage by default)
  setUUID(newUuid: string, persist?: boolean): void {
    uuid = newUuid;
    if (persist ?? persistToStorage) {
      localStorage.setItem(STORAGE_KEY, newUuid);
    }
  },
  
  // Clear session
  clear(): void {
    uuid = '';
    localStorage.removeItem(STORAGE_KEY);
  },
  
  // Restore from localStorage (call in onMount)
  restore(): boolean {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved && saved.length > 0) {
      uuid = saved;
      return true;
    }
    return false;
  }
};
```

### Usage in Layout

```svelte
<!-- src/routes/+layout.svelte -->
<script lang="ts">
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session';
  import { initBackend } from '$lib/api/Auth';
  import Navbar from '$lib/components/Navbar.svelte';

  onMount(() => {
    initBackend();
    session.restore();  // Restore persisted session
  });
</script>

<Navbar />
<slot />
```

---

## Navigation

Use `resolve()` from `$app/paths` for proper base path handling.

```svelte
<script lang="ts">
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';
  
  // Navigate with base path support
  goto(resolve('/'));
  goto(resolve('/login'));
  goto(resolve('/users'));
</script>
```

### Why resolve()?

- Handles base path configuration
- Type-safe route resolution
- Official SvelteKit approach

---

## Components

### Navbar Component

```svelte
<!-- src/lib/components/Navbar.svelte -->
<script lang="ts">
  import { session } from '$lib/state/session';
  import { logout } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';

  async function handleLogout() {
    await logout();
    goto(resolve('/'));
  }
</script>

<header class="bg-white shadow-sm border-b">
  <div class="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center">
    <a href="/" class="text-xl font-bold">KissOO Svelte 5</a>
    
    <nav class="flex items-center gap-4">
      <a href={resolve('/')}>Home</a>
      
      {#if session.isAuthenticated}
        <a href={resolve('/users')}>Users</a>
        <button onclick={handleLogout}>Logout</button>
        <span class="text-green-600">Authenticated</span>
      {:else}
        <a href={resolve('/login')}>Login</a>
        <a href={resolve('/signup')}>Sign Up</a>
      {/if}
    </nav>
  </div>
</header>
```

### Key Features
- Uses `session.isAuthenticated` for reactive auth checking
- Shows different navigation items based on auth status
- Uses `resolve()` for all routes

---

## Layout & Navbar

The root layout (`+layout.svelte`) includes the Navbar and handles session initialization:

```svelte
<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session';
  import { initBackend } from '$lib/api/Auth';
  import Navbar from '$lib/components/Navbar.svelte';

  onMount(() => {
    initBackend();      // Set backend URL
    session.restore();  // Restore session from localStorage
  });
</script>

<Navbar />

<slot />
```

This ensures:
1. Navbar appears on all pages
2. Session is restored on app load
3. Backend URL is initialized

---

## Forms & Validation

### Login Form Pattern

```svelte
<script lang="ts">
  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  
  let isValid = $derived(username.length > 0 && password.length > 0);
</script>

<form onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
  <input 
    type="text" 
    bind:value={username} 
    placeholder="Username"
  />
  <input 
    type="password" 
    bind:value={password} 
    placeholder="Password"
  />
  <button type="submit" disabled={loading || !isValid}>
    {loading ? 'Logging in...' : 'Login'}
  </button>
</form>
```

### Validation with Derived State

```svelte
<script lang="ts">
  let password = $state('');
  let confirmPassword = $state('');
  
  let passwordsMatch = $derived(password === confirmPassword);
  let isValid = $derived(
    password.length >= 3 && 
    password === confirmPassword
  );
</script>

<input 
  type="password" 
  bind:value={password} 
  class:border-red-500={!passwordsMatch && confirmPassword}
/>
<input 
  type="password" 
  bind:value={confirmPassword} 
/>

{#if confirmPassword && !passwordsMatch}
  <p class="text-red-500">Passwords do not match</p>
{/if}
```

---

## Best Practices

### 1. Use Runes for Reactive State
```svelte
<!-- Good -->
let loading = $state(false);

<!-- Avoid (Svelte 4 style) -->
let loading = false;  // Not reactive
```

### 2. Use Derived for Computed Values
```svelte
<!-- Good -->
let isValid = $derived(username.length > 0);

<!-- Avoid manual recomputation -->
let isValid = false;
$: isValid = username.length > 0;  // Svelte 4 style
```

### 3. Use resolve() for Navigation
```typescript
// Good
goto(resolve('/login'));

// Avoid hardcoding (unless you have no base path)
goto('/login');
```

### 4. Keep API Modules Simple
```typescript
// Good - simple function
export async function getUsers() {
  return Server.call('services.Users', 'getRecords', {});
}

// Avoid - unnecessary complexity
export const getUsers = query(async () => {
  // Remote function complexity
});
```

### 5. Session Restoration
```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { session } from '$lib/state/session';
  
  onMount(() => {
    session.restore();  // Always restore in onMount
  });
</script>
```

### 6. Form Submission
```svelte
<!-- Good - Svelte 5 style -->
<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>

<!-- Avoid - Svelte 4 deprecated style -->
<form on:submit|preventDefault={handleSubmit}>
```

---

## Backend Integration

The frontend communicates with the Kiss backend via `Server.call()`:

```typescript
// Server.call(serviceClass, method, params)
const result = await Server.call('services.Users', 'getRecords', {});

// Response format
{
  _Success: boolean;
  _ErrorMessage?: string;
  uuid?: string;      // For login responses
  rows?: any[];       // For list queries
  // ... other fields
}
```

### Backend Credentials
- Default: `admin` / `admin`
- Users stored in Perst database
- Session UUID required for authenticated requests

---

## Running the Application

### Development
```bash
cd src/main/frontend-svelte
npm run dev
# Server runs on http://localhost:5173
```

### Build
```bash
npm run build
```

### Backend Requirement
The Kiss backend must be running on port 8080 for authentication and user management to work.

---

## Summary

| Feature | Implementation |
|---------|----------------|
| **Reactivity** | Svelte 5 `$state` and `$derived` runes |
| **State Management** | Custom session with configurable persistence |
| **API Layer** | Plain TypeScript modules wrapping `Server.call()` |
| **Navigation** | `goto()` with `resolve()` from `$app/paths` |
| **Styling** | Tailwind CSS |
| **Forms** | Manual validation with derived state |

This architecture provides a clean, maintainable Svelte 5 foundation without experimental features.
