# Svelte 5 Development Guidelines

This document outlines the best practices and conventions for developing with Svelte 5 in this project.

## Table of Contents

1. [Event Handling](#event-handling)
2. [State Management](#state-management)
3. [Reactivity](#reactivity)
4. [Navigation](#navigation)
5. [Component Structure](#component-structure)
6. [Styling](#styling)
7. [Common Pitfalls](#common-pitfalls)

## Event Handling

### Buttons vs Forms

- **Use `onclick` for buttons** - When you want to handle button clicks directly
- **Use `onsubmit` for forms** - When you want to handle form submission events
- **Never use deprecated `on:` syntax** - Use standard HTML event attributes

```svelte
<!-- ✅ CORRECT: Button click handling -->
<button onclick={handleClick}>Click me</button>

<!-- ✅ CORRECT: Form submission handling -->
<form onsubmit={handleSubmit}>
  <button type="submit">Submit</button>
</form>

<!-- ❌ WRONG: Deprecated syntax -->
<button on:click={handleClick}>Click me</button>
<form on:submit|preventDefault={handleSubmit}>
```

### Form Handling

- Use `onclick` on buttons for form actions when you need direct control
- Use `onsubmit` on forms when you want native form submission behavior
- Always specify `type="button"` when using `onclick` to prevent accidental form submission

```svelte
<!-- ✅ CORRECT: Button with onclick in form -->
<form>
	<input type="text" bind:value={inputValue} />
	<button type="button" onclick={handleAction}>Action</button>
</form>

<!-- ✅ CORRECT: Form with onsubmit -->
<form onsubmit={handleSubmit}>
	<input type="text" bind:value={inputValue} />
	<button type="submit">Submit</button>
</form>
```

## State Management

### Use `$state` for All Reactive Variables

- **Always declare reactive variables with `$state(...)`**
- **Never use plain `let` for reactive data**
- **This prevents "non_reactive_update" errors**

```svelte
<script>
	// ✅ CORRECT: All reactive variables use $state
	let count = $state(0);
	let items = $state([]);
	let isLoading = $state(false);
	let userInput = $state('');

	// ❌ WRONG: Plain let variables
	let count = 0; // Will cause reactivity issues
	let items = []; // Will cause reactivity issues
</script>
```

### State Patterns

- Use `$state` for local component state
- Use stores for shared state across components
- Use `$derived` for computed values that depend on state

```svelte
<script>
	import { userStore } from '../stores.js';

	// Local state
	let count = $state(0);
	let isLoading = $state(false);

	// Derived state
	let doubleCount = $derived(count * 2);

	// Store usage
	let currentUser = $derived($userStore.user);
</script>
```

## Reactivity

### `$state` Requirements

- **All variables that change and affect the UI must use `$state`**
- **This includes form inputs, loading states, and data arrays**
- **Failure to use `$state` will cause reactivity errors**

```svelte
<script>
	// ✅ CORRECT: Form inputs with $state
	let email = $state('');
	let password = $state('');
	let isLoading = $state(false);

	async function handleSubmit() {
		isLoading = true; // This works because isLoading uses $state
		// ... form submission logic
		isLoading = false;
	}
</script>
```

### `$derived` for Computed Values

- Use `$derived` for values that depend on other reactive values
- `$derived` values automatically update when dependencies change

```svelte
<script>
	let items = $state([]);
	let searchTerm = $state('');

	// ✅ CORRECT: Derived filtered list
	let filteredItems = $derived(
		items.filter((item) => item.name.toLowerCase().includes(searchTerm.toLowerCase()))
	);
</script>
```

## Navigation

### Use `goto()` for Navigation

- Import from `$app/navigation`
- Use for programmatic navigation between routes
- Works seamlessly with Svelte 5 routing

```svelte
<script>
	import { goto } from '$app/navigation';

	function navigateToDashboard() {
		goto('/');
	}

	function navigateToUser(id) {
		goto(`/users/${id}`);
	}
</script>
```

### Route Structure

- Use SvelteKit's file-based routing
- Place route files in `src/routes/` directory
- Use `+page.svelte` for page components
- Use `+layout.svelte` for shared layouts

```bash
src/routes/
├── +layout.svelte          # Shared layout
├── +page.svelte            # Home page (/)
├── login/
│   └── +page.svelte        # Login page (/login)
├── dashboard/
│   └── +page.svelte        # Dashboard page (/dashboard)
└── users/
    └── [id]/
        └── +page.svelte    # User detail page (/users/123)
```

## Component Structure

### Modern Svelte 5 Patterns

- Use runes (`$state`, `$derived`, `$effect`) instead of legacy patterns
- Avoid `onMount`, `onDestroy` - use `$effect` for lifecycle management
- Use `$props` for component props instead of `export let`

```svelte
<!-- ✅ CORRECT: Modern Svelte 5 component -->
<script>
	let { title, items } = $props();
	let count = $state(0);
	let filteredItems = $derived(items.filter((item) => item.active));

	$effect(() => {
		console.log('Component mounted or props changed');
	});
</script>

<h1>{title}</h1>
<p>Count: {count}</p>
<ul>
	{#each filteredItems as item}
		<li>{item.name}</li>
	{/each}
</ul>
```

### Component Naming

- Use PascalCase for component names
- Use descriptive names that indicate the component's purpose
- Keep components focused on a single responsibility

## Styling

### Scoped Styles

- Use component-level styles with `<style>` tags
- Leverage CSS custom properties for theming
- Use responsive design principles

```svelte
<style>
	.container {
		max-width: 1200px;
		margin: 0 auto;
		padding: 2rem;
	}

	.card {
		background: white;
		border-radius: 8px;
		box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.container {
			padding: 1rem;
		}
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
	}

	.button {
		background: var(--primary-color);
		color: white;
		border: 1px solid var(--primary-color);
	}
</style>
```

## Common Pitfalls

### 1. Reactivity Errors

**Problem**: "is updated, but is not declared with `$state(...)`"
**Solution**: Always use `$state()` for reactive variables

```svelte
<!-- ❌ WRONG -->
let count = 0;
function increment() {
  count++; // Error: not reactive
}

<!-- ✅ CORRECT -->
let count = $state(0);
function increment() {
  count++; // Works: reactive
}
```

### 2. Deprecated Event Syntax

**Problem**: "Using `on:submit` to listen to the submit event is deprecated"
**Solution**: Use standard HTML event attributes

```svelte
<!-- ❌ WRONG -->
<form on:submit|preventDefault={handleSubmit}>

<!-- ✅ CORRECT -->
<form onsubmit={handleSubmit}>
```

### 3. Navigation Issues

**Problem**: Buttons don't work or navigation fails
**Solution**: Use proper event handling and navigation patterns

```svelte
<!-- ✅ CORRECT: Button with onclick -->
<button onclick={handleLogin}>Login</button>

<!-- ✅ CORRECT: Form with onsubmit -->
<form onsubmit={handleSubmit}>
	<button type="submit">Submit</button>
</form>
```

### 4. State Management

**Problem**: State changes don't trigger re-renders
**Solution**: Use proper state management patterns

```svelte
<!-- ✅ CORRECT: Proper state management -->
let items = $state([]);
function addItem(item) {
  items = [...items, item]; // Creates new array, triggers reactivity
}

<!-- ❌ WRONG: Direct mutation -->
let items = $state([]);
function addItem(item) {
  items.push(item); // May not trigger reactivity properly
}
```

## Best Practices Summary

1. **Always use `$state()` for reactive variables**
2. **Use `onclick` for buttons, `onsubmit` for forms**
3. **Import navigation from `$app/navigation`**
4. **Use modern Svelte 5 patterns (runes)**
5. **Follow file-based routing conventions**
6. **Use scoped styles with CSS variables**
7. **Avoid deprecated syntax and patterns**
8. **Handle events with proper HTML attributes**

## Migration from Svelte 4

When migrating existing code:

1. Replace `let` with `$state()` for reactive variables
2. Replace `on:` event syntax with standard HTML attributes
3. Replace lifecycle functions with `$effect`
4. Replace `export let` with `$props()`
5. Update navigation imports to use `$app/navigation`

This ensures your codebase follows modern Svelte 5 best practices and avoids common pitfalls.
