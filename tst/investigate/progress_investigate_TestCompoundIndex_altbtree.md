# Working Rules (must be included)

---

# Progress: Investigate TestCompoundIndex altbtree

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** Investigation
- **Overall Progress:** 0% complete

## Task Progress

### Task 1.1: Examine TestCompoundIndex.java
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** Developer
- **Success criteria:** Understand how altbtree mode works
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Read source to understand test

### Task 1.2: Check Perst library version
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Developer
- **Success criteria:** Know which version of perst.jar is used
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Check lib/perst.jar or pom.xml

### Task 1.3: Research known bugs
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** Developer
- **Success criteria:** Find if this is a known issue
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** May need to search online or check Perst docs

### Task 1.4: Determine solution
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Developer
- **Success criteria:** Fix, workaround, or exclusion
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Likely exclusion or library update

### Task 1.5: Document and implement
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.4
- **Owner:** Developer
- **Success criteria:** Solution documented
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Update tracking docs

## Risk Assessment
- **High Risk:** May be library bug requiring update
- **Medium Risk:** May need to exclude this test variant
- **Low Risk:** Fixable in test code

## Current Blockers
- None

## Next Steps
1. Read tst/TestCompoundIndex.java
2. Check Perst version
3. Research the NoSuchMethodException error
4. Decide on fix or exclusion
