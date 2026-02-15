# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Progress: JUnit Test Conversion - Phase 2

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** In Progress
- **Overall Progress:** 71% complete (27/38 tasks)
- **Test Count:** 252 tests (increased from 148)

## Task Progress

### Priority #1: Core Tests

#### Task 1: Convert TestAgg
- **Status:** pending
- **Priority:** High
- **Dependencies:** data.csv file
- **Owner:** Cline
- **Success criteria:** TestAgg converted to JUnit 5, all assertions pass
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Aggregation functions - requires data.csv file, complex test

#### Task 2: Convert TestCompoundIndex
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestCompoundIndex converted to JUnit 5
- **Timestamp:** 2026-02-14 23:09
- **Effort estimate:** M
- **Notes:** Compound index support - 4 tests added

#### Task 3: Convert TestIndexIterator
- **Status:** skipped
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** N/A - functionality already covered in TestIndex.java
- **Timestamp:** 2026-02-14 23:10
- **Effort estimate:** M
- **Notes:** Index iterator functionality already exists in TestIndex.java

### Priority #2: Data Types

#### Task 4: Convert TestBit
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestBit converted to JUnit 5
- **Timestamp:** 2026-02-14 23:11
- **Effort estimate:** S
- **Notes:** Bit operations - 2 tests added

#### Task 5: Convert TestBitmap
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestBitmap converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** S
- **Notes:** Bitmap index - 5 tests added

#### Task 6: Convert TestDecimal
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestDecimal converted to JUnit 5
- **Timestamp:** 2026-02-14 23:15
- **Effort estimate:** M
- **Notes:** Decimal field type - 6 tests added

#### Task 7: Convert TestLink
- **Status:** skipped
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestLink converted to JUnit 5
- **Timestamp:** 2026-02-14 23:24
- **Effort estimate:** M
- **Notes:** Linked list - Skipped, it's an interactive demo app (menu-driven)

#### Task 8: Convert TestRaw
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRaw converted to JUnit 5
- **Timestamp:** 2026-02-14 23:24
- **Effort estimate:** M
- **Notes:** Raw data access - 3 tests added

### Priority #3: Advanced Indexing

#### Task 9: Convert TestRtree
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRtree converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** M
- **Notes:** R-tree spatial index - 4 tests added

#### Task 10: Convert TestKDTree
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestKDTree converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** M
- **Notes:** K-D tree index - 6 tests added

#### Task 11: Convert TestKDTree2
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestKDTree2 converted to JUnit 5
- **Timestamp:** 2026-02-15 11:16
- **Effort estimate:** M
- **Notes:** K-D tree variant - 6 tests added

#### Task 12: Convert TestPatricia
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestPatricia converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** M
- **Notes:** Patricia trie - 5 tests added

#### Task 13: Convert TestTtree
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestTtree converted to JUnit 5
- **Timestamp:** 2026-02-15 10:51
- **Effort estimate:** M
- **Notes:** T-tree index - 5 tests added

#### Task 14: Convert TestThickIndex
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestThickIndex converted to JUnit 5
- **Timestamp:** 2026-02-15 10:52
- **Effort estimate:** M
- **Notes:** Thick index - 5 tests added

#### Task 15: Convert TestRndIndex
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRndIndex converted to JUnit 5
- **Timestamp:** 2026-02-15 10:53
- **Effort estimate:** M
- **Notes:** Randomized index - 6 tests added

#### Task 16: Convert TestDerivedIndex
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestDerivedIndex converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** M
- **Notes:** Derived indices - 3 tests added

### Priority #4: Query & Search

#### Task 17: Convert TestJSQLContains
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestJSQLContains converted to JUnit 5
- **Timestamp:** 2026-02-15 11:16
- **Effort estimate:** M
- **Notes:** JSQL CONTAINS operator - 3 tests added

#### Task 18: Convert TestJsqlJoin
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestJsqlJoin converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** M
- **Notes:** JSQL JOIN queries - 7 tests added

#### Task 19: Convert TestRegex
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRegex converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** S
- **Notes:** Regular expression search - 5 tests added

### Priority #5: Transactions

#### Task 20: Convert TestRollback
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRollback converted to JUnit 5
- **Timestamp:** 2026-02-15 11:21
- **Effort estimate:** M
- **Notes:** Transaction rollback - 2 tests added

#### Task 21: Convert TestRecovery
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRecovery converted to JUnit 5
- **Timestamp:** 2026-02-15 12:18
- **Effort estimate:** M
- **Notes:** Database recovery - 4 tests added

#### Task 22: Convert TestConcur
- **Status:** completed
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestConcur converted to JUnit 5
- **Timestamp:** 2026-02-15 12:55
- **Effort estimate:** L
- **Notes:** Concurrency testing - 4 tests added

### Priority #6: Replication & Server

#### Task 23: Convert TestReplic
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Network setup (master/slave)
- **Owner:** Cline
- **Success criteria:** TestReplic converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Database replication - requires complex network setup

#### Task 24: Convert TestReplic2
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Network setup (master/slave)
- **Owner:** Cline
- **Success criteria:** TestReplic2 converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Replication variant - requires complex network setup

#### Task 25: Convert TestDbServer
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Database server setup
- **Owner:** Cline
- **Success criteria:** TestDbServer converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Database server - requires server setup

#### Task 26: Convert TestServer
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestServer converted to JUnit 5
- **Timestamp:** 2026-02-15 16:07
- **Effort estimate:** L
- **Notes:** Multi-threaded server operations - 2 tests added (testServerMultiThreaded, testServerIndexOperations)

### Priority #7: Other Features

#### Task 27: Convert TestAlloc
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestAlloc converted to JUnit 5
- **Timestamp:** 2026-02-15 10:50
- **Effort estimate:** S
- **Notes:** Memory allocation - 4 tests added

#### Task 28: Convert TestLeak
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestLeak converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Memory leak detection

#### Task 29: Convert TestLoad
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestLoad converted to JUnit 5
- **Timestamp:** 2026-02-15 12:00
- **Effort estimate:** L
- **Notes:** Load testing - 4 tests added

#### Task 30: Convert TestMaxOid
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestMaxOid converted to JUnit 5
- **Timestamp:** 2026-02-15 11:22
- **Effort estimate:** S
- **Notes:** Max OID handling - 4 tests added

#### Task 31: Convert TestMod
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestMod converted to JUnit 5
- **Timestamp:** 2026-02-15 11:38
- **Effort estimate:** S
- **Notes:** Modification tracking - 3 tests added

#### Task 32: Convert TestPerf
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestPerf converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Performance testing

#### Task 33: Convert TestRandomBlob
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestRandomBlob converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Random BLOB access

#### Task 34: Convert TestSSD
- **Status:** skipped
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestSSD converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Interactive menu-driven app - difficult to convert

#### Task 35: Convert TestSOD
- **Status:** skipped
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestSOD converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Interactive menu-driven app - difficult to convert

#### Task 36: Convert TestTimeSeries
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestTimeSeries converted to JUnit 5
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Time series data

#### Task 37: Convert TestVersion
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestVersion converted to JUnit 5
- **Timestamp:** 2026-02-15 15:52
- **Effort estimate:** M
- **Notes:** Version control - 5 tests added

#### Task 38: Convert TestXML
- **Status:** completed
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestXML converted to JUnit 5
- **Timestamp:** 2026-02-15 11:53
- **Effort estimate:** M
- **Notes:** XML export/import - 2 tests added

## Risk Assessment
- **High Risk:** Some tests may have complex setup requirements or external dependencies
- **Medium Risk:** Threading/concurrency tests may be difficult to convert reliably
- **Low Risk:** Standard tests should convert easily

## Current Blockers
- TestReplic, TestReplic2 - require network setup for master/slave replication
- TestDbServer - requires Database server setup
- TestSSD, TestSOD - interactive menu-driven apps

## Next Steps
1. Continue with remaining tests
2. Run `mvn test` after each conversion to verify
3. Update progress doc after each task completion
