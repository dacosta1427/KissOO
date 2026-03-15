# KissOO Testing Guide

## Running Tests - The Correct Way

### Prerequisites

This guide assumes:
- Windows environment
- Java 25
- Project at `C:\opt\Projects\KissOO`

### The Build System

KissOO uses a custom build system with:
- `bld.cmd` - Windows build script (compiles Tasks.java automatically)
- `Tasks.java` - Build tasks (build, unit-tests, etc.)
- `work/KissUnitTest.jar` - Pre-built test runner JAR

### Important Notes

1. **NEVER use `ant build` or `ant unit-tests`** - These fail because the Windows classpath separator (`;`) is not handled correctly by Ant's exec task.

2. **The `bld.cmd` script** - This is the correct way to build. It:
   - Compiles Tasks.java if needed (checks timestamps)
   - Uses correct Windows classpath with `;` separator
   - Works via PowerShell: `.\bld.cmd <task>`

3. **Running tests via `bld.cmd`** - This builds the test JAR but output may not be visible in all environments.

4. **The reliable alternative** - Run tests directly from the pre-built JAR:
   ```bash
   java -jar work/KissUnitTest.jar
   ```

### Step-by-Step Commands

#### Method 1: Using bld.cmd (may have output issues)

```powershell
# Navigate to project
cd C:\opt\Projects\KissOO

# Build the project
.\bld.cmd build

# Run unit tests
.\bld.cmd unit-tests
```

#### Method 2: Using pre-built JAR (recommended, reliable)

```powershell
cd C:\opt\Projects\KissOO

# Run all tests
java -jar work/KissUnitTest.jar

# Run specific packages
java -jar work/KissUnitTest.jar --select-package=oodb
java -jar work/KissUnitTest.jar --select-package=org.kissweb

# Run both Perst and Kiss tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb

# Run specific test class
java -jar work/KissUnitTest.jar --select-class=oodb.PerstConfigTest
```

### Rebuilding the Test JAR

If you modify test files and need to rebuild:

```powershell
# This rebuilds the test JAR
.\bld.cmd unit-tests
```

Or manually:

```bash
java -cp "work/exploded/WEB-INF/classes;libs/commons-compress-1.27.1.jar;libs/commons-io-2.16.1.jar;libs/commons-lang3-3.18.0.jar" Tasks unitTests
```

### Troubleshooting

#### "Could not find or load main class Tasks"

Tasks.java hasn't been compiled. Run:
```powershell
.\bld.cmd build
```

The `bld.cmd` script automatically compiles Tasks.java if needed.

#### Tests not found after modifying test files

Rebuild the test JAR:
```powershell
.\bld.cmd unit-tests
```

#### Locale-dependent test failures

Some tests fail due to locale differences (e.g., "AM" vs "a.m.", XML indentation). These are pre-existing issues not related to new code.

### Current Test Status

- **Total**: 140 tests
- **Passing**: 139
- **Skipped**: 1 (IniFileTest.testSaveAndLoadIniFile - requires servlet context)

### Adding New Tests

1. Place test files in `src/test/core/`:
   - Unit tests: `src/test/core/org/kissweb/`
   - Perst tests: `src/test/core/oodb/`

2. Use JUnit 5 annotations:
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

3. Rebuild test JAR: `.\bld.cmd unit-tests`

4. Run tests: `java -jar work/KissUnitTest.jar --select-class=oodb.MyTest`
