package example.managers;

import koo.core.database.StorageManager;
import example.domain.CostProfile;
import java.util.Collection;

public class CostProfileManager {
    
    public static Collection<CostProfile> getAll() {
        return StorageManager.getAll(CostProfile.class);
    }
    
    public static CostProfile getByOid(long oid) {
        return StorageManager.getByOid(CostProfile.class, oid);
    }
    
    public static void create(CostProfile profile) {
        StorageManager.store(StorageManager.createContainer().addInsert(profile));
    }
    
    public static void update(CostProfile profile) {
        StorageManager.store(StorageManager.createContainer().addUpdate(profile));
    }
    
    public static void delete(CostProfile profile) {
        StorageManager.store(StorageManager.createContainer().addDelete(profile));
    }
}