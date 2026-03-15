package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import mycompany.database.PerstUserManager
import mycompany.domain.PerstUser

/**
 * Users service for CRUD operations on PerstUser.
 * 
 * Uses Perst OODBMS instead of SQL database.
 */
class Users {

    /**
     * Get all user records from Perst.
     */
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
        
        outjson.put("rows", rows)
    }

    /**
     * Add a new user record to Perst.
     */
    void addRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String userName = injson.getString("userName")
            String password = injson.getString("userPassword")
            
            PerstUser user = PerstUserManager.create(userName, password, 0)
            user.setActive(injson.getString("userActive") == "Y")
            PerstUserManager.update(user)
            
            outjson.put("success", true)
            outjson.put("id", user.getOid())
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    /**
     * Update an existing user record in Perst.
     */
    void updateRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            PerstUser userToUpdate = PerstUserManager.getByOid(oid)
            
            if (userToUpdate == null) {
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setUsername(injson.getString("userName"))
            userToUpdate.setPassword(injson.getString("userPassword"))
            userToUpdate.setActive(injson.getString("userActive") == "Y")
            
            PerstUserManager.update(userToUpdate)
            
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    /**
     * Delete a user record from Perst.
     */
    void deleteRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            PerstUser userToDelete = PerstUserManager.getByOid(oid)
            
            if (userToDelete == null) {
                outjson.put("error", "User not found")
                return
            }
            
            PerstUserManager.delete(userToDelete)
            
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }
}
