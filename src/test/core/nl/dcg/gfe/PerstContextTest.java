package nl.dcg.gfe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerstContext
 * Tests the singleton pattern and availability checks
 */
class PerstContextTest {

    @Test
    void testSingletonPattern() {
        PerstContext instance1 = PerstContext.getInstance();
        PerstContext instance2 = PerstContext.getInstance();
        assertSame(instance1, instance2, "PerstContext should be a singleton");
    }

    @Test
    void testIsAvailableWhenPerstDisabled() {
        // By default Perst is disabled, so isAvailable should return false
        PerstContext context = PerstContext.getInstance();
        assertFalse(context.isAvailable(), "Perst should not be available when disabled");
    }

    @Test
    void testInitializeWhenDisabled() {
        PerstContext context = PerstContext.getInstance();
        
        // When Perst is disabled, initialize should not actually initialize
        context.initialize();
        
        // Should still return false because PerstEnabled is false
        assertFalse(context.isAvailable(), "Should not be available after initialize when disabled");
    }

    @Test
    void testCloseWhenNotInitialized() {
        PerstContext context = PerstContext.getInstance();
        
        // Should not throw when closing without initialization
        assertDoesNotThrow(() -> context.close(), "Close should not throw when not initialized");
    }

    @Test
    void testRetrieveObjectThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        
        // Should throw IllegalStateException when Perst is not available
        assertThrows(IllegalStateException.class, () -> {
            context.retrieveObject(Actor.class, "test-uuid");
        }, "Should throw when Perst not available");
    }

    @Test
    void testRetrieveAllObjectsThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        
        assertThrows(IllegalStateException.class, () -> {
            context.retrieveAllObjects(Actor.class);
        }, "Should throw when Perst not available");
    }

    @Test
    void testStoreNewObjectThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        Actor actor = new Actor("Test", "TEST");
        
        assertThrows(IllegalStateException.class, () -> {
            context.storeNewObject(actor);
        }, "Should throw when Perst not available");
    }

    @Test
    void testStoreModifiedObjectThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        Actor actor = new Actor("Test", "TEST");
        
        assertThrows(IllegalStateException.class, () -> {
            context.storeModifiedObject(actor);
        }, "Should throw when Perst not available");
    }

    @Test
    void testRemoveObjectThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        Actor actor = new Actor("Test", "TEST");
        
        assertThrows(IllegalStateException.class, () -> {
            context.removeObject(actor);
        }, "Should throw when Perst not available");
    }

    @Test
    void testStartTransactionThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        
        assertThrows(IllegalStateException.class, () -> {
            context.startTransaction();
        }, "Should throw when Perst not available");
    }

    @Test
    void testEndTransactionThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        
        assertThrows(IllegalStateException.class, () -> {
            context.endTransaction();
        }, "Should throw when Perst not available");
    }

    @Test
    void testRollbackTransactionThrowsWhenNotAvailable() {
        PerstContext context = PerstContext.getInstance();
        
        assertThrows(IllegalStateException.class, () -> {
            context.rollbackTransaction();
        }, "Should throw when Perst not available");
    }

    @Test
    @Disabled("Requires actual Perst initialization - for integration testing only")
    void testFullLifecycleWithPerst() {
        // This test would require Perst to be enabled and initialized
        // Marked as @Disabled - can be run in integration testing phase
        fail("This is an integration test placeholder");
    }
}
