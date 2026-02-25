# Perst Integration Guide

## Overview

This document describes how Perst (an embedded Object-Oriented Database) is integrated into the kissweb framework.

---

## Architecture

### Connection Pool Integration

kissweb uses **C3P0** for PostgreSQL connection pooling. Perst integrates with this architecture as follows:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    kissweb Application                               │
├─────────────────────────────────────────────────────────────────────┤
│  application.ini                                                    │
│  ├── DatabaseType = PostgreSQL        (C3P0 pool)                 │
│  ├── PerstEnabled = true             (Perst enabled)              │
│  ├── PerstDatabasePath = oodb         (Perst storage location)    │
│  └── PerstPagePoolSize = 536870912   (512MB cache)                │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  MainServlet (Server Startup)                                       │
│  └── Initializes C3P0 ComboPooledDataSource                       │
│      └── Manages PostgreSQL connection pool                        │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  ProcessServlet (Per Request)                                       │
│  1. getConnection() → from C3P0 pool                              │
│  2. Wrap in kissweb Connection                                     │
│  3. If PerstEnabled → wrap in PerstConnection                    │
│  4. Pass to service method                                         │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Service Methods                                                    │
│  Example: ActorService.getActor()                                 │
│                                                                     │
│  public void getActor(JSONObject injson, JSONObject outjson,        │
│                      Connection db, ProcessServlet servlet) {       │
│      // PostgreSQL operations                                        │
│      db.executeSQL("SELECT * FROM actors WHERE uuid=?", uuid);    │
│                                                                     │
│      // Perst operations (if available)                            │
│      if (db instanceof PerstConnection) {                          │
│          PerstConnection perstDb = (PerstConnection) db;           │
│          Actor actor = perstDb.retrieveObject(Actor.class, uuid); │
│      }                                                             │
│  }                                                                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Configuration

### application.ini Settings

```ini
# ===== DATABASE CONNECTION POOL (C3P0) =====
DatabaseType = PostgreSQL
DatabaseHost = localhost
DatabasePort = 5432
DatabaseName = compare_test
DatabaseUser = postgres
DatabasePassword = gfe

# Pool settings (optional - defaults work well)
# DatabaseMinPoolSize = 10
# DatabaseMaxPoolSize = 100

# ===== PERST OODBMS SETTINGS =====
# Set to true to enable Perst OODBMS (false uses PostgreSQL only)
PerstEnabled = true

# Path to Perst database files (relative or absolute)
PerstDatabasePath = oodb

# Page pool size in bytes (default: 512MB = 536870912)
PerstPagePoolSize = 536870912
```

### Key Settings

| Setting | Description | Default |
|---------|-------------|---------|
| `PerstEnabled` | Enable/disable Perst | `false` |
| `PerstDatabasePath` | Directory for Perst files | `oodb` |
| `PerstPagePoolSize` | Memory cache size (bytes) | `536870912` (512MB) |

---

## Usage in Services

### Basic Pattern

```java
import nl.dcg.gfe.PerstConnection;
import gfe.Actor;

public class MyService {
    public void myMethod(JSONObject injson, JSONObject outjson, 
                        Connection db, ProcessServlet servlet) {
        
        // === PostgreSQL Operations (always available) ===
        db.executeSQL("SELECT * FROM table WHERE id = ?", id);
        
        
        // === Perst Operations (check availability first) ===
        if (db instanceof PerstConnection) {
            PerstConnection perstDb = (PerstConnection) db;
            
            // Check if Perst is initialized
            if (perstDb.isPerstAvailable()) {
                
                // Retrieve object by UUID
                Actor actor = perstDb.retrieveObject(Actor.class, actorUuid);
                
                // Store object
                perstDb.storeObject(actor);
                
                // Create index
                perstDb.createIndex(Actor.class, "name");
                
                // Query with filter
                List<Actor> actors = perstDb.query(Actor.class, 
                    "name LIKE ?", "John%");
            }
        }
    }
}
```

### PerstConnection API

```java
// Retrieve single object by UUID
<T extends CVersion> T retrieveObject(Class<T> clazz, String uuid)

// Retrieve single object by indexed field
<T extends CVersion> T retrieveObject(Class<T> clazz, String field, String value)

// Store or update object
void storeObject(IPersistent obj)

// Delete object
void deleteObject(IPersistent obj)

// Create index on a field
void createIndex(Class<?> clazz, String field)

// Query with filter
<T extends CVersion> List<T> query(Class<T> clazz, String filter, Object... args)

// Get all objects of a class
<T extends CVersion> List<T> getAll(Class<T> clazz)

// Check if Perst is available
boolean isPerstAvailable()
```

---

## Object Model

### Creating Perst-Enabled Classes

Your persistent classes must extend `CVersion` (for versioning support):

```java
package gfe;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Index;
import org.garret.perst.FieldIndex;

public class Actor extends CVersion {
    public String name;
    public String bio;
    public int birthYear;
    public String country;
    
    // Default constructor required
    public Actor() {}
    
    // Convenience constructor
    public Actor(String name, String bio) {
        this.name = name;
        this.bio = bio;
    }
    
    // Convert to JSON for API responses
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("uuid", getUuid());
        json.put("name", name);
        json.put("bio", bio);
        json.put("birthYear", birthYear);
        json.put("country", country);
        return json;
    }
}
```

---

## Performance Considerations

### When to Use Perst

| Use Case | Database | Reason |
|----------|----------|--------|
| Simple CRUD operations | **Perst** | 10-75x faster |
| Object-oriented data | **Perst** | Direct object storage |
| Complex SQL queries | PostgreSQL | Optimized query planner |
| JOIN operations | PostgreSQL | Proper join execution |
| Full-text search | PostgreSQL/Lucene | Mature text indexing |

### Connection Pool Impact

- **PostgreSQL**: Uses C3P0 pool (configured in application.ini)
- **Perst**: Embedded - no pool needed, single Storage instance
- PerstConnection wraps pooled PostgreSQL connections seamlessly

---

## Troubleshooting

### Perst Not Available

If `isPerstAvailable()` returns false:

1. Check `application.ini`: `PerstEnabled = true`
2. Check database path exists and is writable
3. Check perst.jar is in classpath
4. Check server logs for initialization errors

### Object Not Found

- Ensure class extends `CVersion`
- Ensure UUID field is set
- Check Perst database path contains files

---

## Files Reference

| File | Purpose |
|------|---------|
| `Perst_backup/PerstContext.java` | Singleton managing Perst Storage |
| `Perst_backup/PerstConnection.java` | Wrapper adding Perst methods to Connection |
| `Perst_backup/PerstConfig.java` | Configuration reader |
| `ActorService.java` | Example service using both databases |

---

*Document Version: 1.0*
*Created: 2026-02-25*
