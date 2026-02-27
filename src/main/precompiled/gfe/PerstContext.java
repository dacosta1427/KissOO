package gfe;

import org.garret.perst.Storage;
import org.garret.perst.FieldIndex;
import org.garret.perst.Key;

import java.util.Collection;
import java.util.ArrayList;

/**
 * PerstContext - Provides Perst database operations.
 * Uses FieldIndex for indexed access.
 */
public class PerstContext {
    private static PerstContext instance;
    
    private Storage storage;
    private boolean initialized = false;
    private Root root;
    
    private PerstContext() {}
    
    public static synchronized PerstContext getInstance() {
        if (instance == null) {
            instance = new PerstContext();
        }
        return instance;
    }
    
    public synchronized void initialize() {
        if (initialized || !PerstConfig.getInstance().isPerstEnabled()) {
            return;
        }
        
        try {
            System.out.println("[PerstContext] Initializing Perst database...");
            
            storage = org.garret.perst.StorageFactory.getInstance().createStorage();
            storage.setProperty("perst.serialize.transient.objects", java.lang.Boolean.FALSE);
            storage.setProperty("perst.file.noflush", Boolean.TRUE);
            
            String dbPath = PerstConfig.getInstance().getDatabasePath();
            System.out.println("[PerstContext] Database path: " + dbPath);
            
            java.io.File dbFile = new java.io.File(dbPath);
            java.io.File dbDir = dbFile.getParentFile();
            if (dbDir != null && !dbDir.canWrite()) {
                String tempPath = System.getProperty("java.io.tmpdir") + "perst_" + System.currentTimeMillis() + ".dbs";
                System.out.println("[PerstContext] Cannot write to configured path, using temp: " + tempPath);
                dbPath = tempPath;
                dbFile = new java.io.File(tempPath);
            }
            
            java.io.File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            int poolSize = PerstConfig.getInstance().getPagePoolSize();
            storage.open(dbPath, poolSize);
            
            // Get or create root with indexes
            root = (Root) storage.getRoot();
            if (root == null) {
                root = new Root();
                root.setCollections(storage);
                storage.setRoot(root);
                storage.commit();
            }
            
            initialized = true;
            System.out.println("[PerstContext] Perst database initialized successfully.");
            
        } catch (Exception e) {
            System.err.println("[PerstContext] Failed to initialize Perst: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
        }
    }
    
    public synchronized void close() {
        if (storage != null) {
            storage.close();
        }
        initialized = false;
        System.out.println("[PerstContext] Perst database closed.");
    }
    
    public boolean isAvailable() {
        if (PerstConfig.getInstance().isPerstEnabled() && !initialized) {
            initialize();
        }
        return PerstConfig.getInstance().isPerstEnabled() && initialized;
    }
    
    // Actor operations
    public Actor retrieveObject(Class<Actor> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if ("username".equals(field)) {
            return root.actorIndex.get(value);
        }
        return null;
    }
    
    public Collection<Actor> retrieveAllObjects(Class<Actor> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        ArrayList<Actor> result = new ArrayList<>();
        for (Actor a : root.actorIndex) {
            result.add(a);
        }
        return result;
    }
    
    public void storeNewObject(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        root.actorIndex.put(obj);
    }
    
    public void storeModifiedObject(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        root.actorIndex.put(obj);
    }
    
    public void removeObject(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        root.actorIndex.remove(obj);
    }
    
    // PerstUser operations
    public PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if ("username".equals(field)) {
            return root.userIndex.get(value);
        }
        return null;
    }
    
    public Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        ArrayList<PerstUser> result = new ArrayList<>();
        for (PerstUser u : root.userIndex) {
            result.add(u);
        }
        return result;
    }
    
    public void storeNewUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        root.userIndex.put(obj);
    }
    
    public void storeModifiedUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        root.userIndex.put(obj);
    }
    
    public void removeUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        root.userIndex.remove(obj);
    }
}
