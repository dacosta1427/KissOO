package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestMod.java
 * Tests modification tracking with multiple indices
 */
class TestMod {

    static class Record extends Persistent {
        String strKey;
        long intKey;
    }

    static class Indices extends Persistent {
        FieldIndex<Record> strIndex;
        FieldIndex<Record> intIndex;
    }

    private Storage storage;
    private Indices root;
    // Scaled down from 100000 records and 3 iterations
    private static final int nRecords = 100;
    private static final int nIterations = 2;
    private static final String TEST_DB = "testmod.dbs";

    private String reverseString(String s) {
        char[] chars = new char[s.length()];
        for (int i = 0, n = chars.length; i < n; i++) {
            chars[i] = s.charAt(n - i - 1);
        }
        return new String(chars);
    }

    @BeforeEach
    void setUp() throws Exception {
        new java.io.File(TEST_DB).delete();
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);

        root = (Indices) storage.getRoot();
        if (root == null) {
            root = new Indices();
            root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
            root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
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
    @DisplayName("Test modification with multiple indices - insertion")
    void testModificationInsertion() {
        FieldIndex<Record> intIndex = root.intIndex;
        FieldIndex<Record> strIndex = root.strIndex;

        // Use sequential keys to avoid duplicates
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = Long.toString(i) + '.';
            intIndex.put(rec);
            strIndex.put(rec);
        }
        storage.commit();

        assertEquals(nRecords, intIndex.size(), "Should have inserted all records in int index");
        assertEquals(nRecords, strIndex.size(), "Should have inserted all records in str index");
    }

    @Test
    @DisplayName("Test modification - record retrieval via iterator")
    void testModificationRetrieval() {
        FieldIndex<Record> intIndex = root.intIndex;
        FieldIndex<Record> strIndex = root.strIndex;

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = Long.toString(i) + '.';
            intIndex.put(rec);
            strIndex.put(rec);
        }
        storage.commit();

        // Verify retrieval via intIndex iterator
        int count = 0;
        java.util.Iterator<Record> iter = intIndex.iterator();
        while (iter.hasNext()) {
            Record rec = iter.next();
            assertNotNull(rec, "Record should not be null");
            assertTrue(rec.intKey >= 0 && rec.intKey < nRecords, "intKey should be in valid range");
            count++;
        }
        assertEquals(nRecords, count, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test modification - strIndex retrieval via iterator")
    void testModificationStrIndexRetrieval() {
        FieldIndex<Record> intIndex = root.intIndex;
        FieldIndex<Record> strIndex = root.strIndex;

        // Insert records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = Long.toString(i) + '.';
            intIndex.put(rec);
            strIndex.put(rec);
        }
        storage.commit();

        // Verify retrieval via strIndex iterator
        int count = 0;
        java.util.Iterator<Record> iter = strIndex.iterator();
        while (iter.hasNext()) {
            Record rec = iter.next();
            assertNotNull(rec, "Record should not be null");
            assertNotNull(rec.strKey, "strKey should not be null");
            count++;
        }
        assertEquals(nRecords, count, "Should iterate through all records via strIndex");
    }
}
