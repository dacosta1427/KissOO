# KissOO Framework - Consolidated Documentation

## Executive Summary

KissOO is a **Java-based full-stack web application framework** that successfully integrates **Perst OODBMS (Object-Oriented Database)** with the KISS framework. It provides embedded persistence without requiring external database servers, making it ideal for rapid business application development.

**Key Value Proposition:**
- **Dual persistence**: SQL databases (PostgreSQL, MySQL, SQLite, etc.) + embedded OODB (Perst)
- **Hot reload development**: No recompilation needed for backend services
- **Multi-language support**: Java, Groovy, and Lisp services
- **Built-in security**: Mandatory authorization framework with type-safe permissions

---

## Core Architecture

### High-Level Stack

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (HTML/JS)                       │
│            Custom components + jQuery + AG-Grid             │
├─────────────────────────────────────────────────────────────┤
│                  REST Services (JSON-RPC)                   │
│                   (Groovy / Java / Lisp)                    │
├─────────────────────────────────────────────────────────────┤
│                    Authorization Layer                      │
│        (EndpointMethod → Agreement → Actor → Manager)      │
├───────────────────────┬─────────────────────────────────────┤
│   SQL Database        │         Perst OODB                  │
│   (Connection Pool)  │     (Thread-local Storage)           │
└───────────────────────┴─────────────────────────────────────┘
```

### Project Structure

```
src/main/
├── core/                              # Framework utilities (READ-ONLY)
│   └── org/kissweb/
│       ├── json/                      # JSON utilities
│       ├── database/                  # SQL connection/ORM (Connection, Record, Command, Cursor)
│       └── restServer/                 # HTTP server (MainServlet, ProcessServlet)
│
├── backend/                           # Application services (MODIFIABLE)
│   ├── services/                      # REST endpoints
│   ├── scripts/                       # Server scripts
│   ├── CronTasks/                     # Scheduled tasks
│   ├── DB.sqlite                      # SQLite database
│   ├── application.ini                # Configuration
│   ├── KissInit.groovy                # Initialization
│   └── Login.groovy                   # Login service
│
├── frontend/                          # Frontend application
│   ├── kiss/                          # Framework components (READ-ONLY)
│   ├── lib/                           # Client-side libraries
│   ├── screens/                       # Application screens
│   ├── mobile/                        # Mobile-specific pages
│   └── index.html                     # Main entry point
│
└── precompiled/                       # Precompiled Java (MODIFIABLE)
    ├── mycompany/
    │   ├── domain/                     # Domain entities
    │   │   ├── Actor.java              # User actor with Agreement
    │   │   ├── Agreement.java          # Permission container
    │   │   ├── Group.java              # Group-based permissions
    │   │   ├── PerstUser.java          # Perst-persisted user
    │   │   ├── CRUD.java               # CRUD constants
    │   │   └── EndpointMethod.java     # Type-safe endpoints
    │   │
    │   └── database/                   # Data managers
    │       ├── PerstHelper.java        # Perst CRUD operations
    │       ├── ActorManager.java       # Actor CRUD with auth
    │       ├── BaseManager.java        # Base manager class
    │       └── PerstUserManager.java   # User management
    │
    └── oodb/                           # Perst configuration
        ├── PerstConfig.java            # Configuration reader
        └── PerstContext.java           # Thread-local storage
```

---

## Key Technologies

### Perst OODBMS Integration

**Purpose:** Provides embedded object-oriented database for fast, schema-less persistence

**Key Features:**
- **Thread-local storage** via `PerstContext`
- **Version history** with `CVersion` (extends Perst's CVersion)
- **Transaction support** via `CDatabase`
- **CRUD operations** via `PerstHelper` utility class

**Configuration (`application.ini`):**
```ini
PerstEnabled = true
PerstDatabasePath = oodb
PerstPagePoolSize = 536870912
PerstUseCDatabase = true
```

### Authorization Pattern: "Manager at the Gate"

This is a **critical security architecture** that must be followed:

#### Three-Layer Authorization

```
Request → EndpointMethod.execute()
              ↓
         Agreement.canExecute()
              ↓
         Actor.canExecute()
              ↓
         Manager operations
```

#### Permission Types

1. **CRUD Permissions:** `"Actor:create"`, `"Actor:read"`, etc.
2. **EndpointMethod Permissions:** Type-safe via `agreement.grant(ActorService.GET_ACTOR)`
3. **Group Permissions:** UNIX-like group-based access

#### Key Classes

| Class | Purpose |
|-------|---------|
| `EndpointMethod` | Type-safe endpoint identifier |
| `Agreement` | Permission container (MUST have one per Actor) |
| `Group` | Group-based permissions |
| `Actor` | Represents user, HAS an Agreement |
| `Manager` | CRUD operations with authorization checks |

---

## Development Workflow

### Build System

- **Primary**: Custom `bld` script (bash-based) for framework-specific tasks
- **Secondary**: Maven for dependency management and standard Java tasks
- **Integration**: Maven-antrun-plugin to bridge both systems

### Development Commands

```bash
# Linux/macOS
./bld develop          # Start development environment
./bld -v build        # Build application (compiles Java)
./bld war             # Create WAR file
./bld -v test         # Run unit tests
./bld clean           # Clean build artifacts
./bld javadoc         # Generate JavaDoc

# Windows
bld develop
bld -v build
# etc.

# Run specific tests
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

### Hot Reload Development

- **No recompilation needed** for backend services
- **Changes auto-detected and loaded**
- **Excellent for rapid iteration**
- **Frontend component scanning** with automatic loading

---

## Key Features

### 1. Dynamic Service Development
- Services auto-compile and reload without restart
- Support for Java, Groovy, and Lisp services
- REST API endpoints created automatically

### 2. Component-Based Frontend
- Custom HTML tags for UI components
- Automatic component loading and rendering
- Modular JavaScript architecture
- Responsive design support

### 3. Database Support
- Multiple database backends (SQLite, PostgreSQL, MySQL, SQL Server, Oracle)
- Connection pooling with C3P0
- SQL query building utilities
- ORM-like functionality through Perst

### 4. Security Features
- **Perst-based authentication** stored in Perst database
- **Session management** with automatic handling
- **Role-based access** with support for user roles and permissions
- **SQL injection protection** via prepared statements
- **Content Security Policy** compatible

---

## Technology Stack

### Backend
- **Java 16+** (specified in pom.xml)
- **Groovy scripting support**
- **Multiple database drivers** (SQLite, PostgreSQL, MySQL, SQL Server, Oracle)
- **Perst OODBMS** for embedded persistence
- **C3P0 connection pooling**
- **Log4j2 for logging**
- **JUnit 5 for testing**

### Frontend
- **Vanilla JavaScript ES6+**
- **Custom DOM manipulation utilities**
- **AG-Grid for data grids**
- **CKEditor for rich text**
- **CSS Grid/Flexbox for layouts**

---

## Strengths

### 1. Developer Productivity
- **Hot Reload Excellence**: Auto-compilation and reload system for backend services
- **Minimal Configuration**: Framework requires very little setup compared to modern alternatives
- **Rapid Prototyping**: Custom component system allows for quick UI development
- **Multiple Language Support**: Java, Groovy, and Lisp services provide flexibility

### 2. Performance Advantages
- **Embedded Persistence**: Perst OODBMS eliminates network overhead for database operations
- **Efficient Build System**: Custom `bld` script optimized for framework's specific needs
- **Minimal Frontend Overhead**: No heavy frameworks means faster load times
- **Connection Pooling**: C3P0 integration provides efficient database connection management

### 3. Architecture Quality
- **Clear Separation**: Well-defined boundaries between core, domain, and service layers
- **Modular Design**: Components can be developed and tested independently
- **Extensibility**: Framework is designed to be extended rather than replaced

### 4. Security Features
- **Type-safe authorization**: EndpointMethod pattern prevents string typos in permissions
- **Default-deny security model**: Everything denied unless explicitly granted
- **Built-in authentication**: Perst-based user management
- **SQL injection protection**: Framework provides parameterized queries

### 5. Flexibility
- **Dual database support**: SQL for structured relational data + Perst for flexible object storage
- **Multi-language services**: Support for Java, Groovy, and Lisp
- **Multiple database backends**: SQLite, PostgreSQL, MySQL, SQL Server, Oracle
- **Full-stack capabilities**: Web applications, CLI tools, and Electron desktop apps

---

## Potential Considerations

### 1. Learning Curve
- **Custom framework requires learning specific patterns**
- **Non-standard patterns** (Record API, JSON-RPC)
- **Custom HTML components** require learning curve
- **Large KnowledgeBase** to learn

### 2. Community and Support
- **Smaller community** compared to mainstream frameworks
- **Limited documentation** for some features
- **Uncertain long-term support**
- **Harder to find developers** familiar with framework

### 3. Scalability and Performance
- **Perst concurrency** may not handle high concurrent user loads well
- **Single-threaded frontend** with no built-in support for Web Workers
- **No caching layer** - framework doesn't provide application-level caching
- **Database limitations** - SQLite not suitable for high-write scenarios

### 4. Technical Issues
- **LSP/IDE errors** due to system-scoped dependencies
- **Multiple build systems** causing confusion (bld, Gradle, Maven)
- **Manual schema management** requiring updates to PerstDBRoot.java
- **Static indexes** in domain classes could cause memory leaks

---

## Development Commands

### Building
```bash
./bld develop   # Linux/macOS
bld develop     # Windows
```

### Testing
```bash
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

### Running
- Open http://localhost:8000 in browser

---

## Configuration

### application.ini

```ini
[main]
DatabaseType = SQLite
DatabaseName = ./DB.sqlite
MaxWorkerThreads = 30
UserInactiveSeconds = 900

# Perst OODBMS
PerstEnabled = true
PerstDatabasePath = oodb
PerstPagePoolSize = 536870912
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `AI/KnowledgeBase.md` | Complete framework reference (843 lines) |
| `MANAGER_AT_THE_GATE.md` | Authorization pattern guide |
| `PERST_USAGE.md` | Perst OODB usage guide |
| `docs/PerstIntegration.md` | Perst setup guide |
| `docs/TestingGuide.md` | Testing instructions |
| `build.gradle` | Gradle build (not recommended) |
| `pom.xml` | Maven build |
| `bld` / `bld.cmd` | Native build scripts (recommended) |

---

## Testing

### Current Test Coverage
- Unit tests in `src/test/core/`
- Perst tests: `PerstConfigTest`, `PerstContextTest`, `PerstContextIntegrationTest`
- Other tests: DateUtils, StringUtils, NumberUtils, JSON, XML, etc.

### Running Tests
```bash
java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb
```

---

## Security Considerations

### Authentication System
- **Perst-based**: User authentication stored in Perst database
- **Session management**: Automatic session handling
- **Role-based access**: Support for user roles and permissions

### Input Validation
- **Frontend validation**: JavaScript validation for user input
- **Backend validation**: Server-side validation required
- **SQL injection protection**: Framework provides parameterized queries

### Security Best Practices
- **No eval()**: Framework avoids dangerous JavaScript eval()
- **CSP support**: Content Security Policy compatible
- **HTTPS ready**: Framework supports secure connections

---

## Performance Characteristics

### Database Performance
- **Perst**: Fast embedded database, no network overhead
- **Connection pooling**: C3P0 provides efficient connection reuse
- **Query optimization**: Framework provides query building utilities

### Frontend Performance
- **Minimal dependencies**: Small JavaScript footprint
- **Lazy loading**: Components load on demand
- **Caching**: Built-in caching for static resources

### Scalability
- **Stateless services**: REST services are stateless
- **Database scaling**: Multiple database backends supported
- **Load balancing**: Framework is load balancer friendly

---

## Recommendations

### For New Projects
- **Consider KissOO** for business applications requiring embedded persistence
- **Evaluate learning curve** and team's ability to learn framework
- **Assess long-term maintenance** requirements

### For Existing Projects
- **Evaluate migration** based on current pain points
- **Consider hybrid approach** integrating modern tools alongside existing framework
- **Focus on performance optimization** for bottlenecks

### For Framework Maintainers
- **Modernization**: Gradually introduce modern development practices
- **Documentation**: Significantly improve documentation quality
- **Community**: Build stronger community engagement
- **Enterprise features**: Add features needed for larger deployments

---

## Future Considerations

### Modernization Roadmap
1. **TypeScript support** for frontend
2. **ESLint/Prettier** integration
3. **Modern build tools** (Vite or Webpack)
4. **API documentation** (OpenAPI/Swagger)

### Performance Improvements
1. **Read replicas** for database scaling
2. **Connection optimization** strategies
3. **Query caching** at application level
4. **Database sharding** for horizontal scaling

### Security Enhancements
1. **Multi-factor authentication**
2. **More granular permission system**
3. **Comprehensive audit logging**
4. **Advanced session management**

---

## Conclusion

KissOO is a **well-architected framework fork** that successfully integrates Perst OODB with the KISS framework. The "Manager at the Gate" authorization pattern provides solid security, and the hot-reload development is excellent for productivity.

**Main Strengths:**
1. **Developer productivity** with hot reload and minimal configuration
2. **Embedded persistence** eliminating external database dependencies
3. **Clean architecture** with clear separation of concerns
4. **Built-in security** with type-safe authorization

**Main Concerns:**
1. **Learning curve** due to custom patterns
2. **Multiple build systems** causing confusion
3. **Perst-specific documentation gaps**
4. **Missing application-specific details**

**Recommendation:** This is a solid foundation for business applications requiring embedded persistence. Focus on better Perst documentation, filling in ApplicationDetails.md, adding integration tests, and standardizing the build process.

---

*Last Updated: 2026-03-15*