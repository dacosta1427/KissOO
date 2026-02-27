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
