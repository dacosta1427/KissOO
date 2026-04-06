# KissOO Development Protocol

**Version:** 1.0  
**Type:** Generic Protocol for OO Development with Perst + Svelte  
**Purpose:** Constitutional rules, patterns, and memory integration for AI agents working on KissOO projects

---

## SECTION 1: CONSTITUTION (Core Principles)

### 1.1 Memory Is Primary, Code Is Secondary

Before writing a single line, consult memory. After every action, update memory.

```typescript
// BEFORE any action:
1. Read AP.md (this file)
2. Read KISSOO_DEVELOPMENT_PROTOCOL.md (this document)
3. Check .memory/EXPERIENCE_LOG.md (current state)
4. Check .memory/PITFALL_REGISTRY.md (known failures)

// AFTER any meaningful action:
1. Update EXPERIENCE_LOG.md
2. Update PITFALL_REGISTRY.md if failure occurred
3. Update BEST_PRACTICES.md if new pattern discovered
```

### 1.2 Pure OO Navigation (No SQL Patterns)

**THE GOLDEN RULE:** Always navigate through object references, never filter by ID.

```groovy
// ❌ WRONG - SQL-style filtering
Collection<Schedule> all = PerstStorageManager.getAll(Schedule.class)
for (Schedule s : all) {
    if (s.getCleaner().getOid() == cleanerOid) { ... }
}

// ✅ CORRECT - Pure OO navigation
Cleaner cleaner = getCurrentCleaner(servlet)
return cleaner.getSchedules()  // Ask the object directly!
```

This applies to ALL relationships:
- Actor → PerstUser → Actor (bidirectional)
- Owner → houses (via collection)
- House → bookings (via collection)
- Cleaner → schedules (via collection)
- Booking → house (reference)

### 1.3 Manager at the Gate

All access to data storage MUST go through a Manager class.

```java
// ❌ WRONG - Direct Perst access from service
CDatabase db = PerstStorageManager.getDatabase();
db.insert(myObject);

// ✅ CORRECT - Go through Manager
MyEntityManager.create(param1, param2);
```

Why this matters:
- Centralizes validation and business logic
- Enforces authorization checks
- Provides transaction boundaries
- Makes testing easier

### 1.4 Session-Based Authorization

The frontend sends ONLY the session UUID. The backend derives all context from the session.

```typescript
// Frontend - ONLY sends _uuid
Server.call('services.Cleaning', 'getSchedules', {})

// Backend - Gets actor from session
PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
def actor = pu.getActor()  // Actor → Owner or Cleaner
```

```typescript
// Frontend session state (Svelte 5 runes)
let uuid = $state('')
let ownerOid = $state(0)
let cleanerOid = $state(0)
```

### 1.5 Simple, Elegant, Readable Code

- Prefer clarity over cleverness
- Code is read more than written – optimize for readability
- If it's not obvious, add a comment explaining WHY, not WHAT
- Small classes, small methods, single responsibility
- No premature optimization – make it work, make it right, then make it fast

---

## SECTION 2: MEMORY SYSTEM INTEGRATION

### 2.1 Document Structure

Maintain these documents in a `.memory/` folder:

| Document | Purpose | When Read | When Write |
|----------|---------|-----------|------------|
| `AP.md` | Entry point, quick reference | Start of session | Never |
| `KISSOO_DEVELOPMENT_PROTOCOL.md` | Master rules, patterns | Start of session | Protocol changes |
| `EXPERIENCE_LOG.md` | Current state + history | Always | Every iteration |
| `PITFALL_REGISTRY.md` | Failures + remedies | When stuck | After failure |
| `BEST_PRACTICES.md` | Reusable patterns | When designing | New pattern discovered |
| `PROJECT_STATE.md` | Architecture | When modifying | Architecture changes |
| `DECISION_LOG.md` | Rationale | When deciding | Decision made |

### 2.2 The READ-ACT-WRITE Loop

```
┌─────────────────────────────────────────────────────────────┐
│  BEFORE TASK (READ)                                         │
│  1. Read AP.md                                              │
│  2. Read KISSOO_DEVELOPMENT_PROTOCOL.md                    │
│  3. Read .memory/EXPERIENCE_LOG.md                         │
│  4. Check .memory/PITFALL_REGISTRY.md for related failures │
│  5. Check .memory/BEST_PRACTICES.md for applicable patterns│
│  6. Create feature branch (git checkout -b feat/...)        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  EXECUTION (ACT)                                            │
│  1. Small, incremental changes                              │
│  2. Compile/verify after each change                        │
│  3. Commit after each logical unit (git commit)            │
│  4. Push after each commit (git push)                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  AFTER TASK (WRITE)                                         │
│  1. Update EXPERIENCE_LOG.md (iteration entry)             │
│  2. Update PITFALL_REGISTRY.md (if failures occurred)      │
│  3. Update BEST_PRACTICES.md (if new patterns found)       │
│  4. Update PROJECT_STATE.md (if architecture changed)      │
│  5. Tag completion if significant (git tag -a vX -m "...")  │
│  6. Push (git push)                                         │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 Document Templates

#### EXPERIENCE_LOG.md

```markdown
# Experience Log

## Current State
- Active Branch: <branch-name>
- Current Iteration: <N>
- Last Updated: YYYY-MM-DD

## Active Context
- Primary Task: <description>
- Blocking Issues: <list>
- Open Questions: <list>
- Next Actions: [ ] <task>

## Iteration History

### Iteration <N> - YYYY-MM-DD HH:MM
- Task: <brief>
- Decisions: <OO choices made>
- Outcome: success | partial | failure
- Changes: <files modified>
- Commits: <hashes>
- Reflections: <what was learned>
```

#### PITFALL_REGISTRY.md

```markdown
# Pitfall Registry

## Active Pitfalls

### PIT-<XXX>: <Title>
- **Category:** SYNTAX | DESIGN | LOGIC | INTEGRATION | ASSUMPTION
- **First Seen:** YYYY-MM-DD
- **Last Updated:** YYYY-MM-DD
- **Occurrences:** <count>

**Context:** <what was being attempted>

**Symptom:** <observable error>

**Root Cause (5 Whys):**
1. Why? → …
2. Why? → …
3. Why? → …
4. Why? → …
5. Why? → <fundamental cause>

**Resolution:** <how fixed>

**Prevention:** <code/design/process level>

**Related Classes:** <affected classes>
```

---

## SECTION 3: PERST OODBMS INTEGRATION

### 3.1 Domain Class Requirements

All domain entities MUST extend `CVersion` (not `Persistent`) for versioning support.

```java
package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

public class MyEntity extends CVersion {
    
    // Indexed field for fast lookup (unique constraint)
    @Indexable(unique=true)
    private String name;
    
    // Full-text searchable for Lucene queries
    @FullTextSearchable
    private String description;
    
    // Regular field - persisted but not indexed
    private int value;
    
    // Required no-arg constructor for Perst
    public MyEntity() {}
    
    public MyEntity(String name, String description, int value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }
    
    // Getters and setters (or use Lombok @Getter @Setter)
}
```

### 3.2 Key Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Indexable` | B-tree index on field | `@Indexable(unique=true) String name` |
| `@Indexable(caseInsensitive=true)` | Case-insensitive index | |
| `@FullTextSearchable` | Lucene full-text search | `@FullTextSearchable String description` |

### 3.3 StorageManager Usage

**ALWAYS go through PerstStorageManager, never directly to CDatabase:**

```java
// Initialization (in KissInit.groovy)
if (PerstConfig.getInstance().isPerstEnabled()) {
    PerstStorageManager.initialize();
}

// Retrieval - use select(), NOT getRecords()
Collection<MyEntity> items = PerstStorageManager.getAll(MyEntity.class);

// Get by OID
MyEntity entity = PerstStorageManager.getByOid(MyEntity.class, oid);

// Create with TransactionContainer
TransactionContainer tc = PerstStorageManager.createContainer();
tc.addInsert(myEntity);
PerstStorageManager.store(tc);

// Update
tc = PerstStorageManager.createContainer();
tc.addUpdate(myEntity);
PerstStorageManager.store(tc);

// Delete
tc = PerstStorageManager.createContainer();
tc.addDelete(myEntity);
PerstStorageManager.store(tc);
```

### 3.4 Critical: select() vs getRecords()

```groovy
// ✅ CORRECT: select() iterates class extent directly
Collection<Owner> owners = PerstStorageManager.getAll(Owner.class)

// ❌ WRONG: getRecords() uses Lucene full-text search (wrong tool!)
Collection<Owner> owners = PerstStorageManager.getRecords(Owner.class)
```

**Why:** `getRecords()` uses Lucene indexing which is designed for text search, not object retrieval.

### 3.5 Transaction Patterns

```groovy
// Simple operations - Manager handles transactions
def owner = OwnerManager.getByOid(oid)
owner.setName("New Name")
OwnerManager.update(owner)

// Complex operations - manual transaction
PerstStorageManager.beginTransaction()
try {
    // Multiple operations
    def booking = new Booking(...)
    def house = booking.getHouse()
    house.getBookings().add(booking)
    PerstStorageManager.commitTransaction()
} catch (e) {
    PerstStorageManager.rollbackTransaction()
    throw e
}
```

---

## SECTION 4: DOMAIN MODEL ARCHITECTURE

### 4.1 Bidirectional Collections

For complete OO navigation, domain classes MUST have bidirectional collections:

```java
// Owner.java - HAS collection of houses
public class Owner extends Actor {
    @Getter @Setter
    private Set<House> houses = new HashSet<>();
}

// Cleaner.java - HAS collection of schedules
public class Cleaner extends Actor {
    @Getter @Setter
    private Set<Schedule> schedules = new HashSet<>();
}

// House.java - HAS collection of bookings (optional but recommended)
public class House extends Persistent {
    @Getter @Setter
    private Set<Booking> bookings = new HashSet<>();
}
```

### 4.2 OO Convenience Methods

For JSON serialization, provide `getXxxOid()` methods:

```java
// House.java
public long getOwnerOid() {
    return owner != null ? owner.getOid() : 0;
}

// Booking.java  
public long getHouseOid() {
    return house != null ? house.getOid() : 0;
}

// Schedule.java
public long getCleanerOid() {
    return cleaner != null ? cleaner.getOid() : 0;
}
```

**NEVER use `getHouseId()`, `getCleanerId()` - these methods don't exist in OO model.**

### 4.3 Actor/PerstUser Relationship

```java
// Every Actor has an ActorType: NATURAL (default) or CORPORATE
// NATURAL actors: Have a persisted PerstUser
// CORPORATE actors: Container only, no PerstUser

// Actor → PerstUser (persisted)
public class Actor extends CVersion {
    private PerstUser perstUser;  // Set in constructor for NATURAL
    
    public PerstUser getPerstUser() { return perstUser; }
}

// PerstUser → Actor (persistent reference)
public class PerstUser extends CVersion {
    private Actor actor;
    
    public Actor getActor() { return actor; }
}

// Navigation: PerstUser → Actor → Owner/Cleaner
PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
Actor actor = pu.getActor()  // Could be Owner or Cleaner
if (actor instanceof Owner) { ... }
if (actor instanceof Cleaner) { ... }
```

### 4.4 Object Deletion Protocol

All domain classes inherit from Perst's `CVersion → Persistent → PinnedPersistent`.

```java
// Deletion pattern
TransactionContainer tc = PerstStorageManager.createContainer();
tc.addDelete(object);
PerstStorageManager.store(tc);

// Check if deleted (inherited from PinnedPersistent)
if (user.isDeleted()) {
    // User has been deleted
}
```

---

## SECTION 5: SERVICE DEVELOPMENT PROTOCOL

### 5.1 Service Method Signature

```groovy
void methodName(JSONObject injson, JSONObject outjson, 
                Connection db, ProcessServlet servlet) {
    // Implementation
}
```

**Note:** When Perst-only mode is enabled (no SQL), `db` is actually a `PerstConnection` instance.

### 5.2 Authorization Flow

```groovy
void myService(JSONObject injson, JSONObject outjson, 
               Connection db, ProcessServlet servlet) {
    
    // Get actor from session (NOT from request parameters!)
    PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
    if (pu == null) {
        outjson.put("_Success", false)
        outjson.put("_ErrorMessage", "Not authenticated")
        return
    }
    
    Actor actor = pu.getActor()
    
    // Check role if needed
    boolean isAdmin = actor?.getAgreement()?.getRole() in ["admin", "superAdmin"]
    
    // Business logic...
}
```

### 5.3 Response Format

**ALWAYS include these fields:**

```groovy
// Success
outjson.put("_Success", true)
outjson.put("data", result)

// Failure
outjson.put("_Success", false)
outjson.put("_ErrorMessage", "Human readable error")
outjson.put("_ErrorCode", 2)  // 2 = session expired
```

### 5.4 Error Codes

| Code | Meaning |
|------|---------|
| 0 | Unknown error |
| 1 | Validation error |
| 2 | Session expired / not authenticated |
| 3 | Authorization denied |
| 4 | Not found |
| 5 | Conflict (e.g., unique constraint) |

---

## SECTION 6: SVELTE 5 + BACKEND CONTRACT

### 6.1 Server.call() Pattern

```typescript
// src/lib/services/Server.ts

export class Server {
    static async call(cls: string, meth: string, injson: any = {}): Promise<any> {
        // Only _uuid is sent - backend derives everything from session
        const payload = {
            ...injson,
            _uuid: Server.uuid,  // Session UUID from login
            _method: meth,
            _class: cls
        };
        
        const response = await fetch(`${Server.url}/rest`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        const result = await response.json();
        
        if (!result._Success) {
            if (result._ErrorCode === 2) {
                await this.handleSessionError();
            }
            throw new Error(result._ErrorMessage);
        }
        
        return result;
    }
}
```

### 6.2 Session State (Svelte 5 Runes)

```typescript
// src/lib/state/session.svelte.ts

// Reactive state using Svelte 5 runes
let uuid = $state('');
let ownerOid = $state(0);
let cleanerOid = $state(0);

export const session = {
    get uuid(): string { return uuid; },
    setUUID(newUuid: string): void { uuid = newUuid; },
    get ownerOid(): number { return ownerOid; },
    setOwnerOid(id: number): void { ownerOid = id; },
    get cleanerOid(): number { return cleanerOid; },
    setCleanerOid(id: number): void { cleanerOid = id; },
    get isAuthenticated(): boolean { return uuid.length > 0; },
    
    // Persistence to localStorage
    restore(): boolean { /* ... */ },
    clear(): void { /* ... */ }
};
```

### 6.3 Login Flow

```typescript
// src/lib/api/Auth.ts

export async function login(username: string, password: string): Promise<LoginResult> {
    const res = await Server.call('', 'Login', {
        username: username.toLowerCase(),
        password: password
    }) as LoginResult;
    
    if (res._Success && res.uuid) {
        session.setUUID(res.uuid);
        Server.setUUID(res.uuid);
        
        // Store derived OIDs from response
        if (res.ownerOid) session.setOwnerOid(res.ownerOid);
        if (res.cleanerOid) session.setCleanerOid(res.cleanerOid);
    }
    
    return res;
}
```

### 6.4 Silent Re-Authentication

```typescript
// Server.ts - automatic session recovery

private static async handleSessionError(): Promise<void> {
    const credentials = await session.getStoredCredentials();
    if (credentials) {
        const res = await login(credentials.username, credentials.password);
        if (res._Success && res.uuid) {
            session.setUUID(res.uuid);
            Server.uuid = res.uuid;
            return; // Retry the original call
        }
    }
    // Fall back to logout
    session.clear();
    window.location.href = '/';
}
```

### 6.5 What Frontend Sends vs Derives

| Frontend Sends | Backend Derives |
|----------------|-----------------|
| `_uuid` | PerstUser from UserCache |
| (nothing else) | Actor from PerstUser.getActor() |
| | Role from Actor.getAgreement().getRole() |
| | Owner/Cleaner from Actor casting |

### 6.6 Frontend NEVER Sends

- `_ownerId` - derived from session
- `_cleanerId` - derived from session
- `_isAdmin` - derived from session

---

## SECTION 7: EMAIL VERIFICATION PATTERN

### 7.1 Unified Verification Flow

```groovy
// Backend generates token
String token = UUID.randomUUID().toString();
user.setVerificationToken(token);
PerstUserManager.update(user);

// Send email with verification link
String link = "https://app.com/verify?token=" + token;
sendEmail(user.getEmail(), "Verify your account", link);
```

### 7.2 Verification Endpoint

```groovy
void verify(JSONObject injson, JSONObject outjson, 
            Connection db, ProcessServlet servlet) {
    
    String token = injson.getString("token");
    PerstUser user = PerstUserManager.findByToken(token);
    
    if (user == null) {
        outjson.put("_Success", false);
        outjson.put("_ErrorMessage", "Invalid token");
        return;
    }
    
    user.setEmailVerified(true);
    user.setVerificationToken(null);
    PerstUserManager.update(user);
    
    outjson.put("_Success", true);
}
```

### 7.3 Login Requires Verification

```groovy
boolean canLogin() {
    return active && emailVerified;
}
```

New users cannot login until they verify their email.

---

## SECTION 8: COMMON ANTI-PATTERNS & PITFALLS

### 8.1 SQL-Style Filtering (Most Common)

**Symptom:** Services use `getAll().findAll{ filter }` pattern

```groovy
// ❌ WRONG - Iterating all to filter
Collection<Schedule> all = PerstStorageManager.getAll(Schedule.class)
for (Schedule s : all) {
    if (s.getCleaner().getOid() == cleanerOid) { ... }
}

// ✅ CORRECT - Ask the object
Cleaner cleaner = getCurrentCleaner(servlet)
return cleaner.getSchedules()
```

**Prevention:** Add to PITFALL_REGISTRY.md when seen. Update BEST_PRACTICES.md with "Use OO navigation" rule.

### 8.2 Missing Domain Collections

**Symptom:** Can't call `owner.getHouses()` or `cleaner.getSchedules()`

**Root Cause:** Domain classes don't have `Set<...>` collections with getters

**Fix:** Add collections to domain classes:

```java
// Owner.java
@Getter @Setter
private Set<House> houses = new HashSet<>();

// Cleaner.java  
@Getter @Setter
private Set<Schedule> schedules = new HashSet<>();
```

**Prevention:** When adding a new domain class, immediately add collections for all relationships.

### 8.3 Frontend Sends ID Parameters

**Symptom:** Frontend passes `_ownerId`, `_cleanerId`, `_isAdmin` in request

**Fix:** Remove all ID parameters from API calls. Backend derives from session.

### 8.4 Using getRecords() Instead of getAll()

**Symptom:** Code uses `PerstStorageManager.getRecords(Class)` for retrieval

```groovy
// ❌ WRONG - Uses Lucene (for text search, not retrieval)
Collection<Owner> owners = PerstStorageManager.getRecords(Owner.class)

// ✅ CORRECT - Iterates class extent
Collection<Owner> owners = PerstStorageManager.getAll(Owner.class)
```

### 8.5 Wrong Field Naming

**Symptom:** Using `getHouseId()`, `getCleanerId()` methods

**Root Cause:** Confusing OO model with SQL patterns

**Fix:** Use `getHouseOid()`, `getCleanerOid()` - returns internal OID, not SQL ID.

### 8.6 Non-Reactive Arrays in Svelte 5

**Symptom:** Dropdown/select not updating when data changes

**Root Cause:** Using `const` for arrays that need reactivity

```typescript
// ❌ WRONG - const doesn't trigger reactivity
const options = ['a', 'b', 'c'];

// ✅ CORRECT - use $state for reactivity
let options = $state(['a', 'b', 'c']);
```

---

## SECTION 9: QUICK REFERENCE

### Git Workflow

```bash
# Create branch
git checkout -b feat/my-feature

# Commit
git add . && git commit -m "feat: description"

# Push
git push -u origin feat/my-feature

# Tag
git tag -a v1.0-YYYYMMDD -m "message"
git push origin v1.0-YYYYMMDD
```

### Domain Class Checklist

- [ ] Extends `CVersion` (not `Persistent`)
- [ ] Has no-arg constructor
- [ ] Has indexed fields marked with `@Indexable`
- [ ] Has bidirectional collections (if relationships exist)
- [ ] Has `getXxxOid()` convenience methods
- [ ] Uses Lombok `@Getter @Setter` (if applicable)

### Service Method Checklist

- [ ] Gets actor from session, not request params
- [ ] Returns `_Success: true/false`
- [ ] Returns `_ErrorMessage` on failure
- [ ] Uses OO navigation (not iteration + filter)
- [ ] Uses PerstStorageManager (not direct CDatabase)

### Frontend Checklist

- [ ] Sends only `_uuid` in request
- [ ] Uses `$state` for reactive data
- [ ] Uses Svelte 5 syntax (`$props()`, `$derived()`, `onclick`)
- [ ] Uses `tt()` for all user-facing text (i18n)

---

## SECTION 10: APPENDICES

### Appendix A: OO Principles Checklist

- [ ] Single Responsibility
- [ ] Open/Closed
- [ ] Liskov Substitution
- [ ] Interface Segregation
- [ ] Dependency Inversion
- [ ] Encapsulation
- [ ] Composition over Inheritance
- [ ] Manager at the Gate
- [ ] Simple > Complex
- [ ] Elegant > Clever
- [ ] Readability First

### Appendix B: Commit Types

| Type | Purpose |
|------|---------|
| `feat:` | New feature |
| `fix:` | Bug fix |
| `docs:` | Documentation |
| `style:` | Formatting |
| `refactor:` | Code restructuring |
| `perf:` | Performance |
| `test:` | Tests |
| `chore:` | Maintenance |

### Appendix C: Escalation Triggers

Ask human when:
- Decision contradicts BEST_PRACTICES.md
- Pitfall unresolved after 3 attempts
- New pattern affects >3 existing classes
- Public API signature changes
- Technical debt severity is HIGH

---

## FINAL DIRECTIVE

> You are an **OO specialist who remembers everything**.
>
> **Before every action, read memory. After every action, write to memory.**
>
> **Branch, Commit, Push, Tag.** Work is only safe when it's on remote.
>
> **When in doubt, consult your past self through the documents.**
>
> **Pure OO navigation - never use SQL patterns.**
>
> **Manager at the gate - never access storage directly.**
>
> Your intelligence compounds over time. That is your superpower.

---

*Version 1.0 - Generic Protocol for KissOO Development*