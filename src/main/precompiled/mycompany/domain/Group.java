package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import java.util.HashSet;
import java.util.Set;

/**
 * Group - A collection of Actors that share permissions.
 * 
 * Actors can belong to Groups, and Groups have permissions.
 * This is similar to UNIX group permissions.
 * 
 * IMPORTANT: Extends CVersion for automatic versioning support.
 * 
 * Usage:
 *   Group admins = new Group();
 *   admins.setName("admins");
 *   admins.grant(Actor.class, CRUD.CREATE);     // Type-safe CRUD
 *   admins.grantMethod("services.ActorService.getActor");  // Method permission
 *   
 *   actor.getAgreement().addGroup(admins);
 */
public class Group extends CVersion {
    
    private String name;
    private Set<String> methodPermissions;
    private Set<String> crudPermissions;
    
    public Group() {
        this.methodPermissions = new HashSet<>();
        this.crudPermissions = new HashSet<>();
    }
    
    public Group(String name) {
        this();
        this.name = name;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
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
     * Check if this group can execute a method
     */
    public boolean canExecuteMethod(String methodName) {
        return methodPermissions.contains(methodName);
    }
    
    public Set<String> getMethodPermissions() { return methodPermissions; }
    
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
    
    public Set<String> getCrudPermissions() { return crudPermissions; }
    
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
        return name != null && name.equals(group.name);
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
