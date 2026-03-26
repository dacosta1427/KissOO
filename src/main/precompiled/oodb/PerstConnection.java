package oodb;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.IterableIterator;
import org.kissweb.database.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PerstConnection - Extends KISS Connection with Perst OODBMS operations.
 * 
 * Uses an in-memory H2 database as a dummy SQL connection to satisfy the
 * Connection superclass, then adds Perst-specific methods.
 * 
 * Registered as "NonSqlConnection" in MainServlet environment when Perst is enabled.
 * Services can use Perst methods directly on the db parameter.
 * 
 * Usage in services:
 * <pre>
 * void myService(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
 *     // When Perst is enabled, db is a PerstConnection with Perst methods!
 *     if (db instanceof PerstConnection) {
 *         PerstConnection perst = (PerstConnection) db;
 *         Collection&lt;House&gt; houses = perst.getAll(House.class);
 *         House house = perst.getByOid(House.class, oid);
 *     }
 * }
 * </pre>
 */
public class PerstConnection extends Connection {
    
    private UnifiedDBManager udbm;
    
    /**
     * Create PerstConnection with an in-memory H2 database as dummy connection.
     */
    public PerstConnection() throws SQLException {
        super(createDummyConnection());
        this.udbm = PerstStorageManager.getDBManager();
    }
    
    /**
     * Create an in-memory H2 connection to satisfy the Connection superclass.
     */
    private static java.sql.Connection createDummyConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            // H2 not available, try SQLite
            try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite::memory:");
            } catch (ClassNotFoundException e2) {
                throw new SQLException("No dummy database driver available (H2 or SQLite)");
            }
        }
        return DriverManager.getConnection("jdbc:h2:mem:perstdummy;DB_CLOSE_DELAY=-1");
    }
    
    /**
     * Check if Perst is available.
     */
    public boolean isPerstAvailable() {
        return udbm != null && PerstStorageManager.isAvailable();
    }
    
    // ========== TRANSACTION MANAGEMENT ==========
    
    public void perstBeginTransaction() {
        if (udbm != null) udbm.beginTransaction();
    }
    
    public void perstCommitTransaction() throws Exception {
        if (udbm != null) udbm.commitTransaction();
    }
    
    public void perstRollbackTransaction() {
        if (udbm != null) udbm.rollbackTransaction();
    }
    
    public boolean perstIsInTransaction() {
        return udbm != null && udbm.isInTransaction();
    }
    
    // ========== TRANSACTION CONTAINER ==========
    
    public TransactionContainer perstCreateContainer() {
        if (udbm == null) return null;
        return udbm.createContainer();
    }
    
    public boolean perstStore(TransactionContainer tc) {
        if (udbm == null || tc == null) return false;
        try {
            return udbm.store(tc).isSuccess();
        } catch (Exception e) {
            System.err.println("[PerstConnection] Store failed: " + e.getMessage());
            return false;
        }
    }
    
    // ========== RETRIEVE OPERATIONS ==========
    
    public <T extends CVersion> List<T> getAll(Class<T> clazz) {
        if (udbm == null) return new ArrayList<>();
        try {
            IterableIterator<T> results = udbm.getRecords(clazz);
            return toList(results);
        } catch (Exception e) {
            System.err.println("[PerstConnection] getAll failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public <T extends CVersion> T find(Class<T> clazz, String field, String value) {
        if (udbm == null) return null;
        try {
            IterableIterator<T> results = udbm.find(clazz, field, new org.garret.perst.Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstConnection] find(String) failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T find(Class<T> clazz, String field, int value) {
        if (udbm == null) return null;
        try {
            IterableIterator<T> results = udbm.find(clazz, field, new org.garret.perst.Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstConnection] find(int) failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T find(Class<T> clazz, String field, long value) {
        if (udbm == null) return null;
        try {
            IterableIterator<T> results = udbm.find(clazz, field, new org.garret.perst.Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstConnection] find(long) failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T getByOid(Class<T> clazz, long oid) {
        if (udbm == null) return null;
        try {
            org.garret.perst.dbmanager.RetrieveResult<T> result = udbm.getByOid(oid);
            return result != null ? result.getObject() : null;
        } catch (Exception e) {
            System.err.println("[PerstConnection] getByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T getByUuid(Class<T> clazz, String uuid) {
        if (udbm == null) return null;
        try {
            return udbm.getByUuid(uuid);
        } catch (Exception e) {
            System.err.println("[PerstConnection] getByUuid failed: " + e.getMessage());
            return null;
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private <T> List<T> toList(IterableIterator<T> iter) {
        List<T> list = new ArrayList<>();
        if (iter != null) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }
    
    private <T> T getSingleton(IterableIterator<T> iter) {
        if (iter == null || !iter.hasNext()) return null;
        return iter.next();
    }
}
