# Change Request: No-SQL (`NonSqlConnection`) database support

**Target:** `blakemcbride/Kiss` — framework core (`ProcessServlet.java`, `MainServlet.java`)
**Submitted by:** KissOO fork
**Status:** Proposed

## Summary

Add a generic mechanism that lets a Kiss application run **without a SQL database** by
using a non-SQL (OODBMS) connection object registered in the `MainServlet` environment.
The framework then passes that connection to services as the `db` parameter and to a
dedicated `KissInit.init2ForNonSQL` hook.

This is what enables KissOO to use the **Perst** OODBMS (a `Connection` subclass) instead
of SQL, but the change itself is database-agnostic: any object extending
`org.kissweb.database.Connection` works.

## Motivation

Some deployments persist via an object-oriented OODBMS rather than a relational database.
The framework currently assumes a SQL `Connection` obtained from a connection pool. This
change lets an application register its own `Connection` implementation and have it used
transparently, with zero SQL configuration (`DatabaseType` / `DatabaseName` left empty,
so `MainServlet.hasDatabase()` returns `false`).

## Core changes

### 1. `org/kissweb/restServer/ProcessServlet.java` — `newDatabaseConnection()`

When a `NonSqlConnection` is registered in the environment, use it as `DB` instead of
opening a SQL pool connection. Placed **before** the `hasDatabase()` early-return so it
takes effect even when no SQL database is configured.

```java
    private void newDatabaseConnection() throws SQLException {
        // NonSqlConnection support (e.g., Perst OODBMS). When a non-SQL connection is
        // registered via MainServlet.putEnvironment("NonSqlConnection", conn), it is used
        // in preference to (or instead of) a SQL database.
        Object nonSqlConn = MainServlet.getEnvironment("NonSqlConnection");
        if (nonSqlConn instanceof Connection) {
            DB = (Connection) nonSqlConn;
            logger.info("Using NonSqlConnection (e.g. Perst) for database operations");
            return;
        }
        if (!MainServlet.hasDatabase())
            return;
        logger.info("Pool status - busy: " + MainServlet.getCpds().getNumBusyConnections() +
                   ", idle: " + MainServlet.getCpds().getNumIdleConnections());
        final java.sql.Connection conn = MainServlet.getCpds().getConnection();
        conn.setAutoCommit(false);  //  all SQL operations require a commit but Kiss does a commit at the end of each service
        DB = new Connection(conn);
        String databaseSchema = (String) MainServlet.getEnvironment("DatabaseSchema");
        if (databaseSchema != null  &&  !databaseSchema.isEmpty())
            DB.setSchema(databaseSchema);
        // ... (remainder unchanged)
    }
```

### 2. `org/kissweb/restServer/MainServlet.java` — `init2()` dispatch

After `KissInit.init()` runs, dispatch based on `hasDatabase()`:

- **SQL configured** → call `KissInit.init2(Connection db)` (existing behavior, unchanged).
- **No SQL** → call `KissInit.init2ForNonSQL(<NonSqlConnection>)`, passing the registered
  connection. Null-guarded so a no-SQL app that hasn't registered anything is a no-op.

```java
        if (MainServlet.hasDatabase()) {
            // Normal Kiss path: a SQL database is configured.
            Connection db = MainServlet.openNewConnection();
            (new GroovyService()).internalGroovy(null, "KissInit", "init2", db);
            MainServlet.closeConnection(db);
        } else {
            // No-SQL path (e.g., Perst OODBMS): dispatch to a dedicated hook so the
            // OODBMS initialization stays in the application's KissInit (fork).
            Object nonSqlConn = MainServlet.getEnvironment("NonSqlConnection");
            if (nonSqlConn != null)
                (new GroovyService()).internalGroovy(null, "KissInit", "init2ForNonSQL", nonSqlConn);
        }
```

## Contract for application authors

1. In `KissInit.init()`, register the connection:
   ```groovy
   MainServlet.putEnvironment("NonSqlConnection", nonSqlConn)   // nonSqlConn extends org.kissweb.database.Connection
   ```
2. Implement **two** init hooks in `KissInit.groovy`:
   - `static void init2(Connection db)` — the normal SQL init hook (unused in no-SQL apps).
   - `static void init2ForNonSQL(NonSqlConnection db)` — the no-SQL init hook.

   **Important:** `init2ForNonSQL` must declare the **concrete** connection class (e.g.
   `NonSqlConnection`), *not* the `Connection` base. This is required because
   `GroovyService.internalGroovy` resolves the Groovy method by the **runtime class** of
   the passed argument (`args[i].getClass()`, GroovyService.java:101) and
   `Class.getMethod` requires an **exact** parameter-type match (GroovyService.java:104).
   Declaring `init2ForNonSQL(Connection db)` while passing a `NonSqlConnection` instance
   would throw `NoSuchMethodException`.

3. Applications that configure a SQL database are completely unaffected — `hasDatabase()`
   is `true`, so the existing `init2(Connection db)` path runs as before.

## Verification (KissOO / Perst)

With a Perst-backed `NonSqlConnection` registered, server startup logs:

```
[KissInit] init() - NonSqlConnection registered
[KissInit] init2ForNonSQL() CALLED
[KissInit] db = services.koo.NonSqlConnection@cce77c8
* * * APPLICATION STARTED * * *
```

## Notes

- The build harness (`BuildUtils.java`, `KissBuildUtils.java`, `bld.cmd`) was also
  reconciled with upstream during this work and now matches `blakemcbride/Kiss` exactly
  (the upstream `bld.cmd` compiles `KissBuildUtils.java`, and `Tasks.java` imports it via
  `import static org.kissweb.KissBuildUtils.*`). **No change request is needed there.**
- `CORS` / `web-*.xml` and Lisp (`abcl.jar`) differences are application/deployment
  specifics and are **not** part of this request.
