import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import oodb.PerstConfig
import oodb.PerstStorageManager
import mycompany.domain.PerstUser
import mycompany.domain.Actor
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

        // Example of how to specify a method that is allowed without authentication
        // MainServlet.allowWithoutAuthentication("services.MyGroovyService", "addNumbers")

        println "[KissInit] init() - After readIniFile"
        
        // Initialize Perst HERE - before init2() which might not be called
        // This uses MainServlet.putEnvironment() as suggested by KISS creator
        println "[KissInit] init() - Checking Perst config..."
        println "[KissInit] init() - PerstEnabled=" + PerstConfig.getInstance().isPerstEnabled()
        
        if (PerstConfig.getInstance().isPerstEnabled()) {
            println "[KissInit] init() - Initializing Perst NOW..."
            try {
                PerstStorageManager.initialize()
                println "[KissInit] init() - Perst initialized, isAvailable=" + PerstStorageManager.isAvailable()
                
                // Initialize default admin user if none exists
                if (PerstStorageManager.isAvailable()) {
                    initDefaultUser()
                    indexPerstUsers()
                    indexActors()
                }
            } catch (Exception e) {
                println "[KissInit] ERROR during Perst init: " + e.message
                e.printStackTrace()
            }
        }
        
        // Allow Perst-based login without authentication (required - can't log in otherwise!)
        MainServlet.allowWithoutAuthentication("", "Login")
        
        // Allow user creation without authentication (for first-time setup)
        MainServlet.allowWithoutAuthentication("services.Users", "addRecord")
        
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
            
            if (PerstConfig.getInstance().isPerstEnabled()) {
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
                println "[KissInit] Creating default admin user..."
                def admin = new PerstUser("admin", "admin", 1)
                admin.setEmail("admin@localhost")
                admin.setActive(true)
                admin.index()
                
                // Use proper transaction handling
                PerstStorageManager.beginTransaction()
                PerstStorageManager.save(admin)
                PerstStorageManager.commitTransaction()
                
                println "[KissInit] Default admin user created. CHANGE PASSWORD IMMEDIATELY!"
                
                // Pause briefly to allow database to settle
                println "[KissInit] Pausing for 2 seconds..."
                Thread.sleep(2000)
                println "[KissInit] Resume..."
                
                // Verify user was saved - use getDatabase().find() for indexed lookup
                def db = PerstStorageManager.getDatabase()
                def verify = db.getSingleton(db.find(PerstUser.class, "username", new org.garret.perst.Key("admin")))
                println "[KissInit] Verification: admin user found = ${verify != null}"
            } else {
                println "[KissInit] Users already exist (${users.size()}), skipping admin creation"
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
        try {
            def users = PerstStorageManager.getAll(PerstUser.class)
            println "[KissInit] Indexing ${users.size()} PerstUsers..."
            users.each { PerstUser user ->
                user.index()
            }
            println "[KissInit] PerstUser indexing complete"
        } catch (Exception e) {
            println "[KissInit] Warning: Could not index PerstUsers: ${e.message}"
        }
    }
    
    /**
     * Index all Actors for fast lookup
     */
    private static void indexActors() {
        try {
            def actors = PerstStorageManager.getAll(Actor.class)
            println "[KissInit] Indexing ${actors.size()} Actors..."
            actors.each { Actor actor ->
                actor.index()
            }
            println "[KissInit] Actor indexing complete"
        } catch (Exception e) {
            println "[KissInit] Warning: Could not index Actors: ${e.message}"
        }
    }
}
