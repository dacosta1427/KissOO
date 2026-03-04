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
 * All methods are static - no singleton needed. Thread safety is handled
 * by PerstHelper which uses thread-local PerstContext.
 * 
 * Example:
 *   UserData ud = servlet.getUserData();
 *   Actor actor = ActorManager.getByUserId(ud.getUserId());
 *   ActorManager.create(actor, "param1", "param2");
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
    protected static boolean isPerstAvailable() {
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
    protected static boolean checkPermission(Actor actor, String action, String resource) {
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
    public static <T> Collection<T> getAll() {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Get all entities of this type with Actor context for authorization
     * 
     * @param actor The Actor making the request (for authorization)
     */
    public static <T> Collection<T> getAll(Actor actor) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Get entity by unique key (requires READ permission)
     */
    public static <T> T getByKey(String key) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Get entity by key with Actor context for authorization
     */
    public static <T> T getByKey(Actor actor, String key) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Create a new entity (requires CREATE permission)
     */
    public static <T> T create(Object... params) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Create with Actor context for authorization
     */
    public static <T> T create(Actor actor, Object... params) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Update an existing entity (requires UPDATE permission)
     */
    public static <T> boolean update(T entity) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Update with Actor context for authorization
     */
    public static <T> boolean update(Actor actor, T entity) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Delete an entity (requires DELETE permission)
     */
    public static <T> boolean delete(T entity) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Delete with Actor context for authorization
     */
    public static <T> boolean delete(Actor actor, T entity) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Validate entity before save
     */
    protected static <T> boolean validate(T entity) {
        throw new UnsupportedOperationException("Override in subclass");
    }
    
    /**
     * Get the resource name for this manager (for authorization logging)
     */
    protected static String getResourceName() {
        throw new UnsupportedOperationException("Override in subclass");
    }
}
