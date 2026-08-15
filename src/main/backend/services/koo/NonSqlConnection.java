package services.koo;

import koo.core.database.StorageManager;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.IterableIterator;
import org.kissweb.database.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * NonSqlConnection - Extends KISS Connection with Perst OODBMS operations.
 * 
 * Uses an in-memory SQLite database as a dummy SQL connection to satisfy the
 * Connection superclass, then adds Perst-specific methods.
 * 
 * Registered as "NonSqlConnection" in MainServlet environment when Perst is enabled.
 * Services receive it as the db parameter and can use Perst methods directly.
 * 
 * IMPORTANT: commit(), rollback(), and close() are no-ops because:
 * 1. The dummy SQLite connection is only to satisfy the Connection superclass
 * 2. Real Perst operations use PerstStorageManager, not this dummy connection
 * 3. NonSqlConnection is reused across requests, so we don't close the dummy connection
 */
public class NonSqlConnection extends Connection {
    
    private CDatabase cdb;
    
    /**
     * Create NonSqlConnection with an in-memory SQLite database as dummy connection.
     */
    public NonSqlConnection() throws SQLException {
        super(createDummyConnection());
        this.cdb = StorageManager.getDBManager();
    }
    
    /**
     * Create an in-memory SQLite connection to satisfy the Connection superclass.
     * Auto-commit is disabled to match other database connection behavior.
     */
    private static java.sql.Connection createDummyConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not available");
        }
        java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        conn.setAutoCommit(false);
        return conn;
    }
    
    /**
     * Check if Perst is available.
     */
    public boolean isPerstAvailable() {
        return cdb != null && StorageManager.isAvailable();
    }
    
    /**
     * No-op: Perst operations don't use this dummy connection.
     */
    @Override
    public void rollback() throws java.sql.SQLException {
        // Perst doesn't use the dummy connection
    }
    
    /**
     * No-op: Perst operations don't use this dummy connection.
     */
    @Override
    public void commit() throws java.sql.SQLException {
        // Perst doesn't use the dummy connection
    }
    
    /**
     * No-op: NonSqlConnection is reused across requests.
     * The real Perst database operations use PerstStorageManager.
     */
    @Override
    public void close() throws java.sql.SQLException {
        // Don't close - NonSqlConnection is reused
    }
    
    // ========== TRANSACTION MANAGEMENT ==========
    
    public void perstBeginTransaction() {
        if (cdb != null) cdb.beginTransaction();
    }
    
    public void perstCommitTransaction() throws Exception {
        if (cdb != null) cdb.commitTransaction();
    }
    
    public void perstRollbackTransaction() {
        if (cdb != null) cdb.rollbackTransaction();
    }
    
    public boolean perstIsInTransaction() {
        return cdb != null && cdb.isInTransaction();
    }
    
    // ========== TRANSACTION CONTAINER ==========
    
    public TransactionContainer perstCreateContainer() {
        if (cdb == null) return null;
        return cdb.createContainer();
    }
    
    public boolean perstStore(TransactionContainer tc) {
        if (cdb == null || tc == null) return false;
        try {
            return cdb.store(tc).isSuccess();
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] Store failed: " + e.getMessage());
            return false;
        }
    }
    
    // ========== RETRIEVE OPERATIONS ==========
    
    public <T extends CVersion> List<T> getAll(Class<T> clazz) {
        if (cdb == null) return new ArrayList<>();
        try {
            IterableIterator<T> results = cdb.getRecords(clazz);
            return toList(results);
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] getAll failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public <T extends CVersion> T find(Class<T> clazz, String field, String value) {
        if (cdb == null) return null;
        try {
            IterableIterator<T> results = cdb.find(clazz, field, new org.garret.perst.Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] find(String) failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T find(Class<T> clazz, String field, int value) {
        if (cdb == null) return null;
        try {
            IterableIterator<T> results = cdb.find(clazz, field, new org.garret.perst.Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] find(int) failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T find(Class<T> clazz, String field, long value) {
        if (cdb == null) return null;
        try {
            IterableIterator<T> results = cdb.find(clazz, field, new org.garret.perst.Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] find(long) failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T getByOid(Class<T> clazz, long oid) {
        if (cdb == null) return null;
        try {
            return cdb.getByOid(oid);
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] getByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T getByUuid(Class<T> clazz, String uuid) {
        if (cdb == null) return null;
        try {
            return cdb.getByUuid(uuid);
        } catch (Exception e) {
            System.err.println("[NonSqlConnection] getByUuid failed: " + e.getMessage());
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
