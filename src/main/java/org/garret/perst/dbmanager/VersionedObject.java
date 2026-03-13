package org.garret.perst.dbmanager;

import java.util.Date;

/**
 * Wrapper containing a CVersion object and its version info.
 * Used in the optimistic locking workflow:
 * 1. User receives VersionedObject from getVersionForUpdate()
 * 2. User modifies the object
 * 3. User calls storeWithOptimisticLock() with the version
 * 4. If conflict, user gets new VersionedObject with current version
 */
public class VersionedObject<T> {
    private final T object;
    private final long versionId;
    private final long transactionId;
    private final Date timestamp;
    
    public VersionedObject(T object, long versionId, long transactionId, Date timestamp) {
        this.object = object;
        this.versionId = versionId;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
    }
    
    public T getObject() { return object; }
    public long getVersionId() { return versionId; }
    public long getTransactionId() { return transactionId; }
    public Date getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "VersionedObject{versionId=" + versionId + 
               ", transactionId=" + transactionId + 
               ", timestamp=" + timestamp + "}";
    }
}
