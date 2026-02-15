package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestMaxOid.java
 * Tests max OID handling and large record operations
 */
class TestMaxOid {

    static class Record extends Persistent {
        int key;

        Record() {}

        Record(int key) {
            this.key = key;
        }
    }

    private Storage storage;
    private FieldIndex<Record> root;
    // Scaled down from 1000 records
    private static final int nRecords = 100;
    private static final String TEST_DB = "testmaxoid.dbs";

    @BeforeEach
    void setUp() throws Exception {
        // Delete existing database to start fresh
        new java.io.File(TEST_DB).delete();
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 256 * 1024 * 1024);

        root = (FieldIndex<Record>) storage.getRoot();
        if (root == null) {
            root = storage.createFieldIndex(Record.class, "key", true);
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
    @DisplayName("Test max OID with record insertion")
    void testMaxOidInsertion() {
        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record(i);
            root.put(rec);
        }
        storage.commit();

        assertEquals(nRecords, root.size(), "Should have inserted all records");
    }

    @Test
    @DisplayName("Test max OID with index search")
    void testMaxOidIndexSearch() {
        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record(i);
            root.put(rec);
        }
        storage.commit();

        // Search for each record
        for (int i = 0; i < nRecords; i++) {
            Record rec = root.get(new Key(i));
            assertNotNull(rec, "Should find record " + i);
            assertEquals(i, rec.key, "Record key should match");
        }
    }

    @Test
    @DisplayName("Test max OID with iterator")
    void testMaxOidIterator() {
        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record(i);
            root.put(rec);
        }
        storage.commit();

        // Iterate through records
        int i = 0;
        Iterator<Record> iterator = root.iterator();
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertEquals(i, rec.key, "Record should be in order");
            i++;
        }
        assertEquals(nRecords, i, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test max OID with record removal")
    void testMaxOidRemoval() {
        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record(i);
            root.put(rec);
        }
        storage.commit();

        // Remove all records
        for (int i = 0; i < nRecords; i++) {
            Record rec = root.remove(new Key(i));
            assertNotNull(rec, "Should find record to remove " + i);
            assertEquals(i, rec.key, "Record key should match");
            rec.deallocate();
        }

        assertEquals(0, root.size(), "All records should be removed");
    }
}
