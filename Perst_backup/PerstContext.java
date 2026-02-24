package nl.dcg.gfe;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.IOError;
import org.garret.perst.userdef.StorageFactoryDCG;
import org.garret.perst.IPersistent;
import org.garret.perst.IterableIterator;
import org.garret.perst.Key;
import org.garret.perst.SortedCollection;
import org.garret.perst.PersistentComparator;

import java.util.Collection;
import java.util.HashSet;

/**
 * PerstContext - Provides Perst database operations.
 * This is the main interface for Perst operations in GFE.
 */
public class PerstContext {
    private static PerstContext instance;
    
    private Storage storage;
    private CDatabase database;
    private boolean initialized = false;
    
    private PerstContext() {
        // Private constructor for singleton
    }
    
    public static synchronized PerstContext getInstance() {
        if (instance == null) {
            instance = new PerstContext();
        }
        return instance;
    }
    
    /**
     * Initialize Perst database. Call this once at startup.
     */
    public synchronized void initialize() {
        if (initialized || !PerstConfig.getInstance().isPerstEnabled()) {
            return;
        }
        
        try {
            System.out.println("[PerstContext] Initializing Perst database...");
            
            storage = StorageFactoryDCG.getInstance().createStorageDCG();
            storage.setProperty("perst.serialize.transient.objects", java.lang.Boolean.FALSE);
            storage.setProperty("perst.file.noflush", Boolean.TRUE);
            
            String dbPath = PerstConfig.getInstance().getDatabasePath();
            int poolSize = PerstConfig.getInstance().getPagePoolSize();
            
            storage.open(dbPath, poolSize);
            
            database = CDatabase.instance;
            database.open(storage, dbPath + "/idx");
            
            initialized = true;
            System.out.println("[PerstContext] Perst database initialized successfully.");
            
        } catch (IOError e) {
            System.err.println("[PerstContext] Failed to initialize Perst: " + e.getMessage());
            initialized = false;
        } catch (Exception e) {
            System.err.println("[PerstContext] Unexpected error: " + e.getMessage());
            initialized = false;
        }
    }
    
    /**
     * Close the database. Call on shutdown.
     */
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        if (storage != null) {
            storage.close();
        }
        initialized = false;
        System.out.println("[PerstContext] Perst database closed.");
    }
    
    /**
     * Check if Perst is enabled and initialized
     */
    public boolean isAvailable() {
        return PerstConfig.getInstance().isPerstEnabled() && initialized;
    }
    
    // ==================== CRUD Operations ====================
    
    /**
     * Retrieve a single object by class and key field
     */
    public <T extends CVersion> T retrieveObject(Class<T> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        return database.getSingleton(database.find(clazz, field, new Key(value)));
    }
    
    /**
     * Retrieve a single object by UUID
     */
    public <T extends CVersion> T retrieveObject(Class<T> clazz, String uuid) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        return database.getSingleton(database.find(clazz, "_UUID", new Key(uuid)));
    }
    
    /**
     * Retrieve all objects of a given type
     */
    public <T extends CVersion> Collection<T> retrieveAllObjects(Class<T> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        IterableIterator<T> iter = database.getRecords(clazz);
        Collection<T> result = new HashSet<>();
        for (T obj : iter) {
            result.add(obj);
        }
        return result;
    }
    
    /**
     * Store a new object
     */
    public void storeNewObject(CVersion obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        database.insert(obj);
    }
    
    /**
     * Store a modified object
     */
    public void storeModifiedObject(CVersion obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        database.update(obj);
    }
    
    /**
     * Remove an object
     */
    public void removeObject(CVersion obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        database.delete(obj);
    }
    
    // ==================== Transaction Operations ====================
    
    /**
     * Start a transaction
     */
    public void startTransaction() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        database.beginTransaction();
        storage.beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
    }
    
    /**
     * End/Commit a transaction
     */
    public void endTransaction() throws Exception {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        database.commitTransaction();
        storage.endThreadTransaction();
    }
    
    /**
     * Rollback a transaction
     */
    public void rollbackTransaction() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        database.rollbackTransaction();
    }
    
    // ==================== Collection Operations ====================
    
    /**
     * Create a sorted collection
     */
    public <T extends IPersistent> SortedCollection<T> createSortedCollection(
            PersistentComparator<?> comparator, boolean unique) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        return (SortedCollection<T>) storage.createSortedCollection(comparator, unique);
    }
}
