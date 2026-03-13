# Working Rules

> **IMPORTANT:** These rules must appear at the top of every plan, todolist, and progress document.

---

# Architecture Documentation - Perst OODBMS

## Overview

Perst is an embedded Object-Oriented Database Management System (OODBMS) for Java. It provides direct persistence of Java objects without requiring object-relational mapping or SQL.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Code                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │  Database    │      │    Query     │      │    Index     │  │
│  │   (SQL API)  │      │   (JSQL)     │      │   Manager    │  │
│  └──────┬───────┘      └──────┬───────┘      └──────┬───────┘  │
│         │                      │                      │          │
│  ┌──────▼──────────────────────▼──────────────────────▼───────┐ │
│  │                    Storage Interface                         │ │
│  │         (org.garret.perst.Storage)                          │ │
│  └──────────────────────────┬──────────────────────────────────┘ │
│                             │                                     │
│  ┌──────────────────────────▼──────────────────────────────────┐ │
│  │              StorageImpl - Core Implementation                │ │
│  │  (org.garret.perst.impl.StorageImpl) - 225KB                │ │
│  ├──────────────────────────────────────────────────────────────┤ │
│  │  • Page Pool Management                                      │ │
│  │  • Object Cache (LRU)                                       │ │
│  │  • Transaction Manager                                       │ │
│  │  • Class Descriptor Registry                                 │ │
│  │  • B-tree Index Implementation                               │ │
│  └──────────────────────────┬──────────────────────────────────┘ │
│                             │                                     │
│  ┌──────────────────────────▼──────────────────────────────────┐ │
│  │                      Page Pool                               │ │
│  │  - Cached database pages in memory                          │ │
│  - Configurable size (default 4MB)                              │ │
│  - Infinite pool option (all pages in memory)                  │ │
│  └──────────────────────────┬──────────────────────────────────┘ │
│                             │                                     │
│  ┌──────────────────────────▼──────────────────────────────────┐ │
│  │                    IFile Interface                           │ │
│  │  - MappedFile, CompressedFile, RC4File, NullFile            │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Database File (.dat)                         │
│  ┌────────────┬────────────┬────────────┬────────────┐        │
│  │   Header   │  Bitmap    │  Index     │  Data      │        │
│  │  (512B)    │  Pages     │  Pages     │  Pages     │        │
│  └────────────┴────────────┴────────────┴────────────┘        │
└──────────────────────────────────────────────────────────────────┘
```

---

## Core Components

### 1. Storage Layer

#### Storage Interface (`org.garret.perst.Storage`)
Main entry point for database operations:

| Method | Description |
|--------|-------------|
| `open(String, long)` | Open database file |
| `getRoot()` / `setRoot()` | Access root object |
| `commit()` / `rollback()` | Transaction control |
| `createXXX()` | Create persistent collections |
| `getClass()` | Get class descriptor |

#### StorageImpl (`org.garret.perst.impl.StorageImpl`)
- **Size**: 225KB - main implementation class
- **Responsibilities**:
  - Page allocation and deallocation via bitmap
  - Object caching (LRU cache)
  - Transaction management
  - B-tree index operations
  - Class descriptor handling

### 2. Database Layer (`org.garret.perst.Database`)
- Provides relational database emulation on top of Perst
- Automatic table/index creation via `@Indexable` annotation
- JSQL query support
- Schema management

### 3. Index Implementations

Perst provides multiple index types in `org.garret.perst.impl`:

| Index Type | Class | Use Case |
|------------|-------|----------|
| **B-tree** | `Btree`, `AltBtree` | General purpose, range queries |
| **R-tree** | `Rtree`, `RtreeR2`, `RtreeRn` | Spatial data (2D, nD) |
| **T-tree** | `Ttree` | In-memory time series |
| **KD-tree** | `KDTree` | Multidimensional point data |
| **Patricia Trie** | `PTrie` | Prefix-based string search |
| **Bitmap** | `BitIndexImpl` | Set membership, enumeration |
| **Hash** | `PersistentHashImpl` | Key-value lookups |
| **Regex** | `RegexIndexImpl` | Regular expression search |

#### B-tree Variants
- **Btree**: Standard B-tree
- **AltBtree**: Alternative implementation
- **RndBtree**: Randomized B-tree for concurrency
- **BtreeCompoundIndex**: Multi-field indexes

### 4. Query System

#### QueryImpl (`org.garret.perst.impl.QueryImpl`)
- **Size**: 185KB - largest implementation file
- Supports JSQL (Java SQL) query language
- Query optimization
- Query profiling

#### Query Interface (`org.garret.perst.Query`)
- Type-safe query builder
- Parameter binding

### 5. Persistent Object Model

```
IPersistent (interface)
    │
    ├── Persistent (base class)
    │       │
    │       ├── IndexedCollection (B-tree based)
    │       ├── Link (array of OIDs)
    │       └── ...
    │
    ├── PinnedPersistent (non-evictable)
    │
    └── Object (java.lang.Object)
            └── Any user-defined persistent class
```

### 6. Collection Types

| Collection | Implementation | Description |
|------------|----------------|-------------|
| `PersistentSet` | `PersistentSet` | Set of unique objects |
| `PersistentList` | `PersistentListImpl` | Ordered list with index |
| `PersistentMap` | `PersistentMapImpl` | Key-value map |
| `Link` | `LinkImpl` | Array of OIDs |
| `TimeSeries` | `TimeSeriesImpl` | Time-ordered data |

### 7. Full-Text Search

Integration with Apache Lucene:
- `FullTextSearchable` - Mark classes for indexing
- `FullTextIndexImpl` - Manages Lucene index
- Supports complex text queries

### 8. Replication

| Component | Description |
|-----------|-------------|
| `ReplicationMasterStorage` | Master node |
| `ReplicationSlaveStorage` | Slave node |
| `ReplicationMasterFile` | Master file operations |
| `ReplicationSlaveStorageImpl` | Slave synchronization |

---

## Data Storage Format

### Database File Structure

```
┌────────────────────────────────────┐
│         Header (512 bytes)          │
│  - Format version                   │
│  - Root page pointers               │
│  - Bitmap extent                    │
│  - Object count                     │
├────────────────────────────────────┤
│         Bitmap Pages                │
│  - Free page tracking               │
│  - Allocation bitmap                │
├────────────────────────────────────┤
│         Index Pages                 │
│  - B-tree index pages               │
│  - Object handle table              │
├────────────────────────────────────┤
│         Data Pages                  │
│  - Object data                      │
│  - Variable-length fields           │
└────────────────────────────────────┘
```

### Page Size
- Default: 4KB per page
- Configurable via `Page.pageSize`

### Object Representation
- **Fixed fields**: Stored inline in data page
- **Variable fields**: Stored separately with pointer
- **OID (Object ID)**: 31-bit identifier
- **Handle**: Pointer to object in database

---

## Transaction Model

### Transaction Types

| Mode | Constant | Description |
|------|----------|-------------|
| Exclusive | `EXCLUSIVE_TRANSACTION` | Single thread, all operations |
| Read-Write | `READ_WRITE_TRANSACTION` | Alias for exclusive |
| Cooperative | `COOPERATIVE_TRANSACTION` | Shared transaction, synchronized commit |
| Read-Only | `READ_ONLY_TRANSACTION` | No modifications |

### Concurrency Control
- **Multiclient support**: Multiple application instances
- **Object locking**: Per-object pessimistic locking
- **Transaction isolation**: Serialized by default

### Recovery
- Write-ahead logging
- Automatic recovery on startup
- Backup/restore support

---

## Memory Management

### Page Pool
- **Default size**: 4MB
- **Infinite pool**: Load entire database in memory
- **LRU eviction**: Least Recently Used page removal

### Object Cache
- **Default size**: 1319 objects
- **Soft references**: Allow GC when memory pressure
- **Weak references**: Optional weak cache

### Allocation
- Bitmap-based allocation
- Allocation quantum: 32 bytes
- Extension quantum: 1MB

---

## Extension Points

### Custom IFile Implementations
- `NullFile` - In-memory database
- `CompressedFile` - On-disk compression
- `RC4File` - Encryption
- `MultiFile` - Large database (multiple files)

### Custom Serialization
- `CustomSerializable` - User-defined serialization
- `CustomSerializer` - External serializer

### Custom Allocator
- `CustomAllocator` - Memory allocation strategy

---

## Build Configuration

- **Java Version**: 25
- **Build Tool**: Maven
- **Key Dependencies**:
  - Lucene 9.11.0 (full-text search)
  - Javassist 3.29.2 (bytecode generation)

---

## Related Documentation

- [Testing Strategy](./testing_strategy.md)
- [Migration Guide](./todo_init.md)
- [API Reference](./src/main/java/org/garret/perst/)
