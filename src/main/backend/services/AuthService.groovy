package services

import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import oodb.PerstStorageManager
import mycompany.domain.PerstUser
import mycompany.domain.Owner

/**
 * Authentication service for user signup and owner creation.
 * Creates both User and Owner entities with linking.
 * Uses PerstStorageManager for Perst OODBMS operations.
 */
class AuthService {

    /**
     * Sign up a new user and create corresponding owner.
     * Input JSON: { "username": "...", "password": "...", "email": "...", "name": "...", "phone": "...", "address": "..." }
     */
    void signup(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String username = injson.getString("username")
            String password = injson.getString("password")
            String email = injson.getString("email", "")
            String name = injson.getString("name", username)
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            
            // Check if username already exists
            Collection<PerstUser> existingUsers = PerstStorageManager.getAll(PerstUser.class)
            if (existingUsers.any { it.getUsername() == username }) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Username already exists")
                return
            }
            
            // Generate unique userId
            int maxUserId = existingUsers.collect { it.getUserId() }.max() ?: 0
            int newUserId = maxUserId + 1
            
            // Create Owner
            Owner owner = new Owner(name, email, phone, address)
            def ownerTc = PerstStorageManager.createContainer()
            ownerTc.addInsert(owner)
            if (!PerstStorageManager.store(ownerTc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create owner")
                return
            }
            
            // Create User with ownerId
            PerstUser user = new PerstUser(username, password, newUserId)
            user.setOwner(owner)
            def userTc = PerstStorageManager.createContainer()
            userTc.addInsert(user)
            if (!PerstStorageManager.store(userTc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create user")
                return
            }
            
            // Link owner to user
            owner.setPerstUser(user)
            def linkTc = PerstStorageManager.createContainer()
            linkTc.addUpdate(owner)
            PerstStorageManager.store(linkTc)
            
            // Set email as verified
            user.setEmailVerified(true)
            user.setEmail(email)
            def updateTc = PerstStorageManager.createContainer()
            updateTc.addUpdate(user)
            PerstStorageManager.store(updateTc)
            
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
            long userId = injson.getLong("userId")
            Collection<Owner> allOwners = PerstStorageManager.getAll(Owner.class)
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
            long ownerId = injson.getLong("ownerId")
            Collection<PerstUser> allUsers = PerstStorageManager.getAll(PerstUser.class)
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
