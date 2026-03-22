# KissOO Project Overview

## Executive Summary

KissOO is a **Perst OODBMS integration fork** of the KISS web application framework. This project adds object-oriented database capabilities to the existing Java-based full-stack framework, implementing a sophisticated "Manager at the Gate" authorization pattern.

## Project Overview

**Core Technology Stack:**
- **Framework**: KISS (Java-based full-stack web framework)
- **Database**: Perst OODBMS (Object-Oriented Database) with optional CDatabase versioning
- **Languages**: Java, Groovy, JavaScript, HTML/CSS
- **Build System**: Custom `bld` tool (not Gradle/Maven)
- **Frontend**: Svelte 5 + AG-Grid + custom KissJS components

## Architecture Analysis

### 1. **Perst Integration Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP Request                                 │
│            { _class, _method, _uuid, ... }                     │
└─────────────────────────┬───────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Service Layer                                │
│         (ActorService, UserService, etc.)                      │
│                                                                │
│   MUST obtain Actor from UserData and pass to Managers        │
└─────────────────────────┬───────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Manager Layer                                │
│   ┌─────────────┐  ┌──────────────┐  ┌─────────────┐          │
│   │ActorManager │  │UserManager   │  │ XXXManager  │          │
│   └──────┬──────┘  └──────┬───────┘  └──────┬──────┘          │
│          │                 │                  │                 │
│   ┌──────┴─────────────────┴──────────────────┴──────┐        │
│   │     ✓ Authorization Check                        │        │
│   │     ✓ Validation                                  │        │
│   │     ✓ Business Logic                              │        │
│   └──────────────────┬─────────────────────────────────┘        │
└──────────────────────┼─────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│                  PerstHelper (Data Access)                     │
│         Thread-safe, per-session isolation                     │
└─────────────────────────────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────────┐
│                  Perst OODBMS                                   │
│            (CVersion Objects)                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 2. **Authorization Pattern: "Manager at the Gate"**

**Critical Security Implementation:**
- **Every service call MUST go through authorization**
- **Two-level enforcement**: EndpointMethod + Manager level
- **Actor-based authorization** (not session-based)
- **Agreement contracts** define permissions
- **Default deny** - everything blocked unless explicitly granted

**Key Components:**
- `EndpointMethod` - Type-safe endpoint identifiers
- `Agreement` - Permission contracts (CRUD + EndpointMethod + Group)
- `Actor` - MUST have Agreement to perform operations
- `Group` - UNIX-like group permissions
- `BaseManager` - Abstract base with authorization checks

### 3. **Domain Model Structure**

**Core Domain Entities:**
- `Actor` - Generic entity with authorization (extends CVersion)
- `PerstUser` - User entity for authentication (extends CVersion)
- `Agreement` - Authorization contract (extends CVersion)
- `Group` - Group-based permissions (extends CVersion)
- `EndpointMethod` - Type-safe service endpoints

**Manager Pattern:**
- `ActorManager` - Manages Actor entities
- `PerstUserManager` - Manages users + authentication
- `BaseManager<T>` - Abstract base with authorization
- All data access goes through Managers (never direct)

### 4. **Perst Database Architecture**

**Storage Strategy:**
- **Single shared Storage instance** via MainServlet environment
- **Thread-local transactions** for isolation
- **CDatabase option** for automatic versioning
- **FieldIndex** for fast lookups
- **No SQL database** - pure object storage

**Configuration:**
```ini
PerstEnabled = true
PerstUseCDatabase = false  # Versioning enabled
PerstDatabasePath = ../../../data/oodb
PerstPagePoolSize = 536870912  # 512MB cache
```

## Key Features

### 1. **Multi-Language Service Support**
- **Java Services** - Full compiled performance
- **Groovy Services** - Dynamic compilation, auto-reload
- **Lisp Services** - Common Lisp integration
- **Script-based** - External Groovy scripts

### 2. **Frontend Technologies**
- **Svelte 5** - Modern reactive framework
- **AG-Grid** - Enterprise data grid
- **Custom KissJS** - Framework-specific components
- **Mobile support** - Responsive design

### 3. **Development Workflow**
- **Hot reload** - Services compile on change
- **No build process** - Direct file-based deployment
- **Auto-compilation** - Java/Groovy/Lisp services
- **Custom bld tool** - Project-specific build system

### 4. **Testing Infrastructure**
- **JUnit 5** - Modern testing framework
- **Perst-specific tests** - 20/20 passing tests
- **Integration tests** - Full CRUD operations
- **Transaction testing** - Versioning and rollback

## Project Structure

```
src/main/
├── precompiled/          # Core framework code
│   ├── mycompany/domain/     # Domain entities (Actor, Agreement, Group, PerstUser)
│   ├── mycompany/database/   # Manager classes (ActorManager, PerstHelper)
│   └── oodb/                 # Perst configuration (PerstConfig, PerstContext)
├── backend/              # REST services (frequently changing)
│   └── services/            # Service implementations
├── frontend/             # Web interface
│   ├── kiss/               # KissJS framework
│   ├── svelte/             # Svelte 5 frontend
│   └── mobile/             # Mobile interface
└── core/                 # Kiss framework core
```

## Security Implementation

### **Authorization Flow**
1. **Request** → EndpointMethod.execute()
2. **Authentication** → Get Actor from UserData
3. **Authorization** → Check Agreement permissions
4. **Execution** → Manager performs operation
5. **Storage** → PerstHelper handles persistence

### **Permission Types**
1. **CRUD Permissions** - `grant(Actor.class, CRUD.CREATE)`
2. **EndpointMethod** - `grant(ActorService.GET_ACTOR)` (type-safe!)
3. **Group Permissions** - Via Group membership

### **Security Guarantees**
- **Type-safe** - No string typos in permissions
- **Default deny** - Everything blocked unless granted
- **Three layers** - Endpoint + Manager + Agreement
- **Audit trail** - CVersion provides automatic versioning

## Development Guidelines

### **Critical Rules**
1. **NEVER access Perst directly** - Always use Managers
2. **ALWAYS pass Actor** - Authorization requires Actor context
3. **USE EndpointMethod** - Type-safe endpoint identification
4. **CHECK permissions** - Managers handle authorization automatically
5. **HANDLE transactions** - Use PerstHelper for transaction management

### **Best Practices**
- Use `BaseManager` pattern for new entities
- Implement `validate()` methods in Managers
- Use `CVersion` for versioning support
- Follow naming conventions (Manager suffix)
- Use thread-safe operations only

## Testing Strategy

### **Test Coverage**
- **Unit tests** - Individual component testing
- **Integration tests** - Full service workflows
- **Perst tests** - Database operations and transactions
- **Authorization tests** - Permission checking

### **Test Execution**
```bash
# Run all tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb

# Run Perst tests only
java -jar work/KissUnitTest.jar --select-package=oodb

# Run specific test class
java -jar work/KissUnitTest.jar --select-class=oodb.PerstConfigTest
```

## Deployment & Operations

### **Build Process**
```bash
# Build project
./bld build

# Development mode (watch changes)
./bld develop

# Clean and rebuild
./bld clean build
```

### **Configuration**
- **application.ini** - Main configuration file
- **Perst settings** - Database path, cache size, versioning
- **Service configuration** - Endpoint definitions
- **Security settings** - Authentication requirements

### **Runtime Environment**
- **Java 17+** - Required runtime
- **Perst JARs** - Object database libraries
- **SLF4J** - Logging framework
- **Thread-safe** - Multi-user concurrent access

## Documentation References

- **Main README**: [README.md](README.md)
- **Perst Integration**: [docs/PerstIntegration.md](docs/PerstIntegration.md)
- **Perst Usage**: [PERST_USAGE.md](PERST_USAGE.md)
- **Getting Started**: [GETTING_STARTED.md](GETTING_STARTED.md)
- **Manager Pattern**: [MANAGER_AT_THE_GATE.md](MANAGER_AT_THE_GATE.md)
- **Testing Guide**: [docs/TestingGuide.md](docs/TestingGuide.md)
- **Perst Developer Guide**: [docs/PerstDeveloperGuide.md](docs/PerstDeveloperGuide.md)
- **Transaction Model**: [docs/PerstTransactionModel.md](docs/PerstTransactionModel.md)

## Conclusion

KissOO represents a sophisticated integration of object-oriented database technology with a mature web framework. The "Manager at the Gate" pattern provides robust security while maintaining developer productivity. The multi-language service support and hot-reload capabilities make it suitable for rapid development environments.

**Key Strengths:**
- **Security-first design** with comprehensive authorization
- **Type-safe permissions** preventing configuration errors
- **Multi-language support** for diverse development teams
- **Hot reload development** for rapid iteration
- **Object-oriented persistence** eliminating ORM complexity

**Considerations:**
- **Learning curve** for Perst OODBMS concepts
- **Custom build system** requiring familiarity with `bld`
- **Framework-specific patterns** requiring adherence to conventions
- **Limited SQL integration** (Perst-only storage)

This project demonstrates a mature approach to combining traditional web framework patterns with modern object-oriented database technology, providing a robust foundation for enterprise application development.