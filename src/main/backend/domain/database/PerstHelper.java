package domain.database;

import domain.kissweb.PerstContext;

/**
 * PerstHelper - Static helper for Perst operations.
 */
public class PerstHelper {
    
    private PerstHelper() {}
    
    public static boolean isAvailable() {
        return PerstContext.getInstance().isAvailable();
    }
    
    public static PerstContext getContext() {
        return PerstContext.getInstance();
    }
    
    public static void initialize() {
        PerstContext.getInstance().initialize();
    }
    
    // Actor operations
    public static Actor retrieveActor(Class<Actor> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveObject(clazz, field, value);
    }
    
    public static java.util.Collection<Actor> retrieveAllActors(Class<Actor> clazz) {
        return PerstContext.getInstance().retrieveAllObjects(clazz);
    }
    
    public static void storeActor(Actor obj) {
        PerstContext.getInstance().storeNewObject(obj);
    }
    
    public static void updateActor(Actor obj) {
        PerstContext.getInstance().storeModifiedObject(obj);
    }
    
    public static void removeActor(Actor obj) {
        PerstContext.getInstance().removeObject(obj);
    }
    
    // Generic operations for any Persistent object
    public static void storeNewObject(Object obj) {
        if (obj instanceof Actor) {
            storeActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            storeNewUser((PerstUser) obj);
        }
    }
    
    public static void storeModifiedObject(Object obj) {
        if (obj instanceof Actor) {
            updateActor((Actor) obj);
        } else if (obj instanceof PerstUser) {
            storeModifiedUser((PerstUser) obj);
        }
    }
    
    public static java.util.Collection retrieveAllObjects(Class clazz) {
        if (clazz == Actor.class) {
            return retrieveAllActors(Actor.class);
        } else if (clazz == PerstUser.class) {
            return retrieveAllUsers(PerstUser.class);
        }
        return new java.util.ArrayList();
    }
    
    public static Object retrieveObject(Class clazz, String field, String value) {
        if (clazz == Actor.class) {
            return retrieveActor((Class<Actor>) clazz, field, value);
        } else if (clazz == PerstUser.class) {
            return retrieveUser((Class<PerstUser>) clazz, field, value);
        }
        return null;
    }
    
    // PerstUser operations
    public static PerstUser retrieveUser(Class<PerstUser> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveUser(clazz, field, value);
    }
    
    public static java.util.Collection<PerstUser> retrieveAllUsers(Class<PerstUser> clazz) {
        return PerstContext.getInstance().retrieveAllUsers(clazz);
    }
    
    public static void storeNewUser(PerstUser obj) {
        PerstContext.getInstance().storeNewUser(obj);
    }
    
    public static void storeModifiedUser(PerstUser obj) {
        PerstContext.getInstance().storeModifiedUser(obj);
    }
    
    public static void removeUser(PerstUser obj) {
        PerstContext.getInstance().removeUser(obj);
    }
    
    public static void startTransaction() {}
    public static void commitTransaction() {}
    public static void rollbackTransaction() {}
}
