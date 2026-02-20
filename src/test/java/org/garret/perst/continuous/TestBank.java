package org.garret.perst.continuous;

import org.garret.perst.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Iterator;

public class TestBank {
    private Storage storage;
    private CDatabase db;
    private static final String DB_FILE = "TestBank.dbs";
    private static final String INDEX_DIR = "TestBank_index";

    @BeforeEach
    public void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.file.noflush", Boolean.TRUE);
        storage.open(DB_FILE);
        db = CDatabase.instance;
        db.open(storage, INDEX_DIR);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (db != null) {
            db.close();
        }
        if (storage != null && storage.isOpened()) {
            storage.close();
        }
        new File(DB_FILE).delete();
        deleteDir(new File(INDEX_DIR));
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDir(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    @Test
    public void testInitializeAccounts() {
        db.beginTransaction();
        for (int i = 0; i < 10; i++) {
            BankAccount account = new BankAccount();
            account.balance = 1000;
            account.id = i;
            db.insert(account);
        }
        db.commitTransaction();

        int count = 0;
        long totalBalance = 0;
        IterableIterator<BankAccount> iter = db.getRecords(BankAccount.class);
        while (iter.hasNext()) {
            BankAccount account = iter.next();
            count++;
            totalBalance += account.balance;
        }
        assertEquals(10, count);
        assertEquals(10000, totalBalance);
    }

    @Test
    public void testFindAccountById() {
        db.beginTransaction();
        BankAccount account = new BankAccount();
        account.balance = 5000;
        account.id = 42;
        db.insert(account);
        db.commitTransaction();

        BankAccount found = db.getSingleton(db.find(BankAccount.class, "id", new Key(42)));
        assertNotNull(found);
        assertEquals(42, found.id);
        assertEquals(5000, found.balance);
    }

    @Test
    public void testTransfer() throws Exception {
        db.beginTransaction();
        BankAccount src = new BankAccount();
        src.balance = 1000;
        src.id = 1;
        db.insert(src);

        BankAccount dst = new BankAccount();
        dst.balance = 500;
        dst.id = 2;
        db.insert(dst);
        db.commitTransaction();

        db.beginTransaction();
        src = db.getSingleton(db.find(BankAccount.class, "id", new Key(1))).update();
        dst = db.getSingleton(db.find(BankAccount.class, "id", new Key(2))).update();
        
        long amount = 300;
        src.balance -= amount;
        dst.balance += amount;
        
        BankTransfer transfer = new BankTransfer(src, dst, amount);
        db.insert(transfer);
        db.commitTransaction();

        long totalBalance = 0;
        IterableIterator<BankAccount> iterBal = db.getRecords(BankAccount.class);
        while (iterBal.hasNext()) {
            BankAccount a = iterBal.next();
            totalBalance += a.balance;
        }
        assertEquals(1500, totalBalance);

        int transferCount = 0;
        IterableIterator<BankTransfer> iterTrans = db.getRecords(BankTransfer.class);
        while (iterTrans.hasNext()) {
            BankTransfer t = iterTrans.next();
            transferCount++;
            assertEquals(300, t.amount);
        }
        assertEquals(1, transferCount);
    }

    @Test
    public void testConcurrentTransfers() throws Exception {
        final int nAccounts = 20;
        final long initBalance = 1000;
        final long expectedTotal = nAccounts * initBalance;

        db.beginTransaction();
        for (int i = 0; i < nAccounts; i++) {
            BankAccount account = new BankAccount();
            account.balance = initBalance;
            account.id = i;
            db.insert(account);
        }
        db.commitTransaction();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 3; j++) {
                        int srcId = (threadId + j) % nAccounts;
                        int dstId = (threadId + j + 1) % nAccounts;
                        
                        db.beginTransaction();
                        BankAccount src = db.getSingleton(db.find(BankAccount.class, "id", new Key(srcId))).update();
                        BankAccount dst = db.getSingleton(db.find(BankAccount.class, "id", new Key(dstId))).update();
                        
                        long amount = 10;
                        if (src.balance >= amount) {
                            src.balance -= amount;
                            dst.balance += amount;
                            db.insert(new BankTransfer(src, dst, amount));
                            db.commitTransaction();
                        } else {
                            db.rollbackTransaction();
                        }
                    }
                } catch (Exception e) {
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long totalBalance = 0;
        IterableIterator<BankAccount> iterFinal = db.getRecords(BankAccount.class);
        while (iterFinal.hasNext()) {
            BankAccount a = iterFinal.next();
            totalBalance += a.balance;
        }
        assertEquals(expectedTotal, totalBalance);
    }
}
