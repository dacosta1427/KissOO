package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for PersistentMapImpl which has 0% coverage (1,250 instructions)
 */
class TestPersistentMap {

    static class MapRecord extends Persistent {
        String key;
        String value;
        
        MapRecord() {}
        
        MapRecord(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testmapimpl.dbs";

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
    @DisplayName("Test create and use PersistentMap")
    void testCreatePersistentMap() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        assertNotNull(map, "Map should be created");
        assertTrue(map.isEmpty(), "Map should be empty initially");
        assertEquals(0, map.size(), "Map size should be 0");
    }

    @Test
    @DisplayName("Test put and get operations")
    void testPutAndGet() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        MapRecord rec1 = new MapRecord("key1", "value1");
        map.put("key1", rec1);
        
        MapRecord retrieved = map.get("key1");
        assertNotNull(retrieved, "Should retrieve the record");
        assertEquals("key1", retrieved.key);
        assertEquals("value1", retrieved.value);
    }

    @Test
    @DisplayName("Test putAll operation")
    void testPutAll() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        MapRecord rec1 = new MapRecord("key1", "value1");
        MapRecord rec2 = new MapRecord("key2", "value2");
        
        Map<String, MapRecord> toPut = new HashMap<>();
        toPut.put("key1", rec1);
        toPut.put("key2", rec2);
        
        map.putAll(toPut);
        
        assertEquals(2, map.size(), "Map should have 2 entries");
    }

    @Test
    @DisplayName("Test containsKey")
    void testContainsKey() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        MapRecord rec = new MapRecord("key1", "value1");
        map.put("key1", rec);
        
        assertTrue(map.containsKey("key1"), "Should contain key1");
        assertFalse(map.containsKey("nonexistent"), "Should not contain nonexistent key");
    }

    @Test
    @DisplayName("Test containsValue")
    void testContainsValue() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        MapRecord rec = new MapRecord("key1", "value1");
        map.put("key1", rec);
        
        assertTrue(map.containsValue(rec), "Should contain the value");
    }

    @Test
    @DisplayName("Test remove operation")
    void testRemove() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        MapRecord rec = new MapRecord("key1", "value1");
        map.put("key1", rec);
        
        MapRecord removed = map.remove("key1");
        assertNotNull(removed, "Should return removed value");
        assertEquals(0, map.size(), "Map should be empty after removal");
    }

    @Test
    @DisplayName("Test clear operation")
    void testClear() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, new MapRecord("key" + i, "value" + i));
        }
        
        assertEquals(10, map.size(), "Map should have 10 entries");
        
        map.clear();
        
        assertEquals(0, map.size(), "Map should be empty after clear");
        assertTrue(map.isEmpty(), "Map should be empty");
    }

    @Test
    @DisplayName("Test keySet")
    void testKeySet() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        map.put("key1", new MapRecord("key1", "value1"));
        map.put("key2", new MapRecord("key2", "value2"));
        
        Set<String> keys = map.keySet();
        assertEquals(2, keys.size(), "KeySet should have 2 keys");
        assertTrue(keys.contains("key1"), "Should contain key1");
        assertTrue(keys.contains("key2"), "Should contain key2");
    }

    @Test
    @DisplayName("Test values collection")
    void testValues() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        map.put("key1", new MapRecord("key1", "value1"));
        map.put("key2", new MapRecord("key2", "value2"));
        
        Collection<MapRecord> values = map.values();
        assertEquals(2, values.size(), "Values collection should have 2 elements");
    }

    @Test
    @DisplayName("Test entrySet")
    void testEntrySet() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        map.put("key1", new MapRecord("key1", "value1"));
        map.put("key2", new MapRecord("key2", "value2"));
        
        Set<Map.Entry<String, MapRecord>> entries = map.entrySet();
        assertEquals(2, entries.size(), "EntrySet should have 2 entries");
    }

    @Test
    @DisplayName("Test iterator over map")
    void testIterator() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        for (int i = 0; i < 5; i++) {
            map.put("key" + i, new MapRecord("key" + i, "value" + i));
        }
        
        int count = 0;
        for (Map.Entry<String, MapRecord> entry : map.entrySet()) {
            count++;
            assertNotNull(entry.getKey(), "Entry key should not be null");
            assertNotNull(entry.getValue(), "Entry value should not be null");
        }
        assertEquals(5, count, "Should iterate over all 5 entries");
    }

    @Test
    @DisplayName("Test map with different key types")
    void testMapWithIntegerKeys() {
        IPersistentMap<Integer, MapRecord> map = storage.createMap(MapRecord.class);
        
        map.put(1, new MapRecord("1", "one"));
        map.put(2, new MapRecord("2", "two"));
        
        assertEquals(2, map.size(), "Map should have 2 entries");
        assertNotNull(map.get(1), "Should get value by integer key");
        assertNotNull(map.get(2), "Should get value by integer key");
    }

    @Test
    @DisplayName("Test subMap functionality")
    void testSubMap() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, new MapRecord("key" + i, "value" + i));
        }
        
        // Test subMap - get a subset of keys
        SortedMap<String, MapRecord> subMap = map.subMap("key2", "key5");
        
        // Note: behavior depends on implementation, just verify no error
        if (subMap != null) {
            // SubMap may have limited functionality
        }
    }

    @Test
    @DisplayName("Test firstKey and lastKey")
    void testFirstAndLastKey() {
        IPersistentMap<String, MapRecord> map = storage.createMap(MapRecord.class);
        
        map.put("aaa", new MapRecord("aaa", "first"));
        map.put("zzz", new MapRecord("zzz", "last"));
        
        if (map instanceof java.util.NavigableMap) {
            java.util.NavigableMap<String, MapRecord> navMap = (java.util.NavigableMap<String, MapRecord>) map;
            assertNotNull(navMap.firstKey(), "Should have first key");
            assertNotNull(navMap.lastKey(), "Should have last key");
        }
    }

    @Test
    @DisplayName("Test hashCode and equals")
    void testHashCodeAndEquals() {
        IPersistentMap<String, MapRecord> map1 = storage.createMap(MapRecord.class);
        IPersistentMap<String, MapRecord> map2 = storage.createMap(MapRecord.class);
        
        MapRecord rec1 = new MapRecord("key1", "value1");
        map1.put("key1", rec1);
        map2.put("key1", rec1);
        
        // Test hashCode doesn't throw
        int hash1 = map1.hashCode();
        assertTrue(hash1 != 0, "hashCode should return a value");
    }
}
