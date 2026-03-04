package domain.database;

import domain.Actor;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * ActorManager - Manages Actor domain objects.
 * 
 * This is the "Manager at the Gate" - ALL access to Actor entities
 * must go through this class.
 * 
 * Responsibilities:
 * - CRUD operations
 * - Validation
 * - Business logic
 * - Authorization checks
 * - Audit logging (future)
 * 
 * IMPORTANT: For authorization, services must obtain the current Actor
 * from UserData and pass it to Manager methods:
 * 
 *   UserData ud = servlet.getUserData();
 *   Actor actor = ActorManager.getInstance().getByUserId(ud.getUserId());
 *   ActorManager.getInstance().create(actor, name, type);  // checks authorization
 */
public class ActorManager extends BaseManager<Actor> {
    
    private static ActorManager instance;
    
    private ActorManager() {}
    
    public static synchronized ActorManager getInstance() {
        if (instance == null) {
            instance = new ActorManager();
        }
        return instance;
    }
    
    @Override
    protected String getResourceName() {
        return "Actor";
    }
    
    @Override
    public Collection<Actor> getAll() {
        if (!isPerstAvailable()) {
            return new ArrayList<>();
        }
        return PerstHelper.retrieveAllObjects(Actor.class);
    }
    
    @Override
    public Actor getByKey(String key) {
        if (!isPerstAvailable()) {
            return null;
        }
        return PerstHelper.retrieveObject(Actor.class, "name", key);
    }
    
    /**
     * Get Actor by UUID
     */
    public Actor getByUuid(String uuid) {
        if (!isPerstAvailable()) {
            return null;
        }
        return PerstHelper.retrieveObject(Actor.class, uuid);
    }
    
    /**
     * Get Actor by user ID (from UserData.getUserId())
     * 
     * @param userId The user ID from UserData
     * @return The Actor linked to this userId, or null
     */
    public Actor getByUserId(int userId) {
        if (!isPerstAvailable()) {
            return null;
        }
        return Actor.findByUserId(userId);
    }
    
    @Override
    public Actor create(Object... params) {
        if (!isPerstAvailable()) {
            return null;
        }
        
        if (params.length < 2) {
            throw new IllegalArgumentException("Actor requires name and type");
        }
        
        String name = (String) params[0];
        String type = (String) params[1];
        
        Actor actor = new Actor(name, type);
        
        if (params.length > 2 && params[2] instanceof Integer) {
            actor.setUserId((Integer) params[2]);
        }
        
        if (!validate(actor)) {
            throw new IllegalArgumentException("Validation failed for Actor");
        }
        
        PerstHelper.storeNewObject(actor);
        return actor;
    }
    
    @Override
    public boolean update(Actor actor) {
        if (!isPerstAvailable() || actor == null) {
            return false;
        }
        
        if (!validate(actor)) {
            return false;
        }
        
        PerstHelper.storeModifiedObject(actor);
        return true;
    }
    
    @Override
    public boolean delete(Actor actor) {
        if (!isPerstAvailable() || actor == null) {
            return false;
        }
        
        PerstHelper.removeObject(actor);
        return true;
    }
    
    @Override
    protected boolean validate(Actor actor) {
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
    public List<Actor> getByType(String type) {
        List<Actor> result = new ArrayList<>();
        Collection<Actor> all = getAll();
        
        for (Actor actor : all) {
            if (actor.getType().equals(type)) {
                result.add(actor);
            }
        }
        return result;
    }
    
    /**
     * Check if actor exists
     */
    public boolean exists(String name) {
        return getByKey(name) != null;
    }
}
