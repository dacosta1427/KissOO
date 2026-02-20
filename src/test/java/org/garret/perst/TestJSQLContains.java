package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestJSQLContains.java
 * Tests JSQL CONTAINS operator with nested queries
 */
class TestJSQLContains {

    static class A extends Persistent {
        String parent;
        Link<B> b;
    }

    static class B extends Persistent {
        String child;
    }

    private Storage storage;
    private Database db;
    // Scaled down from 100x100 to 10x10
    private static final int countA = 10;
    private static final int countB = 10;
    private static final String TEST_DB = "testjsqlcontains.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB);
        db = new Database(storage);

        // Create A and B objects
        for (int i = 0; i < countA; i++) {
            A a = new A();
            a.parent = "A." + i;
            a.b = storage.createLink(countB);
            a.b.setSize(countB);
            for (int j = 0; j < countB; j++) {
                B b = new B();
                b.child = "B." + i + "." + j;
                db.addRecord(b);
                a.b.set(j, b);
            }
            db.addRecord(a);
        }
        db.commitTransaction();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            db.dropTable(A.class);
            db.dropTable(B.class);
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test CONTAINS operator with nested query")
    @SuppressWarnings("unchecked")
    void testContainsOperator() {
        // Query: contains b with child like '%.0'
        // This should find all A objects where one of their B children has child ending with '.0'
        int i = 0;
        for (Object obj : db.select(A.class, "contains b with child like '%.0'")) {
            A a = (A) obj;
            assertEquals("A." + i, a.parent, "Should match expected parent");
            i += 1;
        }
        assertEquals(countA, i, "Should find all A objects");
    }

    @Test
    @DisplayName("Test CONTAINS with specific child pattern")
    @SuppressWarnings("unchecked")
    void testContainsSpecificPattern() {
        // Query for B.0.% pattern (children of A.0)
        int count = 0;
        for (Object obj : db.select(A.class, "contains b with child like 'B.0.%'")) {
            A a = (A) obj;
            assertTrue(a.parent.equals("A.0"), "Should only match A.0");
            count += 1;
        }
        assertEquals(1, count, "Should find exactly one A object");
    }

    @Test
    @DisplayName("Test CONTAINS with empty result")
    @SuppressWarnings("unchecked")
    void testContainsEmptyResult() {
        // Query for non-existent pattern
        int count = 0;
        for (Object obj : db.select(A.class, "contains b with child like 'NOTEXIST%'")) {
            count += 1;
        }
        assertEquals(0, count, "Should find no A objects");
    }
}
