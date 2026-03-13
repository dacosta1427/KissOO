# Working Rules (must be included)

---

# Progress: Project Quality Improvements

## Project Status
- **Start Date:** 2026-02-18
- **Current Phase:** Planning
- **Overall Progress:** 0% complete

## Task Progress

### Priority #1: Build Configuration Improvements

#### Task 1.1: Update pom.xml with -Xlint:all compiler flag
- **Description:** Add -Xlint:all to Maven compiler arguments to catch all compiler warnings including missing docs, missing serialVersionUID, etc.
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** pom.xml updated with -Xlint:all flag
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Current flags: deprecation, unchecked, removal. Need to add: all, and possibly -options for release compatibility.

#### Task 1.2: Run mvn clean compile and address any new warnings
- **Description:** After adding -Xlint:all, compile and fix any new warnings revealed
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** 
- **Success criteria:** Build completes with 0 warnings
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** May reveal new issues like missing serialVersionUID, missing javadoc on public methods

#### Task 1.3: Add -Xlint:-options to handle release flag compatibility
- **Description:** Add -Xlint:-options to suppress warnings about using -release flag with source/target
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 1.1
- **Owner:** 
- **Success criteria:** No options-related warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** The -release flag may generate warnings about source/target when combined with -Xlint:all

### Priority #2: Logging Improvements

#### Task 2.1: Add SLF4J dependency to pom.xml
- **Description:** Add SLF4J API as a compile dependency. Use slf4j-api and slf4j-simple for zero-config logging.
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** SLF4J dependency added to pom.xml
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Use slf4j-api for the API and slf4j-simple for the implementation (or logback-classic)

#### Task 2.2: Create Logger wrapper class for consistent logging
- **Description:** Create a PerstLogger utility class that provides consistent logging across the codebase. This abstracts SLF4J and allows changing implementations if needed.
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.1
- **Owner:** 
- **Success criteria:** Logger wrapper class created in impl package
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Should provide error, warn, debug, trace methods

#### Task 2.3: Replace printStackTrace in QueryImpl.java (13 instances)
- **Description:** Replace all printStackTrace() calls in QueryImpl.java (lines: 890, 901, 912, 923, 934, 1064, 1067, 1931, 1934, 2254, 2257, 2597, 2600) with proper logger calls.
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** 
- **Success criteria:** No printStackTrace in QueryImpl.java
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** This is the largest file with printStackTrace. Many are in query parsing error handling.

#### Task 2.4: Replace printStackTrace in ReplicationMasterFile.java (3 instances)
- **Description:** Replace printStackTrace() calls at lines 114, 142, 265 with proper logging
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** 
- **Success criteria:** No printStackTrace in ReplicationMasterFile.java
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** These are in replication error handling

#### Task 2.5: Replace printStackTrace in ReplicationSlaveStorageImpl.java (2 instances)
- **Description:** Replace printStackTrace() calls at lines 174, 195 with proper logging
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** 
- **Success criteria:** No printStackTrace in ReplicationSlaveStorageImpl.java
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** These are in slave storage error handling

#### Task 2.6: Replace printStackTrace in StorageImpl.java (1 instance)
- **Description:** Replace printStackTrace() call at line 3195 with proper logging
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** 
- **Success criteria:** No printStackTrace in StorageImpl.java
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** This is in a catch(Throwable) block - need to determine proper handling

#### Task 2.7: Replace printStackTrace in PerstTranslator.java (1 instance)
- **Description:** Replace printStackTrace() call at line 256 with proper logging
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** 
- **Success criteria:** No printStackTrace in PerstTranslator.java
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** This is in jassist bytecode translation error handling

### Priority #3: Exception Handling Review

#### Task 3.1: Review StorageImpl.java catch(Throwable) at line 3194
- **Description:** This catch(Throwable) block silently swallows serious errors. Review and add proper handling.
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Either add logging or document why swallowing is acceptable
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** This is in a critical path - need to understand why it catches Throwable

#### Task 3.2: Review QueryImpl.java silent catch blocks (lines 3935, 3953, 3959)
- **Description:** Three catch(Exception) blocks that silently swallow exceptions. Add proper error handling.
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Either add logging or document why silent swallowing is acceptable
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** These appear to be in query optimization code

#### Task 3.3: Review Index implementation catch blocks in impl/
- **Description:** Review catch(Exception) blocks in AltBtreeFieldIndex, AltBtreeMultiFieldIndex, RndBtreeFieldIndex, RndBtreeMultiFieldIndex, ThickFieldIndex
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Add proper error handling where needed
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** These are in field indexing code, about 15+ catch blocks total

#### Task 3.4: Add proper error handling or logging where needed
- **Description:** Based on reviews in tasks 3.1-3.3, add appropriate error handling
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Tasks 3.1, 3.2, 3.3
- **Owner:** 
- **Success criteria:** No silent exception swallowing
- **Timestamp:** 
- **Effort estimate:** L
- **Notes:** May involve multiple files

### Priority #4: Deprecated API Cleanup

#### Task 4.1: Review Database.java deprecated methods (lines 306, 546)
- **Description:** Review the two deprecated createTable methods in Database.java
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Decision on removal timeline or keep for compatibility
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** These may still be used by existing applications

#### Task 4.2: Review Persistent.java deprecated annotation (line 15)
- **Description:** Review the @Deprecated(forRemoval=true) annotation on Persistent class
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Decision on removal timeline
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** This is a core interface - removal would be breaking

#### Task 4.3: Decide on removal or deprecation timeline
- **Description:** Create a deprecation policy for these remaining deprecated items
- **Status:** pending
- **Priority:** Low
- **Dependencies:** Tasks 4.1, 4.2
- **Owner:** 
- **Success criteria:** Documented timeline for cleanup
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** May need to maintain for backward compatibility

### Priority #5: Verification

#### Task 5.1: Run full test suite to verify no regressions
- **Description:** Run `mvn test` to ensure all changes work correctly
- **Status:** pending
- **Priority:** High
- **Dependencies:** All above tasks
- **Owner:** 
- **Success criteria:** All 355 tests pass
- **Timestamp:** 
- **Effort estimate:** L
- **Notes:** Full test suite takes several minutes

#### Task 5.2: Verify build completes with 0 warnings
- **Description:** Run `mvn clean compile` and verify 0 warnings
- **Status:** pending
- **Priority:** High
- **Dependencies:** Tasks 1.1, 1.2, 1.3, 2.x
- **Owner:** 
- **Success criteria:** Build shows 0 warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** With -Xlint:all, should still be 0 warnings

## Risk Assessment
- **High Risk:** None expected - logging changes don't affect functionality
- **Medium Risk:** Compiler flag changes may reveal new warnings that need fixing
- **Low Risk:** Exception handling changes could theoretically affect behavior

## Current Blockers
- None - ready to start

## Next Steps
1. Start with Priority #1 (build configuration) to see current state
2. Add SLF4J dependency and create logger wrapper
3. Replace printStackTrace calls systematically
4. Review and improve exception handling
5. Verify with full test suite
