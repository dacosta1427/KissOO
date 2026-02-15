package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestBit.java
 * Tests bit index functionality with car options
 */
class TestBit {

    static class Car extends Persistent {
        int hps;
        int maxSpeed;
        int timeTo100;
        int options;
        String model;
        String vendor;
        String specification;

        static final int CLASS_A = 0x00000001;
        static final int CLASS_B = 0x00000002;
        static final int CLASS_C = 0x00000004;
        static final int CLASS_D = 0x00000008;

        static final int UNIVERAL = 0x00000010;
        static final int SEDAN = 0x00000020;
        static final int HATCHBACK = 0x00000040;
        static final int MINIWAN = 0x00000080;

        static final int AIR_COND = 0x00000100;
        static final int CLIMANT_CONTROL = 0x00000200;
        static final int SEAT_HEATING = 0x00000400;
        static final int MIRROR_HEATING = 0x00000800;

        static final int ABS = 0x00001000;
        static final int ESP = 0x00002000;
        static final int EBD = 0x00004000;
        static final int TC = 0x00008000;

        static final int FWD = 0x00010000;
        static final int REAR_DRIVE = 0x00020000;
        static final int FRONT_DRIVE = 0x00040000;

        static final int GPS_NAVIGATION = 0x00100000;
        static final int CD_RADIO = 0x00200000;
        static final int CASSETTE_RADIO = 0x00400000;
        static final int LEATHER = 0x00800000;

        static final int XEON_LIGHTS = 0x01000000;
        static final int LOW_PROFILE_TIRES = 0x02000000;
        static final int AUTOMATIC = 0x04000000;

        static final int DISEL = 0x10000000;
        static final int TURBO = 0x20000000;
        static final int GASOLINE = 0x40000000;
    }

    static class Catalogue extends Persistent {
        FieldIndex<Car> modelIndex;
        BitIndex<Car> optionIndex;
    }

    private Storage storage;
    private static final int nRecords = 1000; // Scaled down from 1000000
    private static final String TEST_DB = "testbit.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 48 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test bit index insert and search")
    void testBitIndexInsertAndSearch() {
        Catalogue root = new Catalogue();
        root.optionIndex = storage.createBitIndex();
        root.modelIndex = storage.createFieldIndex(Car.class, "model", true);
        storage.setRoot(root);

        BitIndex<Car> index = root.optionIndex;

        int selectedOptions = Car.TURBO | Car.DISEL | Car.FWD | Car.ABS | Car.EBD | Car.ESP | Car.AIR_COND | Car.HATCHBACK | Car.CLASS_C;
        int unselectedOptions = Car.AUTOMATIC;

        long rnd = 1999;
        int expectedCount = 0;

        for (int i = 0; i < nRecords; i++) {
            rnd = (3141592621L * rnd + 2718281829L) % 1000000007L;
            int options = (int) rnd;
            Car car = new Car();
            car.model = Long.toString(rnd);
            car.options = options;
            root.modelIndex.put(car);
            root.optionIndex.put(car, options);
            if ((options & selectedOptions) == selectedOptions && (options & unselectedOptions) == 0) {
                expectedCount += 1;
            }
        }
        storage.commit();

        // Search using bit index
        Iterator<Car> iterator = root.optionIndex.iterator(selectedOptions, unselectedOptions);
        int count = 0;
        while (iterator.hasNext()) {
            Car car = iterator.next();
            assertEquals(selectedOptions, car.options & selectedOptions, "Car should have all selected options");
            assertEquals(0, car.options & unselectedOptions, "Car should not have unselected options");
            count += 1;
        }

        assertEquals(expectedCount, count, "Should find expected number of cars matching criteria");
    }

    @Test
    @DisplayName("Test bit index remove and clear")
    void testBitIndexRemoveAndClear() {
        Catalogue root = new Catalogue();
        root.optionIndex = storage.createBitIndex();
        root.modelIndex = storage.createFieldIndex(Car.class, "model", true);
        storage.setRoot(root);

        // Insert records
        long rnd = 1999;
        for (int i = 0; i < nRecords; i++) {
            rnd = (3141592621L * rnd + 2718281829L) % 1000000007L;
            int options = (int) rnd;
            Car car = new Car();
            car.model = Long.toString(rnd);
            car.options = options;
            root.modelIndex.put(car);
            root.optionIndex.put(car, options);
        }
        storage.commit();

        // Remove all records
        Iterator<Car> iterator = root.modelIndex.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Car car = iterator.next();
            root.optionIndex.remove(car);
            car.deallocate();
            count += 1;
        }
        root.optionIndex.clear();

        assertEquals(nRecords, count, "Should have removed all records");

        // Verify bit index is empty
        assertFalse(root.optionIndex.iterator(0, 0).hasNext(), "Bit index should be empty after clear");
    }
}
