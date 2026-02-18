package org.garret.perst.assoc;

import org.garret.perst.*;
import org.garret.perst.fulltext.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Date;

public class TestLibrary {
    private Storage storage;
    private AssocDB db;
    private static final String DB_FILE = "TestLibrary.dbs";

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
    public void testCreateAuthorAndBook() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item author = t.createItem();
        t.link(author, "name", "Jane Austen");
        t.includeInFullTextIndex(author);

        Item book = t.createItem();
        t.link(book, "title", "Pride and Prejudice");
        t.link(book, "ISBN", "978-0-14-143951-8");
        t.link(book, "publish-date", new Date().getTime());
        t.includeInFullTextIndex(book, new String[]{"title", "ISBN"});
        t.link(book, "author", author);

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        Item found = rt.find(Predicate.compare("title", Predicate.Compare.Operation.Equals, "Pride and Prejudice")).first();
        assertNotNull(found);
        assertEquals("978-0-14-143951-8", found.getString("ISBN"));
        rt.commit();
    }

    @Test
    public void testFindBooksByAuthor() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item author = t.createItem();
        t.link(author, "name", "George Orwell");

        Item book1 = t.createItem();
        t.link(book1, "title", "1984");
        t.link(book1, "author", author);

        Item book2 = t.createItem();
        t.link(book2, "title", "Animal Farm");
        t.link(book2, "author", author);

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        IterableIterator<Item> booksIter = rt.find(Predicate.in("author", Predicate.compare("name", Predicate.Compare.Operation.Equals, "George Orwell")));
        java.util.ArrayList<Item> booksList = booksIter.toList();
        assertEquals(2, booksList.size());
        rt.commit();
    }

    @Test
    public void testFindAuthorByBook() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item author = t.createItem();
        t.link(author, "name", "J.K. Rowling");

        Item book = t.createItem();
        t.link(book, "title", "Harry Potter");
        t.link(book, "author", author);

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        IterableIterator<Item> authorsIter = rt.find(Predicate.in("-author", Predicate.compare("title", Predicate.Compare.Operation.Equals, "Harry Potter")));
        java.util.ArrayList<Item> authorsList = authorsIter.toList();
        assertEquals(1, authorsList.size());
        assertEquals("J.K. Rowling", authorsList.get(0).getString("name"));
        rt.commit();
    }

    @Test
    public void testFullTextSearch() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item doc = t.createItem();
        t.link(doc, "title", "Java Programming Guide");
        t.link(doc, "content", "Learn Java programming from basics to advanced");
        t.includeInFullTextIndex(doc, new String[]{"title", "content"});

        Item doc2 = t.createItem();
        t.link(doc2, "title", "Python Tutorial");
        t.link(doc2, "content", "Python programming for beginners");
        t.includeInFullTextIndex(doc2, new String[]{"title", "content"});

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        FullTextSearchResult result = rt.fullTextSearch("Java", 10, 5000);
        assertNotNull(result);
        assertTrue(result.hits.length >= 1);
        rt.commit();
    }

    @Test
    public void testManyToManyRelation() {
        ReadWriteTransaction t = db.startReadWriteTransaction();

        Item author1 = t.createItem();
        t.link(author1, "name", "Author One");

        Item author2 = t.createItem();
        t.link(author2, "name", "Author Two");

        Item book = t.createItem();
        t.link(book, "title", "Collaboration");
        t.link(book, "author", author1);
        t.link(book, "author", author2);

        t.commit();

        ReadOnlyTransaction rt = db.startReadOnlyTransaction();
        IterableIterator<Item> authorsIter = rt.find(Predicate.in("-author", Predicate.compare("title", Predicate.Compare.Operation.Equals, "Collaboration")));
        java.util.ArrayList<Item> authorsList = authorsIter.toList();
        assertEquals(2, authorsList.size());
        rt.commit();
    }
}
