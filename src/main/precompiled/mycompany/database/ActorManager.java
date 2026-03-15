package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.domain.CDatabaseRoot;
import oodb.PerstStorageManager;
import java.util.*;

/**
 * ActorManager - Manages Actor domain objects.
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
    
    // ========== Base CRUD Methods ==========
    
    public static Collection<Actor> getAll() {
        if (!PerstStorageManager.isAvailable()) {
            return new ArrayList<>();
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        List<Actor> result = new ArrayList<>();
        for (Actor a : root.actorIndex) {
            result.add(a);
        }
        return result;
    }
    
    public static Actor getByName(String name) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        return root.actorIndex.get(name);
    }
    
    public static Actor getByUuid(String uuid) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (Actor a : root.actorIndex) {
            if (a.getUuid() != null && a.getUuid().equals(uuid)) {
                return a;
            }
        }
        return null;
    }
    
    public static Actor getByUserId(int userId) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (Actor a : root.actorIndex) {
            if (a.getUserId() == userId) {
                return a;
            }
        }
        return null;
    }
    
    public static Actor create(Object... params) {
        if (!PerstStorageManager.isAvailable()) {
            return null;
        }
        
        if (params.length < 2) {
            throw new IllegalArgumentException("Actor requires name and type");
        }
        
        String name = (String) params[0];
        
        if (getByName(name) != null) {
            throw new IllegalArgumentException("Actor already exists: " + name);
        }
        
        Agreement agreement = new Agreement(name + "_agreement");
        
        Actor actor = new Actor(name, (String) params[1], agreement);
        
        if (params.length > 2 && params[2] != null) {
            actor.setUserId((Integer) params[2]);
        }
        
        if (!validate(actor)) {
            throw new IllegalArgumentException("Validation failed for Actor");
        }
        
        PerstStorageManager.beginTransaction();
        try {
            PerstStorageManager.saveInTransaction(agreement);
            PerstStorageManager.saveInTransaction(actor);
            PerstStorageManager.commitTransaction();
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            throw new RuntimeException("Failed to create actor: " + e.getMessage(), e);
        }
        
        return actor;
    }
    
    public static boolean update(Actor actor) {
        if (!PerstStorageManager.isAvailable() || actor == null) {
            return false;
        }
        
        if (!validate(actor)) {
            return false;
        }
        
        try {
            PerstStorageManager.save(actor);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean delete(Actor actor) {
        if (!PerstStorageManager.isAvailable() || actor == null) {
            return false;
        }
        
        PerstStorageManager.beginTransaction();
        try {
            PerstStorageManager.deleteInTransaction(actor);
            PerstStorageManager.commitTransaction();
            return true;
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            return false;
        }
    }
    
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
