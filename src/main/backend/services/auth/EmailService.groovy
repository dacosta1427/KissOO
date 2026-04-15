package services.auth

import jakarta.mail.*
import jakarta.mail.internet.*

/**
 * EmailService - Utility for sending emails.
 * 
 * Configure via application.ini:
 *   mail.smtp.host=smtp.example.com
 *   mail.smtp.port=587
 *   mail.smtp.auth=true
 *   mail.smtp.username=user@example.com
 *   mail.smtp.password=secret
 *   mail.from=noreply@example.com
 */
class EmailService {
    
    private static String SMTP_HOST
    private static String SMTP_PORT
    private static String SMTP_USERNAME
    private static String SMTP_PASSWORD
    private static String FROM_ADDRESS
    private static String FROM_NAME
    private static boolean INITIALIZED = false
    
    /**
     * Initialize from config (call once at startup)
     */
    static void initialize(Properties config) {
        SMTP_HOST = config.getProperty("mail.smtp.host", "localhost")
        SMTP_PORT = config.getProperty("mail.smtp.port", "25")
        SMTP_USERNAME = config.getProperty("mail.smtp.username", "")
        SMTP_PASSWORD = config.getProperty("mail.smtp.password", "")
        FROM_ADDRESS = config.getProperty("mail.from.address", "noreply@example.com")
        FROM_NAME = config.getProperty("mail.from.name", "KISS")
        INITIALIZED = true
    }
    
    /**
     * Initialize from application.ini config file
     * Handles INI format with [section] headers
     */
    static void initializeFromConfig() {
        try {
            Properties config = new Properties()
            
            // Try multiple paths to find application.ini
            String configPath = null
            String appPath = null
            
            // Try MainServlet application path first
            try {
                appPath = org.kissweb.restServer.MainServlet.getApplicationPath()
            } catch (Exception ignored) {}
            
            if (appPath != null) {
                configPath = appPath + "application.ini"
            } else {
                // Fallback: try system property
                appPath = System.getProperty("appPath")
                if (appPath != null) {
                    configPath = appPath + "application.ini"
                } else {
                    // Fallback: relative to backend directory
                    configPath = "src/main/backend/application.ini"
                }
            }
            
            println "[EmailService] Loading config from: " + configPath
            
            // Read INI file, skipping section headers and comments
            BufferedReader reader = new BufferedReader(new FileReader(configPath))
            String line
            while ((line = reader.readLine()) != null) {
                line = line.trim()
                if (line.isEmpty() || line.startsWith('#') || line.startsWith('[')) {
                    continue
                }
                int eqIdx = line.indexOf('=')
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim()
                    String value = line.substring(eqIdx + 1).trim()
                    config.setProperty(key, value)
                }
            }
            reader.close()
            
            initialize(config)
            println "[EmailService] Initialized - SMTP: ${SMTP_HOST}:${SMTP_PORT} From: ${FROM_ADDRESS}"
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email config: " + e.message, e)
        }
    }
    
    /**
     * Send email
     */
    static boolean send(String to, String toName, String subject, String body) {
        if (!INITIALIZED) {
            try {
                initializeFromConfig()
            } catch (Exception e) {
                System.err.println("[EmailService] Not initialized and could not load config: " + e.message)
                e.printStackTrace()
                return false
            }
        }
        
        println "[EmailService] Sending to ${to} via ${SMTP_HOST}:${SMTP_PORT}"
        
        try {
            Properties props = new Properties()
            props.put("mail.smtp.host", SMTP_HOST)
            props.put("mail.smtp.port", SMTP_PORT)
            
            if (SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty()) {
                props.put("mail.smtp.auth", "true")
                props.put("mail.smtp.starttls.enable", "true")
            }
            
            Session session
            if (SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD)
                    }
                })
            } else {
                session = Session.getInstance(props)
            }
            
            Message msg = new MimeMessage(session)
            msg.setFrom(new InternetAddress(FROM_ADDRESS, FROM_NAME))
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to, toName))
            msg.setSubject(subject)
            msg.setText(body)
            msg.setSentDate(new java.util.Date())
            
            Transport.send(msg)
            return true
            
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send email: " + e.message)
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Send verification email
     */
    static boolean sendVerificationEmail(String email, String name, String token, String baseUrl) {
        String link = baseUrl + "/verify-email?token=" + token
        String subject = "Verify your email"
        String body = String.format(
            "Hello %s,\n\n" +
            "Your account has been created. Please verify your email by clicking the link below:\n\n" +
            "%s\n\n" +
            "This link expires in 24 hours.\n\n" +
            "If you did not request this account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, link
        )
        return send(email, name, subject, body)
    }
    
    /**
     * Send password reset email
     */
    static boolean sendPasswordResetEmail(String email, String name, String token, String baseUrl) {
        String link = baseUrl + "/reset-password?token=" + token
        String subject = "Reset your password"
        String body = String.format(
            "Hello %s,\n\n" +
            "You requested to reset your password. Click the link below:\n\n" +
            "%s\n\n" +
            "This link expires in 1 hour.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, link
        )
        return send(email, name, subject, body)
    }
    
    /**
     * Send login credentials email when owner is enabled
     */
    static boolean sendLoginCredentialsEmail(String email, String name, String username, String tempPassword, String baseUrl) {
        String loginLink = baseUrl + "/login"
        String subject = "Your login credentials"
        String body = String.format(
            "Hello %s,\n\n" +
            "Your account has been enabled. Here are your login credentials:\n\n" +
            "Username: %s\n" +
            "Temporary Password: %s\n\n" +
            "Login here: %s\n\n" +
            "You will be asked to change your password on first login.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, username, tempPassword, loginLink
        )
        return send(email, name, subject, body)
    }
}
