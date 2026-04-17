package koo.oodb.core.actor;

/**
 * AdministratorRole - Defines the type of Administrator.
 * 
 * SUPER_ADMIN: Full system access - can manage all aspects of the application
 * ADMIN: Content management - can manage business operations but not system settings
 */
public enum AdministratorRole {
    SUPER_ADMIN,
    ADMIN
}