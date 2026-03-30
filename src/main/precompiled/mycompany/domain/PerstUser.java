package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import mycompany.domain.Owner;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * PerstUser - User entity stored in Perst OODBMS.
 * 
 * This class represents a generic user for authentication purposes.
 * It extends CVersion to leverage Perst's automatic versioning.
 * 
 * IMPORTANT: Email verification is required before user can login.
 * Sign up → Verify email → Active
 * 
 * Indexing is handled by CDatabase via @Indexable annotations.
 * Use PerstUserManager for all database operations.
 */
public class PerstUser extends CVersion {
    
    @FullTextSearchable
    @Indexable(unique=true)
    private String username;
    
    private String passwordHash;  // SHA256 hash (64 chars)
    
    @Indexable
    private boolean active = true;
    
    @Indexable(unique=true)
    private int userId;       // User ID
    
    @FullTextSearchable
    private String email;
    
    @FullTextSearchable
    private String firstName;
    
    @FullTextSearchable
    private String lastName;
    
    private long createdDate;
    private long lastLoginDate;
    
    @Indexable
    private boolean emailVerified = true;

    private boolean mustChangePassword = false;  // Force password change on first login

    private Owner owner;  // Direct reference to Owner object
    
    private String verificationToken;
    private long verificationExpiresAt;
    
    private String preferredLanguage = "en";  // User's preferred language (en, nl, de)
    
    public PerstUser() {
        this.createdDate = System.currentTimeMillis();
    }
    
    public PerstUser(String username, String password, int id) {
        this();
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.userId = id;
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    public boolean checkPassword(String password) {
        if (passwordHash == null || password == null) {
            return false;
        }
        return passwordHash.equals(hashPassword(password));
    }
    
    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public long getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(long lastLoginDate) { this.lastLoginDate = lastLoginDate; }
    
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    public String getVerificationToken() { return verificationToken; }
    public long getVerificationExpiresAt() { return verificationExpiresAt; }
    
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    
    public void generateVerificationToken() {
        this.verificationToken = UUID.randomUUID().toString();
        this.verificationExpiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
    }
    
    public boolean verifyEmail(String token) {
        if (verificationToken == null || !verificationToken.equals(token)) {
            return false;
        }
        if (System.currentTimeMillis() > verificationExpiresAt) {
            return false;
        }
        this.emailVerified = true;
        this.verificationToken = null;
        return true;
    }
    
    public boolean canLogin() {
        return active && emailVerified;
    }
    
    public Actor getActor() {
        return mycompany.database.ActorManager.getByUserId(userId);
    }

    @Override
    public String toString() {
        return "PerstUser{" +
                "username='" + username + '\'' +
                ", active=" + active +
                ", userId=" + userId +
                ", owner=" + (owner != null ? owner.getName() : "null") +
                '}';
    }
}
