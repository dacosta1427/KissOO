package oodb;

import org.garret.perst.continuous.CDatabase;
import org.garret.perst.continuous.VersionSelector;
import org.garret.perst.IterableIterator;
import mycompany.domain.Actor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        Actor actor1 = createTestActor("John", "Test user for versioning");
        database.insert(actor1);
        String uuid = actor1.getUuid();
        System.out.println("   Created Actor with UUID: " + uuid);
        System.out.println("   Version: " + actor1.getVersion());

        // 4. Update to create v2
        System.out.println("\n4. Updating to create v2...");
        actor1.setName("John Updated");
        Actor actor2 = database.update(actor1);
        System.out.println("   New version: " + actor2.getVersion());
        System.out.println("   UUID preserved: " + actor2.getUuid().equals(uuid));

        // 5. Update again to create v3
        System.out.println("\n5. Updating again to create v3...");
        actor2.setName("John Final");
        Actor actor3 = database.update(actor2);
        System.out.println("   New version: " + actor3.getVersion());

        // 6. Get CURRENT version (should be v3)
        System.out.println("\n6. Testing getRecords with CURRENT selector...");
        List<Actor> currentRecords = getRecords(database, Actor.class, VersionSelector.Kind.CURRENT);
        System.out.println("   Current records count: " + currentRecords.size());
        assertEquals(1, currentRecords.size(), "Should have 1 current record");
        assertEquals("John Final", currentRecords.get(0).getName(), "Current should be v3");
        System.out.println("   Current name: " + currentRecords.get(0).getName());

        // 7. Get ALL versions (should include v1, v2, v3)
        System.out.println("\n7. Testing getRecords with ALL selector...");
        List<Actor> allRecords = getRecords(database, Actor.class, VersionSelector.Kind.ALL);
        System.out.println("   All versions count: " + allRecords.size());
        assertTrue(allRecords.size() >= 3, "Should have at least 3 versions");
        
        // Print all versions
        System.out.println("   All versions:");
        for (Actor a : allRecords) {
            System.out.println("     - name: " + a.getName() + ", version: " + a.getVersion() + ", current: " + a.isCurrent());
        }

        // 8. Verify non-current versions exist
        System.out.println("\n8. Verifying non-current versions exist...");
        long nonCurrentCount = allRecords.stream().filter(a -> !a.isCurrent()).count();
        System.out.println("   Non-current versions: " + nonCurrentCount);
        assertTrue(nonCurrentCount >= 2, "Should have at least 2 historical versions");

        // 9. Test select with query on current
        System.out.println("\n9. Testing select on current versions...");
        IterableIterator<Actor> currentIter = database.select(Actor.class, "name='John Final'");
        List<Actor> selectedCurrent = toList(currentIter);
        System.out.println("   Found: " + selectedCurrent.size() + " current record(s)");
        assertEquals(1, selectedCurrent.size());

        // 10. Verify Lucene index directory exists
        System.out.println("\n10. Verifying Lucene index directory...");
        String dbPath = PerstConfig.getInstance().getDatabasePath();
        java.io.File idxDir = new java.io.File(dbPath + ".idx");
        System.out.println("   Index path: " + idxDir.getAbsolutePath());
        System.out.println("   Index exists: " + idxDir.exists());
        System.out.println("   Is directory: " + (idxDir.exists() && idxDir.isDirectory()));
        
        if (idxDir.exists() && idxDir.isDirectory()) {
            String[] files = idxDir.list();
            System.out.println("   Index files count: " + (files != null ? files.length : 0));
        }

        System.out.println("\n=== Versioning Test Complete ===");
    }

    private Actor createTestActor(String name, String description) {
        Actor actor = new Actor();
        actor.setName(name);
        actor.setUserType("USER");
        actor.setDescription(description);
        return actor;
    }

    private void clearTestData(CDatabase database) {
        IterableIterator<Actor> iter = database.getRecords(Actor.class, VersionSelector.Kind.ALL);
        while (iter.hasNext()) {
            Actor a = iter.next();
            database.delete(a);
        }
    }

    private List<Actor> getRecords(CDatabase database, Class<Actor> clazz, VersionSelector.Kind kind) {
        VersionSelector selector = new VersionSelector(kind);
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