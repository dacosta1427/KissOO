package example.managers;

import koo.core.database.StorageManager;
import example.domain.Cleaner;
import java.util.Collection;

public class CleanerManager {
    
    public static Collection<Cleaner> getAll() {
        return StorageManager.getAll(Cleaner.class);
    }
    
    public static Cleaner getByOid(long oid) {
        return StorageManager.getByOid(Cleaner.class, oid);
    }
    
    public static void create(Cleaner cleaner) {
        StorageManager.store(StorageManager.createContainer().addInsert(cleaner));
    }
    
    public static void update(Cleaner cleaner) {
        StorageManager.store(StorageManager.createContainer().addUpdate(cleaner));
    }
    
    public static void delete(Cleaner cleaner) {
        StorageManager.store(StorageManager.createContainer().addDelete(cleaner));
    }
}