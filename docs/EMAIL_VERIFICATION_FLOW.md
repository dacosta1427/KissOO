# Email Verification Flow for Owner Login Activation

## Overview

This document describes the proper email verification flow when enabling login for an owner (or any Actor). This is a critical security and UX requirement.

## The Problem

The current implementation of `toggleOwnerLogin` in `Cleaning.groovy` simply sets `user.active = true` without any email verification. This is **WRONG** because:

1. `PerstUser.canLogin()` requires BOTH: `active && emailVerified`
2. Users should set their OWN password, not have admin enable login without verification
3. The toggle UI indicates "Login enabled" but user cannot actually login

## Correct Flow

When admin toggles "Can log in" ON for an owner:

```
1. Admin toggles "Can log in" ON
   ↓
2. Backend generates unique verification token
   ↓
3. Backend sends verification email with link to owner's email
   ↓
4. User clicks email link → lands on /verify-email?token=xxx
   ↓
5. User enters their own password (self-service)
   ↓
6. Backend verifies token, sets:
   - user.emailVerified = true
   - user.active = true
   - user.password = [user-provided hashed password]
   ↓
7. User can now login with their email + password
```

## Key Principles

1. **Email verification is MANDATORY** - No one can login without verifying their email
2. **Self-service password** - Users set their own password during verification, not given by admin
3. **Token-based** - Each verification has a unique, time-limited token
4. **Two flags required** - Both `active` AND `emailVerified` must be true

## Implementation Notes

### Required Changes

1. **Cleaning.groovy - toggleOwnerLogin**:
   - Generate verification token
   - Send verification email via EmailService
   - Set `active = false` (not true!) until verified
   
2. **Users.groovy - verifyEmail**:
   - Currently only sets `emailVerified = true`
   - Should ALSO accept and set password from user

3. **EmailService**:
   - Initialize in KissInit.groovy from application.ini
   - Add "set password" email template for owner verification

### Configuration (application.ini)

```ini
mail.smtp.host=smtp.example.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.username=user@example.com
mail.smtp.password=secret
mail.from.address=noreply@example.com
mail.from.name=KissOO
```

### API Changes

**toggleOwnerLogin** (NEW behavior):
```json
// Input: { "id": <owner_oid>, "canLogin": true }
// Output: { "_Success": true, "message": "Verification email sent" }
```

- Does NOT enable login immediately
- Sends email with verification link
- User must verify email AND set password

**verifyEmail** (EXTENDED):
```json
// Input: { "token": "xxx", "password": "user-choice" }
// Output: { "_Success": true, "message": "Email verified, you can now login" }
```

- Verifies token
- Sets password provided by user
- Sets emailVerified = true
- Sets active = true (after password is set)

## Testing the Flow

```bash
# 1. Get session
UUID=$(curl -s -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"","_method":"Login","username":"admin","password":"admin"}' | jq -r '.uuid')

# 2. Toggle owner login ON
curl -s -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"services.Cleaning","_method":"toggleOwnerLogin","_uuid":"'$UUID'","id":<owner_oid>,"canLogin":true}'

# 3. Check email (if using Mailhog/dev SMTP)
# 4. Copy token from email
# 5. Verify with password
curl -s -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"services.Users","_method":"verifyEmail","token":"<token>","password":"mysecurepassword"}'

# 6. Login as owner
curl -s -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"","_method":"Login","username":"<owner_email>","password":"mysecurepassword"}'
```

## Related Files

- `src/main/backend/services/Cleaning.groovy` - toggleOwnerLogin
- `src/main/backend/services/Users.groovy` - verifyEmail, createUser
- `src/main/backend/services/EmailService.java` - Email sending
- `src/main/backend/KissInit.groovy` - Initialization
- `src/main/frontend-svelte/src/routes/verify-email/+page.svelte` - Verification page
- `src/main/backend/application.ini` - SMTP configuration

## References

- PerstUser.java: `canLogin()` requires `active && emailVerified`
- PerstUser.java: `verifyEmail()` and `verifyWithPassword()` methods
