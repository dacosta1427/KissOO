package gfe;

import org.garret.perst.continuous.CVersion;

/**
 * Actor - Represents an entity that can own and perform actions on OOVs.
 * This is a sample domain class to demonstrate Perst integration.
 */
public class Actor extends CVersion {
    
    private String uuid;
    private String name;
    private String type;  // EMPLOYEE, CORPORATE, RETAIL
    private boolean active;
    private long createdDate;
    
    /**
     * Default constructor for Perst
     */
    public Actor() {
        this.createdDate = System.currentTimeMillis();
        this.active = true;
    }
    
    /**
     * Create a new Actor
     * 
     * @param name The actor's name
     * @param type The actor type
     */
    public Actor(String name, String type) {
        this();
        this.name = name;
        this.type = type;
        this.uuid = java.util.UUID.randomUUID().toString();
    }
    
    // Getters and Setters
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
        modify();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        modify();
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
        modify();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        modify();
    }
    
    public long getCreatedDate() {
        return createdDate;
    }
    
    /**
     * Convert to JSON for API response
     */
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
