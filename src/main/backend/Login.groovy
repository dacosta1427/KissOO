import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import mycompany.database.PerstUserManager
import mycompany.domain.PerstUser
import mycompany.domain.Owner
import oodb.PerstConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Custom Perst-based login authentication.
 * 
 * This replaces the default Login.groovy and uses Perst OODBMS for user storage.
 * It does NOT require PostgreSQL.
 * 
 * IMPORTANT: Add to KissInit.groovy to allow login without authentication:
 *   MainServlet.allowWithoutAuthentication("", "Login")
 */
class Login {
    private static final Logger logger = LogManager.getLogger(Login.class)

    /**
     * Validate a user's login name and password using Perst.
     *
     * @param db - Connection (not used, Perst is used instead)
     * @param user - username
     * @param password - password
     * @param outjson - extra data sent back to the front-end
     * @return UserData if successful, null otherwise
     */
    public static UserData login(PerstConnection db, String user, String password, JSONObject outjson, ProcessServlet servlet) {
        
        logger.info("[PerstAuth] Login attempt via Perst route for user: ${user}")
        
        // Use PerstUserManager for authentication
        try {
            // Find and authenticate user via Manager
            PerstUser perstUser = PerstUserManager.authenticate(user, password)
            
            if (perstUser == null) {
                logger.warn("[PerstAuth] Login FAILED: invalid credentials for user: ${user}")
                return null
            }
            
            // Create session using UserCache - store PerstUser directly
            UserData ud = UserCache.newUser(user, password, null)
            ud.putUserData("perstUser", perstUser)
            
            // Update last login date
            perstUser.setLastLoginDate(System.currentTimeMillis())
            PerstUserManager.update(perstUser)
            
            // Add user info to outjson - ALL OIDs for consistency
            outjson.put("userId", perstUser.getOid())
            outjson.put("username", perstUser.getUsername())
            outjson.put("email", perstUser.getEmail() ?: "")
            outjson.put("preferredLanguage", perstUser.getPreferredLanguage() ?: "en")
            
            // Add owner OID if available
            def actor = perstUser.getActor()
            Owner owner = (actor instanceof Owner) ? (Owner) actor : null
            if (owner != null) {
                outjson.put("ownerId", owner.getOid())
                outjson.put("ownerName", owner.getName() ?: "")
            } else {
                outjson.put("ownerId", 0)
                outjson.put("ownerName", "")
            }
            
            // Add cleaner OID if user is also a cleaner
            long cleanerId = 0
            if (actor != null) {
                try {
                    if (actor instanceof mycompany.domain.Cleaner) {
                        cleanerId = actor.getOid()
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            outjson.put("cleanerId", cleanerId)
            
            // Determine admin role from Actor's Agreement
            String role = null
            if (actor != null && actor.getAgreement() != null) {
                role = actor.getAgreement().getRole()
            }
            
            boolean isAdmin = "admin".equals(role) || "superAdmin".equals(role)
            outjson.put("isAdmin", isAdmin)
            outjson.put("role", role ?: "none")
            
            // adminType: system (full access) or content (business only)
            String adminType = "none"
            if ("superAdmin".equals(role)) {
                adminType = "system"
            } else if ("admin".equals(role)) {
                adminType = "content"
            }
            outjson.put("adminType", adminType)
            
            logger.info("[PerstAuth] Login SUCCESS for user: ${user} (Role: ${role ?: 'none'}, Actor: ${actor?.getName() ?: 'none'})")
            
            return ud
            
        } catch (Exception e) {
            outjson.put("error", "Login failed: " + e.message)
            logger.warn("[PerstAuth] Login FAILED: exception for user: ${user} - " + e.message)
            e.printStackTrace()
            return null
        }
    }

    /**
     * Re-validate a user.
     *
     * Users get re-validated about once every two minutes. This assures that a user is logged out 
     * if their login gets disabled while they're in the system.
     *
     * @param db - Connection (not used)
     * @param ud - UserData to validate
     * @return true if the user is still valid, false if not
     */
    public static Boolean checkLogin(Object db, UserData ud, ProcessServlet servlet) {
        // If using Perst only, we can check the user's active status
        // For now, always return true to allow sessions
        return true
    }
}
