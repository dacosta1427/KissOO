package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.EndpointMethod;
import java.util.Collection;

/**
 * BaseManager - Abstract base class for all database managers.
 * Provides common CRUD operations with permission checking.
 * 
 * @param <T> The entity type managed by this manager
 */
public abstract class BaseManager<T> {
    
    protected static final String ACTION_CREATE = "create";
    protected static final String ACTION_READ = "read";
    protected static final String ACTION_UPDATE = "update";
    protected static final String ACTION_DELETE = "delete";
    protected static final String ACTION_EXECUTE = "execute";
    
    protected BaseManager() {
    }
    
    /**
     * Check if Perst is available
     */
    protected static boolean isPerstAvailable() {
        return oodb.PerstStorageManager.getDatabase() != null;
    }
    
    /**
     * Check permission for an actor to perform an action on a resource class
     */
    protected static boolean checkPermission(Actor actor, String action, Class<?> resourceClass) {
        if (actor == null || actor.getAgreement() == null) {
            return false;
        }
        return actor.getAgreement().hasPermission(action, resourceClass.getSimpleName());
    }
    
    /**
     * Check permission for an actor using EndpointMethod
     */
    protected static boolean checkPermission(Actor actor, EndpointMethod method) {
        if (actor == null || actor.getAgreement() == null) {
            return false;
        }
        return actor.getAgreement().hasPermission(method.getAction(), method.getResourceClass().getSimpleName());
    }
    
    /**
     * Get all entities (no permission check)
     */
    public static <T> Collection<T> getAll() {
        return getAll(null);
    }
    
    /**
     * Get all entities with permission check
     */
    public static <T> Collection<T> getAll(Actor actor) {
        return java.util.Collections.emptyList();
    }
    
    /**
     * Get entity by key (no permission check)
     */
    public static <T> T getByKey(String key) {
        return getByKey(null, key);
    }
    
    /**
     * Get entity by key with permission check
     */
    public static <T> T getByKey(Actor actor, String key) {
        return null;
    }
    
    /**
     * Create entity (no permission check)
     */
    public static <T> T create(Object... args) {
        return create(null, args);
    }
    
    /**
     * Create entity with permission check
     */
    public static <T> T create(Actor actor, Object... args) {
        return null;
    }
    
    /**
     * Update entity (no permission check)
     */
    public static <T> boolean update(T entity) {
        return update(null, entity);
    }
    
    /**
     * Update entity with permission check
     */
    public static <T> boolean update(Actor actor, T entity) {
        return false;
    }
    
    /**
     * Delete entity (no permission check)
     */
    public static <T> boolean delete(T entity) {
        return delete(null, entity);
    }
    
    /**
     * Delete entity with permission check
     */
    public static <T> boolean delete(Actor actor, T entity) {
        return false;
    }
    
    /**
     * Validate entity before create/update
     */
    protected static <T> boolean validate(T entity) {
        return true;
    }
    
    /**
     * Get the resource class for this manager
     */
    protected static <T> Class<T> getResourceClass() {
        return null;
    }
}