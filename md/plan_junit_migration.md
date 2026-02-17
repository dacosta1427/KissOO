# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---
Environment:
- Runs on Linux (*.sh scripts) and in Windows (*.cmd scripts)
- Java: OpenJDK 25
- Maven as a build tool, NO Gradle and NO script based compilation
- Maven should always work with the latest library versions
- Enable -Xlint:all -Werror (or at least during CI) to catch deprecations and preview API shifts.

Standard procedure:
After a change has been made to the core Perst code all the junit tests have to be run prior to stating the task as done.

### NEVER comment out or remove a line because that would make the code work. If you want to do this there has to be approval.

---

# JUnit 3/4 to JUnit 5 Migration Plan

## Overview
This plan outlines the steps to remove all JUnit 3/4 code from the `junit_tests` directory and replace it with JUnit 5 code.

## Files to Migrate
Based on the analysis of `junit_tests/src`, the following files contain JUnit 3/4 dependencies and need to be migrated. The `TestAgg.java` file appears to be already using JUnit 5.

### Priority 1: Core Test Infrastructure
- [ ] `junit_tests/src/org/garret/perst/All.java` (TestSuite) - Needs complete rewrite as JUnit 5 suite
- [ ] `junit_tests/src/org/garret/perst/StorageTest.java`
- [ ] `junit_tests/src/org/garret/perst/StorageFactoryTest.java`
- [ ] `junit_tests/src/org/garret/perst/DatabaseTest.java`
- [ ] `junit_tests/src/org/garret/perst/PersistentSetTest.java`
- [ ] `junit_tests/src/org/garret/perst/QueryTest.java`
- [ ] `junit_tests/src/org/garret/perst/BlobTest.java`
- [ ] `junit_tests/src/org/garret/perst/StorageTestThreaded.java`

### Priority 2: Feature Tests
These files likely extend `TestCase` or use `junit.framework.*` imports.

- [ ] `junit_tests/src/org/garret/perst/TestIndex2.java`
- [ ] `junit_tests/src/org/garret/perst/TestBtreeCompoundIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestXML.java`
- [ ] `junit_tests/src/org/garret/perst/TestPerf.java`
- [ ] `junit_tests/src/org/garret/perst/TestMaxOid.java`
- [ ] `junit_tests/src/org/garret/perst/TestStorage.java`
- [ ] `junit_tests/src/org/garret/perst/TestLink.java`
- [ ] `junit_tests/src/org/garret/perst/TestRndIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestGC.java`
- [ ] `junit_tests/src/org/garret/perst/TestKDTree.java`
- [ ] `junit_tests/src/org/garret/perst/TestThickIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestDynamicObjects.java`
- [ ] `junit_tests/src/org/garret/perst/TestConcur.java`
- [ ] `junit_tests/src/org/garret/perst/TestServer.java`
- [ ] `junit_tests/src/org/garret/perst/TestDbServer.java`
- [ ] `junit_tests/src/org/garret/perst/TestAlloc.java`
- [ ] `junit_tests/src/org/garret/perst/TestMap.java`
- [ ] `junit_tests/src/org/garret/perst/TestLeak.java`
- [ ] `junit_tests/src/org/garret/perst/TestVersion.java`
- [ ] `junit_tests/src/org/garret/perst/TestCompoundIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestFullTextIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestBackup.java`
- [ ] `junit_tests/src/org/garret/perst/TestBit.java`
- [ ] `junit_tests/src/org/garret/perst/TestDecimal.java`
- [ ] `junit_tests/src/org/garret/perst/TestRollback.java`
- [ ] `junit_tests/src/org/garret/perst/TestMod.java`
- [ ] `junit_tests/src/org/garret/perst/TestJsqlJoin.java`
- [ ] `junit_tests/src/org/garret/perst/TestPatricia.java`
- [ ] `junit_tests/src/org/garret/perst/TestDerivedIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestRecovery.java`
- [ ] `junit_tests/src/org/garret/perst/TestRtree.java`
- [ ] `junit_tests/src/org/garret/perst/TestSet.java`
- [ ] `junit_tests/src/org/garret/perst/TestRegex.java`
- [ ] `junit_tests/src/org/garret/perst/TestRandomBlob.java`
- [ ] `junit_tests/src/org/garret/perst/TestAutoIndices.java`
- [ ] `junit_tests/src/org/garret/perst/TestPersistentMap.java`
- [ ] `junit_tests/src/org/garret/perst/TestIndex.java`
- [ ] `junit_tests/src/org/garret/perst/TestList.java`
- [ ] `junit_tests/src/org/garret/perst/TestCodeGenerator.java`
- [ ] `junit_tests/src/org/garret/perst/TestBitmap.java`
- [ ] `junit_tests/src/org/garret/perst/TestLoad.java`
- [ ] `junit_tests/src/org/garret/perst/TestJSQLContains.java`
- [ ] `junit_tests/src/org/garret/perst/TestWeakHashTable.java`
- [ ] `junit_tests/src/org/garret/perst/TestJSQL.java`
- [ ] `junit_tests/src/org/garret/perst/TestRaw.java`
- [ ] `junit_tests/src/org/garret/perst/TestKDTree2.java`
- [ ] `junit_tests/src/org/garret/perst/TestBlob.java`

## Migration Strategy

### 1. Dependency Updates
- [ ] Update `junit_tests/build.xml` to remove JUnit 3.8.1 dependency (already seems to have JUnit 5 jars defined, need to ensure cleanup)
- [ ] Remove `junit_tests/lib/junit-3.8.1.jar` if present/unused.

### 2. Code Conversion Pattern

For each file, apply the following transformations:

1.  **Imports**:
    *   Remove `import junit.framework.*;`
    *   Remove `import junit.framework.TestCase;`
    *   Add `import org.junit.jupiter.api.Test;`
    *   Add `import org.junit.jupiter.api.BeforeEach;`
    *   Add `import org.junit.jupiter.api.AfterEach;`
    *   Add `import static org.junit.jupiter.api.Assertions.*;`

2.  **Class Definition**:
    *   Remove `extends TestCase`
    *   Make class package-private (optional but idiomatic in JUnit 5) or public.

3.  **Lifecycle Methods**:
    *   `setUp()` -> `@BeforeEach void setUp()`
    *   `tearDown()` -> `@AfterEach void tearDown()`
    *   Ensure they are not `protected` (package-private or public is fine).

4.  **Test Methods**:
    *   Annotate `test*` methods with `@Test`
    *   (Optional) Rename `testMethodName` to `methodName` (or keep as is)

5.  **Assertions**:
    *   `assertEquals(expected, actual)` -> `assertEquals(expected, actual)` (Same)
    *   `assertTrue(condition)` -> `assertTrue(condition)` (Same)
    *   `assertFalse(condition)` -> `assertFalse(condition)` (Same)
    *   `assertNull(object)` -> `assertNull(object)` (Same)
    *   `assertNotNull(object)` -> `assertNotNull(object)` (Same)
    *   `fail(message)` -> `fail(message)` (Same)

6.  **Suite Handling**:
    *   Remove `suite()` methods.
    *   Update `All.java` to use `@Suite` and `@SelectClasses` or `@SelectPackages` from JUnit 5 Platform Suite API.

### 3. Verification
- [ ] Run `ant test` (or `runtests.bat`/`runtests.sh`) after each batch of migrations to ensure tests still pass.
