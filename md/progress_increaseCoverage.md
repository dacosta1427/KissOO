# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Progress: Increase Test Coverage

## Project Status
- **Start Date:** 2026-02-15
- **Current Phase:** Planning
- **Overall Progress:** 0% (baseline: 39% coverage)
- **Target Coverage:** 70%+

## Current Coverage Metrics
| Metric | Current | Target |
|--------|---------|--------|
| Instruction | 39% | 70% |
| Branch | 30% | 50% |
| Line | 38% | 70% |
| Method | 62% | 80% |
| Class | 67% | 85% |

## Current Coverage by Package
| Package | Coverage | Classes |
|---------|----------|---------|
| org.garret.perst | 27% | 98 |
| org.garret.perst.impl | 40% | 284 |
| org.garret.perst.fulltext | 64% | 11 |

## Task Progress

### Priority #1: Analyze Current Coverage

#### Task 1.1: Run initial coverage analysis
- **Status:** completed
- **Priority:** High
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** Coverage report generated at target/site/jacoco/index.html
- **Timestamp:** 2026-02-15 21:29
- **Effort estimate:** S
- **Notes:** JaCoCo 0.8.14 successfully generated coverage report

#### Task 1.2: Identify classes with 0% coverage
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Cline
- **Success criteria:** List of classes with 0% coverage documented
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Need to analyze HTML report for uncovered classes

#### Task 1.3: Document uncovered packages and classes
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** Cline
- **Success criteria:** Documentation of priority targets for testing
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Will create prioritized list of classes to test

### Priority #2: Convert Remaining Tests

#### Task 2.1: Convert TestReplic to JUnit
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestReplic converted to JUnit 5, tests pass
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Requires network setup for master/slave replication

#### Task 2.2: Convert TestReplic2 to JUnit
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** TestReplic2 converted to JUnit 5, tests pass
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Requires network setup for master/slave replication

#### Task 2.3: Review remaining demo apps for conversion
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** Cline
- **Success criteria:** List of demo apps that can be converted
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Simple, Benchmark, Guess, etc. - may not be suitable for JUnit

### Priority #3: Add Targeted Tests for org.garret.perst

#### Task 3.1: Add Storage implementation tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Storage implementation tests added, coverage improved
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Core functionality - high impact

#### Task 3.2: Add Query processing tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Query tests added, coverage improved
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** JSQL query processing - high impact

#### Task 3.3: Add Transaction handling tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Transaction tests added, coverage improved
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Commit, rollback, recovery

#### Task 3.4: Add Lock management tests
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Lock tests added, coverage improved
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Concurrency control

### Priority #4: Add Targeted Tests for org.garret.perst.impl

#### Task 4.1: Add B-tree implementation tests
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** B-tree tests added, coverage improved
- **Timestamp:** -
- **Effort estimate:** L
- **Notes:** Core storage mechanism - high impact

#### Task 4.2: Add Page management tests
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Page management tests added
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Internal page handling

#### Task 4.3: Add Cache implementations tests
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Cache tests added
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** LRU, SoftHash, etc.

#### Task 4.4: Add File I/O tests
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** File I/O tests added
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** OSFile implementations

### Priority #5: Edge Case Coverage

#### Task 5.1: Add error condition tests
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Error handling tests added
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** IOException, disk full, etc.

#### Task 5.2: Add boundary case tests
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Boundary tests added
- **Timestamp:** -
- **Effort estimate:** M
- **Notes:** Empty collections, max values, etc.

#### Task 5.3: Add exception handling tests
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Task 1.3
- **Owner:** Cline
- **Success criteria:** Exception tests added
- **Timestamp:** -
- **Effort estimate:** S
- **Notes:** NullPointer, IllegalArgument, etc.

## Risk Assessment
- **High Risk:** Some uncovered code may be deprecated or platform-specific
- **Medium Risk:** Complex internal classes may be difficult to test in isolation
- **Low Risk:** Standard tests should convert easily

## Current Blockers
- None - starting with analysis phase

## Next Steps
1. Analyze JaCoCo HTML report for uncovered classes
2. Identify high-priority targets (low coverage, high impact)
3. Start adding targeted tests
4. Run `mvn test` after each change to verify
