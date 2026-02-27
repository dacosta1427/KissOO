package gfe;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import org.garret.perst.continuous.CVersion;

/**
 * Actor - Represents an entity that can own and perform actions on OOVs.
 */
@Getter
@Setter
@Builder
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
