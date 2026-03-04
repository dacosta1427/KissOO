package domain;

import org.garret.perst.continuous.CVersion;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * PerstUser - User entity stored in Perst OODBMS.
 * 
 * This class represents a generic user for authentication purposes.
 * It extends CVersion to leverage Perst's automatic versioning.
 * Extend this class for domain-specific user attributes.
 */
public class PerstUser extends CVersion {
    
    private String username;
    private String passwordHash;  // SHA256 hash (64 chars)
    private boolean active = true;
    private int userId;       // User ID
    private String email;
    private String firstName;
    private String lastName;
    private long createdDate;
    private long lastLoginDate;
    
    // Static in-memory indexes
    private static java.util.Map<Integer, PerstUser> idIndex = new java.util.HashMap<>();
    private static java.util.Map<String, PerstUser> usernameIndex = new java.util.HashMap<>();
    
    public PerstUser() {
        this.createdDate = System.currentTimeMillis();
    }
    
    public PerstUser(String username, String password, int id) {
        this();
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.userId = id;
    }
    
    // Static finder methods
    public static PerstUser get(int userId) {
        return idIndex.get(userId);
    }
    
    public static PerstUser getByUsername(String username) {
        return usernameIndex.get(username);
    }
    
    public void index() {
        if (username != null && !username.isEmpty()) {
            usernameIndex.put(username, this);
        }
        if (userId > 0) {
            idIndex.put(userId, this);
        }
    }
    
    public void removeIndex() {
        usernameIndex.remove(username);
        if (userId > 0) {
            idIndex.remove(userId);
        }
    }
    
    // Password handling
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
    
    // Getters and Setters
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
    
    // Get the Actor associated with this user
    public Actor getActor() {
        return Actor.findByUserId(userId);
    }
    
    public java.util.Map<String, Object> toJSON() {
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        json.put("username", username);
        json.put("active", active);
        json.put("userId", userId);
        json.put("email", email);
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("createdDate", createdDate);
        json.put("lastLoginDate", lastLoginDate);
        return json;
    }
    
    @Override
    public String toString() {
        return "PerstUser{" +
                "username='" + username + '\'' +
                ", active=" + active +
                ", userId=" + userId +
                '}';
    }
}
