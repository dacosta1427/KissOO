package services

import org.kissweb.restServer.MainServlet
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.garret.perst.dbmanager.UnifiedDBManager
import org.garret.perst.continuous.TransactionContainer
import koo.core.user.PerstUser

/**
 * Initialize Perst users.
 *
 * Run this once to create the default admin user if none exists.
 *
 * HTTP Request:
 * {
 *   "_class": "services.PerstInit",
 *   "_method": "init",
 *   "_uuid": "session-uuid"
 * }
 *
 * Or call directly from code:
 *   groovy: new services.PerstInit().init(null, null, null, null)
 */
class PerstInit {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager")
    }

    void init(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        def udbm = getUdbm(servlet)
        if (udbm == null) {
            if (outjson) outjson.put("error", "Perst is not available")
            return
        }

        def results = udbm.getObjects(PerstUser.class)
        def users = []
        if (results != null) {
            while (results.hasNext()) {
                users.add(results.next())
            }
        }

        if (users && users.size() > 0) {
            if (outjson) {
                outjson.put("status", "skipped")
                outjson.put("message", "Users already exist")
                outjson.put("count", users.size())
            }
            return
        }

        try {
            def admin = new PerstUser("admin", "admin", 1)
            admin.setEmail("admin@localhost")
            admin.setActive(true)
            admin.setEmailVerified(true)

            def tc = udbm.createContainer()
            tc.addInsert(admin)
            def result = udbm.store(tc)

            if (outjson) {
                if (result.isSuccess()) {
                    outjson.put("status", "created")
                    outjson.put("username", "admin")
                    outjson.put("message", "Default admin user created. CHANGE PASSWORD IMMEDIATELY!")
                } else {
                    outjson.put("error", "Failed: " + result.getMessage())
                }
            }
            println "Default admin user created. CHANGE PASSWORD IMMEDIATELY!"

        } catch (Exception e) {
            if (outjson) outjson.put("error", "Failed: " + e.message)
        }
    }
}