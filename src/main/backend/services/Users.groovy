package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import mycompany.database.PerstUserManager
import mycompany.database.OwnerManager
import mycompany.domain.PerstUser
import mycompany.domain.Owner
import java.util.Collection

/**
 * Users service for CRUD operations on PerstUser.
 * 
 * Uses Perst OODBMS instead of SQL database.
 */
class Users {

    void getRecords(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<PerstUser> users = PerstUserManager.getAll()
            JSONArray rows = new JSONArray()
            
            for (PerstUser user : users) {
                JSONObject row = new JSONObject()
                row.put("id", user.getOid())
                row.put("userName", user.getUsername())
                row.put("userPassword", user.getPasswordHash())
                row.put("userActive", user.isActive() ? "Y" : "N")
                rows.put(row)
            }
            
            outjson.put("rows", rows)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void addRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String userName = injson.getString("userName")
            String password = injson.getString("userPassword")
            String email = injson.getString("email", "")
            String name = injson.getString("name", userName)
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            
            // Generate unique userId
            Collection<PerstUser> existingUsers = PerstUserManager.getAll()
            int maxUserId = 0
            for (PerstUser u : existingUsers) {
                if (u.getUserId() > maxUserId) {
                    maxUserId = u.getUserId()
                }
            }
            int newUserId = maxUserId + 1
            
            // Create Owner first
            Owner owner = OwnerManager.create(name, email, phone, address, true)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create owner")
                return
            }
            
            // Create User with ownerId
            PerstUser user = PerstUserManager.create(userName, password, newUserId, owner.getOid())
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create user")
                return
            }
            
            // Link owner to user - temporarily disabled due to type error
            // owner.setUserId(user.getOid())
            // OwnerManager.update(owner)
            
            user.setActive(injson.getString("userActive") == "Y")
            user.setEmailVerified(true)  // Allow immediate login without email verification
            user.setEmail(email)
            PerstUserManager.update(user)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("id", user.getOid())
            outjson.put("ownerId", owner.getOid())
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void updateRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            PerstUser userToUpdate = PerstUserManager.getByOid(oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setUsername(injson.getString("userName"))
            userToUpdate.setPassword(injson.getString("userPassword"))
            userToUpdate.setActive(injson.getString("userActive") == "Y")
            
            PerstUserManager.update(userToUpdate)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void deleteRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            PerstUser userToDelete = PerstUserManager.getByOid(oid)
            
            if (userToDelete == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            PerstUserManager.delete(userToDelete)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }
}
