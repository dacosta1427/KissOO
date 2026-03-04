package domain.kissweb;

import org.garret.perst.Storage;
import org.garret.perst.FieldIndex;
import org.garret.perst.Key;

import java.util.Collection;
import java.util.ArrayList;

import domain.PerstDBRoot;
import domain.PerstUser;
import domain.Actor;

/**
 * PerstContext - Provides Perst database operations.
 * Uses FieldIndex for indexed access.
 */
public class PerstContext {
    private static PerstContext instance;
    
    private Storage storage;
    private boolean initialized = false;
    private PerstDBRoot root;
    
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
            root = (PerstDBRoot) storage.getRoot();
            if (root == null) {
                root = new PerstDBRoot();
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
    
    // ==================== User Context Operations ====================
    
    /**
     * Get the currently logged in Actor from the kissweb UserData.
     * This should be called after authentication has been verified by the framework.
     * 
     * @param userId The user ID from ProcessServlet.getUserData().getUserId()
     * @return The Actor associated with this user, or null if not found
     */
    public Actor getActorByUserId(Object userId) {
        if (userId == null || !isAvailable()) {
            return null;
        }
        
        // Try by Integer ID
        if (userId instanceof Integer) {
            Actor actor = Actor.findByUserId((Integer) userId);
            if (actor != null) {
                return actor;
            }
        }
        
        // Try by String
        if (userId instanceof String) {
            try {
                int id = Integer.parseInt((String) userId);
                return Actor.findByUserId(id);
            } catch (NumberFormatException e) {
                return Actor.findByUuid((String) userId);
            }
        }
        
        // Fallback: search all actors
        for (Actor actor : root.actorIndex) {
            if (actor.getPerstUser() != null) {
                Object actorUserId = actor.getPerstUser().getUserId();
                if (userId.equals(actorUserId) || 
                    (userId instanceof String && actorUserId instanceof String && 
                     userId.equals(actorUserId))) {
                    return actor;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the currently logged in PerstUser from the kissweb UserData.
     * 
     * @param userId The user ID from ProcessServlet.getUserData().getUserId()
     * @return The PerstUser, or null if not found
     */
    public PerstUser getUserById(Object userId) {
        if (userId == null || !isAvailable()) {
            return null;
        }
        
        if (userId instanceof Integer) {
            return PerstUser.get((Integer) userId);
        }
        
        if (userId instanceof String) {
            try {
                return PerstUser.get(Integer.parseInt((String) userId));
            } catch (NumberFormatException e) {
                return PerstUser.getByUsername((String) userId);
            }
        }
        
        return null;
    }
    
    /**
     * Get the currently logged in Actor from the current HTTP request.
     * This uses ProcessServlet.getInstance() to get the current user context.
     * 
     * @return The current Actor, or null if not authenticated
     */
    public static Actor getCurrentActor() {
        try {
            org.kissweb.restServer.ProcessServlet ps = 
                org.kissweb.restServer.ProcessServlet.getInstance();
            if (ps == null) {
                return null;
            }
            org.kissweb.restServer.UserData ud = ps.getUserData();
            if (ud == null) {
                return null;
            }
            Object userId = ud.getUserId();
            if (userId == null) {
                return null;
            }
            return getInstance().getActorByUserId(userId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get the currently logged in PerstUser from the current HTTP request.
     * This uses ProcessServlet.getInstance() to get the current user context.
     * 
     * @return The current PerstUser, or null if not authenticated
     */
    public static PerstUser getCurrentUser() {
        try {
            org.kissweb.restServer.ProcessServlet ps = 
                org.kissweb.restServer.ProcessServlet.getInstance();
            if (ps == null) {
                return null;
            }
            org.kissweb.restServer.UserData ud = ps.getUserData();
            if (ud == null) {
                return null;
            }
            Object userId = ud.getUserId();
            if (userId == null) {
                return null;
            }
            return getInstance().getUserById(userId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if the current request is authenticated.
     * 
     * @return true if user is logged in
     */
    public static boolean isAuthenticated() {
        return getCurrentActor() != null || getCurrentUser() != null;
    }
}
