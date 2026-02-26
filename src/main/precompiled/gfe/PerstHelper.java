package gfe;

import org.garret.perst.continuous.CVersion;

/**
 * PerstHelper - Static helper for Perst operations.
 * 
 * Usage in services:
 * 
 * <pre>
 * void myService(JSONObject injson, JSONObject outjson, Connection db, Servlet servlet) {
 *     // PostgreSQL operations
 *     Record rec = db.newRecord("users");
 *     
 *     // Perst operations
 *     if (PerstHelper.isAvailable()) {
 *         Actor actor = PerstHelper.retrieveObject(Actor.class, uuid);
 *     }
 * }
 * </pre>
 * 
 * Or use PerstContext directly:
 * 
 * <pre>
 * PerstContext ctx = PerstContext.getInstance();
 * if (ctx.isAvailable()) {
 *     Collection<Actor> actors = ctx.retrieveAllObjects(Actor.class);
 * }
 * </pre>
 */
public class PerstHelper {
    
    private PerstHelper() {
        // Utility class
    }
    
    /**
     * Check if Perst is available
     */
    public static boolean isAvailable() {
        return PerstContext.getInstance().isAvailable();
    }
    
    /**
     * Get the PerstContext instance
     */
    public static PerstContext getContext() {
        return PerstContext.getInstance();
    }
    
    /**
     * Initialize Perst. Call once at startup.
     */
    public static void initialize() {
        PerstContext.getInstance().initialize();
    }
    
    /**
     * Retrieve a single object by class and UUID
     */
    public static <T extends CVersion> T retrieveObject(Class<T> clazz, String uuid) {
        return PerstContext.getInstance().retrieveObject(clazz, uuid);
    }
    
    /**
     * Retrieve a single object by class and indexed field
     */
    public static <T extends CVersion> T retrieveObject(Class<T> clazz, String field, String value) {
        return PerstContext.getInstance().retrieveObject(clazz, field, value);
    }
    
    /**
     * Retrieve all objects of a given type
     */
    public static <T extends CVersion> java.util.Collection<T> retrieveAllObjects(Class<T> clazz) {
        return PerstContext.getInstance().retrieveAllObjects(clazz);
    }
    
    /**
     * Store a new object to the database
     */
    public static void storeNewObject(CVersion obj) {
        PerstContext.getInstance().storeNewObject(obj);
    }
    
    /**
     * Store a modified object
     */
    public static void storeModifiedObject(CVersion obj) {
        PerstContext.getInstance().storeModifiedObject(obj);
    }
    
    /**
     * Remove an object from the database
     */
    public static void removeObject(CVersion obj) {
        PerstContext.getInstance().removeObject(obj);
    }
    
    /**
     * Start a Perst transaction
     */
    public static void startTransaction() {
        PerstContext.getInstance().startTransaction();
    }
    
    /**
     * Commit a Perst transaction
     */
    public static void commitTransaction() throws Exception {
        PerstContext.getInstance().endTransaction();
    }
    
    /**
     * Rollback a Perst transaction
     */
    public static void rollbackTransaction() {
        PerstContext.getInstance().rollbackTransaction();
    }
}
