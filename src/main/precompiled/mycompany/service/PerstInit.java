package mycompany.service;

import org.kissweb.json.JSONObject;
import mycompany.database.PerstUserManager;
import mycompany.domain.PerstUser;
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
            
            // Create admin user
            PerstUser admin = new PerstUser("admin", "admin", 1);
            admin.setEmail("admin@localhost");
            admin.setActive(true);
            admin.setEmailVerified(true);  // Allow login without email verification
            PerstUserManager.create(admin);
            
            result.put("message", "Admin user created successfully");
            result.put("username", "admin");
            result.put("password", "admin");
            logger.info("Admin user created successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing Perst", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
