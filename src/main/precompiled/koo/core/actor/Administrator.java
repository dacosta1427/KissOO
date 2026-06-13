package koo.core.actor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Administrator - System|Business administrator that can manage the application.
 * 
 * Extends ANaturalActor (NATURAL by default), so automatically it has a PerstUser created by the AActor constructor (deactivated by default).
 * 
 * Two types of administrators:
 * - SUPER_ADMIN: Full system access
 * - ADMIN: Content/business management access
 * 
 * Super administrators CANNOT be a DOMAIN entity.
 * Business administrators CAN be a DOMAIN at the same time.
 * 
 * The number of each type allowed is configured in application.ini:
 * - MaxSuperAdmins
 * - MaxAdmins
 */
@Getter @Setter
@NoArgsConstructor
public class Administrator extends ANaturalActor {
    
    private Role adminRole;
    private boolean canClean;
    
    public Administrator(String name, String email, Role role) {
        super(name, new Agreement(role), email);
        this.adminRole = role;
        
        // Set the Agreement role based on administrator type
        if (role == Role.SUPER_ADMIN) {
            getAgreement().setRole(Role.SUPER_ADMIN);
        } else {
            getAgreement().setRole(Role.ADMIN);
        }
    }
    
    public boolean isSuperAdmin() {
        return adminRole == Role.SUPER_ADMIN;
    }
    
    public boolean isBusinessAdmin() {
        return adminRole == Role.ADMIN;
    }
    
    @Override
    public String toString() {
        return "Administrator{" +
                "name='" + getName() + '\'' +
                ", adminRole=" + adminRole +
                ", active=" + isActive() +
                '}';
    }
}