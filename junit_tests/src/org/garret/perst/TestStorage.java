package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Storage implementation details
 */
class TestStorage {

    static class TestRecord extends Persistent {
        String name;
        int value;
        Date timestamp;
        
        TestRecord() {}
        
        TestRecord(String name, int value) {
            this.name = name;
            this.value = value;
            this.timestamp = new Date();
        }
    }

    private Storage storage;
    private static final String TEST_DB = "teststorage.dbs";

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
    @DisplayName("Test storage root handling")
    void testRootHandling() {
        TestRecord rec = new TestRecord("test", 1);
        storage.setRoot(rec);
        
        storage.commit();
        
        Object root = storage.getRoot();
        assertNotNull(root, "Root should not be null");
        assertTrue(root instanceof TestRecord, "Root should be TestRecord");
    }

    @Test
    @DisplayName("Test storage gc")
    void testGarbageCollection() {
        // Create some objects
        for (int i = 0; i < 10; i++) {
            TestRecord rec = new TestRecord("test" + i, i);
            rec.store();
        }
        
        storage.commit();
        
        // Force GC
        storage.gc();
        
        // Storage should still be operational
        assertTrue(storage.isOpened(), "Storage should still be opened");
    }

    @Test
    @DisplayName("Test storage get page pool")
    void testPagePool() {
        // Just access the page pool through any operation
        TestRecord rec = new TestRecord("test", 1);
        rec.store();
        
        storage.commit();
        
        // Force some page pool activity
        for (int i = 0; i < 100; i++) {
            TestRecord r = new TestRecord("test" + i, i);
            r.store();
        }
        
        storage.commit();
        
        assertTrue(storage.isOpened(), "Storage should be opened");
    }

    @Test
    @DisplayName("Test storage create list")
    void testCreateList() {
        IPersistentList list = storage.createList();
        storage.setRoot(list);
        
        list.add(new TestRecord("test", 1));
        
        assertEquals(1, list.size(), "List should have 1 element");
    }

    @Test
    @DisplayName("Test storage create set")
    void testCreateSet() {
        IPersistentSet set = storage.createSet();
        storage.setRoot(set);
        
        set.add(new TestRecord("test", 1));
        
        assertEquals(1, set.size(), "Set should have 1 element");
    }


    @Test
    @DisplayName("Test storage multiple commits")
    void testMultipleCommits() {
        for (int round = 0; round < 3; round++) {
            TestRecord rec = new TestRecord("test" + round, round);
            storage.setRoot(rec);
            storage.commit();
        }
        
        assertTrue(storage.isOpened(), "Storage should be opened");
    }

    @Test
    @DisplayName("Test storage transaction rollback")
    void testTransactionRollback() {
        TestRecord rec = new TestRecord("before", 1);
        storage.setRoot(rec);
        storage.commit();
        
        // Modify
        rec.name = "after";
        rec.store();
        
        storage.rollback();
        
        // Verify storage is still operational
        assertTrue(storage.isOpened(), "Storage should still be opened after rollback");
    }
}
