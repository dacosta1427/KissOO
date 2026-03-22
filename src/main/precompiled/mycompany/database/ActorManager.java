package mycompany.database;

import mycompany.domain.Actor;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;

/**
 * ActorManager - Manager for Actor CRUD operations.
 * 
 * All operations use TransactionContainer for atomicity.
 */
public class ActorManager extends BaseManager<Actor> {
    
    private ActorManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<Actor> getAll(Actor actor) {
        return getAll();
    }
    
    public static Actor getByName(Actor actor, String name) {
        return getByName(name);
    }
    
    public static Actor getByUuid(Actor actor, String uuid) {
        return getByUuid(uuid);
    }
    
    public static Collection<Actor> getAll() {
        return oodb.PerstStorageManager.getAll(Actor.class);
    }
    
    public static Actor getByName(String name) {
        return oodb.PerstStorageManager.find(Actor.class, "name", name);
    }
    
    public static Actor getByUuid(String uuid) {
        return oodb.PerstStorageManager.find(Actor.class, "uuid", uuid);
    }
    
    public static Actor getByUserId(int userId) {
        return oodb.PerstStorageManager.find(Actor.class, "userId", userId);
    }
    
    // ========== CRUD ==========
    
    public static Actor create(Actor actor, Object... args) {
        return create(args);
    }
    
    public static boolean update(Actor actor, Actor updated) {
        return update(updated);
    }
    
    public static boolean delete(Actor actor, Actor toDelete) {
        return delete(toDelete);
    }
    
    public static Actor create(Object... args) {
        if (args == null || args.length < 2) {
            return null;
        }
        String name = args[0].toString();
        String type = args.length > 1 ? args[1].toString() : "default";
        
        Actor actor = new Actor(name, type, new mycompany.domain.Agreement());
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(actor);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return actor;
    }
    
    public static boolean update(Actor actor) {
        if (actor == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(actor);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(Actor actor) {
        if (actor == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(actor);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(Actor actor) {
        return actor != null && actor.getName() != null && !actor.getName().isEmpty();
    }
    
    public static boolean exists(String uuid) {
        return getByUuid(uuid) != null;
    }
}
