package mycompany.database;

import mycompany.domain.House;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

/**
 * HouseManager - Manager for House CRUD operations.
 */
public class HouseManager extends BaseManager<House> {
    
    private HouseManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<House> getAll() {
        return oodb.PerstStorageManager.getAll(House.class);
    }
    
    public static House getByKey(String key) {
        // Assume key is the house name (unique)
        return oodb.PerstStorageManager.find(House.class, "name", key);
    }
    
    public static House getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(House.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static House create(Object... args) {
        if (args == null || args.length < 1) {
            return null;
        }
        String name = args[0].toString();
        String address = args.length > 1 ? args[1].toString() : null;
        String description = args.length > 2 ? args[2].toString() : null;
        boolean active = args.length > 3 ? Boolean.parseBoolean(args[3].toString()) : true;
        
        House house = new House(name, address, description, active);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(house);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return house;
    }
    
    public static boolean update(House house) {
        if (house == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(house);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(House house) {
        if (house == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(house);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(House house) {
        return house != null && house.getName() != null && !house.getName().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean deactivate(String name) {
        House house = getByKey(name);
        if (house != null) {
            house.setActive(false);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(house);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean activate(String name) {
        House house = getByKey(name);
        if (house != null) {
            house.setActive(true);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(house);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean exists(String name) {
        return getByKey(name) != null;
    }
}