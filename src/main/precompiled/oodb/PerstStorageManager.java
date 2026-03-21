package oodb;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Key;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.dbmanager.UnifiedDBManagerImpl;
import org.garret.perst.dbmanager.TransactionContainer;
import org.garret.perst.dbmanager.StoreResult;
import org.garret.perst.IterableIterator;

import java.util.List;

/**
 * PerstStorageManager - Manages Perst database lifecycle.
 * 
 * Provides convenience methods for Managers:
 * - Retrieve by indexed field
 * - Get all records
 * - Store via TransactionContainer (atomic, optimistic locking)
 * 
 * Delegates to UnifiedDBManager for all operations.
 */
public class PerstStorageManager {
    
    private static final String DBMANAGER_KEY = "perstDBManager";
    private static final String DATABASE_KEY = "perstDatabase";
    private static boolean initialized = false;
    private static Storage storage;
    
    private PerstStorageManager() {}
    
    /**
     * Initialize Perst with UnifiedDBManager.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        if (!PerstConfig.getInstance().isPerstEnabled()) {
            return;
        }
        
        if (!PerstConfig.getInstance().isUseCDatabase()) {
            initialized = true;
            return;
        }
        
        try {
            String dbPath = PerstConfig.getInstance().getDatabasePath();
            String indexPath = dbPath + ".idx";
            
            storage = createStorage();
            
            // Use UnifiedDBManager
            UnifiedDBManager dbm = new UnifiedDBManagerImpl();
            dbm.open(storage, indexPath);
            
            org.kissweb.restServer.MainServlet.putEnvironment(DBMANAGER_KEY, dbm);
            
            initialized = true;
            
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Storage createStorage() throws Exception {
        Storage storage = org.garret.perst.StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.serialize.transient.objects", java.lang.Boolean.FALSE);
        storage.setProperty("perst.file.noflush", Boolean.TRUE);
        
        String dbPath = PerstConfig.getInstance().getDatabasePath();
        int poolSize = PerstConfig.getInstance().getPagePoolSize();
        
        java.io.File dbFile = new java.io.File(dbPath);
        java.io.File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        storage.open(dbPath, poolSize);
        return storage;
    }
    
    /**
     * Get UnifiedDBManager instance.
     */
    public static UnifiedDBManager getDBManager() {
        if (!initialized) {
            initialize();
        }
        return (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment(DBMANAGER_KEY);
    }
    
    /**
     * Get the raw CDatabase instance (for advanced use only).
     */
    public static CDatabase getDatabase() {
        return null; // Now handled by UnifiedDBManager
    }
    
    /**
     * Check if Perst is available.
     */
    public static boolean isAvailable() {
        return getDBManager() != null;
    }
    
    // ========== RETRIEVE CONVENIENCE METHODS ==========
    
    /**
     * Find record by indexed field value.
     * Example: find(Actor.class, "name", "John")
     */
    public static <T extends CVersion> T find(Class<T> clazz, String field, String value) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            IterableIterator<T> results = dbm.find(clazz, field, new Key(value));
            return dbm.getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find record by integer field value.
     * Example: find(PerstUser.class, "userId", 123)
     */
    public static <T extends CVersion> T find(Class<T> clazz, String field, int value) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            IterableIterator<T> results = dbm.find(clazz, field, new Key(value));
            return dbm.getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all records of a type.
     * Example: getAll(Actor.class)
     */
    public static <T extends CVersion> List<T> getAll(Class<T> clazz) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return java.util.Collections.emptyList();
        
        try {
            IterableIterator<T> results = dbm.getRecords(clazz);
            return dbm.toList(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetAll failed: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * Get record by OID (internal Perst ID).
     */
    public static <T extends CVersion> T getByOid(Class<T> clazz, long oid) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            org.garret.perst.dbmanager.RetrieveResult<T> result = dbm.getByOid(oid);
            return result != null ? result.getObject() : null;
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    // ========== STORE CONVENIENCE METHODS ==========
    
    /**
     * Create a new TransactionContainer for atomic operations.
     */
    public static TransactionContainer createContainer() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        return dbm.createContainer();
    }
    
    /**
     * Create a sync container (forces disk flush for critical operations).
     */
    public static TransactionContainer createSyncContainer() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        return dbm.createSyncContainer();
    }
    
    /**
     * Store objects in container atomically.
     * Handles optimistic locking internally.
     * 
     * @return true if successful, false if conflict/error
     */
    public static boolean store(TransactionContainer container) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null || container == null) return false;
        
        try {
            StoreResult result = dbm.store(container);
            return result.isSuccess();
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Store failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Simple insert - single object atomic.
     * Convenience for simple use cases.
     */
    public static <T extends CVersion> boolean insert(T object) {
        if (object == null) return false;
        
        TransactionContainer tc = createContainer();
        tc.addInsert(object);
        return store(tc);
    }
    
    /**
     * Simple update - single object atomic.
     * Uses optimistic locking (checks version before commit).
     */
    public static <T extends CVersion> boolean update(T object) {
        if (object == null) return false;
        
        TransactionContainer tc = createContainer();
        tc.addUpdate(object);
        return store(tc);
    }
    
    /**
     * Simple delete - single object atomic.
     */
    public static <T extends CVersion> boolean delete(T object) {
        if (object == null) return false;
        
        TransactionContainer tc = createContainer();
        tc.addDelete(object);
        return store(tc);
    }
    
    // ========== HISTORY BUFFER ==========
    
    /**
     * Flush history buffer to disk.
     */
    public static void flushHistory() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.flushHistoryBuffer();
        }
    }
    
    /**
     * Get current history buffer size.
     */
    public static int getHistoryBufferSize() {
        UnifiedDBManager dbm = getDBManager();
        return dbm != null ? dbm.getHistoryBufferSize() : 0;
    }
    
    /**
     * Close Perst.
     */
    public static synchronized void close() {
        UnifiedDBManager dbm = (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment(DBMANAGER_KEY);
        if (dbm != null) {
            try {
                dbm.close();
            } catch (Exception e) {
                System.err.println("[PerstStorageManager] Close failed: " + e.getMessage());
            }
            org.kissweb.restServer.MainServlet.putEnvironment(DBMANAGER_KEY, null);
        }
        if (storage != null) {
            storage.close();
            storage = null;
        }
        initialized = false;
    }
}