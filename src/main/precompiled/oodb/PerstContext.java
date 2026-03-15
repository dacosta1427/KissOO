package oodb;

import org.garret.perst.Storage;
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
 * PerstContext - Delegates to PerstStorageManager for Perst operations.
 * 
 * This class provides a convenient API for Perst operations, delegating
 * to PerstStorageManager which manages the Storage lifecycle via
 * MainServlet.getEnvironment()/putEnvironment().
 * 
 * All methods delegate to PerstStorageManager for storage access.
 */
public class PerstContext {
    
    private PerstContext() {}
    
    /**
     * Get the singleton instance.
     * Note: Now delegates to PerstStorageManager internally.
     */
    public static PerstContext getInstance() {
        return new PerstContext();
    }
    
    /**
     * Check if Perst is available.
     */
    public boolean isAvailable() {
        return PerstStorageManager.isAvailable();
    }
    
    /**
     * Check if versioning is enabled.
     */
    public boolean isVersioningEnabled() {
        return PerstConfig.getInstance().isUseCDatabase();
    }
    
    /**
     * Initialize Perst - delegates to PerstStorageManager.
     */
    public synchronized void initialize() {
        PerstStorageManager.initialize();
    }
    
    // ==================== Transaction Operations ====================
    
    public void beginTransaction() {
        PerstStorageManager.beginTransaction();
    }
    
    public void commitTransaction() {
        PerstStorageManager.commitTransaction();
    }
    
    public void rollbackTransaction() {
        PerstStorageManager.rollbackTransaction();
    }
    
    // ==================== Actor Operations ====================
    
    public Actor retrieveActor(Class<Actor> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        if ("username".equals(field) || "name".equals(field)) {
            return root.actorIndex.get(value);
        }
        return null;
    }
    
    public Actor retrieveActorByUuid(String uuid) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (Actor a : root.actorIndex) {
            if (a.getUuid() != null && a.getUuid().equals(uuid)) {
                return a;
            }
        }
        return null;
    }
    
    public Collection<Actor> retrieveAllActors(Class<Actor> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<Actor> result = new ArrayList<>();
        for (Actor a : root.actorIndex) {
            result.add(a);
        }
        return result;
    }
    
    public void storeActor(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.actorIndex.put(obj);
    }
    
    public void updateActor(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.actorIndex.put(obj);
    }
    
    public void removeActor(Actor obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.actorIndex.remove(obj);
    }
    
    // ==================== PerstUser Operations ====================
    
    public PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        if ("username".equals(field)) {
            return root.userIndex.get(value);
        }
        return null;
    }
    
    public PerstUser retrieveUserById(int userId) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (PerstUser u : root.userIndex) {
            if (u.getUserId() == userId) {
                return u;
            }
        }
        return null;
    }
    
    public Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<PerstUser> result = new ArrayList<>();
        for (PerstUser u : root.userIndex) {
            result.add(u);
        }
        return result;
    }
    
    public void storeUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.userIndex.put(obj);
    }
    
    public void updateUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.userIndex.put(obj);
    }
    
    public void removeUser(PerstUser obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.userIndex.remove(obj);
    }
    
    // ==================== Agreement Operations ====================
    
    public Collection<Agreement> retrieveAllAgreements() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<Agreement> result = new ArrayList<>();
        for (Agreement a : root.agreementIndex) {
            result.add(a);
        }
        return result;
    }
    
    public void storeAgreement(Agreement obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.agreementIndex.put(obj);
    }
    
    public void updateAgreement(Agreement obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.agreementIndex.put(obj);
    }
    
    // ==================== Group Operations ====================
    
    public Collection<Group> retrieveAllGroups() {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<Group> result = new ArrayList<>();
        for (Group g : root.groupIndex) {
            result.add(g);
        }
        return result;
    }
    
    public Group retrieveGroupByName(String name) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        return root.groupIndex.get(name);
    }
    
    public void storeGroup(Group obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.groupIndex.put(obj);
    }
    
    public void updateGroup(Group obj) {
        if (!isAvailable()) throw new IllegalStateException("Perst not available");
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        root.groupIndex.put(obj);
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
}
