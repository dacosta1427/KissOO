package koo.services.auth;

import koo.core.database.StorageManager;
import koo.core.user.PerstUser;
import org.garret.perst.continuous.TransactionContainer;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import domain.actor.owner.Owner;
import java.util.Collection;

public class AuthService {

    private static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }
        return null;
    }

    public void signup(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String username = injson.getString("username");
            String password = injson.getString("password");
            String email = injson.has("email") ? injson.getString("email") : "";
            String name = injson.has("name") ? injson.getString("name") : username;
            String phone = injson.has("phone") ? injson.getString("phone") : "";
            String address = injson.has("address") ? injson.getString("address") : "";

            String passwordError = validatePassword(password);
            if (passwordError != null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", passwordError);
                return;
            }

            Collection<PerstUser> existingUsers = StorageManager.getAll(PerstUser.class);
            for (PerstUser u : existingUsers) {
                if (u.getUsername() != null && u.getUsername().equals(username)) {
                    outjson.put("_Success", false);
                    outjson.put("_ErrorMessage", "Username already exists");
                    return;
                }
            }

            Owner owner = new Owner(name, phone, email, true);
            owner.setAddress(address);

            PerstUser user = owner.getPerstUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setActive(true);
            user.setEmailVerified(true);
            user.setMustChangePassword(true);

            TransactionContainer tc = StorageManager.createContainer();
            tc.addInsert(owner);
            tc.addInsert(user);
            if (!StorageManager.store(tc)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to create owner");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("userId", user.getOid());
            result.put("ownerId", owner.getOid());
            result.put("username", username);
            result.put("email", email);
            result.put("success", true);
            result.put("_Success", true);
            outjson.put("data", result);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getOwnerByUser(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long userId = injson.getLong("userId");
            Collection<Owner> allOwners = StorageManager.getAll(Owner.class);
            Owner foundOwner = null;
            for (Owner o : allOwners) {
                if (o.getPerstUser() != null && o.getPerstUser().getOid() == userId) {
                    foundOwner = o;
                    break;
                }
            }
            if (foundOwner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }
            JSONObject data = new JSONObject();
            data.put("id", foundOwner.getOid());
            data.put("name", foundOwner.getName());
            data.put("email", foundOwner.getEmail());
            data.put("phone", foundOwner.getPhone());
            data.put("address", foundOwner.getAddress());
            data.put("active", foundOwner.isActive());
            data.put("userId", foundOwner.getPerstUser().getOid());
            outjson.put("data", data);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getUserByOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long ownerId = injson.getLong("ownerId");
            Owner owner = StorageManager.getByOid(Owner.class, ownerId);
            if (owner == null || owner.getPerstUser() == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "User not found");
                return;
            }
            PerstUser user = owner.getPerstUser();
            JSONObject data = new JSONObject();
            data.put("id", user.getOid());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("active", user.isActive());
            data.put("emailVerified", user.isEmailVerified());
            data.put("ownerId", owner.getOid());
            data.put("userId", user.getOid());
            outjson.put("data", data);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void changePassword(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Not authenticated");
                return;
            }

            String currentPassword = injson.getString("currentPassword");
            String newPassword = injson.getString("newPassword");

            if (!pu.checkPassword(currentPassword)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Current password is incorrect");
                return;
            }

            String passwordError = validatePassword(newPassword);
            if (passwordError != null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", passwordError);
                return;
            }

            pu.setPassword(newPassword);
            pu.setMustChangePassword(false);

            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(pu);
            if (!StorageManager.store(tc)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update password");
                return;
            }

            outjson.put("_Success", true);
            outjson.put("message", "Password changed successfully");

            if (pu.isEmailVerified()) {
                outjson.put("fullyActivated", true);
            }

        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void sendVerificationEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Not authenticated");
                return;
            }

            if (pu.isEmailVerified()) {
                outjson.put("_Success", true);
                outjson.put("message", "Email already verified");
                return;
            }

            pu.generateVerificationToken();

            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(pu);
            if (!StorageManager.store(tc)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to generate verification token");
                return;
            }

            String frontendUrl = "http://localhost:5173";
            try {
                frontendUrl = "http://localhost:5173";
                if (frontendUrl == null || frontendUrl.isEmpty()) {
                    frontendUrl = "http://localhost:5173";
                }
            } catch (Exception ignored) {}

            String verifyLink = frontendUrl + "/verify?token=" + pu.getVerificationToken();

            boolean emailSent = EmailService.send(
                pu.getEmail() != null ? pu.getEmail() : pu.getUsername(),
                pu.getFirstName() != null ? pu.getFirstName() : pu.getUsername(),
                "Verify your email - KissOO",
                "Click the link to verify your email: " + verifyLink
            );

            if (emailSent) {
                outjson.put("_Success", true);
                outjson.put("message", "Verification email sent to " + pu.getEmail());
            } else {
                System.out.println("[AuthService] Verification token for " + pu.getUsername() + ": " + pu.getVerificationToken());
                outjson.put("_Success", true);
                outjson.put("message", "Verification email sent (check console for token)");
                outjson.put("verificationToken", pu.getVerificationToken());
            }

        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void verifyEmail(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Not authenticated");
                return;
            }

            String token = injson.getString("token");

            if (pu.verifyEmail(token)) {
            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(pu);
            StorageManager.store(tc);

                outjson.put("_Success", true);
                outjson.put("message", "Email verified successfully");

                if (!pu.isMustChangePassword()) {
                    outjson.put("fullyActivated", true);
                }
            } else {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Invalid or expired verification token");
            }

        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getActivationStatus(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Not authenticated");
                return;
            }

            boolean fullyActivated = !pu.isMustChangePassword() && pu.isEmailVerified();

            outjson.put("_Success", true);
            outjson.put("fullyActivated", fullyActivated);
            outjson.put("needsPasswordChange", pu.isMustChangePassword());
            outjson.put("needsEmailVerification", !pu.isEmailVerified());

        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }
}