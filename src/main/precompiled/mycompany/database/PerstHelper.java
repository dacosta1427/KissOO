package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.PerstUser;
import mycompany.domain.Agreement;
import mycompany.domain.Group;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.CVersionHistory;
import java.util.Collection;
import java.util.List;

/**
 * PerstHelper - Facade for Perst database operations.
 */
public class PerstHelper {
    
    private PerstHelper() {
    }
    
    public static boolean isAvailable() {
        return oodb.PerstStorageManager.getDatabase() != null;
    }
    
    public static boolean isVersioningEnabled() {
        return oodb.PerstConfig.getInstance().isUseCDatabase();
    }
    
    public static oodb.PerstContext getContext() {
        return oodb.PerstContext.getInstance();
    }
    
    public static void initialize() {
        oodb.PerstStorageManager.initialize();
    }
    
    public static void beginTransaction() {
        oodb.PerstStorageManager.getDatabase().beginTransaction();
    }
    
    public static void commitTransaction() throws Exception {
        oodb.PerstStorageManager.getDatabase().commitTransaction();
    }
    
    public static void rollbackTransaction() {
        oodb.PerstStorageManager.getDatabase().rollbackTransaction();
    }
    
    // Actor operations
    public static Actor retrieveActor(Class<Actor> clazz, String field, String value) {
        return Actor.findByUuid(value);
    }
    
    public static Actor retrieveActorByUuid(String uuid) {
        return Actor.findByUuid(uuid);
    }
    
    public static Collection<Actor> retrieveAllActors(Class<Actor> clazz) {
        return Actor.getAll();
    }
    
    public static void storeActor(Actor actor) {
        actor.index();
        oodb.PerstStorageManager.getDatabase().insert(actor);
    }
    
    public static void updateActor(Actor actor) {
        actor.index();
    }
    
    public static void removeActor(Actor actor) {
        actor.removeIndex();
        oodb.PerstStorageManager.getDatabase().delete(actor);
    }
    
    // User operations
    public static PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        if ("userId".equals(field) || "id".equals(field)) {
            try {
                return PerstUser.get(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return PerstUser.getByUsername(value);
    }
    
    public static PerstUser retrieveUserById(int userId) {
        return PerstUser.get(userId);
    }
    
    public static Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        return PerstUser.getAll();
    }
    
    public static void storeUser(PerstUser user) {
        user.index();
        oodb.PerstStorageManager.getDatabase().insert(user);
    }
    
    public static void updateUser(PerstUser user) {
        user.index();
    }
    
    public static void removeUser(PerstUser user) {
        user.removeIndex();
        oodb.PerstStorageManager.getDatabase().delete(user);
    }
    
    // Agreement operations
    public static Collection<Agreement> retrieveAllAgreements() {
        return Agreement.getAll();
    }
    
    public static void storeAgreement(Agreement agreement) {
        oodb.PerstStorageManager.getDatabase().insert(agreement);
    }
    
    public static void updateAgreement(Agreement agreement) {
        // Implementation
    }
    
    // Group operations
    public static Collection<Group> retrieveAllGroups() {
        return Group.getAll();
    }
    
    public static Group retrieveGroupByName(String name) {
        return Group.findByName(name);
    }
    
    public static void storeGroup(Group group) {
        oodb.PerstStorageManager.getDatabase().insert(group);
    }
    
    public static void updateGroup(Group group) {
        // Implementation
    }
    
    // Generic object operations
    public static void storeNewObject(Object obj) {
        if (obj instanceof CVersion) {
            CVersion cv = (CVersion) obj;
            cv.index();
            oodb.PerstStorageManager.getDatabase().insert(cv);
        }
    }
    
    public static void storeModifiedObject(Object obj) {
        if (obj instanceof CVersion) {
            CVersion cv = (CVersion) obj;
            cv.index();
        }
    }
    
    public static void removeObject(Object obj) {
        if (obj instanceof CVersion) {
            CVersion cv = (CVersion) obj;
            if (cv instanceof Actor) {
                ((Actor) cv).removeIndex();
            } else if (cv instanceof PerstUser) {
                ((PerstUser) cv).removeIndex();
            }
            oodb.PerstStorageManager.getDatabase().delete(cv);
        }
    }
    
    public static Collection retrieveAllObjects(Class clazz) {
        Iterable results = oodb.PerstStorageManager.getDatabase().getRecords(clazz);
        return oodb.PerstStorageManager.getDatabase().toList(results.iterator());
    }
    
    public static Object retrieveObject(Class clazz, String field, String value) {
        // Simplified - would need field-based lookup
        return null;
    }
    
    // Version history operations
    public static <T extends CVersion> List<T> getVersionHistory(Class<T> clazz, String field, String value) {
        // Would need to use CVersionHistory to get all versions
        return null;
    }
    
    public static <T extends CVersion> T getCurrentVersion(Class<T> clazz, String field, String value) {
        // Get current version from version history
        return null;
    }
}