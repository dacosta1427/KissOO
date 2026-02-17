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

import junit.framework.*;

import java.util.Iterator;

/**
 * These tests verifies an functionality of the <code>Database</code> class.
 */
public class DatabaseTest extends TestCase {

    Storage storage;
    Database database;

    public DatabaseTest(String testName) {
        super(testName);
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.TestSuite(DatabaseTest.class);
    }

    protected void setUp() throws java.lang.Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        database = new Database(storage);
    }

    protected void tearDown() throws java.lang.Exception {
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
     * <li>The first invocation of <code>addRecord(...)</code> created the table automatically.
            </li>
      * </ul>
      */
    public void testCreateTable00() {
        // test target - tables are now created automatically
        database.addRecord(new Stored("test1"));
        database.addRecord(new Stored("test2"));
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
    public void testAddRecord00() {
        database.addRecord(new Stored("test"));
        try{
            //test target
            database.addRecord(null);
            fail("NullPointerExceptions expected");
        }catch(NullPointerException e){
            // expected exception
        }
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
    public void testAddRecord01() {
        database.addRecord(new Stored("test"));
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
    public void testAddRecordGetRecords() {
        database.addRecord(new Stored("test"));
        Stored st = new Stored("qwe");
        database.addRecord(st);
        storage.commit();
        // test target - getRecords returns iterator of all stored records
        Iterator<IPersistent> i = database.getRecords(Stored.class);
        int count = 0;
        while (i.hasNext()) {
            i.next();
            count++;
        }
        assertTrue(count >= 1);
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
    public void testAddRecordDeleteRecordGetRecords() {
        database.addRecord(new Stored("test"));
        Stored st = new Stored("qwe");
        database.addRecord(st);
        storage.commit();
        // delete the record
        database.deleteRecord(st);
        storage.commit();
        // Just verify no exception is thrown - record deletion works
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
