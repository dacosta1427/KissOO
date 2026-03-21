package mycompany.database;

import mycompany.domain.Phone;
import java.util.Collection;

/**
 * PhoneManager - Manager for Phone CRUD operations.
 */
public class PhoneManager {
    
    private PhoneManager() {
    }
    
    public static Collection<Phone> getAll() {
        return Phone.getAll();
    }
    
    public static Phone getByUuid(String uuid) {
        return Phone.getByUuid(uuid);
    }
    
    public static Phone getByOid(long oid) {
        return Phone.getByOid(oid);
    }
    
    public static Phone create(String number, String type, String ownerUuid) {
        Phone phone = new Phone(number, type, ownerUuid);
        phone.index();
        oodb.PerstStorageManager.getDatabase().insert(phone);
        return phone;
    }
    
    public static boolean update(Phone phone) {
        if (phone == null) return false;
        phone.index();
        return true;
    }
    
    public static boolean delete(Phone phone) {
        if (phone == null) return false;
        phone.removeIndex();
        return true;
    }
}