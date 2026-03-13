package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestLoad.java
 * Tests load/stress testing with caching
 */
class TestLoad {

    static class Record extends Persistent {
        int id;
        byte[] body;
    }

    private Storage storage;
    // Scaled down from 10000 elements
    private static final int nElements = 100;
    private static final int bodySize = 1000;
    private static final String TEST_DB = "testload.dbs";

    @BeforeEach
    void setUp() throws Exception {
        new java.io.File(TEST_DB).delete();
        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.object.cache.init.size", 10);
        storage.open(TEST_DB);

        FieldIndex<Record> index = (FieldIndex<Record>) storage.getRoot();
        if (index == null) {
            index = storage.<Record>createFieldIndex(Record.class, "id", true);
            storage.setRoot(index);
            
            for (int i = 0; i < nElements; i++) {
                Record rec = new Record();
                rec.id = i;
                rec.body = new byte[bodySize];
                index.add(rec);
            }
            storage.commit();
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
    @DisplayName("Test load - iterate through all records")
    void testLoadIteration() {
        FieldIndex<Record> index = (FieldIndex<Record>) storage.getRoot();
        
        int count = 0;
        for (Record rec : index) {
            assertEquals(count, rec.id, "Record ID should match iteration order");
            assertEquals(bodySize, rec.body.length, "Body length should match");
            count++;
        }
        
        assertEquals(nElements, count, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test load - random access by key")
    void testLoadRandomAccess() {
        FieldIndex<Record> index = (FieldIndex<Record>) storage.getRoot();
        
        // Use a deterministic key sequence
        long key = 1999;
        for (int i = 0; i < nElements; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int id = (int) (key % nElements);
            
            Record rec = index.get(id);
            assertNotNull(rec, "Should find record with id " + id);
            assertEquals(id, rec.id, "Record ID should match");
            assertEquals(bodySize, rec.body.length, "Body length should match");
        }
    }

    @Test
    @DisplayName("Test load - cache behavior with repeated iterations")
    void testLoadRepeatedIterations() {
        FieldIndex<Record> index = (FieldIndex<Record>) storage.getRoot();
        
        // Multiple iterations to test cache behavior
        for (int iter = 0; iter < 3; iter++) {
            int count = 0;
            for (Record rec : index) {
                assertEquals(count, rec.id, "Record ID should match in iteration " + iter);
                assertEquals(bodySize, rec.body.length, "Body length should match");
                count++;
            }
            assertEquals(nElements, count, "Should iterate through all records in iteration " + iter);
        }
    }

    @Test
    @DisplayName("Test load - sequential access pattern")
    void testLoadSequentialAccess() {
        FieldIndex<Record> index = (FieldIndex<Record>) storage.getRoot();
        
        // Sequential access
        for (int i = 0; i < nElements; i++) {
            Record rec = index.get(i);
            assertNotNull(rec, "Should find record " + i);
            assertEquals(i, rec.id, "Record ID should match");
        }
    }
}
