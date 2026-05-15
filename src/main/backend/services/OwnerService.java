package services;

import org.kissweb.json.JSONArray;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.MainServlet;
import org.kissweb.restServer.ProcessServlet;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.continuous.TransactionContainer;
import koo.core.actor.Role;
import koo.core.user.PerstUser;
import domain.actor.owner.Owner;
import domain.actor.owner.OwnerManager;

import java.util.Collection;

/**
 * OwnerService - REST endpoints for Owner CRUD operations.
 *
 * Authorization:
 * - Admin: can see and do everything
 * - Owner: sees only their own data
 */
public class OwnerService {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }

    public void getOwners(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<Owner> owners = OwnerManager.getAll();
            JSONArray rows = new JSONArray();

            for (Owner owner : owners) {
                JSONObject row = new JSONObject();
                row.put("oid", owner.getOid());
                row.put("name", owner.getName());
                row.put("email", owner.getEmail());
                row.put("phone", owner.getPhone());
                row.put("address", owner.getAddress());
                row.put("active", owner.isActive());
                PerstUser user = owner.getPerstUser();
                row.put("canLogin", user != null && user.isActive());
                row.put("emailVerified", user != null && user.isEmailVerified());
                rows.put(row);
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            Owner owner = OwnerManager.getByOid(oid);
            if (owner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }

            // Authorization check
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu != null) {
                koo.core.actor.AActor actor = pu.getActor();
                if (actor != null) {
                    boolean isAdmin = actor.getAgreement() != null &&
                        (actor.getAgreement().getRole() == Role.ADMIN || actor.getAgreement().getRole() == Role.SUPER_ADMIN);
                    if (!isAdmin) {
                        if (actor.getOid() != owner.getOid()) {
                            outjson.put("_Success", false);
                            outjson.put("_ErrorMessage", "Not authorized");
                            outjson.put("_ErrorCode", 3);
                            return;
                        }
                    }
                }
            }

            JSONObject data = new JSONObject();
            data.put("oid", owner.getOid());
            data.put("name", owner.getName());
            data.put("email", owner.getEmail());
            data.put("phone", owner.getPhone());
            data.put("address", owner.getAddress());
            data.put("active", owner.isActive());
            PerstUser user = owner.getPerstUser();
            data.put("canLogin", user != null && user.isActive());
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void createOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            JSONObject data = injson.getJSONObject("data");
            String name = data.getString("name");
            String email = data.has("email") ? data.getString("email") : "";
            String phone = data.has("phone") ? data.getString("phone") : "";
            String address = data.has("address") ? data.getString("address") : "";
            boolean active = data.has("active") ? data.getBoolean("active") : true;

            Owner owner = new Owner(name, phone, email, address, active);

            if (owner.getPerstUser() != null && email != null && !email.isEmpty()) {
                owner.getPerstUser().setUsername(email);
            }

            TransactionContainer tc = udbm.createContainer();
            tc.addInsert(owner);
            tc.addInsert(owner.getPerstUser());
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to create owner");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("oid", owner.getOid());
            result.put("name", owner.getName());
            result.put("email", owner.getEmail());
            result.put("phone", owner.getPhone());
            result.put("address", owner.getAddress());
            result.put("active", owner.isActive());
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void updateOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            JSONObject data = injson.getJSONObject("data");
            Owner owner = OwnerManager.getByOid(oid);
            if (owner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }

            if (data.has("name")) owner.setName(data.getString("name"));
            if (data.has("email")) owner.setEmail(data.getString("email"));
            if (data.has("phone")) owner.setPhone(data.getString("phone"));
            if (data.has("address")) owner.setAddress(data.getString("address"));
            if (data.has("active")) owner.setActive(data.getBoolean("active"));

            TransactionContainer tc = udbm.createContainer();
            tc.addUpdate(owner);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update owner");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("oid", owner.getOid());
            result.put("name", owner.getName());
            result.put("email", owner.getEmail());
            result.put("phone", owner.getPhone());
            result.put("address", owner.getAddress());
            result.put("active", owner.isActive());
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void deleteOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            Owner owner = OwnerManager.getByOid(oid);
            if (owner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }
            TransactionContainer tc = udbm.createContainer();
            tc.addDelete(owner);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to delete owner");
                return;
            }
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getUserByOwnerId(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long ownerId = injson.getLong("ownerId");
            Owner owner = OwnerManager.getByOid(ownerId);
            if (owner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }
            PerstUser user = owner.getPerstUser();
            if (user == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "User not found for owner");
                return;
            }
            JSONObject data = new JSONObject();
            data.put("oid", user.getOid());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("active", user.isActive());
            data.put("emailVerified", user.isEmailVerified());
            data.put("ownerId", owner.getOid());
            data.put("userId", user.getOid());
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }
}