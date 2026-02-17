package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestAlloc.java
 * Tests memory allocation and BLOB functionality
 */
class TestAlloc {

    private Storage storage;
    private Index index;
    private static final String TEST_DB = "testalloc.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB);
        index = storage.createIndex(String.class, true);
        storage.setRoot(index);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test BLOB creation and storage")
    void testBlobCreationAndStorage() throws Exception {
        // Create test data
        String testContent = "Test content for BLOB allocation";
        byte[] testData = testContent.getBytes();

        // Create and store BLOB
        Blob blob = storage.createBlob();
        OutputStream out = blob.getOutputStream(false);
        out.write(testData);
        out.close();

        index.put("test1.txt", blob);
        storage.commit();

        // Retrieve and verify
        Object retrieved = index.get("test1.txt");
        assertNotNull(retrieved, "Should retrieve BLOB from index");

        InputStream in = ((Blob)retrieved).getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int rc;
        while ((rc = in.read(buffer)) > 0) {
            baos.write(buffer, 0, rc);
        }
        in.close();

        assertEquals(testContent, new String(baos.toByteArray()), "Content should match");
    }

    @Test
    @DisplayName("Test BLOB iterator and removal")
    void testBlobIteratorAndRemoval() throws Exception {
        // Create multiple BLOBs
        for (int i = 0; i < 5; i++) {
            Blob blob = storage.createBlob();
            String content = "Test content " + i;
            OutputStream out = blob.getOutputStream(false);
            out.write(content.getBytes());
            out.close();
            index.put("test" + i + ".txt", blob);
        }
        storage.commit();

        // Iterate and remove - use raw iterator
        Iterator iter = index.iterator(null, null, Index.ASCENT_ORDER);
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
            count += 1;
        }

        assertEquals(5, count, "Should have iterated through all 5 BLOBs");
        assertFalse(index.iterator().hasNext(), "Index should be empty after removal");
    }

    @Test
    @DisplayName("Test multiple BLOB operations")
    void testMultipleBlobOperations() throws Exception {
        // Create multiple BLOBs with different sizes
        for (int i = 0; i < 3; i++) {
            byte[] data = new byte[1024 * (i + 1)];
            for (int j = 0; j < data.length; j++) {
                data[j] = (byte) (j % 256);
            }

            Blob blob = storage.createBlob();
            OutputStream out = blob.getOutputStream(false);
            out.write(data);
            out.close();

            index.put("large" + i + ".dat", blob);
        }
        storage.commit();

        // Verify each BLOB
        for (int i = 0; i < 3; i++) {
            Object obj = index.get("large" + i + ".dat");
            assertNotNull(obj, "Should retrieve BLOB " + i);
        }
    }

    @Test
    @DisplayName("Test BLOB cleanup")
    void testBlobCleanup() throws Exception {
        // Add some BLOBs
        for (int i = 0; i < 3; i++) {
            Blob blob = storage.createBlob();
            String content = "Content " + i;
            OutputStream out = blob.getOutputStream(false);
            out.write(content.getBytes());
            out.close();
            index.put("file" + i + ".txt", blob);
        }
        storage.commit();

        // Clear the index
        index.clear();

        assertEquals(0, index.size(), "Index should be empty after clear");
    }
}
