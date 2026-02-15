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
}
