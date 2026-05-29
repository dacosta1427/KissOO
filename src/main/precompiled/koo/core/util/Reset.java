package koo.core.util;

import org.kissweb.restServer.GroovyClass;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.security.EXTERNAL_CALL;

/**
 * Development utility to reset the Groovy classloader.
 *
 * Calling this forces Kiss to reload all Groovy service classes when they are next
 * invoked. This provides "hot-reloading" of Groovy backend services without
 * restarting the application server.
 *
 * Useful during development when iterating on Groovy service code.
 * Should be used with caution in production (e.g., triggered by admin role).
 * 
 * HTTP Request:
 * {
 *   "_class": "koo.core.util.Reset",
 *   "_method": "resetGroovy",
 *   "_uuid": "session-uuid"
 * }
 */
public class Reset {
    @EXTERNAL_CALL
    public void resetGroovy(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GroovyClass.reset();
        outjson.put("_Success", true);
        outjson.put("message", "Groovy classloader reset");
    }
}
