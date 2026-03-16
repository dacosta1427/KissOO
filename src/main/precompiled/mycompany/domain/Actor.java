package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import java.util.HashSet;
import java.util.Set;

/**
 * Actor - Represents an entity that can own and perform actions on resources.
 * 
 * IMPORTANT: Every Actor MUST have an Agreement. Without an Agreement,
 * the Actor CANNOT exist. This is enforced at construction time.
 * 
 * This is a generic entity - extend it for domain-specific behavior.
 */
public class Actor extends CVersion {
    
    private String uuid;
    private String name;
    private String type;  // Generic type field - customize for your domain
    private boolean active = true;
    private long createdDate;
    private int userId = 0;  // Link to SQL user ID
    private transient PerstUser perstUser;  // Linked PerstUser for authentication
    private Agreement agreement;  // MANDATORY - Authorization contract
    
    // Static in-memory indexes
    private static java.util.Map<Integer, Actor> userIdIndex = new java.util.HashMap<>();
    private static java.util.Map<String, Actor> uuidIndex = new java.util.HashMap<>();
    
    /**
     * Create Actor with Agreement - MANDATORY
     */
    public Actor(String name, String type, Agreement agreement) {
        this.createdDate = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        this.uuid = java.util.UUID.randomUUID().toString();
        
        if (agreement == null) {
            throw new IllegalArgumentException("Actor MUST have an Agreement");
        }
        this.agreement = agreement;
    }
    
    // Static finder methods
    public static Actor findByUserId(int userId) {
        return userIdIndex.get(userId);
    }
    
    public static Actor findByUuid(String uuid) {
        return uuidIndex.get(uuid);
    }
    
    public void index() {
        if (uuid != null && !uuid.isEmpty()) {
            uuidIndex.put(uuid, this);
        }
        if (perstUser != null && perstUser.getUserId() > 0) {
            userIdIndex.put(perstUser.getUserId(), this);
        }
    }
    
    public void removeIndex() {
        uuidIndex.remove(uuid);
        if (perstUser != null && perstUser.getUserId() > 0) {
            userIdIndex.remove(perstUser.getUserId());
        }
    }
    
    // Manual getters/setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public PerstUser getPerstUser() { return perstUser; }
    public void setPerstUser(PerstUser perstUser) { this.perstUser = perstUser; }
    
    /**
     * Get the Actor's Agreement (MANDATORY).
     * Returns null if no agreement - which means no operations are permitted.
     */
    public Agreement getAgreement() { return agreement; }
    
    /**
     * Set the Actor's Agreement.
     */
    public void setAgreement(Agreement agreement) { this.agreement = agreement; }
    
    // ========== Convenience Methods for Groups ==========
    
    /**
     * Add a group to this actor's agreement
     */
    public void addToGroup(Group group) {
        if (agreement == null) {
            agreement = new Agreement();
        }
        agreement.addGroup(group);
    }
    
    /**
     * Remove a group from this actor's agreement
     */
    public void removeFromGroup(Group group) {
        if (agreement != null) {
            agreement.removeGroup(group);
        }
    }
    
    /**
     * Check if actor belongs to a group
     */
    public boolean belongsToGroup(String groupName) {
        return agreement != null && agreement.hasGroup(groupName);
    }
    
    public java.util.Map<String, Object> toJSON() {
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("type", type);
        json.put("active", active);
        json.put("createdDate", createdDate);
        json.put("hasAgreement", agreement != null);
        return json;
    }
    
    @Override
    public String toString() {
        return "Actor{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", active=" + active +
                ", hasAgreement=" + (agreement != null) +
                '}';
    }
}
