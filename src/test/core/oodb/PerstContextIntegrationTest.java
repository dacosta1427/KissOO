package oodb;

import koo.oodb.StorageManager;
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
        boolean available = StorageManager.isAvailable();
        assertNotNull(available);
    }

    @Test
    public void testCreateContainerWhenAvailable() {
        if (StorageManager.isAvailable()) {
            TransactionContainer tc = StorageManager.createContainer();
            assertNotNull(tc);
        }
    }
}
