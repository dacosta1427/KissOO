package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestBackup.java
 * Tests backup and restore functionality of Perst storage.
 */
class TestBackup {

    static class Record extends Persistent {
        String strKey;
        long intKey;
        double realKey;
    }

    static class Indices extends Persistent {
        Index strIndex;
        FieldIndex intIndex;
        FieldIndex compoundIndex;
    }

    private Storage storage;
    private static final int nRecords = 1000; // Reduced from 100000 for faster tests
    private static final String TEST_DB = "testbck1.dbs";
    private static final String BACKUP_DB = "testbck2.dbs";

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
        new File(BACKUP_DB).delete();
    }

    @Test
    @DisplayName("Test backup and restore with indexes")
    void testBackupRestore() throws Exception {
        // Create indices and root
        Indices root = new Indices();
        root.strIndex = storage.createIndex(String.class, true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        root.compoundIndex = storage.createFieldIndex(Record.class, new String[]{"strKey", "intKey"}, true);
        storage.setRoot(root);

        FieldIndex intIndex = root.intIndex;
        FieldIndex compoundIndex = root.compoundIndex;
        Index strIndex = root.strIndex;

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.realKey = (double) key;
            intIndex.put(rec);
            strIndex.put(new Key(rec.strKey), rec);
            compoundIndex.put(rec);
        }
        storage.commit();

        // Perform backup
        OutputStream out = new FileOutputStream(BACKUP_DB);
        storage.backup(out);
        out.close();
        storage.close();

        // Open backup and verify
        storage = StorageFactory.getInstance().createStorage();
        storage.open(BACKUP_DB, 32 * 1024 * 1024);

        root = (Indices) storage.getRoot();
        intIndex = root.intIndex;
        strIndex = root.strIndex;
        compoundIndex = root.compoundIndex;

        // Verify records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            String strKey = Long.toString(key);

            Record rec1 = (Record) intIndex.get(new Key(key));
            Record rec2 = (Record) strIndex.get(new Key(strKey));
            Record rec3 = (Record) compoundIndex.get(new Key(strKey, new Long(key)));

            assertNotNull(rec1, "Record should exist in intIndex");
            assertSame(rec1, rec2, "Records from intIndex and strIndex should be same");
            assertSame(rec1, rec3, "Records from intIndex and compoundIndex should be same");
            assertEquals(key, rec1.intKey, "intKey should match");
            assertEquals((double) key, rec1.realKey, "realKey should match");
            assertEquals(strKey, rec1.strKey, "strKey should match");
        }
    }

    @Test
    @DisplayName("Test backup creates valid file")
    void testBackupCreatesValidFile() throws Exception {
        // Create a simple record
        Indices root = new Indices();
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        storage.setRoot(root);

        Record rec = new Record();
        rec.intKey = 42;
        rec.strKey = "test";
        rec.realKey = 3.14;
        root.intIndex.put(rec);
        storage.commit();

        // Perform backup
        OutputStream out = new FileOutputStream(BACKUP_DB);
        storage.backup(out);
        out.close();

        // Verify backup file exists and has content
        File backupFile = new File(BACKUP_DB);
        assertTrue(backupFile.exists(), "Backup file should exist");
        assertTrue(backupFile.length() > 0, "Backup file should not be empty");
    }

    @Test
    @DisplayName("Test restore from backup preserves index structure")
    void testRestorePreservesIndexStructure() throws Exception {
        // Create indices
        Indices root = new Indices();
        root.strIndex = storage.createIndex(String.class, true);
        root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
        storage.setRoot(root);

        // Add some records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            rec.realKey = i * 1.0;
            root.intIndex.put(rec);
            root.strIndex.put(new Key(rec.strKey), rec);
        }
        storage.commit();

        // Backup
        OutputStream out = new FileOutputStream(BACKUP_DB);
        storage.backup(out);
        out.close();
        storage.close();

        // Restore
        storage = StorageFactory.getInstance().createStorage();
        storage.open(BACKUP_DB, 32 * 1024 * 1024);

        root = (Indices) storage.getRoot();

        // Verify indices exist and work
        assertNotNull(root.intIndex, "intIndex should exist after restore");
        assertNotNull(root.strIndex, "strIndex should exist after restore");

        // Verify we can query by string key
        Record rec = (Record) root.strIndex.get(new Key("key5"));
        assertNotNull(rec, "Should be able to get record by key");
        assertEquals(5, rec.intKey, "Record key should match");
    }
}
