# Perst Transaction and Threading Model

## Overview

Perst in KissOO uses a single shared Storage instance with thread-local transactions to achieve both isolation and efficiency in a multi-user environment.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   MainServlet Environment                   │
│         (stores single Perst Storage instance)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    PerstStorageManager                      │
│  - getStorage()  → returns shared Storage                   │
│  - beginTransaction() → thread-local transaction            │
│  - commitTransaction() → commits + ends thread transaction  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Perst Storage (shared, thread-safe)            │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Thread-local Transaction Context                    │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐               │    │
│  │  │Thread-1 │ │Thread-2 │ │Thread-N │  ...          │    │
│  │  │TX-A     │ │TX-B     │ │TX-N     │               │    │
│  │  └─────────┘ └─────────┘ └─────────┘               │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Data Indexes (userIndex, actorIndex, etc.)         │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Transaction Flow

### Per Service Request

```groovy
// 1. Service call starts
// 2. PerstStorageManager.beginTransaction() is called
service MyService {
    def doSomething() {
        PerstStorageManager.beginTransaction()  // → EXCLUSIVE transaction
        
        try {
            // Perform database operations
            def user = PerstUserManager.getByKey(username)
            user.setLastLoginDate(now)
            PerstUserManager.update(user)
            
            PerstStorageManager.commitTransaction()
        } catch (e) {
            PerstStorageManager.rollbackTransaction()
        }
    }
}
// 3. Transaction ends - fully isolated from other threads
```

### Thread Isolation Explained

| Thread | Transaction | State | Isolated? |
|--------|-------------|-------|------------|
| Thread-1 | TX-1 | Active | Yes |
| Thread-2 | TX-2 | Active | Yes |
| Thread-1 | TX-1 | Committed | Yes |
| Thread-3 | TX-3 | Active | Yes |

Each thread's transaction is completely isolated:
- TX-1 commits → only TX-1's changes are visible
- TX-2 rolls back → only TX-2's changes are undone
- No cross-contamination between threads

## Why This Is Efficient

### Comparison of Approaches

| Approach | Pros | Cons |
|----------|------|------|
| **Current (Shared Storage + Thread TX)** | Single instance memory, fast | Thread overhead (minimal) |
| Separate Storage per request | Clean isolation | High memory, slow creation |
| Connection pool like SQL | Familiar pattern | Overhead for Perst (unnecessary) |

### Perst Best Practice

Perst is designed for exactly this pattern:

1. **Single Storage Instance** - Created once at startup, shared across all threads
2. **Thread-Local Transactions** - `beginThreadTransaction()` provides isolation without separate instances
3. **Lock-Free Reads** - Perst's B-Tree indexes support concurrent readers
4. **Exclusive Writes** - `EXCLUSIVE_TRANSACTION` ensures single-writer safety

From Perst documentation:
> "The recommended way to use Perst in multi-threaded applications is to use a single Storage instance and beginThreadTransaction() to mark transaction boundaries."

## Implementation Details

### PerstStorageManager

```java
public class PerstStorageManager {
    
    public static void beginTransaction() {
        Storage storage = getStorage();
        storage.beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
    }
    
    public static void commitTransaction() {
        Storage storage = getStorage();
        storage.commit();
        storage.endThreadTransaction();
    }
    
    public static void rollbackTransaction() {
        Storage storage = getStorage();
        storage.rollback();
        storage.endThreadTransaction();
    }
}
```

### Manager Usage

All data access goes through Managers (PerstUserManager, ActorManager):

```java
public class PerstUserManager {
    
    public static PerstUser create(Object... params) {
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.userIndex.put(user);
            PerstStorageManager.commitTransaction();
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            throw e;
        }
    }
}
```

## Summary

✅ **Isolation** - Each service request gets isolated transaction via `beginThreadTransaction(EXCLUSIVE)`

✅ **Efficiency** - Single shared Storage instance, no pool overhead, thread-local transactions

✅ **Multi-User** - Perst handles concurrent access internally, safe for hundreds of simultaneous users

This is the recommended Perst pattern for high-performance multi-threaded applications.
