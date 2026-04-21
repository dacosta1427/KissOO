package koo.security;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import org.kissweb.restServer.MainServlet;

import java.io.InputStream;
import java.util.Properties;

public class PasswordSecurity {
    // Lazy-initialized to avoid ExceptionInInitializerError at class load time
    private static Argon2Function ARGON2_FUNCTION;
    private static boolean initialized = false;
    private static final Object lock = new Object();

    public static boolean initialise() {
        if (initialized) {
            return ARGON2_FUNCTION != null;
        }
        synchronized (lock) {
            if (initialized) {
                return ARGON2_FUNCTION != null;
            }
            try {
                ARGON2_FUNCTION = loadArgon2Function();
            } catch (Exception e) {
                System.err.println("* * * PasswordSecurity init failed: " + e.getMessage());
                ARGON2_FUNCTION = null;
            }
            initialized = true;
            return ARGON2_FUNCTION != null;
        }
    }

    private static Argon2Function loadArgon2Function() {
        if (PasswordSecurity.class.getClassLoader().getResource("psw4j.properties") != null) {

            try (InputStream input = PasswordSecurity.class.getClassLoader()
                    .getResourceAsStream("psw4j.properties")) {

                Properties props = new Properties();
                props.load(input);

                // Parse properties with defaults
                return Argon2Function.getInstance(
                        Integer.parseInt(props.getProperty("argon2.memory", "15360")),
                        Integer.parseInt(props.getProperty("argon2.iterations", "2")),
                        Integer.parseInt(props.getProperty("argon2.parallelism", "1")),
                        Integer.parseInt(props.getProperty("argon2.length", "32")),
                        Argon2.valueOf(props.getProperty("argon2.type", "ID").toUpperCase()),
                        Integer.parseInt(props.getProperty("argon2.version", "19"))
                );

            } catch (Exception e) {
                // Log error and fallback to defaults
                System.err.println("* * * ! Failed to load psw4j.properties, using defaults. Error: " + e.getMessage());
                return Argon2Function.getInstance(15360, 2, 1, 32, Argon2.ID);
            }
        }

        return loadProperties();
    }

    // Load the Argon parameters via the application.ini for a web-app
    private static Argon2Function loadProperties() {
        try {
            String memStr = (String) MainServlet.getEnvironment("argon2.memory");
            if (memStr == null || memStr.isEmpty()) {
                return Argon2Function.getInstance(15360, 2, 1, 32, Argon2.ID);
            }
            
            int mem = Integer.parseInt(memStr);
            int it = Integer.parseInt((String) MainServlet.getEnvironment("argon2.iterations"));
            int par = Integer.parseInt((String) MainServlet.getEnvironment("argon2.parallelism"));
            int len = Integer.parseInt((String) MainServlet.getEnvironment("argon2.length"));

            return Argon2Function.getInstance(mem, it, par, len, Argon2.ID);
        } catch (Exception e) {
            System.err.println("* * * ! Failed to load password config from ini, using defaults: " + e.getMessage());
            return Argon2Function.getInstance(15360, 2, 1, 32, Argon2.ID);
        }
    }
    }

    // Public method to get the pre-configured function
    public static Argon2Function getArgon2Function() {
        return ARGON2_FUNCTION;
    }

    /**
     * Hash a password for storage using Argon2id.
     *
     * @param plainPassword The user's password.
     * @return The hash string to store in the database.
     */
    public static String hashPassword(String plainPassword) {
        // Auto-initialize if not done yet
        if (!initialized) {
            initialise();
        }
        if (ARGON2_FUNCTION == null) {
            throw new IllegalStateException("PasswordSecurity not initialized");
        }
        // The salt is automatically generated and managed by Password4j
        Hash hash = Password.hash(plainPassword)
                .with(ARGON2_FUNCTION); // Use the pre-configured function

        return hash.getResult();
    }

    /**
     * Verify a login attempt.
     *
     * @param enteredPassword The password provided at login.
     * @param storedHash      The hash retrieved from the database.
     * @return true if the password matches the hash.
     */
    public static boolean verifyPassword(String enteredPassword, String storedHash) {
        // Auto-initialize if not done yet
        if (!initialized) {
            initialise();
        }
        if (ARGON2_FUNCTION == null) {
            throw new IllegalStateException("PasswordSecurity not initialized");
        }
        // Password4j automatically extracts salt and parameters from the stored hash
        return Password.check(enteredPassword, storedHash)
                .with(ARGON2_FUNCTION);
    }

    /**
     * Example: How to use it in practice.
     */
    public static void main(String[] args) {
//        String userPassword = "MySecureP@ssw0rd123";
//        initialise();
//
//        // Simulate user registration
//        String hashToStore = hashPassword(userPassword);
//        System.out.println("Hash to store in DB:\n" + hashToStore + "\n");
//
//        // Simulate login - correct password
//        boolean isCorrect = verifyPassword("MySecureP@ssw0rd123", hashToStore);
//        System.out.println("Correct password verification: " + isCorrect); // Should be true
//
//        // Simulate login - wrong password
//        boolean isWrong = verifyPassword("WrongPassword", hashToStore);
//        System.out.println("Wrong password verification:  " + isWrong); // Should be false
    }
}