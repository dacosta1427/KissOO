# JSON Serialization Implementation Plan

**Date:** 2026-03-27
**Status:** In Progress
**Tagpoint:** `pre-cost-profile-refactor`
**Dependency:** ChangeNote submitted to Perst team (`ChangeNote-JSONSerialization-CVersion.md`)

---

## Overview

Implement generic JSON serialization for all Perst entities using reflection-based caching. This eliminates manual JSON mapping code and ensures serialization stays in sync with entity definitions.

**Premise:** Perst team will implement the API as described in the ChangeNote. We implement our version now, which will be compatible with their future implementation.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      CVersion (Perst Library)                    │
│                          (cannot modify)                         │
└─────────────────────────────────────────────────────────────────┘
                              △
                              │ extends
┌─────────────────────────────────────────────────────────────────┐
│                    KissCVersion (our base class)                 │
├─────────────────────────────────────────────────────────────────┤
│  toJSON()      → JsonSerializationCache.toJSON(this)            │
│  fromJSON(json) → JsonSerializationCache.fromJSON(this, json)   │
└─────────────────────────────────────────────────────────────────┘
                              △
                              │ extends
┌─────────────────────────────────────────────────────────────────┐
│                    All Domain Entities                           │
│  Cleaner, House, Booking, Schedule, Owner, CostProfile, etc.    │
└─────────────────────────────────────────────────────────────────┘
```

---

## New Files to Create

### 1. Annotations

| File | Purpose |
|------|---------|
| `src/main/precompiled/oodb/annotations/JsonIgnore.java` | Exclude field from JSON |
| `src/main/precompiled/oodb/annotations/JsonIncludeObject.java` | Serialize OO reference inline |

### 2. Core Classes

| File | Purpose |
|------|---------|
| `src/main/precompiled/oodb/KissCVersion.java` | Base class with toJSON()/fromJSON() |
| `src/main/precompiled/oodb/JsonSerializationCache.java` | Reflection cache & serialization |

---

## Files to Modify

| File | Change |
|------|--------|
| `KissInit.groovy` | Add `JsonSerializationCache.initialize(...)` |
| `Cleaning.groovy` | Use `JsonSerializationCache.toJSON()` instead of manual JSON |

---

## Implementation Steps

### Step 1: Create Annotations

```java
// oodb/annotations/JsonIgnore.java
package oodb.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIgnore {}
```

```java
// oodb/annotations/JsonIncludeObject.java
package oodb.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIncludeObject {}
```

### Step 2: Create JsonSerializationCache

```java
// oodb/JsonSerializationCache.java
package oodb;

public class JsonSerializationCache {
    private static final Map<Class<?>, EntityMetadata> cache = new ConcurrentHashMap<>();
    
    public static void initialize(Class<?>... entityClasses)
    public static JSONObject toJSON(Object entity)
    public static <T> T fromJSON(T entity, JSONObject json)
    public static JSONArray toJSONArray(Collection<?> entities)
}
```

### Step 3: Use JsonSerializationCache Directly (until CVersion gets toJSON())

Since CVersion will eventually get `toJSON()` from Perst team, we use the static method directly for now:

```java
// In services, use:
JsonSerializationCache.toJSON(entity)
JsonSerializationCache.toJSONArray(entities)
```

### Step 4: Update KissInit.groovy

```groovy
// After Perst initialization
if (PerstStorageManager.isAvailable()) {
    JsonSerializationCache.initialize(
        Cleaner.class, House.class, Booking.class, Schedule.class,
        Owner.class, CostProfile.class, PerstUser.class, Actor.class,
        Agreement.class, Phone.class, Group.class, BenchmarkData.class
    )
}
```

### Step 5: No Entity Changes Needed

Entities continue to extend CVersion. No changes required.
The JSON serialization is handled by `JsonSerializationCache.toJSON(entity)`.

### Step 6: Simplify Services

```groovy
// Before
void getCleaners(...) {
    JSONArray rows = new JSONArray()
    for (Cleaner c : perst.getAll(Cleaner)) {
        JSONObject row = new JSONObject()
        row.put("id", c.getOid())
        row.put("name", c.getName())
        // ... 10 more lines
        rows.put(row)
    }
    outjson.put("data", rows)
}

// After
void getCleaners(...) {
    outjson.put("data", JsonSerializationCache.toJSONArray(perst.getAll(Cleaner)))
}
```

---

## Usage in Entities

No changes needed to entities. They continue to extend CVersion.

Optional annotations can be added for special handling:

```java
public class House extends CVersion {  // No change!
    private String name;
    private String address;
    
    private Owner owner;                    // → "owner": 4242 (OID by default)
    
    @JsonIncludeObject  // Optional: serialize inline
    private CostProfile costProfile;        // → "costProfile": {name: "Premium", ...}
    
    @JsonIgnore  // Optional: exclude from JSON
    private String internalNotes;
    
    // No toJSON() needed - handled by JsonSerializationCache
}
```

---

## Performance Expectations

| Operation | First Call | Cached Calls |
|-----------|------------|--------------|
| toJSON() | ~1-2ms | ~0.01ms |
| fromJSON() | ~1-2ms | ~0.01ms |
| toJSONArray(100 items) | ~10ms | ~1ms |

---

## Testing Plan

1. **Unit Tests**
   - Round-trip: entity → JSON → entity
   - @JsonIgnore exclusion
   - @JsonIncludeObject inline serialization
   - Collection serialization

2. **Integration Tests**
   - API endpoints return correct JSON
   - fromJSON updates existing entities
   - OO references serialize as OIDs

3. **Performance Tests**
   - Benchmark serialization speed
   - Memory usage with many entity types

---

## Migration Order

1. ✅ Create annotations (JsonIgnore, JsonIncludeObject) in oodb.annotations
2. ✅ Create JsonSerializationCache in oodb package
3. ✅ Update KissInit.groovy with initialization
4. ⏳ Simplify Cleaning.groovy methods (can be done incrementally)

**Note:** When Perst team adds `toJSON()` to CVersion, we can simplify further:
- `entity.toJSON()` instead of `JsonSerializationCache.toJSON(entity)`

---

## Benefits

| Before | After |
|--------|-------|
| 15+ lines per entity method | 1 line per entity method |
| Manual field listing | Automatic via reflection |
| Must update when fields change | Always in sync |
| Inconsistent serialization | Consistent JSON output |

---

**Status:** Implementation in progress
**Next:** Create annotations and core classes
