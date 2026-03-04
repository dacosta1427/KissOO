package domain.database;

import domain.Actor;
import domain.PerstUser;
import java.util.Collection;

/**
 * BaseManager - Abstract base class for all Domain Managers.
 * 
 * This implements the "Manager at the Gate" pattern:
 * - All access to domain objects goes through the Manager
 * - Managers encapsulate business logic
 * - Managers handle validation, authorization, and auditing
 * 
 * IMPORTANT: Authorization is enforced by checking if the calling Actor
 * has permission to perform the requested action. Services MUST obtain
 * the current Actor from UserData and pass it to Manager methods.
 * 
 * Example:
 *   UserData ud = servlet.getUserData();
 *   Actor actor = ActorManager.getInstance().getByUserId(ud.getUserId());
 *   ActorManager.getInstance().create(actor, "param1", "param2");
 * 
 * @param <T> The domain entity type
 */
public abstract class BaseManager<T> {
    
    /**
     * Action constants for authorization
     */
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_READ = "read";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_ADMIN = "admin";
    
    /**
     * Check if Perst is available
     */
    protected boolean isPerstAvailable() {
        return PerstHelper.isAvailable();
    }
    
    /**
     * Check if an Actor has permission to perform an action.
     * Override in subclass to implement specific authorization logic.
     * 
     * @param actor The Actor making the request (can be null for unauthenticated)
     * @param action The action being performed (ACTION_CREATE, ACTION_READ, etc.)
     * @param resource The resource being accessed (e.g., "Actor", "PerstUser")
     * @return true if authorized, false otherwise
     */
    protected boolean checkPermission(Actor actor, String action, String resource) {
        // Default: deny if no actor provided (unless it's a read operation)
        if (actor == null) {
            return ACTION_READ.equals(action);  // Allow read for unauthenticated
        }
        
        // Default implementation: check if actor is active
        // Subclasses should override with specific authorization rules
        return actor.isActive();
    }
    
    /**
     * Get all entities of this type (requires READ permission)
     */
    public abstract Collection<T> getAll();
    
    /**
     * Get all entities of this type with Actor context for authorization
     * 
     * @param actor The Actor making the request (for authorization)
     */
    public Collection<T> getAll(Actor actor) {
        if (!checkPermission(actor, ACTION_READ, getResourceName())) {
            return null;
        }
        return getAll();
    }
    
    /**
     * Get entity by unique key (requires READ permission)
     */
    public abstract T getByKey(String key);
    
    /**
     * Get entity by key with Actor context for authorization
     */
    public T getByKey(Actor actor, String key) {
        if (!checkPermission(actor, ACTION_READ, getResourceName())) {
            return null;
        }
        return getByKey(key);
    }
    
    /**
     * Create a new entity (requires CREATE permission)
     */
    public abstract T create(Object... params);
    
    /**
     * Create with Actor context for authorization
     */
    public T create(Actor actor, Object... params) {
        if (!checkPermission(actor, ACTION_CREATE, getResourceName())) {
            return null;
        }
        return create(params);
    }
    
    /**
     * Update an existing entity (requires UPDATE permission)
     */
    public abstract boolean update(T entity);
    
    /**
     * Update with Actor context for authorization
     */
    public boolean update(Actor actor, T entity) {
        if (!checkPermission(actor, ACTION_UPDATE, getResourceName())) {
            return false;
        }
        return update(entity);
    }
    
    /**
     * Delete an entity (requires DELETE permission)
     */
    public abstract boolean delete(T entity);
    
    /**
     * Delete with Actor context for authorization
     */
    public boolean delete(Actor actor, T entity) {
        if (!checkPermission(actor, ACTION_DELETE, getResourceName())) {
            return false;
        }
        return delete(entity);
    }
    
    /**
     * Validate entity before save
     */
    protected abstract boolean validate(T entity);
    
    /**
     * Get the resource name for this manager (for authorization logging)
     */
    protected abstract String getResourceName();
}
