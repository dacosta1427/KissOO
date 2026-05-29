package services.koo.internal;

import java.util.Collection;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;

import koo.core.database.StorageManager;
import koo.core.actor.user.PerstUser;

/**
 * Initialize Perst users.
 *
 * Run this once to create the default admin user if none exists.
 *
 * HTTP Request:
 * {
 *   "_class": "services.koo.internal.PerstInit",
 *   "_method": "init",
 *   "_uuid": "session-uuid"
 * }
 *
 * Or call directly from code:
 *   new services.koo.internal.PerstInit().init(null, null, null, null);
 */
public class PerstInit {

    public void init(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        if (!StorageManager.isAvailable()) {
            if (outjson != null) {
                outjson.put("error", "Perst is not available");
            }
            return;
        }

        Collection<PerstUser> users = StorageManager.getAll(PerstUser.class);
        if (users != null && !users.isEmpty()) {
            if (outjson != null) {
                outjson.put("status", "skipped");
                outjson.put("message", "Users already exist");
                outjson.put("count", users.size());
            }
            return;
        }

        try {
            PerstUser admin = new PerstUser("admin", "admin", null);
            admin.setEmail("admin@localhost");
            admin.setActive(true);
            admin.setEmailVerified(true);

            org.garret.perst.continuous.TransactionContainer tc = StorageManager.createContainer();
            tc.addInsert(admin);
            StorageManager.store(tc);

            if (outjson != null) {
                outjson.put("status", "created");
                outjson.put("username", "admin");
                outjson.put("message", "Default admin user created. CHANGE PASSWORD IMMEDIATELY!");
            }
            System.out.println("Default admin user created. CHANGE PASSWORD IMMEDIATELY!");

        } catch (Exception e) {
            if (outjson != null) {
                outjson.put("error", "Failed: " + e.getMessage());
            }
        }
    }
}
