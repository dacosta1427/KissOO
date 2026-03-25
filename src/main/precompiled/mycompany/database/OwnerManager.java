package mycompany.database;

import mycompany.domain.Owner;
import mycompany.domain.PerstUser;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

public class OwnerManager extends BaseManager<Owner> {
    
    private OwnerManager() {
    }
    
    public static Collection<Owner> getAll() {
        return oodb.PerstStorageManager.getAll(Owner.class);
    }
    
    public static Owner getByUserId(long userId) {
        return oodb.PerstStorageManager.find(Owner.class, "userId", userId);
    }
    
    public static Owner getByKey(String key) {
        return oodb.PerstStorageManager.find(Owner.class, "name", key);
    }
    
    public static Owner getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(Owner.class, oid);
    }
    
    public static Owner create(Object... args) {
        if (args == null || args.length < 1) {
            return null;
        }
        String name = args[0].toString();
        String email = args.length > 1 ? args[1].toString() : null;
        String phone = args.length > 2 ? args[2].toString() : null;
        String address = args.length > 3 ? args[3].toString() : null;
        boolean active = args.length > 4 ? Boolean.parseBoolean(args[4].toString()) : true;
        PerstUser user = args.length > 5 && args[5] instanceof PerstUser ? (PerstUser) args[5] : null;
        
        Owner owner = new Owner(name, email, phone, address, active);
        owner.setUser(user);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(owner);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return owner;
    }
    
    public static boolean update(Owner owner) {
        if (owner == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(owner);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(Owner owner) {
        if (owner == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(owner);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(Owner owner) {
        return owner != null && owner.getName() != null && !owner.getName().isEmpty();
    }
    
    public static boolean exists(String name) {
        return getByKey(name) != null;
    }
}
