# KissOO Package Restructuring Documentation

## Overview

This document describes the restructuring of the KissOO codebase from a technical package structure to a business-domain oriented structure, with clear separation between generic framework code (`koo`) and project-specific business logic (`domain`).

## Key Design Decisions

### 1. Framework vs Domain Separation

**`koo/` - Generic Framework Code**
- Reusable across any Perst-based project
- Contains: Actor system, persistence layer, security, services, configuration
- Location: `src/main/precompiled/koo/`
- Purpose: Foundation that any cleaning business could use

**`domain/` - Project-Specific Business Logic**
- KissOO-specific domain entities and managers
- Contains: Owner, Cleaner, House, Booking, Schedule, CostProfile concepts
- Location: `src/main/precompiled/domain/`
- Purpose: Business rules specific to the KissOO cleaning service

### 2. Business Domain Model

The domain layer is organized around **real-world business concepts** rather than technical concerns:

```
ACTORS (People/Entities)        OBJECTS OF VALUE (Business Assets)
├─ Owner                       ├─ House (Property)
│  └─ owns → House(s)          │   └─ has → CostProfile (pricing)
│                              │   └─ has → Booking(s) (service events)
└─ Cleaner                     │
   └─ has → Schedule           └─ Booking → Schedule (timing)
      (availability)               (when work occurs)
```

### 3. Critical Business Rules

1. **Schedule belongs to Cleaner**
   - Schedule = "When can Cleaner Y work?" (availability)
   - ScheduleManager manages cleaner availability
   - Location: `domain/actor/cleaner/`

2. **CostProfile belongs to House**
   - CostProfile = "How much to service House X?"
   - CostProfileManager manages house pricing rules
   - Location: `domain/oov/house/`

3. **Booking is a House Event**
   - Booking = "Cleaner Y scheduled to clean House X at Time Z"
   - Booking contains Schedule (when) + references Cleaner (who) + House (where)
   - BookingManager manages house events
   - Location: `domain/oov/house/`

4. **No Assignment Entity Needed**
   - Booking is sufficient: Schedule + Cleaner + House
   - Clean separation: Schedule (availability) vs Booking (assignment)

## Architecture Diagram

### Package Structure

```
src/main/precompiled/
├── koo/                     # GENERIC FRAMEWORK (reusable)
│   ├── core/
│   │   ├── actor/          # Actor, Agreement, Role, ANaturalActor
│   │   ├── database/       # StorageManager, PerstConnection, BaseManager
│   │   └── user/           # PerstUser, PerstUserManager
│   │
│   ├── security/           # EndpointMethod, PasswordSecurity
│   ├── services/           # Login, EmailService, CostService (base)
│   └── config/             # AppConfig, PerstConfig
│
└── domain/                  # KISSOO BUSINESS LOGIC (project-specific)
    ├── actor/
    │   ├── owner/
    │   │   ├── Owner.java                  # Property owner entity
    │   │   └── OwnerManager.java           # Owner CRUD
    │   │
    │   └── cleaner/
    │       ├── Cleaner.java                # Cleaner entity (worker)
    │       ├── CleanerManager.java         # Cleaner CRUD
    │       ├── Schedule.java               # Cleaner availability
    │       └── ScheduleManager.java        # Schedule CRUD
    │
    └── oov/                  # OOV = Object of Value (business assets)
        └── house/
            ├── House.java                  # Property asset
            ├── HouseManager.java           # House CRUD
            │
            ├── CostProfile.java            # Pricing rules
            ├── CostProfileManager.java     # Pricing CRUD
            │
            ├── Booking.java                # Service event (has Schedule)
            └── BookingManager.java         # Booking CRUD
```

### Class Relationship Diagram

```
ACTOR DOMAIN (People)
─────────────────────────────────────────────────────────
  Owner                          Cleaner
  (property owner)              (worker)
      │                              │
      │ has                          │ has
      ▼                              ▼
  House(s)                     Schedule       ← availability
                               (when can work?)
      │                              │
      │                              │ assigned to
      │                              ▼
      │                         Booking           ← service event
      │                     (at this House)
      │                          │
      └──────────────────────────┼─────────────────
                                  ▼
                              Schedule   ← timing (from Booking)
```

### OOV DOMAIN (Assets)

```
House (Object of Value)
    │
    ├── has → CostProfile      ← pricing rules
    │
    └── has → Booking(s)       ← service events
            │
            ├── has → Schedule  ← timing
            │
            └── assigned → Cleaner
                         (the worker)
```

## Business Logic Flow

### Scenario: Schedule a Cleaning

**1. Check Cleaner Availability** (ScheduleManager)
```
ScheduleManager.checkAvailability(cleaner, date)
  ↓ (from domain/actor/cleaner/)
Returns: Schedule slots when cleaner is free
```

**2. Create Booking** (BookingManager)
```
Booking booking = new Booking(house, cleaner, schedule, date)
  ↓ (in domain/oov/house/)
BookingManager.create(booking)
  ↓
Persists: This cleaner at this house at this time
```

**3. Calculate Cost** (CostProfileManager)
```
CostProfile profile = house.getCostProfile()
  ↓ (from domain/oov/house/)
CostProfileManager.calculate(profile, house)
  ↓
Returns: Price for this house
```

## Key Inheritance Pattern

### Framework Extension

```java
// koo/core/actor/ANaturalActor.java (framework)
public abstract class ANaturalActor {
    private Agreement agreement;
    private PerstUser perstUser;
    // ... framework methods
}

// domain/actor/owner/Owner.java (business)
public class Owner extends ANaturalActor {
    private String email;
    private String phone;
    private Link houses;  // Perst Link
    
    // Owner-specific business logic
    public List<House> getHouses() { ... }
    public List<Booking> getBookings() { ... }
}
```

```java
// koo/core/actor/ANaturalActor.java (framework)
public abstract class ANaturalActor { ... }

// domain/actor/cleaner/Cleaner.java (business)
public class Cleaner extends ANaturalActor {
    private String phone;
    private String email;
    private Link schedules;  // Perst Link
    
    // Cleaner-specific business logic
    public List<Schedule> getSchedules() { ... }
}
```

## Migration from Previous Structure

### Before (Technical Grouping)
```
domain/
├── mycompany/actor/owner/      (Owner + OwnerManager)
├── mycompany/actor/cleaner/    (Cleaner + CleanerManager + CostProfileManager ✗)
├── mycompany/database/         (BookingManager + ScheduleManager)
└── mycompany/oov/house/        (House + Booking + Schedule + CostProfile)
```

**Problems:**
- CostProfileManager with cleaner (should be with house pricing)
- Schedule in house package (should be cleaner availability)
- ScheduleManager in database (should be with cleaner)
- BookingManager in database (should be with house events)

### After (Business Grouping)
```
domain/
├── actor/owner/                (Owner + OwnerManager) ✓
├── actor/cleaner/              (Cleaner + CleanerManager + Schedule + ScheduleManager) ✓
└── oov/house/                  (House, Booking, CostProfile, all managers) ✓
```

**Improvements:**
- Schedule/ScheduleManager with cleaner (worker availability)
- CostProfileManager with house (property pricing)
- Booking/BookingManager with house (property events)
- Clean separation of concerns

## Technical Requirements Met

### 1. Precompiled Location ✅
Both `koo/` and `domain/` in `src/main/precompiled/` for Groovy service access

### 2. Inheritance ✅
Domain entities extend koo framework entities
```java
Owner extends ANaturalActor
Cleaner extends ANaturalActor
```

### 3. No MyCompany References ✅
Zero remaining `mycompany.*` imports

### 4. Groovy Service Access ✅
Services can import:
```groovy
import domain.actor.owner.Owner
import domain.actor.cleaner.Cleaner  
import domain.actor.cleaner.Schedule
import domain.oov.house.Booking
import domain.oov.house.House
import domain.oov.house.CostProfile
```

## Summary

This restructuring achieves:

1. **Clear Separation**: `koo/` (generic) vs `domain/` (specific)
2. **Business Alignment**: Organized around real-world concepts (Actors, OOVs)
3. **Correct Cohesion**: 
   - Schedule with Cleaner (availability)
   - CostProfile with House (pricing)
   - Booking with House (events)
4. **No Redundancy**: No Assignment entity needed
5. **Maintainability**: Related concepts grouped together
6. **Extensibility**: Easy to add new OOV types (car, equipment, etc.)

The result is a domain model that makes business sense to domain experts while maintaining all technical requirements for the framework architecture.
