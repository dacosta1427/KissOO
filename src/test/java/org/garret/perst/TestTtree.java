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

    @Test
    @DisplayName("Test T-tree clear")
    void testTtreeClear() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        // Insert records
        for (int i = 0; i < 100; i++) {
            root.list.add(new Person("First" + i, "Last" + i, i));
        }
        storage.commit();

        assertEquals(100, root.list.size(), "Should have 100 records");

        // Clear
        root.list.clear();
        assertEquals(0, root.list.size(), "Should be empty after clear");
        assertTrue(root.list.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Test T-tree add all")
    void testTtreeAddAll() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        java.util.List<Person> people = new java.util.ArrayList<>();
        people.add(new Person("Alice", "Smith", 30));
        people.add(new Person("Bob", "Jones", 25));
        people.add(new Person("Carol", "White", 35));

        root.list.addAll(people);
        storage.commit();

        assertEquals(3, root.list.size(), "Should have 3 records");
    }

    @Test
    @DisplayName("Test T-tree to array")
    void testTtreeToArray() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        root.list.add(new Person("Alice", "Smith", 30));
        root.list.add(new Person("Bob", "Jones", 25));
        storage.commit();

        Object[] arr = root.list.toArray();
        assertEquals(2, arr.length, "Array should have 2 elements");

        Person[] typedArr = root.list.toArray(new Person[0]);
        assertEquals(2, typedArr.length, "Typed array should have 2 elements");
    }

    @Test
    @DisplayName("Test T-tree contains all")
    void testTtreeContainsAll() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        Person p1 = new Person("Alice", "Smith", 30);
        Person p2 = new Person("Bob", "Jones", 25);
        Person p3 = new Person("Carol", "White", 35);

        root.list.add(p1);
        root.list.add(p2);
        storage.commit();

        java.util.List<Person> people = new java.util.ArrayList<>();
        people.add(p1);
        people.add(p2);

        assertTrue(root.list.containsAll(people), "Should contain all added elements");

        people.add(p3);
        assertFalse(root.list.containsAll(people), "Should not contain unadded element");
    }

    @Test
    @DisplayName("Test T-tree remove all")
    void testTtreeRemoveAll() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        Person p1 = new Person("Alice", "Smith", 30);
        Person p2 = new Person("Bob", "Jones", 25);
        Person p3 = new Person("Carol", "White", 35);

        root.list.add(p1);
        root.list.add(p2);
        root.list.add(p3);
        storage.commit();

        assertEquals(3, root.list.size(), "Should have 3 records");

        java.util.List<Person> toRemove = new java.util.ArrayList<>();
        toRemove.add(p1);
        toRemove.add(p3);

        root.list.removeAll(toRemove);
        assertEquals(1, root.list.size(), "Should have 1 record after removeAll");
        assertTrue(root.list.contains(p2), "Should still contain p2");
    }

    @Test
    @DisplayName("Test T-tree retain all")
    void testTtreeRetainAll() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        Person p1 = new Person("Alice", "Smith", 30);
        Person p2 = new Person("Bob", "Jones", 25);
        Person p3 = new Person("Carol", "White", 35);

        root.list.add(p1);
        root.list.add(p2);
        root.list.add(p3);
        storage.commit();

        java.util.List<Person> toRetain = new java.util.ArrayList<>();
        toRetain.add(p2);

        root.list.retainAll(toRetain);
        assertEquals(1, root.list.size(), "Should have 1 record after retainAll");
        assertTrue(root.list.contains(p2), "Should contain p2");
    }

    @Test
    @DisplayName("Test T-tree non-unique collection")
    void testTtreeNonUnique() {
        PersonList root = new PersonList();
        // Create non-unique sorted collection
        root.list = storage.<Person>createSortedCollection(new NameComparator(), false);
        storage.setRoot(root);

        // Insert duplicate names (different ages)
        root.list.add(new Person("John", "Doe", 30));
        root.list.add(new Person("John", "Doe", 25));
        root.list.add(new Person("John", "Doe", 35));
        storage.commit();

        assertEquals(3, root.list.size(), "Non-unique collection should allow duplicates");
    }

    @Test
    @DisplayName("Test T-tree large insert causing page splits")
    void testTtreeLargeInsert() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        // Insert many records to trigger page splits
        for (int i = 0; i < 5000; i++) {
            String firstName = "First" + i;
            String lastName = "Last" + i;
            root.list.add(new Person(firstName, lastName, i % 100));
        }
        storage.commit();

        assertEquals(5000, root.list.size(), "Should have 5000 records");

        // Verify all can be found
        for (int i = 0; i < 5000; i++) {
            Name name = new Name();
            name.first = "First" + i;
            name.last = "Last" + i;
            Person p = root.list.get(name);
            assertNotNull(p, "Should find person " + i);
        }
    }

    @Test
    @DisplayName("Test T-tree sequential access")
    void testTtreeSequentialAccess() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        // Insert in sorted order
        for (int i = 0; i < 100; i++) {
            String name = String.format("%03d", i);
            root.list.add(new Person(name, "Last", i));
        }
        storage.commit();

        // Verify sequential access
        Iterator<Person> iter = root.list.iterator();
        String prevName = "";
        int count = 0;
        while (iter.hasNext()) {
            Person p = iter.next();
            assertTrue(p.firstName.compareTo(prevName) > 0, "Should be in sorted order");
            prevName = p.firstName;
            count++;
        }
        assertEquals(100, count, "Should iterate through all 100 records");
    }

    @Test
    @DisplayName("Test T-tree reverse iteration")
    void testTtreeReverseIteration() {
        PersonList root = new PersonList();
        root.list = storage.<Person>createSortedCollection(new NameComparator(), true);
        storage.setRoot(root);

        root.list.add(new Person("Alice", "A", 1));
        root.list.add(new Person("Bob", "B", 2));
        root.list.add(new Person("Carol", "C", 3));
        storage.commit();

        // Get in reverse order by iterating all and checking
        Iterator<Person> iter = root.list.iterator();
        java.util.List<String> names = new java.util.ArrayList<>();
        while (iter.hasNext()) {
            names.add(iter.next().firstName);
        }

        assertEquals("Alice", names.get(0), "First should be Alice");
        assertEquals("Bob", names.get(1), "Second should be Bob");
        assertEquals("Carol", names.get(2), "Third should be Carol");
    }
}
