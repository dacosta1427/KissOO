package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestDerivedIndex.java
 * Tests derived index functionality with inheritance
 */
class TestDerivedIndex {

    static class BaseRecord extends Persistent {
        int key;
    }

    static class DerivedRecord extends BaseRecord {
        int value;
    }

    private Storage storage;
    private Database db;
    private static final String TEST_DB = "testdbi.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB);
        db = new Database(storage);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test derived index with inheritance")
    void testDerivedIndexWithInheritance() {
        // Add a derived record
        DerivedRecord dr = new DerivedRecord();
        dr.key = 1;
        dr.value = 2;
        db.addRecord(dr);

        // Add a base record
        BaseRecord br = new BaseRecord();
        br.key = 1;
        db.addRecord(br);

        // Query derived records with both key and value
        Iterator<DerivedRecord> di = db.select(DerivedRecord.class, "key=1 and value=2");
        int derivedCount = 0;
        while (di.hasNext()) {
            DerivedRecord rec = di.next();
            assertEquals(1, rec.key, "Key should be 1");
            assertEquals(2, rec.value, "Value should be 2");
            derivedCount += 1;
        }
        assertEquals(1, derivedCount, "Should find 1 derived record");

        // Query base records with key
        Iterator<BaseRecord> bi = db.select(BaseRecord.class, "key=1");
        int baseCount = 0;
        while (bi.hasNext()) {
            BaseRecord rec = bi.next();
            assertEquals(1, rec.key, "Key should be 1");
            baseCount += 1;
        }
        assertEquals(2, baseCount, "Should find 2 base records (1 base + 1 derived)");
    }

    @Test
    @DisplayName("Test derived index with no matches")
    void testDerivedIndexNoMatches() {
        // Add a derived record
        DerivedRecord dr = new DerivedRecord();
        dr.key = 1;
        dr.value = 2;
        db.addRecord(dr);

        // Query with non-matching value
        Iterator<DerivedRecord> di = db.select(DerivedRecord.class, "key=1 and value=999");
        assertFalse(di.hasNext(), "Should find no records with value=999");
    }

    @Test
    @DisplayName("Test derived index multiple records")
    void testDerivedIndexMultipleRecords() {
        // Add multiple records
        for (int i = 0; i < 5; i++) {
            DerivedRecord dr = new DerivedRecord();
            dr.key = i;
            dr.value = i * 10;
            db.addRecord(dr);
        }

        // Query all derived records
        Iterator<DerivedRecord> di = db.select(DerivedRecord.class, "key >= 0");
        int count = 0;
        while (di.hasNext()) {
            DerivedRecord rec = di.next();
            assertEquals(rec.value, rec.key * 10, "Value should equal key * 10");
            count += 1;
        }
        assertEquals(5, count, "Should find 5 records");
    }
}
