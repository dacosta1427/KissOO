# Svelte 5 Migration Guide

## Overview

This document captures the experience and lessons learned from migrating a SvelteKit frontend to Svelte 5.

## Key Concepts

### Runes System

Svelte 5 introduces **runes** - special functions that mark reactivity:

- `$state(initialValue)` - Creates reactive state
- `$derived(expression)` - Creates computed values
- `$effect(() => {...})` - Runs side effects
- `$props()` - Component props (replaces `export let`)

### State Sharing Pattern

**IMPORTANT**: You cannot export and reassign `$state` directly from modules. Use one of these patterns:

#### 1. Reactive Object Pattern (Recommended)

```javascript
// stores.svelte.js
export const userState = $state({ value: null });

// Usage in components
import { userState } from '$lib/stores.svelte.js';

// Read/write via .value property
{#if userState.value}
  <p>Logged in as {userState.value.name}</p>
{/if}

function login(userData) {
  userState.value = userData;
}
```

#### 2. Class-Based Pattern

```javascript
// stores.svelte.js
class StateHolder {
  userState = $state(null);
  
  login(userData) {
    this.userState = userData;
  }
}

export const state = new StateHolder();
```

### Event Handling

**OLD (Svelte 4)**:
```svelte
<button on:click={handleClick}>Click</button>
<form on:submit|preventDefault={handleSubmit}>
```

**NEW (Svelte 5)**:
```svelte
<button onclick={handleClick}>Click</button>
<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
```

### Slots to Snippets

**OLD (Svelte 4)**:
```svelte
<!-- Layout -->
<slot />

<!-- Usage -->
<Layout>Content</Layout>
```

**NEW (Svelte 5)**:
```svelte
<!-- Layout -->
let { children } = $props();
{@render children()}

<!-- Usage -->
<Layout>
  {#snippet children()}Content{/snippet}
</Layout>
```

### Props

**OLD (Svelte 4)**:
```svelte
<script>
  export let title = 'Default';
  export let data = [];
</script>
```

**NEW (Svelte 5)**:
```svelte
<script>
  let { title = 'Default', data = [], onAction } = $props();
</script>
```

## Common Issues & Solutions

### 1. "$state is not defined"

**Cause**: `.js` files don't get Svelte 5 runes by default.

**Solution**: Rename to `.svelte.js` extension:
```bash
mv stores.js stores.svelte.js
```

### 2. "Cannot export state from a module if it is reassigned"

**Cause**: Trying to export a `$state` variable that can be reassigned.

**Solution**: Use reactive objects:
```javascript
// WRONG
export let count = $state(0);

// CORRECT - use object
export const count = $state({ value: 0 });
count.value++; // Works!
```

### 3. "Cannot assign to import"

**Cause**: JavaScript doesn't allow reassigning imports.

**Solution**: Use object property mutation:
```javascript
// WRONG
import { count } from './stores.svelte.js';
count = 5; // Error!

// CORRECT
import { count } from './stores.svelte.js';
count.value = 5; // Works!
```

### 4. Legacy store compatibility

Keep backward compatibility by providing both:
```javascript
export const user = {
  subscribe: (fn) => {
    fn(userState.value);
    return () => {};
  },
  set: (value) => { userState.value = value; }
};
```

## File Structure Recommendations

```
src/lib/
├── stores.svelte.js    # Global state (runes)
├── api.ts              # API calls
└── components/
    └── MyComponent.svelte
```

## ESLint Config Fix

If you get `The "paths[0]" argument must be of type string` error:

```javascript
// eslint.config.js
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const gitignorePath = path.resolve(__dirname, '.gitignore');
```

## Testing

Run type checking:
```bash
npm run check
```

Run linting:
```bash
npm run lint
```

Format code:
```bash
npm run format
```

## Dependencies

Ensure Node.js 20+ is used (SvelteKit/Vite requires it):
```bash
nvm use 20
```

## Migration Checklist

- [ ] Rename `.js` files with `$state` to `.svelte.js`
- [ ] Replace `export let` with `$props()`
- [ ] Replace `on:event` with `onevent`
- [ ] Replace `<slot />` with `{@render children()}`
- [ ] Update store exports to use reactive objects
- [ ] Fix ESLint config for ESM compatibility
- [ ] Update imports to use `.svelte.js` extension
- [ ] Test all components for reactivity issues
