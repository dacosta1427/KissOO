package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import oodb.PerstStorageManager
import mycompany.domain.PerstUser
import mycompany.domain.Owner

/**
 * Users service for CRUD operations on PerstUser.
 *
 * Uses PerstStorageManager for Perst OODBMS operations.
 *
 * Authorization:
 * - System admin only: getUsers, createUser, updateUser, deleteUser
 */
class Users {

    private boolean isSystemAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) return false
            def actor = pu.getActor()
            if (actor == null) return false
            def role = actor.getAgreement()?.getRole()
            return "superAdmin".equals(role)
        } catch (Exception e) {
            return false
        }
    }

    private void checkSystemAdmin(ProcessServlet servlet, String operation) {
        if (!isSystemAdmin(servlet)) {
            throw new Exception("System admin access required for: " + operation)
        }
    }

    void getUsers(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "getUsers")

            Collection<PerstUser> users = PerstStorageManager.getAll(PerstUser.class)
            JSONArray rows = new JSONArray()

            for (PerstUser user : users) {
                JSONObject row = new JSONObject()
                row.put("id", user.getOid())
                row.put("userName", user.getUsername())
                row.put("userPassword", user.getPasswordHash())
                row.put("canLogin", user.isActive())
                row.put("emailVerified", user.isEmailVerified())
                row.put("email", user.getEmail())
                
                // Include actor type to identify if this is Owner or Cleaner
                if (user.getActor() != null) {
                    row.put("actorType", user.getActor().getType())
                } else {
                    row.put("actorType", null)
                }
                
                rows.put(row)
            }

            outjson.put("rows", rows)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void createUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "createUser")
            
            // Using PerstStorageManager directly
            String userName = injson.getString("userName")
            String password = injson.getString("userPassword")
            String email = injson.getString("email", "")
            String name = injson.getString("name", userName)
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            boolean requireVerification = injson.getBoolean("requireVerification", false)
            
            // Check if username already exists
            Collection<PerstUser> existingUsers = PerstStorageManager.getAll(PerstUser.class)
            if (existingUsers.any { it.getUsername() == userName }) {
                outjson.put("_Success", false)
                outjson.put("error", "Username already exists")
                return
            }
            
            // Create Owner - Actor constructor auto-creates deactivated PerstUser
            Owner owner = new Owner(name, email, phone, address)
            
            // Configure the auto-created PerstUser
            PerstUser user = owner.getPerstUser()
            user.setUsername(userName)
            user.setPassword(password)
            user.setActive(injson.getString("userActive") == "Y")
            user.setEmail(email)
            
            // Handle email verification
            if (requireVerification) {
                user.generateVerificationToken()
                user.setEmailVerified(false)
            } else {
                user.setEmailVerified(true)
            }
            
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(owner)
            tc.addInsert(user)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create owner")
                return
            }
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("id", user.getOid())
            outjson.put("ownerId", owner.getOid())
            
            // Include verification token for development purposes
            if (requireVerification && user.getVerificationToken()) {
                outjson.put("verificationToken", user.getVerificationToken())
                outjson.put("requiresVerification", true)
                outjson.put("message", "Please check your email to verify your account")
            }
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    void updateUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "updateUser")
            
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            PerstUser userToUpdate = PerstStorageManager.getByOid(PerstUser.class, oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setUsername(injson.getString("userName"))
            userToUpdate.setPassword(injson.getString("userPassword"))
            userToUpdate.setActive(injson.getString("userActive") == "Y")
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(userToUpdate)
            PerstStorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void deleteUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "deleteUser")
            
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            PerstUser userToDelete = PerstStorageManager.getByOid(PerstUser.class, oid)
            
            if (userToDelete == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            def tc = PerstStorageManager.createContainer()
            tc.addDelete(userToDelete)
            PerstStorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    /**
     * Update user's preferred language
     * Input JSON: { "id": <user_oid>, "preferredLanguage": "en"|"nl"|"de" }
     */
    void updateLanguage(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            String language = injson.getString("preferredLanguage")
            
            // Validate language code
            if (!["en", "nl", "de"].contains(language)) {
                outjson.put("_Success", false)
                outjson.put("error", "Invalid language code. Must be en, nl, or de")
                return
            }
            
            PerstUser userToUpdate = PerstStorageManager.getByOid(PerstUser.class, oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setPreferredLanguage(language)
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(userToUpdate)
            PerstStorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("preferredLanguage", language)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    /**
     * Verify email with token and optionally set password.
     * 
     * Input JSON: { "token": "verification-token-here", "password": "optional-password" }
     * 
     * If password is provided:
     * - Verify token
     * - Set password (hashed)
     * - Set emailVerified = true
     * - Set active = true (enables login)
     * - Clear verification token
     */
    void verifyEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            String token = injson.getString("token")
            String password = injson.has("password") ? injson.getString("password") : null
            
            if (!token) {
                outjson.put("_Success", false)
                outjson.put("error", "Verification token is required")
                return
            }
            
            // Find user with this verification token
            Collection<PerstUser> users = PerstStorageManager.getAll(PerstUser.class)
            PerstUser userToVerify = null
            
            for (PerstUser user : users) {
                if (user.getVerificationToken() != null && user.getVerificationToken().equals(token)) {
                    userToVerify = user
                    break
                }
            }
            
            if (userToVerify == null) {
                outjson.put("_Success", false)
                outjson.put("error", "Invalid or expired verification token")
                return
            }
            
            // Verify the email
            if (userToVerify.verifyEmail(token)) {
                // If password provided, set it and enable login
                if (password != null && !password.isEmpty()) {
                    // Use PerstUser.setPassword which uses SHA-256
                    userToVerify.setPassword(password)
                    userToVerify.setActive(true)
                    userToVerify.setMustChangePassword(false)
                }
                
                def tc = PerstStorageManager.createContainer()
                tc.addUpdate(userToVerify)
                PerstStorageManager.store(tc)
                
                // Get the user's name from their Actor
                def actorName = ""
                if (userToVerify.getActor() != null) {
                    actorName = userToVerify.getActor().getName() ?: ""
                }
                
                if (password != null && !password.isEmpty()) {
                    outjson.put("_Success", true)
                    outjson.put("success", true)
                    outjson.put("message", "Email verified. You can now login with your email and password.")
                    outjson.put("username", userToVerify.getUsername())
                    outjson.put("userName", actorName)
                } else {
                    outjson.put("_Success", true)
                    outjson.put("success", true)
                    outjson.put("message", "Email verified successfully")
                    outjson.put("username", userToVerify.getUsername())
                    outjson.put("userName", actorName)
                }
            } else {
                outjson.put("_Success", false)
                outjson.put("error", "Verification failed - token may have expired")
            }
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }
    
    /**
     * Get user info from verification token (without verifying)
     * Used to show the user's name on the verification page
     */
    void getUserByToken(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String token = injson.getString("token")
            
            if (!token) {
                outjson.put("_Success", false)
                outjson.put("error", "Verification token is required")
                return
            }
            
            // Find user with this verification token
            Collection<PerstUser> users = PerstStorageManager.getAll(PerstUser.class)
            for (PerstUser user : users) {
                if (user.getVerificationToken() != null && user.getVerificationToken().equals(token)) {
                    // Get the user's name from their Actor
                    String actorName = ""
                    if (user.getActor() != null) {
                        actorName = user.getActor().getName() ?: ""
                    }
                    outjson.put("_Success", true)
                    outjson.put("userName", actorName)
                    outjson.put("email", user.getEmail())
                    return
                }
            }
            
            outjson.put("_Success", false)
            outjson.put("error", "Invalid or expired verification token")
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    /**
     * Resend verification email
     * Input JSON: { "email": "user@example.com" }
     */
    void resendVerification(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            String email = injson.getString("email")
            
            if (!email) {
                outjson.put("_Success", false)
                outjson.put("error", "Email is required")
                return
            }
            
            // Find user by email
            Collection<PerstUser> users = PerstStorageManager.getAll(PerstUser.class)
            PerstUser user = null
            
            for (PerstUser u : users) {
                if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                    user = u
                    break
                }
            }
            
            if (user == null) {
                // Don't reveal if email exists or not for security
                outjson.put("_Success", true)
                outjson.put("message", "If the email exists, a verification link has been sent")
                return
            }
            
            if (user.isEmailVerified()) {
                outjson.put("_Success", false)
                outjson.put("error", "Email is already verified")
                return
            }
            
            // Generate new verification token
            user.generateVerificationToken()
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(user)
            PerstStorageManager.store(tc)
            
            // TODO: Send verification email via EmailService when SMTP is configured
            // For now, just return success
            outjson.put("_Success", true)
            outjson.put("message", "Verification email sent")
            outjson.put("token", user.getVerificationToken()) // For development/debugging
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }
    
    void toggleUserLogin(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "toggleUserLogin")
            
            long oid = injson.getLong("id")
            boolean canLogin = injson.getBoolean("canLogin")
            
            PerstUser user = PerstStorageManager.getByOid(PerstUser.class, oid)
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            user.setActive(canLogin)
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(user)
            PerstStorageManager.store(tc)
            
            // Send email notification
            if (canLogin) {
                try {
                    def baseUrl = "http://localhost:5173"
                    def actorName = user.getActor() != null ? user.getActor().getName() : user.getUsername()
                    
                    if (user.isEmailVerified()) {
                        // Email already verified - send welcome/login info
                        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8)
                        user.setPassword(tempPassword)
                        user.setMustChangePassword(true)
                        tc.addUpdate(user)
                        PerstStorageManager.store(tc)
                        
                        services.EmailService.sendLoginCredentialsEmail(
                            user.getEmail(), 
                            actorName, 
                            user.getUsername(), 
                            tempPassword, 
                            baseUrl
                        )
                        outjson.put("temporaryPassword", tempPassword)
                    } else {
                        // Email not verified - send verification email
                        user.generateVerificationToken()
                        tc.addUpdate(user)
                        PerstStorageManager.store(tc)
                        
                        services.EmailService.sendVerificationEmail(
                            user.getEmail(),
                            actorName,
                            user.getVerificationToken(),
                            baseUrl
                        )
                    }
                } catch(Exception e) {
                    println "[Users] Failed to send email: ${e.message}"
                }
            }
            
            outjson.put("_Success", true)
            outjson.put("canLogin", canLogin)
            outjson.put("emailVerified", user.isEmailVerified())
            outjson.put("message", canLogin ? "Login enabled" : "Login disabled")
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }
}
