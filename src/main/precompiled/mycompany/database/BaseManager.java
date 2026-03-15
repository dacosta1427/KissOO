package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.domain.CRUD;
import mycompany.domain.EndpointMethod;
import mycompany.domain.PerstUser;
import java.util.Collection;

/**
 * BaseManager - Abstract base class for all Domain Managers.
 * 
 * This implements the "Manager at the Gate" pattern:
 * - All access to domain objects goes through the Manager
 * - Managers encapsulate business logic
 * - Managers handle validation, authorization, and auditing
 * 
 * IMPORTANT: Authorization is enforced via the Actor's Agreement.
 * Every Actor MUST have an Agreement to perform any operation.
 * 
 * All methods are static - no singleton needed. Thread safety is handled
 * by PerstHelper which uses thread-local PerstContext.
 * 
 * Example:
 *   UserData ud = servlet.getUserData();
 *   Actor actor = ActorManager.getByUserId(ud.getUserId());
 *   ActorManager.create(actor, "param1", "param2");  // Agreement checked automatically
 * 
 * @param <T> The domain entity type
 */
public abstract class BaseManager<T> {
    
    // Action constants for authorization
    protected static final String ACTION_CREATE = "create";
    protected static final String ACTION_READ = "read";
    protected static final String ACTION_UPDATE = "update";
    protected static final String ACTION_DELETE = "delete";
    protected static final String ACTION_EXECUTE = "execute";
    
    /**
     * Check if Perst is available
     */
    protected static boolean isPerstAvailable() {
        return PerstHelper.isAvailable();
    }
    
    /**
     * Check if an Actor has permission to perform an action (type-safe with Class)
     * 
     * This checks the Actor's Agreement:
     * 1. Actor must exist
     * 2. Actor MUST have an Agreement (enforced at construction)
     * 3. Agreement must be valid (active, within date range)
     * 4. Agreement must grant permission for the resource/action
     * 
     * @param actor The Actor making the request (can be null for unauthenticated)
     * @param action The action (CRUD.CREATE, CRUD.READ, etc.)
     * @param resourceClass The resource class (e.g., Actor.class)
     * @return true if authorized, false otherwise
     */
    protected static boolean checkPermission(Actor actor, String action, Class<?> resourceClass) {
        // 1. Check if actor exists
        if (actor == null) {
            // Deny all for unauthenticated
            return false;
        }
        
        // 2. Actor MUST have an Agreement (enforced at construction, but safe check)
        Agreement agreement = actor.getAgreement();
        if (agreement == null) {
            return false;
        }
        
        // 3. Check if Agreement grants permission
        return agreement.grants(resourceClass, action);
    }
    
    /**
     * Check if an Actor has permission for a specific endpoint.
     * 
     * @param actor The Actor making the request
     * @param endpoint The EndpointMethod being called
     * @return true if authorized, false otherwise
     */
    protected static boolean checkPermission(Actor actor, EndpointMethod endpoint) {
        if (actor == null) {
            return false;
        }
        
        Agreement agreement = actor.getAgreement();
        if (agreement == null) {
            return false;
        }
        
        return agreement.grants(endpoint, endpoint.getResourceClass(), CRUD.EXECUTE);
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
     * Get the resource class for this manager (for authorization)
     */
    protected static <T> Class<T> getResourceClass() {
        throw new UnsupportedOperationException("Override in subclass");
    }
}
