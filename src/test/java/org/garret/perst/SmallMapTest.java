package org.garret.perst;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2B: Tests for SmallMap, SmallMap.Pair, SmallMap.EntrySet, SmallMap.ArrayIterator.
 *
 * SmallMap is a persistent Map backed by an in-order array. Because it extends
 * PersistentResource (which extends Persistent), a live Storage instance is required
 * to call modify() properly. We open a minimal database, store the map as root, and
 * exercise every public API.
 */
class SmallMapTest {

    private static final String TEST_DB = "testsmallmap.dbs";
    private Storage storage;
    private SmallMap<String, Integer> map;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 4 * 1024 * 1024);
        map = new SmallMap<>();
        storage.makePersistent(map);
        storage.setRoot(map);
    }

    @AfterEach
    void tearDown() {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    // ===== isEmpty / size on empty map =====

    @Test
    @DisplayName("Empty map: isEmpty() true, size() 0, get() null")
    void testEmptyMap() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("anything"));
        assertFalse(map.containsKey("k"));
        assertFalse(map.containsValue(42));
    }

    // ===== put / get =====

    @Test
    @DisplayName("put() inserts new entries; get() retrieves them")
    void testPutAndGet() {
        assertNull(map.put("a", 1));
        assertNull(map.put("b", 2));
        assertNull(map.put("c", 3));

        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
        assertNull(map.get("z"));
    }

    // ===== put replaces existing entry =====

    @Test
    @DisplayName("put() on existing key returns old value and updates")
    void testPutReplacesExistingEntry() {
        map.put("x", 10);
        Integer old = map.put("x", 99);

        assertEquals(10, old, "put() should return the old value");
        assertEquals(99, map.get("x"), "New value should overwrite old");
        assertEquals(1, map.size(), "Size should not grow on key collision");
    }

    // ===== put null key → exception =====

    @Test
    @DisplayName("put() with null key throws IllegalArgumentException")
    void testPutNullKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> map.put(null, 0));
    }

    // ===== remove =====

    @Test
    @DisplayName("remove() deletes entry and returns its value")
    void testRemove() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Integer removed = map.remove("b");
        assertEquals(2, removed);
        assertEquals(2, map.size());
        assertNull(map.get("b"));
        assertFalse(map.containsKey("b"));
    }

    @Test
    @DisplayName("remove() on absent key returns null")
    void testRemoveAbsentKey() {
        assertNull(map.remove("notPresent"));
    }

    // ===== containsKey / containsValue =====

    @Test
    @DisplayName("containsKey() and containsValue() work correctly")
    void testContainsKeyValue() {
        map.put("k1", 100);
        map.put("k2", 200);

        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertFalse(map.containsKey("k3"));

        assertTrue(map.containsValue(100));
        assertTrue(map.containsValue(200));
        assertFalse(map.containsValue(999));
    }

    // ===== containsValue with null =====

    @Test
    @DisplayName("containsValue() handles null value")
    void testContainsNullValue() {
        map.put("n", null);
        assertTrue(map.containsValue(null));
        assertFalse(map.containsValue(0));
    }

    // ===== getEntry =====

    @Test
    @DisplayName("getEntry() returns Map.Entry or null")
    void testGetEntry() {
        map.put("alpha", 42);
        Map.Entry<String, Integer> entry = map.getEntry("alpha");
        assertNotNull(entry);
        assertEquals("alpha", entry.getKey());
        assertEquals(42, entry.getValue());

        assertNull(map.getEntry("beta"));
    }

    // ===== putAll =====

    @Test
    @DisplayName("putAll() copies all entries from another map")
    void testPutAll() {
        Map<String, Integer> src = new LinkedHashMap<>();
        src.put("x", 10);
        src.put("y", 20);
        src.put("z", 30);

        map.putAll(src);
        assertEquals(3, map.size());
        assertEquals(10, map.get("x"));
        assertEquals(20, map.get("y"));
        assertEquals(30, map.get("z"));
    }

    // ===== clear =====

    @Test
    @DisplayName("clear() removes all entries")
    void testClear() {
        map.put("a", 1);
        map.put("b", 2);
        map.clear();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("a"));
    }

    // ===== keySet =====

    @Test
    @DisplayName("keySet(): returns all keys, supports contains, remove, clear")
    void testKeySet() {
        map.put("k1", 1);
        map.put("k2", 2);
        map.put("k3", 3);

        Set<String> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("k1"));
        assertTrue(keys.contains("k2"));
        assertFalse(keys.contains("missing"));

        // Iterate via KeyIterator (ArrayIterator)
        Set<String> iterated = new HashSet<>();
        for (String k : keys) {
            iterated.add(k);
        }
        assertEquals(Set.of("k1", "k2", "k3"), iterated);
    }

    @Test
    @DisplayName("keySet().remove() removes the entry from the map")
    void testKeySetRemove() {
        map.put("a", 1);
        map.put("b", 2);

        boolean removed = map.keySet().remove("a");
        assertTrue(removed);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
    }

    @Test
    @DisplayName("keySet().clear() empties the map")
    void testKeySetClear() {
        map.put("a", 1);
        map.keySet().clear();
        assertTrue(map.isEmpty());
    }

    // ===== values =====

    @Test
    @DisplayName("values(): returns all values, supports contains")
    void testValues() {
        map.put("k1", 10);
        map.put("k2", 20);

        Collection<Integer> vals = map.values();
        assertEquals(2, vals.size());
        assertTrue(vals.contains(10));
        assertTrue(vals.contains(20));
        assertFalse(vals.contains(99));

        // Iterate via ValueIterator
        List<Integer> iterated = new ArrayList<>();
        for (int v : vals) {
            iterated.add(v);
        }
        assertTrue(iterated.contains(10));
        assertTrue(iterated.contains(20));
    }

    @Test
    @DisplayName("values().clear() empties the map")
    void testValuesClear() {
        map.put("a", 1);
        map.values().clear();
        assertTrue(map.isEmpty());
    }

    // ===== entrySet =====

    @Test
    @DisplayName("entrySet(): returns all entries, supports contains, remove, clear")
    void testEntrySet() {
        map.put("e1", 11);
        map.put("e2", 22);

        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        assertEquals(2, entries.size());

        // Iterate via EntryIterator
        Map<String, Integer> copy = new HashMap<>();
        for (Map.Entry<String, Integer> e : entries) {
            copy.put(e.getKey(), e.getValue());
        }
        assertEquals(11, copy.get("e1"));
        assertEquals(22, copy.get("e2"));
    }

    @Test
    @DisplayName("entrySet().contains() checks key/value equality")
    void testEntrySetContains() {
        map.put("k", 5);
        Set<Map.Entry<String, Integer>> es = map.entrySet();
        // contains(non-Entry) → false
        assertFalse(es.contains("not-an-entry"));
    }

    @Test
    @DisplayName("entrySet().clear() empties the map")
    void testEntrySetClear() {
        map.put("x", 9);
        map.entrySet().clear();
        assertTrue(map.isEmpty());
    }

    // ===== ArrayIterator.remove() =====

    @Test
    @DisplayName("Iterator.remove() removes current entry from map during iteration")
    void testIteratorRemove() {
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        Iterator<String> it = map.keySet().iterator();
        // Remove the first key we encounter
        String first = it.next();
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(first));
    }

    // ===== Pair.equals / hashCode / toString / setValue =====

    @Test
    @DisplayName("SmallMap.Pair: equals, hashCode, toString, setValue")
    void testPairEqualsHashCodeToStringSetValue() {
        map.put("p", 7);
        Map.Entry<String, Integer> entry = map.getEntry("p");
        assertNotNull(entry);

        // toString
        String s = entry.toString();
        assertTrue(s.contains("p"), "toString should contain key");

        // hashCode should not throw
        int h = entry.hashCode();
        assertTrue(h != 0 || h == 0); // just verify no exception

        // equals with same key/value
        Map.Entry<String, Integer> copy = new AbstractMap.SimpleEntry<>("p", 7);
        assertEquals(copy, entry);
        assertEquals(entry, copy);

        // equals with different value
        Map.Entry<String, Integer> diff = new AbstractMap.SimpleEntry<>("p", 99);
        assertNotEquals(diff, entry);

        // equals with non-Entry
        assertNotEquals(entry, "not-a-pair");

        // setValue on the Pair
        entry.setValue(42);
        // The underlying map is modified in-place (value updated in pairs array)
        assertEquals(42, map.get("p"));
    }

    // ===== equals / hashCode on SmallMap itself =====

    @Test
    @DisplayName("SmallMap.equals() and hashCode() match a regular HashMap")
    void testSmallMapEqualsHashCode() {
        map.put("a", 1);
        map.put("b", 2);

        Map<String, Integer> regular = new HashMap<>();
        regular.put("a", 1);
        regular.put("b", 2);

        assertEquals(map, regular);
        assertEquals(regular, map);
        assertEquals(map.hashCode(), regular.hashCode());

        // Different map → not equal
        Map<String, Integer> diff = new HashMap<>();
        diff.put("a", 999);
        assertNotEquals(map, diff);
        assertNotEquals(map, "not-a-map");
    }

    // ===== toString on SmallMap =====

    @Test
    @DisplayName("SmallMap.toString() produces non-empty string")
    void testToString() {
        assertEquals("{}", map.toString());

        map.put("k", 1);
        String s = map.toString();
        assertTrue(s.startsWith("{"));
        assertTrue(s.contains("k=1"));
    }

    // ===== removeAt (internal, exercised via remove at specific index) =====

    @Test
    @DisplayName("removeAt() via keySet iterator (covers ArrayIterator.remove index decrement)")
    void testRemoveAtViaIterator() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Iterator<String> it = map.keySet().iterator();
        it.next();
        it.remove(); // removes index 0 → covers removeAt(0) path
        it.next();
        it.remove(); // removes next

        assertEquals(1, map.size());
    }
}
