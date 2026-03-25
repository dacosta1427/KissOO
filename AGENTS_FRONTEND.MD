# Frontend Agent Guide - KissOO Svelte 5

## Quick Reference
- **Svelte 5 Runes**: `$state`, `$derived`, `$effect`, `$props`
- **Reactivity**: Use `$state` for reactive variables, `$derived` for computed values
- **Components**: Prefer Svelte 5 patterns, avoid Svelte 4 stores
- **Styling**: Tailwind CSS with consistent class patterns

## Debugging Protocols

### 1. Reactivity Issues (`$state`, `$derived`)
**Symptoms**: UI doesn't update, dropdowns empty, forms not reactive

**Checklist**:
1. Verify arrays use `$state` for reactivity
2. Check if mutating `const` arrays (won't trigger re-render)
3. Ensure `$derived` for computed options from arrays
4. Confirm proxy objects aren't interfering with rendering

**Common Fixes**:
```svelte
<!-- ❌ WRONG: const array not reactive -->
const options = $state([]);

<!-- ✅ CORRECT: Use $derived for computed options -->
let cleanOptions = $derived(sourceData.map(item => ({ 
  value: item.id.toString(), 
  label: item.name 
})));
```

### 2. Component Failures
**Symptoms**: Components not rendering, select dropdowns empty, modal issues

**Checklist**:
1. Check Svelte 5 binding syntax (`bind:value`)
2. Verify props are passed correctly with `$props()`
3. Test with native HTML elements first
4. Check console for proxy object warnings

**Common Fixes**:
- If custom Form.svelte select fails, use native `<select>`
- For modal issues, ensure proper `$state` bindings
- Replace Svelte 4 stores with `$state` patterns

### 3. API Communication Issues
**Symptoms**: Backend errors, data not loading, field name mismatches

**Checklist**:
1. Compare frontend expectations with backend responses
2. Check field names match exactly
3. Verify data types (select expects string values)
4. Ensure `_Success` field in backend responses

**Common Fixes**:
- Add TypeScript interfaces for API responses
- Use `getString()` not `optString()` in Groovy services
- Generate unique IDs for indexed fields

### 4. Session Management Bugs
**Symptoms**: Authentication failures after refresh, unexpected logouts

**Checklist**:
1. Verify `session.uuid` and `Server.uuid` are synchronized
2. Check session expiration handling (401 responses)
3. Ensure `Server.setUUID()` called after session restore

**Common Fixes**:
```javascript
// In +layout.svelte after session.restore():
if (session.uuid) {
  Server.setUUID(session.uuid);
}
```

### 5. Authentication Flow
**Symptoms**: Login fails with correct credentials, generic error messages

**Checklist**:
1. Check `emailVerified` flag in PerstUser
2. Differentiate between credential errors and unverified email
3. Verify signup flow auto-verification settings

## Non-Working Solutions Log

### Reactivity Anti-Patterns

| Issue Pattern | Failed Solution | Root Cause | Correct Solution | File References |
|---------------|-----------------|------------|------------------|-----------------|
| const arrays not reactive | `const scheduleFields = [...]` | const doesn't trigger Svelte 5 reactivity | Use `$state` for reactive arrays | `schedules/+page.svelte` |
| Mutating $state arrays inside $state objects | Mutated options array directly | Proxy reactivity limitations | Use `$derived` for computed options | `schedules/+page.svelte` |
| Option values as numbers | Number values in select options | HTML select expects string values | Convert all option values to strings | `schedules/+page.svelte` |
| Form data not updating | Direct assignment without reactivity | Missing `$state` wrapper | Wrap form data with `$state` | `users/+page.svelte` |

### Component Failures

| Issue Pattern | Failed Solution | Root Cause | Correct Solution | File References |
|---------------|-----------------|------------|------------------|-----------------|
| Form.svelte select not rendering | Custom Form select component | Proxy objects interfere with rendering | Use native HTML `<select>` elements | `schedules/+page.svelte` |
| Modal binding errors | Direct prop binding | Svelte 5 binding requirements | Use proper `$state` bindings | `GlobalModal.svelte` |
| Notification toasts not showing | Inline alert/confirm calls | Poor UX, blocking UI | Use notification store with toast component | `Utils.ts`, `NotificationToast.svelte` |

### API Communication Issues

| Issue Pattern | Failed Solution | Root Cause | Correct Solution | File References |
|---------------|-----------------|------------|------------------|-----------------|
| Backend JSON method errors | Used `optString`/`optBoolean` | Methods don't exist in JSONObject | Use `getString`/`getBoolean` | `Cleaning.groovy` |
| Optional JSON methods missing | Used `optLong` with default | JSONObject may not have optLong with default | Use `has()` + `getLong()` or default value | `Cleaning.groovy` |
| Field name mismatches | Expected `assigned_date`/`house_name` | Backend returns `date`/`guest_name` | Align field names between frontend/backend | `ScheduleBoard.svelte` |
| Unique constraint violations | `userId = 0` for all users | Unique constraint on `userId` field | Generate unique `userId` (max+1) | `Users.groovy` |
| Missing `_Success` field | Inconsistent backend responses | All services must return `_Success` | Ensure all service methods set `_Success` | All service files |

### Session Management Bugs

| Issue Pattern | Failed Solution | Root Cause | Correct Solution | File References |
|---------------|-----------------|------------|------------------|-----------------|
| UUID not synchronized | `session.restore()` updates `session.uuid` only | `Server.uuid` remains empty | Call `Server.setUUID(session.uuid)` after restore | `+layout.svelte` |
| Session expiry redirect | Redirect to `/login?expired=true` | Should redirect to home screen | Redirect to `'/'` and show "Logged out" indicator | `Server.ts`, `Navbar.svelte` |
| Encrypted credentials not working | Missing Web Crypto API implementation | Need proper AES-GCM encryption | Implement with PBKDF2 key derivation | `session.svelte.ts` |

### Authentication Flow

| Issue Pattern | Failed Solution | Root Cause | Correct Solution | File References |
|---------------|-----------------|------------|------------------|-----------------|
| Login fails with correct credentials | Didn't check `emailVerified` flag | `PerstUser.canLogin()` requires `emailVerified = true` | Auto-verify users or add verification flow | `PerstUser.java:141-143` |
| Generic error messages | Same error for wrong password vs unverified email | Frontend doesn't distinguish error types | Add specific error handling for different cases | `Auth.ts`, `login/+page.svelte` |
| Signup succeeds but login fails | No auto-verification after signup | New users have `emailVerified = false` | Add email verification workflow or auto-verify | `Auth.ts`, `Users.groovy` |

## Template for Future Documentation

```markdown
### Issue: [Brief Description]
**Date**: YYYY-MM-DD  
**Severity**: High/Medium/Low  
**Component**: [File/Component name]

#### Problem Description
[What was happening - include error messages, symptoms]

#### Failed Solution Attempted
[What was tried and didn't work - include code snippets if relevant]

#### Root Cause
[Why it failed - be specific about the technical reason]

#### Correct Solution
[What worked - include exact changes made]

#### File References
- `path/to/file.js:45-67` - specific lines
- `src/components/Component.svelte` - entire component

#### Prevention Tips
[How to avoid this in future development]
```

## Common Pitfalls & Lessons

1. **Always test backend API independently** (curl/postman) before frontend changes
2. **Check unique constraints** when creating new records (especially indexed fields)
3. **Verify response format** includes `_Success` field for all backend services
4. **Test both success and failure paths** - not just happy path
5. **Verify hot reload actually works** after Groovy service changes
6. **Use Svelte 5 runes consistently** - mixing with Svelte 4 patterns causes issues
7. **Convert option values to strings** for HTML select elements
8. **Document non-working solutions immediately** to prevent repetition

## Protocol Compliance Checklist

- [ ] Update this guide after significant frontend changes
- [ ] Document new non-working solutions as they're discovered
- [ ] Keep troubleshooting flowcharts current
- [ ] Reference specific file paths and line numbers
- [ ] Update CLEANERS2_INTEGRATION_PLAN.md within 4 hours of task completion
- [ ] Run `npm run build` to catch TypeScript errors
- [ ] Test with backend server running on port 8080
- [ ] Check browser console for API errors

## Quick Debugging Commands

```bash
# Frontend development
cd src/main/frontend-svelte && npm run dev

# Build check (TypeScript errors)
cd src/main/frontend-svelte && npm run build

# Backend test
./test-backend.sh

# Check backend logs
tail -f work/exploded/WEB-INF/logs/console.log
```

## Svelte 5 Patterns Reference

```svelte
<script lang="ts">
  // ✅ Reactive state
  let count = $state(0);
  let user = $state({ name: '', email: '' });
  let items = $state<string[]>([]);

  // ✅ Derived values
  let isValid = $derived(user.name.length > 0 && user.email.includes('@'));
  let itemCount = $derived(items.length);

  // ✅ Effects
  $effect(() => {
    console.log('Count changed:', count);
  });

  // ✅ Props
  interface Props {
    title: string;
    onSubmit: (data: any) => void;
  }
  let { title, onSubmit }: Props = $props();
</script>
```

## API Integration Pattern

```typescript
// src/lib/api/Example.ts
import { Server } from '$lib/services/Server';

export interface Example {
  id: number;
  name: string;
}

export async function getExamples(): Promise<Example[]> {
  const res = await Server.call('services.Example', 'getAll', {});
  return res.rows || [];
}

export async function createExample(data: Partial<Example>): Promise<boolean> {
  const res = await Server.call('services.Example', 'create', data);
  return res._Success || false;
}
```

---

**Last Updated**: 2026-03-25  
**Next Review**: After completing Week 2 tasks  
**Protocol**: Update within 4 hours of significant frontend changes