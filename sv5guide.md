# Svelte 5 Guide - KissOO Implementation

## Table of Contents
1. [Overview](#overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Project Structure](#project-structure)
4. [Svelte 5 Runes](#svelte-5-runes)
5. [API Architecture](#api-architecture)
6. [Available API Endpoints](#available-api-endpoints)
7. [Session Management](#session-management)
8. [Navigation](#navigation)
9. [Component Patterns](#component-patterns)
10. [Layout & Navbar](#layout--navbar)
11. [Forms & Validation](#forms--validation)
12. [Data Fetching](#data-fetching)
13. [Error Handling](#error-handling)
14. [Tailwind CSS Integration](#tailwind-css-integration)
15. [Best Practices](#best-practices)
16. [Quick Reference](#quick-reference)

---

## Overview

KissOO uses **Svelte 5** with a clean, simple architecture that avoids experimental features. The frontend communicates with the Kiss Java/Groovy backend via `Server.call()` wrapped in TypeScript API modules.

### Key Principles
- **Simple API modules** - Plain TypeScript functions wrapping `Server.call()`
- **Svelte 5 Runes** - Reactive state management with `$state` and `$derived`
- **Configurable persistence** - Session can use memory or localStorage
- **Auth-aware UI** - Components react to authentication state

### Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Svelte | 5.x |
| **Build Tool** | Vite | 5.x |
| **Styling** | Tailwind CSS | 3.4.x |
| **Language** | TypeScript | 5.x |
| **Backend** | Kiss (Java/Groovy) | - |

---

## Architecture Diagram

### Frontend Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SVELTE 5 FRONTEND                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                         LAYOUT (+layout.svelte)                      │    │
│  │  ┌──────────────────────────────────────────────────────────────┐   │    │
│  │  │                     Navbar.svelte                             │   │    │
│  │  │  [Home] [Users] [Logout]          KissOO Svelte 5            │   │    │
│  │  └──────────────────────────────────────────────────────────────┘   │    │
│  │                              │                                        │    │
│  │                              ▼                                        │    │
│  │                         <slot />                                      │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                      │                                       │
│  ┌───────────────────────────────────┼───────────────────────────────────┐  │
│  │                                    ▼                                    │  │
│  │                              PAGE CONTENT                              │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │  │
│  │  │   login/        │  │   signup/       │  │   users/        │      │  │
│  │  │   +page.svelte  │  │   +page.svelte  │  │   +page.svelte  │      │  │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘      │  │
│  │           │                    │                    │                │  │
│  └───────────┼────────────────────┼────────────────────┼────────────────┘  │
│              │                    │                    │                     │
│              ▼                    ▼                    ▼                     │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                         API MODULES ($lib/api/)                      │    │
│  │  ┌─────────────────┐  ┌─────────────────┐                           │  │
│  │  │   Auth.ts       │  │   Users.ts      │                           │  │
│  │  │  - login()      │  │  - getUsers()   │                           │  │
│  │  │  - logout()     │  │  - addUser()    │                           │  │
│  │  │  - signup()     │  │  - deleteUser() │                           │  │
│  │  └────────┬────────┘  └────────┬────────┘                           │  │
│  └───────────┼────────────────────┼────────────────────────────────────┘  │
│              │                    │                                         │
│              └────────────┬───────┘                                         │
│                           │                                                 │
│                           ▼                                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      Server.call() ($lib/services/)                  │    │
│  │               POST http://localhost:8080/rest                        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           KISS BACKEND (port 8080)                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Hierarchy

```
+layout.svelte
├── Navbar.svelte (always visible)
└── <slot /> (page content)
    ├── +page.svelte (home)
    ├── login/+page.svelte
    ├── signup/+page.svelte
    └── users/+page.svelte
```

### Data Flow

```
┌──────────────┐         ┌─────────────────┐         ┌─────────────────┐
│   User       │         │   Component     │         │   API Module    │
│   Action     │         │   ($state)      │         │   (Auth.ts)     │
└──────┬───────┘         └────────┬────────┘         └────────┬────────┘
       │                          │                           │
       │ onclick={handleLogin}    │                           │
       ├─────────────────────────>│                           │
       │                          │                           │
       │                          │ await login(username, pw) │
       │                          ├──────────────────────────>│
       │                          │                           │
       │                          │                           │ Server.call('', 'Login', ...)
       │                          │                           │──────────────────────────>
       │                          │                           │
       │                          │          { _Success, uuid }│
       │                          │                           │<──────────────────────────
       │                          │                           │
       │                          │ session.setUUID(uuid)     │
       │                          │ goto(resolve('/'))        │
       │                          │                           │
       │    Page changes          │                           │
       │<─────────────────────────┤                           │
       │                          │                           │
```

---

## Project Structure

```
src/main/frontend-svelte/
├── src/
│   ├── lib/
│   │   ├── api/                    # API modules (TypeScript)
│   │   │   ├── Auth.ts            # Authentication functions
│   │   │   └── Users.ts           # User CRUD functions
│   │   ├── components/            # Reusable components
│   │   │   └── Navbar.svelte      # Navigation bar
│   │   ├── services/              # Backend communication
│   │   │   └── Server.ts          # Server.call() wrapper
│   │   ├── state/                 # Reactive state
│   │   │   └── session.svelte.ts  # Session with runes
│   │   ├── api/                   # Re-exports
│   │   │   └── index.ts
│   │   └── index.ts
│   ├── routes/                    # SvelteKit pages
│   │   ├── +layout.svelte         # Root layout (Navbar)
│   │   ├── +page.svelte           # Home page
│   │   ├── login/
│   │   │   └── +page.svelte       # Login page
│   │   ├── signup/
│   │   │   └── +page.svelte       # Signup page
│   │   └── users/
│   │       └── +page.svelte       # User management
│   ├── app.css                    # Tailwind imports
│   └── app.html                   # HTML template
├── static/                        # Static assets
├── svelte.config.js               # SvelteKit config
├── tailwind.config.js             # Tailwind config
├── vite.config.ts                 # Vite config
├── tsconfig.json                  # TypeScript config
└── package.json                   # Dependencies
```

---

## Svelte 5 Runes

Runes are Svelte 5's new reactivity system. They replace stores and reactive declarations.

### $state - Reactive Variables

```svelte
<script lang="ts">
  // Primitive state
  let count = $state(0);
  let username = $state('');
  let loading = $state(false);
  
  // Object state (deeply reactive)
  let user = $state({ name: '', email: '' });
  user.name = 'John';  // Triggers reactivity
  
  // Array state
  let items = $state<string[]>([]);
  items.push('new item');  // Triggers reactivity
</script>

<button onclick={() => count++}>
  Count: {count}
</button>
```

### $derived - Computed Values

```svelte
<script lang="ts">
  let firstName = $state('');
  let lastName = $state('');
  
  // Auto-updates when firstName or lastName changes
  let fullName = $derived(`${firstName} ${lastName}`);
  
  // Complex derived values
  let isValid = $derived(
    firstName.length >= 2 && 
    lastName.length >= 2
  );
  
  // Derived from arrays
  let items = $state<number[]>([]);
  let itemCount = $derived(items.length);
  let total = $derived(items.reduce((sum, n) => sum + n, 0));
</script>

<p>Name: {fullName}</p>
<p>Total: {total} items</p>
<button disabled={!isValid}>Submit</button>
```

### Complete Login Page Example

```svelte
<!-- src/routes/login/+page.svelte -->
<script lang="ts">
  import { login, initBackend } from '$lib/api/Auth';
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';

  // Reactive state with $state
  let username = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  
  // Computed validation with $derived
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
      password = '';  // Clear password on failure
    }
    
    loading = false;
  }
</script>

<div class="min-h-screen flex items-center justify-center">
  <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
    <h1 class="text-2xl font-bold text-center mb-6">Login</h1>
    
    {#if error}
      <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {error}
      </div>
    {/if}
    
    <form onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
      <input type="text" bind:value={username} placeholder="Username" />
      <input type="password" bind:value={password} placeholder="Password" />
      <button type="submit" disabled={loading || !isValid}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  </div>
</div>
```

### Real Examples from Codebase

| File | State Variables | Derived Values |
|------|-----------------|----------------|
| `login/+page.svelte` | username, password, loading, error | isValid |
| `signup/+page.svelte` | username, password, confirmPassword, loading, error | isValid, passwordsMatch |
| `users/+page.svelte` | users[], newUserName, newUserPassword, loading, error, dataLoading | canAddUser |

---

## API Architecture

API modules are **plain TypeScript functions** that wrap `Server.call()`. They run on the client and communicate directly with the Kiss backend.

### Communication Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API CALL FLOW                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Component                           API Module                             │
│   ─────────                           ──────────                             │
│                                                                              │
│   import { login } from 'Auth'                                             │
│   const res = await login('admin', 'pass')                                  │
│        │                                                                    │
│        │ calls login()                                                      │
│        ▼                                                                    │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  Auth.ts                                                             │   │
│   │                                                                      │   │
│   │  export async function login(username, password) {                  │   │
│   │    const res = await Server.call('', 'Login', {                     │   │
│   │      username: username.toLowerCase(),                               │   │
│   │      password: password                                              │   │
│   │    });                                                               │   │
│   │                                                                      │   │
│   │    if (res._Success && res.uuid) {                                  │   │
│   │      session.setUUID(res.uuid);  // Store UUID                      │   │
│   │    }                                                                 │   │
│   │    return res;                                                       │   │
│   │  }                                                                   │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│        │                                                                    │
│        │ calls Server.call()                                                │
│        ▼                                                                    │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  Server.ts                                                           │   │
│   │                                                                      │   │
│   │  POST http://localhost:8080/rest                                    │   │
│   │  Body: { _class: "", _method: "Login", username, password }        │   │
│   │                                                                      │   │
│   │  Response: { _Success: true, uuid: "abc-123..." }                  │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Auth.ts

```typescript
// src/lib/api/Auth.ts
import { Server } from '$lib/services/Server';
import { session } from '$lib/state/session';

export interface LoginResult {
  _Success: boolean;
  uuid?: string;
  _ErrorMessage?: string;
}

export async function login(username: string, password: string): Promise<LoginResult> {
  const res = await Server.call('', 'Login', {
    username: username.toLowerCase(),
    password: password
  });
  
  if (res._Success && res.uuid) {
    session.setUUID(res.uuid);
  }
  return res;
}

export async function logout(): Promise<void> {
  try {
    await Server.call('', 'Logout', {});
  } catch (e) {}
  session.clear();
}

export async function signup(username: string, password: string): Promise<LoginResult> {
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

export function initBackend(): void {
  // Determine backend URL based on environment
  if (typeof window === 'undefined') return;
  
  if (window.location.protocol === 'file:') {
    Server.setURL('http://localhost:8080');
  } else {
    const port = parseInt(window.location.port || '0');
    // Dev mode (Vite) uses port 5173/5174, backend is on 8080
    if (port === 5173 || port === 5174) {
      Server.setURL('http://localhost:8080');
    } else {
      Server.setURL(window.location.origin);
    }
  }
}
```

### Users.ts

```typescript
// src/lib/api/Users.ts
import { Server } from '$lib/services/Server';

export interface User {
  id: number;      // Perst object ID (oid)
  userName: string;
  userPassword: string;
  userActive: 'Y' | 'N';
}

export interface ApiResult {
  success: boolean;
  error?: string;
  id?: number;
}

export async function getUsers(): Promise<User[]> {
  const res = await Server.call('services.Users', 'getRecords', {});
  return res.rows || [];
}

export async function addUser(userName: string, userPassword: string): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'addRecord', {
    userName: userName.toLowerCase(),
    userPassword,
    userActive: 'Y'
  });
  
  return {
    success: res._Success || res.success || false,
    error: res._ErrorMessage || res.error,
    id: res.id
  };
}

export async function deleteUser(id: number): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'deleteRecord', { id });
  
  return {
    success: res._Success || res.success || false,
    error: res._ErrorMessage || res.error
  };
}

export async function updateUser(
  id: number,
  userName: string,
  userPassword: string,
  userActive: 'Y' | 'N'
): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'updateRecord', {
    id,
    userName: userName.toLowerCase(),
    userPassword,
    userActive
  });
  
  return {
    success: res._Success || res.success || false,
    error: res._ErrorMessage || res.error
  };
}
```

### Server.ts

```typescript
// src/lib/services/Server.ts
export class Server {
  private static url: string = '';
  private static uuid: string = '';
  
  static setURL(url: string): void {
    this.url = url;
  }
  
  static getURL(): string {
    return this.url;
  }
  
  static setUUID(uuid: string): void {
    this.uuid = uuid;
  }
  
  static getUUID(): string {
    return this.uuid;
  }
  
  static async call(
    serviceClass: string,
    method: string,
    params: Record<string, any> = {}
  ): Promise<any> {
    const payload = {
      ...params,
      _uuid: this.uuid,
      _method: method,
      _class: serviceClass
    };
    
    const response = await fetch(`${this.url}/rest`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    const result = await response.json();
    
    // Handle session expiration
    if (!result._Success && result._ErrorCode === 2) {
      this.uuid = '';
      throw new Error('Session expired');
    }
    
    return result;
  }
}
```

### Creating New API Modules

To add a new API module (e.g., for Actors):

```typescript
// src/lib/api/Actors.ts
import { Server } from '$lib/services/Server';

export interface Actor {
  id: number;
  uuid: string;
  name: string;
  type: string;
  active: boolean;
}

export async function getActors(): Promise<Actor[]> {
  const res = await Server.call('services.ActorService', 'getAll', {});
  return res.actors || [];
}

export async function createActor(name: string, type: string): Promise<Actor> {
  const res = await Server.call('services.ActorService', 'create', { name, type });
  if (!res._Success) throw new Error(res._ErrorMessage);
  return res;
}

export async function deleteActor(id: number): Promise<void> {
  const res = await Server.call('services.ActorService', 'delete', { id });
  if (!res._Success) throw new Error(res._ErrorMessage);
}
```

---

## Available API Endpoints

### Authentication Endpoints

| Service Class | Method | Auth Required | Description |
|---------------|--------|---------------|-------------|
| `""` (empty) | `Login` | ❌ No | Authenticate user, returns UUID |
| `""` (empty) | `Logout` | ✅ Yes | Invalidate session |
| `""` (empty) | `checkLogin` | ✅ Yes | Verify session is valid |
| `""` (empty) | `LoginRequired` | ❌ No | Check if auth required |

### Users Service (`services.Users`)

| Method | Auth Required | Request Body | Response |
|--------|---------------|--------------|----------|
| `getRecords` | ✅ Yes | `{}` | `{ rows: User[] }` |
| `addRecord` | ❌ No* | `{ userName, userPassword, userActive }` | `{ success, id }` |
| `updateRecord` | ✅ Yes | `{ id, userName, userPassword, userActive }` | `{ success }` |
| `deleteRecord` | ✅ Yes | `{ id }` | `{ success }` |

*Endpoint allowed without auth for first-time setup

### Request/Response Format

**Request:**
```json
{
  "_class": "services.Users",
  "_method": "getRecords",
  "_uuid": "session-uuid-here",
  "param1": "value1"
}
```

**Success Response:**
```json
{
  "_Success": true,
  "rows": [...],
  "id": 123
}
```

**Error Response:**
```json
{
  "_Success": false,
  "_ErrorMessage": "Description of the error",
  "_ErrorCode": 1
}
```

### Common Error Codes

| Code | Meaning | Action |
|------|---------|--------|
| 1 | General error | Display `_ErrorMessage` |
| 2 | Session expired | Redirect to login |
| - | Success | Process `_Success: true` response |

### Using Endpoints in Components

```svelte
<script lang="ts">
  import { getUsers, addUser } from '$lib/api/Users';
  
  // GET all users
  const users = await getUsers();
  
  // CREATE user
  const result = await addUser('john', 'password123');
  if (result.success) {
    console.log('Created user with ID:', result.id);
  } else {
    console.error('Error:', result.error);
  }
  
  // DELETE user
  await deleteUser(123);
</script>
```

---

## Session Management

### Session State Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SESSION LIFECYCLE                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐                                                           │
│  │ App Start    │                                                           │
│  └──────┬───────┘                                                           │
│         │                                                                    │
│         ▼                                                                    │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  onMount() in +layout.svelte                                         │   │
│  │  ┌──────────────────────────────────────────────────────────────┐    │   │
│  │  │  1. initBackend()     → Set backend URL                      │    │   │
│  │  │  2. session.restore() → Try to restore UUID from localStorage│    │   │
│  │  └──────────────────────────────────────────────────────────────┘    │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│         │                                                                    │
│         ▼                                                                    │
│  ┌──────────────┐                                                           │
│  │ UUID exists? │                                                           │
│  └──────┬───────┘                                                           │
│         │                                                                    │
│    ┌────┴────┐                                                               │
│    │         │                                                               │
│   YES       NO                                                               │
│    │         │                                                               │
│    │         ▼                                                               │
│    │    ┌──────────────┐                                                    │
│    │    │ Not logged in │                                                    │
│    │    └──────────────┘                                                    │
│    │         │                                                               │
│    │         ▼                                                               │
│    │    ┌────────────────────────────────────────────────────────────┐     │
│    │    │ User navigates to login page                               │     │
│    │    │                                                            │     │
│    │    │ Login Form                                                 │     │
│    │    │ ┌──────────────────────────────────────────────────────┐  │     │
│    │    │ │ Username: [____________]                              │  │     │
│    │    │ │ Password: [____________]                              │  │     │
│    │    │ │                                      [Login]         │  │     │
│    │    │ └──────────────────────────────────────────────────────┘  │     │
│    │    │                        │                                   │     │
│    │    │                        ▼                                   │     │
│    │    │              Server.call('', 'Login')                     │     │
│    │    │                        │                                   │     │
│    │    │                        ▼                                   │     │
│    │    │              { _Success: true, uuid: "abc-123" }         │     │
│    │    │                        │                                   │     │
│    │    │                        ▼                                   │     │
│    │    │              session.setUUID(uuid)                        │     │
│    │    │              goto(resolve('/'))                           │     │
│    │    └────────────────────────────────────────────────────────────┘     │
│    │                                                                       │
│    ▼                                                                       │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ Logged in - UUID stored                                               │  │
│  │                                                                      │  │
│  │  Storage Options:                                                    │  │
│  │  ┌──────────────────────────────────────────────────────────────┐   │  │
│  │  │  Memory: session.setUUID(uuid, false)                        │   │  │
│  │  │  - Lost on page refresh                                      │   │  │
│  │  │  - More secure                                               │   │  │
│  │  │                                                               │   │  │
│  │  │  localStorage: session.setUUID(uuid) or session.setUUID(uuid, true) │  │
│  │  │  - Survives page refresh                                     │   │  │
│  │  │  - Better UX                                                 │   │  │
│  │  └──────────────────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│         │                                                                    │
│         ▼                                                                    │
│  ┌──────────────┐                                                           │
│  │ Logout       │                                                           │
│  └──────┬───────┘                                                           │
│         │                                                                    │
│         ▼                                                                    │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  1. Server.call('', 'Logout')                                       │  │
│  │  2. session.clear()                                                 │  │
│  │  3. goto(resolve('/login'))                                         │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

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
  },
  
  // Configure persistence
  setPersistence(enabled: boolean): void {
    persistToStorage = enabled;
    if (!enabled) {
      localStorage.removeItem(STORAGE_KEY);
    }
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

### Navigation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NAVIGATION FLOW                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────┐                                                      │
│   │ Import Required │                                                      │
│   │ Functions       │                                                      │
│   └────────┬────────┘                                                      │
│            │                                                                │
│   ┌────────┴────────────────────────────────────────────────────────────┐  │
│   │  import { goto } from '$app/navigation';                            │  │
│   │  import { resolve } from '$app/paths';                              │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│            │                                                                │
│            ▼                                                                │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │  Programmatic Navigation                                            │  │
│   │                                                                      │  │
│   │  goto(resolve('/'))           → Home page                           │  │
│   │  goto(resolve('/login'))      → Login page                          │  │
│   │  goto(resolve('/users'))      → Users page                          │  │
│   │                                                                      │  │
│   │  With query params:                                                  │  │
│   │  goto(resolve('/users?id=123')) → Users page with ID               │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│            │                                                                │
│            ▼                                                                │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │  HTML Links (also use resolve)                                      │  │
│   │                                                                      │  │
│   │  <a href={resolve('/')}>Home</a>                                   │  │
│   │  <a href={resolve('/login')}>Login</a>                             │  │
│   │                                                                      │  │
│   │  With query params:                                                  │  │
│   │  <a href={resolve('/users')} class="link">View Users</a>           │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Code Examples

```svelte
<script lang="ts">
  import { goto } from '$app/navigation';
  import { resolve } from '$app/paths';
  
  // Programmatic navigation
  function navigateHome() {
    goto(resolve('/'));
  }
  
  function navigateToLogin() {
    goto(resolve('/login'));
  }
  
  // With query parameters
  function viewUser(userId: number) {
    goto(resolve(`/users?id=${userId}`));
  }
  
  // After async operation
  async function handleLogin() {
    const res = await login(username, password);
    if (res._Success) {
      goto(resolve('/'));
    }
  }
</script>

<!-- HTML Links -->
<a href={resolve('/')}>Home</a>
<a href={resolve('/login')}>Login</a>
<a href={resolve('/users')}>Users</a>
```

### Why resolve()?

| Without resolve() | With resolve() |
|-------------------|----------------|
| `goto('/login')` | `goto(resolve('/login'))` |
| Hardcoded paths | Handles base path configuration |
| May break with base path | Type-safe route resolution |
| Simple but limited | Official SvelteKit approach |

---

## Component Patterns

### Common Component Patterns

#### Pattern 1: Page with Data Loading

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { getUsers } from '$lib/api/Users';
  
  // State
  let users = $state<User[]>([]);
  let loading = $state(true);
  let error = $state('');
  
  // Load data on mount
  onMount(async () => {
    try {
      users = await getUsers();
    } catch (e: any) {
      error = 'Failed to load users: ' + e.message;
    } finally {
      loading = false;
    }
  });
</script>

{#if loading}
  <p>Loading...</p>
{:else if error}
  <p class="text-red-500">{error}</p>
{:else}
  <ul>
    {#each users as user (user.id)}
      <li>{user.userName}</li>
    {/each}
  </ul>
{/if}
```

#### Pattern 2: Form with Validation

```svelte
<script lang="ts">
  let email = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state('');
  
  // Derived validation
  let isValid = $derived(
    email.includes('@') && 
    password.length >= 6
  );
  
  async function handleSubmit() {
    if (!isValid) return;
    loading = true;
    error = '';
    
    try {
      // Submit logic
    } catch (e: any) {
      error = e.message;
    } finally {
      loading = false;
    }
  }
</script>

<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
  <input type="email" bind:value={email} />
  <input type="password" bind:value={password} />
  <button type="submit" disabled={loading || !isValid}>
    {loading ? 'Submitting...' : 'Submit'}
  </button>
</form>
```

#### Pattern 3: Reusable List Component

```svelte
<!-- List.svelte -->
<script lang="ts" generics="T">
  interface Props {
    items: T[];
    loading?: boolean;
    error?: string;
    renderItem: (item: T) => any;
    key: (item: T) => string | number;
  }
  
  let { items, loading, error, renderItem, key }: Props = $props();
</script>

{#if loading}
  <slot name="loading">
    <p>Loading...</p>
  </slot>
{:else if error}
  <slot name="error" {error}>
    <p class="text-red-500">{error}</p>
  </slot>
{:else if items.length === 0}
  <slot name="empty">
    <p>No items found.</p>
  </slot>
{:else}
  <ul>
    {#each items as item (key(item))}
      <li>{@render renderItem(item)}</li>
    {/each}
  </ul>
{/if}
```

#### Pattern 4: Auth-Aware Component

```svelte
<script lang="ts">
  import { session } from '$lib/state/session';
  
  interface Props {
    whenAuth?: any;
    whenUnauth?: any;
  }
  
  let { whenAuth, whenUnauth }: Props = $props();
</script>

{#if session.isAuthenticated}
  <slot name="auth">
    {@render whenAuth?.()}
  </slot>
{:else}
  <slot name="unauth">
    {@render whenUnauth?.()}
  </slot>
{/if}
```

---

## Layout & Navbar

### Layout Structure

```
+layout.svelte
│
├── <script>
│   └── onMount: initBackend(), session.restore()
│
├── <Navbar />    (always visible)
│   ├── Logo/Title
│   └── Navigation (auth-aware)
│
└── <slot />      (page content)
    ├── +page.svelte
    ├── login/+page.svelte
    ├── signup/+page.svelte
    └── users/+page.svelte
```

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
  <input type="text" bind:value={username} placeholder="Username" />
  <input type="password" bind:value={password} placeholder="Password" />
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

### Complete Form Example (Signup)

```svelte
<script lang="ts">
  import { signup } from '$lib/api/Auth';
  
  let username = $state('');
  let password = $state('');
  let confirmPassword = $state('');
  let loading = $state(false);
  let error = $state('');
  
  // Multiple derived validations
  let passwordsMatch = $derived(password === confirmPassword);
  let isUsernameValid = $derived(username.length >= 3);
  let isPasswordValid = $derived(password.length >= 3);
  let isValid = $derived(isUsernameValid && isPasswordValid && passwordsMatch);
  
  async function handleSignup() {
    if (!isValid) return;
    
    loading = true;
    error = '';
    
    try {
      const res = await signup(username, password);
      if (res._Success) {
        window.location.href = '/';
      } else {
        error = res._ErrorMessage || 'Signup failed';
      }
    } catch (e: any) {
      error = 'Signup failed: ' + e.message;
    } finally {
      loading = false;
    }
  }
</script>

<form onsubmit={(e) => { e.preventDefault(); handleSignup(); }}>
  {#if error}
    <div class="bg-red-100 text-red-700 p-3 rounded mb-4">{error}</div>
  {/if}
  
  <div class="mb-4">
    <label for="username">Username</label>
    <input
      type="text"
      id="username"
      bind:value={username}
      class:border-red-500={!isUsernameValid && username.length > 0}
    />
    {#if !isUsernameValid && username.length > 0}
      <p class="text-red-500 text-sm">Username must be at least 3 characters</p>
    {/if}
  </div>
  
  <div class="mb-4">
    <label for="password">Password</label>
    <input
      type="password"
      id="password"
      bind:value={password}
      class:border-red-500={!isPasswordValid && password.length > 0}
    />
  </div>
  
  <div class="mb-6">
    <label for="confirmPassword">Confirm Password</label>
    <input
      type="password"
      id="confirmPassword"
      bind:value={confirmPassword}
      class:border-red-500={!passwordsMatch && confirmPassword}
    />
    {#if !passwordsMatch && confirmPassword}
      <p class="text-red-500 text-sm">Passwords do not match</p>
    {/if}
  </div>
  
  <button
    type="submit"
    disabled={loading || !isValid}
    class="w-full bg-green-600 text-white py-2 px-4 rounded disabled:opacity-50"
  >
    {loading ? 'Creating account...' : 'Sign Up'}
  </button>
</form>
```

---

## Data Fetching

### Pattern: Load on Mount

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { getUsers } from '$lib/api/Users';
  
  let users = $state<User[]>([]);
  let loading = $state(true);
  let error = $state('');
  
  onMount(async () => {
    try {
      users = await getUsers();
    } catch (e: any) {
      error = e.message;
    } finally {
      loading = false;
    }
  });
</script>

{#if loading}
  <div class="flex justify-center">
    <div class="animate-spin h-8 w-8 border-4 border-blue-500 rounded-full border-t-transparent"></div>
  </div>
{:else if error}
  <div class="bg-red-100 text-red-700 p-4 rounded">
    {error}
  </div>
{:else}
  <ul class="space-y-2">
    {#each users as user}
      <li class="p-3 bg-white rounded shadow">{user.userName}</li>
    {/each}
  </ul>
{/if}
```

### Pattern: Refresh Data

```svelte
<script lang="ts">
  import { getUsers } from '$lib/api/Users';
  
  let users = $state<User[]>([]);
  let loading = $state(false);
  
  async function loadData() {
    loading = true;
    try {
      users = await getUsers();
    } finally {
      loading = false;
    }
  }
  
  // Initial load
  $effect(() => {
    loadData();
  });
</script>

<button onclick={loadData} disabled={loading}>
  {loading ? 'Refreshing...' : 'Refresh'}
</button>
```

### Pattern: CRUD Operations

```svelte
<script lang="ts">
  import { getUsers, addUser, deleteUser } from '$lib/api/Users';
  
  let users = $state<User[]>([]);
  let newUserName = $state('');
  let newUserPassword = $state('');
  let operationLoading = $state(false);
  let error = $state('');
  
  // Load users
  async function loadUsers() {
    users = await getUsers();
  }
  
  // Add user
  async function handleAddUser() {
    operationLoading = true;
    error = '';
    
    const result = await addUser(newUserName, newUserPassword);
    if (result.success) {
      newUserName = '';
      newUserPassword = '';
      await loadUsers();  // Refresh list
    } else {
      error = result.error || 'Failed to add user';
    }
    
    operationLoading = false;
  }
  
  // Delete user
  async function handleDeleteUser(id: number) {
    if (!confirm('Are you sure?')) return;
    
    const result = await deleteUser(id);
    if (result.success) {
      await loadUsers();  // Refresh list
    } else {
      error = result.error || 'Failed to delete user';
    }
  }
</script>
```

---

## Error Handling

### Error Display Component

```svelte
<!-- ErrorMessage.svelte -->
<script lang="ts">
  interface Props {
    message: string;
    onDismiss?: () => void;
  }
  
  let { message, onDismiss }: Props = $props();
</script>

{#if message}
  <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
    <span class="block sm:inline">{message}</span>
    {#if onDismiss}
      <button 
        onclick={onDismiss}
        class="absolute top-0 right-0 px-4 py-3"
      >
        ×
      </button>
    {/if}
  </div>
{/if}
```

### Error Boundary Pattern

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  
  let error = $state('');
  let data = $state(null);
  
  async function fetchData() {
    try {
      // API call
    } catch (e: any) {
      if (e.message.includes('Session expired')) {
        // Redirect to login
        window.location.href = '/login';
        return;
      }
      error = e.message;
    }
  }
  
  onMount(fetchData);
</script>

{#if error}
  <div class="bg-red-100 text-red-700 p-4 rounded">
    <p>{error}</p>
    <button onclick={() => { error = ''; fetchData(); }}>
      Retry
    </button>
  </div>
{:else}
  <slot />
{/if}
```

### API Error Handling

```typescript
// src/lib/api/Users.ts

export async function getUsers(): Promise<User[]> {
  try {
    const res = await Server.call('services.Users', 'getRecords', {});
    
    if (!res._Success) {
      throw new Error(res._ErrorMessage || 'Failed to fetch users');
    }
    
    return res.rows || [];
  } catch (e: any) {
    // Handle specific errors
    if (e.message.includes('Session expired')) {
      // Clear local session
      session.clear();
      // Redirect will be handled by component
      throw new Error('Session expired');
    }
    throw e;
  }
}
```

---

## Tailwind CSS Integration

### Configuration Files

**tailwind.config.js**:
```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {}
  },
  plugins: []
};
```

**postcss.config.js**:
```javascript
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {}
  }
};
```

**app.css**:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

### Common Tailwind Patterns

```svelte
<!-- Buttons -->
<button class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
  Click Me
</button>

<!-- Cards -->
<div class="bg-white rounded-lg shadow-md p-6">
  <h2 class="text-xl font-bold mb-4">Title</h2>
  <p class="text-gray-600">Content</p>
</div>

<!-- Form inputs -->
<input class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">

<!-- Loading spinner -->
<div class="animate-spin h-8 w-8 border-4 border-blue-500 rounded-full border-t-transparent"></div>

<!-- Alerts -->
<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
  Error message
</div>
<div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
  Success message
</div>
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
$: isValid = username.length > 0;  // Svelte 4 style
```

### 3. Use resolve() for Navigation
```typescript
// Good
goto(resolve('/login'));

// Avoid hardcoding (unless no base path)
goto('/login');
```

### 4. Keep API Modules Simple
```typescript
// Good - simple function
export async function getUsers() {
  return Server.call('services.Users', 'getRecords', {});
}

// Avoid - unnecessary complexity
export const getUsers = query(async () => { /* ... */ });
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

### 6. Form Submission (Svelte 5 Style)
```svelte
<!-- Good -->
<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>

<!-- Avoid - Svelte 4 deprecated style -->
<form on:submit|preventDefault={handleSubmit}>
```

### 7. Use $effect for Side Effects
```svelte
<script lang="ts">
  let count = $state(0);
  
  // Runs when count changes
  $effect(() => {
    console.log('Count changed:', count);
    document.title = `Count: ${count}`;
  });
</script>
```

### 8. Clean Up in onDestroy (if needed)
```svelte
<script lang="ts">
  import { onDestroy } from 'svelte';
  
  let interval: number;
  
  $effect(() => {
    interval = setInterval(() => { /* ... */ }, 1000);
    
    return () => {
      clearInterval(interval);
    };
  });
</script>
```

---

## Quick Reference

### Svelte 5 Runes Cheat Sheet

| Rune | Purpose | Example |
|------|---------|---------|
| `$state` | Reactive variable | `let count = $state(0)` |
| `$derived` | Computed value | `let doubled = $derived(count * 2)` |
| `$effect` | Side effect | `$effect(() => { console.log(count) })` |
| `$props` | Component props | `let { name }: Props = $props()` |
| `$bindable` | Two-way binding | `let { value = $bindable() }: Props = $props()` |

### Common Imports

```typescript
// Svelte
import { onMount, onDestroy } from 'svelte';

// SvelteKit
import { goto } from '$app/navigation';
import { resolve } from '$app/paths';
import { page } from '$app/state';  // Svelte 5

// KissOO
import { Server } from '$lib/services/Server';
import { session } from '$lib/state/session';
import { login, logout, signup } from '$lib/api/Auth';
import { getUsers, addUser, deleteUser } from '$lib/api/Users';
```

### API Response Format

```typescript
// Success
{ _Success: true, ...data }

// Error
{ _Success: false, _ErrorMessage: "Error message", _ErrorCode: 1 }

// Login success
{ _Success: true, uuid: "session-uuid" }
```

### Build Commands

```bash
cd src/main/frontend-svelte
npm run dev     # Development server (port 5173)
npm run build   # Production build
npm run check   # Type checking
npm run lint    # Linting
```

### Backend Credentials

- **Username**: `admin`
- **Password**: `admin`
- **Backend URL**: `http://localhost:8080` (when running dev server)

---

## See Also

- **KissOO-Guide.md** - Backend/Perst comprehensive guide
- **PERST_USAGE.md** - Perst database usage
- **MANAGER_AT_THE_GATE.md** - Authorization pattern
- **AI/KnowledgeBase.md** - Framework knowledge base

---

*Last Updated: 2026-03-22*
