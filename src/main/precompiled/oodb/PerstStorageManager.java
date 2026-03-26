package oodb;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Key;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.dbmanager.UnifiedDBManagerImpl;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.dbmanager.StoreResult;
import org.garret.perst.IterableIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SINGLE ENTRY POINT for all Perst database operations.
 * 
 * Standard pattern for ALL operations (no exceptions):
 * <pre>
 *   TransactionContainer tc = PerstStorageManager.createContainer();
 *   tc.addInsert(obj);  // or addUpdate, addDelete
 *   PerstStorageManager.store(tc);
 * </pre>
 * 
 * Benefits:
 * - Atomic batch operations (all-or-nothing)
 * - Optimistic locking built-in (conflict detection)
 * - Lin/Lex history tracking
 * - Crash recovery support
 * 
 * Delegates to UnifiedDBManager for all operations.
 */
public class PerstStorageManager {
    
    private static final String DBMANAGER_KEY = "perstDBManager";
    private static boolean initialized = false;
    private static Storage storage;
    private static ScheduledExecutorService optimizerScheduler;
    
    private PerstStorageManager() {}
    
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        if (!PerstConfig.getInstance().isPerstEnabled()) {
            return;
        }
        
        if (!PerstConfig.getInstance().isUseCDatabase()) {
            initialized = true;
            return;
        }
        
        try {
            String dbPath = PerstConfig.getInstance().getDatabasePath();
            String indexPath = dbPath + ".idx";
            
            storage = createStorage();
            
            UnifiedDBManager dbm = new UnifiedDBManagerImpl();
            dbm.open(storage, indexPath);
            
            org.kissweb.restServer.MainServlet.putEnvironment(DBMANAGER_KEY, dbm);
            
            initialized = true;
            
            startOptimizerScheduler(dbm);
            
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Storage createStorage() throws Exception {
        Storage storage = org.garret.perst.StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.serialize.transient.objects", java.lang.Boolean.FALSE);
        storage.setProperty("perst.file.noflush", PerstConfig.getInstance().isPerstNoflush());
        
        String dbPath = PerstConfig.getInstance().getDatabasePath();
        int poolSize = PerstConfig.getInstance().getPagePoolSize();
        
        java.io.File dbFile = new java.io.File(dbPath);
        java.io.File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        storage.open(dbPath, poolSize);
        return storage;
    }
    
    public static UnifiedDBManager getDBManager() {
        if (!initialized) {
            initialize();
        }
        return (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment(DBMANAGER_KEY);
    }
    
    public static boolean isAvailable() {
        return getDBManager() != null;
    }
    
    // ========== TRANSACTION CONTROL ==========
    // Note: Transaction state is managed by UnifiedDBManager.
    // If transaction leak issues are suspected, see ChangeNote-UDBM-Improvements.md
    
    public static void beginTransaction() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.beginTransaction();
        }
    }
    
    public static void commitTransaction() throws Exception {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.commitTransaction();
        }
    }
    
    public static void rollbackTransaction() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.rollbackTransaction();
        }
    }
    
    public static boolean isInTransaction() {
        UnifiedDBManager dbm = getDBManager();
        return dbm != null && dbm.isInTransaction();
    }
    
    // ========== RETRIEVE ==========
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, String value) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            IterableIterator<T> results = dbm.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, int value) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            IterableIterator<T> results = dbm.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, long value) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            IterableIterator<T> results = dbm.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> List<T> getAll(Class<T> clazz) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return java.util.Collections.emptyList();
        
        try {
            IterableIterator<T> results = dbm.getRecords(clazz);
            return toList(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetAll failed: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    public static <T extends CVersion> T getByOid(Class<T> clazz, long oid) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            org.garret.perst.dbmanager.RetrieveResult<T> result = dbm.getByOid(oid);
            return result != null ? result.getObject() : null;
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T getByUuid(Class<T> clazz, String uuid) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            return dbm.getByUuid(uuid);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetByUuid failed: " + e.getMessage());
            return null;
        }
    }
    
    public static IterableIterator<CVersion> searchFullText(String query) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        
        try {
            return dbm.searchFullText(query);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] FullTextSearch failed: " + e.getMessage());
            return null;
        }
    }
    
    // ========== STORE (TransactionContainer) ==========
    
    public static TransactionContainer createContainer() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        return dbm.createContainer();
    }
    
    public static TransactionContainer createSyncContainer() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null) return null;
        return dbm.createSyncContainer();
    }
    
    public static boolean store(TransactionContainer container) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm == null || container == null) return false;
        
        try {
            StoreResult result = dbm.store(container);
            return result.isSuccess();
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Store failed: " + e.getMessage());
            return false;
        }
    }
    
    // ========== HISTORY/Lex ==========
    
    public static void flushHistory() {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.flushHistoryBuffer();
        }
    }
    
    public static int getHistoryBufferSize() {
        UnifiedDBManager dbm = getDBManager();
        return dbm != null ? dbm.getHistoryBufferSize() : 0;
    }
    
    public static void setHistoryBufferSize(int threshold) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.setHistoryBufferSize(threshold);
        }
    }
    
    public static void setHistoryFlushInterval(int seconds) {
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            dbm.setHistoryFlushInterval(seconds);
        }
    }
    
    // ========== LIFECYCLE ==========
    
    public static synchronized void close() {
        UnifiedDBManager dbm = (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment(DBMANAGER_KEY);
        if (dbm != null) {
            try {
                dbm.close();
            } catch (Exception e) {
                System.err.println("[PerstStorageManager] Close failed: " + e.getMessage());
            }
            org.kissweb.restServer.MainServlet.putEnvironment(DBMANAGER_KEY, null);
        }
        if (storage != null) {
            storage.close();
            storage = null;
        }
        stopOptimizerScheduler();
        initialized = false;
    }
    
    private static void startOptimizerScheduler(UnifiedDBManager dbm) {
        int interval = PerstConfig.getInstance().getPerstOptimizeInterval();
        if (interval <= 0) {
            System.out.println("[PerstStorageManager] Lucene optimization disabled (interval=0)");
            return;
        }
        
        optimizerScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LuceneOptimizer");
            t.setDaemon(true);
            return t;
        });
        
        optimizerScheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[PerstStorageManager] Running Lucene full-text index optimization...");
                dbm.flushHistoryBuffer();
                System.out.println("[PerstStorageManager] Lucene optimization complete.");
            } catch (Exception e) {
                System.err.println("[PerstStorageManager] Lucene optimization failed: " + e.getMessage());
            }
        }, interval, interval, TimeUnit.SECONDS);
        
        System.out.println("[PerstStorageManager] Lucene optimizer scheduled every " + interval + " seconds");
    }
    
    private static void stopOptimizerScheduler() {
        if (optimizerScheduler != null) {
            optimizerScheduler.shutdown();
            try {
                if (!optimizerScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    optimizerScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                optimizerScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            optimizerScheduler = null;
            System.out.println("[PerstStorageManager] Lucene optimizer stopped");
        }
    }
    
    // ========== HEALTH CHECK ==========
    
    public static java.util.Map<String, Object> healthCheck() {
        java.util.Map<String, Object> health = new java.util.HashMap<>();
        
        UnifiedDBManager dbm = getDBManager();
        
        health.put("initialized", initialized);
        health.put("perstEnabled", PerstConfig.getInstance().isPerstEnabled());
        health.put("useCDatabase", PerstConfig.getInstance().isUseCDatabase());
        health.put("available", dbm != null);
        
        if (dbm != null) {
            try {
                health.put("inTransaction", dbm.isInTransaction());
                health.put("historyBufferSize", dbm.getHistoryBufferSize());
                health.put("databasePath", PerstConfig.getInstance().getDatabasePath());
                health.put("optimizerScheduler", optimizerScheduler != null && !optimizerScheduler.isShutdown());
            } catch (Exception e) {
                health.put("error", e.getMessage());
            }
        }
        
        if (storage != null) {
            try {
                health.put("databaseSize", storage.getDatabaseSize());
                health.put("usedSize", storage.getUsedSize());
            } catch (Exception e) {
                health.put("storageError", e.getMessage());
            }
        }
        
        return health;
    }
    
    public static java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        UnifiedDBManager dbm = getDBManager();
        if (dbm != null) {
            try {
                org.garret.perst.dbmanager.MemoryStats memStats = dbm.getMemoryStats();
                stats.put("usedMemory", memStats.getUsedMemory());
                stats.put("maxMemory", memStats.getMaxMemory());
                stats.put("usagePercent", memStats.getUsagePercent());
                stats.put("loadedObjects", memStats.getLoadedObjects());
                stats.put("lazyLoadedCollections", memStats.getLazyLoadedCollections());
                stats.put("lowMemory", memStats.isLowMemory());
            } catch (Exception e) {
                stats.put("error", e.getMessage());
            }
        }
        
        return stats;
    }
    
    // ========== HELPER METHODS ==========
    
    private static <T> T getSingleton(IterableIterator<T> iter) {
        if (iter == null || !iter.hasNext()) {
            return null;
        }
        T result = iter.next();
        return result;
    }
    
    private static <T> List<T> toList(IterableIterator<T> iter) {
        List<T> list = new ArrayList<>();
        if (iter != null) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }
}
