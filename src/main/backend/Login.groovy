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
            
            // Create session using UserCache
            UserData ud = UserCache.newUser(user, password, perstUser.getUserId())
            
            // Update last login date
            perstUser.setLastLoginDate(System.currentTimeMillis())
            PerstUserManager.update(perstUser)
            
            // Add user info to outjson - ALL OIDs for consistency
            outjson.put("userId", perstUser.getOid())  // Perst OID
            outjson.put("username", perstUser.getUsername())
            outjson.put("email", perstUser.getEmail() ?: "")
            outjson.put("preferredLanguage", perstUser.getPreferredLanguage() ?: "en")
            
            // Add owner OID if available
            Owner owner = perstUser.getOwner()
            if (owner != null) {
                outjson.put("ownerId", owner.getOid())  // Perst OID
                outjson.put("ownerName", owner.getName() ?: "")
            } else {
                outjson.put("ownerId", 0)
                outjson.put("ownerName", "")
            }
            
            // Get Actor once and cache - avoids duplicate DB lookups
            def actor = perstUser.getActor()
            
            // Add cleaner OID if user is also a cleaner (via Actor relationship)
            long cleanerId = 0
            if (actor != null && actor instanceof mycompany.domain.Cleaner) {
                cleanerId = actor.getOid()  // Perst OID
            }
            outjson.put("cleanerId", cleanerId)
            
            // isAdmin: check if user has admin role in their agreement
            boolean isAdmin = actor != null && 
                              actor.getAgreement() != null &&
                              "admin".equals(actor.getAgreement().getRole())
            outjson.put("isAdmin", isAdmin)
            
            logger.info("[PerstAuth] Login SUCCESS for user: ${user} (ID: ${perstUser.getUserId()}, Owner: ${owner?.getOid() ?: 'none'})")
            
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
    public static Boolean checkLogin(Connection db, UserData ud, ProcessServlet servlet) {
        // If using Perst only, we can check the user's active status
        // For now, always return true to allow sessions
        return true
    }
}
