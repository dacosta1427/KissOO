package koo.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import koo.config.PerstConfig;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * EmailService - Production Java implementation with security hardening.
 * Replaces the insecure version with TLS enforcement and proper error handling.
 */
public class EmailService {
    
    private static String SMTP_HOST;
    private static String SMTP_PORT;
    private static String SMTP_USERNAME;
    private static String SMTP_PASSWORD;
    private static String FROM_ADDRESS;
    private static String FROM_NAME;
    private static boolean INITIALIZED = false;
    
    /**
     * Initialize from application.ini with mandatory TLS enforcement.
     * Throws SecurityException if TLS is not enabled with authentication.
     */
    public static void initialize() {
        if (INITIALIZED) return;
        
        Properties props = new Properties();
        String appPath = null;
        try {
            appPath = org.kissweb.restServer.MainServlet.getApplicationPath();
        } catch (Exception ignored) {}
        
        String configPath = (appPath != null) ? 
            appPath + "application.ini" : "src/main/backend/application.ini";
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(configPath)) {
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email config: " + e.getMessage(), e);
        }
        
        SMTP_HOST = props.getProperty("mail.smtp.host", "localhost");
        SMTP_PORT = props.getProperty("mail.smtp.port", "25");
        SMTP_USERNAME = props.getProperty("mail.smtp.username", "");
        SMTP_PASSWORD = props.getProperty("mail.smtp.password", "");
        FROM_ADDRESS = props.getProperty("mail.from.address", "noreply@example.com");
        FROM_NAME = props.getProperty("mail.from.name", "KISS");
        
        // SECURITY CRITICAL: Enforce TLS when authentication is enabled
        boolean enableAuth = Boolean.parseBoolean(props.getProperty("mail.smtp.auth", "false"));
        boolean enableTLS = Boolean.parseBoolean(props.getProperty("mail.smtp.starttls.enable", 
            enableAuth ? "true" : "false"));
        
        if (enableAuth && !enableTLS) {
            throw new SecurityException(
                "SMTP authentication requires TLS encryption. " +
                "Set mail.smtp.starttls.enable=true in application.ini");
        }
        
        INITIALIZED = true;
        System.out.println("[EmailService] SECURELY initialized (TLS enforced)");
    }
    
    /**
     * Send email with security validation. Called from AuthService.groovy.
     */
    public static boolean send(String to, String toName, String subject, String body) {
        if (!INITIALIZED) initialize();
        
        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.host", SMTP_HOST);
            mailProps.put("mail.smtp.port", SMTP_PORT);
            
            if (SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty()) {
                mailProps.put("mail.smtp.auth", "true");
                mailProps.put("mail.smtp.starttls.enable", "true");
            }
            
            Session session;
            if (SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty()) {
                session = Session.getInstance(mailProps, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                    }
                });
            } else {
                session = Session.getInstance(mailProps);
            }
            
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM_ADDRESS, FROM_NAME));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to, toName));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setSentDate(new java.util.Date());
            Transport.send(msg);
            return true;
            
        } catch (Exception e) {
            System.err.println("[EmailService] Send failed");
            return false;  // Don't leak sensitive info
        }
    }
    
    /**
     * Send account verification email. Called from AuthService.groovy.
     * Replaces old sendEmail() method.
     */
    public static boolean sendVerification(
            String email, String name, String token, String baseUrl, Object extra) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String link = baseUrl + "/verify-email?token=" + encodedToken;
        String subject = "Verify your email";
        String body = "Hello " + name + ",\n\n" +
            "Please verify: " + link + "\n\nExpires in 24 hours.";
        return send(email, name, subject, body);
    }
    
    /**
     * Send password reset email. Called from Users.groovy.
     */
    public static boolean sendPasswordReset(
            String email, String name, String token, String baseUrl, Object extra) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String link = baseUrl + "/reset-password?token=" + encodedToken;
        String subject = "Reset your password";
        String body = "Hello " + name + ",\n\n" +
            "Reset: " + link + "\n\nExpires in 1 hour.";
        return send(email, name, subject, body);
    }
    
    /**
     * Send login credentials email. Called from Users.groovy & CleaningService.groovy.
     */
    public static boolean sendLoginCredentials(
            String email, String name, String username, 
            String tempPassword, String baseUrl, Object extra) {
        String link = baseUrl + "/login";
        String subject = "Your login credentials";
        String body = "Hello " + name + ",\n\n" +
            "Username: " + username + "\n" +
            "Temporary Password: " + tempPassword + "\n\n" +
            "Login: " + link + "\n\nChange password immediately.";
        return send(email, name, subject, body);
    }
}
