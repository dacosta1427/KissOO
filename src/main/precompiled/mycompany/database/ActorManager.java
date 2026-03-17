package mycompany.database;

import mycompany.domain.Actor;
import java.util.*;

/**
 * ActorManager - Manages Actor domain objects.
 * Contains business logic only - storage is delegated to PerstStorageManager.
 */
public class ActorManager extends BaseManager<Actor> {
    
    private ActorManager() {}
    
    // ========== Authorization-Aware Methods ==========
    
    public static Collection<Actor> getAll(Actor actor) {
        if (!checkPermission(actor, ACTION_READ, Actor.class)) {
            return null;
        }
        return getAll();
    }
    
    public static Actor getByName(Actor actor, String name) {
        if (!checkPermission(actor, ACTION_READ, Actor.class)) {
            return null;
        }
        return getByName(name);
    }
    
    public static Actor getByUuid(Actor actor, String uuid) {
        if (!checkPermission(actor, ACTION_READ, Actor.class)) {
            return null;
        }
        return getByUuid(uuid);
    }
    
    public static Actor create(Actor actor, Object... params) {
        if (!checkPermission(actor, ACTION_CREATE, Actor.class)) {
            return null;
        }
        return create(params);
    }
    
    public static boolean update(Actor actor, Actor obj) {
        if (!checkPermission(actor, ACTION_UPDATE, Actor.class)) {
            return false;
        }
        return update(obj);
    }
    
    public static boolean delete(Actor actor, Actor obj) {
        if (!checkPermission(actor, ACTION_DELETE, Actor.class)) {
            return false;
        }
        return delete(obj);
    }
    
    // ========== Base CRUD Methods (delegated to PSM) ==========
    
    public static Collection<Actor> getAll() {
        Collection<Actor> result = new ArrayList<>();
        for (Object obj : oodb.PerstStorageManager.getAll(Actor.class)) {
            result.add((Actor) obj);
        }
        return result;
    }
    
    public static Actor getByName(String name) {
        for (Actor a : getAll()) {
            if (a.getName() != null && a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }
    
    public static Actor getByUuid(String uuid) {
        // TODO: Implement getByUuid when domain classes have getUuid()
        return null;
    }
    
    public static Actor getByUserId(int userId) {
        if (userId <= 0) {
            return null;
        }
        for (Actor a : getAll()) {
            if (a.getUserId() == userId) {
                return a;
            }
        }
        return null;
    }
    
    public static Actor create(Object... params) {
        if (params.length < 2) {
            throw new IllegalArgumentException("Actor requires name and type");
        }
        
        String name = (String) params[0];
        
        if (getByName(name) != null) {
            throw new IllegalArgumentException("Actor already exists: " + name);
        }
        
        Actor actor = new Actor(name, (String) params[1], new mycompany.domain.Agreement(name + "_agreement"));
        
        if (params.length > 2 && params[2] != null) {
            actor.setUserId((Integer) params[2]);
        }
        
        if (!validate(actor)) {
            throw new IllegalArgumentException("Validation failed for Actor");
        }
        
        oodb.PerstStorageManager.save(actor);
        return actor;
    }
    
    public static boolean update(Actor actor) {
        if (actor == null) return false;
        if (!validate(actor)) return false;
        
        try {
            oodb.PerstStorageManager.save(actor);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean delete(Actor actor) {
        if (actor == null) return false;
        
        try {
            oodb.PerstStorageManager.delete(actor);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== Business Logic ==========
    
    public static boolean validate(Actor actor) {
        if (actor == null) return false;
        if (actor.getName() == null || actor.getName().isEmpty()) return false;
        if (actor.getType() == null || actor.getType().isEmpty()) return false;
        return true;
    }
    
    public static boolean exists(String name) {
        return getByName(name) != null;
    }
}
