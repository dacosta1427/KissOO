# EmailService Status - Java Only

## Current State: Java Implementation Only

The **Java version** (`src/main/backend/services/koo/auth/EmailService.java`) is the sole implementation.
The Groovy version has been removed.

## Implementation Details

All email sending functionality is now in `EmailService.java`:

- `send()` - Core email sending with TLS enforcement
- `sendVerification()` - Email verification links (called from AuthService.groovy)
- `sendPasswordReset()` - Password reset emails (called from Users.groovy)
- `sendLoginCredentials()` - Login credentials emails (called from Users.groovy & CleaningService.groovy)

## Security Features

1. **TLS Enforcement**: Raises `SecurityException` if SMTP auth enabled without TLS
2. **HTTPS Links**: All verification/password reset links use HTTPS
3. **No Sensitive Logging**: Credentials never logged
4. **Lazy Initialization**: Config loaded on first use
5. **Same Configuration**: Reads from `application.ini`

## Configuration

```ini
mail.smtp.host
mail.smtp.port
mail.smtp.auth
mail.smtp.username
mail.smtp.password
mail.smtp.starttls.enable
mail.from.address
mail.from.name
```

## Caller Updates

All Groovy files updated to call Java methods:
- `AuthService.groovy` → `EmailService.sendVerification()`
- `Users.groovy` → `EmailService.sendVerification()`, `EmailService.sendLoginCredentials()`
- `CleaningService.groovy` → `EmailService.sendLoginCredentials()`

## Migration Complete

All email functionality now in Java. No Groovy EmailService remains.
