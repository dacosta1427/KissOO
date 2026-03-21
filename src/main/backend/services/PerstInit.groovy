import oodb.PerstStorageManager
import mycompany.domain.PerstUser

/**
 * Initialize Perst users.
 * 
 * Run this once to create the default admin user if none exists.
 * 
 * HTTP Request:
 * {
 *   "_class": "services.PerstInit",
 *   "_method": "init",
 *   "_uuid": "session-uuid"
 * }
 * 
 * Or call directly from code:
 *   groovy: new services.PerstInit().init(null, null, null, null)
 */
class PerstInit {

    /**
     * Initialize default user.
     */
    static void init(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        
        if (!PerstStorageManager.isAvailable()) {
            if (outjson) outjson.put("error", "Perst is not available")
            return
        }
        
        // Check if any users exist
        def users = PerstStorageManager.getAll(PerstUser.class)
        if (users) {
            if (outjson) {
                outjson.put("status", "skipped")
                outjson.put("message", "Users already exist")
                outjson.put("count", users.size())
            }
            return
        }
        
        // Create default admin user
        try {
            PerstStorageManager.beginTransaction()
            
            def admin = new PerstUser("admin", "admin", 1)
            admin.setEmail("admin@localhost")
            admin.setActive(true)
            
            PerstStorageManager.save(admin)
            PerstStorageManager.commitTransaction()
            
            if (outjson) {
                outjson.put("status", "created")
                outjson.put("username", "admin")
                outjson.put("message", "Default admin user created. CHANGE PASSWORD IMMEDIATELY!")
            }
            println "Default admin user created. CHANGE PASSWORD IMMEDIATELY!"
            
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction()
            if (outjson) outjson.put("error", "Failed: " + e.message)
        }
    }
}
