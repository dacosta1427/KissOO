package domain;

import org.garret.perst.continuous.CVersion;

/**
 * Actor - Represents an entity that can own and perform actions on resources.
 * This is a generic entity - extend it for domain-specific behavior.
 */
public class Actor extends CVersion {
    
    private String uuid;
    private String name;
    private String type;  // Generic type field - customize for your domain
    private boolean active = true;
    private long createdDate;
    private transient PerstUser perstUser;  // Linked PerstUser for authentication
    
    // Static in-memory indexes
    private static java.util.Map<Integer, Actor> userIdIndex = new java.util.HashMap<>();
    private static java.util.Map<String, Actor> uuidIndex = new java.util.HashMap<>();
    
    public Actor() {
        this.createdDate = System.currentTimeMillis();
    }
    
    public Actor(String name, String type) {
        this();
        this.name = name;
        this.type = type;
        this.uuid = java.util.UUID.randomUUID().toString();
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
    
    public PerstUser getPerstUser() { return perstUser; }
    public void setPerstUser(PerstUser perstUser) { this.perstUser = perstUser; }
    
    public java.util.Map<String, Object> toJSON() {
        java.util.Map<String, Object> json = new java.util.HashMap<>();
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("type", type);
        json.put("active", active);
        json.put("createdDate", createdDate);
        return json;
    }
    
    @Override
    public String toString() {
        return "Actor{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", active=" + active +
                '}';
    }
}
