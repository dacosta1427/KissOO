package services

import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import mycompany.database.PerstUserManager
import mycompany.database.OwnerManager
import mycompany.domain.PerstUser
import mycompany.domain.Owner

/**
 * Authentication service for user signup and owner creation.
 * Creates both User and Owner entities with linking.
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
            String name = injson.getString("name", username) // default to username
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            
            // Check if username already exists
            if (PerstUserManager.getByKey(username) != null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Username already exists")
                return
            }
            
            // Generate unique userId
            Collection<PerstUser> existingUsers = PerstUserManager.getAll()
            int maxUserId = 0
            for (PerstUser u : existingUsers) {
                if (u.getUserId() > maxUserId) {
                    maxUserId = u.getUserId()
                }
            }
            int newUserId = maxUserId + 1
            
            // Generate unique ownerId
            Collection<Owner> existingOwners = OwnerManager.getAll()
            long maxOwnerId = 0
            for (Owner o : existingOwners) {
                if (o.getOid() > maxOwnerId) {
                    maxOwnerId = o.getOid()
                }
            }
            long newOwnerId = maxOwnerId + 1
            
            // Create Owner first (need userId for linking, but we don't have it yet)
            Owner owner = OwnerManager.create(name, email, phone, address, true)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create owner")
                return
            }
            
            // Create User with ownerId
            PerstUser user = PerstUserManager.create(username, password, newUserId, owner.getOid())
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create user")
                return
            }
            
            // Link owner to user
            owner.setUserId(user.getOid())
            OwnerManager.update(owner)
            
            // Set email as verified for now (can be changed later for email verification workflow)
            user.setEmailVerified(true)
            user.setEmail(email)
            PerstUserManager.update(user)
            
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
            Owner owner = OwnerManager.getByUserId(userId)
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
            PerstUser user = PerstUserManager.getByOwnerId(ownerId)
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
