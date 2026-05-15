package services

import org.kissweb.restServer.MainServlet
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.garret.perst.dbmanager.UnifiedDBManager
import org.garret.perst.continuous.TransactionContainer
import koo.core.user.PerstUser
import koo.core.user.PerstUserManager
import koo.core.actor.AActor
import domain.actor.owner.Owner
import domain.actor.cleaner.Cleaner

class Users {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager")
    }

    @SuppressWarnings("unchecked")
    private List<PerstUser> getAllUsers(ProcessServlet servlet) {
        def udbm = getUdbm(servlet)
        if (udbm == null) return []
        def results = udbm.getObjects(PerstUser.class)
        def list = []
        if (results != null) {
            while (results.hasNext()) {
                list.add(results.next())
            }
        }
        return list
    }

    void updateLanguage(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet)
            long oid = injson.getLong("oid")
            String language = injson.getString("preferredLanguage")

            if (!["en", "nl", "de"].contains(language)) {
                outjson.put("_Success", false)
                outjson.put("error", "Invalid language code. Must be en, nl, or de")
                return
            }

            PerstUser userToUpdate = udbm.getByOid(oid, PerstUser.class)?.getObject()

            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }

            userToUpdate.setPreferredLanguage(language)

            TransactionContainer tc = udbm.createContainer()
            tc.addUpdate(userToUpdate)
            udbm.store(tc)

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

            List<PerstUser> users = getAllUsers(servlet)
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

                UnifiedDBManager udbm = getUdbm(servlet)
                TransactionContainer tc = udbm.createContainer()
                tc.addUpdate(userToVerify)
                udbm.store(tc)

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

            List<PerstUser> users = getAllUsers(servlet)
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
            UnifiedDBManager udbm = getUdbm(servlet)
            String email = injson.getString("email")

            if (!email) {
                outjson.put("_Success", false)
                outjson.put("error", "Email is required")
                return
            }

            List<PerstUser> users = getAllUsers(servlet)
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
            TransactionContainer tc = udbm.createContainer()
            tc.addUpdate(user)
            udbm.store(tc)

            outjson.put("_Success", true)
            outjson.put("message", "Verification email sent")
            outjson.put("token", user.getVerificationToken())
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    void getUsers(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<PerstUser> users = PerstUserManager.getAll()
            JSONArray rows = new JSONArray()

            for (PerstUser user : users) {
                JSONObject row = new JSONObject()
                row.put("oid", user.getOid())
                row.put("userName", user.getUsername())
                row.put("userPassword", user.getPasswordHash())
                row.put("userActive", user.isActive() ? "Y" : "N")
                row.put("emailVerified", user.isEmailVerified())
                row.put("email", user.getEmail())
                row.put("canLogin", user.isActive() && user.isEmailVerified())

                // Get actor type
                AActor actor = user.getAActor()
                if (actor instanceof Owner) {
                    row.put("actorType", "Owner")
                } else if (actor instanceof Cleaner) {
                    row.put("actorType", "Cleaner")
                } else {
                    row.put("actorType", "")
                }

                rows.put(row)
            }

            outjson.put("_Success", true)
            outjson.put("rows", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    void createUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String userName = injson.getString("userName")
            String password = injson.getString("userPassword")
            String userActive = injson.has("userActive") ? injson.getString("userActive") : "Y"

            PerstUser user = PerstUserManager.create(userName, password, 0)
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("error", "Failed to create user")
                return
            }
            user.setActive(userActive == "Y")
            user.setEmailVerified(true)
            PerstUserManager.update(user)

            outjson.put("_Success", true)
            outjson.put("oid", user.getOid())
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    void deleteUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid")
            PerstUser userToDelete = PerstUserManager.getByOid(oid)

            if (userToDelete == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }

            PerstUserManager.delete(userToDelete)

            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    void updateUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid")
            PerstUser userToUpdate = PerstUserManager.getByOid(oid)

            if (userToUpdate == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }

            userToUpdate.setUsername(injson.getString("userName"))
            if (injson.has("userPassword")) {
                userToUpdate.setPassword(injson.getString("userPassword"))
            }
            if (injson.has("userActive")) {
                userToUpdate.setActive(injson.getString("userActive") == "Y")
            }

            PerstUserManager.update(userToUpdate)

            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }

    void toggleUserLogin(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid")
            boolean canLogin = injson.getBoolean("canLogin")

            PerstUser user = PerstUserManager.getByOid(oid)
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("error", "User not found")
                return
            }

            if (canLogin) {
                user.setActive(true)
                user.setEmailVerified(true)
            } else {
                user.setActive(false)
            }
            PerstUserManager.update(user)

            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }
}