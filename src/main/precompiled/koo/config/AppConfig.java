package koo.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AppConfig - Utility to read configuration from application.ini
 */
public class AppConfig {
    
    private static Map<String, String> config = new HashMap<>();
    private static boolean initialized = false;
    
    private static final String CONFIG_PATH = "src/main/backend/application.ini";
    
    /**
     * Initialize config from application.ini
     */
    public static synchronized void initialize() {
        if (initialized) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    config.put(key, value);
                }
            }
            initialized = true;
        } catch (IOException e) {
            System.err.println("[AppConfig] Failed to load config: " + e.getMessage());
        }
    }
    
    /**
     * Get config value by key
     */
    public static String get(String key) {
        if (!initialized) {
            initialize();
        }
        return config.get(key);
    }
    
    /**
     * Get config value by key with default
     */
    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }
    
    /**
     * Get config value as integer
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String val = get(key);
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get config value as boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            String val = get(key);
            return val != null ? Boolean.parseBoolean(val) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}