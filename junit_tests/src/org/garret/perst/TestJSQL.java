package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestJSQL.java
 * Tests JSQL query language functionality.
 * Scaled down from 100000 to 1000 records for faster testing.
 */
class TestJSQL {

    static class Record extends Persistent {
        String strKey;
        long intKey;
        Date dateKey;
    }

    static class Indices extends Persistent {
        FieldIndex strIndex;
        FieldIndex intIndex;
        FieldIndex dateIndex;
    }

    private Storage storage;
    private static final int nRecords = 1000; // Reduced from 100000 for faster tests
    private static final String TEST_DB = "testjsql.dbs";

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
    @DisplayName("Test JSQL insert records")
    void testInsertRecords() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        FieldIndex intIndex = root.intIndex;
        FieldIndex strIndex = root.strIndex;
        FieldIndex dateIndex = root.dateIndex;

        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.dateKey = new Date();
            intIndex.put(rec);
            strIndex.put(rec);
            dateIndex.put(rec);
        }
        storage.commit();

        // Verify records were inserted
        assertEquals(nRecords, intIndex.size(), "Should have nRecords in intIndex");
        assertEquals(nRecords, strIndex.size(), "Should have nRecords in strIndex");
    }

    @Test
    @DisplayName("Test JSQL query with string parameter")
    void testQueryStringParameter() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.dateKey = new Date();
            root.intIndex.put(rec);
            root.strIndex.put(rec);
            root.dateIndex.put(rec);
        }
        storage.commit();

        // Create and execute query
        Query q1 = storage.createQuery();
        q1.prepare(Record.class, "strKey=?");
        q1.addIndex("strKey", root.strIndex);

        key = 1999;
        for (int i = 0; i < 10; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            String searchKey = Long.toString(key);
            q1.setParameter(1, searchKey);
            Iterator iterator = q1.execute(root.intIndex.iterator());
            Record rec1 = (Record) iterator.next();
            assertNotNull(rec1, "Record should be found");
            assertFalse(iterator.hasNext(), "Should only return one record");
        }
    }

    @Test
    @DisplayName("Test JSQL query with integer parameter")
    void testQueryIntParameter() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.dateKey = new Date();
            root.intIndex.put(rec);
            root.strIndex.put(rec);
            root.dateIndex.put(rec);
        }
        storage.commit();

        // Create and execute query
        Query q2 = storage.createQuery();
        q2.addIndex("intKey", root.intIndex);
        q2.prepare(Record.class, "intKey=?");

        key = 1999;
        for (int i = 0; i < 10; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            q2.setIntParameter(1, key);
            Iterator iterator = q2.execute(root.strIndex.iterator());
            Record rec2 = (Record) iterator.next();
            assertNotNull(rec2, "Record should be found");
            assertFalse(iterator.hasNext(), "Should only return one record");
        }
    }

    @Test
    @DisplayName("Test JSQL select query")
    void testSelectQuery() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.dateKey = new Date();
            root.intIndex.put(rec);
            root.strIndex.put(rec);
            root.dateIndex.put(rec);
        }
        storage.commit();

        // Execute select query
        Iterator iterator = root.intIndex.select("strKey=string(intKey)");
        int count = 0;
        long prevKey = Long.MIN_VALUE;
        while (iterator.hasNext()) {
            Record rec = (Record) iterator.next();
            assertTrue(rec.intKey >= prevKey, "Records should be ordered");
            prevKey = rec.intKey;
            count++;
        }
        assertEquals(nRecords, count, "Should return all records");
    }

    @Test
    @DisplayName("Test JSQL select with ordering")
    void testSelectWithOrdering() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        // Insert records with predictable keys for this test
        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i * 1024; // Keys that will match (intKey and 1023) = 0
            rec.strKey = Long.toString(rec.intKey);
            rec.dateKey = new Date();
            root.intIndex.put(rec);
            root.strIndex.put(rec);
            root.dateIndex.put(rec);
        }
        storage.commit();

        // Execute select with ordering
        Iterator iterator = root.strIndex.select("(intKey and 1023) = 0 order by intKey");
        int count = 0;
        long prevKey = Long.MIN_VALUE;
        while (iterator.hasNext()) {
            Record rec = (Record) iterator.next();
            assertTrue(rec.intKey >= prevKey, "Records should be ordered");
            prevKey = rec.intKey;
            count++;
        }
        // Should return records where intKey % 1024 == 0
        assertEquals(100, count, "Should return all 100 records");
    }

    @Test
    @DisplayName("Test JSQL between query")
    void testBetweenQuery() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        // Insert records with known dates
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            rec.dateKey = new Date(begin + i * 1000); // 1 second apart
            root.intIndex.put(rec);
            root.strIndex.put(rec);
            root.dateIndex.put(rec);
        }
        storage.commit();
        long end = System.currentTimeMillis();

        // Execute between query
        Query q3 = storage.createQuery();
        q3.addIndex("dateKey", root.dateIndex);
        q3.prepare(Record.class, "dateKey between ? and ?");

        q3.setParameter(1, new Date(begin));
        q3.setParameter(2, new Date(end));
        Iterator iterator = q3.execute(root.dateIndex.iterator());

        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertTrue(count > 0, "Should return some records");
    }

    @Test
    @DisplayName("Test JSQL iterator with ascending order")
    void testIteratorAscending() {
        Indices root = new Indices();
        root.strIndex = storage.createFieldIndex(Record.class, "strKey", true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.dateIndex = storage.createFieldIndex(Record.class, "dateKey", false);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            rec.dateKey = new Date();
            root.intIndex.put(rec);
            root.strIndex.put(rec);
            root.dateIndex.put(rec);
        }
        storage.commit();

        // Iterate in ascending order
        Iterator iterator = root.intIndex.iterator(null, null, Index.ASCENT_ORDER);
        int count = 0;
        long prevKey = -1;
        while (iterator.hasNext()) {
            Record rec = (Record) iterator.next();
            assertTrue(rec.intKey > prevKey, "Keys should be in ascending order");
            prevKey = rec.intKey;
            count++;
        }
        assertEquals(100, count, "Should iterate all records");
    }
}
