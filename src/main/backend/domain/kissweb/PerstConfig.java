package domain.kissweb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for Perst integration.
 * Reads settings from application.ini
 */
public class PerstConfig {
    private static final String CONFIG_FILE = "WEB-INF/backend/application.ini";
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
        String appPath = org.kissweb.restServer.MainServlet.getApplicationPath();
        String configPath = appPath + "application.ini";
        System.out.println("[PerstConfig] Trying to load config from: " + configPath);
        try (FileInputStream fis = new FileInputStream(configPath)) {
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
    
    public boolean isPerstEnabled() {
        return perstEnabled;
    }
    
    public String getDatabasePath() {
        return databasePath;
    }
    
    public int getPagePoolSize() {
        return pagePoolSize;
    }
}
