package koo.security;

import koo.oodb.core.actor.Role;
import koo.oodb.core.StorageManager;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * RolePermissions - Stores default permission bitmaps for each Role.
 * 
 * Persisted in Perst. Each role has a default bitmap of allowed endpoints.
 * New actors inherit these permissions unless explicitly overridden.
 * 
 * Usage:
 *   // Get default permissions for a role
 *   BigInteger perms = RolePermissions.getDefaultPermissions(Role.ADMIN);
 *   
 *   // Set default permissions for a role
 *   RolePermissions.setDefaultPermissions(Role.OWNER, bitmap);
 */
public class RolePermissions extends CVersion {
    
    private static final String SINGLETON_KEY = "RolePermissionsSingleton";
    private static RolePermissions instance;
    
    @Indexable
    private String name;  // Always "RolePermissionsSingleton"
    
    // Map of Role -> permission bitmap (stored as String for Perst)
    private Map<String, String> rolePermissions;
    
    public RolePermissions() {
        this.name = SINGLETON_KEY;
        this.rolePermissions = new HashMap<>();
        initializeDefaults();
    }
    
    /**
     * Initialize default permission bitmaps for each role
     */
    private void initializeDefaults() {
        // Default: MEMBER has no permissions
        rolePermissions.put(Role.MEMBER.name(), "0");
        
        // Default: CLEANER basic permissions (will be populated later)
        rolePermissions.put(Role.CLEANER.name(), "0");
        
        // Default: OWNER basic permissions (will be populated later)
        rolePermissions.put(Role.OWNER.name(), "0");
        
        // Default: ADMIN has all business permissions
        rolePermissions.put(Role.ADMIN.name(), "0");
        
        // Default: SUPER_ADMIN has all permissions
        rolePermissions.put(Role.SUPER_ADMIN.name(), "0");
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized RolePermissions getInstance() {
        if (instance == null) {
            instance = loadFromStorage();
            if (instance == null) {
                instance = new RolePermissions();
                saveToStorage(instance);
            }
        }
        return instance;
    }
    
    /**
     * Get default permissions for a role
     */
    public static BigInteger getDefaultPermissions(Role role) {
        RolePermissions rp = getInstance();
        String bitmapStr = rp.rolePermissions.get(role.name());
        if (bitmapStr == null || bitmapStr.isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(bitmapStr);
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Set default permissions for a role
     */
    public static void setDefaultPermissions(Role role, BigInteger permissions) {
        RolePermissions rp = getInstance();
        rp.rolePermissions.put(role.name(), permissions != null ? permissions.toString() : "0");
        saveToStorage(rp);
    }
    
    /**
     * Grant endpoint to a role
     */
    public static void grantEndpointToRole(Role role, String endpointName) {
        BigInteger bit = EndpointRegistry.getEndpointBit(endpointName);
        if (bit == null || bit.signum() == 0) {
            bit = EndpointRegistry.registerEndpoint(endpointName);
        }
        
        BigInteger current = getDefaultPermissions(role);
        setDefaultPermissions(role, current.or(bit));
    }
    
    /**
     * Revoke endpoint from a role
     */
    public static void revokeEndpointFromRole(Role role, String endpointName) {
        BigInteger bit = EndpointRegistry.getEndpointBit(endpointName);
        if (bit != null && bit.signum() > 0) {
            BigInteger current = getDefaultPermissions(role);
            setDefaultPermissions(role, current.andNot(bit));
        }
    }
    
    /**
     * Check if role has specific endpoint permission
     */
    public static boolean roleHasEndpoint(Role role, String endpointName) {
        BigInteger bit = EndpointRegistry.getEndpointBit(endpointName);
        if (bit == null || bit.signum() == 0) {
            return false;
        }
        BigInteger current = getDefaultPermissions(role);
        return current.and(bit).signum() > 0;
    }
    
    /**
     * Get all role permissions as map
     */
    public static Map<Role, BigInteger> getAllRolePermissions() {
        Map<Role, BigInteger> result = new HashMap<>();
        for (Role role : Role.values()) {
            result.put(role, getDefaultPermissions(role));
        }
        return result;
    }
    
    /**
     * Clear all permissions for a role (reset to default)
     */
    public static void clearRolePermissions(Role role) {
        setDefaultPermissions(role, BigInteger.ZERO);
    }
    
    // ========== Persistence ==========
    
    private static RolePermissions loadFromStorage() {
        try {
            for (RolePermissions rp : StorageManager.getAll(RolePermissions.class)) {
                if (SINGLETON_KEY.equals(rp.getName())) {
                    return rp;
                }
            }
        } catch (Exception e) {
            System.out.println("[RolePermissions] Load failed: " + e.getMessage());
        }
        return null;
    }
    
    private static void saveToStorage(RolePermissions rp) {
        try {
            var tc = StorageManager.createContainer();
            tc.addInsert(rp);
            StorageManager.store(tc);
        } catch (Exception e) {
            System.out.println("[RolePermissions] Save failed: " + e.getMessage());
        }
    }
    
    // ========== Getters/Setters ==========
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Map<String, String> getRolePermissions() { 
        return new HashMap<>(rolePermissions); 
    }
    
    public void setRolePermissions(Map<String, String> rolePermissions) {
        this.rolePermissions = new HashMap<>(rolePermissions);
    }
}