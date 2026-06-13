package koo.services;

import domain.security.LoginDomainGuard;
import org.kissweb.database.Connection;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;
import org.kissweb.restServer.UserCache;
import koo.core.actor.user.PerstUserManager;
import koo.core.actor.user.PerstUser;
import koo.core.actor.AActor;
import koo.core.actor.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Java-based Login service.
 * Replaces Groovy Login.groovy for Perst-only mode.
 */
public class Login {
    private static final Logger logger = LogManager.getLogger(Login.class);

    public static UserData login(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        System.out.println("[PerstAuth-DIRECT] Login called with username: " + (injson != null ? injson.getString("username") : "null"));
        
        try {
            String username = injson.getString("username");
            String password = injson.getString("password");
            
            PerstUser perstUser = PerstUserManager.authenticate(username, password);
            
            if (perstUser == null) {
                logger.warn("[PerstAuth] Login FAILED: invalid credentials for user: {}", username);
                outjson.put("_Success", false);
                outjson.put("_ErrorCode", 2);
                outjson.put("_ErrorMessage", "Invalid login.");
                return null;
            }
            
            UserData ud = UserCache.newUser(username, password, null);
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
            String roleName = "user";
            boolean isAdmin = false;

            AActor actor = perstUser.getActor();
            if (actor != null) {
                // Enter the DomainLoginGuard to deal with domain specific particulars
                LoginDomainGuard loginDomainGuard = new LoginDomainGuard();
                JSONObject domainData = loginDomainGuard.login(actor);

                // TODO Kiss check for object addition
                //  Merge domain data into response
                if (domainData != null) {
                    outjson.put("domainData", domainData);
                }
            }

            // Get role from agreement
            Role role = null;
            if (actor != null && actor.getAgreement() != null) {
                role = actor.getAgreement().getRole();
                roleName = role.name();
                isAdmin = role.equals(Role.ADMIN) || role.equals(Role.SUPER_ADMIN);
            }

            outjson.put("isAdmin", isAdmin);
            outjson.put("role", roleName);
            
            // Add adminType for frontend role detection
            String adminType = "none";
            if (isAdmin) {
                if ("super_admin".equals(roleName.toLowerCase()) || "superadmin".equals(roleName.toLowerCase())) {
                    adminType = "system";
                } else {
                    adminType = "content";
                }
            }
            outjson.put("adminType", adminType);

            outjson.put("_Success", true);
            
            logger.info("[PerstAuth] outjson AFTER: {}", outjson.keySet());
            logger.info("[PerstAuth] Login SUCCESS for user: {} (Role: {}, Actor: {})",
                    username, roleName, actor != null ? actor.getName() : "none");
            
            return ud;
            
        } catch (Exception e) {
            outjson.put("error", "Login failed: " + e.getMessage());
            logger.warn("[PerstAuth] Login FAILED: exception for user: {} - {}", 
                    injson != null ? injson.getString("username") : "unknown", e.getMessage());
            return null;
        }
    }
    
    public static void checkLogin(Connection db, UserData ud, ProcessServlet servlet) {
        if (ud != null)
            ud.setLastAccessDate(java.time.LocalDateTime.now());
    }
}