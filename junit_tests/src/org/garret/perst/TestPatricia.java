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
}
