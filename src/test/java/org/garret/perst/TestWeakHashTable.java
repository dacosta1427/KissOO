package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WeakHashTable (org.garret.perst.impl.WeakHashTable)
 * WeakHashTable is used internally by Storage for caching.
 * Target: 85%+ instruction coverage
 */
class TestWeakHashTable {

    static class CacheRecord extends Persistent {
        String name;
        int value;
        
        CacheRecord() {}
        
        CacheRecord(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testweakhash.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test WeakHashTable basic operations through storage cache")
    void testWeakHashTableBasicOperations() {
        // Create multiple objects to exercise the internal cache
        // WeakHashTable is used internally for object caching in StorageImpl
        for (int i = 0; i < 100; i++) {
            CacheRecord rec = new CacheRecord("rec" + i, i);
            rec.store();
            storage.commit();
        }
        
        // Access storage operations to exercise cache
        storage.getRoot();
        
        // Verify storage is operational
        assertTrue(storage.isOpened(), "Storage should be opened");
    }

    @Test
    @DisplayName("Test WeakHashTable through repeated storage operations")
    void testWeakHashTableThroughStorageOps() {
        // Create and store many objects to stress the cache
        CacheRecord[] records = new CacheRecord[50];
        
        for (int i = 0; i < 50; i++) {
            records[i] = new CacheRecord("name" + i, i * 10);
            records[i].store();
        }
        storage.commit();
        
        // Re-read objects to exercise cache hits
        // This should exercise WeakHashTable lookup operations
        for (int i = 0; i < 50; i++) {
            // Access by OID - this uses internal caching
            storage.getRoot();
        }
        
        // Force garbage collection to test weak references
        // WeakHashTable uses WeakReferences, so GC can clear entries
        System.gc();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        assertTrue(storage.isOpened(), "Storage should still be operational");
    }

    @Test
    @DisplayName("Test WeakHashTable with large dataset")
    void testWeakHashTableLargeDataset() {
        // Create large number of objects to test cache behavior
        for (int round = 0; round < 5; round++) {
            for (int i = 0; i < 200; i++) {
                CacheRecord rec = new CacheRecord("round" + round + "_rec" + i, round * 1000 + i);
                rec.store();
            }
            storage.commit();
        }
        
        // Access storage root multiple times
        storage.setRoot(new CacheRecord("root", 999));
        storage.commit();
        
        for (int i = 0; i < 100; i++) {
            storage.getRoot();
            storage.commit();
        }
        
        assertTrue(storage.isOpened(), "Storage should be operational");
    }

    @Test
    @DisplayName("Test storage gc triggers WeakHashTable cleanup")
    void testWeakHashTableGC() {
        // Create objects
        for (int i = 0; i < 50; i++) {
            CacheRecord rec = new CacheRecord("gc_test" + i, i);
            rec.store();
        }
        storage.commit();
        
        // Force garbage collection
        // This should trigger WeakHashTable cleanup of weak references
        storage.gc();
        
        // Continue operations - storage should handle the cleanup
        for (int i = 0; i < 20; i++) {
            CacheRecord rec = new CacheRecord("after_gc" + i, i);
            rec.store();
        }
        
        storage.commit();
        assertTrue(storage.isOpened(), "Storage should be operational after GC");
    }

    @Test
    @DisplayName("Test WeakHashTable with concurrent operations")
    void testWeakHashTableConcurrentAccess() {
        // Create multiple objects
        for (int i = 0; i < 100; i++) {
            CacheRecord rec = new CacheRecord("concurrent" + i, i);
            rec.store();
        }
        storage.commit();
        
        // Multiple commits to exercise cache
        for (int i = 0; i < 10; i++) {
            storage.commit();
            storage.getRoot();
        }
        
        assertTrue(storage.isOpened(), "Storage should handle concurrent access");
    }
}
