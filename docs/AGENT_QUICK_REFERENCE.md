# Quick Reference Card for AI Agents

## ⚡ Fast Facts

- **Project:** Perst - Object-Oriented Database for Java
- **Main API:** `CDatabase` (singleton via `CDatabase.instance`)
- **Branch:** `master`
- **Version:** 5.1.0-SNAPSHOT
- **Java Target:** OpenJDK 25

---

## 🚀 Initialization Pattern

This is the ONLY correct way to initialize Perst:

```java
// 1. Create storage
Storage storage = StorageFactory.getInstance().createStorage();
storage.open("mydb.dbs");

// 2. Get CDatabase singleton and open
CDatabase cdb = CDatabase.instance;
cdb.open(storage, "mydb_index");  // null for internal Lucene, or path to external

// Now use cdb for all operations...
```

**Key points:**
- Always use `CDatabase.instance` (singleton)
- Call `open()` AFTER opening storage
- First parameter is opened Storage, second is Lucene index path (can be null)

---

## 📦 Adding New Entity Classes

```java
public class MyEntity extends CVersion {
    @Indexable                    // Creates B-tree index on "name"
    private String name;
    
    @Indexable(unique=true)      // Unique B-tree index
    private String code;
    
    @FullTextSearchable          // Lucene full-text index
    private String description;
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**Key points:**
- **Indexes are auto-created on first use** - no manual registration needed
- `@Indexable` creates B-tree index (fast lookup by exact value or range)
- `@FullTextSearchable` creates Lucene index (text search with AND/OR/NOT)
- All fields should have getters/setters for proper serialization

---

## 🔑 Critical Code Patterns

### Always Use This Pattern for Updates
```java
// CORRECT - working copy
StoreResult result = cdb.update(oid, obj -> {
    obj.setName("new");
});
if (result.isConflict()) {
    // Handle conflict - someone else updated it
}

// WRONG - direct modification
MyObj obj = cdb.getByOid(oid);
obj.setName("new");  // Modifies history directly!
```

### TransactionContainer Usage (Batch Operations)
```java
TransactionContainer tc = cdb.createContainer();
tc.addInsert(newObj);        // Insert new object (no OID yet)
tc.addUpdate(existingObj);   // Update existing (auto-captures version)
tc.delete(objToDelete);      // Mark for deletion
StoreResult result = cdb.store(tc);

if (result.isSuccess()) {
    // ALL OBJECTS NOW HAVE OIDs! (automatic assignment)
    newObj.getOid();  // e.g., 12345
    existingObj.getOid();  // Already had OID
}
```

**IMPORTANT:** After successful `store()`, all objects in the TC have their OIDs assigned automatically. New objects that had `getOid() == 0` will now have valid OIDs. This enables the Manager pattern:

```java
// Manager creates objects, submits, and uses immediately
TransactionContainer tc = cdb.createContainer();
for (String name : namesToCreate) {
    tc.addInsert(new Company(name, address));
}
cdb.store(tc);  // Objects now have OIDs!
// Manager can use objects directly - OIDs are set!

### Simple Transaction Pattern
```java
cdb.beginTransaction();
cdb.insert(myObject);
cdb.commitTransaction();
```

---

## 🎯 Key Commands

```bash
# Compile
mvn compile

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=org.garret.perst.continuous.TestName

# Run multiple tests
mvn test -Dtest="org.garret.perst.continuous.Test*,org.garret.perst.smart.*"

# Push to remote
git push origin master
```

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `src/main/java/org/garret/perst/continuous/CDatabase.java` | Main API - use this for all DB operations |
| `src/main/java/org/garret/perst/continuous/TransactionContainer.java` | Batch operations |
| `src/main/java/org/garret/perst/continuous/StoreResult.java` | Store operation results |
| `src/main/java/org/garret/perst/Indexable.java` | Annotation for indexed fields |
| `src/main/java/org/garret/perst/continuous/CVersion.java` | Base class for all persistent objects |
| `docs/guides/CDatabase-API-Guide.md` | Full API documentation |
| `docs/guides/DEVELOPMENT_GUIDE.md` | Development guidelines |

---

## 🚨 Common Mistakes to Avoid

1. **Don't use `dbmanager` package** - it was deleted in v4.1.0
2. **Don't modify retrieved objects directly** - use `update(oid, fn)` or `update()` method
3. **Don't call `searchFullText()`** - use `fullTextSearch(query, limit)`
4. **Don't forget to commit transactions** - insert/update without commit is not persisted
5. **Don't use Storage directly for queries** - always go through `CDatabase.instance`

---

## 🧪 Test Base Class

```java
public class MyTest extends AbstractCDatabaseTest {
    // Automatic setup (cdb field available):
    // - Fresh CDatabase instance (cdb)
    // - Fresh Storage instance (storage)
    // - Unique file paths per test
    // - Automatic cleanup after each test
}
```

---

## ⚠️ CRITICAL: Iterator Memory Leak Prevention

**Perst uses `TrackedIterator` to detect unclosed iterators.** This is NOT optional cleanup - it prevents memory leaks in production databases with 1Q+ objects.

### The Problem

`TrackedIterator` only calls `iteratorClosed()` when `hasNext()` returns `false`. Methods like `toList()` and `toArray()` consume iterators without calling `hasNext()`.

### The Solution: ALWAYS use `exhaust(iter)`

```java
// WRONG - causes memory leak warning
IterableIterator<Entity> iter = cdb.find(Entity.class, "name", new Key("test"));
List<Entity> list = cdb.toList(iter);  // Consumes iterator but doesn't close it!

// CORRECT - exhaust the iterator
IterableIterator<Entity> iter = cdb.find(Entity.class, "name", new Key("test"));
List<Entity> list = cdb.toList(iter);
exhaust(iter);  // Call hasNext() to trigger iteratorClosed()
```

### When to Call `exhaust(iter)`

| Pattern | Need exhaust()? | Reason |
|---------|-----------------|--------|
| `cdb.find()` then `iter.hasNext()` | Yes | If you don't consume to end |
| `cdb.find()` then `iter.next()` exactly once | **YES** | Must consume remaining items |
| `cdb.getRecords()` | **YES** | Always, even if empty |
| `cdb.toList(iter)` | **YES** | Doesn't call hasNext() internally |
| `cdb.toArray(iter)` | **YES** | Doesn't call hasNext() internally |
| `while (iter.hasNext()) { iter.next(); }` | **YES** | Only closes when loop exits via hasNext() |
| `cdb.select()` | **YES** | BUT may still leak - see Known Issues |

### Common Test Mistakes

```java
// MISTAKE 1: Forgot to exhaust after exact next()
@Test
void badTest() {
    cdb.insert(entity);
    IterableIterator<Entity> iter = cdb.find(...);
    assertEquals(1, iter.next());  // Iterator not exhausted!
}

// MISTAKE 2: Using toList/toArray without exhaust
@Test
void badTest2() {
    IterableIterator<Entity> iter = cdb.find(...);
    List<Entity> list = cdb.toList(iter);  // Leak!
}

// MISTAKE 3: Multiple iterators in one test
@Test
void badTest3() {
    iter1 = cdb.find(...);  // Creates iter1
    iter2 = cdb.find(...);  // Creates iter2
    // Forgot to exhaust either!
}
```

### Known Issues

#### `cdb.select()` Iterator Leak (FIXED)

**Status:** Fixed - was a bug introduced when adding `TrackedIterator` tracking.

The `select()` method called `getRecords()` which created a `TrackedIterator`, then passed it to `execute()`. But `execute()` could replace the iterator with an `IndexIterator`, abandoning the `TrackedIterator`.

**Fix:** Modified `select()` to use `getRecordsInternal()` (non-tracking) and explicitly manage `TrackedIterator` lifecycle.

### Verifying Tests Are Clean

```bash
# Run tests and check for leak warnings
mvn test 2>&1 | grep "leaked resources"

# Should see NO warnings like:
# WARNING: Closing database with potentially leaked resources: ResourceMetrics{openIter=N}

# Exception: select() tests may still show warnings due to Perst bug
```

### Available Helper in AbstractCDatabaseTest

```java
protected <T> void exhaust(IterableIterator<T> iter) {
    while (iter.hasNext()) {
        iter.next();
    }
}
```

For tests NOT extending `AbstractCDatabaseTest`, use inline pattern:
```java
while (iter.hasNext()) iter.next();
```

---

## 📊 Smart Index Confidence

| Level | Meaning | Auto-create |
|-------|---------|-------------|
| HIGH | Clear signal, safe | Yes (with dry run) |
| MEDIUM | Likely beneficial | No |
| LOW | Weak signal | No |

---

## 🔧 Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `IllegalStateException` after close | State is CLOSED | Reopen database |
| NPE on transaction | Root is null after close | Check state before ops |
| ConflictException | Version mismatch | Re-fetch and retry |
| Memory leak warning | Unclosed iterators | Use `exhaust(iter)` helper |
| NoSuchIndexException | Field not indexed | Add `@Indexable` annotation |

---

## Git Workflow

```bash
# Before making changes
git checkout master
git pull origin master

# Commit with clear message
git add -A
git commit -m "type: description

Details:
- What changed
- Why it changed"

# Push
git push origin master
```

---

## 📚 Documentation Protocol

### JAR-Embedded vs Source Documentation

**IMPORTANT:** Documentation exists in TWO locations:

| Location | Purpose | Update Command |
|----------|---------|---------------|
| `docs/guides/` | Source documentation | Edit directly |
| `src/main/resources/docs/` | Bundled in JAR | **Sync from docs/guides/** |

### When to Update Documentation

1. **API changes** - Update both locations
2. **New features** - Add to `docs/guides/` then sync
3. **Bug fixes in patterns** - Update in both places

### How to Sync Documentation

After updating `docs/guides/*.md`, sync to JAR resources:

```bash
# Sync all docs to JAR
cp docs/guides/*.md src/main/resources/docs/

# Or sync specific file
cp docs/guides/AGENT_QUICK_REFERENCE.md src/main/resources/docs/
```

### Maven Build

The `src/main/resources/docs/` directory is automatically included in the JAR during `mvn package`.

```bash
# Build JAR (includes docs)
mvn package -DskipTests

# Verify docs are in JAR
jar tf target/perst-dcg-*.jar | grep "^docs/"
```

### Checklist for Documentation Updates

- [ ] Updated `docs/guides/` source files
- [ ] Synced to `src/main/resources/docs/`
- [ ] Built JAR and verified docs exist
- [ ] Committed both locations

---

*Last updated: March 2026*
