# Koo Framework Manual
*Designing Domains and Building Applications*

## Table of Contents

1. [Introduction](#introduction)
2. [Domain Design Principles](#domain-design-principles)
3. [Getting Started](#getting-started)
4. [Creating Domain Classes](#creating-domain-classes)
5. [Services](#services)
6. [Frontend Integration](#frontend-integration)
7. [Permissions](#permissions)
8. [Deployment](#deployment)
9. [Best Practices](#best-practices)

---

## Introduction

Koo Framework is a pure object-oriented framework built on top of the Kiss framework and ooGTxQ database. It provides:

- **Pure OO design** - No ID fields, objects reference each other directly
- **Automatic UI generation** - CRUD interfaces from domain classes
- **ProtoBuf optimization** - Fast binary serialization
- **Native KISS integration** - Uses `returnBinary()`/`binaryCall()` for performance

### Key Concepts

1. **Domain Classes** extend `CVersion` (from Perst)
2. **Services** follow naming conventions for auto-UI generation
3. **Frontend** uses Svelte 5 components
4. **Permissions** integrate with Agreement system

---

## Domain Design Principles

### 1. Pure Object References

**DO THIS:**
```java
public class House extends CVersion {
    private Owner owner;  // Direct object reference
    private List<Booking> bookings;
}
```

**NOT THIS:**
```java
public class House extends CVersion {
    private long ownerId;  // SQL-style ID - NO!
    private List<Long> bookingIds;
}
```

### 2. Business Logic in Domain Classes

```java
public class House extends CVersion {
    private double price;
    private String address;
    
    public boolean isAffordable(double budget) {
        return this.price <= budget;
    }
    
    public void updatePrice(double newPrice) {
        if (newPrice < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.price = newPrice;
    }
}
```

### 3. Aggregate Roots

Identify which classes are aggregate roots (top-level entities):

```java
// GOOD aggregate roots
public class Owner extends CVersion { ... }
public class House extends CVersion { ... }
public class Booking extends CVersion { ... }

// GOOD value objects (embedded)
public class Address extends CVersion { ... }
public class Money extends CVersion { ... }
```

---

## Getting Started

### 1. Create New Project

```bash
koo create my-app
cd my-app
```

### 2. Add Domain Class

```bash
koo generate domain House
```

### 3. Run Development Server

```bash
koo build
./bld dev
```

---

## Creating Domain Classes

### Basic Structure

```java
package mycompany.domain;

import koo.framework.domain.FrameworkEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class House extends FrameworkEntity {
    private String address;
    private double price;
    private Owner owner;
    private List<Booking> bookings;
    
    public House() {
        // Default constructor required
    }
    
    public House(String address, double price) {
        this.address = address;
        this.price = price;
    }
}
```

### Field Types

| Java Type | Proto Type | UI Component |
|-----------|------------|--------------|
| String | string | TextInput |
| int | int32 | NumberInput |
| long | int64 | NumberInput |
| double | double | NumberInput |
| float | float | NumberInput |
| boolean | bool | Checkbox |
| Date/LocalDate | string (date) | DateInput |
| Enum | enum | Select |
| Object reference | nested message | Lookup |
| Collection | repeated | DataTable |

### Annotations

```java
// @toJSON - Auto-generate JSON serialization
// @toProto - Auto-generate ProtoBuf serialization
// @Indexable - Create database index
// @FullTextSearchable - Enable full-text search
```

---

## Services

### Naming Conventions

Koo uses method names to determine UI behavior:

| Pattern | UI Generated |
|---------|--------------|
| `searchXyz` | Search form + results table |
| `getXyz` | View/detail page |
| `createXyz` | Create modal/form |
| `updateXyz` | Edit modal/form |
| `deleteXyz` | Delete action |

### Service Template

```java
package mycompany.services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import mycompany.domain.House;
import mycompany.managers.HouseManager;

public class HouseService {
    
    public void searchHouses(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String address = injson.optString("address");
        double minPrice = injson.optDouble("minPrice", 0);
        double maxPrice = injson.optDouble("maxPrice", Double.MAX_VALUE);
        
        java.util.Collection<House> houses = HouseManager.search(address, minPrice, maxPrice);
        
        // KISS automatically handles proto conversion
        outjson.put("_Success", true);
    }
    
    public void getHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        long oid = injson.getLong("oid");
        House house = HouseManager.getByOid(oid);
        
        if (house == null) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", "House not found");
            return;
        }
        
        // Use KISS's native binary support
        servlet.returnBinary(house.toProto().toByteArray());
    }
}
```

### Using KISS Binary Support

**Server-side:**
```java
// In your service method
byte[] protoBytes = myObject.toProto().toByteArray();
servlet.returnBinary(protoBytes);
```

**Client-side:**
```javascript
const response = await Server.binaryCall('HouseService', 'getHouse', { oid: 123 });
if (response._Success) {
    const house = HouseProto.decode(response._data);
}
```

---

## Frontend Integration

### Auto-Generated Components

When you create a domain class `House`, Koo generates:

- `HouseList.svelte` - List/search view
- `HouseForm.svelte` - Create/edit form
- `HouseView.svelte` - Detail view

### Using Components

```svelte
<script>
    import { DataTable, FormGenerator } from 'koo-svelte';
    import { HouseProto } from './generated/HouseProto.js';
    
    let houses = $state([]);
    let showForm = $state(false);
</script>

<DataTable data={houses} columns={houseColumns} />

{#if showForm}
    <FormGenerator 
        schema={HouseProto.schema} 
        bind:formData={newHouse}
    />
{/if}
```

### Modal Integration

```svelte
<script>
    import { openModal } from 'koo-svelte';
    
    function handleEdit(house) {
        openModal('HouseForm', { house });
    }
</script>
```

---

## Permissions

### Agreement Integration

Services automatically check permissions based on naming:

```java
// Method name: searchHouses
// Checks permission: House.read

// Method name: createHouse  
// Checks permission: House.create

// Method name: updateHouse
// Checks permission: House.update
```

### Permission Configuration

```java
// In Agreement class
public class Agreement {
    private Set<String> permissions;
    
    public boolean hasPermission(String resource, String action) {
        return permissions.contains(resource + "." + action);
    }
}
```

---

## Deployment

### Development

```bash
koo build
./bld dev
```

### Production

```bash
koo build --env production
./bld war
```

### Database Setup

1. Configure in `application.ini`:
```
PerstDatabasePath=/path/to/database
```

2. Clear database if needed:
```bash
pkill -9 java
rm -rf /path/to/database*
mkdir -p /path/to
./bld develop
```

---

## Best Practices

### 1. Domain Design
- Keep domain classes focused on business logic
- Use value objects for complex attributes
- Maintain pure OO relationships

### 2. Service Design
- Follow naming conventions
- Use `returnBinary()` for proto responses
- Handle errors gracefully

### 3. Frontend
- Use generated components when possible
- Customize via props/slots
- Leverage Svelte 5 runes

### 4. Performance
- Use proto schemas for nested objects
- Implement lazy loading for large collections
- Cache frequently accessed data

### 5. Security
- Always check permissions
- Validate input data
- Use Agreement system properly

---

## Troubleshooting

### Common Issues

1. **Service not found**
   - Check package location (should be in `backend/services/`)
   - Verify method signature

2. **Proto conversion errors**
   - Ensure domain class extends `CVersion`
   - Check field types are supported

3. **Permission denied**
   - Verify Agreement has correct permissions
   - Check method naming conventions

4. **UI not updating**
   - Use `$state` for reactive data
   - Check component props

---

## Quick Reference

### Commands

```bash
koo create <name>        # Create new project
koo generate domain <Name>    # Generate domain class
koo generate service <Name>   # Generate service
koo build              # Build project
```

### File Structure

```
src/
├── main/
│   ├── java/
│   │   └── mycompany/
│   │       ├── domain/      # Domain classes
│   │       ├── services/    # Service classes
│   │       └── managers/    # Manager classes
│   └── frontend/            # Svelte app
└── test/                    # Tests
```

---

*This manual is continuously updated. Last updated: June 2026*