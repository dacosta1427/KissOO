# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Plan: Perst Project Improvements

## Overview
This plan addresses deprecated API usage in the Perst OODBMS codebase to ensure compatibility with Java 25+ (OpenJDK 25). The main issues are the use of deprecated and removed APIs that cause compilation warnings and will cause failures in future Java versions.

## Background
The Perst OODBMS was originally designed for Java 1.6. Since then, several APIs have been deprecated and eventually removed in Java 25:
- `Class.newInstance()` - deprecated in Java 9, removed in Java 25
- `Object.finalize()` - deprecated for removal since Java 9
- `System.runFinalization()` - deprecated for removal since Java 9

## Current Issues Found

### Issue #1: Class.newInstance() (40+ occurrences)
**Severity:** High
**Impact:** Compilation errors in Java 25

The code uses `Class.newInstance()` in 40+ locations, primarily in:
- `src/org/garret/perst/impl/ClassDescriptor.java` (lines 164, 169, 318)
- `src/org/garret/perst/impl/StorageImpl.java` (lines 3433, 3456, 3788, 4012, 4264)
- Various index implementations (AltBtree, Btree, etc.)

**Solution:** Replace with `Class.getDeclaredConstructor().newInstance()` which provides the same functionality with better exception handling.

### Issue #2: finalize() method
**Severity:** High
**Impact:** Compilation warnings, will cause errors in future Java versions

Location: `src/org/garret/perst/Persistent.java:16`

```java
@Deprecated(forRemoval=true)
protected void finalize() {
```

**Solution:** Replace with `java.lang.ref.Cleaner` or `java.lang.ref.PhantomReference` for resource cleanup.

### Issue #3: System.runFinalization() (6 occurrences)
**Severity:** Medium
**Impact:** Compilation warnings

Locations:
- `src/org/garret/perst/impl/WeakHashTable.java` (lines 85, 117, 139)
- `src/org/garret/perst/impl/LruObjectCache.java` (lines 127, 169, 218)

**Solution:** Replace with explicit resource management or use `Cleaner` API.

## Implementation Approach

1. **Create wrapper utility** for object instantiation to handle checked exceptions consistently
2. **Replace Class.newInstance()** calls throughout the codebase
3. **Replace finalize()** with Cleaner-based cleanup in Persistent.java
4. **Replace runFinalization()** calls with proper cleanup mechanisms
5. **Verify** all JUnit tests pass after changes

## Risk Assessment

| Risk | Level | Mitigation |
|------|-------|------------|
| Breaking existing functionality | Medium | Test thoroughly with existing test suite |
| Performance impact from new API | Low | Benchmark if needed |
| Complex exception handling changes | Medium | Wrap in utility class |

## Success Criteria
- [ ] All 139 JUnit tests pass
- [ ] No deprecation warnings in compilation
- [ ] No use of removed Java APIs
- [ ] Backward compatibility maintained for Java 25+ applications

## Rollback Plan
1. **Checkpoint:** Git commit before changes
2. **Revert command:** `git revert HEAD` or `git reset --hard HEAD~1`
3. **Verification:** Run `mvn test` and confirm all tests pass
4. **Impact:** Minimal - only deprecated APIs being replaced

---

## Related Documentation
- `md/todo_improve.md` - Detailed task checklist
- `md/progress_improve.md` - Progress tracking
