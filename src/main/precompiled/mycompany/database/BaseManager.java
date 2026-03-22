package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.Agreement;
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
    
    protected static boolean isPerstAvailable() {
        return oodb.PerstStorageManager.isAvailable();
    }
    
    protected static boolean checkPermission(Actor actor, String action, Class<?> resourceClass) {
        if (actor == null || actor.getAgreement() == null) {
            return false;
        }
        return actor.getAgreement().hasCrudPermission(resourceClass.getSimpleName(), action);
    }
    
    protected static boolean checkPermission(Actor actor, EndpointMethod method) {
        if (actor == null || actor.getAgreement() == null) {
            return false;
        }
        return actor.getAgreement().grants(method, method.getResourceClass(), ACTION_EXECUTE);
    }
    
    public static <T> Collection<T> getAll() {
        return getAll(null);
    }
    
    public static <T> Collection<T> getAll(Actor actor) {
        return java.util.Collections.emptyList();
    }
    
    public static <T> T getByKey(String key) {
        return getByKey(null, key);
    }
    
    public static <T> T getByKey(Actor actor, String key) {
        return null;
    }
    
    public static <T> T create(Object... args) {
        return create(null, args);
    }
    
    public static <T> T create(Actor actor, Object... args) {
        return null;
    }
    
    public static <T> boolean update(T entity) {
        return update(null, entity);
    }
    
    public static <T> boolean update(Actor actor, T entity) {
        return false;
    }
    
    public static <T> boolean delete(T entity) {
        return delete(null, entity);
    }
    
    public static <T> boolean delete(Actor actor, T entity) {
        return false;
    }
    
    protected static <T> boolean validate(T entity) {
        return true;
    }
    
    protected static <T> Class<T> getResourceClass() {
        return null;
    }
}
