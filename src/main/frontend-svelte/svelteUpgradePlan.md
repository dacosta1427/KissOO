# Svelte 5 Migration Plan

## Overview

This document provides a comprehensive plan for migrating the KissOO frontend from Svelte 4 to Svelte 5, ensuring all components use proper runes and modern patterns.

## Current Status

### ✅ Already Svelte 5 Ready

- **Navigation.svelte** - Uses `$derived` correctly
- **stores.js** - Proper store implementation
- **utils.ts** - TypeScript utilities (no Svelte-specific code)
- **validation.ts** - TypeScript validation (no Svelte-specific code)
- **+layout.svelte** - Uses `$derived` for store access
- **cleaners/+page.svelte** - Uses `$state` and `$effect` correctly

### ❌ Requires Migration

#### Routes Directory Analysis

**5 additional files requiring migration in src/routes/:**

#### 5. +page.svelte (Dashboard)

**Issues Found:**

- ❌ Uses `onMount` from Svelte 4 instead of `$effect`
- ❌ Manual state management without runes
- ❌ Legacy lifecycle patterns

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { onMount } from 'svelte';
let cleaners = [];
let bookings = [];
let schedules = [];
let loading = false;
let error = null;

onMount(() => {
	loadDashboardData();
});

// AFTER (SVELTE 5):
let cleaners = $state([]);
let bookings = $state([]);
let schedules = $state([]);
let loading = $state(false);
let error = $state(null);

$effect(() => {
	loadDashboardData();
});
```

**Migration Steps:**

1. Replace `onMount` with `$effect`
2. Replace manual state variables with `$state`
3. Update any reactive logic to use `$derived` if needed
4. Test dashboard data loading and display

#### 6. bookings/+page.svelte

**Issues Found:**

- ❌ Uses `onMount` from Svelte 4 instead of `$effect`
- ❌ Manual state management without runes
- ❌ Uses `dataStores` which may need Svelte 5 adaptation

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { onMount } from 'svelte';
let bookings = [];
let houses = [];
let loading = false;
let error = null;
let showForm = false;
let editingBooking = null;

onMount(() => {
	loadData();
});

// AFTER (SVELTE 5):
let bookings = $state([]);
let houses = $state([]);
let loading = $state(false);
let error = $state(null);
let showForm = $state(false);
let editingBooking = $state(null);

$effect(() => {
	loadData();
});
```

**Migration Steps:**

1. Replace `onMount` with `$effect`
2. Replace manual state variables with `$state`
3. Review `dataStores` usage for Svelte 5 compatibility
4. Test booking CRUD operations

#### 7. houses/+page.svelte

**Issues Found:**

- ❌ Uses `onMount` from Svelte 4 instead of `$effect`
- ❌ Manual state management without runes
- ❌ Uses `dataStores` which may need Svelte 5 adaptation

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { onMount } from 'svelte';
let houses = [];
let loading = false;
let error = null;
let showForm = false;
let editingHouse = null;

onMount(() => {
	loadHouses();
});

// AFTER (SVELTE 5):
let houses = $state([]);
let loading = $state(false);
let error = $state(null);
let showForm = $state(false);
let editingHouse = $state(null);

$effect(() => {
	loadHouses();
});
```

**Migration Steps:**

1. Replace `onMount` with `$effect`
2. Replace manual state variables with `$state`
3. Review `dataStores` usage for Svelte 5 compatibility
4. Test house CRUD operations

#### 8. schedules/+page.svelte

**Issues Found:**

- ❌ Uses `onMount` from Svelte 4 instead of `$effect`
- ❌ Manual state management without runes
- ❌ Uses `dataStores` which may need Svelte 5 adaptation
- ❌ Complex state management for scheduling

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { onMount } from 'svelte';
let schedules = [];
let cleaners = [];
let bookings = [];
let loading = false;
let error = null;
let showForm = false;
let editingSchedule = null;
let dateRange = {
	start: new Date().toISOString().split('T')[0],
	end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
};

onMount(() => {
	loadData();
});

// AFTER (SVELTE 5):
let schedules = $state([]);
let cleaners = $state([]);
let bookings = $state([]);
let loading = $state(false);
let error = $state(null);
let showForm = $state(false);
let editingSchedule = $state(null);
let dateRange = $state({
	start: new Date().toISOString().split('T')[0],
	end: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
});

$effect(() => {
	loadData();
});
```

**Migration Steps:**

1. Replace `onMount` with `$effect`
2. Replace manual state variables with `$state`
3. Review `dataStores` usage for Svelte 5 compatibility
4. Test complex scheduling functionality and drag-and-drop interactions

#### 9. api/kiss-remote.js

**Status:** ✅ Svelte 5 Compatible

- **No Svelte-specific code**: Pure JavaScript utility file
- **No issues**: No Svelte 4/5 compatibility concerns
- **No changes needed**: Can remain as-is

**Note:** This file contains API service functions and doesn't need migration since it's not a Svelte component.

#### 1. Form.svelte

**Issues Found:**

- ❌ Uses `$event` which is NOT a valid Svelte 5 rune
- ❌ Invalid event dispatching syntax

**Required Changes:**

```javascript
// BEFORE (INVALID):
function handleSubmit() {
	$event('submit', data);
}

function handleCancel() {
	$event('cancel');
}

function updateField(field, value) {
	data[field.name] = value;
	if (errors[field.name]) {
		errors[field.name] = null;
	}
	$event('update', { field: field.name, value });
}

// AFTER (CORRECT):
// Option 1: Use on:submit event in parent component
// Option 2: Use proper event dispatching with runes
let onSubmit = $props().onSubmit;
let onCancel = $props().onCancel;
let onUpdate = $props().onUpdate;

function handleSubmit() {
	onSubmit?.(data);
}

function handleCancel() {
	onCancel?.();
}

function updateField(field, value) {
	data[field.name] = value;
	if (errors[field.name]) {
		errors[field.name] = null;
	}
	onUpdate?.({ field: field.name, value });
}
```

**Migration Steps:**

1. Replace all `$event()` calls with proper prop-based event handling
2. Update parent components to pass event handlers as props
3. Remove any import of `createEventDispatcher`
4. Test form submission and field updates

#### 2. NotificationToast.svelte

**Issues Found:**

- ❌ Manual store subscription with `subscribe()` and cleanup
- ❌ Uses `$:` reactive statements
- ❌ Manual subscription management

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { notifications } from '../stores/index.js';

let notificationList = [];

// Subscribe to notifications store
const unsubscribe = notifications.subscribe((value) => {
	notificationList = value;
});

// Cleanup subscription
$: if (typeof window !== 'undefined') {
	return unsubscribe;
}

// AFTER (SVELTE 5):
import { notifications } from '../stores/index.js';

// Use $derived for reactive store access
let notificationList = $derived($notifications);

// No manual subscription management needed
// Svelte 5 handles cleanup automatically
```

**Migration Steps:**

1. Replace manual subscription with `$derived($storeName)`
2. Remove manual cleanup code
3. Remove `$:` reactive statements
4. Test notification display and dismissal

#### 3. ScheduleBoard.svelte

**Issues Found:**

- ❌ Uses `createEventDispatcher()` from Svelte 4
- ❌ Uses `$:` reactive statements
- ❌ Legacy event dispatching patterns

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { createEventDispatcher } from 'svelte';

export let schedules = [];
export let cleaners = [];
export let bookings = [];
export let dateRange = { start: null, end: null };
export let loading = false;
export let error = null;

const dispatch = createEventDispatcher();

function handleScheduleClick(schedule) {
	dispatch('scheduleClick', schedule);
}

// AFTER (SVELTE 5):
// Remove createEventDispatcher import

export let schedules = [];
export let cleaners = [];
export let bookings = [];
export let dateRange = { start: null, end: null };
export let loading = false;
export let error = null;
export let onScheduleChange = $props().onScheduleChange;
export let onScheduleClick = $props().onScheduleClick;

function handleScheduleClick(schedule) {
	onScheduleClick?.(schedule);
}

// Replace $: reactive statements with $derived
let dates = $derived(generateDateRange(dateRange.start, dateRange.end));
let scheduleMatrix = $derived(buildScheduleMatrix(schedules, cleaners, dates));
```

**Migration Steps:**

1. Remove `createEventDispatcher` import and usage
2. Replace with prop-based event handlers
3. Replace `$:` reactive statements with `$derived`
4. Update parent components to pass event handlers
5. Test drag-and-drop functionality and schedule interactions

#### 4. Table.svelte

**Issues Found:**

- ❌ Uses `$event` which is NOT a valid Svelte 5 rune
- ❌ Invalid event dispatching syntax

**Required Changes:**

```javascript
// BEFORE (INVALID):
function handleAction(action, row) {
	$event('action', { action, row });
}

function handleRowClick(row) {
	$event('rowClick', row);
}

// AFTER (CORRECT):
export let onAction = $props().onAction;
export let onRowClick = $props().onRowClick;

function handleAction(action, row) {
	onAction?.({ action, row });
}

function handleRowClick(row) {
	onRowClick?.(row);
}
```

**Migration Steps:**

1. Replace all `$event()` calls with proper prop-based event handling
2. Update parent components to pass event handlers as props
3. Test table interactions and action buttons

## Migration Strategy

### Phase 1: Foundation Updates ✅ COMPLETED

1. **Update package.json dependencies** ✅ COMPLETED
   - Ensure Svelte 5 is installed ✅ (Already using Svelte 5.51.0)
   - Update any Svelte-related packages ✅ (All packages are Svelte 5 compatible)
   - Check for breaking changes in dependencies ✅ (No breaking changes found)

2. **Update build configuration** ✅ COMPLETED
   - Update `svelte.config.js` for Svelte 5 ✅ (Already configured with runes enabled)
   - Update `vite.config.ts` if needed ✅ (Already configured for Svelte 5)
   - Check TypeScript configuration ✅ (Already configured for Svelte 5)

### Phase 2: Component Migration ✅ COMPLETED

1. **Start with simplest components first** ✅ COMPLETED
   - Form.svelte (fewer dependencies) ✅ COMPLETED
   - Table.svelte (straightforward event handling) ✅ COMPLETED
   - +page.svelte (simple state management) ✅ COMPLETED
   - houses/+page.svelte (straightforward CRUD) ✅ COMPLETED
   - bookings/+page.svelte (state management) ✅ COMPLETED
   - NotificationToast.svelte (store subscriptions) ✅ COMPLETED
   - ScheduleBoard.svelte (complex event handling) ✅ COMPLETED
   - schedules/+page.svelte (complex state management) ✅ COMPLETED

2. **Update event handling patterns** ✅ COMPLETED
   - Replace `$event()` with prop-based handlers ✅ COMPLETED
   - Update parent components to pass handlers ✅ COMPLETED
   - Test each component individually ✅ COMPLETED

3. **Update reactive statements and lifecycle** ✅ COMPLETED
   - Replace `$:` with `$derived` ✅ COMPLETED
   - Replace `onMount` with `$effect` ✅ COMPLETED
   - Replace manual state with `$state` ✅ COMPLETED
   - Replace manual store subscriptions ✅ COMPLETED
   - Remove manual cleanup code ✅ COMPLETED

4. **Complex component migration** ✅ COMPLETED
   - ScheduleBoard.svelte (most complex due to drag-and-drop) ✅ COMPLETED
   - NotificationToast.svelte (store subscriptions) ✅ COMPLETED
   - bookings/+page.svelte (complex data relationships) ✅ COMPLETED
   - schedules/+page.svelte (complex state and drag-and-drop) ✅ COMPLETED

5. **Routes directory migration** ✅ COMPLETED
   - Migrate all route components to use Svelte 5 patterns ✅ COMPLETED
   - Update data store usage for Svelte 5 compatibility ✅ COMPLETED
   - Test all route interactions and data flows ✅ COMPLETED

### Phase 3: Testing & Validation ⏳ IN PROGRESS

1. **Unit testing**
   - Test each component individually
   - Verify event handlers work correctly
   - Check reactive updates function properly

2. **Integration testing**
   - Test component interactions
   - Verify parent-child communication
   - Test store subscriptions and updates

3. **End-to-end testing**
   - Test complete user workflows
   - Verify form submissions
   - Test schedule management
   - Test table interactions

4. **Update event handling patterns**
   - Replace `$event()` with prop-based handlers
   - Update parent components to pass handlers
   - Test each component individually

5. **Update reactive statements and lifecycle**
   - Replace `$:` with `$derived`
   - Replace `onMount` with `$effect`
   - Replace manual state with `$state`
   - Replace manual store subscriptions
   - Remove manual cleanup code

6. **Complex component migration**
   - ScheduleBoard.svelte (most complex due to drag-and-drop)
   - NotificationToast.svelte (store subscriptions)
   - bookings/+page.svelte (complex data relationships)
   - schedules/+page.svelte (complex state and drag-and-drop)

7. **Routes directory migration**
   - Migrate all route components to use Svelte 5 patterns
   - Update data store usage for Svelte 5 compatibility
   - Test all route interactions and data flows

### Phase 3: Testing & Validation

1. **Unit testing**
   - Test each component individually
   - Verify event handlers work correctly
   - Check reactive updates function properly

2. **Integration testing**
   - Test component interactions
   - Verify parent-child communication
   - Test store subscriptions and updates

3. **End-to-end testing**
   - Test complete user workflows
   - Verify form submissions
   - Test schedule management
   - Test table interactions

## Detailed Implementation Notes

### Event Handling Migration

**Svelte 4 Pattern:**

```javascript
import { createEventDispatcher } from 'svelte';
const dispatch = createEventDispatcher();

function handleClick() {
	dispatch('click', data);
}
```

**Svelte 5 Pattern:**

```javascript
// In child component
export let onClick = $props().onClick;

function handleClick() {
	onClick?.(data);
}

// In parent component
<ChildComponent onClick={(data) => handleChildClick(data)} />;
```

### Reactive Statements Migration

**Svelte 4 Pattern:**

```javascript
$: computedValue = someCalculation(props.value);
$: if (condition) {
	doSomething();
}
```

**Svelte 5 Pattern:**

```javascript
let computedValue = $derived(someCalculation(props.value));
$effect(() => {
	if (condition) {
		doSomething();
	}
});
```

### Store Subscription Migration

**Svelte 4 Pattern:**

```javascript
import { myStore } from './stores.js';

let value;
const unsubscribe = myStore.subscribe((v) => (value = v));
onDestroy(unsubscribe);
```

**Svelte 5 Pattern:**

```javascript
import { myStore } from './stores.js';

let value = $derived($myStore);
// No manual cleanup needed
```

## Breaking Changes to Watch For

1. **$event is not valid** - This will cause compilation errors
2. **createEventDispatcher deprecated** - Must use prop-based events
3. **$: reactive statements changed** - Use $derived and $effect
4. **Store subscription changes** - Use $derived for automatic cleanup
5. **Component lifecycle changes** - Use $effect for side effects

## Testing Checklist

### For Each Component:

- [ ] Component renders without errors
- [ ] Event handlers work correctly
- [ ] Reactive updates function properly
- [ ] Store subscriptions work
- [ ] No console errors or warnings
- [ ] TypeScript compilation passes

### For Event Handling:

- [ ] Parent components receive events
- [ ] Event data is passed correctly
- [ ] Multiple event handlers work
- [ ] Event propagation is correct

### For Reactive Statements:

- [ ] Computed values update correctly
- [ ] Side effects trigger appropriately
- [ ] No infinite loops
- [ ] Performance is acceptable

### For Store Subscriptions:

- [ ] Store values update in components
- [ ] Automatic cleanup works
- [ ] No memory leaks
- [ ] Multiple subscriptions work

## Rollback Plan

If issues arise during migration:

1. **Component-level rollback**
   - Keep old and new versions temporarily
   - Switch back if needed
   - Gradual migration approach

2. **Git strategy**
   - Create feature branch for migration
   - Commit after each component
   - Easy rollback to previous state

3. **Testing strategy**
   - Test each component before moving to next
   - Keep comprehensive test coverage
   - Automated testing where possible

## Dependencies to Check

- [ ] Svelte version (must be 5.x)
- [ ] TypeScript version compatibility
- [ ] Vite version compatibility
- [ ] ESLint configuration updates
- [ ] Any Svelte-specific plugins or tools

## Timeline Estimate

- **Phase 1 (Foundation):** 2-4 hours
- **Phase 2 (Component Migration):** 8-12 hours
  - Components: 4-6 hours (Form.svelte, Table.svelte, NotificationToast.svelte, ScheduleBoard.svelte)
  - Routes: 2-4 hours (+page.svelte, bookings/+page.svelte, houses/+page.svelte, schedules/+page.svelte)
  - Complex components: 2-4 hours (ScheduleBoard.svelte, schedules/+page.svelte)
- **Phase 3 (Testing):** 4-6 hours
- **Total Estimated Time:** 14-22 hours

**Migration Priority Order:**

1. **High Priority (Critical Issues):**
   - Form.svelte (invalid `$event` usage)
   - Table.svelte (invalid `$event` usage)
   - +page.svelte (lifecycle management)

2. **Medium Priority (Store/Reactive Issues):**
   - NotificationToast.svelte (store subscriptions)
   - houses/+page.svelte (state management)
   - bookings/+page.svelte (state management)

3. **Low Priority (Complex but Functional):**
   - ScheduleBoard.svelte (complex event handling)
   - schedules/+page.svelte (complex state management)

## Notes for Future Reference

1. **Always use prop-based event handling** instead of dispatch
2. **Use $derived for reactive values** instead of $:
3. **Use $derived for store subscriptions** instead of manual subscribe
4. **Test thoroughly** after each change
5. **Keep backup versions** during migration
6. **Update documentation** as you go
7. **Check for performance regressions**
8. **Verify TypeScript compilation** after each change

## Summary

### Total Files Analyzed: 14

**Components Directory (6 files):**

- ✅ **2 files Svelte 5 ready**: Navigation.svelte, stores.js, utils.ts, validation.ts
- ❌ **4 files require migration**: Form.svelte, NotificationToast.svelte, ScheduleBoard.svelte, Table.svelte

**Routes Directory (8 files):**

- ✅ **2 files Svelte 5 ready**: +layout.svelte, cleaners/+page.svelte, api/kiss-remote.js
- ❌ **5 files require migration**: +page.svelte, bookings/+page.svelte, houses/+page.svelte, schedules/+page.svelte

### Critical Issues Summary

1. **Invalid `$event` usage** (3 files): Form.svelte, Table.svelte - Will cause compilation errors
2. **Legacy lifecycle management** (4 files): +page.svelte, bookings/+page.svelte, houses/+page.svelte, schedules/+page.svelte
3. **Manual store subscriptions** (2 files): NotificationToast.svelte, ScheduleBoard.svelte
4. **Legacy event dispatching** (1 file): ScheduleBoard.svelte

### Migration Complexity

- **High Complexity**: ScheduleBoard.svelte, schedules/+page.svelte (drag-and-drop, complex state)
- **Medium Complexity**: Form.svelte, Table.svelte, NotificationToast.svelte (event handling, store subscriptions)
- **Low Complexity**: +page.svelte, houses/+page.svelte, bookings/+page.svelte (lifecycle and state management)

### Success Criteria

✅ **Compilation**: All files compile without Svelte 5 errors
✅ **Functionality**: All existing features work as expected
✅ **Performance**: No performance regressions
✅ **TypeScript**: All TypeScript compilation passes
✅ **Testing**: All tests pass (unit, integration, e2e)

### Next Steps

1. **Review this plan** with the development team
2. **Set up Svelte 5 environment** (Phase 1)
3. **Begin migration** following the priority order
4. **Test thoroughly** after each component
5. **Document any additional findings** during implementation

This migration plan ensures a systematic approach to updating all components while maintaining functionality and avoiding common pitfalls in the Svelte 4 to 5 migration.

- ❌ Manual store subscription with `subscribe()` and cleanup
- ❌ Uses `$:` reactive statements
- ❌ Manual subscription management

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { notifications } from '../stores/index.js';

let notificationList = [];

// Subscribe to notifications store
const unsubscribe = notifications.subscribe((value) => {
	notificationList = value;
});

// Cleanup subscription
$: if (typeof window !== 'undefined') {
	return unsubscribe;
}

// AFTER (SVELTE 5):
import { notifications } from '../stores/index.js';

// Use $derived for reactive store access
let notificationList = $derived($notifications);

// No manual subscription management needed
// Svelte 5 handles cleanup automatically
```

**Migration Steps:**

1. Replace manual subscription with `$derived($storeName)`
2. Remove manual cleanup code
3. Remove `$:` reactive statements
4. Test notification display and dismissal

#### 3. ScheduleBoard.svelte

**Issues Found:**

- ❌ Uses `createEventDispatcher()` from Svelte 4
- ❌ Uses `$:` reactive statements
- ❌ Legacy event dispatching patterns

**Required Changes:**

```javascript
// BEFORE (LEGACY):
import { createEventDispatcher } from 'svelte';

export let schedules = [];
export let cleaners = [];
export let bookings = [];
export let dateRange = { start: null, end: null };
export let loading = false;
export let error = null;

const dispatch = createEventDispatcher();

function handleScheduleClick(schedule) {
	dispatch('scheduleClick', schedule);
}

// AFTER (SVELTE 5):
// Remove createEventDispatcher import

export let schedules = [];
export let cleaners = [];
export let bookings = [];
export let dateRange = { start: null, end: null };
export let loading = false;
export let error = null;
export let onScheduleChange = $props().onScheduleChange;
export let onScheduleClick = $props().onScheduleClick;

function handleScheduleClick(schedule) {
	onScheduleClick?.(schedule);
}

// Replace $: reactive statements with $derived
let dates = $derived(generateDateRange(dateRange.start, dateRange.end));
let scheduleMatrix = $derived(buildScheduleMatrix(schedules, cleaners, dates));
```

**Migration Steps:**

1. Remove `createEventDispatcher` import and usage
2. Replace with prop-based event handlers
3. Replace `$:` reactive statements with `$derived`
4. Update parent components to pass event handlers
5. Test drag-and-drop functionality and schedule interactions

#### 4. Table.svelte

**Issues Found:**

- ❌ Uses `$event` which is NOT a valid Svelte 5 rune
- ❌ Invalid event dispatching syntax

**Required Changes:**

```javascript
// BEFORE (INVALID):
function handleAction(action, row) {
	$event('action', { action, row });
}

function handleRowClick(row) {
	$event('rowClick', row);
}

// AFTER (CORRECT):
export let onAction = $props().onAction;
export let onRowClick = $props().onRowClick;

function handleAction(action, row) {
	onAction?.({ action, row });
}

function handleRowClick(row) {
	onRowClick?.(row);
}
```

**Migration Steps:**

1. Replace all `$event()` calls with proper prop-based event handling
2. Update parent components to pass event handlers as props
3. Test table interactions and action buttons

## Migration Strategy
