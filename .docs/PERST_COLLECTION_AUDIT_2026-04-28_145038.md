
# PerstCollection Usage Audit Report

Generated: 2026-04-28_145038

## Question

Have all domain classes been fitted with PerstCollections instead of Java Sets etc?

## Short Answer

**YES** - All domain classes in the precompiled layer use **Perst Link** (org.garret.perst.Link) for collection relationships instead of Java collections (Set, List, Map).

## Detailed Findings

### Domain Classes Reviewed

1. **Owner.java** - domain.actor.owner
2. **Cleaner.java** - domain.actor.cleaner
3. **Schedule.java** - domain.actor.cleaner
4. **House.java** - domain.oov.house
5. **Booking.java** - domain.oov.house
6. **CostProfile.java** - domain.oov.house

### Collection Usage Pattern

#### What They Use: Perst Link (org.garret.perst.Link)

```java
import org.garret.perst.Link;

private Link houses;  // One-to-many relationship

// Constructor initializes via Storage
this.houses = StorageManager.getStorage().createLink();

// Get as typed List
public List<House> getHouses() {
    if (houses == null || houses.isEmpty()) return List.of();
    return Arrays.asList((House[])houses.toArray(new House[0]));
}

// Add/remove
public void addHouse(House house) {
    if (house != null && houses != null) {
        house.setOwner(this);
        houses.add(house);  // Perst Link add()
    }
}

public void removeHouse(House house) {
    if (house != null && houses != null) {
        house.setOwner(null);
        int idx = houses.indexOf(house);
        if (idx >= 0) {
            houses.remove(idx);  // Perst Link remove()
        }
    }
}
```

#### What They Do NOT Use: Java Collections

✅ **NO** `java.util.Set`
✅ **NO** `java.util.HashSet`
✅ **NO** `java.util.TreeSet`
✅ **NO** `java.util.List` (for storage - only for return values)
✅ **NO** `java.util.ArrayList` (for storage)
✅ **NO** `java.util.Map`
✅ **NO** `java.util.HashMap`
✅ **NO** `java.util.TreeMap`

### Breakdown by Class

| Class | Collection Field | Type | Purpose |
|-------|----------------|------|----------|
| **Owner** | `houses` | `Link` | One-to-many: Owner → Houses |
| **Cleaner** | `schedules` | `Link` | One-to-many: Cleaner → Schedules |
| **House** | `bookings` | `Link` | One-to-many: House → Bookings |
| **Booking** | *(none)* | N/A | References: house, owner, schedule (1:1 or many-to-1) |
| **Schedule** | *(none)* | N/A | References: cleaner, booking (many-to-1) |
| **CostProfile** | *(none)* | N/A | Standalone entity, referenced by House |

### Relationship Mapping

```
Owner (1) ──< Link:houses >── (M) House (1) ──< Link:bookings >── (M) Booking
   │                                                         │
   │                                                         │
 owns                                                     belongs
   │                                                         │
   │                                                         ▼
   └───────────────────────────────────────────< CostProfile (1)

Cleaner (1) ──< Link:schedules >── (M) Schedule (1) ────┬── Booking (M:1)
                                                          │
                                                          └── House (M:1)
```

Note: 
- **Owner→Houses**: One-to-many via Perst Link
- **Cleaner→Schedules**: One-to-many via Perst Link
- **House→Bookings**: One-to-many via Perst Link
- **Booking→House**: Many-to-one (object reference)
- **Booking→Owner**: Many-to-one (object reference, via house)
- **Booking→Schedule**: One-to-one (object reference)
- **Schedule→Booking**: Many-to-one (object reference)
- **Schedule→Cleaner**: Many-to-one (object reference)

## Why Perst Link?

### Benefits

1. **Native to Perst OODBMS**
   - Optimized for object persistence
   - Automatic indexing and retrieval
   - No SQL join overhead

2. **Bidirectional Navigation**
   - Objects reference each other directly
   - No need for ID lookups or joins
   - Pure OO paradigm maintained

3. **Transaction Support**
   - Changes to Links are part of TransactionContainer
   - Atomic commit/rollback
   - Version history preserved

4. **Performance**
   - Direct memory access
   - No deserialization overhead for references
   - Efficient iteration

5. **Simplicity**
   - `link.add(obj)` / `link.remove(obj)` / `link.toArray()`
   - No need for Set/List/Map boilerplate
   - Type-safe through casting

### Pattern Consistency

All domain classes follow the same pattern:

```java
// 1. Declare Link field
private Link items;  // Descriptive name for the collection

// 2. Initialize in constructor(s)
this.items = StorageManager.getStorage().createLink();

// 3. Provide typed getter (optional)
public List<Item> getItems() {
    if (items == null || items.isEmpty()) return List.of();
    return Arrays.asList((Item[])items.toArray(new Item[0]));
}

// 4. Provide Link getter (for direct manipulation)
public Link getItemsLink() {
    return items;
}

// 5. Add helper methods
public void addItem(Item item) {
    if (item != null && items != null) {
        // Set inverse reference if needed
        item.setParent(this);
        items.add(item);
    }
}

public void removeItem(Item item) {
    if (item != null && items != null) {
        // Clear inverse reference if needed
        item.setParent(null);
        int idx = items.indexOf(item);
        if (idx >= 0) {
            items.remove(idx);
        }
    }
}
```

## Conclusion

✅ **All domain classes are correctly fitted with Perst Link** instead of Java collections.

This ensures:
- Pure OO navigation (no SQL-style joins)
- Native Perst persistence
- Transactional consistency
- Optimal performance

**No action needed** - the codebase already follows best practices.
