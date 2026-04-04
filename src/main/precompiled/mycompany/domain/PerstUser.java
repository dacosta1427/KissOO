package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * PerstUser - User entity stored in Perst OODBMS.
 * 
 * Every PerstUser belongs to an Actor (Owner, Cleaner, etc.).
 * The bidirectional link is:
 *   - Actor has a transient perstUser reference (cached lookup)
 *   - PerstUser has a persistent actor reference (the real link)
 * 
 * Indexing is handled by CDatabase via @Indexable annotations.
 * Use PerstUserManager for all database operations.
 */
@Getter @Setter
public class PerstUser extends CVersion {
    
    @FullTextSearchable
    @Indexable(unique=true)
    private String username;
    
    private String passwordHash;
    
    @Indexable
    private boolean active = false;
    
    @FullTextSearchable
    private String email;
    
    @FullTextSearchable
    private String firstName;
    
    @FullTextSearchable
    private String lastName;
    
    private long createdDate;
    private long lastLoginDate;
    
    @Indexable
    private boolean emailVerified = false;

    private boolean mustChangePassword = false;

    private Actor actor;  // Persistent reference to the owning Actor
    
    private String verificationToken;
    private long verificationExpiresAt;
    
    private String preferredLanguage = "en";
    
    public PerstUser() {
        this.createdDate = System.currentTimeMillis();
    }
    
    /**
     * Create a PerstUser with username, password, and linked Actor.
     */
    public PerstUser(String username, String password, Actor actor) {
        this();
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.actor = actor;
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    public boolean checkPassword(String password) {
        if (passwordHash == null || password == null) {
            return false;
        }
        return passwordHash.equals(hashPassword(password));
    }
    
    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }
    
    public void generateVerificationToken() {
        this.verificationToken = UUID.randomUUID().toString();
        this.verificationExpiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
    }
    
    public boolean verifyEmail(String token) {
        if (verificationToken == null || !verificationToken.equals(token)) {
            return false;
        }
        if (System.currentTimeMillis() > verificationExpiresAt) {
            return false;
        }
        this.emailVerified = true;
        this.verificationToken = null;
        return true;
    }
    
    public boolean canLogin() {
        return active && emailVerified;
    }

    @Override
    public String toString() {
        return "PerstUser{" +
                "username='" + username + '\'' +
                ", active=" + active +
                ", actor=" + (actor != null ? actor.getName() + "(" + actor.getType() + ")" : "null") +
                '}';
    }
}
