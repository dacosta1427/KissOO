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
            } else {
                // Root exists - ensure all indexes are initialized (in case new ones were added)
                root.setCollections(storage);
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
        if (!isAvailable()) throw new IllegalStateException("Perst not available - ensure PerstStorageManager.initialize() is called in KissInit");
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
    
    // ========== Generic CRUD Operations ==========
    
    /**
     * Save an object to the database.
     * Perst must be initialized first (called from KissInit).
     */
    public static void save(Object obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available - ensure initialize() is called in KissInit");
        
        try {
            CDatabaseRoot root = getRoot();
            
            // Determine index based on object type
            if (obj instanceof mycompany.domain.PerstUser) {
                ((mycompany.domain.PerstUser) obj).index();
                root.userIndex.put((mycompany.domain.PerstUser) obj);
            } else if (obj instanceof mycompany.domain.Actor) {
                root.actorIndex.put((mycompany.domain.Actor) obj);
            } else if (obj instanceof mycompany.domain.Phone) {
                root.phoneIndex.put((mycompany.domain.Phone) obj);
            } else if (obj instanceof mycompany.domain.BenchmarkData) {
                root.benchmarkIndex.put((mycompany.domain.BenchmarkData) obj);
            } else if (obj instanceof mycompany.domain.Agreement) {
                root.agreementIndex.put((mycompany.domain.Agreement) obj);
            } else if (obj instanceof mycompany.domain.Group) {
                root.groupIndex.put((mycompany.domain.Group) obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save object: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save an object within a transaction.
     * Caller must handle transaction.
     */
    public static void saveInTransaction(Object obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        save(obj);
    }
    
    /**
     * Delete an object from the database.
     */
    public static void delete(Object obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        try {
            CDatabaseRoot root = getRoot();
            
            if (obj instanceof mycompany.domain.PerstUser) {
                root.userIndex.remove((mycompany.domain.PerstUser) obj);
            } else if (obj instanceof mycompany.domain.Actor) {
                root.actorIndex.remove((mycompany.domain.Actor) obj);
            } else if (obj instanceof mycompany.domain.Phone) {
                root.phoneIndex.remove((mycompany.domain.Phone) obj);
            } else if (obj instanceof mycompany.domain.BenchmarkData) {
                root.benchmarkIndex.remove((mycompany.domain.BenchmarkData) obj);
            } else if (obj instanceof mycompany.domain.Agreement) {
                root.agreementIndex.remove((mycompany.domain.Agreement) obj);
            } else if (obj instanceof mycompany.domain.Group) {
                root.groupIndex.remove((mycompany.domain.Group) obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete an object within a transaction.
     */
    public static void deleteInTransaction(Object obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        delete(obj);
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
    
    // ========== Storage Query Methods ==========
    
    /**
     * Get all objects of a given class.
     * 
     * @param clazz the class type to retrieve
     * @return collection of objects
     */
    public static java.util.Collection<java.lang.Object> getAll(Class<?> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = getRoot();
        java.util.Collection<java.lang.Object> result = new java.util.ArrayList<>();
        
        if (clazz == mycompany.domain.PerstUser.class) {
            for (mycompany.domain.PerstUser u : root.userIndex) result.add(u);
        } else if (clazz == mycompany.domain.Actor.class) {
            for (mycompany.domain.Actor a : root.actorIndex) result.add(a);
        } else if (clazz == mycompany.domain.Phone.class) {
            for (mycompany.domain.Phone p : root.phoneIndex) result.add(p);
        } else if (clazz == mycompany.domain.BenchmarkData.class) {
            for (mycompany.domain.BenchmarkData b : root.benchmarkIndex) result.add(b);
        } else if (clazz == mycompany.domain.Agreement.class) {
            for (mycompany.domain.Agreement a : root.agreementIndex) result.add(a);
        } else if (clazz == mycompany.domain.Group.class) {
            for (mycompany.domain.Group g : root.groupIndex) result.add(g);
        }
        
        return result;
    }
}
