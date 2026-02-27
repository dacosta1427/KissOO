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
 * @param <T> The domain entity type
 */
public abstract class BaseManager<T> {
    
    /**
     * Check if Perst is available
     */
    protected boolean isPerstAvailable() {
        return PerstHelper.isAvailable();
    }
    
    /**
     * Get all entities of this type
     */
    public abstract Collection<T> getAll();
    
    /**
     * Get entity by unique key (e.g., username, uuid)
     */
    public abstract T getByKey(String key);
    
    /**
     * Create a new entity
     */
    public abstract T create(Object... params);
    
    /**
     * Update an existing entity
     */
    public abstract boolean update(T entity);
    
    /**
     * Delete an entity
     */
    public abstract boolean delete(T entity);
    
    /**
     * Validate entity before save
     */
    protected abstract boolean validate(T entity);
}
