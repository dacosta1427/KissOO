package koo.services;

import org.kissweb.database.Connection;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;
import org.kissweb.restServer.UserCache;
import koo.core.user.PerstUserManager;
import koo.core.user.PerstUser;
import koo.core.actor.AActor;
import koo.core.actor.ActorType;
import koo.core.actor.Role;
import mycompany.actor.owner.Owner;
import mycompany.actor.cleaner.Cleaner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Java-based Login service.
 * Replaces Groovy Login.groovy for Perst-only mode.
 */
public class Login {
    private static final Logger logger = LogManager.getLogger(Login.class);

    public static UserData login(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet) {
        System.out.println("[PerstAuth-DIRECT] Login called directly from ProcessServlet for user: " + user);
        
        try {
            PerstUser perstUser = PerstUserManager.authenticate(user, password);
            
            if (perstUser == null) {
                logger.warn("[PerstAuth] Login FAILED: invalid credentials for user: {}", user);
                return null;
            }
            
            UserData ud = UserCache.newUser(user, password, null);
            ud.putUserData("perstUser", perstUser);
            
            perstUser.setLastLoginDate(System.currentTimeMillis());
            PerstUserManager.update(perstUser);
            
            // Populate outjson with user data for frontend session
            outjson.put("userOid", perstUser.getOid());
            outjson.put("username", perstUser.getUsername() != null ? perstUser.getUsername() : "");
            outjson.put("email", perstUser.getEmail() != null ? perstUser.getEmail() : "");
            outjson.put("preferredLanguage", perstUser.getPreferredLanguage() != null ? perstUser.getPreferredLanguage() : "en");
            
            // Add activation status
            boolean fullyActivated = !perstUser.isMustChangePassword() && perstUser.isEmailVerified();
            outjson.put("needsPasswordChange", perstUser.isMustChangePassword());
            outjson.put("needsEmailVerification", !perstUser.isEmailVerified());
            outjson.put("fullyActivated", fullyActivated);
            
            // Extract actor information
            long ownerOid = 0;
            String ownerName = "";
            long cleanerOid = 0;
            String cleanerName = "";
            String roleName = "user";
            boolean isAdmin = false;
            
            AActor actor = perstUser.getActor();
            if (actor != null) {
                if (ActorType.NATURAL == actor.getActorType()) {
                    Owner owner = (Owner) actor;
                    ownerOid = owner.getOid();
                    ownerName = owner.getName() != null ? owner.getName() : "";
                } else if (ActorType.CORPORATE == actor.getActorType()) {
                    Cleaner cleaner = (Cleaner) actor;
                    cleanerOid = cleaner.getOid();
                    cleanerName = cleaner.getName() != null ? cleaner.getName() : "";
                }
            }
            
            outjson.put("ownerOid", ownerOid);
            outjson.put("ownerName", ownerName);
            outjson.put("cleanerOid", cleanerOid);
            outjson.put("cleanerName", cleanerName);
            
            // Get role from agreement
            Role role = null;
            if (actor != null && actor.getAgreement() != null) {
                role = actor.getAgreement().getRole();
            }
            if (role != null) {
                roleName = role.name();
                String roleStr = roleName != null ? roleName.toLowerCase() : "";
                if ("superadmin".equals(roleStr) || "super_admin".equals(roleStr) || "admin".equals(roleStr)) {
                    isAdmin = true;
                }
            }
            
            outjson.put("isAdmin", isAdmin);
            outjson.put("role", roleName);
            
            logger.info("[PerstAuth] outjson AFTER: {}", outjson.keySet());
            logger.info("[PerstAuth] Login SUCCESS for user: {} (Role: {}, Actor: {})",
                    user, roleName, actor != null ? actor.getName() : "none");
            
            return ud;
            
        } catch (Exception e) {
            outjson.put("error", "Login failed: " + e.getMessage());
            logger.warn("[PerstAuth] Login FAILED: exception for user: {} - {}", user, e.getMessage());
            return null;
        }
    }

    public static void checkLogin(Connection db, UserData ud, ProcessServlet servlet) {
        if (ud != null)
            ud.setLastAccessDate(java.time.LocalDateTime.now());
    }
}
