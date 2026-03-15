package oodb;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CDatabase;
import org.garret.perst.FieldIndex;

import mycompany.domain.CDatabaseRoot;

/**
 * PerstStorageManager - Manages Perst Storage lifecycle using MainServlet Environment.
 * 
 * This class follows the KISS framework creator's suggestion to use
 * MainServlet.putEnvironment/getEnvironment for storing the Perst Storage handle.
 * 
 * Benefits:
 * - No modifications to KISS core code
 * - Perst is completely independent from KISS database layer
 * - Easy to update KISS framework without merge conflicts
 * 
 * Usage:
 * 1. Call initialize() from KissInit.init2() to start Perst
 * 2. Call getStorage() anywhere to get the Storage instance
 * 3. Call getRoot() to get the CDatabaseRoot for indexing
 */
public class PerstStorageManager {
    
    private static final String STORAGE_KEY = "perstStorage";
    private static final String ROOT_KEY = "perstRoot";
    private static final String DATABASE_KEY = "perstDatabase";
    
    private static boolean initialized = false;
    
    private PerstStorageManager() {}
    
    /**
     * Initialize Perst Storage.
     * Called from KissInit during application startup.
     * Stores the Storage handle in MainServlet environment.
     */
    public static synchronized void initialize() {
        if (initialized) {
            System.out.println("[PerstStorageManager] Already initialized");
            return;
        }
        
        if (!PerstConfig.getInstance().isPerstEnabled()) {
            System.out.println("[PerstStorageManager] Perst is not enabled in configuration");
            return;
        }
        
        try {
            Storage storage = createStorage();
            
            // Store in MainServlet environment (per creator's suggestion)
            org.kissweb.restServer.MainServlet.putEnvironment(STORAGE_KEY, storage);
            
            // Create and store root
            CDatabaseRoot root = (CDatabaseRoot) storage.getRoot();
            if (root == null) {
                root = new CDatabaseRoot();
                root.setCollections(storage);
                storage.setRoot(root);
                storage.commit();
            }
            org.kissweb.restServer.MainServlet.putEnvironment(ROOT_KEY, root);
            
            initialized = true;
            System.out.println("[PerstStorageManager] Perst Storage initialized and stored in MainServlet environment");
            
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new Storage instance.
     */
    private static Storage createStorage() throws Exception {
        Storage storage = org.garret.perst.StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.serialize.transient.objects", java.lang.Boolean.FALSE);
        storage.setProperty("perst.file.noflush", Boolean.TRUE);
        
        String dbPath = PerstConfig.getInstance().getDatabasePath();
        int poolSize = PerstConfig.getInstance().getPagePoolSize();
        
        // Ensure directory exists
        java.io.File dbFile = new java.io.File(dbPath);
        if (!dbFile.exists()) {
            dbFile.mkdirs();
        }
        
        storage.open(dbPath, poolSize);
        return storage;
    }
    
    /**
     * Get the Perst Storage instance from MainServlet environment.
     * Initializes if not already done.
     * 
     * @return Storage instance
     */
    public static Storage getStorage() {
        if (!initialized) {
            initialize();
        }
        
        Storage storage = (Storage) org.kissweb.restServer.MainServlet.getEnvironment(STORAGE_KEY);
        if (storage == null) {
            throw new IllegalStateException("Perst Storage not initialized. Call initialize() first.");
        }
        return storage;
    }
    
    /**
     * Get the CDatabaseRoot from MainServlet environment.
     * 
     * @return CDatabaseRoot instance
     */
    public static CDatabaseRoot getRoot() {
        if (!initialized) {
            initialize();
        }
        
        CDatabaseRoot root = (CDatabaseRoot) org.kissweb.restServer.MainServlet.getEnvironment(ROOT_KEY);
        if (root == null) {
            throw new IllegalStateException("Perst Root not initialized. Call initialize() first.");
        }
        return root;
    }
    
    /**
     * Check if Perst is available.
     * 
     * @return true if initialized and available
     */
    public static boolean isAvailable() {
        return initialized && getStorage() != null;
    }
    
    /**
     * Begin a transaction.
     */
    public static void beginTransaction() {
        Storage storage = getStorage();
        storage.beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
    }
    
    /**
     * Commit the current transaction.
     */
    public static void commitTransaction() {
        Storage storage = getStorage();
        storage.commit();
        storage.endThreadTransaction();
    }
    
    /**
     * Rollback the current transaction.
     */
    public static void rollbackTransaction() {
        Storage storage = getStorage();
        storage.rollback();
        storage.endThreadTransaction();
    }
    
    /**
     * Close the Perst Storage.
     * Should be called during application shutdown.
     */
    public static synchronized void close() {
        Storage storage = (Storage) org.kissweb.restServer.MainServlet.getEnvironment(STORAGE_KEY);
        if (storage != null) {
            storage.close();
            org.kissweb.restServer.MainServlet.putEnvironment(STORAGE_KEY, null);
            org.kissweb.restServer.MainServlet.putEnvironment(ROOT_KEY, null);
            initialized = false;
            System.out.println("[PerstStorageManager] Perst Storage closed");
        }
    }
}
