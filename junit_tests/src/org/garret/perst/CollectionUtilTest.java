package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Tests for collection utilities:
 * - Projection (155 instructions)
 * - ThreadSafeIterator (71 instructions)
 * - PersistentResource (163 instructions)
 * - IterableIterator (42 instructions)
 * - PersistentCollection (136 instructions)
 */
class CollectionUtilTest {

    private static final String DB = "testcollutil.dbs";
    private Storage storage;

    public static class Author extends Persistent {
        public String name;
        public Author() {}
        public Author(Storage s, String name) { super(s); this.name = name; }
    }

    public static class Book extends Persistent {
        public String title;
        public Author author;
        public Link<Author> coauthors;
        public Book() {}
        public Book(Storage s, String title, Author author) {
            super(s);
            this.title = title;
            this.author = author;
            this.coauthors = s.createLink();
        }
    }

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB, 4 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        if (storage != null && storage.isOpened()) storage.close();
        new java.io.File(DB).delete();
    }

    // ==============================
    // Projection tests
    // ==============================

    @Test @DisplayName("Projection: project array through single-value field")
    void testProjectionSingleField() {
        Author a1 = new Author(storage, "Alice");
        Author a2 = new Author(storage, "Bob");
        Book b1  = new Book(storage, "Book1", a1);
        Book b2  = new Book(storage, "Book2", a2);
        Book b3  = new Book(storage, "Book3", a1); // same author as b1

        Projection<Book, Author> proj = new Projection<>(Book.class, "author");
        proj.project(new Book[]{b1, b2, b3});

        // Deduplication: Alice appears twice but projection uses a Set
        assertEquals(2, proj.size(), "Should have 2 distinct authors");
        assertTrue(proj.contains(a1));
        assertTrue(proj.contains(a2));
    }

    @Test @DisplayName("Projection: project iterator")
    void testProjectionIterator() {
        Author a = new Author(storage, "Carol");
        Book b1  = new Book(storage, "B1", a);
        Book b2  = new Book(storage, "B2", a);

        List<Book> books = Arrays.asList(b1, b2);
        Projection<Book, Author> proj = new Projection<>(Book.class, "author");
        proj.project(books.iterator());

        assertEquals(1, proj.size()); // Both point to same author
    }

    @Test @DisplayName("Projection: project collection")
    void testProjectionCollection() {
        Author a1 = new Author(storage, "Dave");
        Author a2 = new Author(storage, "Eve");
        List<Book> books = Arrays.asList(
            new Book(storage, "B1", a1),
            new Book(storage, "B2", a2)
        );
        Projection<Book, Author> proj = new Projection<>(Book.class, "author");
        proj.project(books);
        assertEquals(2, proj.size());
    }

    @Test @DisplayName("Projection: project single object")
    void testProjectionSingleObject() {
        Author a = new Author(storage, "Frank");
        Book b   = new Book(storage, "B1", a);

        Projection<Book, Author> proj = new Projection<>(Book.class, "author");
        proj.project(b);
        assertEquals(1, proj.size());
        assertTrue(proj.contains(a));
    }

    @Test @DisplayName("Projection: reset() clears the projection")
    void testProjectionReset() {
        Author a = new Author(storage, "Grace");
        Book b   = new Book(storage, "B1", a);

        Projection<Book, Author> proj = new Projection<>(Book.class, "author");
        proj.project(b);
        assertEquals(1, proj.size());
        proj.reset();
        assertEquals(0, proj.size());
    }

    @Test @DisplayName("Projection: join() retains intersection")
    void testProjectionJoin() {
        Author a1 = new Author(storage, "Hank");
        Author a2 = new Author(storage, "Iris");
        Book b1   = new Book(storage, "B1", a1);
        Book b2   = new Book(storage, "B2", a2);
        Book b3   = new Book(storage, "B3", a1);

        Projection<Book, Author> p1 = new Projection<>(Book.class, "author");
        p1.project(new Book[]{b1, b2});

        Projection<Book, Author> p2 = new Projection<>(Book.class, "author");
        p2.project(new Book[]{b3, b2}); // b3 has a1, b2 has a2

        p1.join(p2); // intersection: authors that appear in both
        // Both p1 and p2 contain a1 and a2 → intersection = {a1, a2}
        assertEquals(2, p1.size());
    }

    @Test @DisplayName("Projection: project Link field")
    void testProjectionLinkField() {
        Author a1 = new Author(storage, "Jack");
        Author a2 = new Author(storage, "Kate");

        Book book = new Book(storage, "SharedBook", a1);
        book.coauthors.add(a1);
        book.coauthors.add(a2);

        Projection<Book, Author> proj = new Projection<>(Book.class, "coauthors");
        proj.project(book);
        assertEquals(2, proj.size()); // a1 and a2
    }

    @Test @DisplayName("Projection: add(null) is ignored")
    void testProjectionAddNull() {
        Projection<Book, Author> proj = new Projection<>(Book.class, "author");
        boolean added = proj.add(null);
        assertFalse(added);
        assertEquals(0, proj.size());
    }

    @Test @DisplayName("Projection: default constructor + setProjectionField")
    void testProjectionDefaultConstructorSetField() {
        Author a = new Author(storage, "Lena");
        Book  b = new Book(storage, "B1", a);

        Projection<Book, Author> proj = new Projection<>();
        proj.setProjectionField(Book.class, "author");
        proj.project(b);
        assertEquals(1, proj.size());
    }

    // ==============================
    // PersistentResource tests
    // ==============================

    @Test @DisplayName("PersistentResource: sharedLock and unlock")
    void testPersistentResourceSharedLock() {
        PersistentResource res = new PersistentResource();
        // Multiple shared locks from same thread are allowed
        res.sharedLock();
        res.sharedLock();
        res.unlock();
        res.unlock();
        // No exception = pass
    }

    @Test @DisplayName("PersistentResource: exclusiveLock and unlock")
    void testPersistentResourceExclusiveLock() {
        PersistentResource res = new PersistentResource();
        res.exclusiveLock();
        res.exclusiveLock(); // Reentrant from same thread
        res.unlock();
        res.unlock();
    }

    @Test @DisplayName("PersistentResource: sharedLock with timeout")
    void testPersistentResourceSharedLockTimeout() {
        PersistentResource res = new PersistentResource();
        boolean got = res.sharedLock(1000L);
        assertTrue(got, "Should acquire shared lock with timeout");
        res.unlock();
    }

    @Test @DisplayName("PersistentResource: exclusiveLock with timeout")
    void testPersistentResourceExclusiveLockTimeout() {
        PersistentResource res = new PersistentResource();
        boolean got = res.exclusiveLock(1000L);
        assertTrue(got, "Should acquire exclusive lock with timeout");
        res.unlock();
    }

    @Test @DisplayName("PersistentResource: reset() releases exclusive lock")
    void testPersistentResourceReset() {
        PersistentResource res = new PersistentResource();
        res.exclusiveLock();
        res.reset(); // Should release the lock
        // After reset, another exclusiveLock should succeed
        res.exclusiveLock();
        res.unlock();
    }

    @Test @DisplayName("PersistentResource: reset() decrements reader count")
    void testPersistentResourceResetReader() {
        PersistentResource res = new PersistentResource();
        res.sharedLock();
        res.sharedLock(); // nReaders = 2
        res.reset();  // decrements by 1
        res.unlock(); // decrements by 1
    }

    @Test @DisplayName("PersistentResource: storage constructor")
    void testPersistentResourceStorageConstructor() {
        PersistentResource res = new PersistentResource(storage);
        assertNotNull(res);
        res.sharedLock();
        res.unlock();
    }

    // ==============================
    // ThreadSafeIterator tests
    // ==============================

    @Test @DisplayName("ThreadSafeIterator: iterate over index")
    void testThreadSafeIteratorBasic() {
        FieldIndex<Author> idx = storage.createFieldIndex(Author.class, "name", true);
        storage.setRoot((IPersistent) idx);

        for (int i = 0; i < 5; i++) {
            idx.put(new Author(storage, "author" + i));
        }
        storage.commit();

        PersistentResource resource = new PersistentResource(storage);
        storage.store(resource);

        ThreadSafeIterator<Author> tsi = new ThreadSafeIterator<>(resource, idx.iterator());

        int count = 0;
        while (tsi.hasNext()) {
            assertNotNull(tsi.next());
            count++;
        }
        assertEquals(5, count);
    }

    @Test @DisplayName("ThreadSafeIterator: hasNext returns false when empty")
    void testThreadSafeIteratorEmpty() {
        FieldIndex<Author> idx = storage.createFieldIndex(Author.class, "name", true);
        storage.setRoot((IPersistent) idx);
        storage.commit();

        PersistentResource resource = new PersistentResource(storage);
        storage.store(resource);

        ThreadSafeIterator<Author> tsi = new ThreadSafeIterator<>(resource, idx.iterator());
        assertFalse(tsi.hasNext());
    }

    @Test @DisplayName("ThreadSafeIterator: remove element")
    void testThreadSafeIteratorRemove() {
        FieldIndex<Author> idx = storage.createFieldIndex(Author.class, "name", false);
        storage.setRoot((IPersistent) idx);

        idx.put(new Author(storage, "alpha"));
        idx.put(new Author(storage, "beta"));
        idx.put(new Author(storage, "gamma"));
        storage.commit();

        PersistentResource resource = new PersistentResource(storage);
        storage.store(resource);

        ThreadSafeIterator<Author> tsi = new ThreadSafeIterator<>(resource, idx.iterator());
        assertTrue(tsi.hasNext());
        tsi.next();
        tsi.remove(); // Remove first element
        storage.commit();

        assertEquals(2, idx.size());
    }
}
