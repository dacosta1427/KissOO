package domain;

import org.garret.perst.FieldIndex;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

/**
 * PerstDBRoot - Root object holding all Perst indexes.
 * Central hub for accessing all domain entities.
 */
public class PerstDBRoot extends Persistent {
    
    // Field indexes for different entity types
    public FieldIndex<PerstUser> userIndex;
    public FieldIndex<Actor> actorIndex;
    
    public PerstDBRoot() {
        super();
    }
    
    /**
     * Initialize indexes - called on first creation
     */
    public void setCollections(Storage db) {
        userIndex = db.createFieldIndex(PerstUser.class, "username", true);
        actorIndex = db.createFieldIndex(Actor.class, "username", true);
    }
    
    /**
     * Called by Perst after loading from disk
     */
    public void onLoad() {
        // Indexes are automatically restored
    }
}
