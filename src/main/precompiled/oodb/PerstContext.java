package oodb;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.IOError;
import org.garret.perst.IterableIterator;
import org.garret.perst.Key;
import org.garret.perst.SortedCollection;
import org.garret.perst.PersistentComparator;
import org.garret.perst.FieldIndex;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import mycompany.domain.CDatabaseRoot;
import mycompany.domain.PerstUser;
import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.domain.Group;

/**
 * PerstContext - Provides Perst database operations using CDatabase for versioning.
 * 
 * Features:
 * - Automatic versioning of all entities (CVersion subclasses)
 * - Full transaction support (begin, commit, rollback)
 * - Temporal queries (query historical versions)
 * - Audit trail (all changes tracked)
 * 
 * IMPORTANT: All entities stored through this context must extend CVersion
 * to leverage the automatic versioning feature.
 */
public class PerstContext {
    private static PerstContext instance;
    
    private Storage storage;
    private CDatabase database;
    private boolean initialized = false;
    private CDatabaseRoot root;
    
    private PerstContext() {}
    
    public static synchronized PerstContext getInstance() {
        if (instance == null) {
            instance = new PerstContext();
        }
        return instance;
    }
    
    public static synchronized void setInstance(PerstContext testInstance) {
        instance = testInstance;
    }
    
    /**
     * Initialize Perst database with CDatabase for versioning support.
     */
    public synchronized void initialize() {
        if (initialized || !PerstConfig.getInstance().isPerstEnabled()) {
            return;
        }
        
        if (!PerstConfig.getInstance().isUseCDatabase()) {
            System.out.println("[PerstContext] CDatabase disabled, using standard Storage");
            initializeStandard();
            return;
        }
        
        try {
            System.out.println("[PerstContext] Initializing Perst CDatabase for versioning...");
            
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
            
            database = CDatabase.instance;
            database.open(storage, dbPath + "/idx");
            
            root = (CDatabaseRoot) storage.getRoot();
            if (root == null) {
                root = new CDatabaseRoot();
                root.setCollections(storage);
                storage.setRoot(root);
                storage.commit();
            }
            
            initialized = true;
            System.out.println("[PerstContext] Perst CDatabase initialized successfully with versioning.");
            
        } catch (IOError e) {
            System.err.println("[PerstContext] Failed to initialize Perst CDatabase: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
        } catch (Exception e) {
            System.err.println("[PerstContext] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
        }
    }
    
    /**
     * Initialize standard Storage (without versioning) - fallback mode.
     */
    private void initializeStandard() {
        try {
            System.out.println("[PerstContext] Initializing Perst standard Storage...");
            
            storage = org.garret.perst.StorageFactory.getInstance().createStorage();
            storage.setProperty("perst.serialize.transient.objects", java.lang.Boolean.FALSE);
            storage.setProperty("perst.file.noflush", Boolean.TRUE);
            
            String dbPath = PerstConfig.getInstance().getDatabasePath();
            int poolSize = PerstConfig.getInstance().getPagePoolSize();
            storage.open(dbPath, poolSize);
            
            root = (CDatabaseRoot) storage.getRoot();
            if (root == null) {
                root = new CDatabaseRoot();
                root.setCollections(storage);
                storage.setRoot(root);
                storage.commit();
            }
            
            initialized = true;
            System.out.println("[PerstContext] Perst standard Storage initialized.");
            
        } catch (Exception e) {
            System.err.println("[PerstContext] Failed to initialize Perst: " + e.getMessage());
            initialized = false;
        }
    }
    
    /**
     * Close the database.
     */
    public synchronized void close() {
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.close();
        }
        if (storage != null) {
            storage.close();
        }
        initialized = false;
        System.out.println("[PerstContext] Perst database closed.");
    }
    
    /**
     * Check if Perst is available
     */
    public boolean isAvailable() {
        if (PerstConfig.getInstance().isPerstEnabled() && !initialized) {
            initialize();
        }
        return PerstConfig.getInstance().isPerstEnabled() && initialized;
    }
    
    /**
     * Check if CDatabase versioning is enabled
     */
    public boolean isVersioningEnabled() {
        return isAvailable() && PerstConfig.getInstance().isUseCDatabase() && database != null;
    }
    
    // ==================== Transaction Operations ====================
    
    /**
     * Begin a new transaction.
     * Required for all write operations in CDatabase mode.
     */
    public void beginTransaction() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.beginTransaction();
            storage.beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
        }
    }
    
    /**
     * Commit the current transaction.
     * Saves all changes made since beginTransaction().
     */
    public void commitTransaction() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.commitTransaction();
            storage.endThreadTransaction();
        } else {
            storage.commit();
        }
    }
    
    /**
     * Rollback the current transaction.
     * Discards all changes made since beginTransaction().
     */
    public void rollbackTransaction() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.rollbackTransaction();
            storage.endThreadTransaction();
        }
    }
    
    // ==================== Actor CRUD Operations ====================
    
    public Actor retrieveActor(Class<Actor> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if ("username".equals(field) || "name".equals(field)) {
            return root.actorIndex.get(value);
        }
        return null;
    }
    
    public Actor retrieveActorByUuid(String uuid) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        for (Actor a : root.actorIndex) {
            if (a.getUuid() != null && a.getUuid().equals(uuid)) {
                return a;
            }
        }
        return null;
    }
    
    public Collection<Actor> retrieveAllActors(Class<Actor> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        List<Actor> result = new ArrayList<>();
        for (Actor a : root.actorIndex) {
            result.add(a);
        }
        return result;
    }
    
    public void storeActor(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.insert(obj);
        } else {
            root.actorIndex.put(obj);
        }
    }
    
    public void updateActor(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.update(obj);
        } else {
            root.actorIndex.put(obj);
        }
    }
    
    public void removeActor(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.delete(obj);
        } else {
            root.actorIndex.remove(obj);
        }
    }
    
    // ==================== PerstUser CRUD Operations ====================
    
    public PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if ("username".equals(field)) {
            return root.userIndex.get(value);
        }
        return null;
    }
    
    public PerstUser retrieveUserById(int userId) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        for (PerstUser u : root.userIndex) {
            if (u.getUserId() == userId) {
                return u;
            }
        }
        return null;
    }
    
    public Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        List<PerstUser> result = new ArrayList<>();
        for (PerstUser u : root.userIndex) {
            result.add(u);
        }
        return result;
    }
    
    public void storeUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.insert(obj);
        } else {
            root.userIndex.put(obj);
        }
    }
    
    public void updateUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.update(obj);
        } else {
            root.userIndex.put(obj);
        }
    }
    
    public void removeUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.delete(obj);
        } else {
            root.userIndex.remove(obj);
        }
    }
    
    // ==================== Agreement CRUD Operations ====================
    
    public Collection<Agreement> retrieveAllAgreements() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        List<Agreement> result = new ArrayList<>();
        for (Agreement a : root.agreementIndex) {
            result.add(a);
        }
        return result;
    }
    
    public void storeAgreement(Agreement obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.insert(obj);
        } else {
            root.agreementIndex.put(obj);
        }
    }
    
    public void updateAgreement(Agreement obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.update(obj);
        } else {
            root.agreementIndex.put(obj);
        }
    }
    
    // ==================== Group CRUD Operations ====================
    
    public Collection<Group> retrieveAllGroups() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        List<Group> result = new ArrayList<>();
        for (Group g : root.groupIndex) {
            result.add(g);
        }
        return result;
    }
    
    public Group retrieveGroupByName(String name) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        return root.groupIndex.get(name);
    }
    
    public void storeGroup(Group obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.insert(obj);
        } else {
            root.groupIndex.put(obj);
        }
    }
    
    public void updateGroup(Group obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        if (database != null && PerstConfig.getInstance().isUseCDatabase()) {
            database.update(obj);
        } else {
            root.groupIndex.put(obj);
        }
    }
    
    // ==================== Version History Operations ====================
    
    /**
     * Get version history for an object.
     * Note: CDatabase automatically versions all changes. This returns the current version.
     * Full version history retrieval requires using VersionSelector with queries.
     */
    public <T extends CVersion> List<T> getVersionHistory(Class<T> clazz, String keyField, String keyValue) {
        List<T> history = new ArrayList<>();
        
        if (!isVersioningEnabled()) {
            return history;
        }
        
        try {
            T current = getCurrentVersion(clazz, keyField, keyValue);
            if (current != null) {
                history.add(current);
            }
        } catch (Exception e) {
            System.err.println("[PerstContext] Error getting version history: " + e.getMessage());
        }
        
        return history;
    }
    
    /**
     * Get the current version of an object.
     */
    public <T extends CVersion> T getCurrentVersion(Class<T> clazz, String keyField, String keyValue) {
        if (clazz == Actor.class) {
            return (T) retrieveActorByUuid(keyValue);
        } else if (clazz == PerstUser.class) {
            return (T) retrieveUser(null, "username", keyValue);
        }
        return null;
    }
    
    // ==================== User Context Operations ====================
    
    public Actor getActorByUserId(Object userId) {
        if (userId == null || !isAvailable()) {
            return null;
        }
        
        if (userId instanceof Integer) {
            Actor actor = Actor.findByUserId((Integer) userId);
            if (actor != null) {
                return actor;
            }
        }
        
        if (userId instanceof String) {
            try {
                int id = Integer.parseInt((String) userId);
                return Actor.findByUserId(id);
            } catch (NumberFormatException e) {
                return retrieveActorByUuid((String) userId);
            }
        }
        
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
    
    public PerstUser getUserById(Object userId) {
        if (userId == null || !isAvailable()) {
            return null;
        }
        
        if (userId instanceof Integer) {
            return retrieveUserById((Integer) userId);
        }
        
        if (userId instanceof String) {
            try {
                return retrieveUserById(Integer.parseInt((String) userId));
            } catch (NumberFormatException e) {
                return retrieveUser(null, "username", (String) userId);
            }
        }
        
        return null;
    }
    
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
    
    public static boolean isAuthenticated() {
        return getCurrentActor() != null || getCurrentUser() != null;
    }
}
