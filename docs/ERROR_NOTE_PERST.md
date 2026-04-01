# Error Note: CDatabase RootObject ClassCastException

## Date
2026-04-01

## Affected Component
`org.garret.perst.continuous.CDatabase` - Database initialization

## Issue Summary

When `CDatabase.open(Storage storage, String indexPath)` is called on an existing database that was not created with CDatabase, a `ClassCastException` is thrown because the storage's root object cannot be cast to `org.garret.perst.continuous.RootObject`.

## Error Message

```
java.lang.ClassCastException: class mycompany.domain.CDatabaseRoot cannot be cast to class org.garret.perst.continuous.RootObject
    at org.garret.perst.continuous.CDatabase.open(CDatabase.java:197)
```

## Root Cause

1. `RootObject` is a package-private class in `org.garret.perst.continuous` package
2. `CDatabase.open()` internally casts `storage.getRoot()` to `RootObject`
3. When storage was created with a different root type (e.g., a class extending `Persistent`), the cast fails
4. There is no way for external code to create a class that extends `RootObject` due to package visibility

## Code Location

`CDatabase.java` around line 197 (in open method):
```java
RootObject root = (RootObject) storage.getRoot();
```

## Impact

- Applications cannot migrate existing Perst databases to CDatabase mode
- No fallback mechanism - initialization fails completely
- Data becomes inaccessible unless database is recreated from scratch

## Reproduction Steps

1. Create a database using standard `Storage` with a custom root class:
   ```java
   public class MyRoot extends Persistent {
       // ... fields and methods
   }
   MyRoot root = new MyRoot();
   storage.setRoot(root);
   ```

2. Try to open the same database with CDatabase:
   ```java
   Storage storage = StorageFactory.getInstance().createStorage();
   storage.open("mydb.dbs");  // Loads existing MyRoot
   CDatabase cdb = CDatabase.instance;
   cdb.open(storage, "mydb.idx");  // FAILS: ClassCastException
   ```

## Suggested Solutions

### Option 1: Make RootObject Public
Change `RootObject` from package-private to public so external code can extend it:

```java
// In org.garret.perst.continuous
public class RootObject extends Persistent {
    // ... existing code
}
```

### Option 2: Add Graceful Fallback
Modify `CDatabase.open()` to handle incompatible root types:

```java
public void open(Storage storage, String indexPath) {
    Object rootObj = storage.getRoot();
    if (rootObj == null) {
        this.root = new RootObject();
        storage.setRoot(this.root);
    } else if (rootObj instanceof RootObject) {
        this.root = (RootObject) rootObj;
    } else {
        // Option A: Create new RootObject, migrate data
        // Option B: Throw descriptive exception with migration instructions
        throw new CDatabaseException(
            "Storage root is " + rootObj.getClass().getName() + 
            " which is incompatible with CDatabase. " +
            "Please migrate database or use standard Storage mode.");
    }
    // ... rest of initialization
}
```

### Option 3: Provide Migration Utility
Add a utility method to migrate from standard Storage to CDatabase:

```java
public static void migrateToCDatabase(Storage oldStorage, String newPath) {
    // Read all objects from old storage
    // Create new CDatabase
    // Insert objects into new database
    // Update references
}
```

## Current Workaround

Our application now catches the `ClassCastException` and falls back to standard Storage mode, preserving data but losing CDatabase features (Lucene search, versioning).

```java
try {
    cdb.open(storage, indexPath);
} catch (ClassCastException e) {
    // Fall back to standard Storage - data preserved
    logger.warn("CDatabase unavailable, using standard Storage");
    cdb = null;
}
```

## Environment

- Perst Version: 5.1.1 (perst-dcg)
- Java Version: 23
- Platform: Linux
- Tomcat: 10.x

## Contact

For questions about this error note, please contact the KissOO development team.
