package oodb;

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
    private boolean useCDatabase = true;  // Use CDatabase for versioning
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
    
    public static synchronized void setInstance(PerstConfig testInstance) {
        instance = testInstance;
    }
    
    private void loadConfig() {
        Properties props = new Properties();
        String appPath = null;
        try {
            appPath = org.kissweb.restServer.MainServlet.getApplicationPath();
        } catch (Exception e) {
            System.out.println("[PerstConfig] Cannot get application path (not in web container), using defaults.");
        }
        
        String configPath = (appPath != null) ? appPath + "application.ini" : "application.ini";
        System.out.println("[PerstConfig] Trying to load config from: " + configPath);
        
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            
            this.perstEnabled = "true".equalsIgnoreCase(props.getProperty("PerstEnabled", "false"));
            this.useCDatabase = "true".equalsIgnoreCase(props.getProperty("PerstUseCDatabase", "true"));
            this.databasePath = props.getProperty("PerstDatabasePath", "oodb");
            this.pagePoolSize = Integer.parseInt(props.getProperty("PerstPagePoolSize", "536870912"));
            
            System.out.println("[PerstConfig] Perst Enabled: " + perstEnabled);
            System.out.println("[PerstConfig] Use CDatabase: " + useCDatabase);
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
    
    public boolean isUseCDatabase() {
        return useCDatabase;
    }
    
    public String getDatabasePath() {
        return databasePath;
    }
    
    public int getPagePoolSize() {
        return pagePoolSize;
    }
}
