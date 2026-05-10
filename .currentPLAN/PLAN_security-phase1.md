# Plan: Phase1 Core Security Fixes (Week 1)

## Objective
Implement Phase1 of the Superhardened Security Implementation Plan focusing on critical security fixes that can be completed in one week.

## Expected Behavior
After completion, the KissOO framework will have:
- Secure environment variable configuration with KOO_ prefix and VaultProvider interface
- NIST-compliant password strength validation with breach checking
- Fixed authentication bypass with explicit deny for unauthenticated access
- Password history prevention (last 12 passwords)
- Security event logging with append-only logs and SIEM hooks
- Standardized KISS error message usage (no HTTP status codes)

## Tasks

### Phase1.1: Environment Variable Configuration
- [ ] Create `koo/config/EnvironmentVariableConfig.java`
- [ ] Implement `getSecure()` method with KOO_ prefix
- [ ] Create generic `VaultProvider` interface (no default implementation)
- [ ] Update any existing config usage to use the new secure method

### Phase1.2: Password Strength Validation
- [ ] Modify `koo/security/PasswordSecurity.java`
- [ ] Implement password length validation (8-128 chars, configurable)
- [ ] Implement 3/4 character class requirements (uppercase, lowercase, digit, special)
- [ ] Add common password rejection
- [ ] Add HaveIBeenPwned k-anonymity checking (local cache option)
- [ ] Implement password history check (last 12 passwords, configurable)
- [ ] Add time-based reuse restriction (6 months default, configurable)

### Phase1.3: Fix Authentication Bypass
- [ ] Modify `koo/security/EndpointMethod.java`
- [ ] Add explicit deny for unauthenticated access (caller == null)
- [ ] Add IP-based restriction capability for sensitive endpoints
- [ ] Ensure UUID-based logging (not username) for security events
- [ ] Verify active check, temporal check, and agreement grants

### Phase1.4: Security Event Logging
- [ ] Create `koo/security/AuditLog.java`
- [ ] Implement append-only file logging
- [ ] Add SIEM webhook endpoint configuration hook
- [ ] Ensure no PII in logs (use UUID instead of username)
- [ ] Add security event types for monitoring

### Phase1.5: KISS Error Message Standardization
- [ ] Audit all service methods for proper error code usage
- [ ] Ensure only KISS framework error codes (0-5) are used
- [ ] Replace any HTTP status codes with KISS error codes
- [ ] Replace any hardcoded English messages with message keys
- [ ] Verify framework handles translation properly

### Phase1.6: Break-Glass Emergency Access
- [ ] Create `koo/security/BreakGlassAccess.java`
- [ ] Create `koo/security/BreakGlassService.java`
- [ ] Implement Vault-based token storage/retrieval
- [ ] Add mandatory incident reason field
- [ ] Implement 1-hour default expiry (configurable)
- [ ] Add full audit trail with SIEM alerts
- [ ] Update `EndpointMethod.execute()` to check for break-glass tokens

## Files to Modify/Create
- `koo/config/EnvironmentVariableConfig.java` (NEW)
- `koo/security/PasswordSecurity.java` (MODIFIED)
- `koo/security/EndpointMethod.java` (MODIFIED)
- `koo/security/AuditLog.java` (NEW)
- `koo/security/BreakGlassAccess.java` (NEW)
- `koo/security/BreakGlassService.java` (NEW)
- Various service files (for error code audit)

## Notes
- All security logging must use UUID instead of username for PII protection
- KISS error codes: 0=Success, 1=Invalid request, 2=Not found, 3=Access denied, 4=System error, 5=Validation error
- Environment variables use KOO_ prefix (not KISSOO_)
- VaultProvider interface should be generic with no default implementation
- Password history uses Perst CVersion mechanism (last 12 passwords by default)
- Break-glass access requires vault-retrieved token + incident reason + still needs authentication
