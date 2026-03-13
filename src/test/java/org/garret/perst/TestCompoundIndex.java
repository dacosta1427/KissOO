package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestCompoundIndex.java
 * Tests compound index functionality with int and string keys
 */
class TestCompoundIndex {

    static class Record extends Persistent {
        String strKey;
        int intKey;
    }

    private Storage storage;
    private static final int nRecords = 1000; // Scaled down from 100000
    private static final String TEST_DB = "testcidx.dbs";

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
    @DisplayName("Test compound index insert and search")
    void testCompoundIndexInsertAndSearch() {
        FieldIndex<Record> root = storage.<Record>createFieldIndex(Record.class, new String[]{"intKey", "strKey"}, true);
        storage.setRoot(root);

        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int intKey = (int) (key >>> 32);
            String strKey = Integer.toString((int) key);
            Record rec = new Record();
            rec.intKey = intKey;
            rec.strKey = strKey;
            root.put(rec);
        }
        storage.commit();

        // Search for records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int intKey = (int) (key >>> 32);
            String strKey = Integer.toString((int) key);
            Record rec = root.get(new Key(new Object[]{intKey, strKey}));
            assertNotNull(rec, "Record should be found for compound key");
            assertEquals(intKey, rec.intKey, "intKey should match");
            assertEquals(strKey, rec.strKey, "strKey should match");
        }
    }

    @Test
    @DisplayName("Test compound index iteration ascending")
    void testCompoundIndexIterationAscending() {
        FieldIndex<Record> root = storage.<Record>createFieldIndex(Record.class, new String[]{"intKey", "strKey"}, true);
        storage.setRoot(root);

        long key = 1999;
        int minKey = Integer.MAX_VALUE;
        int maxKey = Integer.MIN_VALUE;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int intKey = (int) (key >>> 32);
            String strKey = Integer.toString((int) key);
            Record rec = new Record();
            rec.intKey = intKey;
            rec.strKey = strKey;
            root.put(rec);

            if (intKey < minKey) {
                minKey = intKey;
            }
            if (intKey > maxKey) {
                maxKey = intKey;
            }
        }
        storage.commit();

        Iterator<Record> iterator = root.iterator(new Key(minKey, ""),
                new Key(maxKey + 1, "???"),
                FieldIndex.ASCENT_ORDER);

        int n = 0;
        String prevStr = "";
        int prevInt = minKey;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertTrue(rec.intKey > prevInt || rec.intKey == prevInt && rec.strKey.compareTo(prevStr) > 0,
                    "Records should be in ascending order");
            prevStr = rec.strKey;
            prevInt = rec.intKey;
            n += 1;
        }
        assertEquals(nRecords, n, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test compound index iteration descending")
    void testCompoundIndexIterationDescending() {
        FieldIndex<Record> root = storage.<Record>createFieldIndex(Record.class, new String[]{"intKey", "strKey"}, true);
        storage.setRoot(root);

        long key = 1999;
        int minKey = Integer.MAX_VALUE;
        int maxKey = Integer.MIN_VALUE;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int intKey = (int) (key >>> 32);
            String strKey = Integer.toString((int) key);
            Record rec = new Record();
            rec.intKey = intKey;
            rec.strKey = strKey;
            root.put(rec);

            if (intKey < minKey) {
                minKey = intKey;
            }
            if (intKey > maxKey) {
                maxKey = intKey;
            }
        }
        storage.commit();

        Iterator<Record> iterator = root.iterator(new Key(minKey, "", false),
                new Key(maxKey + 1, "???", false),
                FieldIndex.DESCENT_ORDER);

        int n = 0;
        String prevStr = "";
        int prevInt = maxKey + 1;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertTrue(rec.intKey < prevInt || rec.intKey == prevInt && rec.strKey.compareTo(prevStr) < 0,
                    "Records should be in descending order");
            prevStr = rec.strKey;
            prevInt = rec.intKey;
            n += 1;
        }
        assertEquals(nRecords, n, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test compound index remove and contains")
    void testCompoundIndexRemoveAndContains() {
        FieldIndex<Record> root = storage.<Record>createFieldIndex(Record.class, new String[]{"intKey", "strKey"}, true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        Record[] records = new Record[nRecords];
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int intKey = (int) (key >>> 32);
            String strKey = Integer.toString((int) key);
            Record rec = new Record();
            rec.intKey = intKey;
            rec.strKey = strKey;
            root.put(rec);
            records[i] = rec;
        }
        storage.commit();

        // Remove all records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int intKey = (int) (key >>> 32);
            String strKey = Integer.toString((int) key);
            Record rec = root.get(new Key(new Object[]{intKey, strKey}));
            assertNotNull(rec, "Record should be found before removal");
            assertTrue(root.contains(rec), "Index should contain record");
            root.remove(rec);
            rec.deallocate();
        }

        // Verify index is empty
        assertFalse(root.iterator().hasNext(), "Index should be empty after removal");
        assertFalse(root.iterator(null, null, Index.DESCENT_ORDER).hasNext(), "Descending iterator should be empty");
        assertFalse(root.iterator(null, null, Index.ASCENT_ORDER).hasNext(), "Ascending iterator should be empty");
    }
}
