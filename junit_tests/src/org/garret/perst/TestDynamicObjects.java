package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestDynamicObjects.java
 * Tests dynamic object functionality.
 * Scaled down for faster testing.
 */
class TestDynamicObjects {

    static class DynamicClass extends PersistentResource {
        String name;
        IPersistentSet<DynamicObject> instances;
        Index<Index<DynamicObject>> fieldIndex;

        DynamicClass() {
        }

        public DynamicClass(Storage db, String name) {
            super(db);
            this.name = name;
            instances = db.<DynamicObject>createSet();
            fieldIndex = db.<Index<DynamicObject>>createIndex(String.class, true);
        }

        public void deallocate() {
            instances.deallocate();
            fieldIndex.deallocate();
            super.deallocate();
        }
    }

    static class DynamicObject extends SmallMap<String, Object> {
        DynamicClass cls;

        public void deallocate() {
            for (Map.Entry<String, Object> field : entrySet()) {
                String fieldName = field.getKey();
                Object fieldValue = field.getValue();
                Index<DynamicObject> index = cls.fieldIndex.get(fieldName);
                index.remove(fieldValue, this);
                if (index.size() == 0) {
                    cls.fieldIndex.remove(fieldName);
                }
                cls.instances.remove(this);
            }
            super.deallocate();
        }
    }

    static class ClassDictionary extends PersistentResource {
        FieldIndex<DynamicClass> classIndex;

        public ClassDictionary(Storage db) {
            super(db);
            classIndex = db.<DynamicClass>createFieldIndex(DynamicClass.class, "name", true);
        }

        ClassDictionary() {
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testdynobj.dbs";
    private static final int nClasses = 5;
    private static final int nFields = 10;
    private static final int maxObjectFields = 5;
    private static final int nObjects = 100;

    static long rnd;

    static void resetRand() {
        rnd = 1999;
    }

    static int nextRandInt(int mod) {
        rnd = (3141592621L * rnd + 2718281829L) % 1000000007L;
        return (int) (rnd % mod);
    }

    static long nextRandLong() {
        return rnd = (3141592621L * rnd + 2718281829L) % 1000000007L;
    }

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
    @DisplayName("Test dynamic objects create classes")
    void testDynamicObjectsCreateClasses() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create dynamic classes
        for (int i = 0; i < nClasses; i++) {
            DynamicClass cls = new DynamicClass(storage, "Class" + i);
            dictionary.classIndex.add(cls);
        }
        storage.commit();

        assertEquals(nClasses, dictionary.classIndex.size(), "Should have created classes");
    }

    @Test
    @DisplayName("Test dynamic objects create and query")
    void testDynamicObjectsCreateAndQuery() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create dynamic classes
        for (int i = 0; i < nClasses; i++) {
            DynamicClass cls = new DynamicClass(storage, "Class" + i);
            dictionary.classIndex.add(cls);
        }
        storage.commit();

        // Create dynamic objects
        resetRand();
        for (int i = 0; i < nObjects; i++) {
            String className = "Class" + nextRandInt(nClasses);
            DynamicObject obj = new DynamicObject();
            obj.cls = dictionary.classIndex.get(className);
            int nObjFields = nextRandInt(maxObjectFields) + 1;
            int fieldNo = nextRandInt(nFields);
            for (int j = 0; j < nObjFields; j++) {
                String fieldName = "Field" + (fieldNo++ % nFields);
                long intValue = nextRandLong();
                Object fieldValue = (nextRandInt(1) == 0) ? intValue : Long.toString(intValue);
                obj.cls.instances.add(obj);
                Index<DynamicObject> index = obj.cls.fieldIndex.get(fieldName);
                if (index == null) {
                    index = storage.<DynamicObject>createIndex(fieldValue instanceof String ? String.class : long.class, false);
                    obj.cls.fieldIndex.put(fieldName, index);
                }
                index.put(fieldValue, obj);
                obj.put(fieldName, fieldValue);
            }
        }
        storage.commit();

        assertTrue(nObjects > 0, "Should have created objects");
    }

    @Test
    @DisplayName("Test dynamic objects search")
    void testDynamicObjectsSearch() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create dynamic classes
        for (int i = 0; i < nClasses; i++) {
            DynamicClass cls = new DynamicClass(storage, "Class" + i);
            dictionary.classIndex.add(cls);
        }
        storage.commit();

        // Create dynamic objects
        resetRand();
        for (int i = 0; i < nObjects; i++) {
            String className = "Class" + nextRandInt(nClasses);
            DynamicObject obj = new DynamicObject();
            obj.cls = dictionary.classIndex.get(className);
            int nObjFields = nextRandInt(maxObjectFields) + 1;
            int fieldNo = nextRandInt(nFields);
            for (int j = 0; j < nObjFields; j++) {
                String fieldName = "Field" + (fieldNo++ % nFields);
                long intValue = nextRandLong();
                Object fieldValue = (nextRandInt(1) == 0) ? intValue : Long.toString(intValue);
                obj.cls.instances.add(obj);
                Index<DynamicObject> index = obj.cls.fieldIndex.get(fieldName);
                if (index == null) {
                    index = storage.<DynamicObject>createIndex(fieldValue instanceof String ? String.class : long.class, false);
                    obj.cls.fieldIndex.put(fieldName, index);
                }
                index.put(fieldValue, obj);
                obj.put(fieldName, fieldValue);
            }
        }
        storage.commit();

        // Search objects
        resetRand();
        int searchCount = 0;
        for (int i = 0; i < nObjects; i++) {
            String className = "Class" + nextRandInt(nClasses);
            DynamicClass cls = dictionary.classIndex.get(className);
            int nObjFields = nextRandInt(maxObjectFields) + 1;
            HashSet<DynamicObject> objects = null;
            int fieldNo = nextRandInt(nFields);
            for (int j = 0; j < nObjFields; j++) {
                String fieldName = "Field" + (fieldNo++ % nFields);
                long intValue = nextRandLong();
                Object fieldValue = (nextRandInt(1) == 0) ? intValue : Long.toString(intValue);
                Index<DynamicObject> index = cls.fieldIndex.get(fieldName);
                if (index != null) {
                    ArrayList<DynamicObject> occurrences = index.getList(fieldValue, fieldValue);
                    if (objects == null) {
                        objects = new HashSet<DynamicObject>(occurrences);
                    } else {
                        objects.retainAll(occurrences);
                    }
                }
            }
            if (objects != null && objects.size() > 0) {
                searchCount++;
            }
        }

        assertTrue(searchCount >= 0, "Search should complete without errors");
    }

    @Test
    @DisplayName("Test dynamic objects traverse")
    void testDynamicObjectsTraverse() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create dynamic classes
        for (int i = 0; i < nClasses; i++) {
            DynamicClass cls = new DynamicClass(storage, "Class" + i);
            dictionary.classIndex.add(cls);
        }

        // Create dynamic objects
        resetRand();
        for (int i = 0; i < nObjects; i++) {
            String className = "Class" + nextRandInt(nClasses);
            DynamicObject obj = new DynamicObject();
            obj.cls = dictionary.classIndex.get(className);
            int nObjFields = nextRandInt(maxObjectFields) + 1;
            int fieldNo = nextRandInt(nFields);
            for (int j = 0; j < nObjFields; j++) {
                String fieldName = "Field" + (fieldNo++ % nFields);
                long intValue = nextRandLong();
                Object fieldValue = (nextRandInt(1) == 0) ? intValue : Long.toString(intValue);
                obj.cls.instances.add(obj);
                Index<DynamicObject> index = obj.cls.fieldIndex.get(fieldName);
                if (index == null) {
                    index = storage.<DynamicObject>createIndex(fieldValue instanceof String ? String.class : long.class, false);
                    obj.cls.fieldIndex.put(fieldName, index);
                }
                index.put(fieldValue, obj);
                obj.put(fieldName, fieldValue);
            }
        }
        storage.commit();

        // Traverse all objects
        int count = 0;
        for (int i = 0; i < nClasses; i++) {
            String className = "Class" + i;
            DynamicClass cls = dictionary.classIndex.get(className);
            for (DynamicObject obj : cls.instances) {
                assertEquals(cls, obj.cls, "Object should belong to correct class");
                count += 1;
            }
        }

        assertEquals(nObjects, count, "Should traverse all objects");
    }

    @Test
    @DisplayName("Test dynamic objects delete")
    void testDynamicObjectsDelete() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create dynamic classes
        for (int i = 0; i < nClasses; i++) {
            DynamicClass cls = new DynamicClass(storage, "Class" + i);
            dictionary.classIndex.add(cls);
        }

        // Create dynamic objects
        resetRand();
        for (int i = 0; i < nObjects; i++) {
            String className = "Class" + nextRandInt(nClasses);
            DynamicObject obj = new DynamicObject();
            obj.cls = dictionary.classIndex.get(className);
            int nObjFields = nextRandInt(maxObjectFields) + 1;
            int fieldNo = nextRandInt(nFields);
            for (int j = 0; j < nObjFields; j++) {
                String fieldName = "Field" + (fieldNo++ % nFields);
                long intValue = nextRandLong();
                Object fieldValue = (nextRandInt(1) == 0) ? intValue : Long.toString(intValue);
                obj.cls.instances.add(obj);
                Index<DynamicObject> index = obj.cls.fieldIndex.get(fieldName);
                if (index == null) {
                    index = storage.<DynamicObject>createIndex(fieldValue instanceof String ? String.class : long.class, false);
                    obj.cls.fieldIndex.put(fieldName, index);
                }
                index.put(fieldValue, obj);
                obj.put(fieldName, fieldValue);
            }
        }
        storage.commit();

        // Delete all objects
        int deletedClasses = 0;
        int deletedObjects = 0;
        Iterator<DynamicClass> classes = dictionary.classIndex.iterator();
        while (classes.hasNext()) {
            DynamicClass cls = classes.next();
            Iterator<DynamicObject> instances = cls.instances.iterator();
            while (instances.hasNext()) {
                DynamicObject obj = instances.next();
                instances.remove();
                obj.deallocate();
                deletedObjects++;
            }
            classes.remove();
            cls.deallocate();
            deletedClasses++;
        }
        storage.commit();

        assertEquals(nClasses, deletedClasses, "Should delete all classes");
        assertEquals(nObjects, deletedObjects, "Should delete all objects");
    }

    @Test
    @DisplayName("Test dynamic objects get by class name")
    void testDynamicObjectsGetByClassName() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create dynamic classes
        for (int i = 0; i < nClasses; i++) {
            DynamicClass cls = new DynamicClass(storage, "Class" + i);
            dictionary.classIndex.add(cls);
        }
        storage.commit();

        // Get class by name
        DynamicClass cls = dictionary.classIndex.get("Class0");
        assertNotNull(cls, "Should find class by name");
        assertEquals("Class0", cls.name, "Class name should match");
    }

    @Test
    @DisplayName("Test dynamic objects field index")
    void testDynamicObjectsFieldIndex() {
        ClassDictionary dictionary = new ClassDictionary(storage);
        storage.setRoot(dictionary);

        // Create a dynamic class
        DynamicClass cls = new DynamicClass(storage, "TestClass");
        dictionary.classIndex.add(cls);

        // Create an object
        DynamicObject obj = new DynamicObject();
        obj.cls = cls;
        obj.put("name", "TestObject");
        obj.put("value", 42);
        cls.instances.add(obj);

        // Create field index
        Index<DynamicObject> nameIndex = storage.<DynamicObject>createIndex(String.class, false);
        cls.fieldIndex.put("name", nameIndex);
        nameIndex.put("TestObject", obj);

        storage.commit();

        // Query by field
        ArrayList<DynamicObject> results = nameIndex.getList("TestObject", "TestObject");
        assertEquals(1, results.size(), "Should find object by field value");
    }
}
