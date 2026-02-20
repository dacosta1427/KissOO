# Working Rules (must be included)

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
### NEVER remove the md dir

## Core Principles

1. **All MD documentation lives in the `md/` directory**

2. **Every plan MUST be accompanied by:**
   - A **todolist** (`md/todo_*.md`) containing all items/steps to implement the plan
   - A **progress doc** (`md/progress_*.md`) containing all tasks with their current state

3. **Working rules at top of every plan(ning), progress and todolist document** - This ensures they are never forgotten

---

# Progress: Fix Warnings in CodeGeneratorImpl.java

## Project Status
- **Start Date:** 2026-02-20
- **Current Phase:** Analysis Complete - Deferred due to complexity
- **Overall Progress:** 20% complete (Analysis and planning done)

## Task Progress

### Phase 1: Analysis

#### Task 1.1: Identify all Node subclasses in QueryImpl.java
- **Description:** Analyze QueryImpl.java to find all classes that extend or reference Node
- **Status:** in_progress
- **Priority:** High
- **Dependencies:** None
- **Owner:** TBD
- **Success criteria:** Complete list of all Node-related classes
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** Classes identified: Node, BinOpNode, EmptyNode, IntLiteralNode, RealLiteralNode, StrLiteralNode, CurrentNode, DateLiteralNode, ConstantNode, IndexOutOfRangeError, ExistsNode, IndexNode, GetAtNode, InvokeNode, InvokeAnyNode, ConvertAnyNode, CompareNode, UnaryOpNode, LoadAnyNode, ResolveNode, LoadNode, AggregateFunctionNode, InvokeElementNode, ElementNode, ContainsNode, OrderNode

#### Task 1.2: Identify which classes CodeGeneratorImpl.java uses
- **Description:** Determine which Node classes are actually needed by CodeGeneratorImpl
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.1
- **Owner:** TBD
- **Success criteria:** List of classes to move
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** Known: Node, BinOpNode, CurrentNode, IntLiteralNode, RealLiteralNode, StrLiteralNode, ConstantNode, DateLiteralNode, ParameterNode, LoadNode

### Phase 2: Move Base Class

#### Task 2.1: Create `Node.java` with base class and constants
- **Description:** Extract Node class from QueryImpl.java to its own file
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 1.2
- **Owner:** TBD
- **Success criteria:** Node.java created, QueryImpl.java updated
- **Timestamp:** 2026-02-20
- **Effort estimate:** M
- **Notes:** Node class is ~350 lines with many constants

#### Task 2.2: Remove Node class from QueryImpl.java
- **Description:** Delete Node class from QueryImpl.java after extraction
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.1
- **Owner:** TBD
- **Success criteria:** QueryImpl.java compiles without Node class
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** Verify all references resolve to new file

#### Task 2.3: Verify compilation
- **Description:** Ensure project compiles after Node extraction
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.2
- **Owner:** TBD
- **Success criteria:** `mvn compile` succeeds
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** May need to fix references

### Phase 3: Move Subclasses

#### Task 3.1: Move `BinOpNode.java`
- **Description:** Extract BinOpNode class to its own file
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 2.3
- **Owner:** TBD
- **Success criteria:** BinOpNode.java created, compiles correctly
- **Timestamp:** 2026-02-20
- **Effort estimate:** M
- **Notes:** BinOpNode is ~450 lines

#### Task 3.2: Move `CurrentNode.java`
- **Description:** Extract CurrentNode class to its own file
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.3
- **Owner:** TBD
- **Success criteria:** CurrentNode.java created
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** Simple class

#### Task 3.3: Move literal node classes
- **Description:** Extract IntLiteralNode, RealLiteralNode, StrLiteralNode, DateLiteralNode, ConstantNode
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.3
- **Owner:** TBD
- **Success criteria:** All literal node files created
- **Timestamp:** 2026-02-20
- **Effort estimate:** M
- **Notes:** May need LiteralNode base class first

#### Task 3.4: Move `ParameterNode.java`
- **Description:** Extract ParameterNode class to its own file
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.3
- **Owner:** TBD
- **Success criteria:** ParameterNode.java created
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** 

#### Task 3.5: Move `LoadNode.java`
- **Description:** Extract LoadNode class to its own file
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Task 2.3
- **Owner:** TBD
- **Success criteria:** LoadNode.java created
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** 

#### Task 3.6: Move remaining Node subclasses
- **Description:** Move any other classes needed to resolve all warnings
- **Status:** pending
- **Priority:** Medium
- **Dependencies:** Tasks 3.1-3.5
- **Owner:** TBD
- **Success criteria:** All auxiliary class warnings resolved
- **Timestamp:** 2026-02-20
- **Effort estimate:** M
- **Notes:** 

### Phase 4: Verification

#### Task 4.1: Compile to verify no auxiliary class warnings
- **Description:** Run Maven compile and verify no warnings
- **Status:** pending
- **Priority:** High
- **Dependencies:** Phase 3 complete
- **Owner:** TBD
- **Success criteria:** No auxiliary class warnings for CodeGeneratorImpl.java
- **Timestamp:** 2026-02-20
- **Effort estimate:** S
- **Notes:** 

#### Task 4.2: Run all junit tests
- **Description:** Execute full test suite
- **Status:** pending
- **Priority:** High
- **Dependencies:** Task 4.1
- **Owner:** TBD
- **Success criteria:** All tests pass
- **Timestamp:** 2026-02-20
- **Effort estimate:** M
- **Notes:** Run `mvn test`

## Risk Assessment
- **High Risk:** None
- **Medium Risk:** Large number of interdependent classes; may miss some dependencies
- **Low Risk:** Individual class extractions are straightforward

## Current Blockers
- None

## Next Steps
1. Complete analysis of Node class hierarchy
2. Extract Node base class
3. Extract subclasses in dependency order