package services.koo;

import jakarta.json.JsonObject;
import org.kissweb.restServer.ProcessServlet;

/**
 * Helper for checking user activation status.
 * Services should call requireFullActivation() before sensitive operations.
 */
public class ActivationCheck {
    
    /**
     * Check if user is fully activated (password changed AND email verified).
     */
    public static boolean isFullyActivated(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) return false;
        
        return ud.containsKey("isFullyActivated") && 
               ud.getBoolean("isFullyActivated");
    }
    
    /**
     * Check if user needs to change password.
     */
    public static boolean needsPasswordChange(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) return false;
        return ud.containsKey("needsPasswordChange") && 
               ud.getBoolean("needsPasswordChange");
    }
    
    /**
     * Check if user needs to verify email.
     */
    public static boolean needsEmailVerification(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) return false;
        return ud.containsKey("needsEmailVerification") && 
               ud.getBoolean("needsEmailVerification");
    }
    
    /**
     * Require full activation - fail if not fully activated.
     * Call this at the start of any service method that requires full activation.
     * @return true if allowed, false if output already set with error
     */
    public static boolean requireFullActivation(JsonObject invson, 
                                                JsonObject outjson, 
                                                ProcessServlet servlet) {
        if (isFullyActivated(servlet)) {
            return true;
        }
        
        outjson.addProperty("_Success", false);
        outjson.addProperty("_ErrorCode", 3);  // Not fully activated
        
        StringBuilder msg = new StringBuilder("Please complete activation: ");
        if (needsPasswordChange(servlet)) {
            msg.append("change password, ");
        }
        if (needsEmailVerification(servlet)) {
            msg.append("verify email");
        }
        
        outjson.addProperty("_ErrorMessage", msg.toString());
        outjson.addProperty("needsPasswordChange", needsPasswordChange(servlet));
        outjson.addProperty("needsEmailVerification", needsEmailVerification(servlet));
        
        return false;
    }
    
    /**
     * Require admin role - fail if not fully activated or not admin.
     * @return true if allowed
     */
    public static boolean requireAdmin(JsonObject invson, 
                                       JsonObject outjson, 
                                       ProcessServlet servlet) {
        if (!requireFullActivation(invson, outjson, servlet)) {
            return false;
        }
        
        JsonObject ud = servlet.getUserData();
        if (ud == null || !ud.containsKey("isAdmin") || !ud.getBoolean("isAdmin")) {
            outjson.addProperty("_Success", false);
            outjson.addProperty("_ErrorCode", 4);
            outjson.addProperty("_ErrorMessage", "Admin access required");
            return false;
        }
        return true;
    }
}
