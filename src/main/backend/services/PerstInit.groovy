package services


import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import koo.oodb.core.StorageManager
import koo.oodb.core.user.PerstUser

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

    void init(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        
        if (!StorageManager.isAvailable()) {
            if (outjson) outjson.put("error", "Perst is not available")
            return
        }
        
        def users = StorageManager.getAll(PerstUser.class)
        if (users && users.size() > 0) {
            if (outjson) {
                outjson.put("status", "skipped")
                outjson.put("message", "Users already exist")
                outjson.put("count", users.size())
            }
            return
        }
        
        try {
            def admin = new PerstUser("admin", "admin", 1)
            admin.setEmail("admin@localhost")
            admin.setActive(true)
            admin.setEmailVerified(true)
            
            def tc = StorageManager.createContainer()
            tc.addInsert(admin)
            StorageManager.store(tc)
            
            if (outjson) {
                outjson.put("status", "created")
                outjson.put("username", "admin")
                outjson.put("message", "Default admin user created. CHANGE PASSWORD IMMEDIATELY!")
            }
            println "Default admin user created. CHANGE PASSWORD IMMEDIATELY!"
            
        } catch (Exception e) {
            if (outjson) outjson.put("error", "Failed: " + e.message)
        }
    }
}
