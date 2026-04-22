package mycompany.actor;

import koo.oodb.BaseManager;
import koo.oodb.core.user.PerstUser;
import koo.oodb.core.database.StorageManager;
import mycompany.actor.owner.Owner;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

public class OwnerManager extends BaseManager<Owner> {
    
    private OwnerManager() {
    }
    
    public static Collection<Owner> getAll() {
        return StorageManager.getAll(Owner.class);
    }
    
    public static Owner getByUser(PerstUser user) {
        return user != null ? user.getActor() instanceof Owner ? (Owner) user.getActor() : null : null;
    }
    
    public static Owner getByKey(String key) {
        return StorageManager.find(Owner.class, "name", key);
    }
    
    public static Owner getByOid(long oid) {
        return StorageManager.getByOid(Owner.class, oid);
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
        
        Owner owner = new Owner(name, phone, email, active);
        owner.setUser(user);
        owner.setAddress(address);
        
        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addInsert(owner);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return owner;
    }
    
    public static boolean update(Owner owner) {
        if (owner == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addUpdate(owner);
        return StorageManager.store(tc);
    }
    
    public static boolean delete(Owner owner) {
        if (owner == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addDelete(owner);
        return StorageManager.store(tc);
    }
    
    public static boolean validate(Owner owner) {
        return owner != null && owner.getName() != null && !owner.getName().isEmpty();
    }
    
    public static boolean exists(String name) {
        return getByKey(name) != null;
    }
}
