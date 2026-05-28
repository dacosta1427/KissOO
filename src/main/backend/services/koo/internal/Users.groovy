package services.koo.internal

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import koo.core.database.StorageManager
import koo.core.user.PerstUser
import koo.core.actor.Role
import domain.actor.owner.Owner
import koo.core.user.EmailService
import org.kissweb.security.EXTERNAL_CALL

class Users {

    private boolean isFullyActivated(ProcessServlet servlet) {
        def activated = servlet.getUserData("isFullyActivated")
        return activated == true
    }
    
    private boolean isSystemAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) return false
            def actor = pu.getAActor()
            if (actor == null) return false
            def role = actor.getAgreement()?.getRole()
            return role == Role.SUPER_ADMIN
        } catch (Exception e) {
            return false
        }
    }

    private void checkSystemAdmin(ProcessServlet servlet, String operation) {
        if (!isSystemAdmin(servlet)) {
            throw new Exception("System admin access required for: " + operation)
        }
    }
    
    private void requireFullActivation(JSONObject injson, JSONObject outjson, ProcessServlet servlet) {
        if (!isFullyActivated(servlet)) {
            boolean needsPwd = servlet.getUserData("needsPasswordChange") == true
            boolean needsEmail = servlet.getUserData("needsEmailVerification") == true
            outjson.put("_Success", false)
            outjson.put("_ErrorCode", 3)
            outjson.put("_ErrorMessage", "Please complete activation: " + 
                (needsPwd ? "change password" : "") + 
                (needsPwd && needsEmail ? " and " : "") + 
                (needsEmail ? "verify email" : ""))
        }
    }

    @EXTERNAL_CALL
    void getUsers(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            requireFullActivation(injson, outjson, servlet)
            if (outjson.has("_Success") && !outjson.getBoolean("_Success")) {
                return
            }
            
            checkSystemAdmin(servlet, "getUsers")

            Collection<PerstUser> users = StorageManager.getAll(PerstUser.class)
            JSONArray rows = new JSONArray()

            for (PerstUser user : users) {
                JSONObject row = new JSONObject()
                row.put("id", user.getOid())
                row.put("userName", user.getUsername())
                row.put("userPassword", user.getPasswordHash())
                row.put("canLogin", user.isActive())
                row.put("emailVerified", user.isEmailVerified())
                row.put("email", user.getEmail())
                
                if (user.getAActor() != null) {
                    row.put("actorType", user.getAActor().getType())
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

    @EXTERNAL_CALL
    void createUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "createUser")
            
            String userName = injson.getString("userName")
            String password = injson.getString("userPassword")
            String email = injson.getString("email", "")
            String name = injson.getString("name", userName)
            String phone = injson.getString("phone", "")
            String address = injson.getString("address", "")
            boolean requireVerification = injson.getBoolean("requireVerification", false)
            
            Collection<PerstUser> existingUsers = StorageManager.getAll(PerstUser.class)
            if (existingUsers.any { it.getUsername() == userName }) {
                outjson.put("_Success", false)
                outjson.put("error", "Username already exists")
                return
            }
            
            Owner owner = new Owner(name, phone, email, address)
            
            PerstUser user = owner.getPerstUser()
            user.setUsername(userName)
            user.setPassword(password)
            user.setActive(injson.getString("userActive") == "Y")
            user.setEmail(email)
            
            if (requireVerification) {
                user.generateVerificationToken()
                user.setEmailVerified(false)
            } else {
                user.setEmailVerified(true)
            }
            
            def tc = StorageManager.createContainer()
            tc.addInsert(owner)
            tc.addInsert(user)
            if (!StorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create owner")
                return
            }
            
            outjson.put("_Success", true)
            outjson.put("success", true)
            outjson.put("id", user.getOid())
            outjson.put("ownerId", owner.getOid())
            
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

    @EXTERNAL_CALL
    void updateUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "updateUser")
            
            long oid = injson.getLong("id")
            PerstUser userToUpdate = StorageManager.getByOid(PerstUser.class, oid)
            
            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            userToUpdate.setUsername(injson.getString("userName"))
            userToUpdate.setPassword(injson.getString("userPassword"))
            userToUpdate.setActive(injson.getString("userActive") == "Y")
            
            def tc = StorageManager.createContainer()
            tc.addUpdate(userToUpdate)
            StorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    @EXTERNAL_CALL
    void deleteUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "deleteUser")
            
            long oid = injson.getLong("id")
            PerstUser userToDelete = StorageManager.getByOid(PerstUser.class, oid)
            
            if (userToDelete == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            def tc = StorageManager.createContainer()
            tc.addDelete(userToDelete)
            StorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    @EXTERNAL_CALL
    void toggleUserLogin(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "toggleUserLogin")
            
            long oid = injson.getLong("id")
            boolean canLogin = injson.getBoolean("canLogin")
            
            PerstUser user = StorageManager.getByOid(PerstUser.class, oid)
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }
            
            user.setActive(canLogin)
            
            def tc = StorageManager.createContainer()
            tc.addUpdate(user)
            StorageManager.store(tc)
            
            if (canLogin) {
                try {
                    def baseUrl = "http://localhost:5173"
                    def actorName = user.getAActor() != null ? user.getAActor().getName() : user.getUsername()
                    
                    if (user.isEmailVerified()) {
                        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8)
                        user.setPassword(tempPassword)
                        user.setMustChangePassword(true)
                        tc.addUpdate(user)
                        StorageManager.store(tc)
                        
                        EmailService.sendLoginCredentials(
                            user.getEmail(), 
                            actorName, 
                            user.getUsername(), 
                            tempPassword, 
                            baseUrl
                        )
                        outjson.put("temporaryPassword", tempPassword)
                    } else {
                        user.generateVerificationToken()
                        tc.addUpdate(user)
                        StorageManager.store(tc)
                        
                        EmailService.sendVerification(
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