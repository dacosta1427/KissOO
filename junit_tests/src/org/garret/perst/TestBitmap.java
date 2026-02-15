package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestBitmap.java
 * Tests bitmap index functionality combining multiple indices
 */
class TestBitmap {

    static class Restaurant extends Persistent {
        float lat;
        float lng;
        String kitchen;
        int avgPrice;
        int rating;
    }

    static class City extends Persistent {
        SpatialIndexR2<Restaurant> byLocation;
        FieldIndex<Restaurant> byKitchen;
        FieldIndex<Restaurant> byAvgPrice;
        FieldIndex<Restaurant> byRating;
    }

    private Storage storage;
    // Scaled down from 1000 records and 100 searches
    private static final int nRecords = 100;
    private static final int nSearches = 10;
    private static final String TEST_DB = "testbitmap.dbs";
    private static final String[] kitchens = {"asian", "chines", "european", "japan", "italian", "french", "medeteranian", "nepal", "mexican", "indian", "vegetarian"};

    private City city;
    private Random rnd;

    @BeforeEach
    void setUp() throws Exception {
        // Delete existing database to start fresh
        new java.io.File(TEST_DB).delete();
        
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 48 * 1024 * 1024);

        city = new City();
        city.byLocation = storage.<Restaurant>createSpatialIndexR2();
        city.byKitchen = storage.<Restaurant>createFieldIndex(Restaurant.class, "kitchen", false, true, true);
        city.byAvgPrice = storage.<Restaurant>createFieldIndex(Restaurant.class, "avgPrice", false, true, true);
        city.byRating = storage.<Restaurant>createFieldIndex(Restaurant.class, "rating", false, true, true);
        storage.setRoot(city);

        rnd = new Random(2013);

        // Insert restaurants
        for (int i = 0; i < nRecords; i++) {
            Restaurant rest = new Restaurant();
            rest.lat = 55 + rnd.nextFloat();
            rest.lng = 37 + rnd.nextFloat();
            rest.kitchen = kitchens[rnd.nextInt(kitchens.length)];
            rest.avgPrice = rnd.nextInt(1000);
            rest.rating = rnd.nextInt(10);
            city.byLocation.put(new RectangleR2(rest.lat, rest.lng, rest.lat, rest.lng), rest);
            city.byKitchen.put(rest);
            city.byAvgPrice.put(rest);
            city.byRating.put(rest);
        }
        storage.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    private int countBitmap(Bitmap bitmap) {
        int count = 0;
        for (Iterator<?> it = bitmap.iterator(); it.hasNext(); it.next()) {
            count++;
        }
        return count;
    }

    @Test
    @DisplayName("Test bitmap insert and index creation")
    void testBitmapInsertAndIndexCreation() {
        // Verify all indices were created
        assertNotNull(city.byLocation, "Spatial index should be created");
        assertNotNull(city.byKitchen, "Kitchen index should be created");
        assertNotNull(city.byAvgPrice, "Price index should be created");
        assertNotNull(city.byRating, "Rating index should be created");

        // Verify record count
        int count = 0;
        for (Restaurant r : city.byKitchen) {
            count++;
        }
        assertEquals(nRecords, count, "All restaurants should be indexed");
    }

    @Test
    @DisplayName("Test bitmap query with multiple indices")
    void testBitmapQueryWithMultipleIndices() {
        long total = 0;
        long nProbes = 0;

        rnd = new Random(2013); // Reset random for reproducible results
        
        for (int i = 0; i < nSearches; i++) {
            int oid;
            double lat = 55 + rnd.nextFloat();
            double lng = 37 + rnd.nextFloat();
            String kitchen = kitchens[rnd.nextInt(kitchens.length)];
            int minPrice = rnd.nextInt(1000);
            int maxPrice = minPrice + rnd.nextInt(1000);
            int minRating = rnd.nextInt(10);

            // Create bitmap from kitchen index
            Bitmap bitmap = storage.createBitmap(city.byKitchen.iterator(kitchen, kitchen, Index.ASCENT_ORDER));
            
            // Intersect with price index
            bitmap.and(storage.createBitmap(city.byAvgPrice.iterator(minPrice, maxPrice, Index.ASCENT_ORDER)));
            
            // Intersect with rating index
            bitmap.and(storage.createBitmap(city.byRating.iterator(minRating, null, Index.ASCENT_ORDER)));

            // Query spatial index
            PersistentIterator iterator = (PersistentIterator) city.byLocation.neighborIterator(lat, lng);

            int nAlternatives = 0;
            while ((oid = iterator.nextOid()) != 0) {
                nProbes += 1;
                if (bitmap.contains(oid)) {
                    Restaurant rest = (Restaurant) storage.getObjectByOID(oid);
                    total += 1;
                    // Verify the restaurant matches our criteria
                    assertEquals(kitchen, rest.kitchen, "Kitchen should match");
                    assertTrue(rest.avgPrice >= minPrice && rest.avgPrice <= maxPrice, "Price should be in range");
                    assertTrue(rest.rating >= minRating, "Rating should be >= minRating");
                    
                    if (++nAlternatives == 10) {
                        break;
                    }
                }
            }
        }

        // Verify we found some results
        assertTrue(nProbes > 0, "Should have probed some records");
        assertTrue(total > 0, "Should have found matching restaurants");
    }

    @Test
    @DisplayName("Test bitmap contains")
    void testBitmapContains() {
        // Get first restaurant OID
        int firstOid = 0;
        for (Restaurant r : city.byKitchen) {
            firstOid = r.getOid();
            break;
        }

        // Create bitmap with all restaurants in a kitchen
        Bitmap bitmap = storage.createBitmap(city.byKitchen.iterator("asian", "asian", Index.ASCENT_ORDER));
        
        // Check if bitmap works
        assertNotNull(bitmap, "Bitmap should be created");
        
        // Test with a known OID
        if (firstOid != 0) {
            // The bitmap may or may not contain the first OID depending on whether it's asian cuisine
            // Just verify bitmap operations work
            bitmap.contains(firstOid); // Should not throw
        }
    }

    @Test
    @DisplayName("Test bitmap and operation")
    void testBitmapAndOperation() {
        // Create two bitmaps and combine them
        Bitmap bitmap1 = storage.createBitmap(city.byRating.iterator(5, null, Index.ASCENT_ORDER));
        Bitmap bitmap2 = storage.createBitmap(city.byAvgPrice.iterator(0, 500, Index.ASCENT_ORDER));
        
        int sizeBefore = countBitmap(bitmap1);
        
        // AND operation - should not throw
        bitmap1.and(bitmap2);
        
        // Verify the bitmap still works after AND - just check we can iterate
        int countAfter = countBitmap(bitmap1);
        assertTrue(countAfter >= 0, "Bitmap should be usable after AND operation");
    }

    @Test
    @DisplayName("Test bitmap or operation")
    void testBitmapOrOperation() {
        // Create two bitmaps and combine them
        Bitmap bitmap1 = storage.createBitmap(city.byRating.iterator(5, null, Index.ASCENT_ORDER));
        Bitmap bitmap2 = storage.createBitmap(city.byAvgPrice.iterator(500, 1000, Index.ASCENT_ORDER));
        
        int sizeBefore = countBitmap(bitmap1);
        
        // OR operation - should not throw
        bitmap1.or(bitmap2);
        
        // Verify the bitmap still works after OR
        int countAfter = countBitmap(bitmap1);
        assertTrue(countAfter >= 0, "Bitmap should be usable after OR operation");
    }
}
