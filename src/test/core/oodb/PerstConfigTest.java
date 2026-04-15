package oodb;

import koo.oodb.core.PerstConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerstConfig.
 * Tests configuration loading and defaults.
 */
public class PerstConfigTest {

    @Test
    public void testGetInstanceReturnsSingleton() {
        PerstConfig config1 = PerstConfig.getInstance();
        PerstConfig config2 = PerstConfig.getInstance();
        assertSame(config1, config2);
    }

    @Test
    public void testIsPerstEnabledCanBeCalled() {
        // Just verify method doesn't throw - actual value depends on config
        boolean enabled = PerstConfig.getInstance().isPerstEnabled();
        assertNotNull(enabled);
    }

    @Test
    public void testGetDatabasePathCanBeCalled() {
        String path = PerstConfig.getInstance().getDatabasePath();
        assertNotNull(path);
    }

    @Test
    public void testGetPagePoolSizeHasDefault() {
        int size = PerstConfig.getInstance().getPagePoolSize();
        assertTrue(size > 0);
    }
}
