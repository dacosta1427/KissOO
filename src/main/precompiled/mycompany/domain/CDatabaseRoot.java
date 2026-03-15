package mycompany.domain;

import org.garret.perst.FieldIndex;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

/**
 * CDatabaseRoot - Root object holding all Perst indexes for CDatabase versioning.
 * 
 * This replaces PerstDBRoot when using CDatabase for automatic versioning.
 * Supports all domain entities with their indexes.
 * 
 * IMPORTANT: When adding or removing domain classes, update this class!
 * 
 * ADDING A NEW DOMAIN CLASS:
 * 1. Add the import: import domain.YourNewClass;
 * 2. Add a FieldIndex: public FieldIndex<YourNewClass> yourNewClassIndex;
 * 3. Add to setCollections(): yourNewClassIndex = db.createFieldIndex(YourNewClass.class, "fieldName", unique);
 * 
 * REMOVING A DOMAIN CLASS:
 * 1. Remove the import
 * 2. Remove the FieldIndex declaration
 * 3. Remove from setCollections()
 * 
 * Example:
 *   // To add MyEntity indexed by "name":
 *   import domain.MyEntity;
 *   
 *   public FieldIndex<MyEntity> myEntityIndex;
 *   
 *   public void setCollections(Storage db) {
 *       ...
 *       myEntityIndex = db.createFieldIndex(MyEntity.class, "name", true);
 *   }
 */
public class CDatabaseRoot extends Persistent {
    
    // Field indexes for different entity types
    public FieldIndex<PerstUser> userIndex;
    public FieldIndex<Actor> actorIndex;
    public FieldIndex<Agreement> agreementIndex;
    public FieldIndex<Group> groupIndex;
    
    public CDatabaseRoot() {
        super();
    }
    
    /**
     * Initialize indexes - called on first creation.
     * Add new domain class indexes here.
     */
    public void setCollections(Storage db) {
        userIndex = db.createFieldIndex(PerstUser.class, "username", true);
        actorIndex = db.createFieldIndex(Actor.class, "name", false);
        agreementIndex = db.createFieldIndex(Agreement.class, "role", false);
        groupIndex = db.createFieldIndex(Group.class, "name", true);
    }
    
    /**
     * Called by Perst after loading from disk
     */
    public void onLoad() {
        // Indexes are automatically restored
    }
}
