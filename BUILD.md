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
src/main/
├── backend/           # ← Domain classes go HERE
│   └── gfe/
│       ├── Actor.java        # Domain entity
│       ├── PerstUser.java   # Domain entity
│       ├── Root.java        # Perst root object
│       └── ...
│
└── precompiled/     # Legacy - avoid using
```

## Adding New Domain Classes

1. Create class in `src/main/backend/gfe/`
2. Extend `CVersion` for automatic versioning
3. Run `./bld build`

Example:
```java
package gfe;

public class MyEntity extends CVersion {
    private String name;
    // getters/setters
}
```
