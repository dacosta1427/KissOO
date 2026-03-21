# Perst CDatabase Integration - Findings & Recommendations

## Executive Summary

The previous agent overcomplicated the Perst CDatabase integration. The CDatabase class (in `org.garret.perst.continuous`) handles most functionality automatically via annotations - no manual root object or index management needed.

---

## Key Findings

### 1. CDatabase Manages Its Own RootObject

**Location**: `org.garret.perst.continuous.CDatabase`

**What it does internally** (from `RootObject.java`):
- Creates its own internal `RootObject` for tracking versioned tables
- Stores `FieldIndex<TableDescriptor> tables` for managing table metadata
- Uses Lucene for full-text indexing via `PerstDirectory.PerstCatalogue`

**Usage from examples** (SimpleRelation.java, Bank.java):
```java
Storage storage = StorageFactory.getInstance().createStorage();
storage.open("database.dbs");
CDatabase db = new CDatabase();
db.open(storage, "indexPath");  // null = no Lucene, or path for Lucene index
```

**IMPORTANT**: No `storage.setRoot()` call needed - CDatabase handles this internally.

---

### 2. Automatic Indexing via Annotations

CDatabase automatically creates indexes based on annotations in your domain classes:

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Indexable` | B-tree index on field | `@Indexable(unique=true) private String name;` |
| `@Indexable(caseInsensitive=true)` | Case-insensitive index | |
| `@FullTextSearchable` | Lucene full-text search | `@FullTextSearchable private String description;` |

**Example from SimpleRelation.java**:
```java
class Company extends CVersion {
    @FullTextSearchable
    @Indexable(unique=true)
    private String name;

    @FullTextSearchable
    private Address location;
}

class Employee extends CVersion {
    @Indexable
    private CVersionHistory<Company> company;
}
```

---

### 3. CDatabase API Methods

| Method | Description |
|--------|-------------|
| `db.open(storage, indexPath)` | Open database with optional Lucene index |
| `db.beginTransaction()` | Start transaction |
| `db.commitTransaction()` | Commit transaction |
| `db.insert(object)` | Insert versioned object |
| `db.find(Class, field, Key)` | Find by index |
| `db.select(Class, query)` | JSQL query |
| `db.fullTextSearch(query, limit)` | Lucene search |
| `db.getRecords(Class)` | Get all records |
| `db.getSingleton(result)` | Get single result |

---

### 4. What Happened in Previous Implementation

The previous agent:
1. ❌ Created `CDatabaseRootWrapper extends RootObject` (RootObject is package-private - causes ClassCastException)
2. ❌ Manually created `FieldIndex` objects for each entity type
3. ❌ Tried to set Storage root before calling `CDatabase.open()`
4. ❌ Added `@Indexable` annotations to domain classes but never used them

**Why it failed**: Perst's `RootObject` is package-private in `org.garret.perst.continuous`. You cannot extend it from outside the package.

---

### 5. No DBManager Class

**Confirmed**: No `DBManager` or similar wrapper class exists in Perst JARs. You will need to supply your own.

---

## Recommended Implementation

### Option A: Use CDatabase Directly

Simplest approach - modify `PerstStorageManager.java`:

```java
public static synchronized void initialize() {
    if (initialized) return;
    
    Storage storage = createStorage();
    storage.open(dbPath, poolSize);
    
    // DON'T set root - let CDatabase handle it
    
    if (PerstConfig.getInstance().isUseCDatabase()) {
        CDatabase db = new CDatabase();
        String indexPath = PerstConfig.getInstance().getDatabasePath() + ".idx";
        db.open(storage, indexPath);
        MainServlet.putEnvironment(DATABASE_KEY, db);
    }
    
    initialized = true;
}

public static CDatabase getDatabase() {
    return (CDatabase) MainServlet.getEnvironment(DATABASE_KEY);
}
```

### Option B: Create DBManager Wrapper

Create your own `DBManager` class that wraps `CDatabase`:

```java
package oodb;

import org.garret.perst.continuous.CDatabase;

public class DBManager {
    private CDatabase db;
    
    public void initialize(Storage storage, String indexPath) {
        db = new CDatabase();
        db.open(storage, indexPath);
    }
    
    public void insert(Object obj) {
        db.beginTransaction();
        db.insert(obj);
        db.commitTransaction();
    }
    
    // ... wrap other methods as needed
}
```

---

## Domain Classes - Required Changes

Your domain classes need to extend `CVersion` (not `Persistent`) and use annotations:

```java
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

public class Actor extends CVersion {
    @Indexable
    private String name;
    
    @FullTextSearchable
    private String description;
    
    // Getters/setters...
}
```

---

## Files to Modify

1. **PerstStorageManager.java** - Remove root setting, use CDatabase directly
2. **Domain classes** - Add `@Indexable`/`@FullTextSearchable` annotations, extend `CVersion`
3. **Managers** - Use `CDatabase` API instead of manual indexes
4. **Delete** - `CDatabaseRootWrapper.java` (not needed)

---

## References

- Perst examples: `/home/dacosta/Projects/perstLatest/continuous/tst/`
- SimpleRelation.java - Full CRUD example with versioning
- Bank.java - Concurrency/transaction example
- CDatabase source: Not available (binary only)
- RootObject source: `/home/dacosta/Projects/perstLatest/continuous/src/org/garret/perst/continuous/RootObject.java`
- TableDescriptor source: `/home/dacosta/Projects/perstLatest/continuous/src/org/garret/perst/continuous/TableDescriptor.java`
