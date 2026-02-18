# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Increase Test Coverage

## Objective
Increase code coverage from the current 40% to at least 70% by systematically identifying and testing uncovered code paths.

## Background
- Current coverage: 40% (72,723 of 122,679 instructions)
- Branch coverage: 31%
- Line coverage: 39%
- Method coverage: 62%
- Class coverage: 67%
- Total classes analyzed: 393
- Total tests: 316 (all passing)

## Progress Update (2026-02-17)

### Coverage Improvement
- **Previous Coverage:** 39% (74,168 missed)
- **Current Coverage:** 40% (72,723 missed)
- **Net Improvement:** +1,445 instructions covered
- **Tests Added:** ~45 new tests

### New Test Files Added
- TestPersistentMap.java - 17 tests
- TestLink.java - 9 tests  
- TestRtree.java - 8 tests
- TestStorage.java - 7 tests
- TestBtreeCompoundIndex.java - 4 tests

### Package Coverage
| Package | Coverage | Classes |
|---------|----------|---------|
| org.garret.perst | 27% | 98 |
| org.garret.perst.impl | 42% | 284 |
| org.garret.perst.fulltext | 64% | 11 |

## Approach

### Step 1: Analyze Coverage Report ✓
- [x] Use the JaCoCo HTML report to identify classes with 0% coverage
- [x] Identify methods not called by tests
- [x] Document uncovered branches

### Step 2: Convert Remaining Tests
- [ ] TestReplic / TestReplic2 - SKIPPED (requires network setup)
- [ ] Remaining demo apps (Simple, Benchmark, etc.)

### Step 3: Add Targeted Tests ✓ (partial)
- [x] Storage implementation - TestStorage.java
- [x] Index implementations - TestBtreeCompoundIndex.java, TestCompoundIndex.java
- [x] Transaction handling - TestStorage.java
- [ ] Cache management
- [ ] File I/O

### Step 4: Increase Edge Case Coverage
- [ ] Error conditions
- [ ] Boundary cases
- [ ] Exception handling

## Implementation Instructions

### Quick Start
```bash
# Navigate to worktree
cd /home/dacosta/Projects/oodb-testcoverage

# Run tests and generate coverage report
mvn clean test

# View coverage report
# Open target/site/jacoco/index.html in browser

# Run tests without cleaning (faster)
mvn test
```

### Before Making Changes
1. Run `mvn test` to ensure baseline passes
2. Check current coverage: `target/site/jacoco/index.html`
3. Note which classes are uncovered

### After Making Changes
1. Run `mvn test` to verify tests pass
2. Check coverage improved
3. Commit changes

### Standard Procedure (MUST FOLLOW)
Per md/workingRules.md:
- After any change to core Perst code, ALL junit tests must pass
- Run `mvn test` before marking task as complete
- NEVER comment out code to make tests work

## Target Areas for Coverage Improvement

### High Priority (Low Coverage)
1. **org.garret.perst package (27%)**
   - Storage interface implementations ✓
   - Query processing ✓
   - Transaction handling ✓
   - Lock management

2. **org.garret.perst.impl package (42%)**
   - B-tree implementation ✓
   - Page management
   - Cache implementations
   - File I/O

### 0% Coverage Classes (High Impact)
- ReplicationMasterFile - 0% (1,362 instructions)
- PersistentMapImpl - 0% (1,250 instructions)
- RtreeRnPage - 0% (938 instructions)
- BitmapCustomAllocator - 0% (892 instructions)
- ReplicationSlaveStorageImpl - 0% (852 instructions)
- WeakHashTable - 0% (605 instructions)
- PersistentListImpl.ListIntermediatePage - 0% (589 instructions)

### Medium Priority
3. **Core utility classes**
   - String handling
   - Reflection utilities
   - Serialization

### Lower Priority (Already High)
4. **org.garret.perst.fulltext (64%)**
   - Full-text search (already good)

## Risks
- Some code may be platform-specific or deprecated
- Complex edge cases may be difficult to test
- Network-based tests (replication) require special setup - SKIPPED

## Rollback Plan
- Use git to revert changes: `git revert HEAD` or `git reset --hard HEAD`
- Verify with: `mvn test`
- Keep original tests as backup

## Success Criteria
- [x] All 316 tests continue to pass
- [x] No regressions in existing functionality
- [ ] Increase overall instruction coverage from 40% to 70%+
- [x] Document coverage improvements by package
