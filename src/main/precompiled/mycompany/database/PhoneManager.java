package mycompany.database;

import mycompany.domain.Phone;
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
        return oodb.PerstStorageManager.getAll(Phone.class);
    }
    
    public static Phone getByUuid(String uuid) {
        return oodb.PerstStorageManager.find(Phone.class, "uuid", uuid);
    }
    
    public static Phone getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(Phone.class, oid);
    }
    
    public static Phone create(String firstName, String lastName, String phoneNumber) {
        Phone phone = new Phone(firstName, lastName, phoneNumber);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(phone);
        if (oodb.PerstStorageManager.store(tc)) {
            return phone;
        }
        return null;
    }
    
    public static boolean update(Phone phone) {
        if (phone == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(phone);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(Phone phone) {
        if (phone == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(phone);
        return oodb.PerstStorageManager.store(tc);
    }
}
