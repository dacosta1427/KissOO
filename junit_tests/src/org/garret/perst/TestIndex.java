package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class TestIndex {

    static class Record extends Persistent {
        String strKey;
        long intKey;
    }

    static class Indices extends Persistent {
        Index<Record> strIndex;
        Index<Record> intIndex;
    }

    private Storage storage;
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testindex.dbs";

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
    @DisplayName("Test index insert and get by long key")
    void testIndexInsertAndGetLongKey() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            intIndex.put(rec.intKey, rec);
        }

        storage.commit();

        for (int i = 0; i < nRecords; i++) {
            Record rec = intIndex.get((long) i);
            assertNotNull(rec, "Record should be found for key " + i);
            assertEquals(i, rec.intKey, "Record intKey should match");
        }
    }

    @Test
    @DisplayName("Test index insert and get by String key")
    void testIndexInsertAndGetStringKey() {
        Indices root = new Indices();
        root.strIndex = storage.createIndex(String.class, true);
        storage.setRoot(root);

        Index<Record> strIndex = root.strIndex;

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            strIndex.put(rec.strKey, rec);
        }

        storage.commit();

        for (int i = 0; i < nRecords; i++) {
            String key = "key" + i;
            Record rec = strIndex.get(key);
            assertNotNull(rec, "Record should be found for key " + key);
            assertEquals("key" + i, rec.strKey, "Record strKey should match");
        }
    }

    @Test
    @DisplayName("Test index contains")
    void testIndexContains() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        Record rec = new Record();
        rec.intKey = 42;
        intIndex.put(rec.intKey, rec);
        storage.commit();

        boolean found = false;
        for (Record r : intIndex) {
            if (r.intKey == 42) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Index should contain key 42 via iteration");
    }

    @Test
    @DisplayName("Test index iteration")
    void testIndexIteration() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }

        storage.commit();

        int count = 0;
        long sum = 0;
        for (Record r : intIndex) {
            count++;
            sum += r.intKey;
        }
        assertEquals(100, count, "Should have 100 records");
        assertEquals(4950, sum, "Sum of keys should be 0+1+...+99 = 4950");
    }

    @Test
    @DisplayName("Test index iterator")
    void testIndexIterator() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }

        storage.commit();

        Iterator<Record> iterator = intIndex.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertNotNull(rec, "Iterator should return non-null records");
            count++;
        }
        assertEquals(nRecords, count, "Iterator should visit all records");
    }

    @Test
    @DisplayName("Test index size")
    void testIndexSize() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        assertEquals(0, intIndex.size(), "Empty index should have size 0");

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }

        assertEquals(10, intIndex.size(), "Index should have 10 elements");
    }

    @Test
    @DisplayName("Test index put multiple keys")
    void testIndexPutMultipleKeys() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "value" + i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        int count = 0;
        for (Record r : intIndex) {
            count++;
        }
        assertEquals(10, count, "Index should have 10 elements");
    }
}
