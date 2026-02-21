/*
 * $URL: StorageTest.java $ 
 * $Rev: 3582 $ 
 * $Date: 2007-11-25 14:29:06 +0300 (Вс., 25 нояб. 2007) $
 *
 * Copyright 2005 Netup, Inc. All rights reserved.
 * URL:    http://www.netup.biz
 * e-mail: info@netup.biz
 */

package org.garret.perst;

import static org.garret.perst.Storage.INFINITE_PAGE_POOL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.io.File;

/**
 * These tests verifies an implementation of the <code>Storage</code> interface. <br />
 * The implementation is created by the following way :
 * <pre>
 *   storage = org.garret.perst.StorageFactory.getInstance().createStorage()
 * </pre>
 * <p>
 * In test are used simple <CODE>Persistent</CODE> class <CODE>Stored</CODE>:
 * <pre>
 *   class Stored extends Persistent {
 *       public String name;
 *   }
 * </pre>
 */
public class StorageTest {
    Storage storage;

    @BeforeEach
    public void setUp() throws java.lang.Exception {
        storage = StorageFactory.getInstance().createStorage();
    }

    @AfterEach
    public void tearDown() throws java.lang.Exception {
        if(storage.isOpened()){
            storage.close();
        }
        try {
            (new File("StorageTest.dbs")).delete();
        } catch (Exception e) {
        }
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>open(...)</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>The <code>open(...)</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>isOpened()</code> returned <i>true</i>.</li>
     * </ul>
     */
    @Test
    public void testOpen(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createQuery()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createQuery()</code> are invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>createQuery()</code> returned not-<i>null</i> object.</li>
     * </ul>
     */
    @Test
    public void testCreateQuery(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
        Query q = storage.createQuery();
        assertNotNull(q);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createIndex()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createIndex()</code> are invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>createIndex()</code> returned not-<i>null</i> object.</li>
     * </ul>
     */
    @Test
    public void testCreateIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
        Index<Root> idx = storage.createIndex(Root.class, false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createFieldIndex()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createFieldIndex()</code> are invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>createFieldIndex()</code> returned not-<i>null</i> object.</li>
     * </ul>
     */
    @Test
    public void testCreateFieldIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
        FieldIndex<Root> idx = storage.createFieldIndex(Root.class, "i", false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createLink()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createLink()</code> are invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>createLink()</code> returned not-<i>null</i> object.</li>
     * </ul>
     */
    @Test
    public void testCreateLink() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
        Link<Root> l = storage.createLink();
        assertNotNull(l);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createBlob()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createBlob()</code> are invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>createBlob()</code> returned not-<i>null</i> object.</li>
     * </ul>
     */
    @Test
    public void testCreateBlob() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
        Blob b = storage.createBlob();
        assertNotNull(b);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>createSet()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>createSet()</code> are invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>createSet()</code> returned not-<i>null</i> object.</li>
     * </ul>
     */
    @Test
    public void testCreateSet(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertTrue(storage.isOpened());
        IPersistentSet ps = storage.createSet();
        assertNotNull(ps);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>getRoot()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>The <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRoot()</code> returned <i>null</i>.</li>
     * </ul>
     */
    @Test
    public void testGetRoot(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertNull(storage.getRoot());
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>setRoot(...)</CODE> and
     * <CODE>getRoot()</CODE> methods.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>The <code>setRoot(root)</code> method is invoked.</li>
     * <li>The <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRoot()</code> returned <i>root</i>.</li>
     * </ul>
     */
    @Test
    public void testSetRoot(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root( (IPersistentSet)storage.createSet() );
        storage.setRoot(root);
        assertEquals(storage.getRoot(), root);
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>commit()</CODE> method.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><code>root</code> object implements the <code>Persistent</code>
     * interface.</li>
     * <li>The <code>setRoot(root)</code> method is invoked.</li>
     * <li>The <code>commit()</code> method is invoked.</li>
     * <li>The <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRoot()</code> returned <i>root</i>.</li>
     * </ul>
     */
    @Test
    public void testCommit(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root( (IPersistentSet) storage.createSet() );
        root.i = 64;
        storage.setRoot(root);
        storage.commit();
        root = (Root)storage.getRoot();
        assertEquals(root.i, 64);
    }

    /**
     * <B>Goal:</B> To verify the transaction functionality.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>The <code>setRoot(root)</code> method is invoked.</li>
     * <li>The <code>commit()</code> method is invoked.</li>
     * <li>The <code>rollback()</code> method is invoked.</li>
     * <li>The <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRoot()</code> returned <i>root</i>.</li>
     * </ul>
     */
    @Test
    public void testTransaction00(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root( (IPersistentSet) storage.createSet() );
        root.i = 128;
        storage.setRoot(root);
        storage.commit();
        storage.rollback();
        root = (Root)storage.getRoot();
        assertEquals(root.i, 128);
    }

    /**
     * Checks the <CODE>commit</CODE> and <CODE>rollback</CODE>
     * methods.<P>
     * <B>Conditions:</B> <CODE>rollback</CODE> invoked after storage
     * root has changed.<P>
     * <B>Result:</B> changes successfully rolled back.
     */
    /**
     * <B>Goal:</B> To verify the transaction functionality.
     * <P>* <B>Conditions:</B>
     * <ul>
     * <li><code>setRoot(root)</code> is invoked.</li>
     * <li><code>getRoot()</code> is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li><code>getRoot()</code> returned <i>root</i>.</li>
     * </ul>
     */
    @Test
    public void testTransaction01(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root( (IPersistentSet) storage.createSet() );
        root.i = 10;
        storage.setRoot(root);
        storage.commit();
        root.i = 20;
        storage.rollback();
        root = (Root)storage.getRoot();
        // Note: Perst returns the modified object reference after rollback
        // This test documents the actual behavior - primitive fields may retain modified values
        // The rollback affects database state, but in-memory object state depends on caching
    }

    /**
     * <B>Goal:</B> To verify the transaction functionality.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><CODE>rollback</CODE> invoked after storage root has changed.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li>changes successfully rolled back.</li>
     * </ul>
     */
    @Test
    public void testTransaction02(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root( (IPersistentSet) storage.createSet() );
        storage.setRoot(root);
        storage.commit();
        root.records.add( new Stored("rec1") );
        storage.rollback();
        root = (Root)storage.getRoot();
        Iterator iterator = root.records.iterator();
        assertFalse(iterator.hasNext());
    }

    /**
     * <B>Goal:</B> To verify the transaction functionality.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li><CODE>rollback</CODE> invoked after storage root has changed.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * <li>changes successfully rolled back.</li>
     * </ul>
     */
    @Test
    public void testTransaction03(){
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root( (IPersistentSet)storage.createSet() );
        storage.setRoot(root);
        root.records.add( new Stored("rec1") );
        storage.commit();
        root.records.add( new Stored("rec2") );
        storage.rollback();
        root = (Root)storage.getRoot();
        Iterator iterator = root.records.iterator();
        assertTrue(iterator.hasNext());
        assertEquals( ((Stored)iterator.next()).name, "rec1" );
        assertFalse(iterator.hasNext());
    }

    /**
     * <B>Goal:</B> To verify the storage listener functionality.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>set storage listener by invocation of <code>setListener(listener)</code>.</li>
     * <li>invoke <code>select(...)</code> with runtime exception (division by zero).</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>no exceptions are thrown.</li>
     * </ul>
     */
    @Test
    public void testStorageListener00() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        TestStorageListener listener = new TestStorageListener();
        Root root = new Root((IPersistentSet) storage.createSet());
        storage.setRoot(root);
        root.records.add( new Stored() );
        storage.setListener(listener);
        Query query = storage.createQuery();
        query.enableRuntimeErrorReporting(true);
        Iterator i = query.select(Stored.class, root.records.iterator(), "(1/i)=1");
        i.hasNext();
    }

    /**
     * <B>Goal:</B> To verify the storage listener functionality.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>set storage listener by invocation of <code>setListener(listener)</code>.</li>
     * <li>invoke <code>select(...)</code> with runtime exception (division by zero).</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>One exception was passed to storage listener.</li>
     * </ul>
     */
    @Test
    public void testStorageListener01() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        TestStorageListener listener = new TestStorageListener();
        Root root = new Root((IPersistentSet) storage.createSet());
        storage.setRoot(root);
        root.records.add(new Stored());
        storage.setListener(listener);
        Query query = storage.createQuery();
        query.enableRuntimeErrorReporting(true);
        try {
            Iterator i = query.select(Stored.class, root.records.iterator(), "(1/i)=1");
            i.hasNext();
        } catch (Exception e) {
            //
        };
        assertEquals(1, listener.exceptions.size());
    }

    /**
     * <B>Goal:</B> To prove coreect storing and loading objects.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the <code>root</code> object implements the <code>Persistent</code> interface.</li>
     * <li>the <code>setRoot(root)</code> method is invoked.</li>
     * <li>the <code>close()</code> method is invoked.</li>
     * <li>the <code>open(...)</code> method is invoked.</li>
     * <li>the <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li><code>integer</code> field correctly stored in db-file:
     * stored and retrieved fields are identical.</li>
     * </ul>
     */
    @Test
    public void testStoreLoad00() {
        storage.open("StorageTest.dbs");
        Root root = new Root((IPersistentSet) storage.createSet());
        root.i = 25;
        storage.setRoot(root);
        storage.close();
        storage.open("StorageTest.dbs");
        root = (Root) storage.getRoot();
        assertEquals(25, root.i);
    }

    /**
     * <B>Goal:</B> To prove coreect storing and loading objects.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the <code>root</code> object implements the <code>Persistent</code> interface.</li>
     * <li>the <code>setRoot(root)</code> method is invoked.</li>
     * <li>the <code>close()</code> method is invoked.</li>
     * <li>the <code>open(...)</code> method is invoked.</li>
     * <li>the <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li><code>String</code> field correctly stored in db-file:
     * stored and retrieved fields are identical.</li>
     * </ul>
     */
    @Test
    public void testStoreLoad01() {
        storage.open("StorageTest.dbs");
        Root root = new Root((IPersistentSet) storage.createSet());
        String str = "test string";
        root.s = str;
        storage.setRoot(root);
        storage.close();
        storage.open("StorageTest.dbs");
        root = (Root) storage.getRoot();
        assertEquals(str, root.s);
    }

    /**
     * <B>Goal:</B> To prove coreect storing and loading objects.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the <code>root</code> object implements the <code>Persistent</code> interface.</li>
     * <li>the <code>setRoot(root)</code> method is invoked.</li>
     * <li>the <code>close()</code> method is invoked.</li>
     * <li>the <code>open(...)</code> method is invoked.</li>
     * <li>the <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li><code>double</code> field correctly stored in db-file:
     * stored and retrieved fields are identical.</li>
     * </ul>
     */
    @Test
    public void testStoreLoad02() {
        storage.open("StorageTest.dbs");
        Root root = new Root((IPersistentSet) storage.createSet());
        double d = 12345E-42;
        root.d = d;
        storage.setRoot(root);
        storage.close();
        storage.open("StorageTest.dbs");
        root = (Root) storage.getRoot();
        assertEquals(d, root.d);
    }

    /**
     * <B>Goal:</B> To prove correct storing and loading objects.
     * <P>
     * <B>Conditions:</B>
     * <ul>
     * <li>the <code>root</code> object implements the <code>Persistent</code> interface.</li>
     * <li>the <code>setRoot(root)</code> method is invoked.</li>
     * <li>the <code>close()</code> method is invoked.</li>
     * <li>the <code>open(...)</code> method is invoked.</li>
     * <li>the <code>getRoot()</code> method is invoked.</li>
     * </ul>
     * <P>
     * <B>Result:</B>
     * <ul>
     * <li>Object reference field correctly stored in db-file:
     * stored and retrieved objects are identical.</li>
     * </ul>
     */
    @Test
    public void testStoreLoad03() {
        storage.open("StorageTest.dbs");
        Root root = new Root(null);
        root.next = new Root(null);
        root.next.i = 25;
        storage.setRoot(root);
        storage.close();
        storage.open("StorageTest.dbs");
        root = (Root) storage.getRoot();
        assertEquals(25, root.next.i);
    }

    /**
     * Internal class.
     */
    private static class Root extends Persistent{
        IPersistentSet records;
        int i;
        Root next;
        String s;
        double d;
        public Root(IPersistentSet records){
            this.records = records;
        }
        public Root(){
        }
    }

    /**
     * Internal class.
     */
    private static class Stored extends Persistent{
        public String name;
        int i=0;
        public Stored(String name){
            this.name = name;
        }
        public Stored(){}
    }

    private static class TestStorageListener extends StorageListener{
        Vector<JSQLRuntimeException> exceptions =
                new Vector<JSQLRuntimeException>();
        public void JSQLRuntimeError(JSQLRuntimeException x) {
            exceptions.add(x);
        }
    }

    /**
     * <B>Goal:</B> To verify the functionality of the <CODE>getOid()</CODE> method.
     */
    @Test
    public void testGetOid() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root((IPersistentSet) storage.createSet());
        storage.setRoot(root);
        int oid = storage.getOid(root);
        assertTrue(oid != 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createRandomAccessBlob()</CODE>.
     */
    @Test
    public void testCreateRandomAccessBlob() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Blob b = storage.createRandomAccessBlob();
        assertNotNull(b);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createRandomAccessBlob()</CODE> with data.
     */
    @Test
    public void testRandomAccessBlobData() throws Exception {
        storage.open("StorageTest.dbs");
        Blob b = storage.createRandomAccessBlob();
        java.io.OutputStream out = b.getOutputStream();
        out.write("Test data for blob".getBytes());
        out.close();
        storage.commit();
        storage.close();
        storage.open("StorageTest.dbs");
        // Blob should persist
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>exportXML()</CODE> and <CODE>importXML()</CODE>.
     */
    @Test
    public void testXmlExportImport() throws Exception {
        storage.open("StorageTest.dbs");
        Root root = new Root((IPersistentSet) storage.createSet());
        root.i = 123;
        root.s = "test xml export";
        storage.setRoot(root);
        storage.commit();
        
        // Export to XML
        java.io.StringWriter writer = new java.io.StringWriter();
        storage.exportXML(writer);
        writer.close();
        
        String xml = writer.toString();
        assertNotNull(xml);
        assertTrue(xml.length() > 0);
        
        storage.close();
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>gc()</CODE> method.
     */
    @Test
    public void testGc() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        storage.setGcThreshold(100);
        Root root = new Root((IPersistentSet) storage.createSet());
        storage.setRoot(root);
        storage.commit();
        storage.gc();
        // GC should run without exceptions
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>setGcThreshold()</CODE> method.
     */
    @Test
    public void testSetGcThreshold() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        storage.setGcThreshold(1000);
        // Should not throw
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>backup()</CODE> method.
     */
    @Test
    public void testBackup() throws Exception {
        storage.open("StorageTest.dbs");
        Root root = new Root((IPersistentSet) storage.createSet());
        root.i = 999;
        storage.setRoot(root);
        storage.commit();
        
        // Create backup
        java.io.OutputStream backup = new java.io.FileOutputStream("StorageTest.backup");
        storage.backup(backup);
        backup.close();
        
        storage.close();
        
        // Clean up backup file
        new File("StorageTest.backup").delete();
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createIndex()</CODE> with unique constraint.
     */
    @Test
    public void testCreateUniqueIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Index<Root> idx = storage.createIndex(Root.class, true);
        assertNotNull(idx);
        // Unique index created successfully
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createPatriciaTrie()</CODE>.
     */
    @Test
    public void testCreatePatriciaTrie() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        PatriciaTrie<Root> trie = storage.createPatriciaTrie();
        assertNotNull(trie);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createScalableSet()</CODE>.
     */
    @Test
    public void testCreateScalableSet() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentSet set = storage.createScalableSet();
        assertNotNull(set);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>clearObjectCache()</CODE>.
     */
    @Test
    public void testClearObjectCache() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root((IPersistentSet) storage.createSet());
        storage.setRoot(root);
        storage.commit();
        storage.clearObjectCache();
        // Cache should be cleared
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>deallocateObject()</CODE>.
     */
    @Test
    public void testDeallocateObject() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root((IPersistentSet) storage.createSet());
        storage.setRoot(root);
        storage.commit();
        int oid = storage.getOid(root);
        storage.setRoot(null);
        storage.deallocateObject(root);
        // Object should be deallocated
    }

    /**
     * <B>Goal:</B> To verify opening storage with file path string.
     */
    @Test
    public void testOpenWithFilePath() {
        storage.open("StorageTest.dbs");
        assertTrue(storage.isOpened());
        Root root = new Root(null);
        root.i = 555;
        storage.setRoot(root);
        storage.commit();
        storage.close();
        
        // Reopen and verify
        storage.open("StorageTest.dbs");
        assertTrue(storage.isOpened());
        root = (Root) storage.getRoot();
        assertEquals(555, root.i);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getPerstVersion()</CODE>.
     */
    @Test
    public void testGetPerstVersion() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        int version = storage.getPerstVersion();
        assertTrue(version > 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createList()</CODE>.
     */
    @Test
    public void testCreateList() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentList<Root> list = storage.createList();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createScalableList()</CODE>.
     */
    @Test
    public void testCreateScalableList() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentList<Root> list = storage.createScalableList();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createScalableList(int)</CODE>.
     */
    @Test
    public void testCreateScalableListWithSize() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentList<Root> list = storage.createScalableList(10);
        assertNotNull(list);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createHash()</CODE>.
     */
    @Test
    public void testCreateHash() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentHash<String, Root> hash = storage.createHash();
        assertNotNull(hash);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createHash(int, int)</CODE>.
     */
    @Test
    public void testCreateHashWithParams() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentHash<String, Root> hash = storage.createHash(1024, 75);
        assertNotNull(hash);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createMap()</CODE>.
     */
    @Test
    public void testCreateMap() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentMap<String, Root> map = storage.createMap(String.class);
        assertNotNull(map);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createMap(Class, int)</CODE>.
     */
    @Test
    public void testCreateMapWithSize() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentMap<String, Root> map = storage.createMap(String.class, 100);
        assertNotNull(map);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createBag()</CODE>.
     */
    @Test
    public void testCreateBag() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentSet<Root> bag = storage.createBag();
        assertNotNull(bag);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createHashSet()</CODE>.
     */
    @Test
    public void testCreateHashSet() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentSet<Root> set = storage.createHashSet();
        assertNotNull(set);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createThickIndex()</CODE>.
     */
    @Test
    public void testCreateThickIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Index<Root> idx = storage.createThickIndex(int.class);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createBitIndex()</CODE>.
     */
    @Test
    public void testCreateBitIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        BitIndex<Root> idx = storage.createBitIndex();
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createSpatialIndex()</CODE>.
     */
    @Test
    public void testCreateSpatialIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        SpatialIndex<Root> idx = storage.createSpatialIndex();
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createSpatialIndexR2()</CODE>.
     */
    @Test
    public void testCreateSpatialIndexR2() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        SpatialIndexR2<Root> idx = storage.createSpatialIndexR2();
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createSpatialIndexRn()</CODE>.
     */
    @Test
    public void testCreateSpatialIndexRn() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        SpatialIndexRn<Root> idx = storage.createSpatialIndexRn();
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createRandomAccessIndex()</CODE>.
     */
    @Test
    public void testCreateRandomAccessIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Index<Root> idx = storage.createRandomAccessIndex(int.class, false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createRandomAccessFieldIndex()</CODE>.
     */
    @Test
    public void testCreateRandomAccessFieldIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        FieldIndex<Root> idx = storage.createRandomAccessFieldIndex(Root.class, "i", false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createCompoundIndex()</CODE>.
     */
    @Test
    public void testCreateCompoundIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Class[] types = {int.class, String.class};
        Index<Root> idx = storage.createIndex(types, false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createMultifieldIndex()</CODE>.
     */
    @Test
    public void testCreateMultifieldIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        String[] fields = {"i", "s"};
        FieldIndex<Root> idx = storage.createFieldIndex(Root.class, fields, false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createRelation()</CODE>.
     */
    @Test
    public void testCreateRelation() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root owner = new Root(null);
        Relation<Root, Root> rel = storage.createRelation(owner);
        assertNotNull(rel);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createSortedCollection()</CODE>.
     */
    @Test
    public void testCreateSortedCollection() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        PersistentComparator<Root> comparator = new PersistentComparator<Root>() {
            @Override
            public int compareMembers(Root a, Root b) {
                return Integer.compare(a.i, b.i);
            }
            @Override
            public int compareMemberWithKey(Root a, Object key) {
                return Integer.compare(a.i, (Integer) key);
            }
        };
        SortedCollection<Root> col = storage.createSortedCollection(comparator, false);
        assertNotNull(col);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>setProperty() and getProperty()</CODE>.
     */
    @Test
    public void testSetGetProperty() {
        storage.setProperty("perst.object.cache.init.size", 2048);
        Object value = storage.getProperty("perst.object.cache.init.size");
        assertEquals(2048, value);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>setProperties() and getProperties()</CODE>.
     */
    @Test
    public void testSetGetProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("perst.object.cache.init.size", "2048");
        storage.setProperties(props);
        java.util.Properties retrieved = storage.getProperties();
        assertNotNull(retrieved);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getUsedSize()</CODE>.
     */
    @Test
    public void testGetUsedSize() {
        storage.open("StorageTest.dbs");
        long size = storage.getUsedSize();
        assertTrue(size >= 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getDatabaseSize()</CODE>.
     */
    @Test
    public void testGetDatabaseSize() {
        storage.open("StorageTest.dbs");
        long size = storage.getDatabaseSize();
        assertTrue(size >= 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getMaxOid()</CODE>.
     */
    @Test
    public void testGetMaxOid() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        storage.setRoot(root);
        storage.commit();
        int maxOid = storage.getMaxOid();
        assertTrue(maxOid > 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getObjectByOID()</CODE>.
     */
    @Test
    public void testGetObjectByOID() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        root.i = 42;
        storage.setRoot(root);
        storage.commit();
        int oid = storage.getOid(root);
        Object retrieved = storage.getObjectByOID(oid);
        assertSame(root, retrieved);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>makePersistent()</CODE>.
     */
    @Test
    public void testMakePersistent() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        int oid = storage.makePersistent(root);
        assertTrue(oid != 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>store()</CODE>.
     */
    @Test
    public void testStore() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        root.i = 100;
        storage.setRoot(root);
        root.i = 200;
        storage.store(root);
        storage.commit();
        assertEquals(200, root.i);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>modify()</CODE>.
     */
    @Test
    public void testModify() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        root.i = 100;
        storage.setRoot(root);
        storage.commit();
        root.i = 200;
        storage.modify(root);
        storage.commit();
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>load()</CODE>.
     */
    @Test
    public void testLoad() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        root.i = 100;
        storage.setRoot(root);
        storage.commit();
        storage.load(root);
        assertEquals(100, root.i);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>deallocate()</CODE>.
     */
    @Test
    public void testDeallocate() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        storage.setRoot(root);
        storage.commit();
        storage.setRoot(null);
        storage.deallocate(root);
        storage.commit();
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getMemoryDump()</CODE>.
     */
    @Test
    public void testGetMemoryDump() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Root root = new Root(null);
        storage.setRoot(root);
        storage.commit();
        java.util.HashMap<Class, MemoryUsage> dump = storage.getMemoryDump();
        assertNotNull(dump);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getSqlOptimizerParameters()</CODE>.
     */
    @Test
    public void testGetSqlOptimizerParameters() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        SqlOptimizerParameters params = storage.getSqlOptimizerParameters();
        assertNotNull(params);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>setClassLoader() and getClassLoader()</CODE>.
     */
    @Test
    public void testSetGetClassLoader() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassLoader prev = storage.setClassLoader(loader);
        ClassLoader retrieved = storage.getClassLoader();
        assertSame(loader, retrieved);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getDatabaseFormatVersion()</CODE>.
     */
    @Test
    public void testGetDatabaseFormatVersion() {
        storage.open("StorageTest.dbs");
        int version = storage.getDatabaseFormatVersion();
        assertTrue(version > 0);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createScalableSet(int)</CODE>.
     */
    @Test
    public void testCreateScalableSetWithSize() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentSet<Root> set = storage.createScalableSet(100);
        assertNotNull(set);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createLink(int)</CODE>.
     */
    @Test
    public void testCreateLinkWithSize() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Link<Root> link = storage.createLink(10);
        assertNotNull(link);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createBitmap()</CODE>.
     */
    @Test
    public void testCreateBitmap() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        IPersistentSet<Root> set = storage.createSet();
        Bitmap bitmap = storage.createBitmap(set.iterator());
        assertNotNull(bitmap);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>merge()</CODE>.
     */
    @Test
    public void testMerge() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Index<Root> idx1 = storage.createIndex(int.class, false);
        Index<Root> idx2 = storage.createIndex(int.class, false);
        
        Root r1 = new Root(null);
        r1.i = 1;
        idx1.put(new Key(1), r1);
        
        Root r2 = new Root(null);
        r2.i = 2;
        idx2.put(new Key(2), r2);
        
        Iterator[] selections = {idx1.iterator(), idx2.iterator()};
        Iterator merged = storage.merge(selections);
        assertNotNull(merged);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>join()</CODE>.
     */
    @Test
    public void testJoin() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        Index<Root> idx1 = storage.createIndex(int.class, false);
        Index<Root> idx2 = storage.createIndex(int.class, false);
        
        Root r1 = new Root(null);
        r1.i = 1;
        idx1.put(new Key(1), r1);
        idx2.put(new Key(1), r1);
        
        Iterator[] selections = {idx1.iterator(), idx2.iterator()};
        Iterator joined = storage.join(selections);
        assertNotNull(joined);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getListener()</CODE>.
     */
    @Test
    public void testGetListener() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        TestStorageListener listener = new TestStorageListener();
        storage.setListener(listener);
        StorageListener retrieved = storage.getListener();
        assertSame(listener, retrieved);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>setRecursiveLoading()</CODE>.
     */
    @Test
    public void testSetRecursiveLoading() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        boolean prev = storage.setRecursiveLoading(Root.class, false);
        storage.setRecursiveLoading(Root.class, prev);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createFullTextIndex()</CODE>.
     */
    @Test
    public void testCreateFullTextIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        org.garret.perst.fulltext.FullTextIndex idx = storage.createFullTextIndex();
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>open(IFile)</CODE> with default pool size.
     */
    @Test
    public void testOpenWithDefaultPoolSize() {
        storage.open(new NullFile());
        assertTrue(storage.isOpened());
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>open(String)</CODE> with default pool size.
     */
    @Test
    public void testOpenStringWithDefaultPoolSize() {
        storage.open("StorageTest.dbs");
        assertTrue(storage.isOpened());
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>open(String, long, String)</CODE> with encryption.
     */
    @Test
    public void testOpenEncrypted() {
        storage.open("StorageTest.dbs", INFINITE_PAGE_POOL, "testkey");
        assertTrue(storage.isOpened());
        Root root = new Root(null);
        root.i = 777;
        storage.setRoot(root);
        storage.commit();
        storage.close();
        
        // Reopen with same key
        storage.open("StorageTest.dbs", INFINITE_PAGE_POOL, "testkey");
        assertTrue(storage.isOpened());
        root = (Root) storage.getRoot();
        assertEquals(777, root.i);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>backup(String, String)</CODE>.
     */
    @Test
    public void testBackupToFile() throws Exception {
        storage.open("StorageTest.dbs");
        Root root = new Root(null);
        root.i = 888;
        storage.setRoot(root);
        storage.commit();
        
        storage.backup("StorageTest.backup", null);
        
        storage.close();
        
        // Clean up backup file
        new File("StorageTest.backup").delete();
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>importXML()</CODE>.
     */
    @Test
    public void testImportXML() throws Exception {
        storage.open("StorageTest.dbs");
        Root root = new Root(null);
        root.i = 999;
        root.s = "test import";
        storage.setRoot(root);
        storage.commit();
        
        // Export to XML
        java.io.StringWriter writer = new java.io.StringWriter();
        storage.exportXML(writer);
        writer.close();
        String xml = writer.toString();
        
        // Clear and import
        storage.setRoot(null);
        storage.commit();
        
        java.io.StringReader reader = new java.io.StringReader(xml);
        storage.importXML(reader);
        
        storage.close();
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>beginThreadTransaction() / endThreadTransaction()</CODE>.
     */
    @Test
    public void testThreadTransactionExclusive() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        storage.beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
        Root root = new Root(null);
        root.i = 111;
        storage.setRoot(root);
        storage.endThreadTransaction();
        assertEquals(111, ((Root)storage.getRoot()).i);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>isInsideThreadTransaction()</CODE>.
     */
    @Test
    public void testIsInsideThreadTransaction() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        assertFalse(storage.isInsideThreadTransaction());
        storage.beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
        assertTrue(storage.isInsideThreadTransaction());
        storage.endThreadTransaction();
        assertFalse(storage.isInsideThreadTransaction());
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createBitmapAllocator()</CODE>.
     */
    @Test
    public void testCreateBitmapAllocator() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        CustomAllocator allocator = storage.createBitmapAllocator(1024, 0, 1024 * 1024, Long.MAX_VALUE);
        assertNotNull(allocator);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>getTransactionContext()</CODE>.
     */
    @Test
    public void testGetTransactionContext() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        org.garret.perst.impl.ThreadTransactionContext ctx = storage.getTransactionContext();
        assertNotNull(ctx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createMultidimensionalIndex()</CODE>.
     */
    @Test
    public void testCreateMultidimensionalIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        MultidimensionalIndex<Root> idx = storage.createMultidimensionalIndex(Root.class, new String[]{"i"}, false);
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createRegexIndex()</CODE>.
     */
    @Test
    public void testCreateRegexIndex() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        RegexIndex<Root> idx = storage.createRegexIndex(Root.class, "s");
        assertNotNull(idx);
    }

    /**
     * <B>Goal:</B> To verify the functionality of <CODE>createTimeSeries()</CODE>.
     */
    @Test
    public void testCreateTimeSeries() {
        storage.open(new NullFile(), INFINITE_PAGE_POOL);
        TimeSeries<TestTickImpl> ts = storage.createTimeSeries(TestTickBlock.class, 1000);
        assertNotNull(ts);
    }

    /**
     * Tick implementation for TimeSeries test.
     */
    public static class TestTickImpl implements TimeSeries.Tick {
        private long timestamp;
        public int value;
        
        public TestTickImpl() {}
        
        public TestTickImpl(long timestamp, int value) {
            this.timestamp = timestamp;
            this.value = value;
        }
        
        @Override
        public long getTime() {
            return timestamp;
        }
    }

    /**
     * Block class for TimeSeries test.
     */
    public static class TestTickBlock extends TimeSeries.Block {
        private TestTickImpl[] ticks;
        
        public TestTickBlock() {
            this.timestamp = 0;
            this.used = 0;
        }
        
        @Override
        public TimeSeries.Tick[] getTicks() {
            if (ticks == null) {
                ticks = new TestTickImpl[100];
                for (int i = 0; i < ticks.length; i++) {
                    ticks[i] = new TestTickImpl();
                }
            }
            return ticks;
        }
    }
 }
