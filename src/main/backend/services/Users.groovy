package services

import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import koo.core.database.StorageManager
import koo.core.user.PerstUser

class Users {

    void updateLanguage(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            String language = injson.getString("preferredLanguage")
            
            if (!["en", "nl", "de"].contains(language)) {
                outjson.put("_Success", false)
                outjson.put("error", "Invalid language code. Must be en, nl, or de")
                return
            }
            
            PerstUser userToUpdate = StorageManager.getByOid(PerstUser.class, oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setPreferredLanguage(language)
            
            def tc = StorageManager.createContainer()
            tc.addUpdate(userToUpdate)
            StorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("preferredLanguage", language)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void verifyEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String token = injson.getString("token")
            String password = injson.has("password") ? injson.getString("password") : null
            
            if (!token) {
                outjson.put("_Success", false)
                outjson.put("error", "Verification token is required")
                return
            }
            
            Collection<PerstUser> users = StorageManager.getAll(PerstUser.class)
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
            
            boolean verified = userToVerify.verifyEmail(token)
            
            if (verified) {
                if (password != null && !password.isEmpty()) {
                    userToVerify.setPassword(password)
                    userToVerify.setActive(true)
                    userToVerify.setMustChangePassword(false)
                }
                
                def tc = StorageManager.createContainer()
                tc.addUpdate(userToVerify)
                StorageManager.store(tc)
                
                def actorName = ""
                if (userToVerify.getAActor() != null) {
                    actorName = userToVerify.getAActor().getName() ?: ""
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

    void getUserByToken(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String token = injson.getString("token")
            
            if (!token) {
                outjson.put("_Success", false)
                outjson.put("error", "Verification token is required")
                return
            }
            
            Collection<PerstUser> users = StorageManager.getAll(PerstUser.class)
            for (PerstUser user : users) {
                if (user.getVerificationToken() != null && user.getVerificationToken().equals(token)) {
                    String actorName = ""
                    if (user.getAActor() != null) {
                        actorName = user.getAActor().getName() ?: ""
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

    void resendVerification(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String email = injson.getString("email")
            
            if (!email) {
                outjson.put("_Success", false)
                outjson.put("error", "Email is required")
                return
            }
            
            Collection<PerstUser> users = StorageManager.getAll(PerstUser.class)
            PerstUser user = null
            
            for (PerstUser u : users) {
                if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                    user = u
                    break
                }
            }
            
            if (user == null) {
                outjson.put("_Success", true)
                outjson.put("message", "If the email exists, a verification link has been sent")
                return
            }
            
            if (user.isEmailVerified()) {
                outjson.put("_Success", false)
                outjson.put("error", "Email is already verified")
                return
            }
            
            user.generateVerificationToken()
            def tc = StorageManager.createContainer()
            tc.addUpdate(user)
            StorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("message", "Verification email sent")
            outjson.put("token", user.getVerificationToken())
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }
}