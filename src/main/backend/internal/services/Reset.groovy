package internal.services

import org.kissweb.restServer.GroovyClass
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.security.EXTERNAL_CALL

class Reset {
    @EXTERNAL_CALL
    void resetGroovy(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GroovyClass.reset()
        outjson.put("_Success", true)
        outjson.put("message", "Groovy classloader reset")
    }
}