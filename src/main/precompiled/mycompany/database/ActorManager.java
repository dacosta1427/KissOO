package mycompany.database;

import mycompany.domain.Actor;
import java.util.Collection;

/**
 * ActorManager - Manager for Actor CRUD operations.
 */
public class ActorManager extends BaseManager<Actor> {
    
    private ActorManager() {
    }
    
    public static Collection<Actor> getAll(Actor actor) {
        return getAll();
    }
    
    public static Actor getByName(Actor actor, String name) {
        return getByName(name);
    }
    
    public static Actor getByUuid(Actor actor, String uuid) {
        return getByUuid(uuid);
    }
    
    public static Actor create(Actor actor, Object... args) {
        return create(args);
    }
    
    public static boolean update(Actor actor, Actor updated) {
        return update(updated);
    }
    
    public static boolean delete(Actor actor, Actor toDelete) {
        return delete(toDelete);
    }
    
    public static Collection<Actor> getAll() {
        return Actor.findAll();
    }
    
    public static Actor getByName(String name) {
        return Actor.findByName(name);
    }
    
    public static Actor getByUuid(String uuid) {
        return Actor.findByUuid(uuid);
    }
    
    public static Actor getByUserId(int userId) {
        return Actor.findByUserId(userId);
    }
    
    public static Actor create(Object... args) {
        if (args == null || args.length < 2) {
            return null;
        }
        String name = args[0].toString();
        String type = args.length > 1 ? args[1].toString() : "default";
        Actor actor = new Actor(name, type, new mycompany.domain.Agreement());
        actor.index();
        oodb.PerstStorageManager.getDatabase().insert(actor);
        return actor;
    }
    
    public static boolean update(Actor actor) {
        if (actor == null) return false;
        actor.index();
        return true;
    }
    
    public static boolean delete(Actor actor) {
        if (actor == null) return false;
        actor.removeIndex();
        return true;
    }
    
    public static boolean validate(Actor actor) {
        return actor != null && actor.getName() != null && !actor.getName().isEmpty();
    }
    
    public static boolean exists(String uuid) {
        return Actor.findByUuid(uuid) != null;
    }
}