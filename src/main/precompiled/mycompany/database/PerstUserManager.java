package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.PerstUser;
import mycompany.domain.CDatabaseRoot;
import oodb.PerstStorageManager;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * PerstUserManager - Manages PerstUser domain objects.
 * 
 * This is the "Manager at the Gate" - ALL access to PerstUser entities
 * must go through this class.
 * 
 * Communicates directly with PerstStorageManager (no intermediate layers).
 * 
 * Responsibilities:
 * - CRUD operations
 * - Validation
 * - Authentication
 * - Password management
 * - Authorization checks
 */
public class PerstUserManager extends BaseManager<PerstUser> {
    
    private PerstUserManager() {}  // Prevent instantiation
    
    // ========== Authorization-Aware Methods ==========
    
    public static Collection<PerstUser> getAll(Actor actor) {
        if (!checkPermission(actor, ACTION_READ, PerstUser.class)) {
            return null;
        }
        return getAll();
    }
    
    public static PerstUser getByKey(Actor actor, String key) {
        if (!checkPermission(actor, ACTION_READ, PerstUser.class)) {
            return null;
        }
        return getByKey(key);
    }
    
    public static PerstUser create(Actor actor, Object... params) {
        if (!checkPermission(actor, ACTION_CREATE, PerstUser.class)) {
            return null;
        }
        return create(params);
    }
    
    public static boolean update(Actor actor, PerstUser user) {
        if (!checkPermission(actor, ACTION_UPDATE, PerstUser.class)) {
            return false;
        }
        return update(user);
    }
    
    public static boolean delete(Actor actor, PerstUser user) {
        if (!checkPermission(actor, ACTION_DELETE, PerstUser.class)) {
            return false;
        }
        return delete(user);
    }
    
    // ========== Base CRUD Methods (no authorization) ==========
    
    public static Collection<PerstUser> getAll() {
        if (!PerstStorageManager.isAvailable()) {
            return new ArrayList<>();
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<PerstUser> result = new ArrayList<>();
        for (PerstUser u : root.userIndex) {
            result.add(u);
        }
        return result;
    }
    
    public static PerstUser getByKey(String key) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        return root.userIndex.get(key);
    }
    
    public static PerstUser getByOid(long oid) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (PerstUser u : root.userIndex) {
            if (u.getOid() == oid) {
                return u;
            }
        }
        return null;
    }
    
    public static PerstUser authenticate(String username, String password) {
        if (!PerstStorageManager.isAvailable() || username == null || password == null) {
            return null;
        }
        
        PerstUser user = getByKey(username);
        if (user == null || !user.isActive()) {
            return null;
        }
        
        return user.checkPassword(password) ? user : null;
    }
    
    public static PerstUser create(Object... params) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        
        if (params.length < 2) {
            throw new IllegalArgumentException("PerstUser requires username and password");
        }
        
        String username = (String) params[0];
        
        if (getByKey(username) != null) {
            throw new IllegalArgumentException("User already exists: " + username);
        }
        
        PerstUser user = new PerstUser(username, (String) params[1], 
            params.length > 2 ? (Integer) params[2] : 0);
        
        if (params.length > 3) {
            user.setEmail((String) params[3]);
        }
        
        if (!validate(user)) {
            throw new IllegalArgumentException("Validation failed for PerstUser");
        }
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.userIndex.put(user);
            PerstStorageManager.commitTransaction();
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
        
        return user;
    }
    
    public static boolean update(PerstUser user) {
        if (!PerstStorageManager.isAvailable() || user == null) {
            return false;
        }
        
        if (!validate(user)) {
            return false;
        }
        
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.userIndex.put(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean delete(PerstUser user) {
        if (!PerstStorageManager.isAvailable() || user == null) {
            return false;
        }
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.userIndex.remove(user);
            PerstStorageManager.commitTransaction();
            return true;
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            return false;
        }
    }
    
    public static boolean validate(PerstUser user) {
        if (user == null) return false;
        if (user.getUsername() == null || user.getUsername().isEmpty()) return false;
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) return false;
        return true;
    }
    
    // ========== Password Management ==========
    
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        PerstUser user = authenticate(username, oldPassword);
        if (user == null) return false;
        user.setPassword(newPassword);
        return update(user);
    }
    
    public static boolean resetPassword(String username, String newPassword) {
        PerstUser user = getByKey(username);
        if (user == null) return false;
        user.setPassword(newPassword);
        return update(user);
    }
    
    public static boolean deactivate(String username) {
        PerstUser user = getByKey(username);
        if (user == null) return false;
        user.setActive(false);
        return update(user);
    }
    
    public static boolean activate(String username) {
        PerstUser user = getByKey(username);
        if (user == null) return false;
        user.setActive(true);
        return update(user);
    }
    
    public static boolean exists(String username) {
        return getByKey(username) != null;
    }
    
    public static List<PerstUser> getActiveUsers() {
        List<PerstUser> result = new ArrayList<>();
        Collection<PerstUser> all = getAll();
        if (all == null) return result;
        for (PerstUser user : all) {
            if (user.isActive()) {
                result.add(user);
            }
        }
        return result;
    }
}
