package koo.oodb.core.actor;

import mycompany.CRUD;
import koo.security.EndpointMethod;
import koo.security.EndpointRegistry;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Agreement - Defines what an AActor is allowed to do in the system.
 * 
 * Every AActor MUST have an Agreement - without it, the AActor cannot
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
 *   agreement.grant(AActor.class, CRUD.CREATE);
 *   agreement.grant(AActor.class, CRUD.READ);
 *   
 *   // Bitmap-based endpoint permission
 *   agreement.grantEndpoint("services.CleaningService.getCleaners");
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
    private Role role;
    
    private Set<String> crudPermissions;
    private Set<Group> groups;
    
    // Bitmap-based endpoint permissions (replaces Set<EndpointMethod>)
    // Each bit represents permission to execute a specific endpoint
    private String endpointPermissions;  // Stored as String for Perst
    
    private long validFrom;
    private Long validTo;
    
    @Indexable
    private boolean active = true;
    
    public Agreement() {
        this.validFrom = System.currentTimeMillis();
        this.crudPermissions = new HashSet<>();
        this.groups = new HashSet<>();
        this.role = Role.MEMBER; // Default role
        this.endpointPermissions = "0";
    }
    
    public Agreement(Role role) {
        this();
        this.role = role;
    }
    
    // For backward compatibility - convert string to Role
    public Agreement(String role) {
        this();
        this.role = Role.valueOf(role.toUpperCase());
    }
    
    // ========== Role ==========
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    // For backward compatibility - convert string to Role
    public void setRole(String role) { 
        this.role = Role.valueOf(role.toUpperCase()); 
    }
    
    // ========== CRUD Permissions (Type-Safe) ==========
    
    /**
     * Grant CRUD permission using Class (type-safe!)
     * 
     *   agreement.grant(AActor.class, CRUD.CREATE);
     */
    public void grant(Class<?> resource, String action) {
        crudPermissions.add(resource.getName() + ":" + action);
    }
    
    /**
     * Grant CRUD permission using Class and CRUD constant (fully type-safe!)
     * 
     *   agreement.grant(AActor.class, CRUD.CREATE);
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
    
    // ========== Endpoint Permissions (Bitmap-based) ==========
    
    /**
     * Get endpoint permissions as BigInteger
     */
    public BigInteger getEndpointPermissions() {
        if (endpointPermissions == null || endpointPermissions.isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(endpointPermissions);
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Set endpoint permissions from BigInteger
     */
    public void setEndpointPermissions(BigInteger permissions) {
        this.endpointPermissions = permissions != null ? permissions.toString() : "0";
    }
    
    /**
     * Set endpoint permissions from String (for Perst)
     */
    public void setEndpointPermissions(String permissions) {
        this.endpointPermissions = permissions != null ? permissions : "0";
    }
    
    public String getEndpointPermissionsString() { return endpointPermissions; }
    
    /**
     * Grant permission to execute an endpoint by name
     * 
     *   agreement.grantEndpoint("services.CleaningService.getCleaners");
     */
    public void grantEndpoint(String endpointName) {
        BigInteger bit = EndpointRegistry.getEndpointBit(endpointName);
        if (bit == null || bit.signum() == 0) {
            // Register the endpoint first
            bit = EndpointRegistry.registerEndpoint(endpointName);
        }
        
        BigInteger current = getEndpointPermissions();
        setEndpointPermissions(current.or(bit));
    }
    
    /**
     * Grant permission to multiple endpoints
     */
    public void grantEndpoints(String... endpointNames) {
        for (String name : endpointNames) {
            grantEndpoint(name);
        }
    }
    
    /**
     * Revoke permission to execute an endpoint by name
     */
    public void revokeEndpoint(String endpointName) {
        BigInteger bit = EndpointRegistry.getEndpointBit(endpointName);
        if (bit != null && bit.signum() > 0) {
            BigInteger current = getEndpointPermissions();
            BigInteger negated = current.andNot(bit);
            setEndpointPermissions(negated);
        }
    }
    
    /**
     * Check if can execute this endpoint by name
     */
    public boolean hasEndpointPermission(String endpointName) {
        BigInteger bit = EndpointRegistry.getEndpointBit(endpointName);
        if (bit == null || bit.signum() == 0) {
            return false;
        }
        BigInteger current = getEndpointPermissions();
        return current.and(bit).signum() > 0;
    }
    
    /**
     * Check if can execute endpoint (compatibility method)
     * Uses the endpoint's name to check bitmap
     */
    public boolean canExecute(EndpointMethod endpoint) {
        if (endpoint == null) return false;
        return hasEndpointPermission(endpoint.getName());
    }
    
    /**
     * Check if can execute endpoint by string name
     */
    public boolean canExecute(String endpointName) {
        return hasEndpointPermission(endpointName);
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
     * Checks in order: CRUD → Endpoint Bitmap → Groups
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
        
        // 2. Check endpoint permission (bitmap)
        if (endpoint != null && canExecute(endpoint)) {
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