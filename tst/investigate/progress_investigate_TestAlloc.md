# Working Rules (must be included)

---

# Progress: Investigate TestAlloc

## Project Status
- **Start Date:** 2026-02-14
- **Current Phase:** Investigation
- **Overall Progress:** 0% complete

## Task Progress

### Task 1.1: Examine TestAlloc.java source code
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** Developer
- **Success criteria:** Understand what TestAlloc does and what files it expects
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Need to read source code to understand expected behavior

### Task 1.2: Check what arguments TestAlloc accepts
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** Developer
- **Success criteria:** Know all command-line arguments
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** May need to run with different parameters

### Task 1.3: Run makefile test sequence
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** Developer
- **Success criteria:** See if TestAlloc works when run as makefile specifies
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Makefile shows TestAlloc runs 3 times - initialize, verify, cleanup

### Task 1.4: Identify root cause
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.3
- **Owner:** Developer
- **Success criteria:** Document exact cause of file access error
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Based on error, likely missing database file or wrong sequence

### Task 1.5: Implement fix
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.4
- **Owner:** Developer
- **Success criteria:** TestAlloc passes or documented workaround
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** May need to modify test script or test itself

## Risk Assessment
- **High Risk:** None identified yet
- **Medium Risk:** May require modifying test to work with current setup
- **Low Risk:** Likely just sequencing issue

## Current Blockers
- None - starting investigation

## Next Steps
1. Read tst/TestAlloc.java to understand expected behavior
2. Check what files it expects to exist
3. Run in proper sequence per makefile
