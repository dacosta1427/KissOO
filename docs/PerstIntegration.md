# Perst OODB Integration - KissOO Project

## Quick Reference

```bash
# Run Perst tests
java -jar work/KissUnitTest.jar --select-package=oodb

# Run all tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

**For detailed testing instructions, see [docs/TestingGuide.md](docs/TestingGuide.md).**

## Overview

This document describes the Perst OODB (Object-Oriented Database) integration into the KissOO project.

## Dependencies

Added to `libs/`:
- `perst-dcg-4.0.0.jar` - Perst with CDatabase versioning
- `slf4j-api-1.7.30.jar` - Logging facade
- `slf4j-simple-1.7.30.jar` - SLF4J implementation

## Project Structure

```
src/main/
├── precompiled/
│   ├── mycompany/domain/     # Domain entities (Actor, Agreement, Group, PerstUser)
│   ├── mycompany/database/   # Manager classes (ActorManager, PerstHelper)
│   └── oodb/                 # Perst config (PerstConfig, PerstContext)
└── backend/
    └── services/             # REST services (ActorService, etc.)
```

## Package Mapping

| Package | Location | Purpose |
|---------|----------|---------|
| `oodb` | `precompiled/oodb/` | Perst configuration and context |
| `mycompany.domain` | `precompiled/mycompany/domain/` | Domain entities |
| `mycompany.database` | `precompiled/mycompany/database/` | Manager classes |
| `services` | `backend/services/` | REST endpoints |

## Quick Start

### Enable Perst

Edit `backend/application.ini`:

```ini
PerstEnabled = true
PerstDatabasePath = oodb
```

### Basic Usage

```java
import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.database.PerstHelper;

// Create
Agreement agreement = new Agreement("USER");
agreement.grant(Actor.class, "read");
Actor actor = new Actor("John", "USER", agreement);
PerstHelper.storeNewObject(actor);

// Retrieve
Actor found = PerstHelper.retrieveObject(Actor.class, "name", "John");
```

## Configuration

`src/main/precompiled/oodb/PerstConfig.java`:

```java
// Default values
perstEnabled = false        // Disabled by default
useCDatabase = true         // Use CDatabase for versioning + Lucene
databasePath = "oodb"       // Database file location
pagePoolSize = 512MB        // Memory cache size
```

### CDatabase (Versioning + Lucene)

Perst CDatabase provides:
- **Object versioning** - Automatic tracking of object changes
- **Lucene full-text search** - Integrated text indexing

#### Important: Lucene Index Path

The Lucene index must be **adjacent** to the Perst database file, NOT a subdirectory:

```
data/oodb        <- Perst database FILE
data/oodb.idx    <- Lucene index DIRECTORY (auto-created by FSDirectory)
```

**Why this matters:** Perst creates `oodb` as a file, not a directory. The original bug tried to create the Lucene index at `data/oodb/idx` (subdirectory inside the file path), which fails. The fix places it at `data/oodb.idx` (adjacent).

#### Enabling CDatabase

In `backend/application.ini`:
```ini
PerstUseCDatabase = true
PerstDatabasePath = ../../../data/oodb
```

The database and index are stored outside the source tree at `data/oodb` and `data/oodb.idx`.

## Architecture

```
PerstConfig (singleton)
    └── PerstContext (singleton)
            ├── Storage / CDatabase (Perst database)
            └── mycompany.domain (Actor, Agreement, Group, PerstUser)
                    └── mycompany.database (ActorManager, PerstHelper)
```

## See Also

- **Testing**: [docs/TestingGuide.md](docs/TestingGuide.md)
- **Perst Usage**: [PERST_USAGE.md](PERST_USAGE.md)
- **Architecture**: [GETTING_STARTED.md](GETTING_STARTED.md)
