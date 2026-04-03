# Owner Login Verification Flow - Implementation Plan

## Overview

This document describes the complete implementation of the email verification flow when enabling login for an owner. The flow ensures users verify their email AND set their own password before they can login.

## Current Problem

- `toggleOwnerLogin` in `Cleaning.groovy` just sets `active = true`
- `PerstUser.canLogin()` requires both `active && emailVerified`
- Owner cannot login even after toggle shows "Login enabled"
- No actual email verification is sent

## Solution Architecture

### Complete Flow

```
1. Admin toggles "Can log in" ON in UI
   │
   ▼
2. Backend generates verification token
   │
   ▼
3. Backend sends verification email with link
   │   Link: http://localhost:5173/verify-email?token=xxx
   │
   ▼
4. Owner clicks link → /verify-email page loads
   │
   ▼
5. Owner enters password + confirm password
   │
   ▼
6. Frontend calls verifyEmail API with token + password
   │
   ▼
7. Backend:
   - Verifies token is valid
   - Hashes and sets password
   - Sets emailVerified = true
   - Sets active = true
   │
   ▼
8. Success → Owner can now login with email + password
```

## Files to Modify

### 1. application.ini
Add base URL configuration:
```ini
# Frontend base URL for email verification links
app.baseUrl = http://localhost:5173
```

### 2. Cleaning.groovy - toggleOwnerLogin()
**Current behavior:** Sets active=true, returns success

**New behavior when enabling login:**
- Generate verification token via user.generateVerificationToken()
- Send verification email via EmailService.sendVerificationEmail()
- DO NOT set active=true yet
- Return: "Verification email sent to {email}"

**When disabling login:**
- Set active=false immediately
- Return: "Owner login deactivated"

### 3. Users.groovy - verifyEmail()
**Current behavior:** Verifies token, sets emailVerified=true

**New behavior:**
- Accept optional `password` field in input JSON
- If password provided:
  - Hash password using PasswordSecurity
  - Set active=true after verification succeeds
- Return appropriate message

**Input:**
```json
{ "token": "xxx", "password": "user-chosen-password" }
```

**Output:**
```json
{ "_Success": true, "message": "Email verified. You can now login." }
```

### 4. verify-email/+page.svelte
**Current behavior:** Auto-verifies on page load

**New behavior:**
- Show password input form
- Validate password requirements
- On submit: call verifyEmail with token + password
- On success: show success message with "Go to Login" button

**UI Requirements:**
- Password input field
- Confirm password field
- Password strength indicator (optional)
- Submit button
- "Already verified? Login" link

## API Endpoints

### toggleOwnerLogin
```
POST services.Cleaning.toggleOwnerLogin
Input:  { "id": <owner_oid>, "canLogin": true }
Output: { "_Success": true, "message": "Verification email sent to owner@email.com" }
```

### verifyEmail
```
POST services.Users.verifyEmail
Input:  { "token": "xxx", "password": "myPassword123" }
Output: { "_Success": true, "message": "Email verified. You can now login." }
```

## Configuration

### application.ini additions
```ini
# Frontend base URL for email verification links
app.baseUrl = http://localhost:5173
```

### EmailService usage
```groovy
EmailService.sendVerificationEmail(
    owner.getEmail(),      // recipient
    owner.getName(),      // recipient name
    user.getVerificationToken(), // verification token
    baseUrl               // http://localhost:5173
)
```

## Testing Checklist

- [ ] Restart backend, verify EmailService initializes
- [ ] Toggle owner login ON via UI
- [ ] Check Mailhog for verification email (http://localhost:8025)
- [ ] Click verification link
- [ ] Enter password on verify-email page
- [ ] Submit form
- [ ] Verify success message shown
- [ ] Attempt login with email + new password
- [ ] Verify login succeeds

## Edge Cases

1. **Owner has no email** - Return error "Owner has no email address"
2. **Verification token expired** - Show error "Verification link expired. Contact admin."
3. **Password too short** - Frontend validation, show error
4. **Passwords don't match** - Frontend validation, show error
5. **Toggle OFF** - Immediately disable, no email needed

## Related Files

- `src/main/backend/application.ini` - Add baseUrl config
- `src/main/backend/services/Cleaning.groovy` - toggleOwnerLogin method
- `src/main/backend/services/Users.groovy` - verifyEmail method  
- `src/main/backend/services/EmailService.java` - Email sending (exists)
- `src/main/frontend-svelte/src/routes/verify-email/+page.svelte` - Verification page
- `src/main/precompiled/mycompany/domain/PerstUser.java` - Token methods
- `src/main/backend/KissInit.groovy` - EmailService initialization (configured)

## References

- PerstUser.java: `generateVerificationToken()`, `verifyEmail()`, `canLogin()`
- docs/EMAIL_VERIFICATION_FLOW.md - Original flow documentation
