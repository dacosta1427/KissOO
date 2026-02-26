import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import java.util.function.Consumer

class KissInit {

    /**
     * Configure the system.
     */
import org.kissweb.database.Connection
import org.kissweb.restServer.MainServlet
import org.kissweb.restServer.UserCache
import org.kissweb.restServer.UserData
import gfe.PerstHelper
import gfe.PerstConfig
import java.util.function.Consumer

class KissInit {

    /**
     * Configure the system.
     */
    static void init() {

        System.out.println(">>>>>>>>>> KISSINIT IS BEING CALLED!")
        MainServlet.readIniFile "application.ini", "main"

        // Allow Perst-based login without authentication (required - can't log in otherwise!)
        MainServlet.allowWithoutAuthentication("", "Login")
        
        // Initialize Perst database at startup
        if (PerstConfig.getInstance().isPerstEnabled()) {
            System.out.println("[KissInit] Initializing Perst database...")
            PerstHelper.initialize()
            System.out.println("[KissInit] Perst database initialized")
        }

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
     */
    static void init2(Connection db) {
        // If you use db, make sure you commit.
    }
}
