package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.PerstUser;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * PerstUserManager - Manages PerstUser domain objects.
 * 
 * This is the "Manager at the Gate" - ALL access to PerstUser entities
 * must go through this class.
 * 
 * All methods are static - no singleton needed. Thread safety is handled
 * by PerstHelper which uses thread-local PerstContext.
 * 
 * Responsibilities:
 * - CRUD operations
 * - Validation
 * - Authentication
 * - Password management
 * - Authorization checks
 * 
 * IMPORTANT: For authorization, services must obtain the current Actor
 * from UserData and pass it to Manager methods:
 * 
 *   UserData ud = servlet.getUserData();
 *   Actor actor = ActorManager.getByUserId((int) ud.getUserId());
 *   PerstUser user = PerstUserManager.getByKey(actor, username);
 */
public class PerstUserManager extends BaseManager<PerstUser> {
    
    private PerstUserManager() {}  // Prevent instantiation
    
    // ========== Static Authorization-Aware Methods ==========
    
    /**
     * Get all PerstUsers (with authorization check)
     */
    public static Collection<PerstUser> getAll(Actor actor) {
        if (!checkPermission(actor, ACTION_READ, "PerstUser")) {
            return null;
        }
        return getAll();
    }
    
    /**
     * Get PerstUser by key (with authorization check)
     */
    public static PerstUser getByKey(Actor actor, String key) {
        if (!checkPermission(actor, ACTION_READ, "PerstUser")) {
            return null;
        }
        return getByKey(key);
    }
    
    /**
     * Create PerstUser (with authorization check)
     */
    public static PerstUser create(Actor actor, Object... params) {
        if (!checkPermission(actor, ACTION_CREATE, "PerstUser")) {
            return null;
        }
        return create(params);
    }
    
    /**
     * Update PerstUser (with authorization check)
     */
    public static boolean update(Actor actor, PerstUser user) {
        if (!checkPermission(actor, ACTION_UPDATE, "PerstUser")) {
            return false;
        }
        return update(user);
    }
    
    /**
     * Delete PerstUser (with authorization check)
     */
    public static boolean delete(Actor actor, PerstUser user) {
        if (!checkPermission(actor, ACTION_DELETE, "PerstUser")) {
            return false;
        }
        return delete(user);
    }
    
    // ========== Static Base Methods ==========
    
    /**
     * Get all PerstUsers (no authorization)
     */
    public static Collection<PerstUser> getAll() {
        if (!isPerstAvailable()) {
            return new ArrayList<>();
        }
        return PerstHelper.retrieveAllObjects(PerstUser.class);
    }
    
    /**
     * Get PerstUser by username
     */
    public static PerstUser getByKey(String key) {
        if (!isPerstAvailable()) {
            return null;
        }
        return PerstHelper.retrieveObject(PerstUser.class, "username", key);
    }
    
    /**
     * Authenticate user by username and password
     * 
     * @return User if authenticated, null otherwise
     */
    public static PerstUser authenticate(String username, String password) {
        if (!isPerstAvailable() || username == null || password == null) {
            return null;
        }
        
        PerstUser user = getByKey(username);
        if (user == null || !user.isActive()) {
            return null;
        }
        
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return null;
        }
        
        // Support both plain text and SHA-256 (64 chars)
        if (storedPassword.length() == 64) {
            // SHA-256 hash comparison would go here
            return storedPassword.equals(password) ? user : null;
        } else {
            // Plain text comparison
            return storedPassword.equals(password) ? user : null;
        }
    }
    
    /**
     * Create a new PerstUser
     */
    public static PerstUser create(Object... params) {
        if (!isPerstAvailable()) {
            return null;
        }
        
        if (params.length < 2) {
            throw new IllegalArgumentException("PerstUser requires username and password");
        }
        
        String username = (String) params[0];
        String password = (String) params[1];
        
        // Check if user already exists
        if (getByKey(username) != null) {
            throw new IllegalArgumentException("User already exists: " + username);
        }
        
        PerstUser user = new PerstUser(username, password, params.length > 2 ? (Integer) params[2] : 0);
        
        if (params.length > 3) {
            user.setEmail((String) params[3]);
        }
        
        if (!validate(user)) {
            throw new IllegalArgumentException("Validation failed for PerstUser");
        }
        
        PerstHelper.storeNewObject(user);
        return user;
    }
    
    /**
     * Update a PerstUser
     */
    public static boolean update(PerstUser user) {
        if (!isPerstAvailable() || user == null) {
            return false;
        }
        
        if (!validate(user)) {
            return false;
        }
        
        PerstHelper.storeModifiedObject(user);
        return true;
    }
    
    /**
     * Delete a PerstUser
     */
    public static boolean delete(PerstUser user) {
        if (!isPerstAvailable() || user == null) {
            return false;
        }
        
        PerstHelper.removeObject(user);
        return true;
    }
    
    /**
     * Validate a PerstUser
     */
    public static boolean validate(PerstUser user) {
        if (user == null) {
            return false;
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return false;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return false;
        }
        return true;
    }
    
    /**
     * Update password
     */
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        PerstUser user = authenticate(username, oldPassword);
        if (user == null) {
            return false;
        }
        
        user.setPassword(newPassword);
        return update(user);
    }
    
    /**
     * Reset password (admin function)
     */
    public static boolean resetPassword(String username, String newPassword) {
        PerstUser user = getByKey(username);
        if (user == null) {
            return false;
        }
        
        user.setPassword(newPassword);
        return update(user);
    }
    
    /**
     * Deactivate user (soft delete)
     */
    public static boolean deactivate(String username) {
        PerstUser user = getByKey(username);
        if (user == null) {
            return false;
        }
        
        user.setActive(false);
        return update(user);
    }
    
    /**
     * Activate user
     */
    public static boolean activate(String username) {
        PerstUser user = getByKey(username);
        if (user == null) {
            return false;
        }
        
        user.setActive(true);
        return update(user);
    }
    
    /**
     * Check if user exists
     */
    public static boolean exists(String username) {
        return getByKey(username) != null;
    }
    
    /**
     * Get all active users
     */
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
