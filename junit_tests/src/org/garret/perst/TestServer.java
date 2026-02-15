package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * TestServer - Multi-threaded server operations test
 * Converts from tst/TestServer.java
 */
public class TestServer { 
    static class Record extends Persistent { 
        String key;
    }
    
    static class Root extends Persistent { 
        FieldIndex[] indices;
    }
    
    // Scaled down for faster testing
    static final int nThreads = 4;
    static final int nIndices = 4;
    static final int nRecords = 100;

    static String toStr(int i) { 
        String s = "000000" + i;
        return s.substring(s.length()-6);
    }

    static class ClientThread extends Thread {
        Storage db;
        int     id;
        boolean success = true;
        
        ClientThread(Storage db, int id) {
            this.db = db;
            this.id = id;
        }

        public void run() { 
            try {
                int i;
                Root root = (Root)db.getRoot();
                String tid = "Thread" + id + ":";
                FieldIndex index = root.indices[id % nIndices];

                // Insert records
                for (i = 0; i < nRecords; i++) { 
                    db.beginThreadTransaction(Storage.SERIALIZABLE_TRANSACTION);
                    index.exclusiveLock();
                    Record rec = new Record();
                    rec.key = tid + toStr(i);
                    index.put(rec);
                    db.endThreadTransaction();
                }

                // Iterate and verify records
                index.sharedLock();
                Iterator iterator = index.prefixIterator(tid);
                for (i = 0; iterator.hasNext(); i++) { 
                    Record rec = (Record)iterator.next();
                    if (!rec.key.equals(tid + toStr(i))) {
                        success = false;
                        break;
                    }
                }
                if (i != nRecords) {
                    success = false;
                }
                index.unlock();

                // Lookup records using iterator
                for (i = 0; i < nRecords; i++) { 
                    index.sharedLock();
                    String key = tid + toStr(i);
                    Iterator iter = index.prefixIterator(key);
                    boolean found = false;
                    while (iter.hasNext()) {
                        Record rec = (Record)iter.next();
                        if (rec.key.equals(key)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        success = false;
                    }
                    index.unlock();
                }

                // Delete records
                for (i = 0; i < nRecords; i++) { 
                    db.beginThreadTransaction(Storage.SERIALIZABLE_TRANSACTION);
                    index.exclusiveLock();
                    Record rec = (Record)index.remove(new Key(tid + toStr(i)));
                    if (rec != null) {
                        rec.deallocate();
                    }
                    db.endThreadTransaction();
                }
            } catch (Exception e) {
                success = false;
            }
        }
    }

    @Test
    public void testServerMultiThreaded() throws Exception {    
        Storage db = StorageFactory.getInstance().createStorage();
        db.setProperty("perst.alternative.btree", Boolean.TRUE);
        db.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        
        try {
            Root root = (Root)db.getRoot();
            if (root == null) { 
                root = new Root();
                root.indices = new FieldIndex[nIndices];
                for (int i = 0; i < nIndices; i++) {
                    root.indices[i] = db.createFieldIndex(Record.class, "key", true);
                }
                db.setRoot(root);
            }        
            
            // Reset indices for clean test
            for (int i = 0; i < nIndices; i++) {
                root.indices[i].clear();
            }
            
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
            db.close();
        }
    }

    @Test
    public void testServerIndexOperations() throws Exception {
        Storage db = StorageFactory.getInstance().createStorage();
        db.setProperty("perst.alternative.btree", Boolean.TRUE);
        db.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        
        try {
            Root root = new Root();
            root.indices = new FieldIndex[1];
            root.indices[0] = db.createFieldIndex(Record.class, "key", true);
            db.setRoot(root);
            
            FieldIndex index = root.indices[0];
            
            // Insert records
            for (int i = 0; i < 10; i++) {
                Record rec = new Record();
                rec.key = "Key" + i;
                index.put(rec);
            }
            
            // Verify count
            assertEquals(10, index.size());
            
            // Get record using prefix iterator
            Iterator iter = index.prefixIterator("Key5");
            Record rec = null;
            while (iter.hasNext()) {
                Record r = (Record)iter.next();
                if (r.key.equals("Key5")) {
                    rec = r;
                    break;
                }
            }
            assertNotNull(rec, "Should find record with key Key5");
            assertEquals("Key5", rec.key);
            
            // Remove record
            Record removed = (Record)index.remove(new Key("Key5"));
            assertNotNull(removed, "Should find record to remove");
            removed.deallocate();
            assertEquals(9, index.size());
            
            // Prefix iterator
            iter = index.prefixIterator("Key");
            int count = 0;
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
            assertEquals(9, count);
        } finally {
            db.close();
        }
    }
}
