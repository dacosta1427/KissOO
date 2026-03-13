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

# Todolist: Fix Warnings in CodeGeneratorImpl.java

## Tasks

### Phase 1: Analysis

- [ ] Task 1.1: Identify all Node subclasses in QueryImpl.java
- [ ] Task 1.2: Identify which classes CodeGeneratorImpl.java uses

### Phase 2: Move Base Class

- [ ] Task 2.1: Create `Node.java` with base class and constants
- [ ] Task 2.2: Remove Node class from QueryImpl.java
- [ ] Task 2.3: Verify compilation

### Phase 3: Move Subclasses (in dependency order)

- [ ] Task 3.1: Move `BinOpNode.java`
- [ ] Task 3.2: Move `CurrentNode.java`
- [ ] Task 3.3: Move literal node classes (`IntLiteralNode`, `RealLiteralNode`, `StrLiteralNode`, `DateLiteralNode`, `ConstantNode`)
- [ ] Task 3.4: Move `ParameterNode.java`
- [ ] Task 3.5: Move `LoadNode.java`
- [ ] Task 3.6: Move any remaining Node subclasses used externally

### Phase 4: Verification

- [ ] Task 4.1: Compile to verify no auxiliary class warnings
- [ ] Task 4.2: Run all junit tests

## Success Criteria

- [ ] `mvn compile` produces no auxiliary class warnings for CodeGeneratorImpl.java
- [ ] All existing junit tests pass
- [ ] No functional changes to the codebase

## Rollback Plan

1. Git commit before changes
2. Revert command: `git checkout HEAD -- src/main/java/org/garret/perst/impl/QueryImpl.java`
3. Delete all new Node*.java files: `rm src/main/java/org/garret/perst/impl/Node*.java`
4. Verify with `mvn compile`