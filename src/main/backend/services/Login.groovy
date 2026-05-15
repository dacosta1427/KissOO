package services

import koo.core.database.PerstConnection
import org.kissweb.json.JSONObject
import org.kissweb.restServer.ProcessServlet
import org.kissweb.database.Connection

class Login {
    static Object login(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String username = injson.getString("username")
            String password = injson.getString("password")

            // Delegate to Java implementation
            return koo.services.Login.login(injson, outjson, db, servlet)

        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", "Login error: " + e.getMessage())
            e.printStackTrace()
            return null
        }
    }

    static void checkLogin(Connection db, org.kissweb.restServer.UserData ud, ProcessServlet servlet) {
        try {
            koo.services.Login.checkLogin(db, ud, servlet)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}