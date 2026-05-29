package koo.core.database;

import java.util.Collection;

import org.kissweb.json.JSONObject;

import koo.core.actor.user.PerstUser;

/**
 * Framework utility to initialize Perst storage with a default admin user.
 * Call during application startup if no PerstUsers exist yet.
 */
public class PerstInit {

    /**
     * Create a default admin user if none exists yet.
     * Call this from your bootstrap code (KissInit or servlet init).
     *
     * @return true if admin was created, false if skipped/failed
     */
    public static boolean initAdminUser() {

        if (!StorageManager.isAvailable()) {
            System.err.println("Perst is not available");
            return false;
        }

        Collection<PerstUser> users = StorageManager.getAll(PerstUser.class);
        if (users != null && !users.isEmpty()) {
            return false;
        }

        try {
            PerstUser admin = new PerstUser("admin", "admin", null);
            admin.setEmail("admin@localhost");
            admin.setActive(true);
            admin.setEmailVerified(true);

            org.garret.perst.continuous.TransactionContainer tc = StorageManager.createContainer();
            tc.addInsert(admin);
            StorageManager.store(tc);

            System.out.println("Default admin user created. CHANGE PASSWORD IMMEDIATELY!");
            return true;

        } catch (Exception e) {
            System.err.println("Failed to create admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Service-compatible overload that accepts the standard Kiss service signature.
     * Can be called directly from Kiss services when bootstrapping.
     */
    public static boolean initAdminUser(JSONObject injson, JSONObject outjson) {
        boolean created = initAdminUser();
        if (outjson != null) {
            if (created) {
                outjson.put("status", "created");
                outjson.put("username", "admin");
                outjson.put("message", "Default admin user created. CHANGE PASSWORD IMMEDIATELY!");
            } else {
                Collection<PerstUser> users = StorageManager.getAll(PerstUser.class);
                if (users != null && !users.isEmpty()) {
                    outjson.put("status", "skipped");
                    outjson.put("message", "Users already exist");
                    outjson.put("count", users.size());
                } else {
                    outjson.put("status", "failed");
                }
            }
        }
        return created;
    }
}
