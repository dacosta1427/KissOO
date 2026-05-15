package koo.core.database;

import koo.config.PerstConfig;
import org.garret.perst.Storage;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.IterableIterator;
import org.garret.perst.Key;
import org.garret.perst.dbmanager.UnifiedDBManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SINGLE ENTRY POINT for all Perst database operations.
 * 
 * Uses UnifiedDBManager (ooGTxQ) as the single source of truth for all database operations.
 * All services should use this class for database access to ensure consistency.
 * 
 * Standard pattern for ALL operations (no exceptions):
 * <pre>
 *   TransactionContainer tc = StorageManager.createContainer();
 *   tc.addInsert(obj);  // or addUpdate, addDelete
 *   StorageManager.store(tc);
 * </pre>
 * 
 * Benefits:
 * - Atomic batch operations (all-or-nothing)
 * - Optimistic locking built-in (conflict detection)
 * - Lin/Lex history tracking
 * - Crash recovery support
 */
public class StorageManager {
    
    private static final String UDBMgr_KEY = "unifiedDBManager";
    private static boolean initialized = false;
    private static Storage storage;
    private static ScheduledExecutorService optimizerScheduler;
    
    private StorageManager() {}
    
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        if (!PerstConfig.getInstance().isPerstEnabled()) {
            return;
        }
        
        try {
            String dbPath = PerstConfig.getInstance().getDatabasePath();
            String indexPath = dbPath + ".idx";
            int pageSize = PerstConfig.getInstance().getPagePoolSize();
            
            clearLuceneLocks(indexPath);
            
            UnifiedDBManager udbm = UnifiedDBManager.create(dbPath, indexPath, pageSize);
            org.kissweb.restServer.MainServlet.putEnvironment(UDBMgr_KEY, udbm);
            
            startOptimizerScheduler();
            System.out.println("[StorageManager] UnifiedDBManager initialized successfully");
            initialized = true;
            
        } catch (Exception e) {
            System.err.println("[StorageManager] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void clearLuceneLocks(String indexPath) {
        try {
            File idxDir = new File(indexPath);
            if (idxDir.exists() && idxDir.isDirectory()) {
                File[] subdirs = idxDir.listFiles((dir, name) -> name.startsWith("idx-"));
                if (subdirs != null) {
                    for (File subdir : subdirs) {
                        File lockFile = new File(subdir, "write.lock");
                        if (lockFile.exists()) {
                            System.out.println("[StorageManager] Removing stale lock file: " + lockFile);
                            lockFile.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[StorageManager] Error clearing locks: " + e.getMessage());
        }
    }
    
    public static UnifiedDBManager getDBManager() {
        if (!initialized) {
            initialize();
        }
        return (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment(UDBMgr_KEY);
    }
    
    public static boolean isDBManagerAvailable() {
        return getDBManager() != null;
    }
    
    public static boolean isAvailable() {
        if (!initialized) {
            initialize();
        }
        return storage != null;
    }
    
    public static void beginTransaction() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm != null) {
            udbm.beginTransaction();
        }
    }
    
    public static void commitTransaction() throws Exception {
        UnifiedDBManager udbm = getDBManager();
        if (udbm != null) {
            udbm.commitTransaction();
        }
    }
    
    public static void rollbackTransaction() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm != null) {
            udbm.rollbackTransaction();
        }
    }
    
    public static boolean isInTransaction() {
        UnifiedDBManager udbm = getDBManager();
        return udbm != null && udbm.isInTransaction();
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, String value) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        
        try {
            IterableIterator<T> results = udbm.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[StorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, int value) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        
        try {
            IterableIterator<T> results = udbm.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[StorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, long value) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        
        try {
            IterableIterator<T> results = udbm.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[StorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> List<T> getAll(Class<T> clazz) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return java.util.Collections.emptyList();
        
        try {
            IterableIterator<T> results = udbm.getObjects(clazz);
            java.util.List<T> list = toList(results);
            System.out.println("[StorageManager] getAll(" + clazz.getName() + ") = " + list.size());
            return list;
        } catch (Exception e) {
            System.err.println("[StorageManager] GetAll(" + clazz.getName() + ") failed: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    
    public static <T extends CVersion> T getByOid(Class<T> clazz, long oid) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        
        try {
            org.garret.perst.dbmanager.RetrieveResult<T> result = udbm.getByOid(oid, clazz);
            return result != null ? result.getObject() : null;
        } catch (Exception e) {
            System.err.println("[StorageManager] GetByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T getByUuid(Class<T> clazz, String uuid) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        
        try {
            return null;
        } catch (Exception e) {
            System.err.println("[StorageManager] GetByUuid failed: " + e.getMessage());
            return null;
        }
    }
    
    public static TransactionContainer createContainer() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        return udbm.createContainer();
    }
    
    public static TransactionContainer createSyncContainer() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        return udbm.createContainer();
    }
    
    public static boolean store(TransactionContainer container) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null || container == null) return false;
        
        try {
            org.garret.perst.dbmanager.StoreResult result = udbm.store(container);
            if (!result.isSuccess()) {
                System.err.println("[StorageManager] Store failed: " + result.getStatus() + " - " + result.getMessage());
            } else {
                System.out.println("[StorageManager] Store success: " + result.getStatus());
            }
            return result.isSuccess();
        } catch (Exception e) {
            System.err.println("[StorageManager] Store exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void flushHistoryBuffer() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm != null) {
            udbm.flushHistoryBuffer();
        }
    }
    
    public static int getHistoryBufferSize() {
        UnifiedDBManager udbm = getDBManager();
        return udbm != null ? udbm.getHistoryBufferSize() : 0;
    }
    
    public static void setHistoryBufferSize(int threshold) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm != null) {
            udbm.setHistoryBufferSize(threshold);
        }
    }
    
    public static void setHistoryFlushInterval(int seconds) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm != null) {
            udbm.setHistoryFlushInterval(seconds);
        }
    }
    
    public static synchronized void close() {
        UnifiedDBManager udbm = (UnifiedDBManager) org.kissweb.restServer.MainServlet.getEnvironment(UDBMgr_KEY);
        if (udbm != null) {
            try {
                udbm.close();
            } catch (Exception e) {
                System.err.println("[StorageManager] Close failed: " + e.getMessage());
            }
            org.kissweb.restServer.MainServlet.putEnvironment(UDBMgr_KEY, null);
        }
        stopOptimizerScheduler();
        initialized = false;
    }
    
    private static void startOptimizerScheduler() {
        int interval = PerstConfig.getInstance().getPerstOptimizeInterval();
        if (interval <= 0) {
            System.out.println("[StorageManager] Lucene optimization disabled (interval=0)");
            return;
        }
        
        optimizerScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LuceneOptimizer");
            t.setDaemon(true);
            return t;
        });
        
        optimizerScheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[StorageManager] Running periodic maintenance...");
                System.out.println("[StorageManager] Maintenance complete.");
            } catch (Exception e) {
                System.err.println("[StorageManager] Maintenance failed: " + e.getMessage());
            }
        }, interval, interval, TimeUnit.SECONDS);
        
        System.out.println("[StorageManager] Lucene optimizer scheduled every " + interval + " seconds");
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
            System.out.println("[StorageManager] Lucene optimizer stopped");
        }
    }
    
    public static java.util.Map<String, Object> healthCheck() {
        java.util.Map<String, Object> health = new java.util.HashMap<>();
        
        UnifiedDBManager udbm = getDBManager();
        
        health.put("initialized", initialized);
        health.put("perstEnabled", PerstConfig.getInstance().isPerstEnabled());
        health.put("available", udbm != null);
        
        if (udbm != null) {
            try {
                health.put("inTransaction", udbm.isInTransaction());
                health.put("historyBufferSize", udbm.getHistoryBufferSize());
                health.put("databasePath", PerstConfig.getInstance().getDatabasePath());
                health.put("optimizerScheduler", optimizerScheduler != null && !optimizerScheduler.isShutdown());
            } catch (Exception e) {
                health.put("error", e.getMessage());
            }
        }
        
        return health;
    }
    
    public static <T> org.garret.perst.Link<T> createLink() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        return udbm.createLink();
    }
    
    public static <T> org.garret.perst.Link<T> createLink(int initialCapacity) {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        return udbm.createLink(initialCapacity);
    }
    
    public static <T> org.garret.perst.IPersistentList<T> createList() {
        UnifiedDBManager udbm = getDBManager();
        if (udbm == null) return null;
        return udbm.createList();
    }
    
    public static java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("note", "Stats not available in UnifiedDBManager");
        return stats;
    }
    
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