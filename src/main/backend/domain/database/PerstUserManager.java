package domain.database;

import domain.PerstUser;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * PerstUserManager - Manages PerstUser domain objects.
 * 
 * This is the "Manager at the Gate" - ALL access to PerstUser entities
 * must go through this class.
 * 
 * Responsibilities:
 * - CRUD operations
 * - Validation
 * - Authentication
 * - Password management
 * - Authorization checks (future)
 */
public class PerstUserManager extends BaseManager<PerstUser> {
    
    private static PerstUserManager instance;
    
    private PerstUserManager() {}
    
    public static synchronized PerstUserManager getInstance() {
        if (instance == null) {
            instance = new PerstUserManager();
        }
        return instance;
    }
    
    @Override
    public Collection<PerstUser> getAll() {
        if (!isPerstAvailable()) {
            return new ArrayList<>();
        }
        return PerstHelper.retrieveAllObjects(PerstUser.class);
    }
    
    @Override
    public PerstUser getByKey(String key) {
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
    public PerstUser authenticate(String username, String password) {
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
            // For now, simple comparison
            return storedPassword.equals(password) ? user : null;
        } else {
            // Plain text comparison
            return storedPassword.equals(password) ? user : null;
        }
    }
    
    @Override
    public PerstUser create(Object... params) {
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
    
    @Override
    public boolean update(PerstUser user) {
        if (!isPerstAvailable() || user == null) {
            return false;
        }
        
        if (!validate(user)) {
            return false;
        }
        
        PerstHelper.storeModifiedObject(user);
        return true;
    }
    
    @Override
    public boolean delete(PerstUser user) {
        if (!isPerstAvailable() || user == null) {
            return false;
        }
        
        PerstHelper.removeObject(user);
        return true;
    }
    
    @Override
    protected boolean validate(PerstUser user) {
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
    public boolean changePassword(String username, String oldPassword, String newPassword) {
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
    public boolean resetPassword(String username, String newPassword) {
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
    public boolean deactivate(String username) {
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
    public boolean activate(String username) {
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
    public boolean exists(String username) {
        return getByKey(username) != null;
    }
    
    /**
     * Get all active users
     */
    public List<PerstUser> getActiveUsers() {
        List<PerstUser> result = new ArrayList<>();
        Collection<PerstUser> all = getAll();
        
        for (PerstUser user : all) {
            if (user.isActive()) {
                result.add(user);
            }
        }
        return result;
    }
}
