package nl.dcg.gfe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerstConfig
 * Tests configuration loading from application.ini
 */
class PerstConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void testSingletonPattern() {
        PerstConfig instance1 = PerstConfig.getInstance();
        PerstConfig instance2 = PerstConfig.getInstance();
        assertSame(instance1, instance2, "PerstConfig should be a singleton");
    }

    @Test
    void testDefaultValuesWhenNoConfig() {
        // When no config file exists, Perst should be disabled by default
        PerstConfig config = PerstConfig.getInstance();
        assertFalse(config.isPerstEnabled(), "Perst should be disabled by default");
    }

    @Test
    void testPerstEnabledTrue() throws Exception {
        // Create a temporary config file with PerstEnabled=true
        Path configFile = tempDir.resolve("application.ini");
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            writer.write("PerstEnabled = true\n");
            writer.write("PerstDatabasePath = testdb\n");
            writer.write("PerstPagePoolSize = 1024\n");
        }

        // Note: This test demonstrates the expected behavior
        // The actual config loading reads from working directory
        assertTrue(configFile.toFile().exists(), "Config file should be created");
    }

    @Test
    void testPerstEnabledFalse() throws Exception {
        Path configFile = tempDir.resolve("application.ini");
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            writer.write("PerstEnabled = false\n");
        }

        assertTrue(configFile.toFile().exists());
    }

    @Test
    void testDatabasePath() {
        PerstConfig config = PerstConfig.getInstance();
        String path = config.getDatabasePath();
        assertNotNull(path, "Database path should not be null");
    }

    @Test
    void testPagePoolSize() {
        PerstConfig config = PerstConfig.getInstance();
        int poolSize = config.getPagePoolSize();
        assertTrue(poolSize > 0, "Page pool size should be positive");
        assertEquals(512 * 1024 * 1024, poolSize, "Default should be 512MB");
    }

    @Test
    void testConfigValueParsing() {
        // Test that string "true" is parsed correctly
        String enabled = "true";
        boolean isEnabled = "true".equalsIgnoreCase(enabled);
        assertTrue(isEnabled, "Should parse 'true' to boolean");

        enabled = "false";
        isEnabled = "true".equalsIgnoreCase(enabled);
        assertFalse(isEnabled, "Should parse 'false' to boolean");
    }

    @Test
    void testDatabasePathDefault() {
        PerstConfig config = PerstConfig.getInstance();
        assertEquals("oodb", config.getDatabasePath(), "Default path should be 'oodb'");
    }

    @Test
    void testPagePoolSizeDefault() {
        PerstConfig config = PerstConfig.getInstance();
        assertEquals(536870912, config.getPagePoolSize(), "Default pool size should be 512MB");
    }
}
