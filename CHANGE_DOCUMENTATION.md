# Change Documentation: Auto-Endpoint Registration for Perst-Only Mode

## Summary
Implemented automatic endpoint registration with security controls for the Perst-only database mode, enabling proper access control between internal and external service endpoints.

## Files Changed in Core Framework

### 1. `src/main/core/org/kissweb/security/EXTERNAL_CALL.java` (NEW)
**Purpose**: Annotation for opt-in external access to service methods.

**Why**: In the new security model, external packages (non-internal) require explicit annotation to expose methods as external endpoints. This prevents accidental exposure of internal methods.

**Design Decision**: Placed in core framework since it's a fundamental security annotation used by the framework's endpoint registry.

### 2. `src/main/core/org/kissweb/security/EndpointMethodRegistry.java` (MODIFIED)
**Changes**:
- Added `isRegistered(String fullName)` method to check if an endpoint has been registered
- Added `isExternal(String fullName)` method to check if an endpoint is exposed externally
- Updated `registerServiceMethods()` to handle two different security models:
  - **Internal packages** (`internal.*`): Methods are internal by default; can opt-out via `@INTERNAL_CALL` annotation or `_` prefix
  - **External packages**: Methods are internal by default; must opt-in via `@EXTERNAL_CALL` annotation

**Why**: The original implementation treated all packages the same (methods were external unless annotated with `@INTERNAL_CALL`). The new model provides better security by default for external packages while maintaining backward compatibility for internal packages.

**Design Decision**: 
- Internal packages use "opt-out" model (backward compatible with existing internal services)
- External packages use "opt-in" model (more secure by default)
- Underscore prefix (`_methodName`) is treated as internal in both models

### 3. `src/main/core/org/kissweb/restServer/ProcessServlet.java` (MODIFIED)
**Changes**: 
- Added auto-registration check before processing endpoints (lines 471-480)
- Added debug logging for login flow (lines 939-979)
- Updated `newDatabaseConnection()` to handle Perst-only mode (lines 997-1017)

**Why**: Endpoints are now registered on-demand when first accessed, rather than pre-registering all methods. This improves startup time and allows for lazy loading of service classes.

**Design Decision**: Registration happens before the access check, ensuring all valid endpoints are registered before checking if they're external.

### 4. `src/main/core/org/kissweb/restServer/GroovyService.java` (MODIFIED)
**Changes**: Added `loadGroovyClassOnly(String _className)` method that:
- Loads a Groovy class
- Registers its service methods with `EndpointMethodRegistry`
- Returns true if successful, false otherwise

**Why**: Provides the mechanism for auto-registration of Groovy services.

### 5. `src/main/core/org/kissweb/restServer/JavaService.java` (MODIFIED)
**Changes**: Added `loadJavaClassOnly(String _className)` method that:
- Loads a Java class
- Registers its service methods with `EndpointMethodRegistry`
- Returns true if successful, false otherwise

**Why**: Provides the mechanism for auto-registration of Java services.

### 6. `src/main/core/org/kissweb/restServer/CompiledJavaService.java` (MODIFIED)
**Changes**: Added `loadCompiledJavaClassOnly(String _className)` method that:
- Attempts to load class from dynamic class path or standard location
- Registers its service methods with `EndpointMethodRegistry`
- Returns true if successful, false otherwise

**Why**: Provides the mechanism for auto-registration of compiled Java services.

## Files Changed in KissOO Framework

### `src/main/backend/services/CleaningService.groovy` (MODIFIED)
**Changes**: Added `@koo.security.EXTERNAL_CALL` annotation to all public service methods.

**Why**: This service is in the `services` package (external), so all methods need explicit opt-in to be exposed as endpoints.

### `src/main/backend/services/LoadTestdata.groovy` (MODIFIED)
**Changes**: Added `@koo.security.EXTERNAL_CALL` annotation to service methods.

**Why**: Same as above - external package requires explicit opt-in.

### `src/main/backend/services/Users.groovy` (MODIFIED)
**Changes**: Added `@koo.security.EXTERNAL_CALL` annotation to service methods.

**Why**: Same as above - external package requires explicit opt-in.

### `src/test/core/org/kissweb/security/EndpointMethodRegistryTest.java` (MODIFIED)
**Changes**: Updated tests to reflect new security model:
- External package methods now require `@EXTERNAL_CALL` to be exposed
- Added test for underscore prefix behavior
- Updated internal package test expectations

## Security Model Summary

| Package Type | Default Access | Opt-Out Mechanism | Opt-In Mechanism |
|-------------|---------------|-------------------|------------------|
| `internal.*` | Internal | N/A (not allowed) | `@EXTERNAL_CALL` |
| Others | External | `@INTERNAL_CALL` or `_` prefix | N/A (not needed) |

**Note**: Services in `backend/services`, `backend/services/domain`, `precompiled/services`, etc. are EXTERNAL by default. Only services in `backend/services/internal` or `precompiled/services/internal` are INTERNAL by default, and require `@EXTERNAL_CALL` annotation to be exposed externally.

## Backward Compatibility

- Internal packages maintain backward compatibility - methods without annotations remain external (same as before)
- External packages are more secure by default - methods must be explicitly marked as external
- The underscore prefix (`_`) provides a consistent way to mark methods as internal across all packages

## Performance Impact

- Minimal: Registration happens on first access, not at startup
- No impact on already-registered endpoints
- Lazy loading reduces initial memory footprint

## Current Issues to Address

### Login Endpoint Not Working
- Server returns static HTML instead of routing POST requests to backend
- Login method in ProcessServlet has debug logging that shows `requiresAuthentication = TRUE`
- Need to verify frontend is calling correct endpoint (`POST /rest` with empty `_class`)

### Frontend Changes Needed
- Remove redundant `_ownerId`/`_cleanerId` parameters from API calls
- Update services to use OO navigation via `PerstUser.getActor()`
- Update field naming to use OID suffix (e.g., `houseOid` not `houseId`)

### OO Navigation Pattern
- Use `((PerstUser) ud.getUserData("perstUser")).getActor()` to get actor from session
- Actor has direct references to related objects (no ID fields)
- All domain classes use Lombok `@Getter @Setter`