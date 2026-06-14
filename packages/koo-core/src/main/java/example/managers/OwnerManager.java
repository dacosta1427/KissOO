package example.managers;

import koo.core.database.StorageManager;
import example.domain.Owner;
import java.util.Collection;

public class OwnerManager {
    
    public static Collection<Owner> getAll() {
        return StorageManager.getAll(Owner.class);
    }
    
    public static Owner getByOid(long oid) {
        return StorageManager.getByOid(Owner.class, oid);
    }
    
    public static void create(Owner owner) {
        StorageManager.store(StorageManager.createContainer().addInsert(owner));
    }
    
    public static void update(Owner owner) {
        StorageManager.store(StorageManager.createContainer().addUpdate(owner));
    }
    
    public static void delete(Owner owner) {
        StorageManager.store(StorageManager.createContainer().addDelete(owner));
    }
}