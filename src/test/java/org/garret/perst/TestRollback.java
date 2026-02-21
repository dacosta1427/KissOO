package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRollback.java
 * Tests transaction rollback functionality
 */
class TestRollback {

    static class Record1 extends Persistent {
        int count;
    }

    static class Record2 {
        int count;
    }

    private Storage storage;
    private static final String TEST_DB = "testrollback.dbs";

    @BeforeEach
    void setUp() throws Exception {
        // Clean up any existing database file
        new File(TEST_DB).delete();
        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.reload.objects.on.rollback", Boolean.TRUE);
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
    @DisplayName("Test rollback of modified objects")
    void testRollbackModifiedObjects() throws Exception {
        IPersistentSet root = (IPersistentSet) storage.getRoot();
        if (root == null) {
            root = storage.createSet();
            storage.setRoot(root);
        }

        // Create initial records
        Record1 t1 = new Record1();
        t1.count = 1;
        root.add(t1);

        Record1 t2 = new Record1();
        t2.count = 2;
        root.add(t2);

        Record2 t3 = new Record2();
        t3.count = 3;
        root.add(t3);

        storage.commit();

        // Modify objects
        t1.count += 100;
        t1.modify();

        t2.count += 100;
        t2.store();

        t3.count += 100;
        storage.modify(t3);

        // Add new objects
        Record1 t4 = new Record1();
        t4.count = 4;
        root.add(t4);

        Record2 t5 = new Record2();
        t5.count = 5;
        root.add(t5);

        // Rollback
        storage.rollback();

        // Verify rollback worked
        // Note: With perst.reload.objects.on.rollback=true, objects are reloaded
        // But the original objects may not be updated in memory, so we need to re-fetch
        assertEquals(1, t1.count, "t1 count should be rolled back to 1");
        assertEquals(2, t2.count, "t2 count should be rolled back to 2");
        assertEquals(3, t3.count, "t3 count should be rolled back to 3");
    }

    @Test
    @DisplayName("Test rollback with new objects")
    void testRollbackNewObjects() throws Exception {
        IPersistentSet root = (IPersistentSet) storage.getRoot();
        if (root == null) {
            root = storage.createSet();
            storage.setRoot(root);
        }

        // Create and commit initial record
        Record1 t1 = new Record1();
        t1.count = 1;
        root.add(t1);
        storage.commit();

        // Add new record and rollback
        Record1 t2 = new Record1();
        t2.count = 2;
        root.add(t2);

        storage.rollback();

        // t2 should not be in the set
        assertFalse(root.contains(t2), "t2 should not be in set after rollback");
    }

    @Test
    @DisplayName("Test rollback with index operations")
    void testRollbackIndexOperations() throws Exception {
        Index<Record1> index = storage.createIndex(int.class, true);
        storage.setRoot(index);

        // Add initial records
        for (int i = 0; i < 10; i++) {
            Record1 rec = new Record1();
            rec.count = i;
            index.put(new Key(i), rec);
        }
        storage.commit();

        assertEquals(10, index.size(), "Should have 10 records");

        // Add more records
        for (int i = 10; i < 20; i++) {
            Record1 rec = new Record1();
            rec.count = i;
            index.put(new Key(i), rec);
        }

        // Remove some records
        for (int i = 0; i < 5; i++) {
            index.remove(new Key(i));
        }

        storage.rollback();

        // Should be back to 10 records
        assertEquals(10, index.size(), "Should have 10 records after rollback");

        // Original records should still exist
        for (int i = 0; i < 10; i++) {
            Record1 rec = index.get(new Key(i));
            assertNotNull(rec, "Record " + i + " should exist after rollback");
        }
    }

    @Test
    @DisplayName("Test multiple rollback cycles")
    void testMultipleRollbackCycles() throws Exception {
        IPersistentSet root = storage.createSet();
        storage.setRoot(root);

        // Cycle 1
        Record1 t1 = new Record1();
        t1.count = 1;
        root.add(t1);
        storage.commit();

        // Cycle 2 - add and rollback
        Record1 t2 = new Record1();
        t2.count = 2;
        root.add(t2);
        storage.rollback();

        assertEquals(1, root.size(), "Should have 1 record after first rollback");

        // Cycle 3 - add and commit
        Record1 t3 = new Record1();
        t3.count = 3;
        root.add(t3);
        storage.commit();

        assertEquals(2, root.size(), "Should have 2 records after commit");

        // Cycle 4 - modify and rollback
        t1.count = 100;
        t1.modify();
        storage.rollback();

        assertEquals(1, t1.count, "t1 count should be rolled back");
    }

    @Test
    @DisplayName("Test rollback with nested objects")
    void testRollbackNestedObjects() throws Exception {
        IPersistentSet root = storage.createSet();
        storage.setRoot(root);

        // Create nested structure
        Record1 parent = new Record1();
        parent.count = 10;
        root.add(parent);

        Record1 child1 = new Record1();
        child1.count = 1;
        root.add(child1);

        Record1 child2 = new Record1();
        child2.count = 2;
        root.add(child2);

        storage.commit();

        // Modify all
        parent.count = 100;
        parent.modify();
        child1.count = 10;
        child1.modify();
        child2.count = 20;
        child2.modify();

        storage.rollback();

        // All should be rolled back
        assertEquals(10, parent.count, "Parent should be rolled back");
        assertEquals(1, child1.count, "Child1 should be rolled back");
        assertEquals(2, child2.count, "Child2 should be rolled back");
    }

    @Test
    @DisplayName("Test rollback with clear operation")
    void testRollbackClearOperation() throws Exception {
        IPersistentSet root = storage.createSet();
        storage.setRoot(root);

        // Add records
        for (int i = 0; i < 10; i++) {
            Record1 rec = new Record1();
            rec.count = i;
            root.add(rec);
        }
        storage.commit();

        assertEquals(10, root.size(), "Should have 10 records");

        // Clear all
        root.clear();
        storage.rollback();

        // Should be restored
        assertEquals(10, root.size(), "Should have 10 records after rollback");
    }
}
