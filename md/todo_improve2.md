# Working Rules (must be included)

---

# Todolist: Project Quality Improvements

## Tasks

### Priority #1: Build Configuration Improvements
- [ ] Task 1.1: Update pom.xml with -Xlint:all compiler flag
- [ ] Task 1.2: Run mvn clean compile and address any new warnings
- [ ] Task 1.3: Add -Xlint:-options to handle release flag compatibility

### Priority #2: Logging Improvements
- [ ] Task 2.1: Add SLF4J dependency to pom.xml
- [ ] Task 2.2: Create Logger wrapper class for consistent logging
- [ ] Task 2.3: Replace printStackTrace in QueryImpl.java (13 instances)
- [ ] Task 2.4: Replace printStackTrace in ReplicationMasterFile.java (3 instances)
- [ ] Task 2.5: Replace printStackTrace in ReplicationSlaveStorageImpl.java (2 instances)
- [ ] Task 2.6: Replace printStackTrace in StorageImpl.java (1 instance)
- [ ] Task 2.7: Replace printStackTrace in PerstTranslator.java (1 instance)

### Priority #3: Exception Handling Review
- [ ] Task 3.1: Review StorageImpl.java catch(Throwable) at line 3194
- [ ] Task 3.2: Review QueryImpl.java silent catch blocks (lines 3935, 3953, 3959)
- [ ] Task 3.3: Review Index implementation catch blocks in impl/
- [ ] Task 3.4: Add proper error handling or logging where needed

### Priority #4: Deprecated API Cleanup
- [ ] Task 4.1: Review Database.java deprecated methods (lines 306, 546)
- [ ] Task 4.2: Review Persistent.java deprecated annotation (line 15)
- [ ] Task 4.3: Decide on removal or deprecation timeline

### Priority #5: Verification
- [ ] Task 5.1: Run full test suite to verify no regressions
- [ ] Task 5.2: Verify build completes with 0 warnings

## Success Criteria
- [ ] Build completes with 0 warnings using -Xlint:all
- [ ] All printStackTrace() calls replaced with proper logging
- [ ] All JUnit tests pass
- [ ] No functionality regressions

## Rollback Plan
1. **Checkpoint:** Git commit before starting work
2. **Revert command:** `git reset --hard HEAD`
3. **Verification:** Run `mvn clean test` - all tests should pass
4. **Impact:** Code quality improvements, no functional changes
