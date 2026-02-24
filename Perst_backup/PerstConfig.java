package nl.dcg.gfe;

import lombok.Getter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for Perst integration.
 * Reads settings from application.ini
 */
@Getter
public class PerstConfig {
    private static final String CONFIG_FILE = "application.ini";
    private static PerstConfig instance;
    
    private boolean perstEnabled = false;
    private String databasePath = "oodb";
    private int pagePoolSize = 512 * 1024 * 1024; // 512MB default
    
    private PerstConfig() {
        loadConfig();
    }
    
    public static synchronized PerstConfig getInstance() {
        if (instance == null) {
            instance = new PerstConfig();
        }
        return instance;
    }
    
    private void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            
            this.perstEnabled = "true".equalsIgnoreCase(props.getProperty("PerstEnabled", "false"));
            this.databasePath = props.getProperty("PerstDatabasePath", "oodb");
            this.pagePoolSize = Integer.parseInt(props.getProperty("PerstPagePoolSize", "536870912"));
            
            System.out.println("[PerstConfig] Perst Enabled: " + perstEnabled);
            System.out.println("[PerstConfig] Database Path: " + databasePath);
            
        } catch (IOException e) {
            System.out.println("[PerstConfig] Config file not found, using defaults. Perst disabled.");
        } catch (NumberFormatException e) {
            System.out.println("[PerstConfig] Invalid page pool size, using default: " + e.getMessage());
        }
    }
}
