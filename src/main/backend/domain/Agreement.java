package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Agreement - Defines what an Actor is allowed to do in the system.
 * 
 * Every Actor MUST have an Agreement - without it, the Actor cannot
 * perform any operations in the system.
 * 
 * Authorization is checked in order:
 * 1. CRUD permissions: grant(resource, action) - simple create/read/update/delete
 * 2. EndpointMethod permissions: grant(EndpointMethod) - specific method call
 * 3. Group permissions: addGroup(Group) - via groups the actor belongs to
 * 
 * Everything is denied by default - explicit grant required.
 */
public class Agreement {
    
    private String role;  // ADMIN, MANAGER, USER, GUEST, etc.
    private Set<String> crudPermissions;  // e.g., "Actor:create", "Actor:read"
    private Set<EndpointMethod> methodPermissions;  // Type-safe endpoint permissions
    private Set<Group> groups;  // Groups this actor belongs to
    private long validFrom;
    private Long validTo;  // null = infinite
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
    
    // ========== CRUD Permissions ==========
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Grant CRUD permission (e.g., "Actor:create")
     */
    public void grant(String resource, String action) {
        crudPermissions.add(resource + ":" + action);
    }
    
    /**
     * Grant CRUD permission using constants
     */
    public void grant(String resource, String action, boolean grant) {
        String permission = resource + ":" + action;
        if (grant) {
            crudPermissions.add(permission);
        } else {
            crudPermissions.remove(permission);
        }
    }
    
    /**
     * Revoke CRUD permission
     */
    public void revoke(String resource, String action) {
        crudPermissions.remove(resource + ":" + action);
    }
    
    /**
     * Check CRUD permission
     */
    public boolean hasCrudPermission(String resource, String action) {
        return crudPermissions.contains(resource + ":" + action);
    }
    
    public Set<String> getCrudPermissions() {
        return crudPermissions;
    }
    
    // ========== EndpointMethod Permissions ==========
    
    /**
     * Grant permission to execute an endpoint (type-safe!)
     */
    public void grant(EndpointMethod endpoint) {
        methodPermissions.add(endpoint);
    }
    
    /**
     * Revoke endpoint permission
     */
    public void revoke(EndpointMethod endpoint) {
        methodPermissions.remove(endpoint);
    }
    
    /**
     * Check if can execute this endpoint
     */
    public boolean canExecute(EndpointMethod endpoint) {
        return methodPermissions.contains(endpoint);
    }
    
    public Set<EndpointMethod> getMethodPermissions() {
        return methodPermissions;
    }
    
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
        return groups.stream().anyMatch(g -> g.getName().equals(groupName));
    }
    
    public Set<Group> getGroups() {
        return groups;
    }
    
    // ========== Validity ==========
    
    public long getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(long validFrom) {
        this.validFrom = validFrom;
    }
    
    public Long getValidTo() {
        return validTo;
    }
    
    public void setValidTo(Long validTo) {
        this.validTo = validTo;
    }
    
    public boolean isValid() {
        long now = System.currentTimeMillis();
        if (!active) return false;
        if (validFrom > now) return false;
        if (validTo != null && validTo < now) return false;
        return true;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // ========== Comprehensive Permission Check ==========
    
    /**
     * Check if this agreement grants permission for an action.
     * Checks in order: CRUD → EndpointMethod → Groups
     * Everything is denied by default.
     * 
     * @param endpoint The endpoint being called (can be null for CRUD-only check)
     * @param resource The resource being accessed (e.g., "Actor")
     * @param action The action (create, read, update, delete)
     * @return true if authorized
     */
    public boolean grants(EndpointMethod endpoint, String resource, String action) {
        // Must be valid
        if (!isValid()) return false;
        
        // 1. Check CRUD permission
        if (crudPermissions.contains("*") ||
            crudPermissions.contains(resource + ":*") ||
            crudPermissions.contains(resource + ":" + action)) {
            return true;
        }
        
        // 2. Check EndpointMethod permission
        if (endpoint != null && methodPermissions.contains(endpoint)) {
            return true;
        }
        
        // 3. Check via Groups
        for (Group group : groups) {
            if (group.canExecute(endpoint) || 
                group.hasCrudPermission(resource, action)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check CRUD permission only (no endpoint)
     */
    public boolean grants(String resource, String action) {
        return grants(null, resource, action);
    }
}
