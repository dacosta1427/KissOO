package domain.database;

import domain.Actor;
import domain.Agreement;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * ActorManager - Manages Actor domain objects.
 * 
 * This is the "Manager at the Gate" - ALL access to Actor entities
 * must go through this class.
 * 
 * All methods are static - no singleton needed. Thread safety is handled
 * by PerstHelper which uses thread-local PerstContext.
 * 
 * Responsibilities:
 * - CRUD operations
 * - Validation
 * - Business logic
 * - Authorization checks via Agreement
 * 
 * IMPORTANT: For authorization, services must obtain the current Actor
 * from UserData and pass it to Manager methods. The Actor's Agreement
 * determines what operations are permitted.
 * 
 *   UserData ud = servlet.getUserData();
 *   Actor actor = ActorManager.getByUserId(ud.getUserId());
 *   ActorManager.create(actor, name, type);  // Agreement checked automatically
 */
public class ActorManager extends BaseManager<Actor> {
    
    private ActorManager() {}  // Prevent instantiation
    
    // ========== Static Authorization-Aware Methods ==========
    
    /**
     * Get all Actors (with authorization check via Agreement)
     */
    public static Collection<Actor> getAll(Actor actor) {
        if (!checkPermission(actor, ACTION_READ, "Actor")) {
            return null;
        }
        return getAll();
    }
    
    /**
     * Get Actor by name (with authorization check)
     */
    public static Actor getByKey(Actor actor, String key) {
        if (!checkPermission(actor, ACTION_READ, "Actor")) {
            return null;
        }
        return getByKey(key);
    }
    
    /**
     * Get Actor by UUID (with authorization check)
     */
    public static Actor getByUuid(Actor actor, String uuid) {
        if (!checkPermission(actor, ACTION_READ, "Actor")) {
            return null;
        }
        return getByUuid(uuid);
    }
    
    /**
     * Create Actor (with authorization check)
     */
    public static Actor create(Actor actor, Object... params) {
        if (!checkPermission(actor, ACTION_CREATE, "Actor")) {
            return null;
        }
        return create(params);
    }
    
    /**
     * Create a NEW Actor with an Agreement (internal use - bypasses auth)
     * 
     * @param agreement The Agreement for the new Actor (can be null for default USER role)
     * @param name Actor name
     * @param type Actor type
     * @return the created Actor
     */
    public static Actor createWithAgreement(Agreement agreement, String name, String type) {
        if (agreement == null) {
            agreement = new Agreement("USER");
        }
        return create(name, type, agreement);
    }
    
    /**
     * Update Actor (with authorization check)
     */
    public static boolean update(Actor actor, Actor entity) {
        if (!checkPermission(actor, ACTION_UPDATE, "Actor")) {
            return false;
        }
        return update(entity);
    }
    
    /**
     * Delete Actor (with authorization check)
     */
    public static boolean delete(Actor actor, Actor entity) {
        if (!checkPermission(actor, ACTION_DELETE, "Actor")) {
            return false;
        }
        return delete(entity);
    }
    
    // ========== Static Base Methods ==========
    
    /**
     * Get all Actors (no authorization - use getAll(Actor) for authorized access)
     */
    public static Collection<Actor> getAll() {
        if (!isPerstAvailable()) {
            return new ArrayList<>();
        }
        return PerstHelper.retrieveAllObjects(Actor.class);
    }
    
    /**
     * Get Actor by name
     */
    public static Actor getByKey(String key) {
        if (!isPerstAvailable()) {
            return null;
        }
        return PerstHelper.retrieveObject(Actor.class, "name", key);
    }
    
    /**
     * Get Actor by UUID
     */
    public static Actor getByUuid(String uuid) {
        if (!isPerstAvailable()) {
            return null;
        }
        return PerstHelper.retrieveObject(Actor.class, uuid);
    }
    
    /**
     * Get Actor by user ID (from UserData.getUserId())
     */
    public static Actor getByUserId(int userId) {
        if (!isPerstAvailable()) {
            return null;
        }
        return Actor.findByUserId(userId);
    }
    
    /**
     * Create a new Actor
     */
    public static Actor create(Object... params) {
        if (!isPerstAvailable()) {
            return null;
        }
        
        if (params.length < 2) {
            throw new IllegalArgumentException("Actor requires name and type");
        }
        
        String name = (String) params[0];
        String type = (String) params[1];
        
        // Check if Agreement is provided (params[2])
        Agreement agreement = null;
        if (params.length > 2 && params[2] instanceof Agreement) {
            agreement = (Agreement) params[2];
        }
        
        Actor actor = new Actor(name, type, agreement);
        
        if (params.length > 3 && params[3] instanceof Integer) {
            actor.setUserId((Integer) params[3]);
        }
        
        if (!validate(actor)) {
            throw new IllegalArgumentException("Validation failed for Actor");
        }
        
        PerstHelper.storeNewObject(actor);
        return actor;
    }
    
    /**
     * Update an Actor
     */
    public static boolean update(Actor actor) {
        if (!isPerstAvailable() || actor == null) {
            return false;
        }
        
        if (!validate(actor)) {
            return false;
        }
        
        PerstHelper.storeModifiedObject(actor);
        return true;
    }
    
    /**
     * Delete an Actor
     */
    public static boolean delete(Actor actor) {
        if (!isPerstAvailable() || actor == null) {
            return false;
        }
        
        PerstHelper.removeObject(actor);
        return true;
    }
    
    /**
     * Validate an Actor
     */
    public static boolean validate(Actor actor) {
        if (actor == null) {
            return false;
        }
        if (actor.getName() == null || actor.getName().isEmpty()) {
            return false;
        }
        return true;
    }
    
    /**
     * Get all actors of a specific type
     */
    public static List<Actor> getByType(String type) {
        List<Actor> result = new ArrayList<>();
        Collection<Actor> all = getAll();
        
        if (all == null) return result;
        
        for (Actor actor : all) {
            if (type.equals(actor.getType())) {
                result.add(actor);
            }
        }
        return result;
    }
    
    /**
     * Check if actor exists
     */
    public static boolean exists(String name) {
        return getByKey(name) != null;
    }
}
