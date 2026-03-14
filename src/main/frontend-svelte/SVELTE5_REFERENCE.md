# Svelte 5 Developer Reference

## Table of Contents

1. [Core Concepts](#core-concepts)
2. [State Management](#state-management)
3. [Event Handling](#event-handling)
4. [Component Patterns](#component-patterns)
5. [Props and Communication](#props-and-communication)
6. [Navigation](#navigation)
7. [API Calls](#api-calls)
8. [Styling](#styling)
9. [File Organization](#file-organization)
10. [Common Pitfalls](#common-pitfalls)
11. [Migration from Svelte 4](#migration-from-svelte-4)
12. [Testing and Tools](#testing-and-tools)
13. [Quick Reference](#quick-reference)

---

## Core Concepts

### Runes System

Svelte 5 introduces **runes** - special functions that mark reactivity:

| Rune | Description |
|------|-------------|
| `$state(initialValue)` | Creates reactive state |
| `$derived(expression)` | Creates computed values |
| `$effect(() => {...})` | Runs side effects |
| `$props()` | Component props (replaces `export let`) |
| `$inspect()` | Debug reactive values |

### Basic Syntax

```svelte
<script>
    // Reactive state
    let count = $state(0);
    let items = $state([]);
    
    // Computed values
    let doubleCount = $derived(count * 2);
    let isEven = $derived(count % 2 === 0);
    
    // Effects
    $effect(() => {
        console.log('Count changed:', count);
        // Side effect logic here
    });
</script>

<p>Count: {count}</p>
<p>Double: {doubleCount}</p>
<button onclick={() => count++}>Increment</button>
```

---

## State Management

### Local Component State

**ALWAYS use `$state()` for reactive variables:**

```svelte
<script>
    // ✅ CORRECT
    let count = $state(0);
    let items = $state([]);
    let isLoading = $state(false);
    let userInput = $state('');
    
    // ❌ WRONG - Will cause reactivity errors
    let count = 0;
    let items = [];
</script>
```

### Global State (Stores)

Use `.svelte.js` files for global state:

```javascript
// src/lib/stores.svelte.js

// Pattern 1: Reactive Object (Recommended)
export const userState = $state({ value: null });

// Usage
import { userState } from '$lib/stores.svelte.js';

// Access via .value
{#if userState.value}
    <p>Hello, {userState.value.name}</p>
{/if}

// Update
function login(userData) {
    userState.value = userData;
}
```

```javascript
// Pattern 2: Class-Based
class StateHolder {
    userState = $state(null);
    
    login(userData) {
        this.userState = userData;
    }
    
    logout() {
        this.userState = null;
    }
}

export const state = new StateHolder();
```

```javascript
// Pattern 3: Legacy Store Compatibility
export const user = {
    subscribe: (fn) => {
        fn(userState.value);
        return () => {};
    },
    set: (value) => { userState.value = value; }
};
```

### Derived State

```svelte
<script>
    let items = $state([1, 2, 3, 4, 5]);
    
    // Computed from state
    let filteredItems = $derived(items.filter(x => x > 2));
    let total = $derived(items.reduce((a, b) => a + b, 0));
    
    // Computed from props
    let { data = [] } = $props();
    let sortedData = $derived([...data].sort());
</script>
```

### Effects

```svelte
<script>
    $effect(() => {
        // Runs on mount and when dependencies change
        console.log('Effect ran');
    });
    
    $effect(() => {
        // Cleanup function
        return () => {
            console.log('Cleanup');
        };
    });
</script>
```

**Note:** `$effect` replaces `onMount`, `onDestroy`, and `$:` reactive statements for side effects.

---

## Event Handling

### Buttons vs Forms

- **Use `onclick` for buttons** - Direct click handling
- **Use `onsubmit` for forms** - Form submission

```svelte
<!-- ✅ CORRECT: Button click -->
<button onclick={handleClick}>Click me</button>

<!-- ✅ CORRECT: Form submission -->
<form onsubmit={handleSubmit}>
    <input type="text" bind:value={formData.name} />
    <button type="submit">Submit</button>
</form>

<!-- ✅ CORRECT: Button in form with onclick -->
<form>
    <input type="text" bind:value={inputValue} />
    <button type="button" onclick={handleAction}>Action</button>
</form>

<!-- ❌ WRONG: Deprecated syntax -->
<button on:click={handleClick}>Click</button>
<form on:submit|preventDefault={handleSubmit}>
```

### Event Handler Syntax

```svelte
<script>
    function handleClick(event) {
        console.log(event.target);
    }
    
    function handleSubmit(event) {
        event.preventDefault();
        // handle submission
    }
    
    function handleInput(event) {
        const value = event.target.value;
    }
</script>

<button onclick={handleClick}>Click</button>
<input oninput={handleInput} />
<form onsubmit={handleSubmit}>
```

### Binding Values

```svelte
<script>
    let name = $state('');
    let agreed = $state(false);
    let selected = $state('option1');
</script>

<input bind:value={name} />
<input type="checkbox" bind:checked={agreed} />
<select bind:value={selected}>
    <option value="option1">Option 1</option>
    <option value="option2">Option 2</option>
</select>
```

---

## Component Patterns

### Props with $props()

```svelte
<!-- Child: MyComponent.svelte -->
<script>
    let { title = 'Default', items = [], onAction } = $props();
</script>

<h1>{title}</h1>
{#each items as item}
    <button onclick={() => onAction?.(item)}>{item.name}</button>
{/each}
```

```svelte
<!-- Parent usage -->
<script>
    import MyComponent from './MyComponent.svelte';
    
    function handleAction(item) {
        console.log('Action:', item);
    }
</script>

<MyComponent title="My Title" items={[{name: 'A'}]} onAction={handleAction} />
```

### Snippets (formerly Slots)

```svelte
<!-- Layout: Card.svelte -->
<script>
    let { children, title } = $props();
</script>

<div class="card">
    <h2>{title}</h2>
    {@render children()}
</div>
```

```svelte
<!-- Usage -->
<script>
    import Card from './Card.svelte';
</script>

<Card title="Hello">
    {#snippet children()}
        <p>This is the card content</p>
    {/snippet}
</Card>
```

### Component Lifecycle

```svelte
<script>
    // Instead of onMount - use $effect
    $effect(() => {
        // Runs on mount and when dependencies change
        const data = fetchData();
        
        // Return cleanup function
        return () => {
            // Cleanup
        };
    });
    
    // Instead of onDestroy - use effect cleanup
    $effect(() => {
        return () => {
            // Cleanup when component is destroyed
        };
    });
</script>
```

---

## Props and Communication

### Parent to Child

```svelte
<!-- Child -->
<script>
    let { message = 'Hello', count = 0 } = $props();
</script>

<p>{message} - {count}</p>
```

```svelte
<!-- Parent -->
<Child message="Hi" count={5} />
```

### Child to Parent (Callbacks)

```svelte
<!-- Child -->
<script>
    let { onNotify } = $props();
    
    function handleClick() {
        onNotify?.('Hello from child');
    }
</script>

<button onclick={handleClick}>Notify</button>
```

```svelte
<!-- Parent -->
<Child onNotify={(msg) => console.log(msg)} />
```

### Multiple Callbacks

```svelte
<!-- Child -->
<script>
    let { onSave, onCancel, onChange } = $props();
</script>

<input oninput={(e) => onChange?.(e.target.value)} />
<button onclick={onSave}>Save</button>
<button onclick={onCancel}>Cancel</button>
```

### Event Bus Pattern (for complex communication)

```javascript
// src/lib/events.svelte.js
export const createEventBus = () => {
    const listeners = $state({});
    
    return {
        on(event, callback) {
            if (!listeners[event]) listeners[event] = [];
            listeners[event].push(callback);
        },
        off(event, callback) {
            if (listeners[event]) {
                listeners[event] = listeners[event].filter(cb => cb !== callback);
            }
        },
        emit(event, data) {
            if (listeners[event]) {
                listeners[event].forEach(cb => cb(data));
            }
        }
    };
};

export const events = createEventBus();
```

---

## Navigation

### Programmatic Navigation

```svelte
<script>
    import { goto } from '$app/navigation';
    import { page } from '$app/stores';
    
    function navigateTo(path) {
        goto(path);
    }
    
    function navigateHome() {
        goto('/');
    }
    
    function navigateWithData() {
        goto('/user/123');
    }
    
    // Get current page info
    let currentPath = $derived(page.url.pathname);
</script>

<button onclick={() => navigateTo('/about')}>About</button>
<p>Current: {currentPath}</p>
```

### Link Component

```svelte
<script>
    import { link } from '$app/navigation';
</script>

<a href="/dashboard" use:link>Dashboard</a>
<!-- Or use regular anchor tags with goto -->
```

### Route Parameters

```svelte
<!-- src/routes/user/[id]/+page.svelte -->
<script>
    import { page } from '$app/stores';
    
    let userId = $derived($page.params.id);
</script>

<p>User ID: {userId}</p>
```

### Query Parameters

```svelte
<script>
    import { page } from '$app/stores';
    
    let search = $derived($page.url.searchParams.get('search'));
</script>
```

---

## API Calls

### Using kiss-client

```javascript
import { kiss } from '$lib/kiss-client.js';

// GET
const response = await kiss.get('/api/service');
const data = await response.json();

// POST
const result = await kiss.post('/api/login', {
    email: 'user@example.com',
    password: 'secret'
});

// PUT
await kiss.put('/api/update', { id: 1, name: 'New' });

// DELETE
await kiss.delete('/api/delete/1');
```

### Full Example with Loading States

```svelte
<script>
    let data = $state([]);
    let loading = $state(false);
    let error = $state(null);
    
    async function loadData() {
        loading = true;
        error = null;
        try {
            const response = await kiss.get('/api/data');
            data = await response.json();
        } catch (e) {
            error = e.message;
        } finally {
            loading = false;
        }
    }
    
    $effect(() => {
        loadData();
    });
</script>

{#if loading}
    <p>Loading...</p>
{:else if error}
    <p class="error">{error}</p>
{:else}
    <ul>
        {#each data as item}
            <li>{item.name}</li>
        {/each}
    </ul>
{/if}
```

### Form Submission

```svelte
<script>
    let formData = $state({ email: '', password: '' });
    let submitting = $state(false);
    let error = $state(null);
    
    async function handleSubmit(event) {
        event.preventDefault();
        submitting = true;
        error = null;
        
        try {
            const response = await kiss.post('/login', formData);
            if (!response.ok) {
                throw new Error('Login failed');
            }
            goto('/dashboard');
        } catch (e) {
            error = e.message;
        } finally {
            submitting = false;
        }
    }
</script>

<form onsubmit={handleSubmit}>
    <input type="email" bind:value={formData.email} required />
    <input type="password" bind:value={formData.password} required />
    {#if error}
        <p class="error">{error}</p>
    {/if}
    <button type="submit" disabled={submitting}>
        {submitting ? 'Logging in...' : 'Login'}
    </button>
</form>
```

---

## Styling

### Scoped Styles

```svelte
<script>
    let { children } = $props();
</script>

<div class="container">
    {@render children()}
</div>

<style>
    .container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 1rem;
    }
</style>
```

### Global Styles

```svelte
<!-- Add to +layout.svelte -->
<style>
    :global(body) {
        margin: 0;
        font-family: system-ui, sans-serif;
    }
    
    :global(*) {
        box-sizing: border-box;
    }
</style>
```

### CSS Variables for Theming

```svelte
<style>
    :root {
        --primary-color: #3498db;
        --text-color: #333;
        --muted-color: #666;
        --border-color: #ddd;
        --spacing-sm: 0.5rem;
        --spacing-md: 1rem;
        --spacing-lg: 2rem;
    }
    
    .button {
        background: var(--primary-color);
        color: white;
        padding: var(--spacing-sm) var(--spacing-md);
        border: none;
        border-radius: 4px;
    }
</style>
```

### Responsive Design

```svelte
<style>
    .container {
        display: grid;
        grid-template-columns: 1fr;
    }
    
    @media (min-width: 768px) {
        .container {
            grid-template-columns: 200px 1fr;
        }
    }
</style>
```

---

## File Organization

### Recommended Structure

```
src/
├── lib/
│   ├── components/
│   │   ├── Button.svelte
│   │   ├── Form.svelte
│   │   ├── Table.svelte
│   │   └── index.js           # Export all components
│   ├── stores.svelte.js        # Global state
│   ├── api.js                  # API functions
│   ├── utils.js                # Utility functions
│   └── constants.js            # App constants
├── routes/
│   ├── +layout.svelte          # Root layout
│   ├── +layout.server.js       # Server load functions
│   ├── +page.svelte            # Home page
│   ├── +page.server.js         # Home page server logic
│   ├── about/
│   │   └── +page.svelte        # /about
│   └── [id]/
│       └── +page.svelte        # Dynamic route
└── app.html
```

### Naming Conventions

- **Components**: PascalCase (`MyComponent.svelte`)
- **Utilities**: camelCase (`utils.js`, `api.js`)
- **Stores**: camelCase with `.svelte.js` (`stores.svelte.js`)
- **Routes**: kebab-case directories (`/cleaners/`, `/bookings/`)

---

## Common Pitfalls

### 1. "$state is not defined"

**Cause**: Using runes in regular `.js` files.

**Solution**: Use `.svelte.js` extension:
```bash
mv stores.js stores.svelte.js
```

### 2. "Cannot export state from a module if it is reassigned"

**Solution**: Use reactive objects:
```javascript
// ❌ WRONG
export let count = $state(0);

// ✅ CORRECT
export const count = $state({ value: 0 });
count.value++;
```

### 3. "Cannot assign to import"

**Solution**: Use object property mutation:
```javascript
// ❌ WRONG
import { count } from './store.js';
count = 5;

// ✅ CORRECT
import { count } from './store.js';
count.value = 5;
```

### 4. Non-reactive updates

**Cause**: Using plain `let` for variables that change.

**Solution**:
```svelte
// ❌ WRONG
let items = [];
function addItem(item) {
    items.push(item); // Won't trigger update
}

// ✅ CORRECT
let items = $state([]);
function addItem(item) {
    items = [...items, item]; // Creates new array
}
```

### 5. Using deprecated `on:` syntax

**Solution**:
```svelte
// ❌ WRONG
<button on:click={handleClick}>
<form on:submit|preventDefault={handleSubmit}>

// ✅ CORRECT
<button onclick={handleClick}>
<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
```

### 6. Using `$event` (not valid in Svelte 5)

**Solution**: Use standard event handling:
```svelte
// ❌ WRONG
function handleSubmit() {
    $event('submit', data);
}

// ✅ CORRECT
let { onSubmit } = $props();
function handleSubmit() {
    onSubmit?.(data);
}
```

### 7. Memory leaks with subscriptions

**Solution**: Use `$derived` for store access:
```svelte
// ❌ WRONG
import { myStore } from './store.js';
let value;
const unsubscribe = myStore.subscribe(v => value = v);
onDestroy(unsubscribe);

// ✅ CORRECT
import { myStore } from './store.js';
let value = $derived($myStore);
// No cleanup needed!
```

### 8. $: reactive statements

**Solution**: Use `$derived` and `$effect`:
```svelte
// ❌ WRONG
$: doubled = count * 2;
$: if (count > 10) console.log('Big count');

// ✅ CORRECT
let doubled = $derived(count * 2);
$effect(() => {
    if (count > 10) console.log('Big count');
});
```

---

## Migration from Svelte 4

| Svelte 4 | Svelte 5 |
|----------|-----------|
| `export let` | `$props()` |
| `$:` | `$derived` |
| `onMount` | `$effect` |
| `onDestroy` | `$effect` cleanup |
| `createEventDispatcher` | Props callbacks |
| `<slot />` | `{@render children()}` |
| `on:click` | `onclick` |
| `on:submit\|preventDefault` | `onsubmit={(e) => { e.preventDefault(); ... }}` |
| `$store` | `$derived($store)` |

### Quick Migration Checklist

- [ ] Rename `.js` files with `$state` to `.svelte.js`
- [ ] Replace `export let` with `$props()`
- [ ] Replace `on:event` with `onevent`
- [ ] Replace `<slot />` with `{@render children()}`
- [ ] Update store exports to use reactive objects
- [ ] Fix ESLint config for ESM compatibility
- [ ] Update imports to use `.svelte.js` extension
- [ ] Test all components for reactivity issues

---

## Testing and Tools

### Type Checking

```bash
npm run check
```

### Linting

```bash
npm run lint
```

### Formatting

```bash
npm run format
```

### Development Server

```bash
npm run dev
```

### Build

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

---

## Quick Reference

### Runes

```javascript
$state(initial)     // Reactive state
$derived(expr)      // Computed value
$effect(() => {...}) // Side effect
$props()            // Component props
$inspect()          // Debug (dev only)
```

### Event Handlers

```svelte
onclick={handler}
oninput={handler}
onsubmit={handler}
onchange={handler}
```

### Conditionals

```svelte
{#if condition}
    <p>True</p>
{:else if other}
    <p>Other</p>
{:else}
    <p>False</p>
{/if}
```

### Loops

```svelte
{#each items as item}
    <p>{item.name}</p>
{/each}

{#each items as item, index}
    <p>{index}: {item.name}</p>
{/each}
```

### Snippets

```svelte
{#snippet children()}
    <p>Content</p>
{/snippet}

<Component>
    {@render children()}
</Component>
```

### Keyed Each (for performance)

```svelte
{#each items as item (item.id)}
    <p>{item.name}</p>
{/each}
```

---

## Additional Resources

- [Svelte 5 Official Docs](https://svelte.dev/docs/svelte)
- [SvelteKit Docs](https://kit.svelte.dev/docs)
- [Svelte 5 Migration Guide](https://svelte.dev/docs/svelte/v5-migration-guide)
