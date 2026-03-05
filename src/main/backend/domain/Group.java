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
 *   admins.grant(ActorService.GET_ACTOR);       // Type-safe endpoint
 *   admins.grant(Actor.class, CRUD.CREATE);     // Type-safe CRUD
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
     * Check if this group can execute an endpoint
     */
    public boolean canExecute(EndpointMethod endpoint) {
        return methodPermissions.contains(endpoint);
    }
    
    public Set<EndpointMethod> getMethodPermissions() {
        return methodPermissions;
    }
    
    // ========== CRUD Permissions (Type-Safe) ==========
    
    /**
     * Grant CRUD permission using Class (type-safe!)
     * 
     *   group.grant(Actor.class, CRUD.CREATE);
     */
    public void grant(Class<?> resource, String action) {
        crudPermissions.add(resource.getName() + ":" + action);
    }
    
    /**
     * Grant CRUD permission using Class and CRUD constant (fully type-safe!)
     */
    public void grant(Class<?> resource, Class<?> actionConstant) {
        grant(resource, getConstantValue(actionConstant));
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
     * Grant CRUD permission (legacy string version)
     */
    public void grantCrud(String resource, String action) {
        crudPermissions.add(resource + ":" + action);
    }
    
    /**
     * Grant CRUD permission with boolean
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
     * Check if this group has CRUD permission (type-safe)
     */
    public boolean hasCrudPermission(Class<?> resource, String action) {
        return crudPermissions.contains(resource.getName() + ":" + action);
    }
    
    /**
     * Check if this group has CRUD permission (string version)
     */
    public boolean hasCrudPermission(String resource, String action) {
        return crudPermissions.contains(resource + ":" + action);
    }
    
    public Set<String> getCrudPermissions() {
        return crudPermissions;
    }
    
    // ========== Private Helper ==========
    
    private static String getConstantValue(Class<?> constantClass) {
        return constantClass.getSimpleName().toLowerCase();
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
