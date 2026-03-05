package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Group - A collection of Actors that share permissions.
 * 
 * Actors can belong to Groups, and Groups have EndpointMethod permissions.
 * This is similar to UNIX group permissions.
 * 
 * Usage:
 *   Group admins = new Group("admins");
 *   admins.grant(ActorService.GET_ACTOR);
 *   admins.grant(ActorService.CREATE_ACTOR);
 *   
 *   actor.getAgreement().addGroup(admins);
 */
public class Group {
    
    private final String name;
    private final Set<EndpointMethod> methodPermissions;
    private final Set<String> crudPermissions;  // "Actor:create", etc.
    
    public Group(String name) {
        this.name = name;
        this.methodPermissions = new HashSet<>();
        this.crudPermissions = new HashSet<>();
    }
    
    public String getName() {
        return name;
    }
    
    // ========== EndpointMethod Permissions ==========
    
    /**
     * Grant permission to execute an endpoint
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
     * Check if this group can execute an endpoint
     */
    public boolean canExecute(EndpointMethod endpoint) {
        return methodPermissions.contains(endpoint);
    }
    
    public Set<EndpointMethod> getMethodPermissions() {
        return methodPermissions;
    }
    
    // ========== CRUD Permissions ==========
    
    /**
     * Grant CRUD permission (e.g., "Actor:create")
     */
    public void grantCrud(String resource, String action) {
        crudPermissions.add(resource + ":" + action);
    }
    
    /**
     * Grant CRUD permission using constants
     */
    public void grantCrud(String resource, String action, boolean grant) {
        String permission = resource + ":" + action;
        if (grant) {
            crudPermissions.add(permission);
        } else {
            crudPermissions.remove(permission);
        }
    }
    
    /**
     * Check if this group has CRUD permission
     */
    public boolean hasCrudPermission(String resource, String action) {
        return crudPermissions.contains(resource + ":" + action);
    }
    
    public Set<String> getCrudPermissions() {
        return crudPermissions;
    }
    
    @Override
    public String toString() {
        return "Group{" + name + ", methods=" + methodPermissions.size() + ", crud=" + crudPermissions + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group group = (Group) obj;
        return name.equals(group.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
