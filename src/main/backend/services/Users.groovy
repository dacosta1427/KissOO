package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.MainServlet
import mycompany.domain.PerstUser
import mycompany.domain.Owner
import oodb.PerstConnection

/**
 * Users service for CRUD operations on PerstUser.
 * 
 * Uses PerstConnection for Perst OODBMS operations.
 */
class Users {

    /**
     * Get PerstConnection from environment if available.
     */
    private PerstConnection getPerst() {
        return (PerstConnection) MainServlet.getEnvironment("PerstConnection")
    }

    void getRecords(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            Collection<PerstUser> users = perst.getAll(PerstUser)
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
            PerstConnection perst = getPerst()
            String userName = injson.getString("userName")
            String password = injson.getString("userPassword")
            String email = injson.getString("email", "")
            String name = injson.getString("name", userName)
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            
            // Generate unique userId
            Collection<PerstUser> existingUsers = perst.getAll(PerstUser)
            int maxUserId = existingUsers.collect { it.getUserId() }.max() ?: 0
            int newUserId = maxUserId + 1
            
            // Create User first
            PerstUser user = new PerstUser(userName, password, newUserId)
            def userTc = perst.perstCreateContainer()
            userTc.addInsert(user)
            if (!perst.perstStore(userTc)) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create user")
                return
            }
            
            // Create Owner with User reference
            Owner owner = new Owner(name, email, phone, address, true)
            owner.setPerstUser(user)
            def ownerTc = perst.perstCreateContainer()
            ownerTc.addInsert(owner)
            if (!perst.perstStore(ownerTc)) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create owner")
                return
            }
            
            // Update user with email info
            user.setActive(injson.getString("userActive") == "Y")
            user.setEmailVerified(true)
            user.setEmail(email)
            def updateTc = perst.perstCreateContainer()
            updateTc.addUpdate(user)
            perst.perstStore(updateTc)
            
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            PerstUser userToUpdate = perst.getByOid(PerstUser, oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setUsername(injson.getString("userName"))
            userToUpdate.setPassword(injson.getString("userPassword"))
            userToUpdate.setActive(injson.getString("userActive") == "Y")
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(userToUpdate)
            perst.perstStore(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void deleteRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            PerstUser userToDelete = perst.getByOid(PerstUser, oid)
            
            if (userToDelete == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            def tc = perst.perstCreateContainer()
            tc.addDelete(userToDelete)
            perst.perstStore(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }
}
