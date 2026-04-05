package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
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
 * ActorType:
 * - NATURAL: Individual person, has a persisted PerstUser (deactivated by default).
 * - CORPORATE: Company/organization, contains collection of Natural actors, no PU.
 * 
 * Default ActorType is NATURAL.
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
    
    @Indexable
    private ActorType actorType = ActorType.NATURAL;
    
    private PerstUser perstUser;  // persisted - NATURAL actors have PU, CORPORATE don't
    
    private Agreement agreement;
    
    public Actor() {
    }
    
    public Actor(String name, String type, Agreement agreement) {
        this(name, type, agreement, ActorType.NATURAL);
    }
    
    public Actor(String name, String type, Agreement agreement, ActorType actorType) {
        this.createdDate = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        this.uuid = java.util.UUID.randomUUID().toString();
        this.actorType = actorType != null ? actorType : ActorType.NATURAL;
        
        if (agreement == null) {
            throw new IllegalArgumentException("Actor MUST have an Agreement");
        }
        this.agreement = agreement;
        
        // Only NATURAL actors have a PerstUser (deactivated by default)
        if (this.actorType == ActorType.NATURAL) {
            createPerstUser();
        }
    }
    
    private void createPerstUser() {
        String username = name.toLowerCase().replaceAll("\\s+", "_") + "_" + uuid.substring(0, 8);
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 16);
        this.perstUser = new PerstUser(username, tempPassword, this);
        this.perstUser.setActive(false);
        this.perstUser.setEmailVerified(false);
    }
    
    public boolean isNatural() {
        return actorType == ActorType.NATURAL;
    }
    
    public boolean isCorporate() {
        return actorType == ActorType.CORPORATE;
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
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", actorType=" + actorType +
                ", active=" + active +
                ", hasAgreement=" + (agreement != null) +
                '}';
    }
}
