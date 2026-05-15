import koo.core.database.PerstConnection
import koo.core.user.PerstUserManager
import org.kissweb.json.JSONObject
import org.kissweb.restServer.ProcessServlet

class Login {
    static Object login(PerstConnection db, String username, String password, JSONObject outjson, ProcessServlet servlet) {
        try {
            // Build injson from framework parameters
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
            koo.services.Login.checkLogin(db, ud, servlet)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}