# CDatabase Integration - Status Update

## Branch: `feature/cdatabase-lucene-fix`

## What Works

1. **Build system** - Fixed classpath separator issue (`;` → `:`) in `bld` script for Linux
2. **Lucene index path fix** - Changed from `data/oodb/idx` (subdirectory) to `data/oodb.idx` (adjacent)
3. **CDatabase initialization** - Added code to initialize CDatabase when enabled in PerstStorageManager
4. **Build order fix** - Changed Tasks.java to compile `precompiled` before `test`
5. **Missing methods** - Added `ActorManager.getByUserId(int)` method

## Current Blocker

**Issue:** CDatabase requires the Storage root to be a `RootObject` (package-private in Perst), but our domain `CDatabaseRoot` extends `Persistent`.

**Error:**
```
java.lang.ClassCastException: class mycompany.domain.CDatabaseRoot cannot be cast to class org.garret.perst.continuous.RootObject
```

**Root Cause:** Perst CDatabase's `open()` method does:
```java
RootObject root = (RootObject) storage.getRoot();  // Line 77 of CDatabase.java
```

This fails because our `CDatabaseRoot extends Persistent`, not `RootObject`.

## What We've Tried

1. **CDatabaseRoot extends RootObject** - FAILED: `RootObject` is package-private in `org.garret.perst.continuous`

2. **Created CDatabaseRootWrapper** - Partially created in `oodb/` package to extend RootObject, but this created cascading issues with PerstStorageManager and other code expecting CDatabaseRoot

## Key Learnings

1. **Perst RootObject is package-private** - Cannot extend from outside `org.garret.perst.continuous` package

2. **CDatabase manages its own RootObject** - Looking at bytecode, CDatabase.open():
   - Gets/creates its own `RootObject` internally
   - Uses it to track table descriptors for versioning
   - Does NOT require user to provide a special root

3. **Simplest path likely:** Don't set Storage root before calling CDatabase.open(). Let CDatabase create its own RootObject internally.

## Suggested Next Steps

### Option A: Don't Pre-set Root (Simplest)
In PerstStorageManager.initialize():
```java
// Create storage
Storage storage = createStorage();

// DON'T set root here - let CDatabase handle it
// Or set it AFTER CDatabase.open()

// Initialize CDatabase if enabled
if (PerstConfig.getInstance().isUseCDatabase()) {
    CDatabase database = new CDatabase();
    String indexPath = PerstConfig.getInstance().getDatabasePath() + ".idx";
    database.open(storage, indexPath);
    // CDatabase creates its own RootObject internally
}
```

### Option B: Check Existing Code
Look at how Perst examples initialize CDatabase - they may not set a custom root at all.

### Option C: Use Standard Storage Mode
Disable CDatabase temporarily and test with standard Storage to verify the basic Perst integration works.

## Files Modified

- `src/main/precompiled/oodb/PerstStorageManager.java` - CDatabase init logic (may need rollback)
- `src/main/backend/application.ini` - PerstUseCDatabase = true
- `src/main/precompiled/mycompany/database/ActorManager.java` - Added getByUserId()
- `src/test/core/oodb/CDatabaseVersioningTest.java` - Test (needs Perst running)
- `bld` - Fixed classpath for Linux
- `src/main/precompiled/oodb/CDatabaseRootWrapper.java` - NEW (may need deletion)

## Testing

To test when fixed:
```bash
./bld develop  # Start server
# Check logs for: "[PerstStorageManager] CDatabase initialized with Lucene index at: data/oodb.idx"
```

Or run test manually:
```bash
# Copy application.ini to work/exploded/
cp src/main/backend/application.ini work/exploded/
cd work/exploded
java -cp "WEB-INF/classes:../../libs/*" oodb.CDatabaseVersioningTest
```

## Notes for Next Agent

- The LSP errors in IDE are FALSE POSITIVES - Perst JARs aren't in IDE classpath but compile fine with javac
- The bytecode analysis of CDatabase.open() shows it creates/manages its own RootObject
- Don't overcomplicate - CDatabase likely doesn't need a custom root object
