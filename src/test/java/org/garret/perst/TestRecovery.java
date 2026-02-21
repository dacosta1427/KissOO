package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRecovery.java
 * Tests database recovery concepts - set and index consistency
 */
class TestRecovery {

    static class Indices extends Persistent {
        IPersistentSet set;
        Index<Record> index;
    }

    static class Record extends Persistent {
        int id;

        Record() {}

        Record(int id) {
            this.id = id;
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testrecovery.dbs";

    @BeforeEach
    void setUp() throws Exception {
        new java.io.File(TEST_DB).delete();
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);

        Indices root = (Indices) storage.getRoot();
        if (root == null) {
            root = new Indices();
            root.set = storage.createSet();
            root.index = storage.createIndex(int.class, true);
            storage.setRoot(root);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test recovery - set and index consistency")
    void testSetIndexConsistency() {
        Indices root = (Indices) storage.getRoot();
        
        int n = root.set.size();
        assertEquals(n, root.index.size(), "Set and index should have same size");
        assertEquals(0, n % 10, "Size should be divisible by crashPeriod (10)");
    }

    @Test
    @DisplayName("Test recovery - verify records in index")
    void testIndexRecords() {
        Indices root = (Indices) storage.getRoot();
        
        int n = root.set.size();
        for (int i = 0; i < n; i++) {
            Record rec = root.index.get(new Key(i));
            assertNotNull(rec, "Should find record " + i + " in index");
            assertEquals(i, rec.id, "Record ID should match");
        }
    }

    @Test
    @DisplayName("Test recovery - verify records via set iterator")
    @SuppressWarnings("unchecked")
    void testSetIterator() {
        Indices root = (Indices) storage.getRoot();
        
        int n = root.set.size();
        
        // Calculate expected sum
        long expectedSum = (long) n * (n - 1) / 2;
        
        // Iterate through set and calculate sum
        Iterator<Record> iterator = root.set.iterator();
        int count = 0;
        long sum = 0;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            sum += rec.id;
            count++;
        }
        
        assertEquals(n, count, "Iterator should traverse all records");
        assertEquals(expectedSum, sum, "Sum of IDs should match");
    }

    @Test
    @DisplayName("Test recovery - add new records")
    void testAddRecords() {
        Indices root = (Indices) storage.getRoot();
        
        int startId = root.set.size();
        int recordsToAdd = 5;
        
        // Add new records
        for (int i = startId; i < startId + recordsToAdd; i++) {
            Record rec = new Record(i);
            root.set.add(rec);
            root.index.put(new Key(i), rec);
        }
        
        storage.commit();
        
        // Verify
        assertEquals(startId + recordsToAdd, root.set.size(), "Should have added all records");
        assertEquals(startId + recordsToAdd, root.index.size(), "Index should have same size");
        
        // Verify each record
        for (int i = startId; i < startId + recordsToAdd; i++) {
            Record rec = root.index.get(new Key(i));
            assertNotNull(rec, "Should find record " + i);
            assertEquals(i, rec.id, "Record ID should match");
        }
    }

    @Test
    @DisplayName("Test recovery - remove records")
    void testRemoveRecords() {
        Indices root = (Indices) storage.getRoot();
        
        // First add some records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record(i);
            root.set.add(rec);
            root.index.put(new Key(i), rec);
        }
        storage.commit();
        
        assertEquals(10, root.set.size(), "Should have 10 records");
        
        // Remove some records
        for (int i = 0; i < 5; i++) {
            Record rec = root.index.get(new Key(i));
            root.set.remove(rec);
            root.index.remove(new Key(i));
        }
        storage.commit();
        
        assertEquals(5, root.set.size(), "Should have 5 records after removal");
        assertEquals(5, root.index.size(), "Index should have 5 records");
    }

    @Test
    @DisplayName("Test recovery - persistence across sessions")
    void testPersistenceAcrossSessions() throws Exception {
        Indices root = (Indices) storage.getRoot();
        
        // Add records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record(i);
            root.set.add(rec);
            root.index.put(new Key(i), rec);
        }
        storage.commit();
        storage.close();
        
        // Reopen
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);
        
        root = (Indices) storage.getRoot();
        
        assertEquals(10, root.set.size(), "Set should persist 10 records");
        assertEquals(10, root.index.size(), "Index should persist 10 records");
        
        // Verify all records
        for (int i = 0; i < 10; i++) {
            Record rec = root.index.get(new Key(i));
            assertNotNull(rec, "Should find record " + i);
            assertEquals(i, rec.id, "Record ID should match");
        }
    }

    @Test
    @DisplayName("Test recovery - clear all records")
    void testClearAllRecords() {
        Indices root = (Indices) storage.getRoot();
        
        // Add records
        for (int i = 0; i < 20; i++) {
            Record rec = new Record(i);
            root.set.add(rec);
            root.index.put(new Key(i), rec);
        }
        storage.commit();
        
        assertEquals(20, root.set.size(), "Should have 20 records");
        
        // Clear all
        root.set.clear();
        root.index.clear();
        storage.commit();
        
        assertEquals(0, root.set.size(), "Set should be empty");
        assertEquals(0, root.index.size(), "Index should be empty");
    }

    @Test
    @DisplayName("Test recovery - large number of records")
    void testLargeNumberOfRecords() {
        Indices root = (Indices) storage.getRoot();
        
        int numRecords = 1000;
        
        // Add many records
        for (int i = 0; i < numRecords; i++) {
            Record rec = new Record(i);
            root.set.add(rec);
            root.index.put(new Key(i), rec);
        }
        storage.commit();
        
        assertEquals(numRecords, root.set.size(), "Should have all records");
        assertEquals(numRecords, root.index.size(), "Index should have all records");
        
        // Verify random access
        for (int i = 0; i < numRecords; i += 100) {
            Record rec = root.index.get(new Key(i));
            assertNotNull(rec, "Should find record " + i);
            assertEquals(i, rec.id, "Record ID should match");
        }
    }
}
