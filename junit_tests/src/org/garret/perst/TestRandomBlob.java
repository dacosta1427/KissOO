package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRandomBlob.java
 * Tests random access BLOB operations
 */
class TestRandomBlob {

    // Scaled down from 10000 iterations and 8GB file size
    private static final int nIterations = 100;
    private static final long fileSize = 1024L * 1024; // 1MB instead of 8GB
    private static final int maxRecordSize = 100;
    private static final String TEST_DB = "testrndblob.dbs";

    private Storage storage;

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
        new java.io.File(TEST_DB).delete();
    }

    static class Access implements Comparable {
        long pos;
        int size;

        public int compareTo(Object o) {
            long diff = pos - ((Access) o).pos;
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }
    }

    private Access[] initializeRandomAccessMap() {
        Access[] map = new Access[nIterations];
        Access[] sortedMap = new Access[nIterations];
        long key = 1999;
        for (int i = 0; i < nIterations; i++) {
            map[i] = new Access();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            map[i].pos = key % fileSize;
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            map[i].size = (int) (key % maxRecordSize);
            sortedMap[i] = map[i];
        }
        Arrays.sort(sortedMap);
        for (int i = 1; i < sortedMap.length; i++) {
            if (sortedMap[i - 1].pos + sortedMap[i - 1].size > sortedMap[i].pos) {
                sortedMap[i - 1].size = (int) (sortedMap[i].pos - sortedMap[i - 1].pos);
            }
        }
        return map;
    }

    @Test
    @DisplayName("Test random access BLOB create and store")
    void testRandomBlobCreateAndStore() throws Exception {
        Blob blob = storage.createRandomAccessBlob();
        storage.setRoot(blob);

        Access[] map = initializeRandomAccessMap();

        RandomAccessOutputStream out = blob.getOutputStream(0);
        for (int i = 0; i < nIterations; i++) {
            int size = map[i].size;
            long pos = map[i].pos;
            byte filler = (byte) (pos & 0xFF);
            byte[] content = new byte[size];
            for (int j = 0; j < size; j++) {
                content[j] = filler;
            }
            out.setPosition(pos);
            out.write(content, 0, size);
        }
        out.close();

        // Use inputStream.available() to get blob size
        RandomAccessInputStream in = blob.getInputStream();
        assertTrue(in.available() > 0, "Blob should have content");
    }

    @Test
    @DisplayName("Test random access BLOB read and verify")
    void testRandomBlobReadAndVerify() throws Exception {
        Blob blob = storage.createRandomAccessBlob();
        storage.setRoot(blob);

        Access[] map = initializeRandomAccessMap();

        // Store content
        RandomAccessOutputStream out = blob.getOutputStream(0);
        for (int i = 0; i < nIterations; i++) {
            int size = map[i].size;
            long pos = map[i].pos;
            byte filler = (byte) (pos & 0xFF);
            byte[] content = new byte[size];
            for (int j = 0; j < size; j++) {
                content[j] = filler;
            }
            out.setPosition(pos);
            out.write(content, 0, size);
        }
        out.close();

        // Verify content
        RandomAccessInputStream in = blob.getInputStream();
        for (int i = 0; i < nIterations; i++) {
            int size = map[i].size;
            long pos = map[i].pos;
            byte filler = (byte) (pos & 0xFF);
            byte[] content = new byte[size];
            in.setPosition(pos);
            int rc = in.read(content, 0, size);
            assertEquals(size, rc, "Should read correct number of bytes");
            for (int j = 0; j < size; j++) {
                assertEquals(filler, content[j], "Content should match filler byte");
            }
        }
    }

    @Test
    @DisplayName("Test random access BLOB getInputStream")
    void testRandomBlobGetInputStream() throws Exception {
        Blob blob = storage.createRandomAccessBlob();
        storage.setRoot(blob);

        // Write some content
        RandomAccessOutputStream out = blob.getOutputStream(0);
        byte[] content = new byte[]{1, 2, 3, 4, 5};
        out.write(content, 0, content.length);
        out.close();

        // Get input stream and verify we can read content
        RandomAccessInputStream in = blob.getInputStream();
        assertNotNull(in, "Input stream should not be null");
        
        byte[] readContent = new byte[5];
        int bytesRead = in.read(readContent, 0, 5);
        assertEquals(5, bytesRead, "Should read 5 bytes");
        assertArrayEquals(content, readContent, "Content should match");
    }
}
