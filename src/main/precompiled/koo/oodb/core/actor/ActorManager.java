package koo.oodb.core.actor;

import koo.oodb.BaseManager;
import koo.oodb.core.StorageManager;
import koo.oodb.core.user.PerstUser;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

/**
 * ActorManager - Manager for AActor CRUD operations.
 * <p>
 * All operations use TransactionContainer for atomicity.
 */
public class ActorManager extends BaseManager<AActor> {
    
    private ActorManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<AActor> getAll(AActor AActor) {
        return getAll();
    }
    
    public static AActor getByName(AActor AActor, String name) {
        return getByName(name);
    }
    
    public static AActor getByUuid(AActor AActor, String uuid) {
        return getByUuid(uuid);
    }
    
    public static Collection<AActor> getAll() {
        return StorageManager.getAll(AActor.class);
    }
    
    public static AActor getByName(String name) {
        return StorageManager.find(AActor.class, "name", name);
    }
    
    public static AActor getByUuid(String uuid) {
        return StorageManager.find(AActor.class, "uuid", uuid);
    }
    
    public static AActor getByPerstUser(PerstUser user) {
        return user != null ? user.getAActor() : null;
    }
    
    // ========== CRUD ==========
    
    public static AActor create(AActor AActor, Object... args) {
        return create(args);
    }
    
    public static boolean update(AActor AActor, AActor updated) {
        return update(updated);
    }
    
    public static boolean delete(AActor AActor, AActor toDelete) {
        return delete(toDelete);
    }
    
    public static AActor create(Object... args) {
        if (args == null || args.length < 2) {
            return null;
        }
        String name = args[0].toString();
        String type = args[1].toString();
        
        AActor AActor = new AActor(name, type, new Agreement());
        
        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addInsert(AActor);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return AActor;
    }
    
    public static boolean update(AActor AActor) {
        if (AActor == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addUpdate(AActor);
        return StorageManager.store(tc);
    }
    
    public static boolean delete(AActor AActor) {
        if (AActor == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        assert tc != null;
        tc.addDelete(AActor);
        return StorageManager.store(tc);
    }
    
    public static boolean validate(AActor AActor) {
        return AActor != null && AActor.getName() != null && !AActor.getName().isEmpty();
    }
    
    public static boolean exists(String uuid) {
        return getByUuid(uuid) != null;
    }
}
