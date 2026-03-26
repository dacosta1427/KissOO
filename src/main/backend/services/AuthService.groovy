package services

import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.MainServlet
import mycompany.domain.PerstUser
import mycompany.domain.Owner
import oodb.PerstConnection

/**
 * Authentication service for user signup and owner creation.
 * Creates both User and Owner entities with linking.
 * Uses PerstConnection for Perst OODBMS operations.
 */
class AuthService {

    /**
     * Get PerstConnection from environment if available.
     */
    private PerstConnection getPerst() {
        return (PerstConnection) MainServlet.getEnvironment("PerstConnection")
    }

    /**
     * Sign up a new user and create corresponding owner.
     * Input JSON: { "username": "...", "password": "...", "email": "...", "name": "...", "phone": "...", "address": "..." }
     */
    void signup(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            String username = injson.getString("username")
            String password = injson.getString("password")
            String email = injson.getString("email", "")
            String name = injson.getString("name", username)
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            
            // Check if username already exists
            Collection<PerstUser> existingUsers = perst.getAll(PerstUser)
            if (existingUsers.any { it.getUsername() == username }) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Username already exists")
                return
            }
            
            // Generate unique userId
            int maxUserId = existingUsers.collect { it.getUserId() }.max() ?: 0
            int newUserId = maxUserId + 1
            
            // Create Owner
            Owner owner = new Owner(name, email, phone, address, true)
            def ownerTc = perst.perstCreateContainer()
            ownerTc.addInsert(owner)
            if (!perst.perstStore(ownerTc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create owner")
                return
            }
            
            // Create User with ownerId
            PerstUser user = new PerstUser(username, password, newUserId, owner.getOid())
            def userTc = perst.perstCreateContainer()
            userTc.addInsert(user)
            if (!perst.perstStore(userTc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create user")
                return
            }
            
            // Link owner to user
            owner.setPerstUser(user)
            def linkTc = perst.perstCreateContainer()
            linkTc.addUpdate(owner)
            perst.perstStore(linkTc)
            
            // Set email as verified
            user.setEmailVerified(true)
            user.setEmail(email)
            def updateTc = perst.perstCreateContainer()
            updateTc.addUpdate(user)
            perst.perstStore(updateTc)
            
            JSONObject result = new JSONObject()
            result.put("userId", user.getOid())
            result.put("ownerId", owner.getOid())
            result.put("username", username)
            result.put("email", email)
            result.put("success", true)
            result.put("_Success", true)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    /**
     * Get owner by user ID
     */
    void getOwnerByUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long userId = injson.getLong("userId")
            Collection<Owner> allOwners = perst.getAll(Owner)
            Owner owner = allOwners.find { it.getUserId() == userId }
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", owner.getOid())
            data.put("name", owner.getName())
            data.put("email", owner.getEmail())
            data.put("phone", owner.getPhone())
            data.put("address", owner.getAddress())
            data.put("active", owner.isActive())
            data.put("userId", owner.getUserId())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    /**
     * Get user by owner ID
     */
    void getUserByOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long ownerId = injson.getLong("ownerId")
            Collection<PerstUser> allUsers = perst.getAll(PerstUser)
            PerstUser user = allUsers.find { it.getOwnerId() == ownerId }
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "User not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", user.getOid())
            data.put("username", user.getUsername())
            data.put("email", user.getEmail())
            data.put("active", user.isActive())
            data.put("emailVerified", user.isEmailVerified())
            data.put("ownerId", user.getOwnerId())
            data.put("userId", user.getUserId())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
}
