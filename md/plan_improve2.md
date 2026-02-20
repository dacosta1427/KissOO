# Working Rules (must be included)

---

# Plan: Project Quality Improvements

## Goal
Improve code quality, logging, and build configuration based on project analysis.

## Background
The Perst project has been analyzed and the following areas need improvement:

1. **Logging** - 21 instances of `printStackTrace()` instead of proper logging
2. **Exception Handling** - 62 broad catch blocks silently swallowing errors
3. **Compiler Configuration** - Missing `-Xlint:all` flag
4. **Deprecated APIs** - Some @Deprecated annotations still present

These improvements will enhance:
- Debugging and monitoring capabilities
- Code maintainability
- Build reliability
- Java version compatibility

## Affected Areas

### Code Changes Required
- Replace `printStackTrace()` with proper logging (21 locations)
- Review and improve broad exception handling (62 locations)
- Address remaining deprecated API usage

### Build Changes Required
- Add `-Xlint:all` compiler flag
- Verify compatibility with Java 25

## Implementation Strategy

### Phase 1: Logging Improvements
1. Add a logging framework (SLF4J with simple implementation)
2. Replace all `printStackTrace()` calls with proper logger calls
3. Use appropriate log levels (error, warn, debug)

### Phase 2: Exception Handling Review
1. Identify catch blocks that silently swallow errors
2. Add proper error handling or logging where appropriate
3. Consider specific exception types instead of broad Exception/Throwable

### Phase 3: Build Configuration
1. Update pom.xml with `-Xlint:all` flag
2. Run compile to verify no new warnings
3. Fix any new warnings revealed

### Phase 4: Deprecated API Cleanup
1. Review remaining @Deprecated methods
2. Determine if they should be removed or kept for compatibility

## Rollback Plan
- **Checkpoint:** Git commit before changes
- **Revert command:** `git reset --hard HEAD`
- **Verification:** Run `mvn clean test` and verify tests pass
- **Impact:** Code quality improvements only, no functional changes

---

## Detailed Analysis

### 1. Logging Improvements (Priority: High)

#### Files with printStackTrace():
| File | Count | Lines |
|------|-------|-------|
| QueryImpl.java | 13 | 890, 901, 912, 923, 934, 1064, 1067, 1931, 1934, 2254, 2257, 2597, 2600 |
| ReplicationMasterFile.java | 3 | 114, 142, 265 |
| ReplicationSlaveStorageImpl.java | 2 | 174, 195 |
| StorageImpl.java | 1 | 3195 |
| PerstTranslator.java | 1 | 256 |
| LruObjectCache.java | 1 (commented) | 183 |

**Approach:** Use SLF4J API with runtime binding to actual implementation. This is the standard for Java libraries.

### 2. Exception Handling (Priority: Medium)

#### Notable Broad Catch Blocks:
| File | Line | Issue |
|------|------|-------|
| StorageImpl.java | 3194 | catch (Throwable) - hides serious errors |
| QueryImpl.java | 3935, 3953, 3959 | catch(Exception) - silently ignored |
| Various Index*.java | Multiple | catch (Exception) - hides reflection errors |

**Approach:** Review each location and either:
- Add proper error handling/logging
- Use more specific exception types
- Document why silent swallowing is acceptable

### 3. Compiler Configuration (Priority: High)

Current pom.xml settings:
```xml
<arg>-Xlint:deprecation</arg>
<arg>-Xlint:unchecked</arg>
<arg>-Xlint:removal</arg>
```

Recommended:
```xml
<arg>-Xlint:all</arg>
<arg>-Xlint:-options</arg>  <!-- needed with -release flag -->
```

### 4. Deprecated APIs (Priority: Low)

Remaining:
- `Database.java:306` - deprecated createTable method
- `Database.java:546` - deprecated createTable method  
- `Persistent.java:15` - marked for removal

**Approach:** Determine if these should be removed (breaking change) or kept for backward compatibility.

---

## Files Modified

### New Dependencies
- Add SLF4J API dependency

### Modified Files (Code)
- All files with printStackTrace() calls
- Exception handling improvements as needed

### Modified Files (Build)
- pom.xml - compiler flags

### Modified Files (Documentation)
- Update md/note_donotforget.md with completed items
