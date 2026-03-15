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

### Core Perst Files

| File | Location | Description |
|------|----------|-------------|
| `PerstConfig.java` | `src/main/precompiled/oodb/` | Configuration singleton |
| `PerstContext.java` | `src/main/precompiled/oodb/` | Main database operations |
| `CDatabaseRoot.java` | `src/main/backend/domain/` | Root object for CDatabase versioning |

### Test Files

| File | Location | Type |
|------|----------|------|
| `PerstConfigTest.java` | `src/test/core/oodb/` | Unit tests for configuration |
| `PerstContextIntegrationTest.java` | `src/test/core/oodb/` | Integration tests for database operations |

## Running Tests

### Standard KissOO Way (Windows)

```powershell
# From project root
cd C:\opt\Projects\KissOO

# Build and run unit tests
.\bld.cmd unit-tests
```

### Alternative: Run Tests from JAR

If the build system has issues, run tests directly from the pre-built jar:

```bash
# Run all tests
java -jar work/KissUnitTest.jar

# Run only Perst tests
java -jar work/KissUnitTest.jar --select-package=oodb

# Run Perst and Kiss tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

```bash
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

### Run Only Perst Tests

```bash
java -jar work/KissUnitTest.jar --select-class=oodb.PerstConfigTest --select-class=oodb.PerstContextIntegrationTest
```

## Test Results

- **Total tests**: 140
- **Passing**: 129
- **Failing**: 11 (pre-existing locale/format issues in existing Kiss tests)

### New Perst Tests: 20/20 Pass

- `PerstConfigTest` (5 tests): Singleton, defaults, dependency injection
- `PerstContextIntegrationTest` (15 tests): CRUD, transactions, versioning

### Known Failing Tests (Pre-existing)

These failures are due to locale/format differences and are not related to Perst:
- `NumberUtilsTest`: 3 tests (decimal parsing)
- `XMLTest`: 3 tests (indentation)
- `DateTimeTest`: 4 tests (AM/PM format, timezone)
- `IniFileTest`: 1 test (null assertion)

## Perst Configuration

Perst is configured via `PerstConfig.java`:

```java
// Default values
perstEnabled = false        // Disabled by default
useCDatabase = true        // Use versioning-enabled database
databasePath = "oodb"      // Database file location
pagePoolSize = 512MB       // Memory cache size
```

To enable Perst, set `perstEnabled=true` in configuration or via `PerstConfig.setInstance()` for testing.

## Architecture

```
PerstConfig (singleton)
    └── PerstContext (singleton)
            ├── Storage / CDatabase (Perst database)
            └── PerstUser, Actor, Agreement, Group (domain objects)
```

## Notes

- **Log4j vs SLF4J**: Kiss uses Log4j 2 directly. Perst uses SLF4J. Both work together without conflict.
- **CDatabase**: The `perst-dcg-4.0.0.jar` includes CDatabase for versioning support.
- **Tests**: Integration tests use in-memory/temporary storage to avoid file system pollution.

## Troubleshooting

### Build Issues

If tests fail to build, run:
```powershell
.\bld.cmd unit-tests
```

This will rebuild the test JAR automatically.
