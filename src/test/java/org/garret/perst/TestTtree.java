package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestTtree.java
 * Tests T-tree sorted collection functionality
 */
class TestTtree {

    static class Name {
        String first;
        String last;
    }

    static class Person extends Persistent {
        String firstName;
        String lastName;
        int age;

        private Person() {}

        Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }
    }

    static class PersonList extends Persistent {
        SortedCollection<Person> list;
    }

    static class NameComparator extends PersistentComparator<Person> {
        public int compareMembers(Person p1, Person p2) {
            int diff = p1.firstName.compareTo(p2.firstName);
            if (diff != 0) {
                return diff;
            }
            return p1.lastName.compareTo(p2.lastName);
        }

        public int compareMemberWithKey(Person p, Object key) {
            Name name = (Name)key;
            int diff = p.firstName.compareTo(name.first);
            if (diff != 0) {
                return diff;
            }
            return p.lastName.compareTo(name.last);
        }
    }

    private Storage storage;
    // Scaled down from 100000 records
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testtree.dbs";

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
    @DisplayName("Test T-tree insert")
    void testTtreeInsert() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            String str = Long.toString(key);
            int m = str.length() / 2;
            String firstName = str.substring(0, m);
            String lastName = str.substring(m);
            int age = (int) key % 100;
            Person p = new Person(firstName, lastName, age);
            root.list.add(p);
        }
        storage.commit();

        // Verify all records were inserted
        int count = 0;
        for (Person p : root.list) {
            count++;
        }
        assertEquals(nRecords, count, "All records should be inserted");
    }

    @Test
    @DisplayName("Test T-tree search by key")
    void testTtreeSearchByKey() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            String str = Long.toString(key);
            int m = str.length() / 2;
            String firstName = str.substring(0, m);
            String lastName = str.substring(m);
            int age = (int) key % 100;
            Person p = new Person(firstName, lastName, age);
            root.list.add(p);
        }
        storage.commit();

        // Search for records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            String str = Long.toString(key);
            int m = str.length() / 2;
            Name name = new Name();
            int age = (int) key % 100;
            name.first = str.substring(0, m);
            name.last = str.substring(m);

            Person p = root.list.get(name);
            assertNotNull(p, "Should find person by name");
            assertTrue(root.list.contains(p), "Collection should contain the person");
            assertEquals(age, p.age, "Age should match");
        }
    }

    @Test
    @DisplayName("Test T-tree iterator")
    void testTtreeIterator() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        // Insert some records
        root.list.add(new Person("John", "Doe", 30));
        root.list.add(new Person("Jane", "Doe", 25));
        root.list.add(new Person("Alice", "Smith", 35));
        storage.commit();

        // Iterate and verify ordering
        Iterator<Person> iterator = root.list.iterator();
        Name name = new Name();
        name.first = name.last = "";
        PersistentComparator comparator = root.list.getComparator();

        int count = 0;
        while (iterator.hasNext()) {
            Person p = iterator.next();
            assertTrue(comparator.compareMemberWithKey(p, name) > 0, "Should be in sorted order");
            name.first = p.firstName;
            name.last = p.lastName;
            count++;
        }

        assertEquals(3, count, "Should have iterated through all 3 records");
    }

    @Test
    @DisplayName("Test T-tree remove")
    void testTtreeRemove() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        // Insert and remove
        Person p1 = new Person("John", "Doe", 30);
        Person p2 = new Person("Jane", "Doe", 25);
        Person p3 = new Person("Alice", "Smith", 35);

        root.list.add(p1);
        root.list.add(p2);
        root.list.add(p3);
        storage.commit();

        // Verify all exist
        assertEquals(3, root.list.size(), "Should have 3 records");

        // Remove one
        root.list.remove(p2);
        assertEquals(2, root.list.size(), "Should have 2 records after removal");
        assertFalse(root.list.contains(p2), "Removed person should not be in collection");

        // Verify others still exist
        assertTrue(root.list.contains(p1), "Person 1 should still exist");
        assertTrue(root.list.contains(p3), "Person 3 should still exist");
    }

    @Test
    @DisplayName("Test T-tree is empty")
    void testTtreeEmpty() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        assertTrue(root.list.isEmpty(), "New collection should be empty");
        assertFalse(root.list.iterator().hasNext(), "Iterator should have no elements");
    }
}
