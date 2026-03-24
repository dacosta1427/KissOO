package mycompany.database;

import mycompany.domain.Cleaner;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

/**
 * CleanerManager - Manager for Cleaner CRUD operations.
 * 
 * All operations use TransactionContainer for atomicity.
 */
public class CleanerManager extends BaseManager<Cleaner> {
    
    private CleanerManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<Cleaner> getAll() {
        return oodb.PerstStorageManager.getAll(Cleaner.class);
    }
    
    public static Cleaner getByKey(String key) {
        // Assume key is the cleaner name (unique)
        return oodb.PerstStorageManager.find(Cleaner.class, "name", key);
    }
    
    public static Cleaner getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(Cleaner.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static Cleaner create(Object... args) {
        if (args == null || args.length < 1) {
            return null;
        }
        String name = args[0].toString();
        String phone = args.length > 1 ? args[1].toString() : null;
        String email = args.length > 2 ? args[2].toString() : null;
        String address = args.length > 3 ? args[3].toString() : null;
        boolean active = args.length > 4 ? Boolean.parseBoolean(args[4].toString()) : true;
        
        Cleaner cleaner = new Cleaner(name, phone, email, address, active);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(cleaner);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return cleaner;
    }
    
    public static boolean update(Cleaner cleaner) {
        if (cleaner == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(cleaner);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(Cleaner cleaner) {
        if (cleaner == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(cleaner);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(Cleaner cleaner) {
        return cleaner != null && cleaner.getName() != null && !cleaner.getName().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean deactivate(String name) {
        Cleaner cleaner = getByKey(name);
        if (cleaner != null) {
            cleaner.setActive(false);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(cleaner);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean activate(String name) {
        Cleaner cleaner = getByKey(name);
        if (cleaner != null) {
            cleaner.setActive(true);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(cleaner);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean exists(String name) {
        return getByKey(name) != null;
    }
}