package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import java.util.HashSet;
import java.util.Set;

/**
 * Agreement - Defines what an Actor is allowed to do in the system.
 * 
 * Every Actor MUST have an Agreement - without it, the Actor cannot
 * perform any operations in the system.
 * 
 * IMPORTANT: Extends CVersion for automatic versioning support.
 * 
 * Authorization is checked via CRUD permissions:
 * - grant(Class, action) for type-safe CRUD permissions
 * 
 * Everything is denied by default - explicit grant required.
 * 
 * Usage:
 *   // Type-safe CRUD (no typos!)
 *   agreement.grant(Actor.class, CRUD.CREATE);
 *   agreement.grant(Actor.class, CRUD.READ);
 *   
 *   // Via group
 *   agreement.addGroup(admins);
 */
public class Agreement extends CVersion {
    
    private String role;
    private Set<String> crudPermissions;
    private Set<String> methodPermissions;  // Stored as strings for flexibility
    private Set<Group> groups;
    private long validFrom;
    private Long validTo;
    private boolean active = true;
    
    public Agreement() {
        this.validFrom = System.currentTimeMillis();
        this.crudPermissions = new HashSet<>();
        this.methodPermissions = new HashSet<>();
        this.groups = new HashSet<>();
    }
    
    public Agreement(String role) {
        this();
        this.role = role;
    }
    
    // ========== CRUD Permissions (Type-Safe) ==========
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    /**
     * Grant CRUD permission using Class (type-safe!)
     * 
     *   agreement.grant(Actor.class, CRUD.CREATE);
     */
    public void grant(Class<?> resource, String action) {
        crudPermissions.add(resource.getName() + ":" + action);
    }
    
    /**
     * Grant CRUD permission using Class and CRUD constant (fully type-safe!)
     * 
     *   agreement.grant(Actor.class, CRUD.CREATE);
     */
    public void grant(Class<?> resource, Class<?> actionConstant) {
        grant(resource, getConstantValue(actionConstant));
    }
    
    /**
     * Grant CRUD permission
     * 
     * @deprecated Use grant(Class, String) instead for type safety
     */
    @Deprecated
    public void grant(String resource, String action) {
        crudPermissions.add(resource + ":" + action);
    }
    
    /**
     * Grant CRUD permission with boolean
     */
    public void grant(Class<?> resource, String action, boolean grant) {
        String permission = resource.getName() + ":" + action;
        if (grant) {
            crudPermissions.add(permission);
        } else {
            crudPermissions.remove(permission);
        }
    }
    
    /**
     * Grant all CRUD permissions for a resource
     */
    public void grantAll(Class<?> resource) {
        grant(resource, CRUD.CREATE);
        grant(resource, CRUD.READ);
        grant(resource, CRUD.UPDATE);
        grant(resource, CRUD.DELETE);
    }
    
    /**
     * Revoke CRUD permission
     */
    public void revoke(Class<?> resource, String action) {
        crudPermissions.remove(resource.getName() + ":" + action);
    }
    
    /**
     * Revoke CRUD permission (string version)
     */
    public void revoke(String resource, String action) {
        crudPermissions.remove(resource + ":" + action);
    }
    
    /**
     * Check CRUD permission (type-safe)
     */
    public boolean hasCrudPermission(Class<?> resource, String action) {
        return crudPermissions.contains(resource.getName() + ":" + action);
    }
    
    /**
     * Check CRUD permission (string version)
     */
    public boolean hasCrudPermission(String resource, String action) {
        return crudPermissions.contains(resource + ":" + action);
    }
    
    public Set<String> getCrudPermissions() { return crudPermissions; }
    
    // ========== EndpointMethod Permissions (as Strings) ==========
    
    /**
     * Grant permission to execute a method (stored as string)
     */
    public void grantMethod(String methodName) {
        methodPermissions.add(methodName);
    }
    
    /**
     * Revoke method permission
     */
    public void revokeMethod(String methodName) {
        methodPermissions.remove(methodName);
    }
    
    /**
     * Check if can execute this method
     */
    public boolean canExecuteMethod(String methodName) {
        return methodPermissions.contains(methodName);
    }
    
    public Set<String> getMethodPermissions() { return methodPermissions; }
    
    // ========== Group Permissions ==========
    
    /**
     * Add a group to this agreement
     */
    public void addGroup(Group group) {
        groups.add(group);
    }
    
    /**
     * Remove a group
     */
    public void removeGroup(Group group) {
        groups.remove(group);
    }
    
    /**
     * Check if actor belongs to a group
     */
    public boolean hasGroup(String groupName) {
        return groups.stream().anyMatch(g -> g.getName() != null && g.getName().equals(groupName));
    }
    
    public Set<Group> getGroups() { return groups; }
    
    // ========== Validity ==========
    
    public long getValidFrom() { return validFrom; }
    public void setValidFrom(long validFrom) { this.validFrom = validFrom; }
    
    public Long getValidTo() { return validTo; }
    public void setValidTo(Long validTo) { this.validTo = validTo; }
    
    public boolean isValid() {
        long now = System.currentTimeMillis();
        if (!active) return false;
        if (validFrom > now) return false;
        if (validTo != null && validTo < now) return false;
        return true;
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    // ========== Comprehensive Permission Check ==========
    
    /**
     * Check if this agreement grants permission for an action.
     * Checks in order: CRUD → Method → Groups
     * Everything is denied by default.
     */
    public boolean grants(Class<?> resource, String action) {
        if (!isValid()) return false;
        
        // 1. Check CRUD permission
        if (crudPermissions.contains("*") ||
            crudPermissions.contains(resource.getName() + ":*") ||
            crudPermissions.contains(resource.getName() + ":" + action)) {
            return true;
        }
        
        // 2. Check via Groups
        for (Group group : groups) {
            if (group.hasCrudPermission(resource.getName(), action)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check method permission
     */
    public boolean grantsMethod(String methodName) {
        if (!isValid()) return false;
        
        if (methodPermissions.contains("*") || methodPermissions.contains(methodName)) {
            return true;
        }
        
        for (Group group : groups) {
            if (group.canExecuteMethod(methodName)) {
                return true;
            }
        }
        
        return false;
    }
    
    // ========== Private Helper ==========
    
    private static String getConstantValue(Class<?> constantClass) {
        return constantClass.getSimpleName().toLowerCase();
    }
}
