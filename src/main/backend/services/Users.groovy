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
            boolean requireVerification = injson.getBoolean("requireVerification", false)
            
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
            user.setEmail(email)
            
            // Handle email verification
            if (requireVerification) {
                // Generate verification token and don't verify yet
                user.generateVerificationToken()
                user.setEmailVerified(false)
                
                // TODO: Send verification email via EmailService when SMTP is configured
                // EmailService.sendVerificationEmail(email, name, user.getVerificationToken(), baseUrl)
            } else {
                // Auto-verify for development/convenience
                user.setEmailVerified(true)
            }
            
            def updateTc = perst.perstCreateContainer()
            updateTc.addUpdate(user)
            perst.perstStore(updateTc)
            
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

    /**
     * Update user's preferred language
     * Input JSON: { "id": <user_oid>, "preferredLanguage": "en"|"nl"|"de" }
     */
    void updateLanguage(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            String language = injson.getString("preferredLanguage")
            
            // Validate language code
            if (!["en", "nl", "de"].contains(language)) {
                outjson.put("_Success", false)
                outjson.put("error", "Invalid language code. Must be en, nl, or de")
                return
            }
            
            PerstUser userToUpdate = perst.getByOid(PerstUser, oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setPreferredLanguage(language)
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(userToUpdate)
            perst.perstStore(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("preferredLanguage", language)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    /**
     * Verify email with token
     * Input JSON: { "token": "verification-token-here" }
     */
    void verifyEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            String token = injson.getString("token")
            
            if (!token) {
                outjson.put("_Success", false)
                outjson.put("error", "Verification token is required")
                return
            }
            
            // Find user with this verification token
            Collection<PerstUser> users = perst.getAll(PerstUser)
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
                def tc = perst.perstCreateContainer()
                tc.addUpdate(userToVerify)
                perst.perstStore(tc)
                
                outjson.put("_Success", true)
                outjson.put("success", true)
                outjson.put("message", "Email verified successfully")
                outjson.put("username", userToVerify.getUsername())
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
     * Resend verification email
     * Input JSON: { "email": "user@example.com" }
     */
    void resendVerification(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            String email = injson.getString("email")
            
            if (!email) {
                outjson.put("_Success", false)
                outjson.put("error", "Email is required")
                return
            }
            
            // Find user by email
            Collection<PerstUser> users = perst.getAll(PerstUser)
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
            def tc = perst.perstCreateContainer()
            tc.addUpdate(user)
            perst.perstStore(tc)
            
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
}
