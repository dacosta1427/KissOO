package koo.core.actor;

import lombok.Getter;
import lombok.Setter;
import org.kissweb.lombok.toJSON;
import org.kissweb.lombok.toJSONExclude;
import org.kissweb.json.JSONObject;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * AActor - Represents an entity that can own and perform actions on resources.
 * 
 * IMPORTANT: Every AActor MUST have an Agreement. Without an Agreement,
 * the AActor CANNOT exist. This is enforced at construction time.
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
@Getter @Setter @toJSON
public abstract class AActor extends CVersion {
    
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
    private ActorType actorType;
    
    private Agreement agreement;
    
    @toJSONExclude  // Exclude internal reference to prevent circular reference
    private transient Object internalState;
    
    public AActor() {
    }
    
    public AActor(String name, Agreement agreement) {
        this.createdDate = System.currentTimeMillis();
        this.name = name;
        this.uuid = java.util.UUID.randomUUID().toString();
        
        // Auto-detect ActorType from class hierarchy
        if (this instanceof ACorporateActor) {
            this.actorType = ActorType.CORPORATE;
        } else {
            this.actorType = ActorType.NATURAL; // default for all natural actors
        }
        
        if (agreement == null) {
            throw new IllegalArgumentException("AActor MUST have an Agreement");
        }
        this.agreement = agreement;
    }
    
    // Convenience constructor with Role
    public AActor(String name, Role role, Agreement agreement) {
        this(name, agreement);
        if (this.agreement == null) {
            this.agreement = new Agreement(role);
        } else {
            this.agreement.setRole(role);
        }
    }
    
    public abstract boolean isNatural();
    
    public abstract boolean isCorporate();
    
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
    
    public void setAgreement(Agreement agreement) {
        if (agreement == null) {
            throw new IllegalArgumentException("AActor MUST have an Agreement");
        }
        this.agreement = agreement;
    }
    
    public boolean belongsToGroup(String groupName) {
        return agreement != null && agreement.hasGroup(groupName);
    }

    @Override
    public String toString() {
        return String.format("%s{oid=%d, uuid='%s', name='%s', active=%b}", 
                this.getClass().getSimpleName(),
                getOid(),
                getUuid(),
                getName(),
                isActive());
    }
    
    /**
     * Generate JSON representation of this AActor using KissOO JSON format.
     */
    public JSONObject toKissJSON() {
        JSONObject json = new JSONObject();
        json.put("uuid", getUuid());
        json.put("name", getName());
        json.put("type", getType());
        json.put("active", isActive());
        json.put("actorType", getActorType().name());
        json.put("createdDate", getCreatedDate());
        
        // Include agreement reference as OID
        if (getAgreement() != null) {
            json.put("agreementOid", getAgreement().getOid());
        }
        
        return json;
    }
}
