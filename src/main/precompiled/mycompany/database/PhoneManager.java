package mycompany.database;

import mycompany.Phone;
import koo.oodb.core.database.StorageManager;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

/**
 * PhoneManager - Manager for Phone CRUD operations.
 * 
 * All operations use TransactionContainer for atomicity.
 */
public class PhoneManager {
    
    private PhoneManager() {
    }
    
    public static Collection<Phone> getAll() {
        return StorageManager.getAll(Phone.class);
    }
    
    public static Phone getByUuid(String uuid) {
        return StorageManager.find(Phone.class, "uuid", uuid);
    }
    
    public static Phone getByOid(long oid) {
        return StorageManager.getByOid(Phone.class, oid);
    }
    
    public static Phone create(String firstName, String lastName, String phoneNumber) {
        Phone phone = new Phone(firstName, lastName, phoneNumber);
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addInsert(phone);
        if (StorageManager.store(tc)) {
            return phone;
        }
        return null;
    }
    
    public static boolean update(Phone phone) {
        if (phone == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addUpdate(phone);
        return StorageManager.store(tc);
    }
    
    public static boolean delete(Phone phone) {
        if (phone == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addDelete(phone);
        return StorageManager.store(tc);
    }
}
