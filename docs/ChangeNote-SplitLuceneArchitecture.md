# Change Note: Split Lucene Architecture for CDatabase

## Problem
Current CDatabase stores ALL versions in file-based Lucene index:
- Real-time indexing of every version
- Index grows unbounded
- Performance degrades with history

## Proposed Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      Application                              │
├──────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌─────────────────┐         ┌─────────────────────────┐        │
│   │  Perst/CDatabase │         │   File-based Lucene    │        │
│   │                 │         │                         │        │
│   │  Current:       │  ──►   │   History:            │        │
│   │  - Full objects │         │   - Full-text only    │        │
│   │  - Fast access  │  sync   │   - Periodic export   │        │
│   │  - RW           │         │   - Read-only         │        │
│   └─────────────────┘         └─────────────────────────┘        │
│          │                              ▲                        │
│          │                              │                        │
│          ▼                              │                        │
│   ┌─────────────────┐                  │                        │
│   │  Query Router   │◄─────────────────┘                        │
│   │  - Check current│                                          │
│   │  - Fallback to │                                          │
│   │    history      │                                          │
│   └─────────────────┘                                          │
│                                                                   │
└──────────────────────────────────────────────────────────────┘
```

## Implementation Suggestion

### Option A: CDatabase Extension (Recommended)

1. **Extend CDatabase** to disable built-in full-text indexing
2. **Override insert/update** to only store in Perst
3. **Add export method** for historical versions:
   ```java
   public void exportHistoryToLucene(Class<?> clazz, String luceneIndexPath) {
       // Get all non-current versions
       // Write to separate Lucene index
       // Mark as exported
   }
   ```
4. **Query router** checks current first, then falls back to history Lucene

### Option B: Scheduled Batch Export

1. Keep CDatabase as-is for current
2. **Add scheduled job** (e.g., every hour):
   - Query for versions older than X hours
   - Export to file-based Lucene
   - Mark as "archived" (custom flag)
3. **Query** checks archived flag → routes to appropriate index

## Key Decisions Needed

1. **Sync frequency:** Real-time? Every hour? Nightly?
2. **Query behavior:** Always check current first? Or split API?
3. **Migration:** How to handle existing data?

## Changes Required

- New class: `LuceneHistoryManager` - handles history index
- Modify: `PerstStorageManager` - disable CDatabase Lucene, add export
- New: Scheduled job or API endpoint for history sync
