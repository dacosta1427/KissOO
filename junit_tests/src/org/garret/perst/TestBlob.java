package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestBlob.java
 * Tests BLOB storage functionality of Perst.
 */
class TestBlob {

    private Storage storage;
    private static final String TEST_DB = "testblob.dbs";

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
    }

    @Test
    @DisplayName("Test create and retrieve BLOB")
    void testCreateAndRetrieveBlob() throws Exception {
        // Create index and root
        Index root = storage.createIndex(String.class, true);
        storage.setRoot(root);

        // Create a simple BLOB
        String testContent = "Hello, Perst BLOB!";
        byte[] contentBytes = testContent.getBytes("UTF-8");

        Blob blob = storage.createBlob();
        OutputStream out = blob.getOutputStream(false);
        out.write(contentBytes);
        out.close();

        // Store in index
        root.put("test.txt", blob);
        storage.commit();

        // Retrieve and verify
        Blob retrieved = (Blob) root.get("test.txt");
        assertNotNull(retrieved, "BLOB should be retrieved");

        InputStream in = retrieved.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        in.close();

        assertEquals(testContent, baos.toString("UTF-8"), "BLOB content should match");
    }

    @Test
    @DisplayName("Test BLOB size")
    void testBlobSize() throws Exception {
        Index root = storage.createIndex(String.class, true);
        storage.setRoot(root);

        // Create BLOB with known size
        byte[] content = new byte[5000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Blob blob = storage.createBlob();
        OutputStream out = blob.getOutputStream(false);
        out.write(content);
        out.close();

        root.put("data.bin", blob);
        storage.commit();

        // Verify size
        Blob retrieved = (Blob) root.get("data.bin");
        assertEquals(5000, retrieved.getInputStream().available(), "BLOB size should match");
    }

    @Test
    @DisplayName("Test multiple BLOBs")
    void testMultipleBlobs() throws Exception {
        Index root = storage.createIndex(String.class, true);
        storage.setRoot(root);

        // Create multiple BLOBs
        for (int i = 0; i < 10; i++) {
            String content = "BLOB number " + i;
            Blob blob = storage.createBlob();
            OutputStream out = blob.getOutputStream(false);
            out.write(content.getBytes("UTF-8"));
            out.close();
            root.put("blob" + i + ".txt", blob);
        }
        storage.commit();

        // Verify all BLOBs
        for (int i = 0; i < 10; i++) {
            Blob retrieved = (Blob) root.get("blob" + i + ".txt");
            assertNotNull(retrieved, "BLOB " + i + " should exist");

            InputStream in = retrieved.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            in.close();

            assertEquals("BLOB number " + i, baos.toString("UTF-8"), "Content should match");
        }
    }

    @Test
    @DisplayName("Test BLOB not found")
    void testBlobNotFound() throws Exception {
        Index root = storage.createIndex(String.class, true);
        storage.setRoot(root);

        Blob retrieved = (Blob) root.get("nonexistent.txt");
        assertNull(retrieved, "Non-existent BLOB should return null");
    }

    @Test
    @DisplayName("Test BLOB with large content")
    void testBlobLargeContent() throws Exception {
        Index root = storage.createIndex(String.class, true);
        storage.setRoot(root);

        // Create larger content (100KB)
        byte[] content = new byte[100 * 1024];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Blob blob = storage.createBlob();
        OutputStream out = blob.getOutputStream(false);
        out.write(content);
        out.close();

        root.put("large.bin", blob);
        storage.commit();

        // Verify
        Blob retrieved = (Blob) root.get("large.bin");
        assertNotNull(retrieved, "Large BLOB should be retrieved");
        assertEquals(100 * 1024, retrieved.getInputStream().available(), "Large BLOB size should match");
    }

    @Test
    @DisplayName("Test update existing BLOB")
    void testUpdateExistingBlob() throws Exception {
        Index root = storage.createIndex(String.class, true);
        storage.setRoot(root);

        // Create initial BLOB
        Blob blob = storage.createBlob();
        OutputStream out = blob.getOutputStream(false);
        out.write("Initial content".getBytes("UTF-8"));
        out.close();
        root.put("update.txt", blob);
        storage.commit();

        // Update BLOB
        blob = (Blob) root.get("update.txt");
        out = blob.getOutputStream(true); // append mode
        out.write(" - appended".getBytes("UTF-8"));
        out.close();
        storage.commit();

        // Verify update
        Blob retrieved = (Blob) root.get("update.txt");
        InputStream in = retrieved.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        in.close();

        assertEquals("Initial content - appended", baos.toString("UTF-8"), "Updated content should match");
    }
}
