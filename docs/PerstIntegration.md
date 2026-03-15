# Perst OODB Integration - KissOO Project

For comprehensive testing instructions, see [TestingGuide.md](TestingGuide.md).

## Overview

This document describes the Perst OODB (Object-Oriented Database) integration into the KissOO project.

## Dependencies Added

The following jars were added to `libs/`:
- `perst-dcg-4.0.0.jar` - Perst database with Continuous Development (CDatabase) support
- `slf4j-api-1.7.30.jar` - Logging facade (required by Perst)
- `slf4j-simple-1.7.30.jar` - SLF4J simple implementation

## Build System Configuration

### Tasks.java (`src/main/precompiled/Tasks.java`)

Two modifications were made to include the new dependencies:

1. **Local Dependencies** (line ~522):
```java
private static LocalDependencies buildLocalDependencies() {
    final LocalDependencies dep = new LocalDependencies();
    dep.add(LIBS, "abcl.jar");
    dep.add(LIBS, "perst-dcg-4.0.0.jar");  // Added
    dep.add(LIBS, "lombok.jar");
    dep.add(LIBS, "slf4j-api-1.7.30.jar");    // Added
    dep.add(LIBS, "slf4j-simple-1.7.30.jar"); // Added
    return dep;
}
```

2. **Unit Tests** (line ~197):
The `unitTests()` method was updated to include:
```java
// Perst DB and dependencies
unJar(workDir, "libs/perst-dcg-4.0.0.jar");
unJar(workDir, "libs/slf4j-api-1.7.30.jar");
unJar(workDir, "libs/slf4j-simple-1.7.30.jar");
unJar(workDir, "libs/jakarta.servlet-api-6.1.0.jar");
unJar(workDir, "libs/log4j-api-2.25.3.jar");
unJar(workDir, "libs/log4j-core-2.25.3.jar");
```

## Source Files

### Project Structure

```
src/main/
├── precompiled/
│   ├── oodb/           # Perst database layer
│   │   ├── PerstConfig.java
│   │   └── PerstContext.java
│   └── mycompany/
│       ├── domain/     # Domain entities (rarely changing)
│       │   ├── Actor.java
│       │   ├── Agreement.java
│       │   ├── Group.java
│       │   ├── PerstUser.java
│       │   ├── CRUD.java
│       │   ├── CDatabaseRoot.java
│       │   └── EndpointMethod.java
│       └── database/   # Manager classes
│           ├── ActorManager.java
│           ├── BaseManager.java
│           ├── PerstHelper.java
│           └── PerstUserManager.java
└── backend/
    └── services/       # REST services (frequently changing)
        └── ActorService.java
```

### Package Mapping

| Package | Location | Purpose |
|---------|----------|---------|
| `oodb` | `precompiled/oodb/` | Perst configuration and context |
| `mycompany.domain` | `precompiled/mycompany/domain/` | Domain entities |
| `mycompany.database` | `precompiled/mycompany/database/` | Manager classes |
| `services` | `backend/services/` | REST endpoints |

## Running Tests

### Standard KissOO Way (Windows)

```powershell
# From project root
cd C:\opt\Projects\KissOO

# Build and run unit tests
.\bld.cmd unit-tests
```

### Alternative: Run Tests from JAR

```bash
# Run all tests
java -jar work/KissUnitTest.jar

# Run only Perst tests
java -jar work/KissUnitTest.jar --select-package=oodb

# Run Perst and Kiss tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

## Test Results

- **Total tests**: 140
- **Passing**: 139
- **Skipped**: 1 (IniFileTest - requires servlet context)

### New Perst Tests: 20/20 Pass

- `PerstConfigTest` (5 tests): Singleton, defaults, dependency injection
- `PerstContextIntegrationTest` (15 tests): CRUD, transactions, versioning

## Perst Configuration

Perst is configured via `PerstConfig.java`:

```java
// Default values
perstEnabled = false        // Disabled by default
useCDatabase = true        // Use versioning-enabled database
databasePath = "oodb"      // Database file location
pagePoolSize = 512MB        // Memory cache size
```

To enable Perst, set `perstEnabled=true` in configuration or via `PerstConfig.setInstance()` for testing.

## Architecture

```
PerstConfig (singleton)
    └── PerstContext (singleton)
            ├── Storage / CDatabase (Perst database)
            └── mycompany.domain (Actor, Agreement, Group, PerstUser)
                    └── mycompany.database (ActorManager, PerstHelper)
```

## Usage Example

```java
// In a service (services.ActorService)
import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.database.ActorManager;
import mycompany.database.PerstHelper;

// Enable Perst for testing
PerstConfig.getInstance().setPerstEnabled(true);

// Get authenticated actor
UserData ud = servlet.getUserId();
Actor caller = ActorManager.getByUserId((int) ud.getUserId());

// Create new actor (Agreement checked automatically)
Actor newActor = ActorManager.create(caller, "John", "USER");
```

## Notes

- **Log4j vs SLF4J**: Kiss uses Log4j 2 directly. Perst uses SLF4J. Both work together without conflict.
- **CDatabase**: The `perst-dcg-4.0.0.jar` includes CDatabase for versioning support.
- **Tests**: Integration tests use in-memory/temporary storage to avoid file system pollution.
- **Package naming**: Use `mycompany.domain` and `mycompany.database` for all domain/manager code.
- **Only services go in backend/**: REST endpoints should be in `backend/services/`.
