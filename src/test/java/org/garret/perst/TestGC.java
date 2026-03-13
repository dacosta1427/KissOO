package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestGC.java
 * Tests garbage collection functionality of Perst storage.
 */
class TestGC {

    static class PObject extends Persistent {
        long intKey;
        PObject next;
        String strKey;
    }

    static class StorageRoot extends Persistent {
        PObject list;
        Index strIndex;
        Index intIndex;
    }

    private Storage storage;
    private static final String TEST_DB = "testgc.dbs";

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
        new File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test GC with linked list and indexes")
    void testGCWithLinkedListAndIndexes() {
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        root.strIndex = storage.createIndex(String.class, true);
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index intIndex = root.intIndex;
        Index strIndex = root.strIndex;

        int nObjectsInTree = 100;
        int nIterations = 200;

        long insKey = 1999;
        long remKey = 1999;

        for (int i = 0; i < nIterations; i++) {
            if (i > nObjectsInTree) {
                remKey = (3141592621L * remKey + 2718281829L) % 1000000007L;
                intIndex.remove(new Key(remKey));
                strIndex.remove(new Key(Long.toString(remKey)));
            }

            PObject obj = new PObject();
            insKey = (3141592621L * insKey + 2718281829L) % 1000000007L;
            obj.intKey = insKey;
            obj.strKey = Long.toString(insKey);
            obj.next = new PObject();
            intIndex.put(new Key(obj.intKey), obj);
            strIndex.put(new Key(obj.strKey), obj);

            root.list = new PObject();
            root.list.intKey = i;
            root.store();

            if (i % 50 == 0) {
                storage.commit();
            }
        }

        // Verify that we have objects in the index
        assertTrue(intIndex.size() > 0, "intIndex should contain objects");
        assertTrue(strIndex.size() > 0, "strIndex should contain objects");
    }

    @Test
    @DisplayName("Test GC threshold setting")
    void testGCThresholdSetting() {
        // Test that GC threshold can be set
        storage.setGcThreshold(500000);
        
        // Create some objects
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Add objects
        for (int i = 0; i < 10; i++) {
            PObject obj = new PObject();
            obj.intKey = i;
            root.intIndex.put(new Key((long) i), obj);
        }
        storage.commit();

        // Verify objects were added
        assertEquals(10, root.intIndex.size(), "Should have 10 objects in index");
    }

    @Test
    @DisplayName("Test GC with background option")
    void testGCWithBackgroundOption() {
        // Set background GC property
        storage.setProperty("perst.background.gc", Boolean.TRUE);
        
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Add and remove objects to trigger GC
        for (int i = 0; i < 50; i++) {
            PObject obj = new PObject();
            obj.intKey = i;
            root.intIndex.put(new Key((long) i), obj);
        }
        storage.commit();

        // Remove some objects
        for (int i = 0; i < 25; i++) {
            root.intIndex.remove(new Key((long) i));
        }
        storage.commit();

        assertEquals(25, root.intIndex.size(), "Should have 25 objects after removal");
    }

    @Test
    @DisplayName("Test GC with alternative btree")
    void testGCWithAltBtree() {
        // Set alternative btree property
        storage.setProperty("perst.alternative.btree", Boolean.TRUE);
        
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Add objects
        for (int i = 0; i < 20; i++) {
            PObject obj = new PObject();
            obj.intKey = i;
            root.intIndex.put(new Key((long) i), obj);
        }
        storage.commit();

        assertEquals(20, root.intIndex.size(), "Should have 20 objects");
    }

    @Test
    @DisplayName("Test object deallocation")
    void testObjectDeallocation() {
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Create and store objects
        PObject obj1 = new PObject();
        obj1.intKey = 1;
        obj1.strKey = "one";
        
        PObject obj2 = new PObject();
        obj2.intKey = 2;
        obj2.strKey = "two";

        root.intIndex.put(new Key(1L), obj1);
        root.intIndex.put(new Key(2L), obj2);
        storage.commit();

        assertEquals(2, root.intIndex.size(), "Should have 2 objects");

        // Remove and deallocate
        root.intIndex.remove(new Key(1L));
        obj1.deallocate();
        storage.commit();

        assertEquals(1, root.intIndex.size(), "Should have 1 object after removal");
    }

    @Test
    @DisplayName("Test GC explicit invocation")
    void testGCExplicitInvocation() {
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Create objects
        for (int i = 0; i < 100; i++) {
            PObject obj = new PObject();
            obj.intKey = i;
            root.intIndex.put(new Key((long) i), obj);
        }
        storage.commit();

        // Remove all objects
        for (int i = 0; i < 100; i++) {
            root.intIndex.remove(new Key((long) i));
        }
        storage.commit();

        // Explicitly invoke GC
        storage.gc();

        assertEquals(0, root.intIndex.size(), "Index should be empty");
    }

    @Test
    @DisplayName("Test GC with cyclic references")
    void testGCWithCyclicReferences() {
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        storage.setRoot(root);

        // Create cyclic references
        PObject obj1 = new PObject();
        PObject obj2 = new PObject();
        PObject obj3 = new PObject();

        obj1.next = obj2;
        obj2.next = obj3;
        obj3.next = obj1; // Cycle

        root.list = obj1;
        storage.commit();

        // Break the reference
        root.list = null;
        storage.commit();

        // GC should handle cyclic references
        storage.gc();
    }

    @Test
    @DisplayName("Test GC memory dump")
    void testGCMemoryDump() {
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Create objects
        for (int i = 0; i < 50; i++) {
            PObject obj = new PObject();
            obj.intKey = i;
            obj.strKey = "string" + i;
            root.intIndex.put(new Key((long) i), obj);
        }
        storage.commit();

        // Get memory dump
        java.util.HashMap<Class, MemoryUsage> dump = storage.getMemoryDump();
        assertNotNull(dump, "Memory dump should not be null");
        assertTrue(dump.size() > 0, "Memory dump should have entries");
    }

    @Test
    @DisplayName("Test GC with multiple indexes")
    void testGCWithMultipleIndexes() {
        storage.setGcThreshold(1000000);
        StorageRoot root = new StorageRoot();
        root.intIndex = storage.createIndex(long.class, true);
        root.strIndex = storage.createIndex(String.class, true);
        storage.setRoot(root);

        // Create objects in both indexes
        for (int i = 0; i < 30; i++) {
            PObject obj = new PObject();
            obj.intKey = i;
            obj.strKey = "key" + i;
            root.intIndex.put(new Key((long) i), obj);
            root.strIndex.put(new Key(obj.strKey), obj);
        }
        storage.commit();

        assertEquals(30, root.intIndex.size(), "intIndex should have 30 objects");
        assertEquals(30, root.strIndex.size(), "strIndex should have 30 objects");

        // Remove from one index only
        for (int i = 0; i < 15; i++) {
            root.intIndex.remove(new Key((long) i));
        }
        storage.commit();

        assertEquals(15, root.intIndex.size(), "intIndex should have 15 objects");
        // strIndex still has references to all objects
        assertEquals(30, root.strIndex.size(), "strIndex should still have 30 objects");
    }
}
