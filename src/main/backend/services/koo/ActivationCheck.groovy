package services.koo

import org.kissweb.json.JSONObject
import org.kissweb.restServer.ProcessServlet

/**
 * Helper for checking user activation status.
 * Services should call requireFullActivation() to block unauthenticated actions.
 */
class ActivationCheck {
    
    /**
     * Check if user is fully activated (password changed AND email verified).
     * @param servlet ProcessServlet to get session data
     * @return true if fully activated
     */
    static boolean isFullyActivated(ProcessServlet servlet) {
        def ud = servlet.getUserData()
        if (ud == null) return false
        return ud.getUserData("isFullyActivated") == true
    }
    
    /**
     * Check if user needs to change password.
     */
    static boolean needsPasswordChange(ProcessServlet servlet) {
        def ud = servlet.getUserData()
        if (ud == null) return false
        return ud.getUserData("needsPasswordChange") == true
    }
    
    /**
     * Check if user needs to verify email.
     */
    static boolean needsEmailVerification(ProcessServlet servlet) {
        def ud = servlet.getUserData()
        if (ud == null) return false
        return ud.getUserData("needsEmailVerification") == true
    }
    
    /**
     * Require full activation - fail if not fully activated.
     * Call this at the start of any service method that requires full activation.
     * @return true if allowed, false if output already set with error
     */
    static boolean requireFullActivation(JSONObject injson, JSONObject outjson, ProcessServlet servlet) {
        if (isFullyActivated(servlet)) {
            return true
        }
        
        outjson.put("_Success", false)
        outjson.put("_ErrorCode", 3)  // Not fully activated
        outjson.put("_ErrorMessage", "Please complete activation: " + 
            (needsPasswordChange(servlet) ? "change password, " : "") +
            (needsEmailVerification(servlet) ? "verify email" : ""))
        outjson.put("needsPasswordChange", needsPasswordChange(servlet))
        outjson.put("needsEmailVerification", needsEmailVerification(servlet))
        return false
    }
    
    /**
     * Require admin role - fail if not admin or not fully activated.
     * @return true if allowed
     */
    static boolean requireAdmin(JSONObject injson, JSONObject outjson, ProcessServlet servlet) {
        if (!requireFullActivation(injson, outjson, servlet)) {
            return false
        }
        
        def isAdmin = servlet.getUserData("isAdmin")
        if (isAdmin != true) {
            outjson.put("_Success", false)
            outjson.put("_ErrorCode", 4)
            outjson.put("_ErrorMessage", "Admin access required")
            return false
        }
        return true
    }
}