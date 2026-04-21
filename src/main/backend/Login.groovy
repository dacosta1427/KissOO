import mycompany.actor.cleaner.Cleaner
import org.kissweb.json.JSONObject
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import koo.core.user.PerstUserManager
import koo.core.user.PerstUser
import mycompany.actor.owner.Owner
import koo.PerstConnection
import koo.core.actor.Role
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
            outjson.put("userOid", perstUser.getOid())
            outjson.put("username", perstUser.getUsername())
            outjson.put("email", perstUser.getEmail() ?: "")
            outjson.put("preferredLanguage", perstUser.getPreferredLanguage() ?: "en")
            
            // Add activation status
            boolean fullyActivated = !perstUser.isMustChangePassword() && perstUser.isEmailVerified()
            outjson.put("needsPasswordChange", perstUser.isMustChangePassword())
            outjson.put("needsEmailVerification", !perstUser.isEmailVerified())
            outjson.put("fullyActivated", fullyActivated)
            
            // Add owner OID if available
            def actor = perstUser.getAActor()
            Owner owner = (actor instanceof Owner) ? (Owner) actor : null
            if (owner != null) {
                outjson.put("ownerOid", owner.getOid())
                outjson.put("ownerName", owner.getName() ?: "")
            } else {
                outjson.put("ownerOid", 0)
                outjson.put("ownerName", "")
            }
            
            // Add cleaner OID if user is also a cleaner
            long cleanerOid = 0
            if (actor != null) {
                try {
                    if (actor instanceof Cleaner) {
                        cleanerOid = actor.getOid()
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            outjson.put("cleanerOid", cleanerOid)
            
            // Determine admin role from AActor's Agreement
            Role role = null
            if (actor != null && actor.getAgreement() != null) {
                role = actor.getAgreement().getRole()
            }
            
            boolean isAdmin = role == Role.ADMIN || role == Role.SUPER_ADMIN
            outjson.put("isAdmin", isAdmin)
            outjson.put("role", role?.name() ?: "none")
            
            // adminType: system (full access) or content (business only)
            String adminType = "none"
            if (role == Role.SUPER_ADMIN) {
                adminType = "system"
            } else if (role == Role.ADMIN) {
                adminType = "content"
            }
            outjson.put("adminType", adminType)
            
            logger.info("[PerstAuth] Login SUCCESS for user: ${user} (Role: ${role ?: 'none'}, AActor: ${actor?.getName() ?: 'none'})")
            
            return ud
            
        } catch (Exception e) {
            outjson.put("error", "Login failed: " + e.message)
            logger.warn("[PerstAuth] Login FAILED: exception for user: ${user} - " + e.message)
            e.printStackTrace()
            return null
        }
    }

    /**
     * Check if user is fully activated (password changed AND email verified).
     * @param ud UserData session
     * @return true if fully activated, false otherwise
     */
    public static boolean isFullyActivated(UserData ud) {
        PerstUser pu = (PerstUser) ud.getUserData("perstUser")
        if (pu == null) return false
        return !pu.isMustChangePassword() && pu.isEmailVerified()
    }
    
    /**
     * Check if user needs password change.
     * @param ud UserData session
     * @return true if password change required
     */
    public static boolean needsPasswordChange(UserData ud) {
        PerstUser pu = (PerstUser) ud.getUserData("perstUser")
        if (pu == null) return false
        return pu.isMustChangePassword()
    }
    
    /**
     * Check if user needs email verification.
     * @param ud UserData session
     * @return true if email verification required
     */
    public static boolean needsEmailVerification(UserData ud) {
        PerstUser pu = (PerstUser) ud.getUserData("perstUser")
        if (pu == null) return false
        return !pu.isEmailVerified()
    }
    
    /**
     * Re-validate a user.
     *
     * Users get re-validated about once every two minutes. This assures that a user is logged out 
     * if their login gets disabled while they're in the system.
     *
     * Note: We allow login even if not fully activated (password change/email verification pending)
     * so users can access the activation services. Services must check isFullyActivated() themselves.
     *
     * @param db - Connection (not used)
     * @param ud - UserData to validate
     * @return true if the user is still valid, false if not
     */
    public static Boolean checkLogin(Object db, UserData ud, ProcessServlet servlet) {
        PerstUser pu = (PerstUser) ud.getUserData("perstUser")
        if (pu == null) {
            logger.warn("[PerstAuth] checkLogin FAILED: no PerstUser in session")
            return false
        }
        
        // Check if user is still active
        if (!pu.isActive()) {
            logger.warn("[PerstAuth] checkLogin FAILED: user {} is inactive", pu.getUsername())
            return false
        }
        
        // Note: We do NOT fail login if email not verified or password needs change.
        // Users need to stay logged in to complete activation.
        // Services must check isFullyActivated() before allowing any action.
        
        // Store activation status flags in session for easy access
        ud.putUserData("needsPasswordChange", pu.isMustChangePassword())
        ud.putUserData("needsEmailVerification", !pu.isEmailVerified())
        ud.putUserData("isFullyActivated", !pu.isMustChangePassword() && pu.isEmailVerified())
        
        return true
    }
}
