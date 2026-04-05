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
            
            // Create Owner - Actor constructor auto-creates deactivated PerstUser
            Owner owner = new Owner(name, email, phone, address)
            
            // Configure the auto-created PerstUser with signup credentials
            PerstUser user = owner.getPerstUser()
            user.setUsername(username)
            user.setPassword(password)
            user.setEmail(email)
            user.setActive(true)
            user.setEmailVerified(true)
            
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(owner)
            tc.addInsert(user)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create owner")
                return
            }
            
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
            Owner owner = allOwners.find { it.getPerstUser() != null && it.getPerstUser().getOid() == userId }
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
            data.put("userId", owner.getPerstUser().getOid())
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
            Owner owner = PerstStorageManager.getByOid(Owner.class, ownerId)
            if (owner == null || owner.getPerstUser() == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "User not found")
                return
            }
            PerstUser user = owner.getPerstUser()
            JSONObject data = new JSONObject()
            data.put("id", user.getOid())
            data.put("username", user.getUsername())
            data.put("email", user.getEmail())
            data.put("active", user.isActive())
            data.put("emailVerified", user.isEmailVerified())
            data.put("ownerId", owner.getOid())
            data.put("userId", user.getOid())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
}
