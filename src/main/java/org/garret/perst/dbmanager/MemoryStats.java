package org.garret.perst.dbmanager;

/**
 * Memory usage statistics for monitoring.
 */
public class MemoryStats {
    private final long usedMemory;
    private final long maxMemory;
    private final int loadedObjects;
    private final int lazyLoadedCollections;
    
    public MemoryStats(long usedMemory, long maxMemory, int loadedObjects, int lazyCollections) {
        this.usedMemory = usedMemory;
        this.maxMemory = maxMemory;
        this.loadedObjects = loadedObjects;
        this.lazyLoadedCollections = lazyCollections;
    }
    
    public long getUsedMemory() { return usedMemory; }
    public long getMaxMemory() { return maxMemory; }
    public int getLoadedObjects() { return loadedObjects; }
    public int getLazyLoadedCollections() { return lazyLoadedCollections; }
    
    public double getUsagePercent() { 
        return maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0; 
    }
    
    public boolean isLowMemory() {
        return getUsagePercent() > 80;
    }
    
    public static MemoryStats current() {
        Runtime rt = Runtime.getRuntime();
        return new MemoryStats(
            rt.totalMemory() - rt.freeMemory(),
            rt.maxMemory(),
            0,
            0
        );
    }
    
    @Override
    public String toString() {
        return String.format("MemoryStats{used=%dMB, max=%dMB, usage=%.1f%%}", 
            usedMemory / 1024 / 1024,
            maxMemory / 1024 / 1024,
            getUsagePercent());
    }
}
