package koo.core.actor;

import mycompany.CRUD;
import koo.security.EndpointMethod;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.HashSet;
import java.util.Set;

/**
 * Group - A collection of Actors that share permissions.
 * </br>
 * Actors can belong to Groups, and Groups have EndpointMethod permissions.
 * This is similar to UNIX group permissions.
 * </br>
 * IMPORTANT: Extends CVersion for automatic versioning support.
 * </br>
 * Usage:</br>
 *   Group admins = new Group();</br>
 *   admins.setName("admins");</br>
 *   admins.grant(ActorService.GET_ACTOR);   // Type-safe endpoint</br>
 *   admins.grant(AActor.class, CRUD.CREATE);// Type-safe CRUD</br>
 *   
 *   actor.getAgreement().addGroup(admins);
 * </br>
 * Indexing:</br>
 * - @Indexable: fields for b-tree indexing (use db.find())</br>
 * - @FullTextSearchable: fields for Lucene full-text search (use db.fullTextSearch())</br>
 */
public class Group extends CVersion {
    
    @FullTextSearchable
    @Indexable(unique=true)
    private String name;
    
    private Set<EndpointMethod> methodPermissions;
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
    
    public Set<EndpointMethod> getMethodPermissions() { return methodPermissions; }
    
    // ========== CRUD Permissions (Type-Safe) ==========
    
    /**
     * Grant CRUD permission using Class (type-safe!)
     * 
     *   group.grant(AActor.class, CRUD.CREATE);
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
