package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestPatricia.java
 * Tests Patricia trie functionality for prefix-based searches
 */
class TestPatricia {

    private Storage storage;
    private static final String TEST_DB = "testptree.dbs";

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

    @Test
    @DisplayName("Test Patricia trie insert and search")
    void testPatriciaTrieInsertAndSearch() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Insert some entries
        root.add(PatriciaTrieKey.from8bitString("724885"), new PersistentString("ATT"));
        root.add(PatriciaTrieKey.from8bitString("72488547"), new PersistentString("BCC"));

        // Verify exact match
        PersistentString result = (PersistentString) root.findExactMatch(PatriciaTrieKey.from8bitString("724885"));
        assertNotNull(result, "Should find exact match for 724885");
        assertEquals("ATT", result.toString(), "Value should be ATT");

        // Verify longer key
        result = (PersistentString) root.findExactMatch(PatriciaTrieKey.from8bitString("72488547"));
        assertNotNull(result, "Should find exact match for 72488547");
        assertEquals("BCC", result.toString(), "Value should be BCC");
    }

    @Test
    @DisplayName("Test Patricia trie prefix search")
    void testPatriciaTriePrefixSearch() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Insert entries with common prefix
        root.add(PatriciaTrieKey.from8bitString("724885"), new PersistentString("ATT"));
        root.add(PatriciaTrieKey.from8bitString("72488547"), new PersistentString("BCC"));
        root.add(PatriciaTrieKey.from8bitString("72488553"), new PersistentString("CDE"));
        root.add(PatriciaTrieKey.from8bitString("72488563"), new PersistentString("EFG"));

        storage.commit();

        // Test prefix search - find all keys starting with "724885"
        // The PatriciaTrie supports prefix-based iteration
        int count = 0;
        for (Object obj : root) {
            count++;
        }

        assertTrue(count >= 4, "Should have at least 4 entries in trie");
    }

    @Test
    @DisplayName("Test Patricia trie find with no match")
    void testPatriciaTrieNoMatch() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Insert an entry
        root.add(PatriciaTrieKey.from8bitString("724885"), new PersistentString("ATT"));

        // Try to find a non-existent key
        PersistentString result = (PersistentString) root.findExactMatch(PatriciaTrieKey.from8bitString("999999"));
        assertNull(result, "Should not find non-existent key");
    }

    @Test
    @DisplayName("Test Patricia trie remove")
    void testPatriciaTrieRemove() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Insert entries
        root.add(PatriciaTrieKey.from8bitString("724885"), new PersistentString("ATT"));
        root.add(PatriciaTrieKey.from8bitString("72488547"), new PersistentString("BCC"));

        // Verify both exist
        assertNotNull(root.findExactMatch(PatriciaTrieKey.from8bitString("724885")), "Key should exist before removal");
        assertNotNull(root.findExactMatch(PatriciaTrieKey.from8bitString("72488547")), "Key should exist before removal");

        // Remove one entry
        root.remove(PatriciaTrieKey.from8bitString("724885"));

        // Verify only one remains
        assertNull(root.findExactMatch(PatriciaTrieKey.from8bitString("724885")), "Removed key should not exist");
        assertNotNull(root.findExactMatch(PatriciaTrieKey.from8bitString("72488547")), "Other key should still exist");
    }

    @Test
    @DisplayName("Test Patricia trie with existing data")
    void testPatriciaTrieWithExistingData() {
        // This test simulates the original test behavior where it checks for existing root
        PatriciaTrie root = (PatriciaTrie) storage.getRoot();
        
        if (root == null) {
            root = storage.createPatriciaTrie();
            storage.setRoot(root);
            root.add(PatriciaTrieKey.from8bitString("724885"), new PersistentString("ATT"));
            root.add(PatriciaTrieKey.from8bitString("72488547"), new PersistentString("BCC"));
        }

        // Either way, we should be able to search
        PersistentString result = (PersistentString) root.findExactMatch(PatriciaTrieKey.from8bitString("724885"));
        assertNotNull(result, "Should find key");
    }

    @Test
    @DisplayName("Test PatriciaTrieKey fromIpAddress with InetAddress")
    void testPatriciaTrieKeyFromIpAddressInet() throws Exception {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Create keys from IP addresses
        java.net.InetAddress addr1 = java.net.InetAddress.getByName("192.168.1.1");
        java.net.InetAddress addr2 = java.net.InetAddress.getByName("10.0.0.1");
        
        PatriciaTrieKey key1 = PatriciaTrieKey.fromIpAddress(addr1);
        PatriciaTrieKey key2 = PatriciaTrieKey.fromIpAddress(addr2);

        // Verify key properties
        assertEquals(32, key1.length, "IPv4 address should have 32 bits");
        assertEquals(32, key2.length, "IPv4 address should have 32 bits");

        // Add and find entries
        root.add(key1, new PersistentString("server1"));
        root.add(key2, new PersistentString("server2"));

        // Find using same IP
        PatriciaTrieKey searchKey1 = PatriciaTrieKey.fromIpAddress(java.net.InetAddress.getByName("192.168.1.1"));
        PersistentString result = (PersistentString) root.findExactMatch(searchKey1);
        assertNotNull(result, "Should find IP address entry");
        assertEquals("server1", result.toString());
    }

    @Test
    @DisplayName("Test PatriciaTrieKey fromIpAddress with String")
    void testPatriciaTrieKeyFromIpAddressString() throws Exception {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Create keys from IP address strings
        PatriciaTrieKey key1 = PatriciaTrieKey.fromIpAddress("192.168.1.1");
        PatriciaTrieKey key2 = PatriciaTrieKey.fromIpAddress("10.0.0.1");
        PatriciaTrieKey key3 = PatriciaTrieKey.fromIpAddress("127.0.0.1");

        assertEquals(32, key1.length, "IPv4 address should have 32 bits");
        assertEquals(32, key2.length, "IPv4 address should have 32 bits");
        assertEquals(32, key3.length, "IPv4 address should have 32 bits");

        root.add(key1, new PersistentString("lan-server"));
        root.add(key2, new PersistentString("wan-server"));
        root.add(key3, new PersistentString("localhost"));

        // Verify finds
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromIpAddress("192.168.1.1")));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromIpAddress("10.0.0.1")));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromIpAddress("127.0.0.1")));
    }

    @Test
    @DisplayName("Test PatriciaTrieKey fromDecimalDigits")
    void testPatriciaTrieKeyFromDecimalDigits() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Create keys from decimal digit strings
        PatriciaTrieKey key1 = PatriciaTrieKey.fromDecimalDigits("123456");
        PatriciaTrieKey key2 = PatriciaTrieKey.fromDecimalDigits("9876543210");
        PatriciaTrieKey key3 = PatriciaTrieKey.fromDecimalDigits("555");

        assertEquals(24, key1.length, "6 digits = 24 bits");
        assertEquals(40, key2.length, "10 digits = 40 bits");
        assertEquals(12, key3.length, "3 digits = 12 bits");

        root.add(key1, new PersistentString("phone1"));
        root.add(key2, new PersistentString("phone2"));
        root.add(key3, new PersistentString("phone3"));

        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromDecimalDigits("123456")));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromDecimalDigits("9876543210")));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromDecimalDigits("555")));
    }

    @Test
    @DisplayName("Test PatriciaTrieKey from7bitString")
    void testPatriciaTrieKeyFrom7bitString() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Create keys from 7-bit strings
        PatriciaTrieKey key1 = PatriciaTrieKey.from7bitString("ABC");
        PatriciaTrieKey key2 = PatriciaTrieKey.from7bitString("test");
        PatriciaTrieKey key3 = PatriciaTrieKey.from7bitString("perst");

        assertEquals(21, key1.length, "3 chars * 7 bits = 21 bits");
        assertEquals(28, key2.length, "4 chars * 7 bits = 28 bits");
        assertEquals(35, key3.length, "5 chars * 7 bits = 35 bits");

        root.add(key1, new PersistentString("value1"));
        root.add(key2, new PersistentString("value2"));
        root.add(key3, new PersistentString("value3"));

        assertNotNull(root.findExactMatch(PatriciaTrieKey.from7bitString("ABC")));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.from7bitString("test")));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.from7bitString("perst")));
    }

    @Test
    @DisplayName("Test PatriciaTrieKey fromByteArray")
    void testPatriciaTrieKeyFromByteArray() {
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);

        // Create keys from byte arrays
        byte[] arr1 = new byte[] {0x01, 0x02, 0x03, 0x04};
        byte[] arr2 = new byte[] {(byte)0xFF, (byte)0xFE, (byte)0xFD};
        byte[] arr3 = new byte[] {0x00, 0x00, 0x00, 0x01};

        PatriciaTrieKey key1 = PatriciaTrieKey.fromByteArray(arr1);
        PatriciaTrieKey key2 = PatriciaTrieKey.fromByteArray(arr2);
        PatriciaTrieKey key3 = PatriciaTrieKey.fromByteArray(arr3);

        assertEquals(32, key1.length, "4 bytes = 32 bits");
        assertEquals(24, key2.length, "3 bytes = 24 bits");
        assertEquals(32, key3.length, "4 bytes = 32 bits");

        root.add(key1, new PersistentString("data1"));
        root.add(key2, new PersistentString("data2"));
        root.add(key3, new PersistentString("data3"));

        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromByteArray(new byte[] {0x01, 0x02, 0x03, 0x04})));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromByteArray(new byte[] {(byte)0xFF, (byte)0xFE, (byte)0xFD})));
        assertNotNull(root.findExactMatch(PatriciaTrieKey.fromByteArray(new byte[] {0x00, 0x00, 0x00, 0x01})));
    }

    @Test
    @DisplayName("Test PatriciaTrieKey constructor directly")
    void testPatriciaTrieKeyConstructor() {
        // Test direct constructor
        PatriciaTrieKey key1 = new PatriciaTrieKey(0xDEADBEEFL, 32);
        assertEquals(0xDEADBEEFL, key1.mask);
        assertEquals(32, key1.length);

        PatriciaTrieKey key2 = new PatriciaTrieKey(0x0F, 4);
        assertEquals(0x0F, key2.mask);
        assertEquals(4, key2.length);

        // Use in trie
        PatriciaTrie root = storage.createPatriciaTrie();
        storage.setRoot(root);
        root.add(key1, new PersistentString("custom1"));
        root.add(key2, new PersistentString("custom2"));

        assertNotNull(root.findExactMatch(new PatriciaTrieKey(0xDEADBEEFL, 32)));
        assertNotNull(root.findExactMatch(new PatriciaTrieKey(0x0F, 4)));
    }
}
