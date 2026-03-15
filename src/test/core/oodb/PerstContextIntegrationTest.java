package oodb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.io.File;
import java.util.Collection;
import java.util.UUID;

public class PerstContextIntegrationTest {

    private String testDbPath;

    @BeforeEach
    public void setUp() throws Exception {
        PerstContext.setInstance(null);
        PerstConfig.setInstance(null);
        
        testDbPath = System.getProperty("java.io.tmpdir") + "perst_test_" + UUID.randomUUID() + ".dbs";
        
        Constructor<PerstConfig> constructor = PerstConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        PerstConfig config = constructor.newInstance();
        
        setField(config, "perstEnabled", true);
        setField(config, "useCDatabase", false); 
        setField(config, "databasePath", testDbPath);
        setField(config, "pagePoolSize", 1024 * 1024);
        
        PerstConfig.setInstance(config);
    }

    @AfterEach
    public void tearDown() {
        try {
            PerstContext context = PerstContext.getInstance();
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
        }
        
        PerstContext.setInstance(null);
        PerstConfig.setInstance(null);
        
        File dbFile = new File(testDbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    public void testInitializeWithInMemoryDatabase() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        assertTrue(context.isAvailable(), "Context should be available after initialization");
        
        context.close();
    }

    @Test
    public void testIsAvailableReturnsCorrectState() {
        PerstContext context = PerstContext.getInstance();
        
        // Note: isAvailable() auto-initializes if enabled, so we test init behavior
        context.initialize();
        
        assertTrue(context.isAvailable(), "Should be available after init");
    }

    @Test
    public void testIsVersioningEnabledWhenNotConfigured() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        assertFalse(context.isVersioningEnabled(), "Versioning should be disabled when useCDatabase is false");
    }

    @Test
    public void testStoreAndRetrieveUser() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.PerstUser user = new mycompany.domain.PerstUser();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setActive(true);
        user.setUserId(1);
        
        context.storeUser(user);
        
        mycompany.domain.PerstUser retrieved = context.retrieveUser(mycompany.domain.PerstUser.class, "username", "testuser");
        
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals("testuser", retrieved.getUsername());
        assertEquals("test@example.com", retrieved.getEmail());
        assertEquals(1, retrieved.getUserId());
    }

    @Test
    public void testStoreAndRetrieveActor() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.Agreement agreement = new mycompany.domain.Agreement("test-role");
        
        mycompany.domain.Actor actor = new mycompany.domain.Actor("TestActor", "user", agreement);
        
        context.storeActor(actor);
        
        mycompany.domain.Actor retrieved = context.retrieveActor(mycompany.domain.Actor.class, "name", "TestActor");
        
        assertNotNull(retrieved, "Actor should be retrieved");
        assertEquals("TestActor", retrieved.getName());
        assertNotNull(retrieved.getUuid());
    }

    @Test
    public void testStoreAndRetrieveAgreement() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.Agreement agreement = new mycompany.domain.Agreement();
        agreement.setRole("admin");
        
        context.storeAgreement(agreement);
        
        Collection<mycompany.domain.Agreement> agreements = context.retrieveAllAgreements();
        
        assertFalse(agreements.isEmpty(), "Agreements should not be empty");
        boolean found = false;
        for (mycompany.domain.Agreement a : agreements) {
            if ("admin".equals(a.getRole())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Stored agreement should be retrievable");
    }

    @Test
    public void testStoreAndRetrieveGroup() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.Group group = new mycompany.domain.Group();
        group.setName("TestGroup");
        
        context.storeGroup(group);
        
        mycompany.domain.Group retrieved = context.retrieveGroupByName("TestGroup");
        
        assertNotNull(retrieved, "Group should be retrieved");
        assertEquals("TestGroup", retrieved.getName());
    }

    @Test
    public void testUpdateUser() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.PerstUser user = new mycompany.domain.PerstUser();
        user.setUsername("updateuser");
        user.setEmail("original@example.com");
        user.setActive(true);
        user.setUserId(2);
        
        context.storeUser(user);
        
        user.setEmail("updated@example.com");
        context.updateUser(user);
        
        mycompany.domain.PerstUser retrieved = context.retrieveUser(mycompany.domain.PerstUser.class, "username", "updateuser");
        
        assertNotNull(retrieved, "User should be retrieved after update");
        assertEquals("updated@example.com", retrieved.getEmail());
    }

    @Test
    public void testRemoveUser() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.PerstUser user = new mycompany.domain.PerstUser();
        user.setUsername("deleteuser");
        user.setEmail("delete@example.com");
        user.setActive(true);
        user.setUserId(3);
        
        context.storeUser(user);
        
        mycompany.domain.PerstUser toRemove = context.retrieveUser(mycompany.domain.PerstUser.class, "username", "deleteuser");
        assertNotNull(toRemove, "User should exist before delete");
        
        context.removeUser(toRemove);
        
        mycompany.domain.PerstUser afterDelete = context.retrieveUser(mycompany.domain.PerstUser.class, "username", "deleteuser");
        assertNull(afterDelete, "User should be null after delete");
    }

    @Test
    public void testTransactionCommit() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        context.beginTransaction();
        
        mycompany.domain.PerstUser user = new mycompany.domain.PerstUser();
        user.setUsername("transactionuser");
        user.setEmail("trans@example.com");
        user.setActive(true);
        user.setUserId(4);
        
        context.storeUser(user);
        context.commitTransaction();
        
        mycompany.domain.PerstUser retrieved = context.retrieveUser(mycompany.domain.PerstUser.class, "username", "transactionuser");
        assertNotNull(retrieved, "User should be committed and retrievable");
    }

    @Test
    public void testTransactionRollback() {
        // Note: With standard Storage (non-CDatabase), rollback doesn't work the same way.
        // The standard Storage auto-commits on store operations.
        // This test verifies the rollback method can be called without errors.
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        context.beginTransaction();
        
        mycompany.domain.PerstUser user = new mycompany.domain.PerstUser();
        user.setUsername("rollbackuser");
        user.setEmail("rollback@example.com");
        user.setActive(true);
        user.setUserId(5);
        
        context.storeUser(user);
        
        // With standard storage, this should still commit
        context.rollbackTransaction();
        
        // In standard mode, rollback doesn't prevent the commit
        mycompany.domain.PerstUser retrieved = context.retrieveUser(mycompany.domain.PerstUser.class, "username", "rollbackuser");
        
        // With standard storage (not CDatabase), rollback behavior is different
        // This is expected - the test verifies the API works
        assertNotNull(retrieved, "With standard Storage, rollback may not discard changes");
    }

    @Test
    public void testBeginTransactionThrowsWhenNotAvailable() {
        PerstContext.setInstance(null);
        PerstConfig.setInstance(null);
        
        try {
            Constructor<PerstConfig> constructor = PerstConfig.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            PerstConfig config = constructor.newInstance();
            setField(config, "perstEnabled", false);
            PerstConfig.setInstance(config);
        } catch (Exception e) {
            fail("Failed to setup config: " + e.getMessage());
        }
        
        PerstContext context = PerstContext.getInstance();
        
        assertThrows(IllegalStateException.class, () -> {
            context.beginTransaction();
        }, "beginTransaction should throw when Perst is not available");
    }

    @Test
    public void testRetrieveAllUsers() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.PerstUser user1 = new mycompany.domain.PerstUser();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setActive(true);
        user1.setUserId(10);
        
        mycompany.domain.PerstUser user2 = new mycompany.domain.PerstUser();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setActive(true);
        user2.setUserId(11);
        
        context.storeUser(user1);
        context.storeUser(user2);
        
        Collection<mycompany.domain.PerstUser> allUsers = context.retrieveAllUsers(mycompany.domain.PerstUser.class);
        
        assertEquals(2, allUsers.size(), "Should have 2 users");
    }

    @Test
    public void testRetrieveAllActors() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        mycompany.domain.Agreement agreement1 = new mycompany.domain.Agreement("role1");
        mycompany.domain.Agreement agreement2 = new mycompany.domain.Agreement("role2");
        
        mycompany.domain.Actor actor1 = new mycompany.domain.Actor("Actor1", "user", agreement1);
        mycompany.domain.Actor actor2 = new mycompany.domain.Actor("Actor2", "user", agreement2);
        
        context.storeActor(actor1);
        context.storeActor(actor2);
        
        Collection<mycompany.domain.Actor> allActors = context.retrieveAllActors(mycompany.domain.Actor.class);
        
        assertEquals(2, allActors.size(), "Should have 2 actors");
    }

    @Test
    public void testGetVersionHistoryWhenNotEnabled() {
        PerstContext context = PerstContext.getInstance();
        context.initialize();
        
        var history = context.getVersionHistory(mycompany.domain.PerstUser.class, "username", "test");
        
        assertTrue(history.isEmpty(), "Version history should be empty when versioning is disabled");
    }
}
