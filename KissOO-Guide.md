# KissOO Comprehensive Guide

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Perst OODB Integration](#perst-oodb-integration)
4. [Domain Model](#domain-model)
5. [Manager at the Gate Authorization](#manager-at-the-gate-authorization)
6. [Backend Services](#backend-services)
7. [REST API](#rest-api)
8. [Authentication System](#authentication-system)
9. [Frontend-Backend Integration](#frontend-backend-integration)
10. [Configuration Reference](#configuration-reference)
11. [Build System](#build-system)
12. [Development Guide](#development-guide)
13. [Troubleshooting](#troubleshooting)
14. [Quick Reference](#quick-reference)

---

## Overview

KissOO is a fork of the Kiss web framework with **Perst OODBMS** integration. It's a Java-based full-stack web application framework that replaces traditional SQL databases with object-oriented persistence.

### Key Features
- **Perst OODBMS** - Object-oriented database (no SQL)
- **Manager at the Gate** - Three-layer authorization pattern
- **Multi-language Services** - Java, Groovy, Lisp support
- **Hot Reload** - Services compile on change
- **Svelte 5 Frontend** - Modern reactive UI

### Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Language** | Java, Groovy | 17+, 3.x |
| **Database** | Perst OODBMS | 5.1.0 (CDatabase) |
| **Search** | Lucene | Integrated |
| **Frontend** | Svelte + Tailwind | 5.x, 3.4.x |
| **Server** | Tomcat | 11.x |

---

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              KISSOO APPLICATION                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ┌────────────────────────────────────────────────────────────────────────────────┐  │
│  │                           PRESENTATION LAYER                                   │  │
│  │                                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────────────────────┐  │  │
│  │  │                        SVELTE 5 FRONTEND                                 │  │  │
│  │  │                                                                          │  │  │
│  │  │   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                │  │  │
│  │  │   │   Pages     │    │ Components  │    │   Stores    │                │  │  │
│  │  │   │ (+page.svelte)│  │ (.svelte)   │    │ (.svelte.ts)│                │  │  │
│  │  │   └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                │  │  │
│  │  │          │                  │                  │                        │  │  │
│  │  │          └──────────────────┼──────────────────┘                        │  │  │
│  │  │                             │                                           │  │  │
│  │  │                             ▼                                           │  │  │
│  │  │              ┌─────────────────────────────────┐                        │  │  │
│  │  │              │      API Modules ($lib/api/)    │                        │  │  │
│  │  │              │   Auth.ts, Users.ts, Actors.ts  │                        │  │  │
│  │  │              └────────────────┬────────────────┘                        │  │  │
│  │  │                               │                                         │  │  │
│  │  │                               ▼                                         │  │  │
│  │  │              ┌─────────────────────────────────┐                        │  │  │
│  │  │              │        Server.call()            │                        │  │  │
│  │  │              │    POST http://localhost:8080/rest│                       │  │  │
│  │  │              └────────────────┬────────────────┘                        │  │  │
│  │  │                               │                                         │  │  │
│  │  └───────────────────────────────┼─────────────────────────────────────────┘  │
│  │                                  │                                            │
│  └──────────────────────────────────┼────────────────────────────────────────────┘  │
│                                     │                                               │
│                                     │ HTTP/JSON                                    │
│                                     ▼                                               │
│  ┌────────────────────────────────────────────────────────────────────────────────┐  │
│  │                           APPLICATION LAYER                                    │  │
│  │                                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────────────────────┐  │  │
│  │  │                        MAIN SERVLET (REST)                               │  │  │
│  │  │                                                                          │  │  │
│  │  │   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                │  │  │
│  │  │   │ Authentication│   │   Session   │    │   Queue     │                │  │  │
│  │  │   │   Check      │    │   Manager   │    │   Manager   │                │  │  │
│  │  │   └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                │  │  │
│  │  │          │                  │                  │                        │  │  │
│  │  │          └──────────────────┼──────────────────┘                        │  │  │
│  │  │                             │                                           │  │  │
│  │  │                             ▼                                           │  │  │
│  │  │              ┌─────────────────────────────────┐                        │  │  │
│  │  │              │     Service Dispatcher          │                        │  │  │
│  │  │              │  (Groovy → Java → Lisp)         │                        │  │  │
│  │  │              └────────────────┬────────────────┘                        │  │  │
│  │  │                               │                                         │  │  │
│  │  └───────────────────────────────┼─────────────────────────────────────────┘  │
│  │                                  │                                            │
│  └──────────────────────────────────┼────────────────────────────────────────────┘  │
│                                     │                                               │
│                                     │ Method Call                                   │
│                                     ▼                                               │
│  ┌────────────────────────────────────────────────────────────────────────────────┐  │
│  │                            BUSINESS LAYER                                      │  │
│  │                                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────────────────────┐  │  │
│  │  │                         SERVICES                                         │  │  │
│  │  │   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                │  │  │
│  │  │   │ Users.groovy│    │ActorService │    │ CustomSvc   │                │  │  │
│  │  │   └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                │  │  │
│  │  │          │                  │                  │                        │  │  │
│  │  │          └──────────────────┼──────────────────┘                        │  │  │
│  │  │                             │                                           │  │  │
│  │  │                             ▼                                           │  │  │
│  │  │              ┌─────────────────────────────────┐                        │  │  │
│  │  │              │     Manager Pattern             │                        │  │  │
│  │  │              │  (PerstUserManager, etc.)       │                        │  │  │
│  │  │              └────────────────┬────────────────┘                        │  │  │
│  │  │                               │                                         │  │  │
│  │  └───────────────────────────────┼─────────────────────────────────────────┘  │
│  │                                  │                                            │
│  └──────────────────────────────────┼────────────────────────────────────────────┘  │
│                                     │                                               │
│                                     │ CRUD Operations                               │
│                                     ▼                                               │
│  ┌────────────────────────────────────────────────────────────────────────────────┐  │
│  │                            DATA LAYER                                          │  │
│  │                                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────────────────────┐  │  │
│  │  │                      PerstStorageManager                                 │  │  │
│  │  │                         ┌──────────┐                                     │  │  │
│  │  │                         │  Unified │                                     │  │  │
│  │  │                         │   DB     │                                     │  │  │
│  │  │                         │ Manager  │                                     │  │  │
│  │  │                         └────┬─────┘                                     │  │  │
│  │  │                              │                                           │  │  │
│  │  │                              ▼                                           │  │  │
│  │  │                         ┌──────────┐                                     │  │  │
│  │  │                         │  Perst   │                                     │  │  │
│  │  │                         │ Storage  │                                     │  │  │
│  │  │                         └────┬─────┘                                     │  │  │
│  │  │                              │                                           │  │  │
│  │  └──────────────────────────────┼───────────────────────────────────────────┘  │
│  │                                 │                                              │
│  └─────────────────────────────────┼──────────────────────────────────────────────┘  │
│                                    │                                                 │
│                                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────────────────────────┐  │
│  │                         STORAGE LAYER                                          │  │
│  │                                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────────────────────┐  │  │
│  │  │                         PERST OODBMS                                     │  │  │
│  │  │                                                                          │  │  │
│  │  │   ┌─────────────────────┐    ┌─────────────────────┐                   │  │  │
│  │  │   │   data/oodb         │    │   data/oodb.idx/    │                   │  │  │
│  │  │   │   (Database File)   │    │   (Lucene Index)    │                   │  │  │
│  │  │   └─────────────────────┘    └─────────────────────┘                   │  │  │
│  │  │                                                                          │  │  │
│  │  │   ┌─────────────────────────────────────────────────────────────────┐   │  │  │
│  │  │   │  Objects Stored:                                                │   │  │  │
│  │  │   │  - Actor (with @Indexable, @FullTextSearchable)                │   │  │  │
│  │  │   │  - PerstUser (username, passwordHash, salt)                    │   │  │  │
│  │  │   │  - Agreement (permissions)                                     │   │  │  │
│  │  │   │  - Group (user groupings)                                      │   │  │  │
│  │  │   │  - Custom domain objects                                       │   │  │  │
│  │  │   └─────────────────────────────────────────────────────────────────┘   │  │  │
│  │  │                                                                          │  │  │
│  │  └──────────────────────────────────────────────────────────────────────────┘  │
│  │                                                                                │  │
│  └────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Component Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            CLASS RELATIONSHIPS                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   DOMAIN ENTITIES                        MANAGERS                                    │
│   ───────────────                        ────────                                    │
│                                                                                      │
│   ┌──────────────┐                       ┌──────────────┐                           │
│   │  CVersion    │                       │ BaseManager  │                           │
│   │  (abstract)  │                       │   <T>        │                           │
│   └──────┬───────┘                       └──────┬───────┘                           │
│          │                                       │                                   │
│          │ extends                               │ extends                           │
│          │                                       │                                   │
│   ┌──────┴───────────────────────┐       ┌──────┴───────────────────────┐           │
│   │           │                  │       │           │                  │           │
│   ▼           ▼                  ▼       ▼           ▼                  ▼           │
│ ┌──────┐  ┌──────────┐  ┌────────────┐ │ ┌──────────────┐  ┌──────────────────┐   │
│ │Actor │  │PerstUser │  │ Agreement  │ │ │ActorManager  │  │PerstUserManager  │   │
│ └──────┘  └──────────┘  └────────────┘ │ └──────────────┘  └──────────────────┘   │
│                                         │                                            │
│   PERST INTEGRATION                     │   SERVICE LAYER                            │
│   ─────────────────                     │   ──────────────                            │
│                                         │                                            │
│   ┌──────────────┐                      │   ┌──────────────┐                        │
│   │ PerstStorage │◄─────────────────────┼───│ Services     │                        │
│   │  Manager     │                      │   │ (Users.groovy│                        │
│   └──────┬───────┘                      │   │ ActorService)│                        │
│          │                              │   └──────────────┘                        │
│          │ delegates                    │                                            │
│          ▼                              │                                            │
│   ┌──────────────┐                      │                                            │
│   │ UnifiedDB    │                      │                                            │
│   │ Manager      │                      │                                            │
│   └──────┬───────┘                      │                                            │
│          │                              │                                            │
│          │ uses                          │                                            │
│          ▼                              │                                            │
│   ┌──────────────┐                      │                                            │
│   │   Perst      │                      │                                            │
│   │  Storage     │                      │                                            │
│   └──────────────┘                      │                                            │
│                                         │                                            │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Request Processing Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         DETAILED REQUEST FLOW                                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  1. HTTP REQUEST RECEIVED                                                            │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  POST /rest                                                                     ││
│  │  Body: { "_class":"services.Users", "_method":"getRecords", "_uuid":"abc" }   ││
│  └─────────────────────────────────────────┬───────────────────────────────────────┘│
│                                            │                                         │
│                                            ▼                                         │
│  2. MAIN SERVLET (MainServlet.doPost)                                               │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  - Parse JSON request body                                                      ││
│  │  - Extract _class, _method, _uuid                                              ││
│  │  - Create ProcessServlet instance                                               ││
│  │  - Submit to QueueManager                                                       ││
│  └─────────────────────────────────────────┬───────────────────────────────────────┘│
│                                            │                                         │
│                                            ▼                                         │
│  3. QUEUE MANAGER (Thread Pool)                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  - Check if thread available (MaxWorkerThreads)                                ││
│  │  - Execute ProcessServlet in worker thread                                      ││
│  └─────────────────────────────────────────┬───────────────────────────────────────┘│
│                                            │                                         │
│                                            ▼                                         │
│  4. PROCESS SERVLET (run2 method)                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   ││
│  │  │ AUTHENTICATION CHECK                                                     │   ││
│  │  │                                                                          │   ││
│  │  │  - Is _uuid provided?                                                   │   ││
│  │  │  - Is method allowed without auth?                                      │   ││
│  │  │  - Validate UUID via UserCache.findUser()                               │   ││
│  │  │  - Call checkLogin() if needed                                          │   ││
│  │  │                                                                          │   ││
│  │  │  FAIL → Return { _Success: false, _ErrorCode: 2 }                      │   ││
│  │  └─────────────────────────────────────────────────────────────────────────┘   ││
│  │                                    │                                            ││
│  │                                    ▼                                            ││
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   ││
│  │  │ SERVICE DISPATCH                                                        │   ││
│  │  │                                                                         │   ││
│  │  │  1. Try GroovyService.tryGroovy(_class, _method, ...)                 │   ││
│  │  │     ↓ (if not found)                                                    │   ││
│  │  │  2. Try JavaService.tryJava(_class, _method, ...)                      │   ││
│  │  │     ↓ (if not found)                                                    │   ││
│  │  │  3. Try LispService.tryLisp(_class, _method, ...) [disabled]           │   ││
│  │  │     ↓ (if not found)                                                    │   ││
│  │  │  4. Return NotFound error                                               │   ││
│  │  │                                                                         │   ││
│  │  └─────────────────────────────────────────────────────────────────────────┘   ││
│  │                                    │                                            ││
│  │                                    ▼                                            ││
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   ││
│  │  │ METHOD INVOCATION                                                       │   ││
│  │  │                                                                         │   ││
│  │  │  service.method(injson, outjson, db, servlet)                          │   ││
│  │  │                                                                         │   ││
│  │  │  - Service calls Manager (e.g., PerstUserManager)                      │   ││
│  │  │  - Manager calls PerstStorageManager                                   │   ││
│  │  │  - PerstStorageManager executes operations                              │   ││
│  │  │                                                                         │   ││
│  │  └─────────────────────────────────────────────────────────────────────────┘   ││
│  │                                    │                                            ││
│  └────────────────────────────────────┼────────────────────────────────────────────┘│
│                                       │                                              │
│                                       ▼                                              │
│  5. RESPONSE                                                                       │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  Success: { "_Success": true, "rows": [...] }                                ││
│  │  Error:   { "_Success": false, "_ErrorMessage": "...", "_ErrorCode": N }    ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Perst OODB Integration

### What is Perst?

Perst is a **pure Java object-oriented database** that stores objects directly without SQL translation.

### Key Features Comparison

| Feature | Perst OODBMS | Traditional SQL |
|---------|--------------|-----------------|
| **Data Model** | Objects directly | Tables/Rows |
| **Query Language** | Java methods | SQL |
| **ORM Required** | No | Yes |
| **Versioning** | Built-in (CDatabase) | Manual |
| **Full-text Search** | Lucene integrated | Separate |
| **Storage Format** | Native Java objects | Normalized tables |

### Configuration

**application.ini**:
```ini
# Enable Perst
PerstEnabled = true

# Use CDatabase for versioning and Lucene
PerstUseCDatabase = true

# Database file path (can be relative to app root)
PerstDatabasePath = data/oodb

# Page pool size in bytes (512MB recommended)
PerstPagePoolSize = 536870912

# Disable flushing for better performance
PerstNoflush = false

# Lucene optimize interval (24 hours)
PerstOptimizeInterval = 86400
```

### Configuration Classes

```java
// PerstConfig.java - Singleton configuration
public class PerstConfig {
    private static PerstConfig instance;
    
    private boolean perstEnabled;        // PerstEnabled = true
    private boolean useCDatabase;        // PerstUseCDatabase = true
    private String databasePath;         // PerstDatabasePath
    private long pagePoolSize;           // PerstPagePoolSize
    private boolean noflush;             // PerstNoflush
    private int optimizeInterval;        // PerstOptimizeInterval
    
    public static void initialize() {
        if (instance == null) {
            instance = new PerstConfig();
            instance.load();
        }
    }
    
    private void load() {
        Properties props = MainServlet.readIniFile("application.ini", "main");
        perstEnabled = Boolean.parseBoolean(
            props.getProperty("PerstEnabled", "false")
        );
        // ... load other properties
    }
}
```

### Initialization Sequence

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         PERST INITIALIZATION SEQUENCE                                │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   Application Startup                                                               │
│         │                                                                            │
│         ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  KissInit.groovy → initialize()                                            │   │
│   │  │                                                                          │   │
│   │  │  PerstConfig.initialize()                                               │   │
│   │  │  - Read application.ini                                                 │   │
│   │  │  - Load Perst settings                                                  │   │
│   │  └─────────────────────────────────────────────────────────────────────────┘   │
│   │                                    │                                            │
│   └────────────────────────────────────┼────────────────────────────────────────────┘
│                                        │
│                                        ▼
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  PerstStorageManager.initialize()                                          │   │
│   │  │                                                                          │   │
│   │  │  ┌─────────────────────────────────────────────────────────────────┐    │   │
│   │  │  │ 1. Create Storage                                               │    │   │
│   │  │  │    storage = StorageFactory.createStorage()                     │    │   │
│   │  │  │                                                                  │    │   │
│   │  │  │ 2. Open Database                                                │    │   │
│   │  │  │    storage.open(path, pagePoolSize)                             │    │   │
│   │  │  │    - Creates data/oodb file if not exists                       │    │   │
│   │  │  │                                                                  │    │   │
│   │  │  │ 3. Create UnifiedDBManager                                      │    │   │
│   │  │  │    dbManager = new UnifiedDBManager(storage)                    │    │   │
│   │  │  │    dbManager.open()                                             │    │   │
│   │  │  │                                                                  │    │   │
│   │  │  │ 4. Store in MainServlet environment                             │    │   │
│   │  │  │    MainServlet.putEnvironment("perstDBManager", dbManager)      │    │   │
│   │  │  │                                                                  │    │   │
│   │  │  │ 5. Start Lucene optimizer (if interval > 0)                     │    │   │
│   │  │  │    ScheduledExecutorService.scheduleAtFixedRate(...)            │    │   │
│   │  │  └─────────────────────────────────────────────────────────────────┘    │   │
│   │  │                                                                          │   │
│   │  └─────────────────────────────────────────────────────────────────────────┘   │
│   │                                    │                                            │
│   └────────────────────────────────────┼────────────────────────────────────────────┘
│                                        │
│                                        ▼
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  Initialize Default User (if no users exist)                               │   │
│   │  │                                                                          │   │
│   │  │  PerstUserManager.getAll()                                              │   │
│   │  │  if (empty) {                                                           │   │
│   │  │      create admin/admin with userId = 1                                 │   │
│   │  │  }                                                                      │   │
│   │  │                                                                          │   │
│   │  └─────────────────────────────────────────────────────────────────────────┘   │
│   │                                    │                                            │
│   └────────────────────────────────────┼────────────────────────────────────────────┘
│                                        │
│                                        ▼
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  Perst Ready                                                               │   │
│   │  - Storage open                                                            │   │
│   │  - Database ready                                                          │   │
│   │  - Default admin user created                                              │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Storage Directory Structure

```
data/
├── oodb                    # Main database file (single file)
├── oodb.idx/              # Lucene full-text index directory
│   ├── _0.cfe             # Compound file entries
│   ├── _0.cfs             # Compound file segments
│   ├── _0.si              # Segment info
│   ├── segments_2         # Segment file
│   └── write.lock         # Lock file (when writing)
└── oodb.idx.lex/          # Version history index
    ├── lexicon_*.txt      # Lexicon files
    └── ...                # Other index files
```

---

## Domain Model

### Entity Inheritance Hierarchy

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         DOMAIN ENTITY HIERARCHY                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│                              ┌──────────────────┐                                   │
│                              │     Object       │                                   │
│                              └────────┬─────────┘                                   │
│                                       │                                              │
│                                       │ extends                                      │
│                                       ▼                                              │
│                              ┌──────────────────┐                                   │
│                              │    Persistent    │                                   │
│                              │  (Perst base)    │                                   │
│                              └────────┬─────────┘                                   │
│                                       │                                              │
│               ┌───────────────────────┼───────────────────────┐                     │
│               │                       │                       │                     │
│               │ extends               │ extends               │                     │
│               ▼                       ▼                       ▼                     │
│      ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐          │
│      │    CVersion      │    │    KeyObject     │    │   (other base)   │          │
│      │ (with versioning)│    │  (with key)      │    │                  │          │
│      └────────┬─────────┘    └──────────────────┘    └──────────────────┘          │
│               │                                                                      │
│               │ extends                                                              │
│               │                                                                      │
│    ┌──────────┼──────────┬──────────────────┬─────────────┐                        │
│    │          │          │                  │             │                        │
│    ▼          ▼          ▼                  ▼             ▼                        │
│ ┌──────┐ ┌────────┐ ┌──────────┐  ┌──────────────┐ ┌─────────┐                   │
│ │Actor │ │Perst   │ │Agreement │  │    Group     │ │  Phone  │                   │
│ │      │ │User    │ │          │  │              │ │         │                   │
│ └──────┘ └────────┘ └──────────┘  └──────────────┘ └─────────┘                   │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Core Entity Definitions

#### CVersion (Base for Versioned Entities)

```java
// src/main/precompiled/mycompany/domain/CVersion.java

@Indexable                    // Enables Perst indexing
@FullTextSearchable          // Enables Lucene full-text search
public class CVersion extends Persistent {
    
    private long id;                  // Perst object ID (OID)
    private Date createdDate;         // Creation timestamp
    private Date lastModifiedDate;    // Last update timestamp
    private String createdBy;         // Who created it
    private String modifiedBy;        // Who last modified
    
    // Version tracking (CDatabase)
    private long version;             // Version number
    private Date versionDate;         // Version timestamp
    
    // Default constructor required
    public CVersion() {
        this.createdDate = new Date();
        this.version = 1;
    }
    
    // Getters and setters...
}
```

#### Actor Entity

```java
// src/main/precompiled/mycompany/domain/Actor.java

@Indexable
@FullTextSearchable
public class Actor extends CVersion {
    
    @Indexable
    private String uuid;              // Unique identifier
    
    @Indexable
    private String name;              // Display name
    
    private String type;              // Actor type (USER, SERVICE, etc.)
    private boolean active;           // Active status
    private String userId;            // Numeric user ID
    private Agreement agreement;      // Permission contract
    
    // Default constructor
    public Actor() {
        super();
        this.active = true;
        this.uuid = java.util.UUID.randomUUID().toString();
    }
    
    // Getters and setters...
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("type", type);
        json.put("active", active);
        json.put("userId", userId);
        json.put("createdDate", createdDate);
        return json;
    }
}
```

#### PerstUser Entity

```java
// src/main/precompiled/mycompany/domain/PerstUser.java

@Indexable
public class PerstUser extends CVersion {
    
    @Indexable(unique = true)
    private String username;           // Login username
    
    private String passwordHash;       // SHA-256 hash
    private String salt;               // Password salt
    private String email;              // Email address
    private boolean active;            // Account active
    private boolean emailVerified;     // Email verified
    private Date lastLoginDate;        // Last login timestamp
    private long userId;               // Numeric user ID
    
    // Default constructor
    public PerstUser() {
        super();
        this.active = true;
        this.emailVerified = false;
    }
    
    /**
     * Check if user can login
     */
    public boolean canLogin() {
        return active && emailVerified;
    }
    
    /**
     * Hash password with salt
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    // Getters and setters...
}
```

#### Agreement Entity

```java
// src/main/precompiled/mycompany/domain/Agreement.java

public class Agreement extends CVersion {
    
    private Actor actor;                              // Owner
    private Set<EndpointMethod> allowedMethods;       // Endpoint permissions
    private Map<String, Set<String>> crudPermissions; // CRUD permissions by class
    
    public Agreement() {
        super();
        this.allowedMethods = new HashSet<>();
        this.crudPermissions = new HashMap<>();
    }
    
    /**
     * Check if endpoint is allowed
     */
    public boolean hasEndpointPermission(String service, String method) {
        return allowedMethods.stream()
            .anyMatch(em -> em.matches(service, method));
    }
    
    /**
     * Check CRUD permission for a class
     */
    public boolean hasCrudPermission(String className, String operation) {
        Set<String> perms = crudPermissions.get(className);
        return perms != null && perms.contains(operation.toUpperCase());
    }
    
    /**
     * Add endpoint permission
     */
    public void addEndpointPermission(String service, String method) {
        allowedMethods.add(new EndpointMethod(service, method));
    }
    
    /**
     * Add CRUD permission
     */
    public void addCrudPermission(String className, String... operations) {
        Set<String> perms = crudPermissions.computeIfAbsent(
            className, k -> new HashSet<>()
        );
        for (String op : operations) {
            perms.add(op.toUpperCase());
        }
    }
}
```

### Entity Relationships Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           ENTITY RELATIONSHIPS                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ┌──────────────────┐                    ┌──────────────────┐                      │
│   │      Actor       │                    │   PerstUser      │                      │
│   ├──────────────────┤                    ├──────────────────┤                      │
│   │ uuid             │                    │ username         │                      │
│   │ name             │                    │ passwordHash     │                      │
│   │ type             │                    │ salt             │                      │
│   │ active           │                    │ email            │                      │
│   │ userId ─────────────────────────────────► userId        │                      │
│   │ agreement ───────┼───────┐            │ active           │                      │
│   └──────────────────┘       │            │ emailVerified    │                      │
│                              │            └──────────────────┘                      │
│                              │                                                      │
│                              ▼                                                      │
│                    ┌──────────────────┐                                            │
│                    │   Agreement      │                                            │
│                    ├──────────────────┤                                            │
│                    │ allowedMethods ──┼──► Set<EndpointMethod>                    │
│                    │ crudPermissions ─┼──► Map<String, Set<String>>              │
│                    └──────────────────┘                                            │
│                              │                                                      │
│                              │ owned by                                             │
│                              ▼                                                      │
│   ┌──────────────────┐          ┌──────────────────┐                              │
│   │     Group        │          │  EndpointMethod  │                              │
│   ├──────────────────┤          ├──────────────────┤                              │
│   │ name             │          │ service          │                              │
│   │ members ─────────┼──► Actors│ method           │                              │
│   │ agreement ───────┼───────┐  └──────────────────┘                              │
│   └──────────────────┘       │                                                      │
│                              │                                                      │
│                              ▼                                                      │
│                    ┌──────────────────┐                                            │
│                    │   Agreement      │                                            │
│                    │ (shared perms)   │                                            │
│                    └──────────────────┘                                            │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Manager at the Gate Authorization

### Three-Layer Authorization Model

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                    THREE-LAYER AUTHORIZATION MODEL                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  LAYER 1: ENDPOINT LEVEL (ProcessServlet)                                     ││
│  │  ───────────────────────────────────────────────────────────────────────────── ││
│  │                                                                                 ││
│  │  Question: Is this endpoint allowed for this actor?                           ││
│  │                                                                                 ││
│  │  Implementation:                                                               ││
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   ││
│  │  │  ProcessServlet.checkEndpointPermission(service, method, actor)        │   ││
│  │  │                                                                         │   ││
│  │  │  if (actor == null) {                                                  │   ││
│  │  │      // Not authenticated but method requires auth                     │   ││
│  │  │      return false;                                                     │   ││
│  │  │  }                                                                      │   ││
│  │  │                                                                         │   ││
│  │  │  if (!actor.getAgreement().hasEndpointPermission(service, method)) {   │   ││
│  │  │      return false;  // Endpoint not in allowed list                    │   ││
│  │  │  }                                                                      │   ││
│  │  │                                                                         │   ││
│  │  │  return true;  // Endpoint allowed                                     │   ││
│  │  └─────────────────────────────────────────────────────────────────────────┘   ││
│  │                                                                                 ││
│  │  Result: ALLOW → Continue to Layer 2                                           ││
│  │          DENY  → Return { _Success: false, _ErrorMessage: "Not authorized" }  ││
│  │                                                                                 ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                         │                                           │
│                                         ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  LAYER 2: MANAGER LEVEL (BaseManager)                                         ││
│  │  ───────────────────────────────────────────────────────────────────────────── ││
│  │                                                                                 ││
│  │  Question: Does this actor have CRUD permission for this resource?            ││
│  │                                                                                 ││
│  │  Implementation:                                                               ││
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   ││
│  │  │  BaseManager.checkPermission(actor, operation, resourceClass)          │   ││
│  │  │                                                                         │   ││
│  │  │  if (actor == null || actor.getAgreement() == null) {                  │   ││
│  │  │      throw new NotAuthorizedException("No agreement");                 │   ││
│  │  │  }                                                                      │   ││
│  │  │                                                                         │   ││
│  │  │  String className = resourceClass.getSimpleName();                     │   ││
│  │  │  if (!actor.getAgreement().hasCrudPermission(className, operation)) {  │   ││
│  │  │      throw new NotAuthorizedException("No " + operation + " permission"); │ │
│  │  │  }                                                                      │   ││
│  │  └─────────────────────────────────────────────────────────────────────────┘   ││
│  │                                                                                 ││
│  │  Result: ALLOW → Continue to Layer 3                                           ││
│  │          DENY  → Throw NotAuthorizedException                                 ││
│  │                                                                                 ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                         │                                           │
│                                         ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │  LAYER 3: BUSINESS LOGIC (Service Method)                                     ││
│  │  ───────────────────────────────────────────────────────────────────────────── ││
│  │                                                                                 ││
│  │  Question: Are all business rules satisfied?                                  ││
│  │                                                                                 ││
│  │  Implementation:                                                               ││
│  │  ┌─────────────────────────────────────────────────────────────────────────┐   ││
│  │  │  // Inside service method                                              │   ││
│  │  │  public void deleteActor(JSONObject injson, JSONObject outjson,        │   ││
│  │  │                           Connection db, ProcessServlet servlet) {     │   ││
│  │  │                                                                         │   ││
│  │  │      Actor actor = (Actor) servlet.getAttribute("currentActor");       │   ││
│  │  │                                                                         │   ││
│  │  │      // Layer 1 & 2 handled by ProcessServlet                          │   ││
│  │  │                                                                         │   ││
│  │  │      // Layer 3: Business rules                                        │   ││
│  │  │      long id = injson.getLong("id");                                   │   ││
│  │  │      Actor target = ActorManager.getByOid(id, Actor.class);           │   ││
│  │  │                                                                         │   ││
│  │  │      if (target == null) {                                             │   ││
│  │  │          outjson.put("_Success", false);                               │   ││
│  │  │          outjson.put("_ErrorMessage", "Actor not found");              │   ││
│  │  │          return;                                                       │   ││
│  │  │      }                                                                  │   ││
│  │  │                                                                         │   ││
│  │  │      // Additional business rule: can't delete yourself                │   ││
│  │  │      if (target.getId() == actor.getId()) {                            │   ││
│  │  │          outjson.put("_Success", false);                               │   ││
│  │  │          outjson.put("_ErrorMessage", "Cannot delete yourself");       │   ││
│  │  │          return;                                                       │   ││
│  │  │      }                                                                  │   ││
│  │  │                                                                         │   ││
│  │  │      // Execute operation                                              │   ││
│  │  │      ActorManager.delete(target);                                      │   ││
│  │  │      outjson.put("_Success", true);                                    │   ││
│  │  │  }                                                                      │   ││
│  │  └─────────────────────────────────────────────────────────────────────────┘   ││
│  │                                                                                 ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Permission Setup in KissInit.groovy

```groovy
// src/main/backend/KissInit.groovy

// Create default admin user
if (PerstUserManager.getAll().isEmpty()) {
    // Create admin user
    PerstUser admin = PerstUserManager.create("admin", "admin", 1)
    admin.setEmailVerified(true)  // Required for canLogin()
    PerstUserManager.update(admin)
    
    // Create admin actor
    Actor adminActor = ActorManager.create("admin", "USER", null)
    
    // Create agreement with permissions
    Agreement agreement = new Agreement()
    
    // Allow endpoints without auth for setup
    MainServlet.allowWithoutAuthentication("", "Login")
    MainServlet.allowWithoutAuthentication("services.Users", "addRecord")
    
    // Admin permissions (full access)
    agreement.addCrudPermission("Actor", "GET", "POST", "PUT", "DELETE")
    agreement.addCrudPermission("PerstUser", "GET", "POST", "PUT", "DELETE")
    agreement.addCrudPermission("Agreement", "GET", "POST", "PUT", "DELETE")
    agreement.addCrudPermission("Group", "GET", "POST", "PUT", "DELETE")
    
    // Save agreement
    PerstStorageManager.insert(agreement)
    adminActor.setAgreement(agreement)
    PerstStorageManager.update(adminActor)
}
```

### Authorization Decision Table

| Endpoint | Actor | Has Endpoint Permission? | Has CRUD Permission? | Result |
|----------|-------|-------------------------|---------------------|--------|
| `""` (Login) | Any | Not required | N/A | ALLOW |
| `services.Users.getRecords` | Admin | ✅ Yes (GET) | ✅ Yes (User:GET) | ALLOW |
| `services.Users.addRecord` | Admin | ✅ Yes (POST) | ✅ Yes (User:POST) | ALLOW |
| `services.Users.getRecords` | Guest | ❌ No | ❌ No | DENY |
| `services.ActorService.delete` | Limited | ✅ Yes | ❌ No (missing DELETE) | DENY |

---

## Backend Services

### Service Discovery Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         SERVICE DISCOVERY FLOW                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   Request: { _class: "services.Users", _method: "getRecords", ... }               │
│                                                                                      │
│   ┌────────────────────────────────────────────────────────────────────────────────┐│
│   │  ProcessServlet.run2()                                                        ││
│   │  │                                                                            ││
│   │  │  String className = "services.Users";                                    ││
│   │  │  String methodName = "getRecords";                                       ││
│   │  │                                                                            ││
│   │  │  // Try Groovy                                                            ││
│   │  │  if (GroovyService.tryGroovy(className, methodName, ...)) {              ││
│   │  │      return;  // Found and executed                                       ││
│   │  │  }                                                                         ││
│   │  │                                                                            ││
│   │  │  // Try Java                                                              ││
│   │  │  if (JavaService.tryJava(className, methodName, ...)) {                  ││
│   │  │      return;  // Found and executed                                       ││
│   │  │  }                                                                         ││
│   │  │                                                                            ││
│   │  │  // Try Lisp (disabled)                                                   ││
│   │  │  if (LispService.tryLisp(className, methodName, ...)) {                  ││
│   │  │      return;                                                               ││
│   │  │  }                                                                         ││
│   │  │                                                                            ││
│   │  │  // Not found                                                              ││
│   │  │  return NotFound;                                                          ││
│   │  │                                                                            ││
│   │  └────────────────────────────────────────────────────────────────────────────┘││
│   └────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Service Language Support

| Language | File Extension | Location | Auto-Reload | Notes |
|----------|----------------|----------|-------------|-------|
| **Groovy** | `.groovy` | `backend/services/` | ✅ Yes | Recommended for dynamic logic |
| **Java** | `.java` | `backend/services/` | ✅ Yes | For performance-critical code |
| **Lisp** | `.lisp` | `backend/services/` | ❌ Disabled | Requires abcl.jar |

### Service Signature (All Languages)

```java
// Required method signature for all services
void methodName(JSONObject injson, JSONObject outjson, 
                Connection db, ProcessServlet servlet) throws Exception
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `injson` | `JSONObject` | Request parameters from frontend |
| `outjson` | `JSONObject` | Response to send back |
| `db` | `Connection` | Database connection (null if no SQL) |
| `servlet` | `ProcessServlet` | HTTP request/response access |

### Complete Service Example

```groovy
// src/main/backend/services/Users.groovy

class Users {
    
    /**
     * Get all users
     */
    void getRecords(JSONObject injson, JSONObject outjson, 
                    Connection db, ProcessServlet servlet) {
        try {
            // Get authenticated actor from servlet
            Actor actor = (Actor) servlet.getAttribute("currentActor")
            
            // Manager-level permission check
            BaseManager.checkPermission(actor, "GET", PerstUser.class)
            
            // Get all users
            Collection<PerstUser> users = PerstUserManager.getAll()
            
            // Build response
            JSONArray rows = new JSONArray()
            for (PerstUser user : users) {
                rows.put(userToJSON(user))
            }
            
            outjson.put("_Success", true)
            outjson.put("rows", rows)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.getMessage())
        }
    }
    
    /**
     * Add new user
     */
    void addRecord(JSONObject injson, JSONObject outjson,
                   Connection db, ProcessServlet servlet) {
        try {
            String userName = injson.getString("userName")
            String userPassword = injson.getString("userPassword")
            long userId = injson.optLong("userId", System.currentTimeMillis())
            
            // Create user
            PerstUser user = PerstUserManager.create(userName, userPassword, userId)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("id", user.getId())
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.getMessage())
            outjson.put("error", e.getMessage())
        }
    }
    
    /**
     * Delete user
     */
    void deleteRecord(JSONObject injson, JSONObject outjson,
                      Connection db, ProcessServlet servlet) {
        try {
            long id = injson.getLong("id")
            PerstUser user = PerstUserManager.getByOid(id, PerstUser.class)
            
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "User not found")
                outjson.put("error", "User not found")
                return
            }
            
            PerstUserManager.delete(user)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.getMessage())
            outjson.put("error", e.getMessage())
        }
    }
    
    /**
     * Convert PerstUser to JSON
     */
    private JSONObject userToJSON(PerstUser user) {
        JSONObject json = new JSONObject()
        json.put("id", user.getId())
        json.put("userName", user.getUsername())
        json.put("userPassword", "********")  // Never expose real password
        json.put("userActive", user.isActive() ? "Y" : "N")
        return json
    }
}
```

### Available Services

| Service | File | Methods | Description |
|---------|------|---------|-------------|
| `services.Users` | `Users.groovy` | `getRecords`, `addRecord`, `updateRecord`, `deleteRecord` | User CRUD |
| `services.ActorService` | `ActorService.java` | `getAll`, `create`, `update`, `delete`, `search` | Actor CRUD |
| `services.Benchmark` | `Benchmark.groovy` | `run`, `getResults` | Performance testing |
| `services.Login` | (built-in) | `Login`, `Logout`, `checkLogin` | Authentication |

---

## REST API

### API Endpoint Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              REST API ENDPOINTS                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   Single Endpoint: POST /rest                                                       │
│                                                                                      │
│   ┌────────────────────────────────────────────────────────────────────────────────┐│
│   │  REQUEST FORMAT (JSON)                                                       ││
│   │  ┌─────────────────────────────────────────────────────────────────────────┐ ││
│   │  │ {                                                                       │ ││
│   │  │   "_class": "services.Users",     // Service class (required)          │ ││
│   │  │   "_method": "getRecords",        // Method name (required)            │ ││
│   │  │   "_uuid": "abc-123-def",         // Session UUID (for auth)           │ ││
│   │  │   "param1": "value1",             // Additional parameters             │ ││
│   │  │   "param2": "value2"                                                  │ ││
│   │  │ }                                                                       │ ││
│   │  └─────────────────────────────────────────────────────────────────────────┘ ││
│   └────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
│   ┌────────────────────────────────────────────────────────────────────────────────┐│
│   │  RESPONSE FORMAT (JSON)                                                      ││
│   │  ┌─────────────────────────────────────────────────────────────────────────┐ ││
│   │  │ SUCCESS:                                                                │ ││
│   │  │ {                                                                       │ ││
│   │  │   "_Success": true,                                                     │ ││
│   │  │   ... additional data ...                                               │ ││
│   │  │ }                                                                       │ ││
│   │  │                                                                         │ ││
│   │  │ ERROR:                                                                  │ ││
│   │  │ {                                                                       │ ││
│   │  │   "_Success": false,                                                    │ ││
│   │  │   "_ErrorMessage": "Description of error",                             │ ││
│   │  │   "_ErrorCode": 1                                                      │ ││
│   │  │ }                                                                       │ ││
│   │  └─────────────────────────────────────────────────────────────────────────┘ ││
│   └────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Core Methods (when _class is empty)

| Method | Auth Required | Request | Response | Description |
|--------|---------------|---------|----------|-------------|
| `Login` | ❌ No | `{ username, password }` | `{ _Success, uuid }` | Authenticate user |
| `Logout` | ✅ Yes | `{ _uuid }` | `{ _Success }` | End session |
| `checkLogin` | ✅ Yes | `{ _uuid }` | `{ _Success }` | Verify session |
| `LoginRequired` | ❌ No | `{}` | `{ _Success, required }` | Check if auth needed |

### Users Service Methods

| Method | Auth | Request | Response | Description |
|--------|------|---------|----------|-------------|
| `getRecords` | ✅ | `{}` | `{ rows: [...] }` | Get all users |
| `addRecord` | ❌* | `{ userName, userPassword, userActive }` | `{ success, id }` | Create user |
| `updateRecord` | ✅ | `{ id, userName, userPassword, userActive }` | `{ success }` | Update user |
| `deleteRecord` | ✅ | `{ id }` | `{ success }` | Delete user |

*Allowed without auth for first-time setup

### Error Codes

| Code | Meaning | Action |
|------|---------|--------|
| 1 | General error | Display `_ErrorMessage` |
| 2 | Session expired | Redirect to login |
| (no code) | Success | Process `_Success: true` response |

### Example API Calls

```bash
# Login
curl -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"","_method":"Login","username":"admin","password":"admin"}'

# Get Users (with UUID)
curl -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"services.Users","_method":"getRecords","_uuid":"abc-123"}'

# Add User
curl -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"services.Users","_method":"addRecord","userName":"test","userPassword":"pass123","userActive":"Y"}'
```

---

## Authentication System

### Authentication Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         AUTHENTICATION FLOW                                          │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ┌─────────────────────┐         ┌─────────────────┐        ┌────────────────────┐  │
│  │      Frontend       │         │ ProcessServlet  │        │    UserCache       │  │
│  └──────────┬──────────┘         └────────┬────────┘        └─────────┬──────────┘  │
│             │                              │                          │              │
│             │ 1. POST /rest               │                          │              │
│             │    { "", "Login", user, pw } │                          │              │
│             └─────────────────────────────>│                          │              │
│                                           │                          │              │
│                                           │ 2. Check if endpoint    │              │
│                                           │    allows without auth   │              │
│                                           │    (Login is allowed)    │              │              │
│                                           │                          │              │
│                                           │ 3. Call Login.login()    │              │
│                                           │─────────────────────────>│              │
│                                           │                          │              │
│                                           │                          │ 4. Validate  │
│                                           │                          │    credentials│
│                                           │                          │              │
│                                           │                          │ 5. Create    │
│                                           │                          │    UserData  │
│                                           │                          │    (UUID)    │
│                                           │                          │              │
│                                           │<─────────────────────────┤              │
│                                           │   UserData with UUID     │              │
│                                           │                          │              │
│             │ 6. Return { _Success, uuid }│                          │              │
│             │<─────────────────────────────│                          │              │
│             │                              │                          │              │
│             │ 7. Store UUID               │                          │              │
│             │    session.setUUID(uuid)    │                          │              │
│             │                              │                          │              │
│             │                              │                          │              │
│             │ ─────────────────────────────────────────────────────────────────────│
│             │  SUBSEQUENT REQUESTS                                                    │
│             │ ─────────────────────────────────────────────────────────────────────│
│             │                              │                          │              │
│             │ 8. POST /rest               │                          │              │
│             │    { "services.Users",       │                          │              │
│             │      "getRecords",          │                          │              │
│             │      "_uuid": "abc-123" }   │                          │              │
│             └─────────────────────────────>│                          │              │
│                                           │                          │              │
│                                           │ 9. Validate UUID         │              │
│                                           │    findUser(uuid)────────>│              │
│                                           │                          │              │
│                                           │<──────────────────────────┤              │
│                                           │    UserData or null       │              │
│                                           │                          │              │
│                                           │ 10. If valid, proceed    │              │
│                                           │     with service call    │              │
│                                           │                          │              │
│             │ 11. Response                │                          │              │
│             │<─────────────────────────────│                          │              │
│             │                              │                          │              │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Session Management Classes

```java
// UserData.java - Session data for each user

public class UserData {
    private String username;
    private String password;     // For re-auth if needed
    private long userId;
    private String UUID;
    private Date lastAccess;
    private Map<String, Object> data;  // Custom session data
    
    public UserData() {
        this.lastAccess = new Date();
        this.data = new HashMap<>();
    }
    
    // Getters and setters...
}

// UserCache.java - Static session management

public class UserCache {
    private static Hashtable<String, UserData> uuidTable = new Hashtable<>();
    private static int userInactiveSeconds = 1800;  // 30 minutes
    
    /**
     * Create new session
     */
    public static UserData newUser(String username, String password, long userId) {
        UserData ud = new UserData();
        ud.setUsername(username);
        ud.setPassword(password);
        ud.setUserId(userId);
        ud.setUUID(UUID.randomUUID().toString());
        ud.setLastAccess(new Date());
        
        uuidTable.put(ud.getUUID(), ud);
        return ud;
    }
    
    /**
     * Find session by UUID
     */
    public static UserData findUser(String uuid) {
        UserData ud = uuidTable.get(uuid);
        if (ud != null) {
            // Check expiration
            long inactive = System.currentTimeMillis() - ud.getLastAccess().getTime();
            if (inactive > userInactiveSeconds * 1000L) {
                uuidTable.remove(uuid);
                return null;
            }
            ud.setLastAccess(new Date());
        }
        return ud;
    }
    
    /**
     * Remove session (logout)
     */
    public static void removeUser(String uuid) {
        UserData ud = uuidTable.remove(uuid);
        if (ud != null && logoutHandler != null) {
            logoutHandler.onLogout(ud);
        }
    }
}
```

### Login Service Implementation

```groovy
// src/main/backend/Login.groovy (simplified)

class Login {
    
    /**
     * Called by ProcessServlet for Login requests
     */
    static void login(String username, String password, JSONObject outjson) {
        try {
            // Authenticate with PerstUserManager
            PerstUser user = PerstUserManager.authenticate(username, password)
            
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Invalid credentials")
                return
            }
            
            // Create session
            UserData ud = UserCache.newUser(
                user.getUsername(), 
                password, 
                user.getUserId()
            )
            
            // Set additional session data
            ud.getData().put("actorId", findActorId(user))
            
            // Return UUID
            outjson.put("_Success", true)
            outjson.put("uuid", ud.getUUID())
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.getMessage())
        }
    }
    
    /**
     * Find actor ID for user
     */
    private static long findActorId(PerstUser user) {
        Collection<Actor> actors = ActorManager.getAll();
        return actors.stream()
            .filter(a -> a.getUserId().equals(String.valueOf(user.getUserId())))
            .map(Actor::getId)
            .findFirst()
            .orElse(0L);
    }
}
```

---

## Frontend-Backend Integration

### Integration Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                    FRONTEND-BACKEND INTEGRATION                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   SVELTE 5 FRONTEND                            KISS BACKEND                         │
│   ─────────────────                            ─────────────                         │
│                                                                                      │
│   ┌──────────────────────────┐              ┌──────────────────────────┐           │
│   │   API Modules            │              │   Services               │           │
│   │   ($lib/api/)            │              │   (backend/services/)    │           │
│   │                          │              │                          │           │
│   │   ┌──────────────────┐   │    HTTP/JSON │   ┌──────────────────┐   │           │
│   │   │ Auth.ts          │   │◄─────────────┼───│ Login.groovy     │   │           │
│   │   │ - login()        │   │              │   │                  │   │           │
│   │   │ - logout()       │   │              │   └──────────────────┘   │           │
│   │   │ - signup()       │   │              │                          │           │
│   │   └──────────────────┘   │              │   ┌──────────────────┐   │           │
│   │                          │              │   │ Users.groovy     │   │           │
│   │   ┌──────────────────┐   │    HTTP/JSON │   │                  │   │           │
│   │   │ Users.ts         │   │◄─────────────┼───│ - getRecords()   │   │           │
│   │   │ - getUsers()     │   │              │   │ - addRecord()    │   │           │
│   │   │ - addUser()      │   │              │   │ - deleteRecord() │   │           │
│   │   │ - deleteUser()   │   │              │   │                  │   │           │
│   │   └──────────────────┘   │              │   └──────────────────┘   │           │
│   │                          │              │                          │           │
│   └──────────────────────────┘              └──────────────────────────┘           │
│               │                                          │                          │
│               │                                          │                          │
│               ▼                                          ▼                          │
│   ┌──────────────────────────┐              ┌──────────────────────────┐           │
│   │   Server.ts              │              │   ProcessServlet         │           │
│   │                          │    POST      │                          │           │
│   │   POST /rest             │──────────────>│  /rest endpoint         │           │
│   │   { _class, _method,     │              │                          │           │
│   │     _uuid, ...params }   │              │  - Validate UUID         │           │
│   │                          │◄──────────────│  - Dispatch service     │           │
│   │   Response:              │   JSON       │  - Return result         │           │
│   │   { _Success, ...data }  │              │                          │           │
│   └──────────────────────────┘              └──────────────────────────┘           │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Frontend-Backend Call Example

**Frontend (TypeScript):**
```typescript
// src/lib/api/Users.ts
import { Server } from '$lib/services/Server';

export async function getUsers(): Promise<User[]> {
  const res = await Server.call('services.Users', 'getRecords', {});
  return res.rows || [];
}
```

**Backend (Groovy):**
```groovy
// src/main/backend/services/Users.groovy
class Users {
    void getRecords(JSONObject injson, JSONObject outjson, 
                    Connection db, ProcessServlet servlet) {
        Collection<PerstUser> users = PerstUserManager.getAll()
        outjson.put("_Success", true)
        outjson.put("rows", users.collect { it.toJSON() })
    }
}
```

### Data Flow Example

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE DATA FLOW EXAMPLE                                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  Scenario: User views list of users in the UI                                      │
│                                                                                      │
│  1. User clicks "Users" link                                                        │
│     │                                                                                │
│     ▼                                                                                │
│  2. users/+page.svelte mounts                                                       │
│     │                                                                                │
│     ▼                                                                                │
│  3. onMount() calls getUsers()                                                      │
│     │                                                                                │
│     ▼                                                                                │
│  4. Users.ts: getUsers()                                                            │
│     │                                                                                │
│     ▼                                                                                │
│  5. Server.call('services.Users', 'getRecords', {})                                │
│     │                                                                                │
│     ▼                                                                                │
│  6. POST http://localhost:8080/rest                                                │
│     { "_class": "services.Users", "_method": "getRecords", "_uuid": "abc" }       │
│     │                                                                                │
│     ▼                                                                                │
│  7. ProcessServlet receives request                                                 │
│     │                                                                                │
│     ▼                                                                                │
│  8. Validate UUID via UserCache.findUser()                                         │
│     │                                                                                │
│     ▼                                                                                │
│  9. Check endpoint permission (Layer 1)                                             │
│     │                                                                                │
│     ▼                                                                                │
│  10. Dispatch to Users.groovy                                                       │
│      │                                                                               │
│      ▼                                                                               │
│  11. Users.getRecords() calls PerstUserManager.getAll()                            │
│      │                                                                               │
│      ▼                                                                               │
│  12. PerstStorageManager.getAll(PerstUser.class)                                   │
│      │                                                                               │
│      ▼                                                                               │
│  13. Returns Collection<PerstUser>                                                  │
│      │                                                                               │
│      ▼                                                                               │
│  14. Build JSON response: { "_Success": true, "rows": [...] }                      │
│      │                                                                               │
│      ▼                                                                               │
│  15. Return to frontend                                                             │
│      │                                                                               │
│      ▼                                                                               │
│  16. Users.ts returns users array                                                   │
│      │                                                                               │
│      ▼                                                                               │
│  17. Component updates UI                                                           │
│      │                                                                               │
│      ▼                                                                               │
│  18. User sees list of users                                                        │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Configuration Reference

### application.ini Settings

```ini
[main]

# === General Settings ===
MaxWorkerThreads = 100
UserInactiveSeconds = 1800
RequireAuthentication = true

# === Perst OODB Settings ===
PerstEnabled = true
PerstUseCDatabase = true
PerstDatabasePath = /path/to/data/oodb
PerstPagePoolSize = 536870912
PerstNoflush = false
PerstOptimizeInterval = 86400
```

### Configuration Table

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `MaxWorkerThreads` | int | 100 | Max concurrent requests |
| `UserInactiveSeconds` | int | 1800 | Session timeout (30 min) |
| `RequireAuthentication` | boolean | true | Require auth for all endpoints |
| `PerstEnabled` | boolean | false | Enable Perst OODBMS |
| `PerstUseCDatabase` | boolean | true | Use CDatabase for versioning |
| `PerstDatabasePath` | string | data/oodb | Database file path |
| `PerstPagePoolSize` | long | 536870912 | Page cache (512MB) |
| `PerstNoflush` | boolean | false | Disable flushing |
| `PerstOptimizeInterval` | int | 86400 | Lucene optimize interval (seconds) |

### Environment Variables

| Variable | Used By | Description |
|----------|---------|-------------|
| `KISS_HOME` | MainServlet | Application home directory |
| `JAVA_HOME` | Build scripts | Java installation |

---

## Build System

### Custom `bld` Tool Commands

```bash
# Development (hot reload)
./bld develop

# Build without running
./bld build

# Build for unit tests
./bld unit-tests

# Run tests
java -jar work/KissUnitTest.jar

# Clean build artifacts
./bld clean

# List available tasks
./bld list-tasks
```

### Build Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            BUILD FLOW                                                │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ./bld develop                                                                      │
│         │                                                                            │
│         ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  1. Compile precompiled classes                                             │   │
│   │     - Domain entities (CVersion, Actor, PerstUser, etc.)                   │   │
│   │     - Manager classes (ActorManager, PerstUserManager, etc.)               │   │
│   │     - Perst configuration (PerstConfig, PerstStorageManager)               │   │
│   │                                                                             │   │
│   │  2. Compile KISS core (if changed)                                          │   │
│   │     - src/main/core/                                                       │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│         │                                                                            │
│         ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  3. Build frontend (Svelte)                                                │   │
│   │     cd src/main/frontend-svelte                                             │   │
│   │     npm run build                                                           │   │
│   │     → Output to work/                                                      │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│         │                                                                            │
│         ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  4. Create WAR file                                                         │   │
│   │     → work/frontend.war                                                    │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│         │                                                                            │
│         ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  5. Start Tomcat                                                            │   │
│   │     - Deploy WAR                                                           │   │
│   │     - Run KissInit.groovy (initialize Perst)                               │   │
│   │     - Start REST endpoint                                                  │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│         │                                                                            │
│         ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  6. Ready for development                                                   │   │
│   │     - Backend: http://localhost:8080                                       │   │
│   │     - Frontend dev: http://localhost:5173                                  │   │
│   │     - Services hot-reload on save                                          │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Directory Structure After Build

```
work/
├── KissUnitTest.jar      # Unit test runner
├── frontend.war          # Deployable WAR file
├── classes/              # Compiled classes
│   ├── precompiled/      # Domain, managers, Perst config
│   ├── backend/          # Services
│   └── core/             # Kiss framework
└── libs/                 # Dependencies (from libs/)
```

---

## Development Guide

### Quick Start

1. **Clone and setup**:
```bash
git clone <repo>
cd KissOO
./bld develop
```

2. **Configure Perst** in `src/main/backend/application.ini`:
```ini
PerstEnabled = true
PerstDatabasePath = data/oodb
```

3. **Start backend** (if not auto-started):
```bash
./startBackend.sh
```

4. **Start frontend dev server**:
```bash
cd src/main/frontend-svelte
npm run dev
```

### Development Workflow

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            DEVELOPMENT WORKFLOW                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ┌────────────────────────────────────────────────────────────────────────────────┐ │
│  │  Scenario 1: Modify a Service (Backend)                                      │ │
│  │  ──────────────────────────────────────────────────────────────────────────── │ │
│  │                                                                               │ │
│  │  1. Edit file in src/main/backend/services/                                  │ │
│  │     e.g., Users.groovy                                                        │ │
│  │                                                                               │ │
│  │  2. Save file                                                                 │ │
│  │                                                                               │ │
│  │  3. GroovyService auto-detects change                                        │ │
│  │                                                                               │ │
│  │  4. Re-compiles class in memory                                              │ │
│  │                                                                               │ │
│  │  5. Next request uses new code                                               │ │
│  │                                                                               │ │
│  │  No restart needed!                                                          │ │
│  └────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                      │
│  ┌────────────────────────────────────────────────────────────────────────────────┐ │
│  │  Scenario 2: Modify a Domain Entity (Precompiled)                            │ │
│  │  ──────────────────────────────────────────────────────────────────────────── │ │
│  │                                                                               │ │
│  │  1. Edit file in src/main/precompiled/mycompany/domain/                      │ │
│  │     e.g., Actor.java                                                          │ │
│  │                                                                               │ │
│  │  2. Save file                                                                 │ │
│  │                                                                               │ │
│  │  3. Restart required! (precompiled code)                                     │ │
│  │                                                                               │ │
│  │  Run: ./bld develop                                                           │ │
│  └────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                      │
│  ┌────────────────────────────────────────────────────────────────────────────────┐ │
│  │  Scenario 3: Modify Frontend (Svelte)                                        │ │
│  │  ──────────────────────────────────────────────────────────────────────────── │ │
│  │                                                                               │ │
│  │  1. Edit file in src/main/frontend-svelte/src/                               │ │
│  │     e.g., routes/users/+page.svelte                                          │ │
│  │                                                                               │ │
│  │  2. Save file                                                                 │ │
│  │                                                                               │ │
│  │  3. Vite HMR updates browser                                                 │ │
│  │                                                                               │ │
│  │  No restart needed!                                                          │ │
│  └────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Testing

```bash
# Run all unit tests
java -jar work/KissUnitTest.jar

# Run Perst-specific tests
java -jar work/KissUnitTest.jar --select-package=oodb

# Run KISS core tests
java -jar work/KissUnitTest.jar --select-package=org.kissweb
```

### Adding New Domain Classes

1. **Create entity** in `src/main/precompiled/mycompany/domain/`:
```java
public class MyEntity extends CVersion {
    @Indexable
    private String name;
    // ... fields, getters, setters
}
```

2. **Create manager** in `src/main/precompiled/mycompany/database/`:
```java
public class MyEntityManager extends BaseManager<MyEntity> {
    public static Collection<MyEntity> getAll() {
        return PerstStorageManager.getAll(MyEntity.class);
    }
    // ... other CRUD methods
}
```

3. **Create service** in `src/main/backend/services/`:
```groovy
class MyEntityService {
    void getRecords(JSONObject injson, JSONObject outjson,
                    Connection db, ProcessServlet servlet) {
        // Implementation
    }
}
```

4. **Restart** to compile precompiled classes.

### Adding New Services

No restart needed - just create `.groovy` or `.java` file in `src/main/backend/services/`:

```groovy
// src/main/backend/services/MyService.groovy
class MyService {
    void myMethod(JSONObject injson, JSONObject outjson,
                  Connection db, ProcessServlet servlet) {
        outjson.put("_Success", true)
        outjson.put("data", "Hello!")
    }
}
```

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| "Invalid credentials" | Wrong password or user not found | Check admin/admin or create new user |
| "Session expired" | UUID not found in UserCache | Re-login to get new UUID |
| "Service not found" | Service class/method doesn't exist | Check file name and method signature |
| Perst won't start | Database path invalid | Check `PerstDatabasePath` setting |
| Hot reload not working | File not in `backend/services/` | Ensure service is in correct directory |

### Debugging Tips

1. **Enable logging** - Check Tomcat logs in `tomcat/logs/catalina.out`

2. **Test API directly**:
```bash
curl -X POST http://localhost:8080/rest \
  -H "Content-Type: application/json" \
  -d '{"_class":"","_method":"Login","username":"admin","password":"admin"}'
```

3. **Check Perst database**:
```bash
ls -la data/oodb*  # Should show database file and index directories
```

4. **Verify configuration**:
```bash
cat src/main/backend/application.ini | grep Perst
```

---

## Quick Reference

### Key Classes

| Class | Location | Purpose |
|-------|----------|---------|
| `PerstStorageManager` | `precompiled/oodb/` | Perst operations |
| `PerstConfig` | `precompiled/oodb/` | Configuration |
| `BaseManager<T>` | `precompiled/mycompany/database/` | CRUD base |
| `Actor` | `precompiled/mycompany/domain/` | Main entity |
| `PerstUser` | `precompiled/mycompany/domain/` | User entity |
| `ProcessServlet` | `core/org/kissweb/restServer/` | Request handler |
| `UserCache` | `core/org/kissweb/restServer/` | Session storage |

### Common Operations

```java
// CRUD Operations
Collection<Actor> all = ActorManager.getAll();
Actor found = ActorManager.getByName("John");
Actor created = ActorManager.create("John", "USER", agreement);
ActorManager.update(actor);
ActorManager.delete(actor);

// Search
Collection<Actor> results = ActorManager.search("search term");

// Authentication
PerstUser user = PerstUserManager.authenticate("admin", "admin");
```

### API Response Format

```json
// Success
{ "_Success": true, "rows": [...] }

// Error
{ "_Success": false, "_ErrorMessage": "Error message", "_ErrorCode": 1 }

// Login Success
{ "_Success": true, "uuid": "session-uuid-here" }
```

---

## See Also

- **sv5guide.md** - Svelte 5 frontend guide
- **PERST_USAGE.md** - Detailed Perst usage
- **MANAGER_AT_THE_GATE.md** - Authorization pattern
- **AI/KnowledgeBase.md** - Framework knowledge base

---

*Last Updated: 2026-03-22*
