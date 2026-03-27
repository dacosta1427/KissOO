/**
 * Standalone test to reproduce Perst optimistic lock conflict issue.
 * 
 * This test demonstrates that updating a CVersion object immediately after
 * retrieval fails with "Optimistic lock conflict: expected version differs 
 * from current version" even in a fresh database.
 * 
 * Compile and run with perst-dcg-4.0.1.jar on classpath:
 *   javac -cp "libs/perst-dcg-4.0.1.jar" PerstOptimisticLockTest.java
 *   java -cp ".:libs/perst-dcg-4.0.1.jar" PerstOptimisticLockTest
 */

import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.continuous.TransactionContainer;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.dbmanager.UnifiedDBManagerImpl;
import org.garret.perst.dbmanager.StoreResult;
import org.garret.perst.dbmanager.RetrieveResult;

import java.io.File;

public class PerstOptimisticLockTest {
    
    // Simple test entity extending CVersion
    public static class TestEntity extends CVersion {
        private String name;
        private int value;
        
        public TestEntity() {}
        
        public TestEntity(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        
        @Override
        public String toString() {
            return "TestEntity{name='" + name + "', value=" + value + "}";
        }
    }
    
    private static final String DB_PATH = "test_perst_lock.db";
    private static final String INDEX_PATH = "test_perst_lock_index";
    
    public static void main(String[] args) {
        System.out.println("=== Perst 4.0.1 Optimistic Lock Test ===\n");
        
        // Clean up any existing database
        cleanupDatabase();
        
        Storage storage = null;
        UnifiedDBManager dbm = null;
        try {
            // Create and open database (same pattern as PerstStorageManager)
            System.out.println("1. Creating database...");
            storage = StorageFactory.getInstance().createStorage();
            storage.setProperty("perst.serialize.transient.objects", Boolean.FALSE);
            storage.open(DB_PATH, 1000000);
            
            dbm = new UnifiedDBManagerImpl();
            dbm.open(storage, INDEX_PATH);
            
            System.out.println("   Database created successfully");
            
            // Step 1: Insert a new entity
            System.out.println("\n2. Inserting new entity...");
            TestEntity entity = new TestEntity("Original Name", 42);
            
            TransactionContainer tc1 = dbm.createContainer();
            tc1.addInsert(entity);
            StoreResult result1 = dbm.store(tc1);
            
            if (!result1.isSuccess()) {
                System.err.println("   FAILED: Could not insert entity");
                System.err.println("   Error: " + result1.getMessage());
                return;
            }
            
            long oid = entity.getOid();
            System.out.println("   Insert succeeded. OID: " + oid);
            System.out.println("   Entity: " + entity);
            
            // Step 2: Retrieve the entity
            System.out.println("\n3. Retrieving entity by OID...");
            RetrieveResult<TestEntity> rr = dbm.getByOid(oid);
            TestEntity retrieved = (rr != null) ? rr.getObject() : null;
            
            if (retrieved == null) {
                System.err.println("   FAILED: Could not retrieve entity");
                return;
            }
            
            System.out.println("   Retrieved: " + retrieved);
            
            // Step 3: Modify the entity
            System.out.println("\n4. Modifying entity...");
            retrieved.setName("Updated Name");
            retrieved.setValue(100);
            System.out.println("   Modified: " + retrieved);
            
            // Step 4: Try to update - THIS IS WHERE IT FAILS
            System.out.println("\n5. Attempting to update entity...");
            TransactionContainer tc2 = dbm.createContainer();
            tc2.addUpdate(retrieved);
            StoreResult result2 = dbm.store(tc2);
            
            System.out.println("   Update result: " + result2.isSuccess());
            
            if (!result2.isSuccess()) {
                System.err.println("\n=== TEST FAILED ===");
                System.err.println("Error: " + result2.getMessage());
                System.err.println("Optimistic lock conflict occurred!");
            } else {
                System.out.println("\n=== TEST PASSED ===");
            }
            
        } catch (Exception e) {
            System.err.println("\n=== TEST ERROR ===");
            e.printStackTrace();
        } finally {
            if (dbm != null) {
                try { dbm.close(); } catch (Exception e) {}
            }
            if (storage != null) {
                storage.close();
            }
            System.out.println("\nDatabase closed.");
            cleanupDatabase();
        }
    }
    
    private static void cleanupDatabase() {
        String[] files = {DB_PATH, INDEX_PATH, INDEX_PATH + ".lex"};
        for (String f : files) {
            new File(f).delete();
        }
    }
}
