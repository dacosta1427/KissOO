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

# Todolist: JUnit Migration - COMPLETED

## Tasks

### Priority #1: Core Test Infrastructure
- [x] Task 1.1: Migrate `junit_tests/src/org/garret/perst/All.java`
- [x] Task 1.2: Migrate `junit_tests/src/org/garret/perst/StorageTest.java`
- [x] Task 1.3: Migrate `junit_tests/src/org/garret/perst/StorageFactoryTest.java`
- [x] Task 1.4: Migrate `junit_tests/src/org/garret/perst/DatabaseTest.java`
- [x] Task 1.5: Migrate `junit_tests/src/org/garret/perst/PersistentSetTest.java`
- [x] Task 1.6: Migrate `junit_tests/src/org/garret/perst/QueryTest.java`
- [x] Task 1.7: Migrate `junit_tests/src/org/garret/perst/BlobTest.java`
- [x] Task 1.8: Migrate `junit_tests/src/org/garret/perst/StorageTestThreaded.java`

### Priority #2: Feature Tests (Batch 1)
- [x] Task 2.1: Migrate `junit_tests/src/org/garret/perst/TestIndex2.java`
- [x] Task 2.2: Migrate `junit_tests/src/org/garret/perst/TestBtreeCompoundIndex.java`
- [x] Task 2.3: Migrate `junit_tests/src/org/garret/perst/TestXML.java`
- [x] Task 2.4: Migrate `junit_tests/src/org/garret/perst/TestPerf.java`
- [x] Task 2.5: Migrate `junit_tests/src/org/garret/perst/TestMaxOid.java`
- [x] Task 2.6: Migrate `junit_tests/src/org/garret/perst/TestStorage.java`
- [x] Task 2.7: Migrate `junit_tests/src/org/garret/perst/TestLink.java`
- [x] Task 2.8: Migrate `junit_tests/src/org/garret/perst/TestRndIndex.java`
- [x] Task 2.9: Migrate `junit_tests/src/org/garret/perst/TestGC.java`
- [x] Task 2.10: Migrate `junit_tests/src/org/garret/perst/TestKDTree.java`

### Priority #3: Feature Tests (Batch 2)
- [x] Task 3.1: Migrate `junit_tests/src/org/garret/perst/TestThickIndex.java`
- [x] Task 3.2: Migrate `junit_tests/src/org/garret/perst/TestDynamicObjects.java`
- [x] Task 3.3: Migrate `junit_tests/src/org/garret/perst/TestConcur.java`
- [x] Task 3.4: Migrate `junit_tests/src/org/garret/perst/TestServer.java`
- [x] Task 3.5: Migrate `junit_tests/src/org/garret/perst/TestDbServer.java`
- [x] Task 3.6: Migrate `junit_tests/src/org/garret/perst/TestAlloc.java`
- [x] Task 3.7: Migrate `junit_tests/src/org/garret/perst/TestMap.java`
- [x] Task 3.8: Migrate `junit_tests/src/org/garret/perst/TestLeak.java`
- [x] Task 3.9: Migrate `junit_tests/src/org/garret/perst/TestVersion.java`
- [x] Task 3.10: Migrate `junit_tests/src/org/garret/perst/TestCompoundIndex.java`

### Priority #4: Feature Tests (Batch 3)
- [x] Task 4.1: Migrate `junit_tests/src/org/garret/perst/TestFullTextIndex.java`
- [x] Task 4.2: Migrate `junit_tests/src/org/garret/perst/TestBackup.java`
- [x] Task 4.3: Migrate `junit_tests/src/org/garret/perst/TestBit.java`
- [x] Task 4.4: Migrate `junit_tests/src/org/garret/perst/TestDecimal.java`
- [x] Task 4.5: Migrate `junit_tests/src/org/garret/perst/TestRollback.java`
- [x] Task 4.6: Migrate `junit_tests/src/org/garret/perst/TestMod.java`
- [x] Task 4.7: Migrate `junit_tests/src/org/garret/perst/TestJsqlJoin.java`
- [x] Task 4.8: Migrate `junit_tests/src/org/garret/perst/TestPatricia.java`
- [x] Task 4.9: Migrate `junit_tests/src/org/garret/perst/TestDerivedIndex.java`
- [x] Task 4.10: Migrate `junit_tests/src/org/garret/perst/TestRecovery.java`

### Priority #5: Feature Tests (Batch 4)
- [x] Task 5.1: Migrate `junit_tests/src/org/garret/perst/TestRtree.java`
- [x] Task 5.2: Migrate `junit_tests/src/org/garret/perst/TestSet.java`
- [x] Task 5.3: Migrate `junit_tests/src/org/garret/perst/TestRegex.java`
- [x] Task 5.4: Migrate `junit_tests/src/org/garret/perst/TestRandomBlob.java`
- [x] Task 5.5: Migrate `junit_tests/src/org/garret/perst/TestAutoIndices.java`
- [x] Task 5.6: Migrate `junit_tests/src/org/garret/perst/TestPersistentMap.java`
- [x] Task 5.7: Migrate `junit_tests/src/org/garret/perst/TestIndex.java`
- [x] Task 5.8: Migrate `junit_tests/src/org/garret/perst/TestList.java`
- [x] Task 5.9: Migrate `junit_tests/src/org/garret/perst/TestCodeGenerator.java`
- [x] Task 5.10: Migrate `junit_tests/src/org/garret/perst/TestBitmap.java`

### Priority #6: Feature Tests (Batch 5)
- [x] Task 6.1: Migrate `junit_tests/src/org/garret/perst/TestLoad.java`
- [x] Task 6.2: Migrate `junit_tests/src/org/garret/perst/TestJSQLContains.java`
- [x] Task 6.3: Migrate `junit_tests/src/org/garret/perst/TestWeakHashTable.java`
- [x] Task 6.4: Migrate `junit_tests/src/org/garret/perst/TestJSQL.java`
- [x] Task 6.5: Migrate `junit_tests/src/org/garret/perst/TestRaw.java`
- [x] Task 6.6: Migrate `junit_tests/src/org/garret/perst/TestKDTree2.java`
- [x] Task 6.7: Migrate `junit_tests/src/org/garret/perst/TestBlob.java`

### Priority #7: Cleanup & Verification
- [x] Task 7.1: Clean up `junit_tests/build.xml`
- [x] Task 7.2: Remove JUnit 3.8.1 jar
- [x] Task 7.3: Verify all tests pass

## Success Criteria
- [x] All JUnit 3/4 imports removed
- [x] All tests successfully compiled with JUnit 5
- [x] All tests pass when run

## Verification Results
- Tests run: 58 (via All.java suite)
- Failures: 0
- Errors: 0
- Skipped: 0

## Completion Date
2026-02-17
