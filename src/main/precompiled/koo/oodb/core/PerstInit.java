package koo.oodb.core;

import mycompany.actor.owner.Owner;
import org.kissweb.json.JSONObject;
import koo.oodb.core.user.PerstUserManager;
import koo.oodb.core.user.PerstUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PerstInit - Initialize Perst database with default admin user
 */
public class PerstInit {
    private static final Logger logger = LogManager.getLogger(PerstInit.class);
    
    public static JSONObject init() {
        JSONObject result = new JSONObject();
        
        try {
            // Check if admin user exists
            PerstUser existing = PerstUserManager.getByKey("admin");
            if (existing != null) {
                // Ensure emailVerified is true for existing admin
                if (!existing.isEmailVerified()) {
                    existing.setEmailVerified(true);
                    PerstUserManager.update(existing);
                    result.put("message", "Admin user updated with emailVerified=true");
                    logger.info("Admin user updated with emailVerified=true");
                } else {
                    result.put("message", "Admin user already exists");
                    logger.info("Admin user already exists");
                }
                return result;
            }
            
            // Create superAdmin AActor using Owner (a concrete NATURAL actor)
            Owner adminActor = new Owner("System Admin", "", "admin@localhost", true);
            adminActor.getAgreement().setRole("superAdmin");
            
            // Replace auto-created PerstUser with one using "admin" as username
            String username = "admin";
            String tempPassword = "admin";
            PerstUser adminUser = new PerstUser(username, tempPassword, adminActor);
            adminUser.setEmail("admin@localhost");
            adminUser.setActive(true);
            adminUser.setEmailVerified(true);
            // PerstUser already linked via ANaturalActor constructor
            
            // Store both together
            org.garret.perst.continuous.TransactionContainer tc = StorageManager.createContainer();
            tc.addInsert(adminActor);
            tc.addInsert(adminUser);
            StorageManager.store(tc);
            
            result.put("message", "Admin user created successfully");
            result.put("username", "admin");
            result.put("password", "admin");
            result.put("role", "superAdmin");
            logger.info("Admin user created successfully with role=superAdmin");
            
        } catch (Exception e) {
            logger.error("Error initializing Perst", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
