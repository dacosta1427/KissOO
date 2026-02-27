package domain.database;

import domain.kissweb.PerstContext;

/**
 * PerstHelper - Static helper for Perst operations.
 * 
 * <p><b>Thread Safety:</b> All methods are thread-safe and isolated per request.
 * This class delegates to PerstContext which maintains thread-local Storage instances.
 * Each HTTP request to kissweb runs in its own thread with its own Perst session,
 * ensuring complete isolation between concurrent requests.
 * 
 * <p><b>Usage:</b> All domain access should go through Manager classes (e.g., ActorManager)
 * which use PerstHelper internally. Direct PerstHelper usage is discouraged.
 */
public class PerstHelper {
    
    private PerstHelper() {}
    
    /**
     * Check if Perst is available for this thread's session.
     * 
     * @return true if Perst is initialized and available
     */
    public static boolean isAvailable() {
        return PerstContext.getInstance().isAvailable();
    }
    
    /**
     * Get the PerstContext for this thread.
     * 
     * @return the thread-local PerstContext
     */
    public static PerstContext getContext() {
        return PerstContext.getInstance();
    }
    
    /**
     * Initialize Perst for this thread's session.
     */
    public static void initialize() {
        PerstContext.getInstance().initialize();
    }
    
    // Actor operations
    /**
     * Retrieve an Actor by field value.
     * 
     * @param clazz the Actor class
     * @param field the field name to search by
     * @param value the value to match
     * @return the Actor if found, null otherwise
     */
    public static Actor retrieveActor(Class<Actor> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveObject(clazz, field, value);
    }
    
    /**
     * Retrieve all Actors.
     * 
     * @param clazz the Actor class
     * @return collection of all Actors
     */
    public static java.util.Collection<Actor> retrieveAllActors(Class<Actor> clazz) {
        return PerstContext.getInstance().retrieveAllObjects(clazz);
    }
    
    /**
     * Store a new Actor.
     * 
     * @param obj the Actor to store
     */
    public static void storeActor(Actor obj) {
        PerstContext.getInstance().storeNewObject(obj);
    }
    
    /**
     * Update an existing Actor.
     * 
     * @param obj the Actor to update
     */
    public static void updateActor(Actor obj) {
        PerstContext.getInstance().storeModifiedObject(obj);
    }
    
    /**
     * Remove an Actor.
     * 
     * @param obj the Actor to remove
     */
    public static void removeActor(Actor obj) {
        PerstContext.getInstance().removeObject(obj);
    }
    
    // Generic operations for any Persistent object
    /**
     * Store a new object (Actor or PerstUser).
     * 
     * @param obj the object to store
     */
    public static void storeNewObject(Object obj) {
        if (obj instanceof Actor) {
            storeActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            storeNewUser((PerstUser) obj);
        }
    }
    
    /**
     * Store a modified object (Actor or PerstUser).
     * 
     * @param obj the object to store
     */
    public static void storeModifiedObject(Object obj) {
        if (obj instanceof Actor) {
            updateActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            storeModifiedUser((PerstUser) obj);
        }
    }
    
    /**
     * Retrieve all objects of a given class.
     * 
     * @param clazz the class to retrieve
     * @return collection of all objects
     */
    public static java.util.Collection retrieveAllObjects(Class clazz) {
        if (clazz == Actor.class) {
            return retrieveAllActors(Actor.class);
        } else if (clazz == PerstUser.class) {
            return retrieveAllUsers(PerstUser.class);
        }
        return new java.util.ArrayList();
    }
    
    /**
     * Retrieve an object by field value.
     * 
     * @param clazz the class to retrieve
     * @param field the field name
     * @param value the value to match
     * @return the object if found, null otherwise
     */
    public static Object retrieveObject(Class clazz, String field, String value) {
        if (clazz == Actor.class) {
            return retrieveActor((Class<Actor>) clazz, field, value);
        } else if (clazz == PerstUser.class) {
            return retrieveUser((Class<PerstUser>) clazz, field, value);
        }
        return null;
    }
    
    // PerstUser operations
    /**
     * Retrieve a PerstUser by field value.
     * 
     * @param clazz the PerstUser class
     * @param field the field name to search by
     * @param value the value to match
     * @return the PerstUser if found, null otherwise
     */
    public static PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveUser(clazz, field, value);
    }
    
    /**
     * Retrieve all PerstUsers.
     * 
     * @param clazz the PerstUser class
     * @return collection of all PerstUsers
     */
    public static java.util.Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        return PerstContext.getInstance().retrieveAllUsers(clazz);
    }
    
    /**
     * Store a new PerstUser.
     * 
     * @param obj the PerstUser to store
     */
    public static void storeNewUser(PerstUser obj) {
        PerstContext.getInstance().storeNewUser(obj);
    }
    
    /**
     * Update an existing PerstUser.
     * 
     * @param obj the PerstUser to update
     */
    public static void storeModifiedUser(PerstUser obj) {
        PerstContext.getInstance().storeModifiedUser(obj);
    }
    
    /**
     * Remove a PerstUser.
     * 
     * @param obj the PerstUser to remove
     */
    public static void removeUser(PerstUser obj) {
        PerstContext.getInstance().removeUser(obj);
    }
    
    /**
     * Start a transaction (currently no-op, for future use).
     */
    public static void startTransaction() {}
    
    /**
     * Commit a transaction (currently no-op, for future use).
     */
    public static void commitTransaction() {}
    
    /**
     * Rollback a transaction (currently no-op, for future use).
     */
    public static void rollbackTransaction() {}
}
