import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import oodb.PerstConfig
import oodb.PerstStorageManager
import oodb.PerstConnection
import mycompany.database.PerstUserManager
import mycompany.domain.PerstUser
import mycompany.domain.Actor
import mycompany.domain.Cleaner
import mycompany.domain.House
import mycompany.domain.Booking
import mycompany.domain.Schedule
import mycompany.domain.Owner
import mycompany.domain.CostProfile
import mycompany.domain.Agreement
import mycompany.domain.Phone
import mycompany.domain.Group
import mycompany.domain.BenchmarkData
import com.mycompany.security.PasswordSecurity
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
        if (PerstConfig.getInstance().isPerstEnabled() && !PerstStorageManager.isAvailable()) {
            println "[KissInit] init() - Initializing Perst NOW..."
            try {
                PerstStorageManager.initialize()
                println "[KissInit] init() - Perst initialized, isAvailable=" + PerstStorageManager.isAvailable()
            } catch (Exception e) {
                println "[KissInit] ERROR during Perst init: " + e.message
                e.printStackTrace()
            }
        }
        
        // Step 2: Register PerstConnection ALWAYS when Perst is enabled and available
        // This runs even if Perst was already initialized from a previous startup
        if (PerstConfig.getInstance().isPerstEnabled() && PerstStorageManager.isAvailable()) {
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
        
        // Allow services.Login (for clients calling services.Login.Login)
        MainServlet.allowWithoutAuthentication("services/Login", "Login")
        
        // Allow user creation without authentication (for first-time setup)
        MainServlet.allowWithoutAuthentication("services/Users", "addRecord")
        
        // Allow signup without authentication
        MainServlet.allowWithoutAuthentication("services.AuthService", "signup")
        
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
            
            if (PerstConfig.getInstance().isPerstEnabled() && !PerstStorageManager.isAvailable()) {
                println "[KissInit] Initializing Perst database via PerstStorageManager..."
                PerstStorageManager.initialize()
                println "[KissInit] Perst initialized, isAvailable=" + PerstStorageManager.isAvailable()
            } else {
                println "[KissInit] Perst is NOT enabled - check application.ini"
            }

            // Initialize default admin user if none exists
            if (PerstStorageManager.isAvailable()) {
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
     * Initialize default admin user if no users exist
     */
    private static void initDefaultUser() {
        try {
            def users = PerstStorageManager.getAll(PerstUser.class)
            if (!users || users.size() == 0) {
                println "[KissInit] Creating default superAdmin user..."
                
                // Create superAdmin Actor with full Agreement
                def agreement = new mycompany.domain.Agreement("superAdmin")
                def adminActor = new mycompany.domain.Actor("System Admin", "superAdmin", agreement, mycompany.domain.ActorType.NATURAL)
                
                // Actor constructor already created a deactivated PerstUser
                // Configure it with admin credentials
                def adminUser = adminActor.getPerstUser()
                adminUser.setUsername("admin")
                adminUser.setPassword("admin")
                adminUser.setEmail("admin@localhost")
                adminUser.setActive(true)
                adminUser.setEmailVerified(true)
                
                // Store both together
                def tc = PerstStorageManager.createContainer()
                tc.addInsert(adminActor)
                tc.addInsert(adminUser)
                if (PerstStorageManager.store(tc)) {
                    println "[KissInit] Default superAdmin user created. CHANGE PASSWORD IMMEDIATELY!"
                } else {
                    println "[KissInit] ERROR: Failed to create admin user"
                }
            } else {
                println "[KissInit] Users already exist (${users.size()}), checking admin user..."
                // Ensure admin user has emailVerified = true
                def admin = PerstUserManager.getByKey("admin")
                if (admin != null && !admin.isEmailVerified()) {
                    admin.setEmailVerified(true)
                    PerstUserManager.update(admin)
                    println "[KissInit] Admin user emailVerified set to true"
                }
                // Ensure all users have emailVerified = true (fix for existing users)
                def updated = 0
                users.each { user ->
                    if (!user.isEmailVerified()) {
                        user.setEmailVerified(true)
                        PerstUserManager.update(user)
                        updated++
                    }
                }
                if (updated > 0) {
                    println "[KissInit] Set emailVerified=true for ${updated} users"
                }
            }
        } catch (Exception e) {
            println "[KissInit] Error creating default user: ${e.message}"
            e.printStackTrace()
        }
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
        println "[KissInit] Skipping Actor indexing (not required for CDatabase)"
    }
    
}
