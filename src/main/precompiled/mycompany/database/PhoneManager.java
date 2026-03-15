package mycompany.database;

import mycompany.domain.Phone;
import mycompany.domain.CDatabaseRoot;
import oodb.PerstStorageManager;
import java.util.*;

/**
 * PhoneManager - Manages Phone domain objects.
 * 
 * This is the "Manager at the Gate" - ALL access to Phone entities
 * must go through this class.
 */
public class PhoneManager {
    
    private PhoneManager() {}
    
    public static Collection<Phone> getAll() {
        if (!PerstStorageManager.isAvailable()) {
            return new ArrayList<>();
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<Phone> result = new ArrayList<>();
        for (Phone p : root.phoneIndex) {
            result.add(p);
        }
        return result;
    }
    
    public static Phone getByUuid(String uuid) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (Phone p : root.phoneIndex) {
            if (p.getUuid() != null && p.getUuid().equals(uuid)) {
                return p;
            }
        }
        return null;
    }
    
    public static Phone getByOid(long oid) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (Phone p : root.phoneIndex) {
            if (p.getOid() == oid) {
                return p;
            }
        }
        return null;
    }
    
    public static Phone create(String firstName, String lastName, String phoneNumber) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        
        Phone phone = new Phone(firstName, lastName, phoneNumber);
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.phoneIndex.put(phone);
            PerstStorageManager.commitTransaction();
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            throw new RuntimeException("Failed to create phone: " + e.getMessage(), e);
        }
        
        return phone;
    }
    
    public static boolean update(Phone phone) {
        if (!PerstStorageManager.isAvailable() || phone == null) {
            return false;
        }
        
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.phoneIndex.put(phone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean delete(Phone phone) {
        if (!PerstStorageManager.isAvailable() || phone == null) {
            return false;
        }
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.phoneIndex.remove(phone);
            PerstStorageManager.commitTransaction();
            return true;
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            return false;
        }
    }
    
    public static Phone getById(long id) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        int index = 0;
        for (Phone p : root.phoneIndex) {
            if (index == id) {
                return p;
            }
            index++;
        }
        return null;
    }
}
