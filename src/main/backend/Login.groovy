import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import gfe.PerstHelper
import gfe.PerstUser
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
    public static UserData login(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet) {
        
        // BLOCKED - Perst authentication route
        System.out.println(">>>>>>>>>> BLOCKED: Perst login route hit for user: " + user);
        logger.info("[PerstAuth] Login attempt via Perst route for user: ${user}")
        
        // Check if Perst is available
        if (!PerstHelper.isAvailable()) {
            outjson.put("error", "Perst is not available")
            logger.warn("[PerstAuth] Perst is not available!")
            return null
        }
        
        try {
            // Find user in Perst
            PerstUser perstUser = PerstHelper.retrieveObject(PerstUser.class, "username", user)
            
            if (perstUser == null) {
                logger.warn("[PerstAuth] Login FAILED: user not found: ${user}")
                return null  // Invalid user
            }
            
            String storedPassword = perstUser.getPassword()
            if (storedPassword == null) {
                return null
            }
            
            // Verify password
            boolean passwordValid = false
            if (storedPassword.length() == 64) {
                // SHA256 hash comparison
                passwordValid = storedPassword.equals(password.sha256())
            } else {
                // Plain text comparison
                passwordValid = storedPassword.equals(password)
            }
            
            if (!passwordValid) {
                logger.warn("[PerstAuth] Login FAILED: invalid password for user: ${user}")
                return null
            }
            
            // Check if user is active
            if (!perstUser.isActive()) {
                outjson.put("error", "User account is inactive")
                logger.warn("[PerstAuth] Login FAILED: user inactive: ${user}")
                return null
            }
            
            // Create session using UserCache
            UserData ud = UserCache.newUser(user, password, perstUser.getUserId())
            
            // Update last login date
            perstUser.setLastLoginDate(System.currentTimeMillis())
            PerstHelper.storeModifiedObject(perstUser)
            
            logger.info("[PerstAuth] Login SUCCESS for user: ${user} (ID: ${perstUser.getUserId()})")
            
            return ud
            
        } catch (Exception e) {
            outjson.put("error", "Login failed: " + e.message)
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
