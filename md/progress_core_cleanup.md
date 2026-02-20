# Working Rules (must be included)

---

# Progress: Perst Core Code Cleanup

## Project Status
- **Start Date:** 2026-02-17
- **Current Phase:** Planning
- **Overall Progress:** 0% complete

## Task Progress

### Priority #1: High Impact Files

#### Task 1.1: Fix Persistent.java
- **Description:** Remove or annotate the deprecated finalize() method that uses API marked for removal in Java 25
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without deprecation warning
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Java 25 marks finalize() for removal

#### Task 1.2: Fix Database.java
- **Description:** Add proper type parameters to resolve ~28 unchecked warnings across Index, FieldIndex, HashMap, Query operations
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** L
- **Notes:** Largest file with most warnings

#### Task 1.3: Fix StorageImpl.java
- **Description:** Fix unchecked return type conversions in getMemoryDump() and getRoot() methods
- **Status:** pending
- **Priority:** High
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** Interface signature changes may be needed

### Priority #2: Heavy Warning Files

#### Task 2.1: Fix AltBtree.java
- **Description:** Address ~58 unchecked calls to Link.setObject(), ArrayList.add(), and Comparable.compareTo()
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** L
- **Notes:** Largest number of warnings, may need @SuppressWarnings with justification

#### Task 2.2: Fix Aggregator.java
- **Description:** Fix 12 unchecked generic method calls (initialize, accumulate, merge, compareTo, add, addAll)
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** M
- **Notes:** 

#### Task 2.3: Fix AltBtreeFieldIndex.java
- **Description:** Fix 6 unchecked array casts and conversions
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 2.4: Fix AltBtreeMultiFieldIndex.java
- **Description:** Fix 7 unchecked calls to Comparable.compareTo(), ArrayList.toArray(), and casts
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

### Priority #3: Moderate Warning Files

#### Task 3.1: Fix SmallMap.java
- **Description:** Fix 6 unchecked conversions and casts for Pair arrays
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** Generic array type issues

#### Task 3.2: Fix Projection.java
- **Description:** Fix 4 unchecked cast operations
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 3.3: Fix FullTextSearchHelper.java
- **Description:** Fix 3 unchecked ArrayList operations
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 3.4: Fix FullTextSearchResult.java
- **Description:** Fix 2 unchecked Comparator issues in Arrays.sort()
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

### Priority #4: Minor Warning Files

#### Task 4.1: Fix Version.java
- **Description:** Fix 2 unchecked List operations
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 4.2: Fix VersionHistory.java
- **Description:** Fix 1 unchecked cast
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 4.3: Fix L2List.java
- **Description:** Fix 1 unchecked Query.select() call
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 4.4: Fix AltBtreeCompoundIndex.java
- **Description:** Fix 1 unchecked compareTo call
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

#### Task 4.5: Fix AltPersistentSet.java
- **Description:** Fix 3 unchecked array casts
- **Status:** pending
- **Priority:** Low
- **Dependencies:** None
- **Owner:** 
- **Success criteria:** Compiles without unchecked warnings
- **Timestamp:** 
- **Effort estimate:** S
- **Notes:** 

## Risk Assessment
- **High Risk:** Changes to core classes may break existing tests if API signatures change
- **Medium Risk:** Large number of warnings in AltBtree.java may require @SuppressWarnings extensively
- **Low Risk:** Most fixes are straightforward type parameter additions

## Current Blockers
- None - this is the planning phase

## Next Steps
1. Begin fixing Priority #1 files (Persistent.java, Database.java, StorageImpl.java)
2. Run tests after each file fix to ensure no regressions
3. Verify with `mvn clean compile` after each fix
