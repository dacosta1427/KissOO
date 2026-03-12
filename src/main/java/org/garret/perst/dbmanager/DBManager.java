package org.garret.perst.dbmanager;

import org.garret.perst.*;
import org.garret.perst.continuous.*;

/**
 * DBManager - Central manager for data retrieval and storage.
 * Like a librarian, it handles all requests to get and store objects.
 * 
 * Features:
 * - Optimistic locking via version tracking
 * - Lazy loading monitoring for large collections
 * - Multi-user concurrent access support
 */
public interface DBManager extends AutoCloseable {
    
    void initialize(Storage storage);
    
    void setLargeCollectionThreshold(int threshold);
    
    <T> RetrieveResult<T> get(long oid);
    
    <T> RetrieveResult<T> get(long oid, String... eagerCollections);
    
    <T extends CVersion> T getCurrent(CVersionHistory<T> versionHistory);
    
    <T extends CVersion> VersionedObject<T> getVersionForUpdate(CVersionHistory<T> versionHistory);
    
    <T> StoreResult<T> store(T object);
    
    <T extends CVersion> StoreResult<T> storeWithOptimisticLock(T object, long expectedVersion);
    
    <T extends CVersion> StoreResult<T> storeWithRetry(
        CVersionHistory<T> versionHistory,
        int maxAttempts,
        java.util.function.Function<T, T> updater
    );
    
    <T> Index<T> getIndex(Class<T> type, String fieldName);
    
    <T> Index<T> createIndex(Class<T> type, String fieldName, boolean unique);
    
    void beginTransaction();
    
    void commit();
    
    void rollback();
    
    boolean isInTransaction();
    
    void registerCollection(Object owner, String fieldName, int estimatedSize);
    
    boolean shouldLazyLoad(String fieldName, int estimatedSize);
    
    MemoryStats getMemoryStats();
    
    @Override
    void close();
}
