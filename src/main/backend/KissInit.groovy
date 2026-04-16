import koo.oodb.core.actor.ActorType
import koo.oodb.core.actor.Agreement
import koo.oodb.core.actor.Role
import koo.oodb.core.actor.Administrator
import koo.oodb.core.actor.AdministratorRole
import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import koo.oodb.core.PerstConfig
import koo.oodb.core.StorageManager
import koo.oodb.core.PerstConnection
import koo.oodb.core.user.PerstUserManager
import koo.oodb.core.user.PerstUser
import mycompany.actor.cleaner.Cleaner
import koo.security.PasswordSecurity
import java.util.function.Consumer

class KissInit {

    /**
     * Configure the system.
     */
    static void init() {
        println "[KissInit] init() CALLED"
        
        MainServlet.readIniFile "application.ini", "main"
        MainServlet.readIniFile "application.ini", "PasswordSecurity"
        
        // EmailService is initialized on first use (lazy init)
        // EmailService.groovy reads application.ini directly via initializeFromConfig()

        // Example of how to specify a method that is allowed without authentication
        // MainServlet.allowWithoutAuthentication("services.MyGroovyService", "addNumbers")

        println "[KissInit] init() - After readIniFile"
        
        // Initialize Perst HERE - before init2() which might not be called
        // This uses MainServlet.putEnvironment() as suggested by KISS creator
        println "[KissInit] init() - Checking Perst config..."
        println "[KissInit] init() - PerstEnabled=" + PerstConfig.getInstance().isPerstEnabled()
        
        // Step 1: Initialize Perst if needed
        if (PerstConfig.getInstance().isPerstEnabled() && !StorageManager.isAvailable()) {
            println "[KissInit] init() - Initializing Perst NOW..."
            try {
                StorageManager.initialize()
                println "[KissInit] init() - Perst initialized, isAvailable=" + StorageManager.isAvailable()
            } catch (Exception e) {
                println "[KissInit] ERROR during Perst init: " + e.message
                e.printStackTrace()
            }
        }
        
        // Step 2: Register PerstConnection ALWAYS when Perst is enabled and available
        // This runs even if Perst was already initialized from a previous startup
        if (PerstConfig.getInstance().isPerstEnabled() && StorageManager.isAvailable()) {
            try {
                // Check if already registered
                def existing = MainServlet.getEnvironment("NonSqlConnection")
                if (existing == null) {
                    def perstConn = new PerstConnection()
                    MainServlet.putEnvironment("NonSqlConnection", perstConn)
                    MainServlet.putEnvironment("PerstConnection", perstConn)
                    println "[KissInit] init() - PerstConnection registered as NonSqlConnection"
                    
                    // Initialize default admin user if none exists (only on first registration)
                    initDefaultUser()
                    indexPerstUsers()
                    indexActors()
                } else {
                    println "[KissInit] init() - NonSqlConnection already registered"
                }
            } catch (Exception e) {
                println "[KissInit] WARNING: Could not create PerstConnection: " + e.message
                e.printStackTrace()
            }
        }
        
        // Allow Perst-based login without authentication (required - can't log in otherwise!)
        MainServlet.allowWithoutAuthentication("", "Login")
        
        // Allow koo.services.Login (for clients calling koo.services.Login.Login)
        MainServlet.allowWithoutAuthentication("services/Login", "Login")
        
        // Allow user creation without authentication (for first-time setup)
        MainServlet.allowWithoutAuthentication("services/Users", "addRecord")
        
        // Allow signup without authentication
        MainServlet.allowWithoutAuthentication("services.auth.AuthService", "signup")
        
        // Allow activation services (requires valid session but no fully activated check)
        MainServlet.allowWithoutAuthentication("services.auth.AuthService", "changePassword")
        MainServlet.allowWithoutAuthentication("services.auth.AuthService", "sendVerificationEmail")
        MainServlet.allowWithoutAuthentication("services.auth.AuthService", "verifyEmail")
        MainServlet.allowWithoutAuthentication("services.auth.AuthService", "getActivationStatus")
        
        println "[KissInit] init() COMPLETED"
        
        // Set up a global logout handler that runs whenever any user logs out
        // This can be used for cleanup tasks like logging, closing resources, etc.
        UserCache.setLogoutHandler({ UserData ud ->
            // Example: Log the logout event
            println "User ${ud.getUsername()} (ID: ${ud.getUserId()}) is logging out"

            // Add any custom cleanup code here
            // Examples:
            // - Close user-specific resources
            // - Update database logout timestamp
            // - Send notifications
            // - Clean up temporary files
        } as Consumer<UserData>)

    }

    /**
     * Code to run once the database is open but before the app is running.
     * Note: No SQL database is configured - Perst is accessed via MainServlet environment.
     */
    static void init2(Connection db) {
        // If you use db, make sure you commit.
        if (!PasswordSecurity.initialise()) System.out.println("! X X X PasswordSecurity NOT initialised!");
        System.out.println("* * * PasswordSecurity initialised!");

        try {
            println "[KissInit] init2() CALLED"
            println "[KissInit] db = " + db
            
            // Initialize Perst database at startup via PerstStorageManager
            // This uses MainServlet.putEnvironment() as suggested by KISS creator
            println "[KissInit] Checking Perst config: enabled=" + PerstConfig.getInstance().isPerstEnabled()
            println "[KissInit] Database path: " + PerstConfig.getInstance().getDatabasePath()
            
            if (PerstConfig.getInstance().isPerstEnabled() && !StorageManager.isAvailable()) {
                println "[KissInit] Initializing Perst database via PerstStorageManager..."
                StorageManager.initialize()
                println "[KissInit] Perst initialized, isAvailable=" + StorageManager.isAvailable()
            } else {
                println "[KissInit] Perst is NOT enabled - check application.ini"
            }

            // Initialize default admin user if none exists
            if (StorageManager.isAvailable()) {
                initDefaultUser()
                indexPerstUsers()
                indexActors()
            } else {
                println "[KissInit] WARNING: Perst not available, skipping user init"
            }
            
            println "[KissInit] init2() COMPLETED"
        } catch (Exception e) {
            println "[KissInit] ERROR in init2: " + e.message
            e.printStackTrace()
        }
    }
    
    /**
     * Initialize default admin users if no users exist.
     * Creates both sysadmin (SUPER_ADMIN) and admin (ADMIN) with forced
     * password change and email verification required on first login.
     */
    private static void initDefaultUser() {
        // User creation temporarily disabled - needs investigation
        // The Owner/Administrator constructors cause ExceptionInInitializerError at runtime
        println "[KissInit] initDefaultUser() - DISABLED (see ExceptionInInitializerError)"
    }
    
    /**
     * Index all PerstUsers for fast lookup
     */
    private static void indexPerstUsers() {
        println "[KissInit] Skipping PerstUser indexing (not required for CDatabase)"
    }
    
    /**
     * Index all Actors for fast lookup
     */
    private static void indexActors() {
        println "[KissInit] Skipping AActor indexing (not required for CDatabase)"
    }
    
}
