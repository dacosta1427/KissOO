# Cost Profile Feature & OO Refactoring Plan

**Created**: 2026-03-27  
**Tagpoint**: `pre-cost-profile-refactor`  
**Status**: In Progress

---

## Executive Summary

This plan covers:
1. **Add CostProfile feature** for calculating cleaning costs
2. **Fix ALL dinosaur patterns** - replace ID-based relationships with proper OO object references

---

## Phase A: Create CostProfile Backend

### A1. Create CostProfile.java Entity
```java
// File: src/main/precompiled/mycompany/domain/CostProfile.java
public class CostProfile extends CVersion {
    @Indexable
    private String name;                    // "Standard", "Custom Beach House"
    
    @Indexable
    private boolean isStandard;             // Only one standard profile
    
    private Owner owner;                    // null = global, else owner-specific
    
    // Base rates
    private double baseHourlyRate;          // €/hour (default 25)
    private double minimumCharge;           // € minimum (default 75)
    
    // Size factors
    private double ratePerM2;               // € per m² (default 0.15)
    private double ratePerFloor;            // € per extra floor (default 15)
    private double ratePerBedroom;          // € per bedroom (default 10)
    private double ratePerBathroom;         // € per bathroom (default 15)
    
    // Special
    private double dogSurcharge;            // € flat fee (default 20)
    
    // Luxury multipliers
    private double basicMultiplier;         // default 1.0
    private double standardMultiplier;      // default 1.0
    private double premiumMultiplier;       // default 1.25
    private double luxuryMultiplier;        // default 1.5
    
    private boolean active;
}
```

### A2. Create CostProfileManager.java
```java
// File: src/main/precompiled/mycompany/database/CostProfileManager.java
// Standard manager pattern with:
// - getById(Oid)
// - getAll()
// - insert(CostProfile)
// - update(CostProfile)
// - delete(CostProfile)
// - getStandard() - returns the standard profile
```

### A3. Create CostService.java
```java
// File: src/main/backend/services/CostService.java
// Static methods:
// - calculateCost(House house, Booking booking, CostProfile profile)
// - estimateTime(House house)
// - getDefaultProfile()
```

### A4. Add CostProfile CRUD to Cleaning.groovy
- getCostProfiles
- getCostProfile
- createCostProfile
- updateCostProfile
- deleteCostProfile
- calculateCost

### A5. Add house cost fields to House.java
```java
private double surfaceM2;
private int floors;
private int bedrooms;
private int bathrooms;
private String luxuryLevel;  // basic|standard|premium|luxury
private CostProfile costProfile;  // Object reference
```

---

## Phase B: Fix ALL Dinosaur Patterns

### B1. Fix House.java
```java
// FROM:
private long ownerId;

// TO:
private Owner owner;  // Use house.getOwner()?.getOid() for API
```

### B2. Fix Booking.java
```java
// FROM:
private int houseId;

// TO:
private House house;  // Use booking.getHouse()?.getOid() for API
```

### B3. Fix Schedule.java
```java
// FROM:
private int cleanerId;
private int bookingId;

// TO:
private Cleaner cleaner;  // Use schedule.getCleaner()?.getOid() for API
private Booking booking;  // Use schedule.getBooking()?.getOid() for API
```

### B4. Update All Manager Classes
- HouseManager.java - use Owner references
- BookingManager.java - use House references
- ScheduleManager.java - use Cleaner and Booking references

### B5. Update Cleaning.groovy Serialization
```groovy
// Pattern for GET:
row.put("owner", house.getOwner()?.getOid() ?: 0)

// Pattern for CREATE/UPDATE:
long ownerOid = data.getLong("owner")
Owner owner = perst.getByOid(Owner, ownerOid)
house.setOwner(owner)
```

---

## Phase C: Frontend Updates

### C1. Update Cleaning.ts Interfaces
```typescript
export interface CostProfile {
  id: number;
  name: string;
  is_standard: boolean;
  owner?: number;          // OID
  base_hourly_rate: number;
  minimum_charge: number;
  rate_per_m2: number;
  rate_per_floor: number;
  rate_per_bedroom: number;
  rate_per_bathroom: number;
  dog_surcharge: number;
  basic_multiplier: number;
  standard_multiplier: number;
  premium_multiplier: number;
  luxury_multiplier: number;
  active: boolean;
}

export interface House {
  id: number;
  name: string;
  address: string;
  description?: string;
  owner?: number;          // Owner OID (was owner_id)
  cost_profile?: number;   // CostProfile OID (NEW)
  surface_m2?: number;     // NEW
  floors?: number;         // NEW
  bedrooms?: number;       // NEW
  bathrooms?: number;      // NEW
  luxury_level?: string;   // NEW
  check_in_time: string;
  check_out_time: string;
  active: boolean;
}
```

### C2. Create cost-profiles/+page.svelte
Admin page for managing cost profiles.

### C3. Update houses/+page.svelte
Add new fields and cost preview.

### C4. Update bookings/+page.svelte
Adapt to new `house` reference.

### C5. Update schedules/+page.svelte
Adapt to new `cleaner` and `booking` references.

### C6. Add i18n Translations
Add cost-related translations to en.json, nl.json, de.json.

---

## Files to Create/Modify

### Backend (Java) - CREATE
| File | Description |
|------|-------------|
| `src/main/precompiled/mycompany/domain/CostProfile.java` | CostProfile entity |
| `src/main/precompiled/mycompany/database/CostProfileManager.java` | CRUD manager |
| `src/main/backend/services/CostService.java` | Cost calculation |

### Backend (Java) - MODIFY
| File | Changes |
|------|---------|
| `src/main/precompiled/mycompany/domain/House.java` | Add cost fields, fix owner ref |
| `src/main/precompiled/mycompany/domain/Booking.java` | Fix house ref |
| `src/main/precompiled/mycompany/domain/Schedule.java` | Fix cleaner/booking refs |
| `src/main/precompiled/mycompany/database/HouseManager.java` | Update for new fields |
| `src/main/precompiled/mycompany/database/BookingManager.java` | Update for ref |
| `src/main/precompiled/mycompany/database/ScheduleManager.java` | Update for refs |

### Backend (Groovy) - MODIFY
| File | Changes |
|------|---------|
| `src/main/backend/services/Cleaning.groovy` | Add cost profile CRUD, fix all refs |

### Frontend (TypeScript/Svelte) - CREATE
| File | Description |
|------|-------------|
| `src/main/frontend-svelte/src/routes/cost-profiles/+page.svelte` | Cost profile management |

### Frontend (TypeScript/Svelte) - MODIFY
| File | Changes |
|------|---------|
| `src/main/frontend-svelte/src/lib/api/Cleaning.ts` | Update interfaces |
| `src/main/frontend-svelte/src/routes/houses/+page.svelte` | Add fields, cost preview |
| `src/main/frontend-svelte/src/routes/bookings/+page.svelte` | Adapt to new refs |
| `src/main/frontend-svelte/src/routes/schedules/+page.svelte` | Adapt to new refs |
| `src/main/frontend-svelte/src/lib/i18n/messages/en.json` | Add translations |
| `src/main/frontend-svelte/src/lib/i18n/messages/nl.json` | Add translations |
| `src/main/frontend-svelte/src/lib/i18n/messages/de.json` | Add translations |

---

## Progress Tracking

| Phase | Task | Status |
|-------|------|--------|
| A | Create CostProfile entity | PENDING |
| A | Create CostProfileManager | PENDING |
| A | Create CostService | PENDING |
| A | Add CostProfile CRUD to Cleaning.groovy | PENDING |
| A | Add cost fields to House | PENDING |
| B | Fix House.ownerId → House.owner | PENDING |
| B | Fix Booking.houseId → Booking.house | PENDING |
| B | Fix Schedule.cleanerId → Cleaner cleaner | PENDING |
| B | Fix Schedule.bookingId → Booking booking | PENDING |
| B | Update all Manager classes | PENDING |
| B | Update Cleaning.groovy serialization | PENDING |
| C | Update Cleaning.ts interfaces | PENDING |
| C | Create cost-profiles page | PENDING |
| C | Update houses page | PENDING |
| C | Update bookings page | PENDING |
| C | Update schedules page | PENDING |
| C | Add i18n translations | PENDING |

---

## API Response Pattern

### Before (Dinosaur):
```json
{
  "id": 12345,
  "owner_id": 4242,    // ❌ Foreign key
  "house_id": 999      // ❌ Foreign key
}
```

### After (OO):
```json
{
  "id": 12345,
  "owner": 4242,       // ✓ Perst OID from owner.getOid()
  "house": 999         // ✓ Perst OID from house.getOid()
}
```

### Frontend to Backend Pattern:
```json
{
  "data": {
    "owner": 4242,      // OID
    "name": "New House"
  }
}
```

### Backend Deserialization:
```groovy
long ownerOid = data.getLong("owner")
Owner owner = perst.getByOid(Owner, ownerOid)
house.setOwner(owner)
```

---

**Tagpoint**: `pre-cost-profile-refactor`
