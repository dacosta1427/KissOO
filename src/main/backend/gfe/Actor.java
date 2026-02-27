package gfe;

import org.garret.perst.continuous.CVersion;

/**
 * Actor - Represents an entity that can own and perform actions on OOVs.
 */
public class Actor extends CVersion {
    
    private String uuid;
    private String name;
    private String type;  // EMPLOYEE, CORPORATE, RETAIL
    private boolean active = true;
    private long createdDate;
    
    public Actor() {
        this.createdDate = System.currentTimeMillis();
    }
    
    public Actor(String name, String type) {
        this();
        this.name = name;
        this.type = type;
        this.uuid = java.util.UUID.randomUUID().toString();
    }
    
    // Manual getters/setters (no Lombok dependency)
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
