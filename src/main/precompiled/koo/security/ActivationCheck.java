package koo.security;

import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;

/**
 * Helper for checking user activation status.
 * Services should call requireFullActivation() to block unauthenticated actions.
 */
public class ActivationCheck {

    /**
     * Check if user is fully activated (password changed AND email verified).
     * @param servlet ProcessServlet to get session data
     * @return true if fully activated
     */
    public static boolean isFullyActivated(ProcessServlet servlet) {
        UserData ud = servlet.getUserData();
        if (ud == null) return false;
        return Boolean.TRUE.equals(ud.getUserData("isFullyActivated"));
    }

    /**
     * Check if user needs to change password.
     */
    public static boolean needsPasswordChange(ProcessServlet servlet) {
        UserData ud = servlet.getUserData();
        if (ud == null) return false;
        return Boolean.TRUE.equals(ud.getUserData("needsPasswordChange"));
    }

    /**
     * Check if user needs to verify email.
     */
    public static boolean needsEmailVerification(ProcessServlet servlet) {
        UserData ud = servlet.getUserData();
        if (ud == null) return false;
        return Boolean.TRUE.equals(ud.getUserData("needsEmailVerification"));
    }

    /**
     * Require full activation - fail if not fully activated.
     * Call this at the start of any service method that requires full activation.
     * @return true if allowed, false if output already set with error
     */
    public static boolean requireFullActivation(JSONObject injson, JSONObject outjson, ProcessServlet servlet) {
        if (isFullyActivated(servlet)) {
            return true;
        }

        outjson.put("_Success", false);
        outjson.put("_ErrorCode", 3);  // Not fully activated

        StringBuilder message = new StringBuilder("Please complete activation: ");
        if (needsPasswordChange(servlet)) {
            message.append("change password, ");
        }
        if (needsEmailVerification(servlet)) {
            message.append("verify email");
        }

        outjson.put("_ErrorMessage", message.toString());
        outjson.put("needsPasswordChange", needsPasswordChange(servlet));
        outjson.put("needsEmailVerification", needsEmailVerification(servlet));
        return false;
    }

    /**
     * Require admin role - fail if not admin or not fully activated.
     * @return true if allowed
     */
    public static boolean requireAdmin(JSONObject injson, JSONObject outjson, ProcessServlet servlet) {
        if (!requireFullActivation(injson, outjson, servlet)) {
            return false;
        }

        Object isAdmin = servlet.getUserData("isAdmin");
        if (!Boolean.TRUE.equals(isAdmin)) {
            outjson.put("_Success", false);
            outjson.put("_ErrorCode", 4);
            outjson.put("_ErrorMessage", "Admin access required");
            return false;
        }
        return true;
    }
}
