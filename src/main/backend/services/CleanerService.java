package services;

import org.kissweb.json.JSONArray;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.continuous.TransactionContainer;
import koo.core.actor.Role;
import koo.core.user.PerstUser;
import domain.actor.cleaner.Cleaner;
import domain.actor.cleaner.CleanerManager;

import java.util.Collection;

/**
 * CleanerService - REST endpoints for Cleaner CRUD operations.
 *
 * Authorization:
 * - Admin: can see and do everything
 */
public class CleanerService {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment("unifiedDBManager");
    }

    public void getCleaners(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            if (!isAdmin(servlet)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: Admin only");
                return;
            }

            Collection<Cleaner> cleaners = CleanerManager.getAll();
            JSONArray rows = new JSONArray();

            for (Cleaner cleaner : cleaners) {
                JSONObject row = new JSONObject();
                row.put("oid", cleaner.getOid());
                row.put("name", cleaner.getName());
                row.put("phone", cleaner.getPhone());
                row.put("email", cleaner.getEmail());
                row.put("address", cleaner.getAddress());

                PerstUser user = cleaner.getPerstUser();
                if (user != null) {
                    row.put("canLogin", user.isActive());
                    row.put("emailVerified", user.isEmailVerified());
                } else {
                    row.put("canLogin", false);
                    row.put("emailVerified", false);
                }
                rows.put(row);
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            if (!isAdmin(servlet)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: Admin only");
                return;
            }

            long oid = injson.getLong("oid");
            Cleaner cleaner = CleanerManager.getByOid(oid);
            if (cleaner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cleaner not found");
                return;
            }

            JSONObject data = new JSONObject();
            data.put("oid", cleaner.getOid());
            data.put("name", cleaner.getName());
            data.put("phone", cleaner.getPhone());
            data.put("email", cleaner.getEmail());
            data.put("address", cleaner.getAddress());
            data.put("active", cleaner.isActive());

            PerstUser user = cleaner.getPerstUser();
            if (user != null) {
                data.put("canLogin", user.isActive());
                data.put("emailVerified", user.isEmailVerified());
            } else {
                data.put("canLogin", false);
                data.put("emailVerified", false);
            }
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void createCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            if (!isAdmin(servlet)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: Admin only");
                return;
            }

            JSONObject data = injson.getJSONObject("data");
            String name = data.getString("name");
            String phone = data.has("phone") ? data.getString("phone") : "";
            String email = data.has("email") ? data.getString("email") : "";
            boolean active = data.has("active") ? data.getBoolean("active") : true;

            Cleaner cleaner = new Cleaner(name, phone, email, active);

            TransactionContainer tc = udbm.createContainer();
            tc.addInsert(cleaner);
            tc.addInsert(cleaner.getPerstUser());
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to create cleaner");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("oid", cleaner.getOid());
            result.put("name", cleaner.getName());
            result.put("phone", cleaner.getPhone());
            result.put("email", cleaner.getEmail());
            result.put("address", cleaner.getAddress());
            result.put("active", cleaner.isActive());
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void updateCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            if (!isAdmin(servlet)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: Admin only");
                return;
            }

            long oid = injson.getLong("oid");
            JSONObject data = injson.getJSONObject("data");
            Cleaner cleaner = CleanerManager.getByOid(oid);
            if (cleaner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cleaner not found");
                return;
            }

            if (data.has("name")) cleaner.setName(data.getString("name"));
            if (data.has("phone")) cleaner.setPhone(data.getString("phone"));
            if (data.has("email")) cleaner.setEmail(data.getString("email"));
            if (data.has("address")) cleaner.setAddress(data.getString("address"));
            if (data.has("active")) cleaner.setActive(data.getBoolean("active"));

            TransactionContainer tc = udbm.createContainer();
            tc.addUpdate(cleaner);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update cleaner");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("oid", cleaner.getOid());
            result.put("name", cleaner.getName());
            result.put("phone", cleaner.getPhone());
            result.put("email", cleaner.getEmail());
            result.put("address", cleaner.getAddress());
            result.put("active", cleaner.isActive());
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void deleteCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            if (!isAdmin(servlet)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: Admin only");
                return;
            }

            long oid = injson.getLong("oid");
            Cleaner cleaner = CleanerManager.getByOid(oid);
            if (cleaner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cleaner not found");
                return;
            }
            TransactionContainer tc = udbm.createContainer();
            tc.addDelete(cleaner);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to delete cleaner");
                return;
            }
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void toggleCleanerLogin(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            if (!isAdmin(servlet)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: Admin only");
                return;
            }

            long oid = injson.getLong("oid");
            boolean canLogin = injson.getBoolean("canLogin");
            Cleaner cleaner = CleanerManager.getByOid(oid);
            if (cleaner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cleaner not found");
                return;
            }
            PerstUser user = cleaner.getPerstUser();
            if (user != null) {
                user.setActive(canLogin);
                TransactionContainer tc = udbm.createContainer();
                tc.addUpdate(user);
                if (!udbm.store(tc).isSuccess()) {
                    outjson.put("_Success", false);
                    outjson.put("_ErrorMessage", "Failed to update cleaner login status");
                    return;
                }
            }

            JSONObject result = new JSONObject();
            result.put("oid", cleaner.getOid());
            result.put("name", cleaner.getName());
            result.put("phone", cleaner.getPhone());
            result.put("email", cleaner.getEmail());
            result.put("address", cleaner.getAddress());
            result.put("active", cleaner.isActive());
            result.put("canLogin", canLogin);
            result.put("emailVerified", user != null && user.isEmailVerified());
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    private boolean isAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return false;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor == null) return false;
            koo.core.actor.Role role = actor.getAgreement() != null ? actor.getAgreement().getRole() : null;
            return role == Role.ADMIN || role == Role.SUPER_ADMIN;
        } catch (Exception e) {
            return false;
        }
    }
}