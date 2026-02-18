package org.garret.perst.assoc;

import org.garret.perst.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class TestHospital {
    private Storage storage;
    private AssocDB db;
    private static final String DB_FILE = "TestHospital.dbs";

    @BeforeEach
    public void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB_FILE);
        db = new AssocDB(storage);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (storage != null && storage.isOpened()) {
            storage.close();
        }
        new File(DB_FILE).delete();
    }

    @Test
    public void testCreateAndLinkItems() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item patient = t.createItem();
        t.link(patient, "class", "patient");
        t.link(patient, "name", "John Smith");
        t.link(patient, "age", 55);

        Item doctor = t.createItem();
        t.link(doctor, "class", "doctor");
        t.link(doctor, "name", "Dr. House");

        t.link(doctor, "patient", patient);

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        Item found = rt.find(Predicate.compare("name", Predicate.Compare.Operation.Equals, "John Smith")).first();
        assertNotNull(found);
        assertEquals("patient", found.getString("class"));
        assertEquals(55, found.getNumber("age").intValue());
        rt.commit();
    }

    @Test
    public void testFindWithPredicate() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item patient1 = t.createItem();
        t.link(patient1, "class", "patient");
        t.link(patient1, "name", "Alice");
        t.link(patient1, "age", 25);

        Item patient2 = t.createItem();
        t.link(patient2, "class", "patient");
        t.link(patient2, "name", "Bob");
        t.link(patient2, "age", 65);

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        IterableIterator<Item> results = rt.find(Predicate.compare("age", Predicate.Compare.Operation.GreaterThan, 30));
        
        int count = 0;
        while (results.hasNext()) {
            results.next();
            count++;
        }
        assertEquals(1, count);
        rt.commit();
    }

    @Test
    public void testUpdateItem() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item patient = t.createItem();
        t.link(patient, "class", "patient");
        t.link(patient, "name", "Charlie");
        t.link(patient, "age", 40);
        t.commit();

        ReadWriteTransaction t2 = db.startReadWriteTransaction();
        Item found = t2.find(Predicate.compare("name", Predicate.Compare.Operation.Equals, "Charlie")).first();
        assertNotNull(found);
        t2.update(found, "age", 41);
        t2.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        Item updated = rt.find(Predicate.compare("name", Predicate.Compare.Operation.Equals, "Charlie")).first();
        assertEquals(41, updated.getNumber("age").intValue());
        rt.commit();
    }

    @Test
    public void testOrderBy() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item book1 = t.createItem();
        t.link(book1, "title", "Zebra");
        t.link(book1, "class", "book");

        Item book2 = t.createItem();
        t.link(book2, "title", "Alpha");
        t.link(book2, "class", "book");

        Item book3 = t.createItem();
        t.link(book3, "title", "Beta");
        t.link(book3, "class", "book");

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        Item[] results = rt.find(Predicate.compare("class", Predicate.Compare.Operation.Equals, "book"), new OrderBy("title"));
        
        assertEquals(3, results.length);
        assertEquals("Alpha", results[0].getString("title"));
        assertEquals("Beta", results[1].getString("title"));
        assertEquals("Zebra", results[2].getString("title"));
        rt.commit();
    }

    @Test
    public void testMultipleValues() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item item = t.createItem();
        t.link(item, "tags", "java");
        t.link(item, "tags", "database");
        t.link(item, "tags", "orm");

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        Object tags = rt.find(Predicate.compare("tags", Predicate.Compare.Operation.Equals, "java")).first().getAttribute("tags");
        assertNotNull(tags);
        assertTrue(tags instanceof String[]);
        String[] tagArray = (String[]) tags;
        assertEquals(3, tagArray.length);
        rt.commit();
    }
}
