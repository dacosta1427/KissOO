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

# Plan: Fix Warnings in CodeGeneratorImpl.java

## Overview

This plan addresses the auxiliary class warnings produced by `CodeGeneratorImpl.java`. The warnings are about accessing classes defined in `QueryImpl.java` from outside its source file.

## Warning Inventory

### Summary

| # | Warning Type | Location | Description | Severity |
|---|-------------|----------|-------------|----------|
| 1 | Auxiliary class access | CodeGeneratorImpl.java:634 | `Node` from QueryImpl.java | Medium |
| 2 | Auxiliary class access | CodeGeneratorImpl.java:634 | `BinOpNode` from QueryImpl.java | Medium |
| 3 | Auxiliary class access | CodeGeneratorImpl.java:727 | `Node` from QueryImpl.java | Medium |
| 4 | Auxiliary class access | CodeGeneratorImpl.java:781 | `Node` from QueryImpl.java | Medium |

### Detailed Analysis

#### Root Cause

`QueryImpl.java` contains many package-private classes used for query expression tree nodes:
- `Node` - Base class with type constants and operator constants (~350 lines)
- `BinOpNode` - Binary operator node
- `CurrentNode`, `IntLiteralNode`, `RealLiteralNode`, etc. - Various node subclasses

`CodeGeneratorImpl.java` needs to create these node types when generating query code.

#### Classes Used by CodeGeneratorImpl.java

Based on analysis, CodeGeneratorImpl.java uses:
- `Node` (type constants like `Node.tpInt`, `Node.opNop`, etc.)
- `BinOpNode` (creates binary operator nodes)
- `CurrentNode`, `IntLiteralNode`, `RealLiteralNode`, `StrLiteralNode`
- `ConstantNode`, `DateLiteralNode`, `ParameterNode`, `LoadNode`

#### Complexity

This is significantly more complex than the `ArrayPos` fix because:
1. Many interdependent classes need to be moved
2. `Node` has many subclasses in `QueryImpl.java`
3. The classes have cross-references to each other
4. ~30+ classes may need to be extracted

### Proposed Approach

**Option A: Move all Node-related classes to separate files**
- Create `Node.java` with the base class
- Create separate files for each node subclass
- Keep them package-private

**Option B: Keep classes together in a dedicated file**
- Create `QueryNodes.java` containing all node classes
- Move all node-related classes from `QueryImpl.java`

**Recommended: Option A** - Follows standard Java conventions (one class per file)

## Implementation Steps

### Phase 1: Analysis
1. Identify all Node subclasses in QueryImpl.java
2. Determine which are used externally by CodeGeneratorImpl.java
3. Map dependencies between classes

### Phase 2: Move Base Class
1. Create `Node.java` with base class and all constants
2. Remove Node class from QueryImpl.java
3. Verify compilation

### Phase 3: Move Subclasses
1. Create separate files for each subclass:
   - `BinOpNode.java`
   - `CurrentNode.java`
   - `IntLiteralNode.java`
   - `RealLiteralNode.java`
   - `StrLiteralNode.java`
   - `ConstantNode.java`
   - `DateLiteralNode.java`
   - `ParameterNode.java`
   - `LoadNode.java`
2. Update QueryImpl.java to remove moved classes
3. Verify compilation after each move

### Phase 4: Verification
1. Run `mvn compile` - should show no auxiliary class warnings
2. Run `mvn test` - all tests should pass

## Risk Assessment

| Risk Level | Description |
|------------|-------------|
| **Medium** | Large number of classes to move, potential for missing dependencies |

## Verification

After implementing the fix:
1. Run `mvn compile` - should show no warnings for CodeGeneratorImpl.java
2. Run `mvn test` - all tests should pass
3. Verify the project builds cleanly

## Success Criteria

- [ ] `mvn compile` produces no auxiliary class warnings for CodeGeneratorImpl.java
- [ ] All existing tests pass
- [ ] No functional changes to the codebase