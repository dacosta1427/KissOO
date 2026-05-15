# KissOO — Pure OO Architecture Assessment

## Inheritance Hierarchy

```
CVersion (Perst base)
  └── AActor (abstract) — has Agreement, uuid, name, active, createdDate
        ├── ANaturalActor (abstract) — has PerstUser
        │     ├── Owner — has Link<houses>, Link<bookings> (via getHouses→flatMap)
        │     ├── Cleaner — has Link<schedules>
        │     └── Administrator
        └── ACorporateActor — container only, no PerstUser, no collections
```

## What's Pure OO (Good)

1. **No foreign keys** — objects reference each other directly. `Owner.getHouses()` returns actual `House` objects, not IDs.

2. **Encapsulated behavior** — `Owner.addHouse(house)` and `Owner.removeHouse(house)` manage both sides of the relationship. Bidirectional consistency is maintained in the domain layer.

3. **Manager at the Gate** — Manager classes enforce access control and business rules before any persistence operation. Recognized pattern.

4. **Agreement as a value object** — Role, permissions, and groups are encapsulated inside `Agreement`, not scattered across the actor.

5. **Perst Link collections** — `Link<houses>`, `Link<schedules>`, `Link<bookings>` are proper OO collection abstractions that persist transparently.

## OO Purity Issues

### 1. ANaturalActor creates PerstUser in its constructor

```java
public ANaturalActor(String name, Agreement agreement, String email) {
    super(name, agreement);
    createPerstUser(email);  // ← Infrastructure concern inside domain constructor
}
```

A `Cleaner` or `Owner` shouldn't know that a `PerstUser` exists. This couples the domain model to the authentication subsystem. What if you later want an Owner who doesn't need a login? You can't — the constructor forces it.

**Better:** Make PerstUser creation optional or move it to a factory/service.

### 2. All managers are static methods (not objects)

```java
public static Collection<Owner> getAll() { ... }
public static Owner getByOid(long oid) { ... }
```

This is the **DAO pattern**, not the **Repository pattern**. In true OO, the manager would be an instance:
```java
OwnerManager manager = new OwnerManager(udbm);
Collection<Owner> owners = manager.getAll();
```

Static managers:
- Can't be mocked easily for testing
- Can't hold state (like caching)
- Create tight coupling to concrete implementations

### 3. Domain classes know about Perst Link

Currently `Owner.houses` is typed as `Link<House>`. The domain layer imports `org.garret.perst.Link` — a persistence framework class. In a perfectly pure OO design, the domain layer wouldn't know about Perst at all. The collection would be typed as `List<House>` and the persistence adapter would handle the translation.

This is the tension between pragmatism and purity. With an OODBMS like Perst, the line is blurry because the storage engine IS the object graph.

### 4. No polymorphism in retrieval

`OwnerManager.getAll()` returns `Collection<Owner>`. But if you want "all actors who are active", there's no polymorphic query — you have to call `ActorManager.getAll()` and filter, or duplicate the logic in each manager. A proper OO approach would use a Specification pattern or a shared interface method.

### 5. Service layer mixes concerns

The Groovy services handle both HTTP serialization (JSON mapping) AND business logic AND authorization. In strict OO:
- **Controller** — JSON serialization/deserialization
- **Service/UseCase** — business logic and orchestration
- **Presenter** — format the response

## Structural Ratings

| Aspect | Rating | Notes |
|--------|--------|-------|
| Class hierarchy | ★★★★☆ | Clean inheritance, good abstractions |
| Encapsulation | ★★★★☆ | Objects manage their own state well |
| Persistence ignorance | ★★☆☆☆ | Domain classes know about Link, Storage |
| Testability | ★★☆☆☆ | Static managers can't be mocked easily |
| Single Responsibility | ★★★☆☆ | Services handle too many concerns |
| Extensibility | ★★★☆☆ | Adding new actor types requires touching managers |
| Cohesion | ★★★★★ | Each class has a clear, focused purpose |
| Coupling | ★★★☆☆ | Managers coupled to concrete types, not interfaces |

## Overall Opinion

This is a **well-structured system** that follows OO principles better than most projects. The class hierarchy is clean, the bidirectional relationships are properly managed, and the Manager-at-the-Gate pattern provides good access control.

Main areas for improvement:
1. Separate PerstUser creation from domain constructors — use a service/factory
2. Make managers instantiable — enables testing and future state management
3. Consider an abstraction layer between domain and Link — even a simple wrapper interface would decouple domain from Perst specifics

The current refactoring (removing StorageManager, using UnifiedDBManager directly) is the right move — it reduces indirection without changing the fundamental architecture. The `DomainCollections.createLink()` helper is an elegant compromise that keeps domain constructors clean while isolating the Perst dependency.