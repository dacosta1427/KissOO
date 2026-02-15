# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Increase Test Coverage

## Objective
Increase code coverage from the current 39% to at least 70% by systematically identifying and testing uncovered code paths.

## Background
- Current coverage: 39% (74,168 of 122,679 instructions)
- Branch coverage: 30%
- Line coverage: 38%
- Method coverage: 62%
- Class coverage: 67%
- Total classes analyzed: 393
- Total tests: 288 (all passing)

## Current Coverage by Package
| Package | Coverage | Classes |
|---------|----------|---------|
| org.garret.perst | 27% | 98 |
| org.garret.perst.impl | 40% | 284 |
| org.garret.perst.fulltext | 64% | 11 |

## Approach

### Step 1: Analyze Coverage Report
Use the JaCoCo HTML report to identify:
- Classes with 0% coverage
- Methods not called by tests
- Uncovered branches (decision points)

### Step 2: Convert Remaining Tests
Convert remaining demo applications to JUnit tests:
- TestReplic / TestReplic2 (replication - requires network)
- Remaining demo apps (Simple, Benchmark, etc.)

### Step 3: Add Targeted Tests for Uncovered Classes
Create new tests for high-priority uncovered classes:
- Storage implementation
- Index implementations (B-tree, R-tree, KD-tree, etc.)
- Transaction handling
- Cache management

### Step 4: Increase Edge Case Coverage
Add tests for:
- Error conditions
- Boundary cases
- Exception handling
- Edge cases in data structures

## Implementation Instructions

### Quick Start
```bash
# Navigate to worktree
cd /home/dacosta/Projects/oodb-testcoverage

# Run tests and generate coverage report
mvn clean test

# View coverage report
# Open target/site/jacoco/index.html in browser
# Or use:
# firefox target/site/jacoco/index.html

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
   - Storage interface implementations
   - Query processing
   - Transaction handling
   - Lock management

2. **org.garret.perst.impl package (40%)**
   - B-tree implementation
   - Page management
   - Cache implementations
   - File I/O

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
- Network-based tests (replication) require special setup

## Rollback Plan
- Use git to revert changes: `git revert HEAD` or `git reset --hard HEAD`
- Verify with: `mvn test`
- Keep original tests as backup

## Success Criteria
- [ ] Increase overall instruction coverage from 39% to 70%+
- [ ] All 288+ tests continue to pass
- [ ] No regressions in existing functionality
- [ ] Document coverage improvements by package
