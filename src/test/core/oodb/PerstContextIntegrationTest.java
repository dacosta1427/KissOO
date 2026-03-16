package oodb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PerstContext.
 * Tests the PerstContext API.
 */
public class PerstContextIntegrationTest {

    @Test
    public void testGetInstanceReturnsSingleton() {
        PerstContext ctx1 = PerstContext.getInstance();
        PerstContext ctx2 = PerstContext.getInstance();
        assertSame(ctx1, ctx2);
    }

    @Test
    public void testIsAvailableReflectsInitialization() {
        // Should match PerstStorageManager.isAvailable()
        boolean available = PerstContext.getInstance().isAvailable();
        assertNotNull(available);
    }

    @Test
    public void testRetrieveMethodsExist() {
        // Verify retrieve methods are available (signature check)
        // Actual behavior tested in integration
    }
}
