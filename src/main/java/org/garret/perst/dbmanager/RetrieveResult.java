package org.garret.perst.dbmanager;

/**
 * Result of retrieving an object with lazy-loaded collection info.
 */
public class RetrieveResult<T> {
    private final T object;
    private final long oid;
    private final long version;
    private final CollectionInfo[] largeCollections;
    
    private RetrieveResult(T object, long oid, long version, CollectionInfo[] largeCollections) {
        this.object = object;
        this.oid = oid;
        this.version = version;
        this.largeCollections = largeCollections;
    }
    
    public static <T> RetrieveResult<T> create(T object, long oid, long version) {
        return new RetrieveResult<>(object, oid, version, new CollectionInfo[0]);
    }
    
    public static <T> RetrieveResult<T> create(T object, long oid, long version, CollectionInfo[] collections) {
        return new RetrieveResult<>(object, oid, version, collections);
    }
    
    public T getObject() { return object; }
    public long getOid() { return oid; }
    public long getVersion() { return version; }
    public CollectionInfo[] getLargeCollections() { return largeCollections; }
    public boolean hasLargeCollections() { return largeCollections != null && largeCollections.length > 0; }
}
