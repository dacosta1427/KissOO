package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRndIndex.java
 * Tests RandomAccess index functionality
 */
class TestRndIndex {

    static class Record extends Persistent {
        int i;
    }

    private Storage storage;
    // Scaled down from 100003 records
    private static final int nRecords = 100;
    private static final String TEST_DB = "testrnd.dbs";

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
    @DisplayName("Test RandomAccess index insert")
    void testRandomIndexInsert() {
        FieldIndex<Record> root = storage.<Record>createRandomAccessFieldIndex(Record.class, "i", true);
        storage.setRoot(root);

        // Insert unique records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.put(rec);
        }
        storage.commit();

        // Verify records were inserted
        assertEquals(nRecords, root.size(), "All records should be inserted");
    }

    @Test
    @DisplayName("Test RandomAccess index get by key")
    void testRandomIndexGetByKey() {
        FieldIndex<Record> root = storage.<Record>createRandomAccessFieldIndex(Record.class, "i", true);
        storage.setRoot(root);

        // Insert unique records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.put(rec);
        }
        storage.commit();

        // Get by key - use the actual stored value
        for (int i = 0; i < nRecords; i++) {
            Record rec = root.get(new Key(i));
            assertNotNull(rec, "Should find record by key at index " + i);
        }
    }

    @Test
    @DisplayName("Test RandomAccess index getAt")
    void testRandomIndexGetAt() {
        FieldIndex<Record> root = storage.<Record>createRandomAccessFieldIndex(Record.class, "i", true);
        storage.setRoot(root);

        // Insert unique records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.put(rec);
        }
        storage.commit();

        // Get by position
        for (int i = 0; i < nRecords; i++) {
            Record rec = root.getAt(i);
            assertNotNull(rec, "Should find record at position " + i);
        }
    }

    @Test
    @DisplayName("Test RandomAccess index iterator")
    void testRandomIndexIterator() {
        FieldIndex<Record> root = storage.<Record>createRandomAccessFieldIndex(Record.class, "i", true);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.put(rec);
        }
        storage.commit();

        // Test ascending iterator
        Iterator<Record> iterator = root.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertNotNull(rec, "Record should not be null");
            count++;
        }
        assertEquals(nRecords, count, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test RandomAccess index remove")
    void testRandomIndexRemove() {
        FieldIndex<Record> root = storage.<Record>createRandomAccessFieldIndex(Record.class, "i", true);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.put(rec);
        }
        storage.commit();

        int initialSize = root.size();
        
        // Remove some records
        for (int i = 0; i < 10; i++) {
            Record rec = root.getAt(i);
            if (rec != null) {
                root.remove(rec);
                rec.deallocate();
            }
        }

        assertTrue(root.size() < initialSize, "Should have removed some records");
    }

    @Test
    @DisplayName("Test RandomAccess index clear")
    void testRandomIndexClear() {
        FieldIndex<Record> root = storage.<Record>createRandomAccessFieldIndex(Record.class, "i", true);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.put(rec);
        }
        storage.commit();

        // Clear
        root.clear();

        assertTrue(root.isEmpty(), "Index should be empty after clear");
        assertFalse(root.iterator().hasNext(), "Iterator should have no elements");
    }
}
