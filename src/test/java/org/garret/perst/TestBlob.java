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

    static class Image extends Persistent { 
        Blob body;
    }

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
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create a simple BLOB
        String testContent = "Hello, Perst BLOB!";
        byte[] contentBytes = testContent.getBytes("UTF-8");

        Image image = new Image();
        image.body = storage.createBlob();
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write(contentBytes);
        }

        // Store in index
        root.put("test.txt", image);
        storage.commit();

        // Retrieve and verify
        Image retrieved = root.get("test.txt");
        assertNotNull(retrieved, "Image should be retrieved");
        assertNotNull(retrieved.body, "BLOB should exist");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = retrieved.body.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }

        assertEquals(testContent, baos.toString("UTF-8"), "BLOB content should match");
    }

    @Test
    @DisplayName("Test BLOB size")
    void testBlobSize() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create BLOB with known size
        byte[] content = new byte[5000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Image image = new Image();
        image.body = storage.createBlob();
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write(content);
        }

        root.put("data.bin", image);
        storage.commit();

        // Verify size - wait, available() is not guaranteed to return total size for all streams, 
        // but for Perst BlobInputStream implementation it likely returns remaining bytes.
        // Let's read it to be sure.
        Image retrieved = root.get("data.bin");
        int size = 0;
        try (InputStream in = retrieved.body.getInputStream()) {
             while (in.read() != -1) {
                 size++;
             }
        }
        assertEquals(5000, size, "BLOB size should match");
    }

    @Test
    @DisplayName("Test multiple BLOBs")
    void testMultipleBlobs() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create multiple BLOBs
        for (int i = 0; i < 10; i++) {
            String content = "BLOB number " + i;
            Image image = new Image();
            image.body = storage.createBlob();
            try (OutputStream out = image.body.getOutputStream(false)) {
                out.write(content.getBytes("UTF-8"));
            }
            root.put("blob" + i + ".txt", image);
        }
        storage.commit();

        // Verify all BLOBs
        for (int i = 0; i < 10; i++) {
            Image retrieved = root.get("blob" + i + ".txt");
            assertNotNull(retrieved, "Image " + i + " should exist");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream in = retrieved.body.getInputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
            }

            assertEquals("BLOB number " + i, baos.toString("UTF-8"), "Content should match for blob " + i);
        }
    }

    @Test
    @DisplayName("Test BLOB not found")
    void testBlobNotFound() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        Image retrieved = root.get("nonexistent.txt");
        assertNull(retrieved, "Non-existent BLOB should return null");
    }

    @Test
    @DisplayName("Test BLOB append")
    void testBlobAppend() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create BLOB with initial content
        Image image = new Image();
        image.body = storage.createBlob();
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write("Initial ".getBytes("UTF-8"));
        }
        
        // Append more content
        try (OutputStream out = image.body.getOutputStream(true)) {
            out.write("Appended".getBytes("UTF-8"));
        }
        
        root.put("append.txt", image);
        storage.commit();

        // Verify combined content
        Image retrieved = root.get("append.txt");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = retrieved.body.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }

        assertEquals("Initial Appended", baos.toString("UTF-8"), "BLOB should have appended content");
    }

    @Test
    @DisplayName("Test BLOB large data")
    void testBlobLargeData() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create a large BLOB (1MB)
        byte[] content = new byte[1024 * 1024];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        Image image = new Image();
        image.body = storage.createBlob();
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write(content);
        }

        root.put("large.bin", image);
        storage.commit();

        // Verify content
        Image retrieved = root.get("large.bin");
        byte[] readContent = new byte[content.length];
        try (InputStream in = retrieved.body.getInputStream()) {
            int totalRead = 0;
            while (totalRead < content.length) {
                int bytesRead = in.read(readContent, totalRead, content.length - totalRead);
                if (bytesRead == -1) break;
                totalRead += bytesRead;
            }
        }

        assertArrayEquals(content, readContent, "Large BLOB content should match");
    }

    @Test
    @DisplayName("Test BLOB empty")
    void testBlobEmpty() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create empty BLOB
        Image image = new Image();
        image.body = storage.createBlob();
        // Don't write anything

        root.put("empty.txt", image);
        storage.commit();

        // Verify empty BLOB
        Image retrieved = root.get("empty.txt");
        try (InputStream in = retrieved.body.getInputStream()) {
            assertEquals(-1, in.read(), "Empty BLOB should return -1 on read");
        }
    }

    @Test
    @DisplayName("Test random access BLOB")
    void testRandomAccessBlob() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create random access BLOB
        Image image = new Image();
        image.body = storage.createRandomAccessBlob();
        
        // Write some data
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write("0123456789".getBytes("UTF-8"));
        }

        root.put("random.bin", image);
        storage.commit();

        // Verify content
        Image retrieved = root.get("random.bin");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = retrieved.body.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }

        assertEquals("0123456789", baos.toString("UTF-8"), "Random access BLOB content should match");
    }

    @Test
    @DisplayName("Test BLOB deletion")
    void testBlobDeletion() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create BLOB
        Image image = new Image();
        image.body = storage.createBlob();
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write("To be deleted".getBytes("UTF-8"));
        }

        root.put("delete.txt", image);
        storage.commit();

        // Delete
        Image toDelete = root.remove("delete.txt");
        assertNotNull(toDelete, "Should retrieve BLOB for deletion");
        toDelete.deallocate();
        storage.commit();

        // Verify deleted
        Image retrieved = root.get("delete.txt");
        assertNull(retrieved, "Deleted BLOB should not be found");
    }

    @Test
    @DisplayName("Test BLOB with binary data")
    void testBlobBinaryData() throws Exception {
        Index<Image> root = storage.<Image>createIndex(String.class, true);
        storage.setRoot(root);

        // Create BLOB with all byte values
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) i;
        }

        Image image = new Image();
        image.body = storage.createBlob();
        try (OutputStream out = image.body.getOutputStream(false)) {
            out.write(allBytes);
        }

        root.put("binary.bin", image);
        storage.commit();

        // Verify all bytes
        Image retrieved = root.get("binary.bin");
        byte[] readBytes = new byte[256];
        try (InputStream in = retrieved.body.getInputStream()) {
            int totalRead = 0;
            while (totalRead < 256) {
                int bytesRead = in.read(readBytes, totalRead, 256 - totalRead);
                if (bytesRead == -1) break;
                totalRead += bytesRead;
            }
        }

        assertArrayEquals(allBytes, readBytes, "Binary BLOB content should match");
    }
}
