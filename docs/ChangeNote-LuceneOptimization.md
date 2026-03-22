# Change Note: Fix Lucene Segment Explosion in CDatabase

## Problem
CDatabase stores ALL versions (current + history) in a single file-based Lucene index. This causes:
- File explosion with high version counts
- Performance degradation
- Database bloat

## Required Architecture: Split Lucene

**IMPORTANT:** This is NOT the current CDatabase behavior. We need a custom implementation:

### Architecture:
```
┌─────────────────────────────────────────────────────────┐
│                    Perst Database                        │
│  ┌───────────────────┐  ┌──────────────────────────┐  │
│  │ Current versions  │  │  History pointers        │  │
│  │ (in-DB, fast)     │  │  (reference to Lucene)  │  │
│  └───────────────────┘  └──────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│              File-based Lucene Index                    │
│  ┌─────────────────────────────────────────────────────┐│
│  │  Historical versions only (full-text indexed)      ││
│  │  Updated periodically, not real-time               ││
│  └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### Implementation Options:

**Option A: Custom CDatabase Extension**
Modify CDatabase to only index current versions in real-time, with periodic export of history to separate Lucene index.

**Option B: Two-Index Approach**
1. Keep current versions in Perst (fast access)
2. Separate process exports historical versions to file-based Lucene (nightly batch)

### Key Points:
- Current versions: IN Perst database (lightweight, fast)
- Historical versions: File-based Lucene (updated periodically, not real-time)
- DO NOT keep full history in-DB
