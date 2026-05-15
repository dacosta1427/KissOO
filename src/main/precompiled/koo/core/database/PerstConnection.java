package koo.core.database;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.IterableIterator;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.kissweb.database.Connection;
import org.kissweb.restServer.MainServlet;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PerstConnection - Extends KISS Connection with Perst OODBMS operations.
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
 * 3. PerstConnection is reused across requests, so we don't close the dummy connection
 */
public class PerstConnection extends Connection {
    
    private UnifiedDBManager udbm;
    
    public PerstConnection() throws SQLException {
        super(createDummyConnection());
        this.udbm = getUnifiedDBManager();
    }
    
    private static UnifiedDBManager getUnifiedDBManager() {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }
    
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
    
    public boolean isPerstAvailable() {
        return udbm != null && StorageManager.isAvailable();
    }
    
    @Override
    public void rollback() throws java.sql.SQLException {
    }
    
    @Override
    public void commit() throws java.sql.SQLException {
    }
    
    @Override
    public void close() throws java.sql.SQLException {
    }
    
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
    
    public <T extends CVersion> List<T> getAll(Class<T> clazz) {
        if (udbm == null) return new ArrayList<>();
        try {
            IterableIterator<T> results = udbm.getObjects(clazz);
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
            org.garret.perst.dbmanager.RetrieveResult<T> result = udbm.getByOid(oid, clazz);
            return result != null ? result.getObject() : null;
        } catch (Exception e) {
            System.err.println("[PerstConnection] getByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    public <T extends CVersion> T getByUuid(Class<T> clazz, String uuid) {
        if (udbm == null) return null;
        try {
            IterableIterator<T> results = udbm.find(clazz, "uuid", new org.garret.perst.Key(uuid));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstConnection] getByUuid failed: " + e.getMessage());
            return null;
        }
    }
    
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