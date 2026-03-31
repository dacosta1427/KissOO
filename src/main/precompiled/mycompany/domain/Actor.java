package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.HashSet;
import java.util.Set;

/**
 * Actor - Represents an entity that can own and perform actions on resources.
 * 
 * IMPORTANT: Every Actor MUST have an Agreement. Without an Agreement,
 * the Actor CANNOT exist. This is enforced at construction time.
 * 
 * This is a generic entity - extend it for domain-specific behavior.
 * 
 * Indexing is handled by CDatabase via @Indexable annotations.
 * Use ActorManager for all database operations.
 */
public class Actor extends CVersion {
    
    @Indexable
    private String uuid;
    
    @FullTextSearchable
    @Indexable
    private String name;
    
    @Indexable
    private String type;
    
    @Indexable
    private boolean active = true;
    
    private long createdDate;
    
    @Indexable
    private int userId = 0;
    private transient PerstUser perstUser;
    private Agreement agreement;
    
    public Actor() {
    }
    
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
    
    public PerstUser getPerstUser() {
        if (perstUser == null && userId > 0) {
            // Lazy-load from Perst using userId (transient field won't persist)
            perstUser = mycompany.database.PerstUserManager.getByUserId(userId);
        }
        return perstUser;
    }
    public void setPerstUser(PerstUser perstUser) { 
        this.perstUser = perstUser; 
        if (perstUser != null) {
            this.userId = perstUser.getUserId();
        }
    }
    
    public Agreement getAgreement() { return agreement; }
    public void setAgreement(Agreement agreement) { this.agreement = agreement; }
    
    public void addToGroup(Group group) {
        if (agreement == null) {
            agreement = new Agreement();
        }
        agreement.addGroup(group);
    }
    
    public void removeFromGroup(Group group) {
        if (agreement != null) {
            agreement.removeGroup(group);
        }
    }
    
    public boolean belongsToGroup(String groupName) {
        return agreement != null && agreement.hasGroup(groupName);
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
