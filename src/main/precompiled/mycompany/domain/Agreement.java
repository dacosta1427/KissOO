package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
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
 * Authorization is checked in order:
 * 1. CRUD permissions: grant(Class, action) - simple create/read/update/delete
 * 2. EndpointMethod permissions: grant(EndpointMethod) - specific method call
 * 3. Group permissions: addGroup(Group) - via groups the actor belongs to
 * 
 * Everything is denied by default - explicit grant required.
 * 
 * Usage:
 *   // Type-safe CRUD (no typos!)
 *   agreement.grant(Actor.class, CRUD.CREATE);
 *   agreement.grant(Actor.class, CRUD.READ);
 *   
 *   // Type-safe endpoint
 *   agreement.grant(ActorService.GET_ACTOR);
 *   
 *   // Via group
 *   agreement.addGroup(admins);
 * 
 * Indexing:
 * - @Indexable: fields for b-tree indexing (use db.find())
 * - @FullTextSearchable: fields for Lucene full-text search (use db.fullTextSearch())
 */
public class Agreement extends CVersion {
    
    @FullTextSearchable
    @Indexable
    private String role;
    
    private Set<String> crudPermissions;
    private Set<EndpointMethod> methodPermissions;
    private Set<Group> groups;
    private long validFrom;
    private Long validTo;
    
    @Indexable
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
    
    public Set<EndpointMethod> getMethodPermissions() { return methodPermissions; }
    
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
     * Checks in order: CRUD → EndpointMethod → Groups
     * Everything is denied by default.
     */
    public boolean grants(EndpointMethod endpoint, Class<?> resource, String action) {
        if (!isValid()) return false;
        
        // 1. Check CRUD permission
        if (crudPermissions.contains("*") ||
            crudPermissions.contains(resource.getName() + ":*") ||
            crudPermissions.contains(resource.getName() + ":" + action)) {
            return true;
        }
        
        // 2. Check EndpointMethod permission
        if (endpoint != null && methodPermissions.contains(endpoint)) {
            return true;
        }
        
        // 3. Check via Groups
        for (Group group : groups) {
            if (group.canExecute(endpoint) || 
                group.hasCrudPermission(resource.getName(), action)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check CRUD permission only (no endpoint)
     */
    public boolean grants(Class<?> resource, String action) {
        return grants(null, resource, action);
    }
    
    // ========== Private Helper ==========
    
    private static String getConstantValue(Class<?> constantClass) {
        return constantClass.getSimpleName().toLowerCase();
    }
}
