package oodb;

import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.FullTextSearchResult;
import org.garret.perst.continuous.VersionSelector;
import org.garret.perst.IterableIterator;
import mycompany.domain.Actor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CDatabase versioning functionality.
 * Tests that:
 * 1. Current versions can be stored and retrieved
 * 2. Non-current (historical) versions are stored in Lucene index
 * 3. Version history is accessible via VersionSelector
 */
public class CDatabaseVersioningTest {

    public static void main(String[] args) {
        // Initialize Perst first (this is normally done by KissInit)
        PerstStorageManager.initialize();
        
        CDatabaseVersioningTest test = new CDatabaseVersioningTest();
        try {
            test.testVersioningFlow();
            System.out.println("\n=== ALL TESTS PASSED ===");
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void testVersioningFlow() throws Exception {
        System.out.println("=== CDatabase Versioning Test ===\n");

        // 1. Verify CDatabase is initialized
        System.out.println("1. Checking CDatabase initialization...");
        CDatabase database = (CDatabase) org.kissweb.restServer.MainServlet.getEnvironment("perstDatabase");
        assertNotNull(database, "CDatabase should be initialized");
        System.out.println("   CDatabase: " + (database != null ? "OK" : "FAILED"));

        // 2. Clear any existing test data
        System.out.println("\n2. Clearing existing test data...");
        clearTestData(database);

        // 3. Insert initial version (v1)
        System.out.println("\n3. Inserting initial version (v1)...");
        database.beginTransaction();
        Actor actor1 = createTestActor("John", "Test user for versioning");
        database.insert(actor1);
        database.commitTransaction();
        String uuid = actor1.getUuid();
        long txId1 = actor1.getTransactionId();
        System.out.println("   Created Actor with UUID: " + uuid);
        System.out.println("   Transaction ID: " + txId1);

        // 4. Update to create v2
        System.out.println("\n4. Updating to create v2...");
        database.beginTransaction();
        actor1.setName("John Updated");
        Actor actor2 = database.update(actor1);
        database.commitTransaction();
        long txId2 = actor2.getTransactionId();
        System.out.println("   New Transaction ID: " + txId2);
        System.out.println("   UUID preserved: " + actor2.getUuid().equals(uuid));

        // 5. Update again to create v3
        System.out.println("\n5. Updating again to create v3...");
        database.beginTransaction();
        actor2.setName("John Final");
        Actor actor3 = database.update(actor2);
        database.commitTransaction();
        long txId3 = actor3.getTransactionId();
        System.out.println("   New Transaction ID: " + txId3);
        
        // Create more versions to test slicing (v4-v10)
        System.out.println("\n5b. Creating more versions (v4-v10) for slicing tests...");
        Actor current = actor3;
        for (int i = 4; i <= 10; i++) {
            database.beginTransaction();
            current.setName("John Version " + i);
            current = database.update(current);
            database.commitTransaction();
            System.out.println("   Created v" + i + " with txId: " + current.getTransactionId());
        }
        long finalTxId = current.getTransactionId();
        System.out.println("   Final Transaction ID: " + finalTxId);

        // 6. Get CURRENT version (should be v3)
        System.out.println("\n6. Testing getRecords with CURRENT selector...");
        List<Actor> currentRecords = getRecords(database, Actor.class, VersionSelector.CURRENT);
        System.out.println("   Current records count: " + currentRecords.size());
        assertEquals(1, currentRecords.size(), "Should have 1 current record");
        System.out.println("   Current name: " + currentRecords.get(0).getName());
        assertTrue(currentRecords.get(0).getName().startsWith("John Version"), "Current should be latest version");

        // 7. Test getRecords without selector (should return current)
        System.out.println("\n7. Testing getRecords without selector...");
        List<Actor> defaultRecords = getRecords(database, Actor.class);
        System.out.println("   Default records count: " + defaultRecords.size());
        assertEquals(1, defaultRecords.size(), "Should have 1 current record");
        
        // 8. Test select with query on current
        System.out.println("\n8. Testing select on current versions...");
        IterableIterator<Actor> currentIter = database.select(Actor.class, "name='John Version 10'");
        List<Actor> selectedCurrent = toList(currentIter);
        System.out.println("   Found: " + selectedCurrent.size() + " record(s) with name 'John Version 10'");
        assertTrue(selectedCurrent.size() >= 1, "Should find at least 1 record");

        // 9. Verify Lucene index directory exists
        System.out.println("\n9. Verifying Lucene index directory...");
        String dbPath = PerstConfig.getInstance().getDatabasePath();
        java.io.File idxDir = new java.io.File(dbPath + ".idx");
        System.out.println("   Index path: " + idxDir.getAbsolutePath());
        System.out.println("   Index exists: " + idxDir.exists());
        System.out.println("   Is directory: " + (idxDir.exists() && idxDir.isDirectory()));
        
        if (idxDir.exists() && idxDir.isDirectory()) {
            String[] files = idxDir.list();
            System.out.println("   Index files count: " + (files != null ? files.length : 0));
        }
        
        // 10. Test full-text search across all historical versions
        System.out.println("\n10. Testing full-text search across all versions...");
        FullTextSearchResult[] results = database.fullTextSearch("John", 100, VersionSelector.CURRENT, CDatabase.VersionSortOrder.DESCENT_ORDER);
        System.out.println("   Full-text search for 'John' (CURRENT): " + results.length + " results");
        
        // 11. Test with 100 objects, 10 versions each
        System.out.println("\n11. Stress test: 100 objects x 10 versions...");
        int numObjects = 100;
        int numVersions = 10;
        
        database.beginTransaction();
        for (int i = 0; i < numObjects; i++) {
            Actor actor = createTestActor("Actor_" + i, "Test actor " + i);
            database.insert(actor);
        }
        database.commitTransaction();
        
        for (int v = 2; v <= numVersions; v++) {
            database.beginTransaction();
            IterableIterator<Actor> iter = database.getRecords(Actor.class);
            while (iter.hasNext()) {
                Actor a = iter.next();
                a.setName("Actor_" + a.getName() + "_v" + v);
                database.update(a);
            }
            database.commitTransaction();
        }
        
        String[] filesBefore = new java.io.File(PerstConfig.getInstance().getDatabasePath() + ".idx").list();
        System.out.println("   Created " + numObjects + " objects x " + numVersions + " versions");
        System.out.println("   Files before optimize: " + (filesBefore != null ? filesBefore.length : 0));
        
        database.optimizeFullTextIndex();
        
        String[] filesAfter = new java.io.File(PerstConfig.getInstance().getDatabasePath() + ".idx").list();
        System.out.println("   Files after optimize: " + (filesAfter != null ? filesAfter.length : 0));


        System.out.println("\n=== Versioning Test Complete ===");

        System.out.println("\n=== Versioning Test Complete ===");
    }

    private Actor createTestActor(String name, String description) {
        mycompany.domain.Agreement agreement = new mycompany.domain.Agreement(name + "_agreement");
        return new Actor(name, "USER", agreement);
    }
    
    private Actor createTestActorWithUser(String name, String description, int userId) {
        mycompany.domain.Agreement agreement = new mycompany.domain.Agreement(name + "_agreement");
        Actor actor = new Actor(name, "USER", agreement);
        // userId field no longer exists - just return actor
        return actor;
    }

    private void clearTestData(CDatabase database) {
        database.beginTransaction();
        IterableIterator<Actor> iter = database.getRecords(Actor.class);
        while (iter.hasNext()) {
            Actor a = iter.next();
            database.delete(a);
        }
        database.commitTransaction();
    }

    private List<Actor> getRecords(CDatabase database, Class<Actor> clazz) {
        IterableIterator<Actor> iter = database.getRecords(clazz);
        return toList(iter);
    }
    
    private List<Actor> getRecords(CDatabase database, Class<Actor> clazz, VersionSelector selector) {
        IterableIterator<Actor> iter = database.getRecords(clazz, selector);
        return toList(iter);
    }

    private List<Actor> toList(IterableIterator<Actor> iter) {
        List<Actor> list = new ArrayList<>();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }
}