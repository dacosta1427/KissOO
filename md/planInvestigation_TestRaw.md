# Working Rules (must be included)

See [workingRules.md](./workingRules.md) for core operating principles

---

# Investigation Plan: TestRaw

## Test Information

| Property | Value |
|----------|-------|
| **Test Name** | TestRaw |
| **Test File** | `tst/TestRaw.java` |
| **Error** | `NoSuchMethodException: ListItem.<init>()` |
| **Issue Type** | Missing No-Arg Constructor |
| **Status** | FAILED |

---

## Problem Analysis

### Error Details
The test fails with a `NoSuchMethodException` indicating that Perst cannot instantiate the `ListItem` class because it lacks a no-argument constructor.

### Root Cause
Perst is an Object-Oriented Database that uses Java serialization/deserialization mechanisms. When loading objects from the database, Perst needs to instantiate classes. The `ListItem` class currently has only one constructor:

```java
class ListItem {
    int id;

    ListItem(int id) { 
        this.id = id;
    }
}
```

Perst requires persistent-capable classes to have a **no-argument constructor** for object instantiation during database loading.

### Code Context

The `ListItem` class is used within an `ArrayList` that is stored as part of the `TestRaw` persistent root:

```java
class ListItem {
    int id;

    ListItem(int id) { 
        this.id = id;
    }
}

public class TestRaw extends Persistent { 
    L1List list;
    ArrayList<ListItem> array;  // <-- Uses ListItem
    Object  nil;
    // ...
}
```

---

## Investigation Steps

### Step 1: Verify Root Cause
- [ ] Confirm the error occurs during database read (not write)
- [ ] Check if `ListItem` needs to extend `Persistent` or be `Serializable`
- [ ] Review Perst documentation for custom class requirements

### Step 2: Proposed Solution
Add a no-argument constructor to the `ListItem` class:

```java
class ListItem {
    int id;

    // No-arg constructor required by Perst
    ListItem() {
    }

    ListItem(int id) { 
        this.id = id;
    }
}
```

### Step 3: Test the Fix
- [ ] Modify `tst/TestRaw.java` to add the no-arg constructor
- [ ] Run the test: `./run_tests_quick.sh TestRaw`
- [ ] Verify the test passes
- [ ] Ensure database file is cleaned up between runs (`rm -f testraw.dbs`)

### Step 4: Verify No Regressions
- [ ] Check if `L1List` class has similar issues (also used in TestRaw)
- [ ] Ensure the test database is properly closed after execution

---

## Related Classes

| Class | Issue | Action Required |
|-------|-------|-----------------|
| `ListItem` | Missing no-arg constructor | Add `ListItem()` constructor |
| `L1List` | Check for similar issue | Verify if no-arg constructor needed |

---

## Success Criteria

- [ ] TestRaw runs without `NoSuchMethodException`
- [ ] Test outputs "Database is OK"
- [ ] No regressions in other tests

---

## Rollback Plan

| Element | Details |
|---------|---------|
| **Checkpoint** | Git commit before changes or backup original `tst/TestRaw.java` |
| **Revert command** | `git checkout tst/TestRaw.java` or restore from backup |
| **Verification** | Re-run test and confirm original error returns |
| **Impact** | Minimal - single test file change |

---

## Notes

- The `L1List` class also has only a parameterized constructor but may not trigger the same error if it's not directly serialized by Perst (it's a transient linked structure)
- This is a common pattern in Perst tests - classes used in persistent collections need proper constructors
- Consider if `ListItem` should also implement `Serializable` or extend `Persistent` for full compatibility

---

## References

- [Perst Documentation](doc/perst.html)
- [TestRaw.java](../tst/TestRaw.java)
- [tracking_tests.md](./tracking_tests.md)
