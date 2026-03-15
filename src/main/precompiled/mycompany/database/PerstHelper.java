package mycompany.database;

import oodb.PerstContext;
import mycompany.domain.CDatabaseRoot;
import mycompany.domain.PerstUser;
import mycompany.domain.Actor;
import mycompany.domain.Agreement;
import mycompany.domain.Group;

/**
 * PerstHelper - Static helper for Perst operations.
 * 
 * <p><b>Thread Safety:</b> All methods are thread-safe and isolated per request.
 * This class delegates to PerstContext which supports both standard Storage
 * and CDatabase for versioning.
 * 
 * <p><b>Transactions:</b> When using CDatabase (versioning enabled), all write
 * operations require explicit transaction handling:
 * 
 * <pre>
 * PerstHelper.beginTransaction();
 * try {
 *     PerstHelper.storeNewObject(user);
 *     PerstHelper.commitTransaction();
 * } catch (Exception e) {
 *     PerstHelper.rollbackTransaction();
 * }
 * </pre>
 * 
 * <p><b>Usage:</b> All domain access should go through Manager classes (e.g., ActorManager)
 * which use PerstHelper internally. Direct PerstHelper usage is discouraged.
 */
public class PerstHelper {
    
    private PerstHelper() {}
    
    /**
     * Check if Perst is available for this thread's session.
     */
    public static boolean isAvailable() {
        return PerstContext.getInstance().isAvailable();
    }
    
    /**
     * Check if CDatabase versioning is enabled.
     */
    public static boolean isVersioningEnabled() {
        return PerstContext.getInstance().isVersioningEnabled();
    }
    
    /**
     * Get the PerstContext for this thread.
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
    
    // ==================== Transaction Operations ====================
    
    /**
     * Begin a new transaction.
     * Required for all write operations when CDatabase versioning is enabled.
     * 
     * @throws IllegalStateException if Perst is not available
     */
    public static void beginTransaction() {
        PerstContext.getInstance().beginTransaction();
    }
    
    /**
     * Commit the current transaction.
     * Saves all changes made since beginTransaction().
     * 
     * @throws IllegalStateException if Perst is not available
     * @throws Exception if commit fails
     */
    public static void commitTransaction() throws Exception {
        PerstContext.getInstance().commitTransaction();
    }
    
    /**
     * Rollback the current transaction.
     * Discards all changes made since beginTransaction().
     * 
     * @throws IllegalStateException if Perst is not available
     */
    public static void rollbackTransaction() {
        PerstContext.getInstance().rollbackTransaction();
    }
    
    // ==================== Actor Operations ====================
    
    /**
     * Retrieve an Actor by field value.
     */
    public static Actor retrieveActor(Class<Actor> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveActor(clazz, field, value);
    }
    
    /**
     * Retrieve an Actor by UUID.
     */
    public static Actor retrieveActorByUuid(String uuid) {
        return PerstContext.getInstance().retrieveActorByUuid(uuid);
    }
    
    /**
     * Retrieve all Actors.
     */
    public static java.util.Collection<Actor> retrieveAllActors(Class<Actor> clazz) {
        return PerstContext.getInstance().retrieveAllActors(clazz);
    }
    
    /**
     * Store a new Actor.
     */
    public static void storeActor(Actor obj) {
        PerstContext.getInstance().storeActor(obj);
    }
    
    /**
     * Update an existing Actor.
     */
    public static void updateActor(Actor obj) {
        PerstContext.getInstance().updateActor(obj);
    }
    
    /**
     * Remove an Actor.
     */
    public static void removeActor(Actor obj) {
        PerstContext.getInstance().removeActor(obj);
    }
    
    // ==================== PerstUser Operations ====================
    
    /**
     * Retrieve a PerstUser by field value.
     */
    public static PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveUser(clazz, field, value);
    }
    
    /**
     * Retrieve a PerstUser by ID.
     */
    public static PerstUser retrieveUserById(int userId) {
        return PerstContext.getInstance().retrieveUserById(userId);
    }
    
    /**
     * Retrieve all PerstUsers.
     */
    public static java.util.Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        return PerstContext.getInstance().retrieveAllUsers(clazz);
    }
    
    /**
     * Store a new PerstUser.
     */
    public static void storeUser(PerstUser obj) {
        PerstContext.getInstance().storeUser(obj);
    }
    
    /**
     * Update an existing PerstUser.
     */
    public static void updateUser(PerstUser obj) {
        PerstContext.getInstance().updateUser(obj);
    }
    
    /**
     * Remove a PerstUser.
     */
    public static void removeUser(PerstUser obj) {
        PerstContext.getInstance().removeUser(obj);
    }
    
    // ==================== Agreement Operations ====================
    
    /**
     * Retrieve all Agreements.
     */
    public static java.util.Collection<Agreement> retrieveAllAgreements() {
        return PerstContext.getInstance().retrieveAllAgreements();
    }
    
    /**
     * Store a new Agreement.
     */
    public static void storeAgreement(Agreement obj) {
        PerstContext.getInstance().storeAgreement(obj);
    }
    
    /**
     * Update an existing Agreement.
     */
    public static void updateAgreement(Agreement obj) {
        PerstContext.getInstance().updateAgreement(obj);
    }
    
    // ==================== Group Operations ====================
    
    /**
     * Retrieve all Groups.
     */
    public static java.util.Collection<Group> retrieveAllGroups() {
        return PerstContext.getInstance().retrieveAllGroups();
    }
    
    /**
     * Retrieve a Group by name.
     */
    public static Group retrieveGroupByName(String name) {
        return PerstContext.getInstance().retrieveGroupByName(name);
    }
    
    /**
     * Store a new Group.
     */
    public static void storeGroup(Group obj) {
        PerstContext.getInstance().storeGroup(obj);
    }
    
    /**
     * Update an existing Group.
     */
    public static void updateGroup(Group obj) {
        PerstContext.getInstance().updateGroup(obj);
    }
    
    // ==================== Generic Operations ====================
    
    /**
     * Store a new object (Actor or PerstUser).
     */
    public static void storeNewObject(Object obj) {
        if (obj instanceof Actor) {
            storeActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            storeUser((PerstUser) obj);
        } else if (obj instanceof Agreement) {
            storeAgreement((Agreement) obj);
        } else if (obj instanceof Group) {
            storeGroup((Group) obj);
        }
    }
    
    /**
     * Store a modified object (Actor or PerstUser).
     */
    public static void storeModifiedObject(Object obj) {
        if (obj instanceof Actor) {
            updateActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            updateUser((PerstUser) obj);
        } else if (obj instanceof Agreement) {
            updateAgreement((Agreement) obj);
        } else if (obj instanceof Group) {
            updateGroup((Group) obj);
        }
    }
    
    /**
     * Remove an object.
     */
    public static void removeObject(Object obj) {
        if (obj instanceof Actor) {
            removeActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            removeUser((PerstUser) obj);
        }
    }
    
    /**
     * Retrieve all objects of a given class.
     */
    public static java.util.Collection retrieveAllObjects(Class clazz) {
        if (clazz == Actor.class) {
            return retrieveAllActors(Actor.class);
        } else if (clazz == PerstUser.class) {
            return retrieveAllUsers(PerstUser.class);
        } else if (clazz == Agreement.class) {
            return retrieveAllAgreements();
        } else if (clazz == Group.class) {
            return retrieveAllGroups();
        }
        return new java.util.ArrayList();
    }
    
    /**
     * Retrieve an object by field value.
     */
    public static Object retrieveObject(Class clazz, String field, String value) {
        if (clazz == Actor.class) {
            return retrieveActor((Class<Actor>) clazz, field, value);
        } else if (clazz == PerstUser.class) {
            return retrieveUser((Class<PerstUser>) clazz, field, value);
        }
        return null;
    }
    
    // ==================== Version History Operations ====================
    
    /**
     * Get version history for an entity.
     * Only works when CDatabase versioning is enabled.
     * 
     * @param clazz the entity class
     * @param keyField the field to search by
     * @param keyValue the value to match
     * @return list of all versions (including current)
     */
    public static <T extends org.garret.perst.continuous.CVersion> java.util.List<T> getVersionHistory(
            Class<T> clazz, String keyField, String keyValue) {
        return PerstContext.getInstance().getVersionHistory(clazz, keyField, keyValue);
    }
    
    /**
     * Get current version of an entity.
     * 
     * @param clazz the entity class
     * @param keyField the field to search by
     * @param keyValue the value to match
     * @return the current version, or null if not found
     */
    public static <T extends org.garret.perst.continuous.CVersion> T getCurrentVersion(
            Class<T> clazz, String keyField, String keyValue) {
        return PerstContext.getInstance().getCurrentVersion(clazz, keyField, keyValue);
    }
}
