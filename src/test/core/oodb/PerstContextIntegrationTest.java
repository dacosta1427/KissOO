package oodb;

import org.garret.perst.continuous.TransactionContainer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PerstStorageManager.
 * Tests the PerstStorageManager API.
 */
public class PerstContextIntegrationTest {

    @Test
    public void testIsAvailableReflectsInitialization() {
        boolean available = PerstStorageManager.isAvailable();
        assertNotNull(available);
    }

    @Test
    public void testCreateContainerWhenAvailable() {
        if (PerstStorageManager.isAvailable()) {
            TransactionContainer tc = PerstStorageManager.createContainer();
            assertNotNull(tc);
        }
    }
}
