# Build Instructions

## IMPORTANT: Use Bld Build Mechanism

**DO NOT use Gradle directly.** Use the custom `bld` build tool:

```bash
# Build the project
cd kissweb
./bld build

# Build in development mode (watches for changes)
./bld develop

# Clean and rebuild
./bld clean build
```

The `bld` tool handles:
- Java compilation
- Groovy compilation  
- Precompiled sources
- Classpath management
- Perst JAR integration

## Directory Structure

```
src/main/backend/domain/
├── Actor.java              # Domain entity
├── PerstUser.java         # Domain entity
├── PerstDBRoot.java       # ⚠️ MUST UPDATE when adding/removing domain classes
│
├── kissweb/               # Framework integration
│   ├── PerstContext.java
│   └── PerstConfig.java
│
└── database/             # Data access layer
    └── PerstHelper.java
```

## Adding New Domain Classes

### Step 1: Create the domain class

Create class in `src/main/backend/domain/`:

```java
package domain;

import org.garret.perst.continuous.CVersion;

public class MyEntity extends CVersion {
    private String name;
    private String description;
    
    // getters/setters
}
```

### Step 2: ⚠️ UPDATE PerstDBRoot

**IMPORTANT:** When adding or removing domain classes, you MUST update `PerstDBRoot.java`:

```java
// In PerstDBRoot.java:

// 1. Add import
import domain.MyEntity;

// 2. Add field index
public FieldIndex<MyEntity> myEntityIndex;

// 3. Add to setCollections()
public void setCollections(Storage db) {
    userIndex = db.createFieldIndex(PerstUser.class, "username", true);
    actorIndex = db.createFieldIndex(Actor.class, "username", true);
    
    // ADD YOUR NEW INDEX HERE
    myEntityIndex = db.createFieldIndex(MyEntity.class, "name", true);
}
```

**This is easily forgotten!** Check PerstDBRoot whenever you modify domain classes.

### Step 3: Build

```bash
./bld build
```

## Domain Class Requirements

- Must extend `CVersion` for automatic versioning
- Must have a default constructor
- Fields should be primitive types, Strings, or other CVersion objects

## Database Architecture

### Current Setup

| Component | Database | Purpose |
|----------|---------|---------|
| **Login/Authentication** | Perst | User authentication and sessions |
| **Domain Objects** | Perst | Application data (Actors, Users, etc.) |
| **Framework Internals** | SQLite | Session management, framework state |

### Perst vs SQL Databases

- **Perst** - Your application data, fully versioned, no SQL
- **SQLite** - Framework only (cannot be removed yet), minimal attack surface

### Security Notes

**SQL Injection Protection:**
- kissweb uses **prepared statements** internally for all SQL queries
- User input is NEVER concatenated directly into SQL strings
- All queries use parameterized queries (`?` placeholders)
- Perst has NO SQL - objects are stored directly, no queries needed

**Example of safe query in kissweb:**
```java
// ✅ SAFE - Uses prepared statement
PreparedStatement ps = connection.prepareStatement(
    "SELECT * FROM users WHERE username = ?"
);
ps.setString(1, username);  // Parameterized - no injection possible
```

**Perst is immune to SQL injection** because:
- No SQL queries - objects are accessed via indexes
- No string concatenation for queries
- All data is stored as Java objects, not SQL rows

### Future: Eliminating SQLite

Discussion needed with kissweb creator. SQLite is currently required for:
- Session management
- Framework internal state

The goal is to eventually replace SQLite entirely with Perst.
