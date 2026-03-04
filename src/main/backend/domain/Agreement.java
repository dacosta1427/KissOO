package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Agreement - Defines what an Actor is allowed to do in the system.
 * 
 * Every Actor MUST have an Agreement - without it, the Actor cannot
 * perform any operations in the system.
 * 
 * Authorization is checked at two levels:
 * 1. Role-based: General permissions based on role
 * 2. Endpoint-based: Specific REST endpoint permissions
 */
public class Agreement {
    
    private String role;  // ADMIN, MANAGER, USER, GUEST, etc.
    private Set<String> rolePermissions;  // e.g., "Actor:read", "Actor:write"
    private Set<String> endpointPermissions;  // e.g., "services.ActorService.getActor"
    private long validFrom;
    private Long validTo;  // null = infinite
    private boolean active = true;
    
    public Agreement() {
        this.validFrom = System.currentTimeMillis();
        this.rolePermissions = new HashSet<>();
        this.endpointPermissions = new HashSet<>();
    }
    
    public Agreement(String role) {
        this();
        this.role = role;
    }
    
    // ========== Role-Based Permissions ==========
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public void addRolePermission(String permission) {
        rolePermissions.add(permission);
    }
    
    public void removeRolePermission(String permission) {
        rolePermissions.remove(permission);
    }
    
    public boolean hasRolePermission(String permission) {
        return rolePermissions.contains(permission);
    }
    
    public boolean hasRolePermission(String resource, String action) {
        return rolePermissions.contains(resource + ":" + action);
    }
    
    public Set<String> getRolePermissions() {
        return rolePermissions;
    }
    
    // ========== Endpoint-Based Permissions ==========
    
    public void addEndpointPermission(String endpoint) {
        endpointPermissions.add(endpoint);
    }
    
    public void removeEndpointPermission(String endpoint) {
        endpointPermissions.remove(endpoint);
    }
    
    public boolean hasEndpointPermission(String endpoint) {
        return endpointPermissions.contains(endpoint);
    }
    
    public Set<String> getEndpointPermissions() {
        return endpointPermissions;
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
    
    // ========== Utility ==========
    
    /**
     * Check if this agreement grants permission for an action on a resource.
     * Uses wildcard matching: "*" means all.
     */
    public boolean grants(String resource, String action, String endpoint) {
        // Must be valid
        if (!isValid()) return false;
        
        // Check role permissions (wildcard support)
        if (rolePermissions.contains("*") || 
            rolePermissions.contains(resource + ":*") ||
            rolePermissions.contains(resource + ":" + action)) {
            return true;
        }
        
        // Check endpoint permissions
        if (endpoint != null && endpointPermissions.contains(endpoint)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check using a fully qualified method reference.
     * Format: "services.ActorService.createActor"
     */
    public boolean grants(String resource, String action) {
        return grants(resource, action, null);
    }
}
