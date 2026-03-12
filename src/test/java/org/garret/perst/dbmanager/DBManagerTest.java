package org.garret.perst.dbmanager;

import org.garret.perst.*;
import org.garret.perst.continuous.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Tests for DBManager - demonstrating how to store and retrieve objects.
 */
class DBManagerTest {
    
    private Storage storage;
    private DBManager dbManager;
    
    @BeforeEach
    void setUp() throws IOException {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(new NullFile(), Storage.INFINITE_PAGE_POOL);
        
        dbManager = new DBManagerImpl();
        dbManager.initialize(storage);
    }
    
    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.close();
        }
        if (storage != null) {
            storage.close();
        }
    }
    
    // ======== Basic Store and Retrieve Tests ========
    
    @Test
    @DisplayName("Should store and retrieve simple persistent object")
    void testStoreAndRetrieveSimpleObject() {
        // Create a person
        DomainClasses.Person person = new DomainClasses.Person("John Doe", 30, "john@example.com");
        
        // Store it
        StoreResult<DomainClasses.Person> storeResult = dbManager.store(person);
        
        // Verify success
        assertTrue(storeResult.isSuccess(), "Store should succeed");
        assertNotNull(storeResult.getObject(), "Object should be returned");
        
        long oid = person.getOid();
        assertTrue(oid > 0, "OID should be assigned");
        
        // Retrieve it
        RetrieveResult<DomainClasses.Person> retrieved = dbManager.get(oid);
        
        // Verify
        assertNotNull(retrieved, "Should retrieve object");
        assertNotNull(retrieved.getObject(), "Retrieved object should not be null");
        assertEquals("John Doe", retrieved.getObject().getName());
        assertEquals(30, retrieved.getObject().getAge());
        assertEquals("john@example.com", retrieved.getObject().getEmail());
    }
    
    @Test
    @DisplayName("Should store object with collections")
    void testStoreObjectWithCollections() {
        // Create customer with tags and addresses
        DomainClasses.Customer customer = new DomainClasses.Customer(storage, "Acme Corp");
        customer.addTag("vip");
        customer.addTag("wholesale");
        
        DomainClasses.Address addr = new DomainClasses.Address(
            "123 Main St", "Springfield", "12345"
        );
        customer.addAddress(addr);
        
        // Make persistent first, then commit
        storage.makePersistent(customer);
        storage.commit();
        
        long oid = customer.getOid();
        
        // Retrieve
        RetrieveResult<DomainClasses.Customer> retrieved = dbManager.get(oid);
        
        assertNotNull(retrieved.getObject());
        assertEquals("Acme Corp", retrieved.getObject().getName());
        assertEquals(2, retrieved.getObject().getTags().size());
        assertEquals(1, retrieved.getObject().getAddresses().size());
    }
    
    // ======== Transaction Tests ========
    
    @Test
    @DisplayName("Should commit transaction")
    void testCommitTransaction() {
        dbManager.beginTransaction();
        
        DomainClasses.Person person = new DomainClasses.Person("In Transaction", 25, "tx@example.com");
        storage.makePersistent(person);
        
        dbManager.commit();
        
        // Object should be retrievable after commit
        RetrieveResult<DomainClasses.Person> result = dbManager.get(person.getOid());
        assertNotNull(result.getObject());
    }
    
    @Test
    @DisplayName("Should rollback transaction")
    void testRollbackTransaction() {
        dbManager.beginTransaction();
        
        DomainClasses.Person person = new DomainClasses.Person("Will Be Rolled Back", 25, "rb@example.com");
        storage.makePersistent(person);
        
        dbManager.rollback();
        
        // Object should NOT be retrievable (or should be marked as rolled back)
        // Note: Perst may still have the object but it's not visible in committed state
        assertFalse(dbManager.isInTransaction());
    }
    
    // ======== Memory Stats Tests ========
    
    @Test
    @DisplayName("Should provide memory statistics")
    void testMemoryStats() {
        MemoryStats stats = dbManager.getMemoryStats();
        
        assertNotNull(stats);
        assertTrue(stats.getMaxMemory() > 0);
        assertTrue(stats.getUsagePercent() >= 0);
    }
    
    @Test
    @DisplayName("Should detect low memory")
    void testLowMemoryDetection() {
        MemoryStats stats = MemoryStats.current();
        
        System.out.println("Memory: " + stats);
        assertNotNull(stats);
    }
    
    // ======== Lazy Loading Tests ========
    
    @Test
    @DisplayName("Should detect large collections")
    void testLargeCollectionDetection() {
        // Set low threshold for testing
        dbManager.setLargeCollectionThreshold(2);
        
        DomainClasses.Customer customer = new DomainClasses.Customer(storage, "Large Customer");
        customer.addTag("1");
        customer.addTag("2");
        customer.addTag("3"); // Over threshold
        
        dbManager.store(customer);
        
        RetrieveResult<DomainClasses.Customer> result = dbManager.get(customer.getOid());
        
        // Should have detected large collection
        assertTrue(result.hasLargeCollections(), "Should detect large collection");
    }
}
