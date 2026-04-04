package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Actor - Represents an entity that can own and perform actions on resources.
 * 
 * IMPORTANT: Every Actor MUST have an Agreement. Without an Agreement,
 * the Actor CANNOT exist. This is enforced at construction time.
 * 
 * Every Actor creates its own deactivated PerstUser in the constructor.
 * The PerstUser has a persistent reference back to this Actor.
 * 
 * Indexing is handled by CDatabase via @Indexable annotations.
 * Use ActorManager for all database operations.
 */
@Getter @Setter
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
    
    private transient PerstUser perstUser;  // cached lookup, not persisted
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
        
        // Create deactivated PerstUser linked to this Actor
        String username = name.toLowerCase().replaceAll("\\s+", "_") + "_" + uuid.substring(0, 8);
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 16);
        this.perstUser = new PerstUser(username, tempPassword, this);
        this.perstUser.setActive(false);
        this.perstUser.setEmailVerified(false);
    }
    
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
