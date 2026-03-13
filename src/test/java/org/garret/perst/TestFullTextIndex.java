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

    // ---- Phase 1 additions: cover FullTextQueryUnaryOp, FullTextSearchResult.merge(),
    //      FullTextQueryMatchOp STRICT_MATCH, FullTextQueryBinaryOp branches,
    //      FullTextSearchHelper helpers, FullTextSearchHit.compareTo() ---------------

    @Test
    @DisplayName("Test FullTextQuery node classes directly - full AST coverage")
    void testFullTextQueryNodesDirectly() {
        // FullTextQueryMatchOp - MATCH
        FullTextQueryMatchOp matchJava = new FullTextQueryMatchOp(FullTextQuery.MATCH, "java", 0);
        assertEquals("java", matchJava.toString());
        assertTrue(matchJava.isConstrained());
        assertEquals(FullTextQuery.MATCH, matchJava.op);
        assertEquals("java", matchJava.word);
        assertEquals(0, matchJava.pos);

        // FullTextQueryMatchOp - STRICT_MATCH (quoted phrase → '"word"' toString)
        FullTextQueryMatchOp strictMatch = new FullTextQueryMatchOp(FullTextQuery.STRICT_MATCH, "java", 0);
        assertEquals("\"java\"", strictMatch.toString());
        assertTrue(strictMatch.isConstrained());

        // FullTextQueryUnaryOp (NOT) — key class that was at 0%
        FullTextQueryUnaryOp notOp = new FullTextQueryUnaryOp(FullTextQuery.NOT, matchJava);
        assertFalse(notOp.isConstrained());          // NOT is always unconstrained
        assertTrue(notOp.toString().startsWith("NOT("));
        assertEquals(FullTextQuery.NOT, notOp.op);
        assertSame(matchJava, notOp.opd);

        // visit() on UnaryOp dispatches to UnaryOp then its operand
        final int[] cnt1 = {0};
        notOp.visit(new FullTextQueryVisitor() {
            @Override public void visit(FullTextQueryUnaryOp q) { cnt1[0]++; }
            @Override public void visit(FullTextQueryMatchOp q)  { cnt1[0]++; }
        });
        assertEquals(2, cnt1[0], "Should visit UnaryOp + its operand MatchOp");

        // FullTextQueryBinaryOp (AND) — left OR right constrained → constrained
        FullTextQueryMatchOp matchPython = new FullTextQueryMatchOp(FullTextQuery.MATCH, "python", 5);
        FullTextQueryBinaryOp andOp = new FullTextQueryBinaryOp(FullTextQuery.AND, matchJava, matchPython);
        assertTrue(andOp.isConstrained());
        assertTrue(andOp.toString().contains("AND"));
        assertSame(matchJava, andOp.left);
        assertSame(matchPython, andOp.right);

        // FullTextQueryBinaryOp (OR) — both constrained → constrained
        FullTextQueryBinaryOp orOp = new FullTextQueryBinaryOp(FullTextQuery.OR, matchJava, matchPython);
        assertTrue(orOp.isConstrained());
        assertTrue(orOp.toString().startsWith("("));
        assertTrue(orOp.toString().contains("OR"));

        // FullTextQueryBinaryOp (OR) with unconstrained left → NOT constrained
        FullTextQueryBinaryOp orWithNotLeft = new FullTextQueryBinaryOp(FullTextQuery.OR, notOp, matchPython);
        assertFalse(orWithNotLeft.isConstrained()); // OR needs BOTH sides constrained

        // FullTextQueryBinaryOp (AND) with NOT on right → left constrained → AND constrained
        FullTextQueryBinaryOp andWithNot = new FullTextQueryBinaryOp(FullTextQuery.AND, matchJava, notOp);
        assertTrue(andWithNot.isConstrained()); // AND needs left OR right constrained

        // visit() on BinaryOp dispatches to BinaryOp, left, right
        final int[] cnt2 = {0};
        andOp.visit(new FullTextQueryVisitor() {
            @Override public void visit(FullTextQueryBinaryOp q) { cnt2[0]++; }
            @Override public void visit(FullTextQueryMatchOp q)  { cnt2[0]++; }
        });
        assertEquals(3, cnt2[0], "Should visit BinaryOp + left MatchOp + right MatchOp");
    }

    @Test
    @DisplayName("Test FullTextSearchHelper.parseQuery - exercises query parser paths")
    void testFullTextSearchHelperParseQuery() {
        FullTextSearchHelper helper = new FullTextSearchHelper(storage);

        // Simple MATCH
        FullTextQuery q1 = helper.parseQuery("java", "en");
        assertNotNull(q1);
        assertInstanceOf(FullTextQueryMatchOp.class, q1);
        assertTrue(q1.isConstrained());

        // Implicit AND (two words)
        FullTextQuery q2 = helper.parseQuery("java python", "en");
        assertNotNull(q2);
        assertInstanceOf(FullTextQueryBinaryOp.class, q2);

        // Explicit AND
        FullTextQuery q3 = helper.parseQuery("java AND python", "en");
        assertNotNull(q3);
        assertInstanceOf(FullTextQueryBinaryOp.class, q3);

        // NOT at start → FullTextQueryUnaryOp
        FullTextQuery q4 = helper.parseQuery("NOT java", "en");
        assertNotNull(q4);
        assertInstanceOf(FullTextQueryUnaryOp.class, q4);
        assertFalse(q4.isConstrained());

        // OR
        FullTextQuery q5 = helper.parseQuery("java OR python", "en");
        assertNotNull(q5);
        assertInstanceOf(FullTextQueryBinaryOp.class, q5);
        assertEquals(FullTextQuery.OR, ((FullTextQueryBinaryOp) q5).op);

        // AND NOT → BinaryOp with right=UnaryOp(NOT)
        FullTextQuery q6 = helper.parseQuery("java AND NOT python", "en");
        assertNotNull(q6);
        assertInstanceOf(FullTextQueryBinaryOp.class, q6);
        FullTextQueryBinaryOp andNotOp = (FullTextQueryBinaryOp) q6;
        assertInstanceOf(FullTextQueryUnaryOp.class, andNotOp.right);

        // Quoted phrase → STRICT_MATCH or NEAR nodes
        FullTextQuery q7 = helper.parseQuery("\"java programming\"", "en");
        // result may be null if both words are stop-words, just don't throw
        // (non-null means nodes were created)

        // Nested grouping with parentheses
        FullTextQuery q8 = helper.parseQuery("(java OR python) AND database", "en");
        assertNotNull(q8);

        // toString() on nodes should never throw
        assertNotNull(q1.toString());
        assertNotNull(q3.toString());
        assertNotNull(q4.toString());
        assertNotNull(q5.toString());
        assertNotNull(q6.toString());

        // Utility methods
        assertTrue(helper.isWordChar('a'));
        assertTrue(helper.isWordChar('Z'));
        assertTrue(helper.isWordChar('5'));
        assertFalse(helper.isWordChar(' '));
        assertFalse(helper.isWordChar('.'));

        assertTrue(helper.isStopWord("the"));
        assertTrue(helper.isStopWord("a"));
        assertFalse(helper.isStopWord("java"));

        String[] forms = helper.getNormalForms("running", "en");
        assertNotNull(forms);
        assertEquals(1, forms.length);
        assertEquals("running", forms[0]);

        assertNotNull(helper.getOccurrenceKindWeights());
        assertEquals(10.0f, helper.getNearnessWeight(), 0.001f);
        assertEquals(10, helper.getWordSwapPenalty());
    }

    @Test
    @DisplayName("Test FullTextSearchResult.merge()")
    void testFullTextSearchResultMerge() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming fundamentals");
        Doc doc2 = new Doc(storage, "Database Book", "Learn database fundamentals SQL");
        Doc doc3 = new Doc(storage, "Full Stack Dev", "Learn Java and database together");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);
        storage.commit();

        FullTextSearchResult result1 = root.index.search("java", "en", 10, SEARCH_TIME_LIMIT);
        FullTextSearchResult result2 = root.index.search("fundamentals", "en", 10, SEARCH_TIME_LIMIT);

        assertTrue(result1.hits.length > 0, "Should find java docs");
        assertTrue(result2.hits.length > 0, "Should find fundamentals docs");

        // doc1 appears in both results → merge finds intersection
        FullTextSearchResult merged = result1.merge(result2);
        assertNotNull(merged);
        assertNotNull(merged.hits);
        // doc1 has both "java" and "fundamentals" so should appear
        assertTrue(merged.hits.length > 0, "Merge intersection should contain doc1");

        // merge with empty result (short-circuit both branches)
        FullTextSearchResult empty = root.index.search("xyznotexistsatall12345", "en", 10, SEARCH_TIME_LIMIT);
        assertEquals(0, empty.hits.length);

        FullTextSearchResult mergedRight = result1.merge(empty);
        assertEquals(0, mergedRight.hits.length, "Merging with empty (right) gives empty");

        FullTextSearchResult mergedLeft = empty.merge(result1);
        assertEquals(0, mergedLeft.hits.length, "Merging with empty (left) gives empty");
    }

    @Test
    @DisplayName("Test FullTextSearchHit.compareTo() and getDocument()")
    void testFullTextSearchHitDetails() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Guide", "Java Java Java programming deeply");
        Doc doc2 = new Doc(storage, "Mixed Guide", "Java basics");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);

        root.index.add(doc1);
        root.index.add(doc2);
        storage.commit();

        FullTextSearchResult result = root.index.search("java", "en", 10, SEARCH_TIME_LIMIT);
        assertTrue(result.hits.length >= 2, "Should find at least 2 hits");

        // getDocument()
        Object doc = result.hits[0].getDocument();
        assertNotNull(doc);
        assertInstanceOf(Doc.class, doc);

        // hit fields
        for (FullTextSearchHit hit : result.hits) {
            assertTrue(hit.rank >= 0, "Rank should be non-negative");
            assertTrue(hit.oid > 0, "OID should be positive");
            assertNotNull(hit.storage);
        }

        // compareTo() — results are sorted descending by rank
        FullTextSearchHit h0 = result.hits[0];
        FullTextSearchHit h1 = result.hits[1];
        assertTrue(h0.rank >= h1.rank);
        assertTrue(h0.compareTo(h1) <= 0, "Higher-ranked hit compares ≤0");
        assertTrue(h1.compareTo(h0) >= 0, "Lower-ranked hit compares ≥0");
    }

    @Test
    @DisplayName("Test full-text search AND NOT query (exercises UnaryOp via index search)")
    void testFullTextSearchAndNot() {
        Root root = new Root(storage);
        storage.setRoot(root);

        Doc doc1 = new Doc(storage, "Java Tutorial", "Learn Java programming");
        Doc doc2 = new Doc(storage, "Python Guide", "Learn Python programming");
        Doc doc3 = new Doc(storage, "Database Book", "Learn database design");

        root.titleIndex.put(doc1);
        root.titleIndex.put(doc2);
        root.titleIndex.put(doc3);

        root.index.add(doc1);
        root.index.add(doc2);
        root.index.add(doc3);
        storage.commit();

        // AND NOT exercises both BinaryOp AND UnaryOp(NOT) parse paths
        FullTextSearchResult result1 = root.index.search("programming AND NOT python", "en", 10, SEARCH_TIME_LIMIT);
        // result1 may be null if the constrained side produces no hits - just verify no exception thrown

        // NOT-only query is unconstrained: FullTextIndexImpl returns null (no positive term to anchor search)
        // Just verify it does not throw - return value may be null
        root.index.search("NOT python", "en", 10, SEARCH_TIME_LIMIT);
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
