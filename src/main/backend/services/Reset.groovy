package services

import org.kissweb.restServer.GroovyClass

/**
 * Reset Groovy classloader - for development only
 */
class Reset {
    static void resetGroovy(Connection db, JSONObject injson, JSONObject outjson, ProcessServlet servlet) {
        GroovyClass.reset()
        outjson.put("_Success", true)
        outjson.put("message", "Groovy classloader reset")
    }
}
