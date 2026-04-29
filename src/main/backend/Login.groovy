/**
 * Login.groovy - Thin wrapper for framework integration
 * Delegates actual authentication logic to Java Login class
 * 
 * This service follows the Kiss framework pattern:
 * void methodName(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)
 * 
 * Note: This class is kept as a thin wrapper for compatibility with the framework
 * which expects to call Login.login via GroovyClass.invoke. The actual implementation
 * is in koo.services.Login.java which can be called directly by Java services.
 */
package services

import koo.core.database.PerstConnection
import org.kissweb.json.JSONObject
import org.kissweb.restServer.ProcessServlet

class Login {
    static Object login(PerstConnection db, String username, String password, JSONObject outjson, ProcessServlet servlet) {
        try {
            // Build injson from framework parameters
            // The framework calls this with (db, username, password, outjson, servlet)
            // We need to adapt it to match the Java Login.signature:
            // (JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)
            JSONObject injson = new JSONObject()
            injson.put("username", username)
            injson.put("password", password)
            
            // Delegate to Java implementation
            return koo.services.Login.login(injson, outjson, db, servlet)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", "Login error: " + e.getMessage())
            e.printStackTrace()
            return null
        }
    }
    
    static void checkLogin(PerstConnection db, org.kissweb.restServer.UserData ud, ProcessServlet servlet) {
        try {
            // Delegate to Java implementation
            koo.services.Login.checkLogin(db, ud, servlet)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
