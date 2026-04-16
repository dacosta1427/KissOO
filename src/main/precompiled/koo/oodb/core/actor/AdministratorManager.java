package koo.oodb.core.actor;

import koo.oodb.config.AppConfig;
import koo.oodb.core.StorageManager;
import org.garret.perst.continuous.TransactionContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AdministratorManager - Manages Administrator entities.
 * 
 * Provides CRUD operations and enforces limits on the number of
 * Super Administrators and Administrators allowed in the system.
 * 
 * Limits are configured in application.ini:
 * - MaxSuperAdmins
 * - MaxAdmins
 */
public class AdministratorManager {
    
    private static final String KEY_MAX_SUPER_ADMINS = "MaxSuperAdmins";
    private static final String KEY_MAX_ADMINS = "MaxAdmins";
    
    /**
     * Get all administrators
     */
    public static Collection<Administrator> getAll() {
        return StorageManager.getAll(Administrator.class);
    }
    
    /**
     * Get administrator by OID
     */
    public static Administrator getByOid(long oid) {
        return StorageManager.getByOid(Administrator.class, oid);
    }
    
    /**
     * Get all Super Administrators
     */
    public static List<Administrator> getSuperAdmins() {
        List<Administrator> result = new ArrayList<>();
        for (Administrator admin : getAll()) {
            if (admin.isSuperAdmin()) {
                result.add(admin);
            }
        }
        return result;
    }
    
    /**
     * Get all Content Administrators
     */
    public static List<Administrator> getContentAdmins() {
        List<Administrator> result = new ArrayList<>();
        for (Administrator admin : getAll()) {
            if (admin.isContentAdmin()) {
                result.add(admin);
            }
        }
        return result;
    }
    
    /**
     * Get count of Super Administrators
     */
    public static int getSuperAdminCount() {
        return getSuperAdmins().size();
    }
    
    /**
     * Get count of Content Administrators
     */
    public static int getAdminCount() {
        return getContentAdmins().size();
    }
    
    /**
     * Check if we can create another Super Admin
     */
    public static boolean canCreateSuperAdmin() {
        int max = getMaxSuperAdmins();
        return getSuperAdminCount() < max;
    }
    
    /**
     * Check if we can create another Admin
     */
    public static boolean canCreateAdmin() {
        int max = getMaxAdmins();
        return getAdminCount() < max;
    }
    
    /**
     * Get maximum Super Admins from config
     */
    public static int getMaxSuperAdmins() {
        return AppConfig.getInt(KEY_MAX_SUPER_ADMINS, 2);
    }
    
    /**
     * Get maximum Admins from config
     */
    public static int getMaxAdmins() {
        return AppConfig.getInt(KEY_MAX_ADMINS, 5);
    }
    
    /**
     * Create a new Administrator, respecting limits
     * 
     * @return Administrator if created, null if limit reached
     */
    public static Administrator create(String name, String email, AdministratorRole role) {
        if (role == AdministratorRole.SUPER_ADMIN && !canCreateSuperAdmin()) {
            return null; // Limit reached
        }
        if (role == AdministratorRole.ADMIN && !canCreateAdmin()) {
            return null; // Limit reached
        }
        
        Administrator admin = new Administrator(name, email, role);
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addInsert(admin);
        tc.addInsert(admin.getPerstUser());
        
        if (StorageManager.store(tc)) {
            return admin;
        }
        return null;
    }
    
    /**
     * Update an existing Administrator
     */
    public static boolean update(Administrator admin) {
        TransactionContainer tc = StorageManager.createContainer();
        tc.addUpdate(admin);
        return StorageManager.store(tc);
    }
    
    /**
     * Delete an Administrator
     */
    public static boolean delete(Administrator admin) {
        TransactionContainer tc = StorageManager.createContainer();
        tc.addDelete(admin);
        tc.addDelete(admin.getPerstUser());
        return StorageManager.store(tc);
    }
}