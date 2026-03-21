package oodb;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CDatabase;

/**
 * PerstStorageManager - Manages Perst CDatabase lifecycle.
 * 
 * CDatabase handles ALL functionality:
 * - Versioning via CVersion base class
 * - Indexing via @Indexable annotations
 * - Full-text search via @FullTextSearchable annotations
 * - History/Lex automatically managed by CDatabase
 * 
 * This class is ONLY for initialization and access.
 */
public class PerstStorageManager {
    
    private static final String DATABASE_KEY = "perstDatabase";
    private static boolean initialized = false;
    
    private PerstStorageManager() {}
    
    /**
     * Initialize CDatabase.
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
            
            Storage storage = createStorage();
            
            CDatabase db = new CDatabase();
            db.open(storage, indexPath);
            
            org.kissweb.restServer.MainServlet.putEnvironment(DATABASE_KEY, db);
            
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
     * Get the CDatabase instance.
     */
    public static CDatabase getDatabase() {
        if (!initialized) {
            initialize();
        }
        return (CDatabase) org.kissweb.restServer.MainServlet.getEnvironment(DATABASE_KEY);
    }
    
    /**
     * Close CDatabase.
     */
    public static synchronized void close() {
        CDatabase db = (CDatabase) org.kissweb.restServer.MainServlet.getEnvironment(DATABASE_KEY);
        if (db != null) {
            db.close();
            org.kissweb.restServer.MainServlet.putEnvironment(DATABASE_KEY, null);
        }
        initialized = false;
    }
}
