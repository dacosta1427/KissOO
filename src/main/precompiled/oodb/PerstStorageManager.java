package oodb;

import org.garret.perst.Storage;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.StoreResult;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.continuous.MemoryStats;
import org.garret.perst.Key;
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
 * Delegates to CDatabase for all operations.
 */
public class PerstStorageManager {
    
    private static final String CDATABASE_KEY = "perstCDatabase";
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
            
            // Create fresh CDatabase instance to avoid stale singleton issues
            System.out.println("[PerstStorageManager] Creating new CDatabase instance...");
            CDatabase cdb = new CDatabase();
            CDatabase.instance = cdb;  // Update the static singleton
            
            storage = createStorage();
            
            try {
                cdb.open(storage, indexPath);
            } catch (ClassCastException e) {
                // RootObject cast error - existing database has incompatible root
                // Delete database and retry with fresh storage
                System.out.println("[PerstStorageManager] RootObject cast error, recreating database...");
                storage.close();
                java.io.File dbFile = new java.io.File(dbPath);
                java.io.File idxDir = new java.io.File(indexPath);
                if (dbFile.exists()) dbFile.delete();
                if (idxDir.exists()) deleteRecursively(idxDir);
                
                // Retry with fresh storage
                storage = createStorage();
                cdb.open(storage, indexPath);
            }
            
            org.kissweb.restServer.MainServlet.putEnvironment(CDATABASE_KEY, cdb);
            
            initialized = true;
            
            startOptimizerScheduler(cdb);
            
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
    
    public static CDatabase getDBManager() {
        if (!initialized) {
            initialize();
        }
        return (CDatabase) org.kissweb.restServer.MainServlet.getEnvironment(CDATABASE_KEY);
    }
    
    public static boolean isAvailable() {
        return getDBManager() != null;
    }
    
    // ========== TRANSACTION CONTROL ==========
    
    public static void beginTransaction() {
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            cdb.beginTransaction();
        }
    }
    
    public static void commitTransaction() throws Exception {
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            cdb.commitTransaction();
        }
    }
    
    public static void rollbackTransaction() {
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            cdb.rollbackTransaction();
        }
    }
    
    public static boolean isInTransaction() {
        CDatabase cdb = getDBManager();
        return cdb != null && cdb.isInTransaction();
    }
    
    // ========== RETRIEVE ==========
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, String value) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        
        try {
            IterableIterator<T> results = cdb.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, int value) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        
        try {
            IterableIterator<T> results = cdb.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T find(Class<T> clazz, String field, long value) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        
        try {
            IterableIterator<T> results = cdb.find(clazz, field, new Key(value));
            return getSingleton(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Find failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> List<T> getAll(Class<T> clazz) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return java.util.Collections.emptyList();
        
        try {
            IterableIterator<T> results = cdb.getRecords(clazz);
            return toList(results);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetAll failed: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    public static <T extends CVersion> T getByOid(Class<T> clazz, long oid) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        
        try {
            return cdb.getByOid(oid);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetByOid failed: " + e.getMessage());
            return null;
        }
    }
    
    public static <T extends CVersion> T getByUuid(Class<T> clazz, String uuid) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        
        try {
            return cdb.getByUuid(uuid);
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] GetByUuid failed: " + e.getMessage());
            return null;
        }
    }
    
    public static IterableIterator<CVersion> searchFullText(String query) {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        
        try {
            // fullTextSearch returns FullTextSearchResult[], need to adapt
            // For backward compatibility, return null - use fullTextSearch directly
            return null;
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] FullTextSearch failed: " + e.getMessage());
            return null;
        }
    }
    
    // ========== STORE (TransactionContainer) ==========
    
    public static TransactionContainer createContainer() {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        return cdb.createContainer();
    }
    
    public static TransactionContainer createSyncContainer() {
        CDatabase cdb = getDBManager();
        if (cdb == null) return null;
        return cdb.createSyncContainer();
    }
    
    public static boolean store(TransactionContainer container) {
        CDatabase cdb = getDBManager();
        if (cdb == null || container == null) return false;
        
        try {
            StoreResult result = cdb.store(container);
            return result.isSuccess();
        } catch (Exception e) {
            System.err.println("[PerstStorageManager] Store failed: " + e.getMessage());
            return false;
        }
    }
    
    // ========== HISTORY/Lex ==========
    
    public static void flushHistory() {
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            cdb.flushHistory();
        }
    }
    
    public static int getHistoryBufferSize() {
        CDatabase cdb = getDBManager();
        return cdb != null ? cdb.getHistoryBufferSize() : 0;
    }
    
    public static void setHistoryBufferSize(int threshold) {
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            cdb.setHistoryBufferSize(threshold);
        }
    }
    
    public static void setHistoryFlushInterval(int seconds) {
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            cdb.setHistoryFlushInterval(seconds);
        }
    }
    
    // ========== LIFECYCLE ==========
    
    public static synchronized void close() {
        CDatabase cdb = (CDatabase) org.kissweb.restServer.MainServlet.getEnvironment(CDATABASE_KEY);
        if (cdb != null) {
            try {
                cdb.close();
            } catch (Exception e) {
                System.err.println("[PerstStorageManager] Close failed: " + e.getMessage());
            }
            org.kissweb.restServer.MainServlet.putEnvironment(CDATABASE_KEY, null);
        }
        if (storage != null) {
            storage.close();
            storage = null;
        }
        stopOptimizerScheduler();
        initialized = false;
    }
    
    private static void startOptimizerScheduler(CDatabase cdb) {
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
                cdb.optimizeFullTextIndex();  // Fixes segment explosion
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
        
        CDatabase cdb = getDBManager();
        
        health.put("initialized", initialized);
        health.put("perstEnabled", PerstConfig.getInstance().isPerstEnabled());
        health.put("useCDatabase", PerstConfig.getInstance().isUseCDatabase());
        health.put("available", cdb != null);
        
        if (cdb != null) {
            try {
                health.put("inTransaction", cdb.isInTransaction());
                health.put("historyBufferSize", cdb.getHistoryBufferSize());
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
        
        CDatabase cdb = getDBManager();
        if (cdb != null) {
            try {
                MemoryStats memStats = cdb.getMemoryStats();
                stats.put("totalCollections", memStats.getTotalCollections());
                stats.put("largeCollections", memStats.getLargeCollections());
                stats.put("totalEstimatedSize", memStats.getTotalEstimatedSize());
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
    
    private static void deleteRecursively(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            if (children != null) {
                for (java.io.File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
