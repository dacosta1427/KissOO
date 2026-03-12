package org.garret.perst.dbmanager;

/**
 * Information about a potentially large collection.
 */
public class CollectionInfo {
    private final String fieldName;
    private final int estimatedSize;
    private final boolean shouldLazyLoad;
    
    public CollectionInfo(String fieldName, int estimatedSize, boolean shouldLazyLoad) {
        this.fieldName = fieldName;
        this.estimatedSize = estimatedSize;
        this.shouldLazyLoad = shouldLazyLoad;
    }
    
    public String getFieldName() { return fieldName; }
    public int getEstimatedSize() { return estimatedSize; }
    public boolean shouldLazyLoad() { return shouldLazyLoad; }
}
