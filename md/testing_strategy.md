# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Testing Strategy - Perst OODBMS

## Overview

This document outlines the testing strategy for Perst, an embedded Object-Oriented Database for Java.

---

## Test Organization

### Test Directory Structure

```
src/test/java/org/garret/perst/
├── Core Tests
│   ├── StorageTest.java           - Storage interface tests
│   ├── StorageTestThreaded.java   - Concurrency tests
│   ├── DatabaseTest.java          - Database (SQL API) tests
│   ├── PersistentSetTest.java      - Set collection tests
│   ├── QueryTest.java             - JSQL query tests
│   └── BlobTest.java              - Binary large object tests
│
├── Index Tests
│   ├── TestIndex.java             - B-tree index tests
│   ├── TestIndex2.java            - Alternative B-tree tests
│   ├── TestBtreeCompoundIndex.java - Compound index tests
│   ├── TestRtree.java             - R-tree spatial tests
│   ├── TestKDTree.java            - KD-tree tests
│   ├── TestPatricia.java          - Patricia trie tests
│   └── TestBit.java               - Bitmap index tests
│
├── Feature Tests
│   ├── TestFullTextIndex.java     - Lucene integration
│   ├── TestBackup.java            - Backup/restore
│   ├── TestRollback.java          - Transaction rollback
│   ├── TestRecovery.java          - Crash recovery
│   ├── TestCodeGenerator.java     - Bytecode generation
│   └── TestRegex.java             - Regex indexing
│
└── Continuous Tests
    └── continuous/                 - Versioning tests
```

### Current Test Count
- **Total tests**: 58 test classes
- **Test methods**: ~300+ test cases
- **Framework**: JUnit 5 (migrated from JUnit 3/4)

---

## Running Tests

### Full Test Suite

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Skip specific tests
mvn test -Dtest='!TestServer,!TestDbServer'
```

### Run Specific Test Categories

```bash
# Core storage tests only
mvn test -Dtest='StorageTest,DatabaseTest'

# Index tests only
mvn test -Dtest='TestIndex*,TestBtree*'

# Single test class
mvn test -Dtest='StorageTest'
```

### Continuous Tests

```bash
# Located in src/test/java/org/garret/perst/continuous/
# Run with: mvn test -Dtest='*Continuous*'
```

---

## Test Categories

### 1. Unit Tests

**Purpose**: Test individual components in isolation

**Examples**:
- `TestBit.java` - Bitmap operations
- `TestAlloc.java` - Memory allocation
- `TestDecimal.java` - Decimal type handling

**Characteristics**:
- Fast execution
- No file I/O
- Mock dependencies where needed

### 2. Integration Tests

**Purpose**: Test component interactions

**Examples**:
- `StorageTest.java` - Storage interface implementation
- `DatabaseTest.java` - Database API
- `TestIndex*.java` - Index implementations

**Characteristics**:
- Use NullFile (in-memory)
- Test CRUD operations
- Verify relationships

### 3. Transaction Tests

**Purpose**: Verify ACID properties

**Examples**:
- `TestRollback.java` - Rollback behavior
- `TestRecovery.java` - Crash recovery
- `StorageTestThreaded.java` - Concurrent transactions

**Characteristics**:
- Test commit/rollback
- Test isolation levels
- Test recovery scenarios

### 4. Performance Tests

**Purpose**: Benchmark operations

**Examples**:
- `TestPerf.java` - Performance benchmarks

**Characteristics**:
- Measure execution time
- Test with various data sizes
- Not part of CI (run manually)

### 5. Concurrency Tests

**Purpose**: Test thread safety

**Examples**:
- `StorageTestThreaded.java`
- `TestConcur.java`

**Characteristics**:
- Multi-threaded operations
- Lock testing
- Race condition detection

---

## Test Infrastructure

### Test Base Classes

Tests typically extend no framework (JUnit 5). Legacy tests used:
- `junit.framework.TestCase` (JUnit 3)

### Test Fixtures

```java
// Common pattern
Storage storage;
Root root;

@BeforeEach
void setUp() {
    storage = StorageFactory.createStorage();
    storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
    root = new Root();
    storage.setRoot(root);
}

@AfterEach
void tearDown() {
    storage.close();
}
```

### NullFile Usage

For in-memory testing:
```java
storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
```

### Test Data Patterns

- Small datasets for correctness tests
- Large datasets for performance tests
- Edge cases (empty, null, boundary values)

---

## Coverage Goals

### Current Status
- **Instruction coverage**: ~40%
- **Target**: 70%+

### High Priority Coverage Areas
1. **Lock management** - Concurrency control
2. **Cache implementations** - Object cache, page pool
3. **File I/O** - All IFile implementations
4. **Error conditions** - Exception handling paths
5. **Edge cases** - Boundary conditions

### Package Coverage Goals

| Package | Current | Target |
|---------|---------|--------|
| `org.garret.perst` | ~50% | 70% |
| `org.garret.perst.impl` | ~35% | 65% |
| `org.garret.perst.fulltext` | ~40% | 70% |
| `org.garret.perst.continuous` | ~20% | 50% |

---

## Excluded Tests

Some tests are excluded from CI due to:

| Test | Reason |
|------|--------|
| `TestReplic` | Requires network setup |
| `TestReplic2` | Requires network setup |
| `TestSOD` | Interactive test |
| Shell-based tests | Run separately via scripts |

---

## CI Configuration

### Maven Surefire Configuration

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test*.java</include>
            <include>**/Test*.java</include>
        </includes>
        <excludes>
            <exclude>**/jassist/**</exclude>
            <exclude>**/android/**</exclude>
        </excludes>
        <systemProperties>
            <property>
                <name>java.awt.headless</name>
                <value>true</value>
            </property>
        </systemProperties>
    </configuration>
</plugin>
```

### Coverage Configuration

JaCoCo is configured for code coverage:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Test Best Practices

### Writing New Tests

1. **Use descriptive names**: `testBtreeInsertAndRetrieve()`
2. **Single responsibility**: One behavior per test
3. **Use @DisplayName** for complex test descriptions
4. **Group related tests** in the same class

### Test Structure

```java
@DisplayName("B-tree Index Tests")
class TestBtreeIndex {
    
    @Nested
    @DisplayName("Insert Operations")
    class InsertTests {
        @Test
        @DisplayName("Should insert and retrieve object by key")
        void testInsertAndRetrieve() {
            // Arrange
            Index index = storage.createIndex(String.class, false);
            
            // Act
            index.put(new Key("key1"), object);
            Object result = index.get(new Key("key1"));
            
            // Assert
            assertEquals(object, result);
        }
    }
}
```

### Assertions

- Use JUnit 5 assertions: `assertEquals()`, `assertThrows()`
- Include descriptive messages: `assertEquals(expected, actual, "message")`
- Test both positive and negative cases

---

## Known Test Issues

See [todo_investigate_tests.md](./todo_investigate_tests.md) for details on:

| Issue | Status | Workaround |
|-------|--------|------------|
| TestAlloc sequencing | Known | Run in order |
| TestLeak timeout | Known | Increase timeout |
| TestRaw constructor | Fixed | Add no-arg constructor |

---

## Test Maintenance

### When Adding New Features

1. Add corresponding tests in `src/test/java/org/garret/perst/`
2. Follow naming convention: `Test<Feature>.java`
3. Include tests in `All.java` if part of the test suite

### When Fixing Bugs

1. Add regression test in appropriate test class
2. Ensure test fails before fix (TDD)
3. Verify test passes after fix

### Test Review Checklist

- [ ] Tests cover success path
- [ ] Tests cover error paths
- [ ] Tests cover edge cases
- [ ] Tests are deterministic
- [ ] Tests clean up resources
- [ ] Coverage is adequate

---

## Related Documentation

- [Architecture](./architecture.md)
- [Increase Coverage Plan](./todo_increaseCoverage.md)
- [Investigate Tests](./todo_investigate_tests.md)
