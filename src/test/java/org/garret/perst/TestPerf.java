package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestPerf.java
 * Tests performance of B-Tree and Hash index operations
 */
class TestPerf {

    static class Record extends Persistent {
        int intKey;
    }

    static class Root extends Persistent {
        FieldIndex<Record> tree;
        IPersistentHash<Integer, Record> hash;
    }

    // Scaled down from 100000 records
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testperf.dbs";

    private Storage storage;

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
    @DisplayName("Test B-Tree and Hash insert performance")
    void testInsertPerformance() {
        Root root = new Root();
        root.tree = storage.<Record>createFieldIndex(Record.class, "intKey", true);
        root.hash = storage.<Integer, Record>createHash(101, 1);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i * 2;
            root.tree.put(rec);
            root.hash.put(rec.intKey, rec);
        }
        storage.commit();

        assertEquals(nRecords, root.tree.size(), "Tree should have all records");
        assertEquals(nRecords, root.hash.size(), "Hash should have all records");
    }

    @Test
    @DisplayName("Test B-Tree search performance")
    void testBtreeSearchPerformance() {
        Root root = new Root();
        root.tree = storage.<Record>createFieldIndex(Record.class, "intKey", true);
        root.hash = storage.<Integer, Record>createHash(101, 1);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i * 2;
            root.tree.put(rec);
            root.hash.put(rec.intKey, rec);
        }
        storage.commit();

        // Search for all keys including non-existing ones
        int searchCount = 0;
        for (int i = 0; i < nRecords * 2; i++) {
            Record rec = root.tree.get(i);
            if ((i & 1) != 0) {
                assertNull(rec, "Odd keys should not exist");
            } else {
                assertNotNull(rec, "Even keys should exist");
                assertEquals(i, rec.intKey, "Key should match");
            }
            searchCount++;
        }
        assertEquals(nRecords * 2, searchCount, "Should perform all searches");
    }

    @Test
    @DisplayName("Test Hash search performance")
    void testHashSearchPerformance() {
        Root root = new Root();
        root.tree = storage.<Record>createFieldIndex(Record.class, "intKey", true);
        root.hash = storage.<Integer, Record>createHash(101, 1);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i * 2;
            root.tree.put(rec);
            root.hash.put(rec.intKey, rec);
        }
        storage.commit();

        // Search for all keys including non-existing ones
        int searchCount = 0;
        for (int i = 0; i < nRecords * 2; i++) {
            Record rec = root.hash.get(i);
            if ((i & 1) != 0) {
                assertNull(rec, "Odd keys should not exist");
            } else {
                assertNotNull(rec, "Even keys should exist");
                assertEquals(i, rec.intKey, "Key should match");
            }
            searchCount++;
        }
        assertEquals(nRecords * 2, searchCount, "Should perform all searches");
    }

    @Test
    @DisplayName("Test B-Tree iteration performance")
    void testIterationPerformance() {
        Root root = new Root();
        root.tree = storage.<Record>createFieldIndex(Record.class, "intKey", true);
        root.hash = storage.<Integer, Record>createHash(101, 1);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i * 2;
            root.tree.put(rec);
            root.hash.put(rec.intKey, rec);
        }
        storage.commit();

        // Iterate through all records
        int count = 0;
        int expectedKey = 0;
        for (Record rec : root.tree) {
            assertEquals(expectedKey, rec.intKey, "Keys should be in order");
            expectedKey += 2;
            count++;
        }
        assertEquals(nRecords, count, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test deletion performance")
    void testDeletionPerformance() {
        Root root = new Root();
        root.tree = storage.<Record>createFieldIndex(Record.class, "intKey", true);
        root.hash = storage.<Integer, Record>createHash(101, 1);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i * 2;
            root.tree.put(rec);
            root.hash.put(rec.intKey, rec);
        }
        storage.commit();

        assertEquals(nRecords, root.tree.size(), "Tree should have all records before deletion");
        assertEquals(nRecords, root.hash.size(), "Hash should have all records before deletion");

        // Delete records with even keys (0, 2, 4, ...)
        // Original code deletes nRecords/2 records (every even index from 0 to nRecords*2)
        int deleteCount = 0;
        for (int i = 0; i < nRecords * 2; i++) {
            Record rec = root.hash.get(i);
            if ((i & 1) != 0) {
                // Odd indices don't exist in our data
                assertNull(rec, "Odd indices should not exist");
            } else {
                // Even indices should exist - delete them
                assertNotNull(rec, "Even indices should exist");
                root.tree.remove(rec);
                root.hash.remove(rec.intKey);
                rec.deallocate();
                deleteCount++;
            }
        }

        assertEquals(nRecords, deleteCount, "Should delete nRecords even-indexed records");
        assertEquals(0, root.tree.size(), "Tree should be empty");
        assertEquals(0, root.hash.size(), "Hash should be empty");
    }
}
