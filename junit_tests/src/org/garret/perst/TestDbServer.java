package org.garret.perst;

import org.garret.perst.fulltext.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * TestDbServer - Database server with full-text search test
 * Converts from tst/TestDbServer.java
 */
public class TestDbServer { 
    static class Record extends Persistent { 
        @Indexable(unique=true, caseInsensitive=true)
        String key;
        
        @FullTextIndexable
        String value;
    }
    
    // Scaled down for faster testing
    static final int nThreads = 4;
    static final int nRecords = 100;

    static final int fullTextSearchMaxResults = 10;
    static final int fullTextSearchTimeout = 10000; // 10 seconds
    
    static String toStr(int i) { 
        String s = "000000" + i;
        return s.substring(s.length()-6);
    }

    static class ClientThread extends Thread {
        Database db;
        int      id;
        boolean  success = true;
        
        ClientThread(Database db, int id) {
            this.db = db;
            this.id = id;
        }

        public void run() { 
            try {
                int i;
                String tid = "Thread" + id + ":";
                Storage storage = db.getStorage();

                // Insert records
                for (i = 0; i < nRecords; i++) { 
                    db.beginTransaction();
                    Record rec = new Record();
                    rec.key = tid + toStr(i) + ".Id";
                    rec.value = "Thread" + id + " Key" + i;
                    boolean added = db.addRecord(rec);
                    if (!added) {
                        success = false;
                    }
                    db.commitTransaction();
                }

                // Query records with LIKE
                db.beginTransaction();
                i = 0;
                for (Record rec : db.<Record>select(Record.class, "key like '" + tid + "%'")) { 
                    if (!rec.key.equals(tid + toStr(i) + ".Id")) {
                        success = false;
                        break;
                    }
                    i += 1;
                }
                if (i != nRecords) {
                    success = false;
                }

                // Full-text search
                FullTextSearchResult result = db.search("thread" + id, null, fullTextSearchMaxResults, fullTextSearchTimeout);
                if (result.hits.length != 10 || result.estimation != nRecords) {
                    success = false;
                }

                db.commitTransaction();

                // Verify and delete records
                for (i = 0; i < nRecords; i++) { 
                    db.beginTransaction();
                    String key = tid + toStr(i) + ".ID";
                    int n = 0;
                    for (Record rec : db.<Record>select(Record.class, "key = '" + key + "'")) {
                        if (!rec.key.equalsIgnoreCase(key)) {
                            success = false;
                        }
                        n += 1;
                    }
                    if (n != 1) {
                        success = false;
                    }

                    // Full-text search for specific record
                    result = db.search("Thread" + id + " Key" + i, null, fullTextSearchMaxResults, fullTextSearchTimeout);
                    if (result.hits.length != 1 || result.estimation != 1
                        || ((Record)result.hits[0].getDocument()).key.equalsIgnoreCase(key) == false) {
                        success = false;
                    }
                    db.commitTransaction();
                }

                // Delete records
                for (i = 0; i < nRecords; i++) { 
                    db.beginTransaction();
                    String key = tid + toStr(i) + ".id";
                    int n = 0;
                    for (Record rec : db.<Record>select(Record.class, "key = '" + key + "'", true)) {
                        if (!rec.key.equalsIgnoreCase(key)) {
                            success = false;
                        }
                        db.deleteRecord(rec);
                        n += 1;
                        break;
                    }
                    if (n != 1) {
                        success = false;
                    }

                    db.commitTransaction();
                }
            } catch (Exception e) {
                success = false;
            }
        }
    }

    @Test
    public void testDbServerMultiThreaded() throws Exception {    
        Storage storage = StorageFactory.getInstance().createStorage();
        storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        
        try {
            Database db = new Database(storage, true);        

            Thread[] threads = new Thread[nThreads];
            ClientThread[] clientThreads = new ClientThread[nThreads];
            for (int i = 0; i < nThreads; i++) { 
                clientThreads[i] = new ClientThread(db, i);
                threads[i] = clientThreads[i];
                threads[i].start();
            }
            for (int i = 0; i < nThreads; i++) { 
                threads[i].join();
            }
            
            // Verify all threads succeeded
            for (int i = 0; i < nThreads; i++) {
                assertTrue(clientThreads[i].success, "Thread " + i + " failed");
            }
        } finally {
            storage.close();
        }
    }

    @Test
    public void testDbServerBasicOperations() throws Exception {
        Storage storage = StorageFactory.getInstance().createStorage();
        storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        
        try {
            Database db = new Database(storage, true);
            
            // Insert a record
            db.beginTransaction();
            Record rec = new Record();
            rec.key = "testKey";
            rec.value = "test value content";
            assertTrue(db.addRecord(rec), "Should add record");
            db.commitTransaction();
            
            // Query by key
            db.beginTransaction();
            int count = 0;
            for (Record r : db.<Record>select(Record.class, "key = 'testKey'")) {
                assertEquals("testKey", r.key);
                count++;
            }
            assertEquals(1, count, "Should find 1 record");
            db.commitTransaction();
            
            // Full-text search
            db.beginTransaction();
            FullTextSearchResult result = db.search("test value", null, fullTextSearchMaxResults, fullTextSearchTimeout);
            assertTrue(result.hits.length >= 1, "Should find at least 1 hit");
            db.commitTransaction();
            
            // Delete record
            db.beginTransaction();
            count = 0;
            for (Record r : db.<Record>select(Record.class, "key = 'testKey'", true)) {
                db.deleteRecord(r);
                count++;
            }
            assertEquals(1, count, "Should delete 1 record");
            db.commitTransaction();
            
            // Verify deletion
            db.beginTransaction();
            count = 0;
            for (Record r : db.<Record>select(Record.class, "key = 'testKey'")) {
                count++;
            }
            assertEquals(0, count, "Should find 0 records after deletion");
            db.commitTransaction();
        } finally {
            storage.close();
        }
    }

    @Test
    public void testDbServerCaseInsensitive() throws Exception {
        Storage storage = StorageFactory.getInstance().createStorage();
        storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        
        try {
            Database db = new Database(storage, true);
            
            // Insert records with different cases
            db.beginTransaction();
            Record rec1 = new Record();
            rec1.key = "TestKey";
            rec1.value = "value1";
            db.addRecord(rec1);
            
            Record rec2 = new Record();
            rec2.key = "testkey";
            rec2.value = "value2";
            db.addRecord(rec2);
            db.commitTransaction();
            
            // Query with different case - should find both due to unique constraint
            // Actually with unique=true and caseInsensitive=true, only one can exist
            // Let's test that we can't insert duplicate
            db.beginTransaction();
            Record rec3 = new Record();
            rec3.key = "TESTKEY";
            rec3.value = "value3";
            boolean added = db.addRecord(rec3);
            assertFalse(added, "Should not allow duplicate key with different case");
            db.commitTransaction();
        } finally {
            storage.close();
        }
    }
}
