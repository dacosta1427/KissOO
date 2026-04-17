package koo.oodb.core.actor;

import lombok.Getter;
import lombok.Setter;

/**
 * Administrator - System administrator that can manage the application.
 * 
 * Extends ANaturalActor (NATURAL by default), so automatically has a PerstUser
 * created by the AActor constructor (deactivated by default).
 * 
 * Two types of administrators:
 * - SUPER_ADMIN: Full system access
 * - ADMIN: Content management access
 * 
 * Administrators CANNOT own houses (cannot be Owner).
 * Administrators CAN be cleaners at the same time.
 * 
 * The number of each type allowed is configured in application.ini:
 * - MaxSuperAdmins
 * - MaxAdmins
 */
@Getter @Setter
public class Administrator extends ANaturalActor {
    
    private AdministratorRole adminRole;
    private boolean canClean;
    
    public Administrator(String name, String email, AdministratorRole role) {
        super(name, new Agreement(), email);
        this.adminRole = role;
        
        // Set the Agreement role based on administrator type
        if (role == AdministratorRole.SUPER_ADMIN) {
            getAgreement().setRole(Role.SUPER_ADMIN);
        } else {
            getAgreement().setRole(Role.ADMIN);
        }
    }
    
    public boolean isSuperAdmin() {
        return adminRole == AdministratorRole.SUPER_ADMIN;
    }
    
    public boolean isContentAdmin() {
        return adminRole == AdministratorRole.ADMIN;
    }
    
    @Override
    public String toString() {
        return "Administrator{" +
                "name='" + getName() + '\'' +
                ", adminRole=" + adminRole +
                ", canClean=" + canClean +
                ", active=" + isActive() +
                '}';
    }
}