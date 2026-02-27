package gfe;

import org.garret.perst.FieldIndex;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

/**
 * Root object to hold Perst indexes.
 * Based on the ABaseRootClass pattern.
 */
public class Root extends Persistent {
    
    // Field indexes for different entity types
    public FieldIndex<PerstUser> userIndex;
    public FieldIndex<Actor> actorIndex;
    
    public Root() {
        super();
    }
    
    /**
     * Initialize indexes - called on first creation
     */
    public void setCollections(Storage db) {
        // Create field indexes on first creation
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
