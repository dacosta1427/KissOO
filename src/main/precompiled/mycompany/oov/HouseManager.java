package mycompany.oov;

import koo.oodb.BaseManager;
import mycompany.actor.owner.Owner;
import koo.oodb.core.database.StorageManager;
import mycompany.oov.house.House;
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
        return StorageManager.getAll(House.class);
    }
    
    public static House getByKey(String key) {
        // Assume key is the house name (unique)
        return StorageManager.find(House.class, "name", key);
    }
    
    public static House getByOid(long oid) {
        return StorageManager.getByOid(House.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static House create(Object... args) {
        if (args == null || args.length < 1) {
            return null;
        }
        Owner owner = (Owner) args[0];
        String name = args[1].toString();
        String address = args[2].toString();
        String description = args.length > 3 ? args[3].toString() : null;
        boolean active = args.length <= 4 || Boolean.parseBoolean(args[4].toString());
        String checkInTime = args.length > 4 ? args[4].toString() : "16:00";
        String checkOutTime = args.length > 5 ? args[5].toString() : "10:00";

        House house = new House(owner, name, address, description, active);
        house.setCheckInTime(checkInTime);
        house.setCheckOutTime(checkOutTime);

        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addInsert(house);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return house;
    }
    
    public static boolean update(House house) {
        if (house == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addUpdate(house);
        return StorageManager.store(tc);
    }
    
    public static boolean delete(House house) {
        if (house == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addDelete(house);
        return StorageManager.store(tc);
    }
    
    public static boolean validate(House house) {
        return house != null && house.getName() != null && !house.getName().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean deactivate(String name) {
        House house = getByKey(name);
        if (house != null) {
            house.setActive(false);
            TransactionContainer tc = StorageManager.createContainer();
            assert tc != null;
            tc.addUpdate(house);
            return StorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean activate(String name) {
        House house = getByKey(name);
        if (house != null) {
            house.setActive(true);
            TransactionContainer tc = StorageManager.createContainer();
            assert tc != null;
            tc.addUpdate(house);
            return StorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean exists(String name) {
        return getByKey(name) != null;
    }
}