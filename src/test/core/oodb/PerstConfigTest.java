package oodb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

public class PerstConfigTest {

    @BeforeEach
    public void setUp() {
        PerstConfig.setInstance(null);
    }

    @AfterEach
    public void tearDown() {
        PerstConfig.setInstance(null);
    }

    @Test
    public void testSingletonPattern() {
        PerstConfig instance1 = PerstConfig.getInstance();
        PerstConfig instance2 = PerstConfig.getInstance();
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }

    @Test
    public void testSetInstance() {
        try {
            Constructor<PerstConfig> constructor = PerstConfig.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            PerstConfig mockConfig = constructor.newInstance();
            PerstConfig.setInstance(mockConfig);
            assertSame(mockConfig, PerstConfig.getInstance(), "setInstance should change the instance");
        } catch (Exception e) {
            fail("Failed to create test instance: " + e.getMessage());
        }
    }

    @Test
    public void testDefaultValues() {
        try {
            Constructor<PerstConfig> constructor = PerstConfig.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            PerstConfig config = constructor.newInstance();
            
            Field fieldEnabled = PerstConfig.class.getDeclaredField("perstEnabled");
            fieldEnabled.setAccessible(true);
            assertFalse(fieldEnabled.getBoolean(config), "Default perstEnabled should be false");
            
            Field fieldCDatabase = PerstConfig.class.getDeclaredField("useCDatabase");
            fieldCDatabase.setAccessible(true);
            assertTrue(fieldCDatabase.getBoolean(config), "Default useCDatabase should be true");
            
            Field fieldPath = PerstConfig.class.getDeclaredField("databasePath");
            fieldPath.setAccessible(true);
            assertEquals("oodb", fieldPath.get(config), "Default database path should be 'oodb'");
            
            Field fieldPool = PerstConfig.class.getDeclaredField("pagePoolSize");
            fieldPool.setAccessible(true);
            assertEquals(512 * 1024 * 1024, fieldPool.getInt(config), "Default page pool size should be 512MB");
        } catch (Exception e) {
            fail("Failed to test default values: " + e.getMessage());
        }
    }

    @Test
    public void testGetInstanceCreatesNewInstanceWhenNull() {
        PerstConfig.setInstance(null);
        assertNotNull(PerstConfig.getInstance(), "getInstance should create new instance if null");
    }

    @Test
    public void testSetInstanceAllowsNull() {
        PerstConfig.setInstance(null);
        assertNotNull(PerstConfig.getInstance(), "getInstance should create new instance if null");
    }
}
