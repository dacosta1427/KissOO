package org.garret.perst.dbmanager;

/**
 * Result of an optimistic locking operation.
 * Contains the outcome and any conflict information.
 */
public class StoreResult<T> {
    private final Status status;
    private final T object;
    private final T conflictObject;
    private final long version;
    private final String message;
    
    private StoreResult(Status status, T object, T conflictObject, long version, String message) {
        this.status = status;
        this.object = object;
        this.conflictObject = conflictObject;
        this.version = version;
        this.message = message;
    }
    
    public static <T> StoreResult<T> success(T object, long version) {
        return new StoreResult<>(Status.SUCCESS, object, null, version, null);
    }
    
    public static <T> StoreResult<T> conflict(T conflictObject, long currentVersion) {
        return new StoreResult<>(Status.CONFLICT, null, conflictObject, currentVersion, 
            "Optimistic lock conflict: expected version differs from current version");
    }
    
    public static <T> StoreResult<T> error(String message) {
        return new StoreResult<>(Status.ERROR, null, null, -1, message);
    }
    
    public enum Status {
        SUCCESS,
        CONFLICT,
        ERROR
    }
    
    public Status getStatus() { return status; }
    public T getObject() { return object; }
    public T getConflictObject() { return conflictObject; }
    public long getVersion() { return version; }
    public String getMessage() { return message; }
    
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isConflict() { return status == Status.CONFLICT; }
    public boolean isError() { return status == Status.ERROR; }
}
