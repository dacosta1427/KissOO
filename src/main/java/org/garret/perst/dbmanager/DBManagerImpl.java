package org.garret.perst.dbmanager;

import org.garret.perst.*;
import org.garret.perst.continuous.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Implementation of DBManager - central manager for data retrieval and storage.
 * Acts like a librarian for Perst database operations.
 */
public class DBManagerImpl implements DBManager {
    
    private Storage storage;
    private int largeCollectionThreshold = 1000;
    private final Map<Long, TransactionState> transactions = new ConcurrentHashMap<>();
    private final Map<String, CollectionInfo> monitoredCollections = new ConcurrentHashMap<>();
    private final LazyCollectionMonitor monitor = new LazyCollectionMonitor();
    
    private static class TransactionState {
        final Storage storage;
        final long startTime;
        boolean committed = false;
        boolean rolledBack = false;
        
        TransactionState(Storage storage) {
            this.storage = storage;
            this.startTime = System.currentTimeMillis();
        }
    }
    
    @Override
    public void initialize(Storage storage) {
        this.storage = storage;
    }
    
    @Override
    public void setLargeCollectionThreshold(int threshold) {
        this.largeCollectionThreshold = threshold;
    }
    
    @Override
    public <T> RetrieveResult<T> get(long oid) {
        return get(oid, new String[0]);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> RetrieveResult<T> get(long oid, String... eagerCollections) {
        if (storage == null) {
            throw new IllegalStateException("DBManager not initialized");
        }
        
        IPersistent obj = (IPersistent) storage.getObjectByOID((int) oid);
        if (obj == null) {
            return null;
        }
        
        long version = -1;
        if (obj instanceof CVersion) {
            version = ((CVersion) obj).getId();
        }
        
        List<CollectionInfo> largeCollections = new ArrayList<>();
        analyzeObjectCollections(obj, largeCollections, eagerCollections);
        
        return RetrieveResult.create((T) obj, oid, version, 
            largeCollections.toArray(new CollectionInfo[0]));
    }
    
    private void analyzeObjectCollections(Object obj, List<CollectionInfo> largeCollections, 
            String[] eagerCollections) {
        if (obj == null) return;
        
        Set<String> eagerSet = new HashSet<>(Arrays.asList(eagerCollections));
        
        for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                if (fieldValue instanceof Collection) {
                    Collection<?> coll = (Collection<?>) fieldValue;
                    int size = coll.size();
                    boolean shouldLazy = shouldLazyLoad(field.getName(), size);
                    boolean isEager = eagerSet.contains(field.getName());
                    
                    if (shouldLazy && !isEager) {
                        largeCollections.add(new CollectionInfo(field.getName(), size, shouldLazy));
                    }
                }
            } catch (IllegalAccessException e) {
                // Skip inaccessible fields
            }
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CVersion> T getCurrent(CVersionHistory<T> versionHistory) {
        if (versionHistory == null) {
            return null;
        }
        return versionHistory.getCurrent();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CVersion> VersionedObject<T> getVersionForUpdate(CVersionHistory<T> versionHistory) {
        if (versionHistory == null) {
            throw new IllegalArgumentException("Version history cannot be null");
        }
        
        T current = versionHistory.getCurrent();
        if (current == null) {
            return null;
        }
        
        long versionId = current.getId();
        long transId = current.getTransactionId();
        java.util.Date timestamp = current.getDate();
        
        T workingCopy = versionHistory.update();
        
        return new VersionedObject<>(workingCopy, versionId, transId, timestamp);
    }
    
    @Override
    public <T> StoreResult<T> store(T object) {
        if (storage == null) {
            return StoreResult.error("DBManager not initialized");
        }
        
        try {
            if (object instanceof IPersistent) {
                IPersistent persistent = (IPersistent) object;
                if (persistent.getOid() == 0) {
                    storage.makePersistent(persistent);
                }
                storage.commit();
                return StoreResult.success(object, persistent.getOid());
            }
            return StoreResult.error("Object is not persistent capable");
        } catch (Exception e) {
            return StoreResult.error("Storage failed: " + e.getMessage());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CVersion> StoreResult<T> storeWithOptimisticLock(T object, long expectedVersion) {
        if (storage == null) {
            return StoreResult.error("DBManager not initialized");
        }
        
        try {
            CVersionHistory<T> history = object.getVersionHistory();
            T current = history.getCurrent();
            
            if (current == null) {
                return StoreResult.error("No current version found");
            }
            
            long currentVersion = current.getId();
            
            if (currentVersion != expectedVersion) {
                storage.rollback();
                return StoreResult.conflict(current, currentVersion);
            }
            
            storage.commit();
            return StoreResult.success(object, currentVersion + 1);
            
        } catch (Exception e) {
            try { storage.rollback(); } catch (Exception ignored) {}
            return StoreResult.error("Optimistic lock failed: " + e.getMessage());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CVersion> StoreResult<T> storeWithRetry(
            CVersionHistory<T> versionHistory,
            int maxAttempts,
            Function<T, T> updater) {
        
        if (versionHistory == null) {
            return StoreResult.error("Version history cannot be null");
        }
        
        int attempts = 0;
        StoreResult<T> lastResult = null;
        
        while (attempts < maxAttempts) {
            attempts++;
            
            VersionedObject<T> versioned = getVersionForUpdate(versionHistory);
            if (versioned == null) {
                return StoreResult.error("No version available for update");
            }
            
            T modified = updater.apply(versioned.getObject());
            if (modified == null) {
                return StoreResult.error("Updater returned null");
            }
            
            lastResult = storeWithOptimisticLock(modified, versioned.getVersionId());
            
            if (lastResult.isSuccess()) {
                return lastResult;
            }
            
            if (lastResult.isConflict()) {
                T current = lastResult.getConflictObject();
                versionHistory = current.getVersionHistory();
            }
        }
        
        return lastResult != null ? lastResult : 
            StoreResult.error("Max retry attempts exceeded");
    }
    
    @Override
    public <T> Index<T> getIndex(Class<T> type, String fieldName) {
        // Index is typically accessed through the root object or via query
        return null;
    }
    
    @Override
    public <T> Index<T> createIndex(Class<T> type, String fieldName, boolean unique) {
        if (storage == null) {
            throw new IllegalStateException("DBManager not initialized");
        }
        
        // Note: Perst creates indexes automatically via @Indexable annotation
        // or via Database class. Manual index creation with field name requires
        // using Database.createIndex() or FieldIndex
        Index<T> index = storage.createIndex(type, unique);
        storage.commit();
        return index;
    }
    
    @Override
    public void beginTransaction() {
        if (storage == null) {
            throw new IllegalStateException("DBManager not initialized");
        }
        
        long threadId = Thread.currentThread().threadId();
        if (transactions.containsKey(threadId)) {
            throw new IllegalStateException("Transaction already in progress for this thread");
        }
        
        storage.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
        transactions.put(threadId, new TransactionState(storage));
    }
    
    @Override
    public void commit() {
        long threadId = Thread.currentThread().threadId();
        TransactionState state = transactions.remove(threadId);
        
        if (state == null) {
            throw new IllegalStateException("No transaction to commit");
        }
        
        if (state.rolledBack) {
            throw new IllegalStateException("Transaction was already rolled back");
        }
        
        state.committed = true;
        storage.commit();
    }
    
    @Override
    public void rollback() {
        long threadId = Thread.currentThread().threadId();
        TransactionState state = transactions.remove(threadId);
        
        if (state == null) {
            throw new IllegalStateException("No transaction to rollback");
        }
        
        state.rolledBack = true;
        storage.rollback();
    }
    
    @Override
    public boolean isInTransaction() {
        return transactions.containsKey(Thread.currentThread().threadId());
    }
    
    @Override
    public void registerCollection(Object owner, String fieldName, int estimatedSize) {
        String key = owner.getClass().getName() + "." + fieldName;
        boolean shouldLazy = shouldLazyLoad(fieldName, estimatedSize);
        monitoredCollections.put(key, new CollectionInfo(fieldName, estimatedSize, shouldLazy));
    }
    
    @Override
    public boolean shouldLazyLoad(String fieldName, int estimatedSize) {
        return monitor.shouldLazyLoad(fieldName, estimatedSize);
    }
    
    @Override
    public MemoryStats getMemoryStats() {
        Runtime rt = Runtime.getRuntime();
        return new MemoryStats(
            rt.totalMemory() - rt.freeMemory(),
            rt.maxMemory(),
            monitoredCollections.size(),
            (int) monitoredCollections.values().stream()
                .filter(CollectionInfo::shouldLazyLoad)
                .count()
        );
    }
    
    @Override
    public void close() {
        for (Long threadId : transactions.keySet()) {
            try {
                storage.rollback();
            } catch (Exception ignored) {}
        }
        transactions.clear();
    }
    
    private class LazyCollectionMonitor {
        private volatile boolean memoryPressure = false;
        
        public boolean shouldLazyLoad(String fieldName, int estimatedSize) {
            return estimatedSize > largeCollectionThreshold || memoryPressure;
        }
        
        public void checkMemory() {
            Runtime rt = Runtime.getRuntime();
            long used = rt.totalMemory() - rt.freeMemory();
            long max = rt.maxMemory();
            
            if (used > max * 0.8) {
                memoryPressure = true;
            } else {
                memoryPressure = false;
            }
        }
    }
}
