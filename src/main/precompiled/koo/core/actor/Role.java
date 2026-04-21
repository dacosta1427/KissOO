package koo.core.actor;

/**
 * Role - Defines the role of an AActor in the system.
 * 
 * Roles are assigned to AActor via their Agreement.
 * Each role determines what permissions the actor has.
 * 
 * Note: Administrator uses its own AdministratorRole enum for admin type distinction
 * (SUPER_ADMIN vs ADMIN), stored in the Agreement as Role.SUPER_ADMIN or Role.ADMIN.
 */
public enum Role {
    SUPER_ADMIN,  // Full system access - reserved for Administrator with AdministratorRole.SUPER_ADMIN
    ADMIN,        // Content management - reserved for Administrator with AdministratorRole.ADMIN
    OWNER,        // House owner - cannot be combined with Administrator
    CLEANER,      // Cleaning staff - can be combined with Administrator
    MEMBER;       // Basic member - default role for new actors
    
    /**
     * Check if this role has admin-level access
     */
    public boolean isAdmin() {
        return this == SUPER_ADMIN || this == ADMIN;
    }
    
    /**
     * Check if this role has system-level (super) admin access
     */
    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }
}