package org.garret.perst;

import org.garret.perst.fulltext.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestFullTextIndex.java
 * Tests FullTextIndex (Lucene integration) functionality.
 */
class TestFullTextIndex {

    static class Doc extends Persistent implements FullTextSearchable {
        String title;
        String content;

        public Doc() {
        }

        public Doc(Storage storage, String title, String content) {
            super(storage);
            this.title = title;
            this.content = content;
        }

        public Reader getText() {
            return new StringReader(title + " " + content);
        }

        public String getLanguage() {
            return "en";
        }
    }

    static class Root extends Persistent {
        FullTextIndex index;
        FieldIndex titleIndex;

        Root(Storage storage) {
            super(storage);
            this.index = storage.createFullTextIndex();
            this.titleIndex = storage.createFieldIndex(Doc.class, "title", true);
        }

        Root() {
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testfulltext.dbs";
    private static final int SEARCH_TIME_LIMIT = 5000; // 5 seconds

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
    @DisplayName("Test full text index create and add documents")
    void testFullTextIndexCreateAndAdd() {
        Root root = new Root(storage);
        storage.setRoot(root);

        // Add documents
        Doc doc1 = new Doc(storage, "Document One", "This is the first document about Perst database");
        Doc doc2 = new Doc(storage, "Document Two", "This is the second document about Java persistence");
        Doc doc3 = new Doc(storage, "Document Three", "This is the third document about object databases");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);

        storage.commit();

        assertEquals(3, root.index.getNumberOfDocuments(), "Should have 3 documents");
        assertTrue(root.index.getNumberOfWords() > 0, "Should have indexed words");
    }

    @Test
    @DisplayName("Test full text search single term")
    void testFullTextSearchSingleTerm() {
        Root root = new Root(storage);
        storage.setRoot(root);

        // Add documents with known content
        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming language");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming language");
        Doc doc3 = new Doc(storage, "Database Book", "Learn about database systems");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);

        storage.commit();

        // Search for "Java"
        FullTextSearchResult result = root.index.search("Java", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length > 0, "Should find documents with Java");
        assertTrue(result.estimation > 0, "Should have estimation");
    }

    @Test
    @DisplayName("Test full text search AND operator")
    void testFullTextSearchAnd() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Database", "Learn Java and database together");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");
        Doc doc3 = new Doc(storage, "Java Tutorial", "Learn Java programming");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);

        storage.commit();

        // Search with AND
        FullTextSearchResult result = root.index.search("Java AND database", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length >= 1, "Should find documents with both Java and database");
    }

    @Test
    @DisplayName("Test full text search OR operator")
    void testFullTextSearchOr() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");
        Doc doc3 = new Doc(storage, "C++ Manual", "Learn C++ programming");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);

        storage.commit();

        // Search with OR
        FullTextSearchResult result = root.index.search("Java OR Python", "en", 10, SEARCH_TIME_LIMIT);

        assertEquals(2, result.hits.length, "Should find documents with Java or Python");
    }

    @Test
    @DisplayName("Test full text search NOT operator")
    void testFullTextSearchNot() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");
        Doc doc3 = new Doc(storage, "Database Book", "Learn about database");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);

        storage.commit();

        // Search with NOT
        FullTextSearchResult result = root.index.search("Java NOT Python", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length >= 1, "Should find documents with Java but not Python");
    }

    @Test
    @DisplayName("Test full text search phrase")
    void testFullTextSearchPhrase() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Tutorial One", "Learn Java programming step by step");
        Doc doc2 = new Doc(storage, "Tutorial Two", "Step by step guide to Python");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);

        storage.commit();

        // Search phrase
        FullTextSearchResult result = root.index.search("\"step by step\"", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length > 0, "Should find phrase");
    }

    @Test
    @DisplayName("Test full text search with limit")
    void testFullTextSearchWithLimit() {
        Root root = new Root(storage);
        storage.setRoot(root);

        // Add multiple documents with same content
        for (int i = 0; i < 20; i++) {
            Doc doc = new Doc(storage, "Document " + i, "Learn Java programming");
            root.titleIndex.put(doc);
            root.index.add(doc);
        }

        storage.commit();

        // Search with limit
        FullTextSearchResult result = root.index.search("Java", "en", 5, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length <= 5, "Should respect limit");
    }

    @Test
    @DisplayName("Test full text search no results")
    void testFullTextSearchNoResults() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);

        storage.commit();

        // Search for non-existent term
        FullTextSearchResult result = root.index.search("Ruby", "en", 10, SEARCH_TIME_LIMIT);

        assertEquals(0, result.hits.length, "Should find no results");
        assertEquals(0, result.estimation, "Estimation should be 0");
    }

    @Test
    @DisplayName("Test full text index remove document")
    void testFullTextIndexRemoveDocument() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);

        storage.commit();

        assertEquals(2, root.index.getNumberOfDocuments(), "Should have 2 documents");

        // Clear index - remove all documents
        root.index.clear();
        storage.commit();

        assertEquals(0, root.index.getNumberOfDocuments(), "Should have 0 documents after clear");
    }

    @Test
    @DisplayName("Test full text index with zero limit returns no hits")
    void testFullTextSearchZeroLimit() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);

        storage.commit();

        // Search with zero limit
        FullTextSearchResult result = root.index.search("Java", "en", 0, 0);

        assertEquals(0, result.hits.length, "Should return no hits with zero limit");
        assertTrue(result.estimation > 0, "Should still have estimation");
    }

    @Test
    @DisplayName("Test full text search with multiple terms")
    void testFullTextSearchMultipleTerms() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Programming", "Java programming basics");
        Doc doc2 = new Doc(storage, "Developer", "Python developer guide");
        Doc doc3 = new Doc(storage, "Coding", "Learn to code");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);

        storage.commit();

        // Search with multiple terms (space = AND)
        FullTextSearchResult result = root.index.search("Java programming", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length >= 1, "Should find documents with Java and programming");
    }

    @Test
    @DisplayName("Test full text search ranking")
    void testFullTextSearchRanking() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Guide", "Java Java Java programming"); // Java appears 3 times
        Doc doc2 = new Doc(storage, "Python Book", "Learn Java basics"); // Java appears once

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);

        storage.commit();

        FullTextSearchResult result = root.index.search("Java", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result.hits.length > 0, "Should find results");
        // First result should be the one with more occurrences (higher rank)
        Doc firstDoc = (Doc) result.hits[0].getDocument();
        assertEquals("Java Guide", firstDoc.title, "Higher ranked document should come first");
    }

    @Test
    @DisplayName("Test full text index search multiple times")
    void testFullTextIndexMultipleSearches() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        root.titleIndex.put(doc1);
        root.index.add(doc1);
        storage.commit();

        // Perform multiple searches
        FullTextSearchResult result1 = root.index.search("Java", "en", 10, SEARCH_TIME_LIMIT);
        FullTextSearchResult result2 = root.index.search("programming", "en", 10, SEARCH_TIME_LIMIT);
        FullTextSearchResult result3 = root.index.search("Tutorial", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result1.hits.length > 0, "Should find Java");
        assertTrue(result2.hits.length > 0, "Should find programming");
        assertTrue(result3.hits.length > 0, "Should find Tutorial");
    }

    @Test
    @DisplayName("Test full text search case insensitive")
    void testFullTextSearchCaseInsensitive() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Tutorial", "Learn JAVA programming");
        
        root.titleIndex.put(doc1);
        root.index.add(doc1);
        storage.commit();

        // Search with different case
        FullTextSearchResult result1 = root.index.search("java", "en", 10, SEARCH_TIME_LIMIT);
        FullTextSearchResult result2 = root.index.search("JAVA", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result1.hits.length > 0, "Should find with lowercase");
        assertTrue(result2.hits.length > 0, "Should find with uppercase");
    }

    @Test
    @DisplayName("Test full text search estimation accuracy")
    void testFullTextSearchEstimation() {
        Root root = new Root(storage);
        storage.setRoot(root);

        // Add documents
        for (int i = 0; i < 10; i++) {
            Doc doc = new Doc(storage, "Doc " + i, "Java programming " + i);
            root.titleIndex.put(doc);
            root.index.add(doc);
        }
        storage.commit();

        FullTextSearchResult result = root.index.search("Java", "en", 100, SEARCH_TIME_LIMIT);

        // Estimation should match hits when limit >= estimation
        assertEquals(result.estimation, result.hits.length, 
            "Estimation should equal hits when limit >= estimation");
    }

    @Test
    @DisplayName("Test full text index clear")
    void testFullTextIndexClear() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);

        storage.commit();

        assertEquals(2, root.index.getNumberOfDocuments());

        // Clear the index
        root.index.clear();
        storage.commit();

        assertEquals(0, root.index.getNumberOfDocuments(), "Index should be empty after clear");
    }
}
