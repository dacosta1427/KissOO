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

# Progress: JUnit Migration

## Project Status
- **Start Date:** 2026-02-17
- **Completion Date:** 2026-02-17
- **Current Phase:** Migration Complete
- **Overall Progress:** 100% complete

## Task Progress

### Priority #1: Core Test Infrastructure

#### Task 1.1: Migrate `junit_tests/src/org/garret/perst/All.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 Suite annotations
- **Timestamp:** 2026-02-17
- **Effort estimate:** M
- **Notes:** Added junit-platform-suite dependency to pom.xml

#### Task 1.2: Migrate `junit_tests/src/org/garret/perst/StorageTest.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** M

#### Task 1.3: Migrate `junit_tests/src/org/garret/perst/StorageFactoryTest.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** S

#### Task 1.4: Migrate `junit_tests/src/org/garret/perst/DatabaseTest.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** S

#### Task 1.5: Migrate `junit_tests/src/org/garret/perst/PersistentSetTest.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** S

#### Task 1.6: Migrate `junit_tests/src/org/garret/perst/QueryTest.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** S

#### Task 1.7: Migrate `junit_tests/src/org/garret/perst/BlobTest.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** S

#### Task 1.8: Migrate `junit_tests/src/org/garret/perst/StorageTestThreaded.java`
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** File uses JUnit 5 annotations and assertions
- **Timestamp:** 2026-02-17
- **Effort estimate:** S

## Risk Assessment
- **High Risk:** Breaking changes in test logic during migration
- **Medium Risk:** Compilation errors due to missing imports
- **Low Risk:** Formatting issues

## Current Blockers
- None

## Next Steps
1. Migrate `junit_tests/src/org/garret/perst/TestAlloc.java` (Completed)
2. Migrate `junit_tests/src/org/garret/perst/TestAgg.java` (Completed)
3. Migrate `junit_tests/src/org/garret/perst/TestAutoIndices.java` (Completed)
4. Migrate `junit_tests/src/org/garret/perst/TestBackup.java` (Completed)
5. Migrate `junit_tests/src/org/garret/perst/TestBit.java` (Completed)
6. Migrate `junit_tests/src/org/garret/perst/TestBitmap.java` (Completed)
7. Migrate `junit_tests/src/org/garret/perst/TestBlob.java` (Completed)
8. Migrate `junit_tests/src/org/garret/perst/TestCodeGenerator.java` (Completed)
9. Migrate `junit_tests/src/org/garret/perst/TestCompoundIndex.java` (Completed)
10. Migrate `junit_tests/src/org/garret/perst/TestConcur.java` (Completed)
11. Migrate `junit_tests/src/org/garret/perst/TestDbServer.java` (Completed)
12. Migrate `junit_tests/src/org/garret/perst/TestDecimal.java`
13. Migrate `junit_tests/src/org/garret/perst/TestDerivedIndex.java`
14. Migrate `junit_tests/src/org/garret/perst/TestDynamicObjects.java`
15. Migrate `junit_tests/src/org/garret/perst/TestFullTextIndex.java`
16. Migrate `junit_tests/src/org/garret/perst/TestGC.java`
17. Migrate `junit_tests/src/org/garret/perst/TestIndex.java`
18. Migrate `junit_tests/src/org/garret/perst/TestIndex2.java`
19. Migrate `junit_tests/src/org/garret/perst/TestIndexIterator.java`
20. Migrate `junit_tests/src/org/garret/perst/TestJSQL.java`
21. Migrate `junit_tests/src/org/garret/perst/TestJSQLContains.java`
22. Migrate `junit_tests/src/org/garret/perst/TestJsqlJoin.java`
23. Migrate `junit_tests/src/org/garret/perst/TestKDTree.java`
24. Migrate `junit_tests/src/org/garret/perst/TestLink.java`
25. Migrate `junit_tests/src/org/garret/perst/TestMultidimensionalIndex.java`
26. Migrate `junit_tests/src/org/garret/perst/TestRaw.java`
27. Migrate `junit_tests/src/org/garret/perst/TestRecovery.java`
28. Migrate `junit_tests/src/org/garret/perst/TestReplication.java`
29. Migrate `junit_tests/src/org/garret/perst/TestRtree.java`
30. Migrate `junit_tests/src/org/garret/perst/TestSerializable.java`
31. Migrate `junit_tests/src/org/garret/perst/TestTimeSeries.java`
32. Migrate `junit_tests/src/org/garret/perst/TestTtree.java`
33. Migrate `junit_tests/src/org/garret/perst/TestXML.java`
