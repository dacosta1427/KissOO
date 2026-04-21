package koo.core;

import koo.core.actor.AActor;
import koo.security.EndpointMethod;
import koo.core.database.StorageManager;

import java.util.Collection;

/**
 * BaseManager - Abstract base class for all database managers.
 * Provides common CRUD operations with permission checking.
 * 
 * @param <T> The entity type managed by this manager
 */
public abstract class BaseManager<T> {
    
    protected static final String ACTION_CREATE  = "create";
    protected static final String ACTION_READ    = "read";
    protected static final String ACTION_UPDATE  = "update";
    protected static final String ACTION_DELETE  = "delete";
    protected static final String ACTION_EXECUTE = "execute";
    
    protected BaseManager() {
    }
    
    protected static boolean isPerstAvailable() {
        return StorageManager.isAvailable();
    }
    
    protected static boolean checkPermission(AActor AActor, String action, Class<?> resourceClass) {
        if (AActor == null || AActor.getAgreement() == null) {
            return false;
        }
        return AActor.getAgreement().hasCrudPermission(resourceClass.getSimpleName(), action);
    }
    
    protected static boolean checkPermission(AActor AActor, EndpointMethod method) {
        if (AActor == null || AActor.getAgreement() == null) {
            return false;
        }
        return AActor.getAgreement().grants(method, method.getResourceClass(), ACTION_EXECUTE);
    }
    
    public static <T> Collection<T> getAll() {
        return getAll(null);
    }
    
    public static <T> Collection<T> getAll(AActor AActor) {
        return java.util.Collections.emptyList();
    }
    
    public static <T> T getByKey(String key) {
        return getByKey(null, key);
    }
    
    public static <T> T getByKey(AActor AActor, String key) {
        return null;
    }
    
    public static <T> T create(Object... args) {
        return create(null, args);
    }
    
    public static <T> T create(AActor AActor, Object... args) {
        return null;
    }
    
    public static <T> boolean update(T entity) {
        return update(null, entity);
    }
    
    public static <T> boolean update(AActor AActor, T entity) {
        return false;
    }
    
    public static <T> boolean delete(T entity) {
        return delete(null, entity);
    }
    
    public static <T> boolean delete(AActor AActor, T entity) {
        return false;
    }
    
    protected static <T> boolean validate(T entity) {
        return true;
    }
    
    protected static <T> Class<T> getResourceClass() {
        return null;
    }
}
