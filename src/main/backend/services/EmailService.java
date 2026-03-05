package services;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

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
public class EmailService {
    
    private static String SMTP_HOST;
    private static String SMTP_PORT;
    private static String SMTP_USERNAME;
    private static String SMTP_PASSWORD;
    private static String FROM_ADDRESS;
    private static String FROM_NAME;
    private static boolean INITIALIZED = false;
    
    /**
     * Initialize from config (call once at startup)
     */
    public static void initialize(Properties config) {
        SMTP_HOST = config.getProperty("mail.smtp.host", "localhost");
        SMTP_PORT = config.getProperty("mail.smtp.port", "25");
        SMTP_USERNAME = config.getProperty("mail.smtp.username", "");
        SMTP_PASSWORD = config.getProperty("mail.smtp.password", "");
        FROM_ADDRESS = config.getProperty("mail.from.address", "noreply@example.com");
        FROM_NAME = config.getProperty("mail.from.name", "KISS");
        INITIALIZED = true;
    }
    
    /**
     * Send email
     */
    public static boolean send(String to, String toName, String subject, String body) {
        if (!INITIALIZED) {
            System.err.println("EmailService not initialized");
            return false;
        }
        
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            
            if (SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty()) {
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
            }
            
            Session session;
            if (SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                    }
                });
            } else {
                session = Session.getInstance(props);
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
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send verification email
     */
    public static boolean sendVerificationEmail(String email, String name, String token, String baseUrl) {
        String link = baseUrl + "/verify-email?token=" + token;
        String subject = "Verify your email";
        String body = String.format(
            "Hello %s,\n\n" +
            "Thank you for signing up. Please verify your email by clicking the link below:\n\n" +
            "%s\n\n" +
            "This link expires in 24 hours.\n\n" +
            "If you did not sign up, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, link
        );
        return send(email, name, subject, body);
    }
    
    /**
     * Send password reset email
     */
    public static boolean sendPasswordResetEmail(String email, String name, String token, String baseUrl) {
        String link = baseUrl + "/reset-password?token=" + token;
        String subject = "Reset your password";
        String body = String.format(
            "Hello %s,\n\n" +
            "You requested to reset your password. Click the link below:\n\n" +
            "%s\n\n" +
            "This link expires in 1 hour.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, link
        );
        return send(email, name, subject, body);
    }
}
