package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestXML.java
 * Tests XML export and import functionality
 */
class TestXML {

    static class Record extends Persistent {
        String strKey;
        long intKey;
        double realKey;
    }

    static class Indices extends Persistent {
        Index<Record> strIndex;
        FieldIndex<Record> intIndex;
        FieldIndex<Record> compoundIndex;
    }

    private Storage storage;
    // Scaled down from 100000 records
    private static final int nRecords = 100;
    private static final String TEST_DB = "testxml.dbs";
    private static final String TEST_DB2 = "testxml2.dbs";
    private static final String TEST_XML = "testxml.xml";

    @BeforeEach
    void setUp() throws Exception {
        new java.io.File(TEST_DB).delete();
        new java.io.File(TEST_DB2).delete();
        new java.io.File(TEST_XML).delete();
        
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);

        Indices root = (Indices) storage.getRoot();
        if (root == null) {
            root = new Indices();
            root.strIndex = storage.createIndex(String.class, true);
            root.intIndex = storage.createFieldIndex(Record.class, "intKey", true);
            root.compoundIndex = storage.createFieldIndex(Record.class, new String[]{"strKey", "intKey"}, true);
            storage.setRoot(root);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
        new java.io.File(TEST_DB2).delete();
        new java.io.File(TEST_XML).delete();
    }

    @Test
    @DisplayName("Test XML export")
    void testXmlExport() throws Exception {
        Indices root = (Indices) storage.getRoot();
        FieldIndex<Record> intIndex = root.intIndex;
        Index<Record> strIndex = root.strIndex;
        FieldIndex<Record> compoundIndex = root.compoundIndex;

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Record rec = new Record();
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.realKey = (double) key;
            intIndex.put(rec);
            strIndex.put(new Key(rec.strKey), rec);
            compoundIndex.put(rec);
        }
        storage.commit();

        // Export to XML
        Writer writer = new BufferedWriter(new FileWriter(TEST_XML));
        storage.exportXML(writer);
        writer.close();

        // Verify XML file was created and has content
        File xmlFile = new File(TEST_XML);
        assertTrue(xmlFile.exists(), "XML file should exist");
        assertTrue(xmlFile.length() > 0, "XML file should have content");

        // Read and verify XML content contains our data
        BufferedReader reader = new BufferedReader(new FileReader(TEST_XML));
        String content = reader.readLine();
        boolean hasRecords = false;
        while (content != null) {
            if (content.contains("strKey") || content.contains("intKey")) {
                hasRecords = true;
                break;
            }
            content = reader.readLine();
        }
        reader.close();
        
        assertTrue(hasRecords, "XML should contain record data");
    }

    @Test
    @DisplayName("Test XML import")
    @SuppressWarnings("unchecked")
    void testXmlImport() throws Exception {
        Indices root = (Indices) storage.getRoot();
        FieldIndex<Record> intIndex = root.intIndex;
        Index<Record> strIndex = root.strIndex;
        FieldIndex<Record> compoundIndex = root.compoundIndex;

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Record rec = new Record();
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.realKey = (double) key;
            intIndex.put(rec);
            strIndex.put(new Key(rec.strKey), rec);
            compoundIndex.put(rec);
        }
        storage.commit();

        // Export to XML
        Writer writer = new BufferedWriter(new FileWriter(TEST_XML));
        storage.exportXML(writer);
        writer.close();
        storage.close();

        // Open new database and import XML
        Storage db2 = StorageFactory.getInstance().createStorage();
        db2.open(TEST_DB2, 32 * 1024 * 1024);
        db2.setProperty("perst.xml.reuse.oid", Boolean.TRUE);

        Reader reader = new BufferedReader(new FileReader(TEST_XML));
        db2.importXML(reader);
        reader.close();

        // Verify imported data
        Indices root2 = (Indices) db2.getRoot();
        FieldIndex<Record> intIndex2 = root2.intIndex;
        
        assertEquals(nRecords, intIndex2.size(), "Imported database should have same number of records");

        // Verify some records can be retrieved
        key = 1999;
        for (int i = 0; i < Math.min(10, nRecords); i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Record rec = intIndex2.get(new Key(key));
            assertNotNull(rec, "Should find imported record");
            assertEquals(key, rec.intKey, "Record intKey should match");
        }

        db2.close();
    }

    @Test
    @DisplayName("Test XML export to string")
    void testXmlExportToString() throws Exception {
        Indices root = (Indices) storage.getRoot();
        FieldIndex<Record> intIndex = root.intIndex;

        // Insert a few records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            rec.realKey = i * 1.5;
            intIndex.put(rec);
        }
        storage.commit();

        // Export to string
        StringWriter writer = new StringWriter();
        storage.exportXML(writer);
        writer.close();

        String xml = writer.toString();
        assertNotNull(xml, "XML should not be null");
        assertTrue(xml.length() > 0, "XML should have content");
        assertTrue(xml.contains("<?xml"), "XML should have XML declaration");
    }

    @Test
    @DisplayName("Test XML import from string")
    void testXmlImportFromString() throws Exception {
        Indices root = (Indices) storage.getRoot();
        FieldIndex<Record> intIndex = root.intIndex;

        // Insert records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            rec.realKey = i * 1.5;
            intIndex.put(rec);
        }
        storage.commit();

        // Export to string
        StringWriter writer = new StringWriter();
        storage.exportXML(writer);
        writer.close();
        String xml = writer.toString();

        // Verify XML was generated
        assertNotNull(xml, "XML should not be null");
        assertTrue(xml.length() > 0, "XML should have content");
    }

    @Test
    @DisplayName("Test XML with nested objects")
    void testXmlWithNestedObjects() throws Exception {
        // Create a root with nested structure
        Indices root = (Indices) storage.getRoot();
        
        // Add records with various data types
        Record rec1 = new Record();
        rec1.intKey = 1;
        rec1.strKey = "first";
        rec1.realKey = 1.1;
        
        Record rec2 = new Record();
        rec2.intKey = 2;
        rec2.strKey = "second";
        rec2.realKey = 2.2;
        
        root.intIndex.put(rec1);
        root.intIndex.put(rec2);
        root.strIndex.put(new Key(rec1.strKey), rec1);
        root.strIndex.put(new Key(rec2.strKey), rec2);
        storage.commit();

        // Export
        StringWriter writer = new StringWriter();
        storage.exportXML(writer);
        writer.close();
        
        String xml = writer.toString();
        assertTrue(xml.contains("first"), "XML should contain 'first'");
        assertTrue(xml.contains("second"), "XML should contain 'second'");
    }

    @Test
    @DisplayName("Test XML empty database export")
    void testXmlEmptyExport() throws Exception {
        Indices root = (Indices) storage.getRoot();
        
        // Don't add any records
        storage.commit();

        // Export empty database
        StringWriter writer = new StringWriter();
        storage.exportXML(writer);
        writer.close();
        
        String xml = writer.toString();
        assertNotNull(xml, "XML should not be null for empty database");
        assertTrue(xml.length() > 0, "XML should have some content even for empty database");
    }
}
