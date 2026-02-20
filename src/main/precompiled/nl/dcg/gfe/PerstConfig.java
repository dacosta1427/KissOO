package nl.dcg.gfe;

/**
 * GFE Perst Integration - PerstConfig
 * 
 * Configuration class for Perst integration.
 * Reads settings from application.ini
 */
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
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            
            String enabled = props.getProperty("PerstEnabled", "false");
            this.perstEnabled = "true".equalsIgnoreCase(enabled);
            
            this.databasePath = props.getProperty("PerstDatabasePath", "oodb");
            
            String poolSize = props.getProperty("PerstPagePoolSize", "536870912");
            this.pagePoolSize = Integer.parseInt(poolSize);
            
            System.out.println("[PerstConfig] Perst Enabled: " + perstEnabled);
            System.out.println("[PerstConfig] Database Path: " + databasePath);
            
        } catch (java.io.IOException e) {
            System.out.println("[PerstConfig] Config file not found, using defaults. Perst disabled.");
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
