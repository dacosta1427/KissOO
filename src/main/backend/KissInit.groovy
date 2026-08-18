import services.koo.NonSqlConnection
import koo.core.actor.Agreement
import koo.core.actor.Role
import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import koo.config.PerstConfig
import koo.core.database.StorageManager
import koo.core.user.PerstUserManager
import koo.core.user.PerstUser
import koo.security.PasswordSecurity
import java.util.function.Consumer

class KissInit {

    /**
     * Configure the system.
     */
    static void init() {
        println "[KissInit] init() CALLED"
        Thread.sleep(10000)

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

        // Step 2: Register NonSqlConnection ALWAYS when Perst is enabled and available
        // This runs even if Perst was already initialized from a previous startup
        if (PerstConfig.getInstance().isPerstEnabled() && StorageManager.isAvailable()) {
            try {
                // Check if already registered
                def existing = MainServlet.getEnvironment("NonSqlConnection")
                println "[KissInit] Checking existing: " + existing

                if (existing == null) {
                    println "[KissInit] Creating NEW NonSqlConnection..."
                    def nonSqlConn = new NonSqlConnection()
                    println "[KissInit] Created nonSqlConn: " + nonSqlConn

                    MainServlet.putEnvironment("NonSqlConnection", nonSqlConn)

                    // VERIFY it was stored
                    def verify = MainServlet.getEnvironment("NonSqlConnection")
                    println "[KissInit] Verified NonSqlConnection: " + verify

                    println "[KissInit] init() - NonSqlConnection registered"

                    // Skip user creation - causes ExceptionInInitializerError
                    // initDefaultUser()
                    // indexPerstUsers()
                    // indexActors()
                    println "[KissInit] User init SKIPPED (causes ExceptionInInitializerError)"
                } else {
                    println "[KissInit] init() - NonSqlConnection already registered"
                }
            } catch (Exception e) {
                println "[KissInit] WARNING: Could not create NonSqlConnection: " + e.message
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

        // Hypermedia showcase pages + the hypermedia demo endpoints (no login required)
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "index")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "datastar")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "htmx")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "sse")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "auth")
        // Wave 1 showcase screens (login + CRUD + controls)
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "login")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "loginPost")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "logout")
        // CRUD PAGE is public but self-gates to login (see ShowcaseService.crud).
        // The CRUD DATA methods below are intentionally NOT whitelisted, so they are
        // framework-protected: a request without a valid session gets _ErrorCode 2.
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "crud")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "controls")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "liveSearch")
        MainServlet.allowWithoutAuthentication("services/ShowcaseService", "echo")
        MainServlet.allowWithoutAuthentication("services/HypermediaTestService", "taskFragment")
        MainServlet.allowWithoutAuthentication("services/HypermediaTestService", "patchSignals")
        MainServlet.allowWithoutAuthentication("services/HypermediaTestService", "liveClock")
        MainServlet.allowWithoutAuthentication("services/HypermediaTestService", "fragmentDs")
        // NOTE: whoami is deliberately NOT whitelisted - it must carry a valid X-Kiss-Uuid.

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
     * Normal Kiss init hook (SQL database is open).
     * Unused in KissOO, which runs Perst (no-SQL); the Perst initialization
     * lives in init2ForNonSQL().
     */
    static void init2(Connection db) {
    }

    /**
     * No-SQL (Perst OODBMS) initialization. Called by MainServlet when no SQL
     * database is configured, with the registered NonSqlConnection.
     */
    static void init2ForNonSQL(NonSqlConnection db) {
        try {
            println "[KissInit] init2ForNonSQL() CALLED"
            println "[KissInit] db = " + db

            // Initialize PasswordSecurity
            if (!PasswordSecurity.initialise()) {
                System.out.println("! X X X PasswordSecurity NOT initialised!");
            } else {
                println "[KissInit] init2ForNonSQL() PW-SEC initialised";
            }

            // Initialize Perst if not already initialized
            if (PerstConfig.getInstance().isPerstEnabled() && !StorageManager.isAvailable()) {
                println "[KissInit] Initializing Perst database..."
                StorageManager.initialize()
                println "[KissInit] Perst initialized, isAvailable=" + StorageManager.isAvailable()
            } else {
                println "[KissInit] Perst is already available"
            }

            // Create default super-admin user if none exists (ExceptionInInitializerError is now fixed)
            initDefaultUser()

            println "[KissInit] init2ForNonSQL() COMPLETED"
        } catch (Exception e) {
            println "[KissInit] ERROR in init2ForNonSQL: ${e.class.simpleName}: ${e.message}"
            e.printStackTrace()
        }
    }

    /**
     * Initialize default admin users if no users exist.
     */
    private static void initDefaultUser() {
        try {
            def users = StorageManager.getAll(PerstUser.class)
            if (!users || users.size() == 0) {
                println "[KissInit] Creating default superAdmin user..."
                println "[KissInit] Role constants at runtime: " + Role.values().collect { it.name() }.join(", ")

                // Create superAdmin Actor with full Agreement (like cleaners2)
                def agreement = new Agreement(Role.SUPER_ADMIN)
                def adminActor = new domain.actor.owner.Owner("System Admin", "", "admin@localhost", true)
                adminActor.getAgreement().setRole(Role.SUPER_ADMIN)

                // Owner constructor already created a deactivated PerstUser
                // Configure it with admin credentials
                def adminUser = adminActor.getPerstUser()
                adminUser.setUsername("admin")
                adminUser.setPassword("admin")
                adminUser.setEmail("admin@localhost")
                adminUser.setActive(true)
                adminUser.setEmailVerified(true)

                // Store both together
                def tc = StorageManager.createContainer()
                tc.addInsert(adminActor)
                tc.addInsert(adminUser)
                if (StorageManager.store(tc)) {
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
            println "[KissInit] ERROR in initDefaultUser: ${e.class.simpleName}: ${e.message}"
            // Don't crash - just log and continue
        }

        // User creation skipped - use signup API after server starts
        // ExceptionInInitializerError at runtime prevents proper initialization
        println "[KissInit] initDefaultUser() - SKIPPED (use signup API after server starts)"
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
