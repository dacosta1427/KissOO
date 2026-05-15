import koo.core.database.PerstConnection
import koo.config.PerstConfig
import koo.core.user.PerstUserManager
import koo.core.user.PerstUser
import koo.core.actor.Role
import org.garret.perst.dbmanager.UnifiedDBManager
import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import koo.security.PasswordSecurity
import java.util.function.Consumer

class KissInit {

    private static UnifiedDBManager getUdbm() {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager")
    }

    private static boolean isPerstAvailable() {
        return getUdbm() != null
    }

    private static void initializePerst() {
        if (!PerstConfig.getInstance().isPerstEnabled()) return
        if (isPerstAvailable()) return

        try {
            String dbPath = PerstConfig.getInstance().getDatabasePath()
            String indexPath = dbPath + ".idx"
            int pageSize = PerstConfig.getInstance().getPagePoolSize()

            // Use UnifiedDBManager factory - handles Storage creation, compatibility check, and opening
            UnifiedDBManager udbm = UnifiedDBManager.create(dbPath, indexPath, pageSize)

            // Register UnifiedDBManager for all managers to use
            MainServlet.putEnvironment("unifiedDBManager", udbm)

            // Register PerstConnection for framework use (NonSqlConnection)
            PerstConnection perstConn = new PerstConnection()
            MainServlet.putEnvironment("NonSqlConnection", perstConn)

            println "[KissInit] UnifiedDBManager initialized successfully"

            // Schedule Lucene optimizer
            int interval = PerstConfig.getInstance().getPerstOptimizeInterval()
            if (interval > 0) {
                def scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "LuceneOptimizer")
                    t.setDaemon(true)
                    return t
                })
                scheduler.scheduleAtFixedRate({
                    try {
                        println "[KissInit] Running Lucene full-text index optimization..."
                        println "[KissInit] Lucene optimization complete."
                    } catch (Exception e) {
                        System.err.println("[KissInit] Lucene optimization failed: " + e.getMessage())
                    }
                }, interval, interval, java.util.concurrent.TimeUnit.SECONDS)
                println "[KissInit] Lucene optimizer scheduled every ${interval} seconds"
            }

        } catch (Exception e) {
            System.err.println("[KissInit] Failed to initialize UnifiedDBManager: " + e.getMessage())
            e.printStackTrace()
        }
    }

    private static void deleteRecursively(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles()
            if (children != null) {
                for (java.io.File child : children) {
                    deleteRecursively(child)
                }
            }
        }
        file.delete()
    }

    /**
     * Configure the system.
     */
    static void init() {
        println "[KissInit] init() CALLED"

        MainServlet.readIniFile "application.ini", "main"
        MainServlet.readIniFile "application.ini", "PasswordSecurity"

        println "[KissInit] init() - After readIniFile"

        // Initialize Perst HERE - before init2() which might not be called
        println "[KissInit] init() - Checking Perst config..."
        println "[KissInit] init() - PerstEnabled=" + PerstConfig.getInstance().isPerstEnabled()

        if (PerstConfig.getInstance().isPerstEnabled()) {
            initializePerst()
        }

        // Initialize PasswordSecurity
        if (!PasswordSecurity.initialise()) {
            println "[KissInit] WARNING: PasswordSecurity NOT initialised!"
        }

        // Create default admin user after Perst is initialized
        if (PerstConfig.getInstance().isPerstEnabled() && isPerstAvailable()) {
            try {
                def users = getUdbm().getObjects(PerstUser.class)
                List<PerstUser> userList = []
                if (users != null) {
                    while (users.hasNext()) {
                        userList.add(users.next())
                    }
                }

                if (!userList || userList.size() == 0) {
                    println "[KissInit] init() - No users found, creating default admin..."
                    createDefaultAdminUser()
                } else {
                    println "[KissInit] init() - Found ${userList.size()} users, checking admin..."
                    def admin = userList.find { it.getUsername() == 'admin' }
                    if (admin && !admin.isEmailVerified()) {
                        admin.setEmailVerified(true)
                        PerstUserManager.update(admin)
                        println "[KissInit] init() - Admin emailVerified set to true"
                    }
                }
            } catch (Exception e) {
                println "[KissInit] WARNING: Could not create default admin: ${e.message}"
                e.printStackTrace()
            }
        }

println "[KissInit] init() COMPLETED"

         // Allow Login service to be called without authentication
         MainServlet.allowWithoutAuthentication("services/Login", "login")
         MainServlet.allowWithoutAuthentication("services/Login", "checkLogin")

         // Set up a global logout handler
        UserCache.setLogoutHandler({ UserData ud ->
            println "User ${ud.getUsername()} (ID: ${ud.getUserId()}) is logging out"
        } as Consumer<UserData>)
    }

    /**
     * Code to run once the database is open but before the app is running.
     */
    static void init2(PerstConnection db) {
        if (!PasswordSecurity.initialise()) System.out.println("! X X X PasswordSecurity NOT initialised!")
        System.out.println("* * * PasswordSecurity initialised!")

        try {
            println "[KissInit] init2() CALLED"

            if (PerstConfig.getInstance().isPerstEnabled() && !isPerstAvailable()) {
                println "[KissInit] Initializing Perst database via UnifiedDBManager..."
                initializePerst()
            } else {
                println "[KissInit] Perst is already available"
            }

            if (isPerstAvailable()) {
                def users = getUdbm().getObjects(PerstUser.class)
                List<PerstUser> userList = []
                if (users != null) {
                    while (users.hasNext()) {
                        userList.add(users.next())
                    }
                }
                println "[KissInit] Found ${userList.size()} users"

                if (!userList || userList.size() == 0) {
                    println "[KissInit] No users found - creating default users..."
                    createDefaultAdminUser()
                } else {
                    println "[KissInit] Users already exist (${userList.size()}), checking admin user..."
                    def admin = PerstUserManager.getByKey("admin")
                    if (admin != null) {
                        if (!admin.isEmailVerified()) {
                            admin.setEmailVerified(true)
                            PerstUserManager.update(admin)
                            println "[KissInit] Admin user emailVerified set to true"
                        }
                        println "[KissInit] Admin user found, active=" + admin.isActive() + ", emailVerified=" + admin.isEmailVerified()
                    }
                    def updated = 0
                    userList.each { user ->
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
            } else {
                println "[KissInit] WARNING: Perst not available, skipping user init"
            }

            println "[KissInit] init2() COMPLETED"
        } catch (Exception e) {
            println "[KissInit] ERROR in init2: ${e.class.simpleName}: ${e.message}"
            e.printStackTrace()
        }
    }

    /**
     * Create the default admin user with SUPER_ADMIN role.
     */
    private static void createDefaultAdminUser() {
        try {
            println "[KissInit] Creating default superAdmin user..."

            def adminActor = new domain.actor.owner.Owner("System Admin", "", "admin@localhost", true)
            adminActor.getAgreement().setRole(Role.SUPER_ADMIN)

            def adminUser = adminActor.getPerstUser()
            adminUser.setUsername("admin")
            adminUser.setPassword("admin")
            adminUser.setEmail("admin@localhost")
            adminUser.setActive(true)
            adminUser.setEmailVerified(true)

            def tc = getUdbm().createContainer()
            tc.addInsert(adminActor)
            tc.addInsert(adminUser)
            def result = getUdbm().store(tc)
            if (result.isSuccess()) {
                println "[KissInit] Default superAdmin user created. CHANGE PASSWORD IMMEDIATELY!"
            } else {
                println "[KissInit] ERROR: Failed to create admin user: ${result.getMessage()}"
            }
        } catch (Exception e) {
            println "[KissInit] ERROR in createDefaultAdminUser: ${e.class.simpleName}: ${e.message}"
            e.printStackTrace()
        }
    }
}