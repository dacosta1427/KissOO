/*
 * $URL: DatabaseTest.java $ 
 * $Rev: 3582 $ 
 * $Date: 2007-11-25 14:29:06 +0300 (Вс., 25 нояб. 2007) $
 *
 * Copyright 2005 Netup, Inc. All rights reserved.
 * URL:    http://www.netup.biz
 * e-mail: info@netup.biz
 */

package org.garret.perst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

/**
 * These tests verifies an functionality of the <code>Database</code> class.
 */
public class DatabaseTest {

    Storage storage;
    Database database;

    @BeforeEach
    public void setUp() throws java.lang.Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        database = new Database(storage);
    }

    @AfterEach
    public void tearDown() throws java.lang.Exception {
        if(storage.isOpened())
            storage.close();
    }
    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createTable(...)</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createTable(Stored.class)</code> is invoked twice.</li>
     * <li><code>Stored</code> class implements the <code>Persistent</code>
     * interface.</li>
     * </ul>
     * <P>
     * <B>Expected result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li>The first invocation of <code>createTable(...)</code> returned <i>true</i> and the
           second invocation returned <i>false</i>.</li>
     * </ul>
     */
    @Test
    public void testCreateTable00() {
        // test target
        assertTrue(database.createTable(Stored.class));
        assertFalse(database.createTable(Stored.class));
    }

    /**
     * <B>Goal:</B> To verify the functionality of the
     * <CODE>addRecord(...)</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the Perst is able to store the <code>persistent</code> object.</li>
     * <li>the <code>addRecord(null)</code>method is invoked.</li>
     * </ul>
     * <P>
     * <B>Expected result:</B>
     * <ul>
     * <li><code>NullPointerException</code> was thrown.</li>
     * </ul>
     */
    @Test
    public void testAddRecord00() {
        assertTrue(database.createTable(Stored.class));
        //test target
        assertThrows(NullPointerException.class, () -> database.addRecord(null));
    }

    /**
     * <B>Goal:</B> To verify the functionality of the
     * <CODE>addRecord(...)</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the Perst is able to store the <code>persistent</code> object.</li>
     * <li>the <code>addRecord(persistent)</code>method is invoked.</li>
     * <li><code>persistent</code> implements the <code>Persistent</code>
     * interface.</li>
     * </ul>
     * <P>
     * <B>Expected result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * </ul>
     */
    @Test
    public void testAddRecord01() {
        assertTrue(database.createTable(Stored.class));
        // test target
        database.addRecord(new Stored("asdf"));
    }

    /**
     * <b>Goal:</b> To verify the functionality of the <CODE>addRecode(...)</CODE>
     * and <CODE>getRecords(...)</CODE> methods.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the Perst is able to store the <code>persistent</code> object.</li>
     * <li><code>Stored</code> class implements the <code>Persistent</code>
     * interface.</li>
     * <li><code>createTable(Stored.class)</code> is invoked.</li>
     * <li><code>addRecord(new Stored(...))</code> is invoked.</li>
     * <li><code>getRecords(Stored.class)</code> is invoked.</li>
     * </ul>
     * <B>Expected result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRecords(...)</code> returned added record.</li>
     * </ul>
     */
    @Test
    public void testAddRecordGetRecords() {
        assertTrue(database.createTable(Stored.class));
        Stored st = new Stored("qwe");
        database.addRecord(st);
        // test target
        Iterator<IPersistent> i = database.getRecords(Stored.class);
        assertEquals(st,  i.next());
        assertFalse(i.hasNext());
    }

    /**
     * <b>Goal:</b> To verify the functionality of the <CODE>addRecod(...)</CODE>,
     * <CODE>deleteRecod(...)</CODE> and <CODE>getRecords(...)</CODE> methods.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createTable(Stored.class)</code> is invoked.</li>
     * <li><code>stored</code> object implements the <code>Persistent</code>
     * interface.</li>
     * <li><code>addRecord(stored)</code> is invoked.</li>
     * <li><code>deleteRecord(stored)</code> is invoked.</li>
     * <li><code>getRecords(Stored.class)</code> is invoked.</li>
     * </ul>
     * <P>
     * <B>Expected result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRecords(...)</code> returned empty set.</li>
     * </ul>
     */
    @Test
    public void testAddRecordDeleteRecordGetRecords() {
        assertTrue(database.createTable(Stored.class));
        Stored st = new Stored("qwe");
        database.addRecord(st);
        database.deleteRecord(st);
        Iterator<IPersistent> i = database.getRecords(Stored.class);
        assertFalse(i.hasNext());
    }

    // ===== Phase 2D extensions =====

    @Test
    public void testDropTable() {
        assertTrue(database.createTable(Stored.class));
        assertTrue(database.dropTable(Stored.class));
        // After dropping, createTable should return true again
        assertTrue(database.createTable(Stored.class));
    }

    @Test
    public void testGetRecordsEmpty() {
        assertTrue(database.createTable(Stored.class));
        Iterator<IPersistent> i = database.getRecords(Stored.class);
        assertFalse(i.hasNext(), "Empty table iterator should be exhausted immediately");
    }

    @Test
    public void testMultipleRecords() {
        assertTrue(database.createTable(Stored.class));
        Stored s1 = new Stored("first");
        Stored s2 = new Stored("second");
        Stored s3 = new Stored("third");
        database.addRecord(s1);
        database.addRecord(s2);
        database.addRecord(s3);

        int count = 0;
        for (Iterator<IPersistent> it = database.getRecords(Stored.class); it.hasNext(); ) {
            it.next();
            count++;
        }
        assertEquals(3, count, "Should have 3 records");
    }

    @Test
    public void testCreateIndex() {
        assertTrue(database.createTable(Stored.class));
        // createIndex on a field that exists
        assertTrue(database.createIndex(Stored.class, "name", true));
        // Second call should return false (index already exists)
        assertFalse(database.createIndex(Stored.class, "name", true));
    }

    @Test
    public void testAddRecordIndexed() {
        assertTrue(database.createTable(Stored.class));
        assertTrue(database.createIndex(Stored.class, "name", true));
        Stored s = new Stored("indexed");
        database.addRecord(s);

        // Record should be retrievable
        Iterator<IPersistent> it = database.getRecords(Stored.class);
        assertTrue(it.hasNext());
        assertEquals(s, it.next());
    }

    @Test
    public void testDeleteRecordWithIndex() {
        assertTrue(database.createTable(Stored.class));
        assertTrue(database.createIndex(Stored.class, "name", true));
        Stored s = new Stored("toDelete");
        database.addRecord(s);
        database.deleteRecord(s);

        Iterator<IPersistent> it = database.getRecords(Stored.class);
        assertFalse(it.hasNext(), "Table should be empty after delete");
    }

    @Test
    public void testAddRecordNoTableCreatesImplicitly() {
        // Adding record without creating table first silently creates it
        assertDoesNotThrow(() -> database.addRecord(new Stored("orphan")));
    }

    @Test
    public void testGetRecordsNoTableReturnsEmpty() {
        // getRecords on a non-existent table returns empty iterator (no throw)
        assertDoesNotThrow(() -> database.getRecords(Stored.class));
    }

    @Test
    public void testSelectRecords() {
        assertTrue(database.createTable(Stored.class));
        database.addRecord(new Stored("alpha"));
        database.addRecord(new Stored("beta"));
        database.addRecord(new Stored("gamma"));

        // Select all that start with a specific name
        Iterator<IPersistent> it = database.select(Stored.class, "name = 'alpha'");
        assertTrue(it.hasNext());
        Stored found = (Stored) it.next();
        assertEquals("alpha", found.name);
        assertFalse(it.hasNext());
    }

	/**
	 * Internal class
	 */
    private static class Stored extends Persistent{
        public String name;
        Stored(String name){
            this.name = name;
        }
        public Stored(){}
    }

}
