package mycompany.service;

import org.kissweb.database.Connection;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;
import org.kissweb.restServer.UserCache;
import mycompany.database.PerstUserManager;
import mycompany.domain.PerstUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Java-based Login service.
 * Replaces Groovy Login.groovy for Perst-only mode.
 */
public class Login {
    private static final Logger logger = LogManager.getLogger(Login.class);

    public static UserData login(Connection db, String user, String password, JSONObject outjson, ProcessServlet servlet) {
        logger.info("[PerstAuth] Login attempt via Perst route for user: {}", user);
        
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
            
            logger.info("[PerstAuth] Login SUCCESS for user: {} (Actor: {})", user, perstUser.getActor().getName());
            
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
