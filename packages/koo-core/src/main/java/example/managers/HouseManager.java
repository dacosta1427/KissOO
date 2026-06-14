package example.managers;

import koo.core.database.StorageManager;
import example.domain.House;
import java.util.Collection;

public class HouseManager {
    
    public static Collection<House> getAll() {
        return StorageManager.getAll(House.class);
    }
    
    public static House getByOid(long oid) {
        return StorageManager.getByOid(House.class, oid);
    }
    
    public static void create(House house) {
        StorageManager.store(StorageManager.createContainer().addInsert(house));
    }
    
    public static void update(House house) {
        StorageManager.store(StorageManager.createContainer().addUpdate(house));
    }
    
    public static void delete(House house) {
        StorageManager.store(StorageManager.createContainer().addDelete(house));
    }
}