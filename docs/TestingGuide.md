# KissOO Testing Guide

## Quick Reference

```bash
# Windows
.\bld.cmd unit-tests

# All tests via JAR
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

## Running Tests

### Prerequisites

- Java 17+ (tested with Java 25)
- Project cloned to any location

### Method 1: Using bld.cmd (Windows)

```powershell
# Run all tests
.\bld.cmd unit-tests

# Run specific test class
java -jar work/KissUnitTest.jar --select-class=oodb.PerstConfigTest
```

### Method 2: Using JAR directly (All Platforms)

```bash
# All tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb

# Perst tests only
java -jar work/KissUnitTest.jar --select-package=oodb

# Kiss web tests only
java -jar work/KissUnitTest.jar --select-package=org.kissweb

# Specific test class
java -jar work/KissUnitTest.jar --select-class=oodb.PerstConfigTest
```

## Test Results

- **Total**: 140 tests
- **Passing**: 139
- **Skipped**: 1 (IniFileTest - requires servlet context)

### New Perst Tests (20/20 Pass)

| Test Class | Tests | Description |
|------------|-------|-------------|
| `PerstConfigTest` | 5 | Singleton, defaults, dependency injection |
| `PerstContextIntegrationTest` | 15 | CRUD, transactions, versioning |

### Known Issues

- Some locale-dependent tests may fail (AM vs a.m., XML indentation)
- IniFileTest requires servlet context (skipped)

## Rebuilding Test JAR

If you modify test files:

```powershell
# Windows
.\bld.cmd unit-tests
```

## Adding New Tests

1. Place test files in `src/test/core/`:
   - Unit tests: `src/test/core/org/kissweb/`
   - Perst tests: `src/test/core/oodb/`

2. Use JUnit 5:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyTest {
    @Test
    void myTest() {
        assertTrue(true);
    }
}
```

3. Rebuild and run:

```bash
.\bld.cmd unit-tests
java -jar work/KissUnitTest.jar --select-class=oodb.MyTest
```

## Troubleshooting

### "Could not find or load main class Tasks"

Run `.\bld.cmd build` to compile Tasks.java first.

### Tests not found after modifying

Rebuild the test JAR: `.\bld.cmd unit-tests`

## See Also

- **Perst Integration**: [docs/PerstIntegration.md](docs/PerstIntegration.md)
- **Perst Usage**: [PERST_USAGE.md](PERST_USAGE.md)
- **Architecture**: [GETTING_STARTED.md](GETTING_STARTED.md)
