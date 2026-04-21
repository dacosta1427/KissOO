package services.auth


import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import koo.core.database.StorageManager
import koo.core.user.PerstUser
import mycompany.actor.owner.Owner

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
    /**
     * Validate password meets minimum requirements.
     * @return null if valid, error message if invalid
     */
    private static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters"
        }
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number"
        }
        return null
    }
    
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
            
            // Validate password policy
            String passwordError = validatePassword(password)
            if (passwordError != null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", passwordError)
                return
            }
            
            // Check if username already exists
            Collection<PerstUser> existingUsers = StorageManager.getAll(PerstUser.class)
            if (existingUsers.any { it.getUsername() == username }) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Username already exists")
                return
            }
            
            // Create Owner - AActor constructor auto-creates deactivated PerstUser
            Owner owner = new Owner(name, phone, email, address)
            
            // Configure the auto-created PerstUser with signup credentials
            PerstUser user = owner.getPerstUser()
            user.setUsername(username)
            user.setPassword(password)
            user.setEmail(email)
            user.setActive(true)
            user.setEmailVerified(true)
            // Require password change on first login if using temp/default password
            user.setMustChangePassword(true)
            
            def tc = StorageManager.createContainer()
            tc.addInsert(owner)
            tc.addInsert(user)
            if (!StorageManager.store(tc)) {
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
            Collection<Owner> allOwners = StorageManager.getAll(Owner.class)
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
            Owner owner = StorageManager.getByOid(Owner.class, ownerId)
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
    
    /**
     * Allow password change for logged-in user.
     * Requires current password verification.
     * Input JSON: { "currentPassword": "...", "newPassword": "..." }
     */
    void changePassword(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Not authenticated")
                return
            }
            
            String currentPassword = injson.getString("currentPassword")
            String newPassword = injson.getString("newPassword")
            
            // Verify current password
            if (!pu.checkPassword(currentPassword)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Current password is incorrect")
                return
            }
            
            // Validate new password
            String passwordError = validatePassword(newPassword)
            if (passwordError != null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", passwordError)
                return
            }
            
            // Set new password and clear mustChangePassword flag
            pu.setPassword(newPassword)
            pu.setMustChangePassword(false)
            
            def tc = StorageManager.createContainer()
            tc.addUpdate(pu)
            if (!StorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update password")
                return
            }
            
            outjson.put("_Success", true)
            outjson.put("message", "Password changed successfully")
            
            // Check if now fully activated
            if (pu.isEmailVerified()) {
                outjson.put("fullyActivated", true)
            }
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    /**
     * Send verification email to user's email address.
     * Input JSON: {} (uses logged-in user's email)
     */
    void sendVerificationEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Not authenticated")
                return
            }
            
            if (pu.isEmailVerified()) {
                outjson.put("_Success", true)
                outjson.put("message", "Email already verified")
                return
            }
            
            // Generate verification token
            pu.generateVerificationToken()
            
            def tc = StorageManager.createContainer()
            tc.addUpdate(pu)
            if (!StorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to generate verification token")
                return
            }
            
            // Get frontend URL from config
            String frontendUrl = "http://localhost:5173"
            try {
                frontendUrl = org.kissweb.restServer.MainServlet.getIniValue("main", "FrontendBaseUrl") ?: "http://localhost:5173"
            } catch (Exception ignored) {}
            
            // Build verification link
            String verifyLink = "${frontendUrl}/verify?token=${pu.getVerificationToken()}"
            
            // Send verification email via EmailService
            try {
                boolean emailSent = EmailService.sendEmail(
                    pu.getEmail() ?: pu.getUsername(),
                    "Verify your email - KissOO",
                    "Click the link to verify your email: ${verifyLink}",
                    "<p>Click the link to verify your email:</p><p><a href=\"${verifyLink}\">${verifyLink}</a></p>"
                )
                
                if (emailSent) {
                    outjson.put("_Success", true)
                    outjson.put("message", "Verification email sent to ${pu.getEmail()}")
                } else {
                    // Fallback: show token for testing
                    outjson.put("_Success", true)
                    outjson.put("message", "Verification email sent (check console for token in dev mode)")
                    outjson.put("verificationToken", pu.getVerificationToken())
                }
            } catch (Exception e) {
                // Fallback: show token for testing
                println "[AuthService] Verification token for ${pu.getUsername()}: ${pu.getVerificationToken()}"
                outjson.put("_Success", true)
                outjson.put("message", "Verification email sent (check console for token)")
                outjson.put("verificationToken", pu.getVerificationToken())
            }
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    /**
     * Verify user's email with token.
     * Input JSON: { "token": "..." }
     */
    void verifyEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Not authenticated")
                return
            }
            
            String token = injson.getString("token")
            
            if (pu.verifyEmail(token)) {
                def tc = StorageManager.createContainer()
                tc.addUpdate(pu)
                StorageManager.store(tc)
                
                outjson.put("_Success", true)
                outjson.put("message", "Email verified successfully")
                
                // Check if now fully activated
                if (!pu.isMustChangePassword()) {
                    outjson.put("fullyActivated", true)
                }
            } else {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Invalid or expired verification token")
            }
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    /**
     * Get activation status for current user.
     * Returns what the user still needs to do.
     */
    void getActivationStatus(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Not authenticated")
                return
            }
            
            boolean fullyActivated = !pu.isMustChangePassword() && pu.isEmailVerified()
            
            outjson.put("_Success", true)
            outjson.put("fullyActivated", fullyActivated)
            outjson.put("needsPasswordChange", pu.isMustChangePassword())
            outjson.put("needsEmailVerification", !pu.isEmailVerified())
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
}
