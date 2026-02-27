package gfe;

import org.garret.perst.continuous.CVersion;

/**
 * PerstUser - User entity stored in Perst OODBMS.
 * 
 * This class represents a user for authentication purposes.
 * It extends CVersion to leverage Perst's automatic versioning.
 */
public class PerstUser extends CVersion {
    
    private String username;
    private String password;  // Plain text or SHA256 hash (64 chars)
    private boolean active = true;
    private int userId;       // User ID (renamed to avoid conflict with CVersion)
    private String email;
    private long createdDate;
    private long lastLoginDate;
    
    public PerstUser() {
        this.createdDate = System.currentTimeMillis();
    }
    
    public PerstUser(String username, String password, int id) {
        this();
        this.username = username;
        this.password = password;
        this.userId = id;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public long getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
    
    public long getLastLoginDate() {
        return lastLoginDate;
    }
    
    public void setLastLoginDate(long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    public java.util.Map<String, Object> toJSON() {
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        json.put("username", username);
        json.put("active", active);
        json.put("userId", userId);
        json.put("email", email);
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
