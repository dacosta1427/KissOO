# Perst CDatabase Lucene Index Directory Issue

## Executive Summary

When attempting to initialize Perst CDatabase (with versioning/full-text indexing), the application fails to create the required Lucene index directory. The database path (`data/oodb`) is being created as a FILE instead of a DIRECTORY, causing subsequent Lucene index initialization to fail with `NoSuchFileException`.

## Current Status

| Item | Status |
|------|--------|
| Perst Enabled | true |
| Use CDatabase | true |
| Configured Path | `C:\opt\Projects\KissOO\data\oodb` |
| Path Resolution | Working (correctly resolves to project root) |
| Directory Creation | FAILING |
| Lucene Index | NOT CREATED |

## Error Log

```
[PerstContext] Database path: C:\opt\Projects\KissOO\data\oodb
[PerstContext] Checking idx dir: C:\opt\Projects\KissOO\data\oodb\idx exists=false
[PerstContext] Created idx directory: false at C:\opt\Projects\KissOO\data\oodb\idx
[PerstContext] idx dir isWritable: false
[PerstContext] Failed to initialize Perst CDatabase: java.nio.file.NoSuchFileException: C:\opt\Projects\KissOO\data\oodb\idx
```

## Observed Behavior

1. `data/oodb` is created as a **FILE** (81920 bytes), not a directory
2. `mkdirs()` returns `false` when attempting to create `data/oodb/idx`
3. Parent directory reports `isWritable: false` despite user having write permissions

## Current Code (PerstContext.java)

```java
String dbPath = PerstConfig.getInstance().getDatabasePath();
java.io.File dbFile = new java.io.File(dbPath);

// Delete if it's a file (attempted fix)
if (dbFile.exists() && dbFile.isFile()) {
    dbFile.delete();
}

// Ensure directory exists
if (!dbFile.exists()) {
    dbFile.mkdirs();
}

// Create Lucene index directory
java.io.File idxDir = new java.io.File(dbPath + java.io.File.separator + "idx");
if (!idxDir.exists()) {
    boolean created = idxDir.mkdirs();  // Returns false
}

// Open Perst
int poolSize = PerstConfig.getInstance().getPagePoolSize();
storage.open(dbPath, poolSize);  // Creates oodb as FILE?

// Open CDatabase with Lucene index
database.open(storage, dbPath + java.io.File.separator + "idx");  // Fails
```

## Questions for Resolution

### 1. File vs Directory
Is Perst CDatabase supposed to receive:
- A **FILE** path (e.g., `oodb.dbs`) - Perst creates the database file
- A **DIRECTORY** path (e.g., `oodb/`) - Perst stores files in directory

### 2. Lucene Index Location
What is the correct format for the Lucene index path?
- Current: `dbPath + "/idx"` = `C:\opt\Projects\KissOO\data\oodb\idx`
- Expected by Perst: ???

### 3. Perst Configuration
Does Perst 4.0.0 require:
- A separate index path configuration?
- Different initialization order?
- Specific Lucene version compatibility?

### 4. Alternative Approaches
Should we:
- Disable CDatabase versioning temporarily to test basic Storage mode?
- Use standard Storage instead of CDatabase?
- Configure a different index storage location?

## Investigation Required

1. **Perst CDatabase API Documentation**
   - Check `CDatabase.open(Storage storage, String indexPath)` method signature
   - Verify if indexPath should be file or directory
   - Check if index files have specific naming convention

2. **Perst JAR Analysis**
   - Verify Perst 4.0.0 JAR contents
   - Check for Lucene integration classes
   - Verify Lucene version compatibility (current: 9.11.0)

3. **Test Basic Storage Mode**
   - Temporarily disable CDatabase (use standard Storage)
   - Verify basic Perst persistence works without Lucene
   - Isolate whether issue is Perst or Lucene-specific

## Files Modified During Investigation

| File | Changes |
|------|---------|
| `application.ini` | Set `PerstDatabasePath = ../../../data/oodb` |
| `PerstConfig.java` | Added path normalization, canonical path resolution |
| `PerstContext.java` | Added directory pre-creation, debug logging |

## Next Steps

1. Determine correct Perst CDatabase initialization approach
2. Verify if `oodb` should be file path or directory path
3. Test with standard Storage (non-CDatabase) to isolate issue
4. Consider disabling Lucene indexing if not required for MVP

## References

- Perst Documentation: https://www.persystdev.com/
- Perst JAR: `libs/perst-dcg-4.0.0.jar` (DCG = Distributed Continuous Geatures)
- Lucene Version: 9.11.0

---

*Created: 2026-03-15*
*Project: KissOO*
