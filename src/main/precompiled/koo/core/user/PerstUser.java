package koo.oodb.core.user;

import koo.security.PasswordSecurity;
import lombok.Getter;
import lombok.Setter;
import koo.oodb.core.actor.AActor;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

import java.util.UUID;

/**
 * PerstUser - User entity stored in Perst OODBMS.
 * 
 * Every PerstUser belongs to a NATURAL AActor (Owner, Cleaner, etc.).
 * The PerstUser has a persistent reference to its AActor.
 * 
 * PerstUser is indexed by username for fast lookup via find(username).
 * After login, PerstUser is stored in session cache with reference to latest AActor.
 * 
 * When AActor is deleted, PerstUser is marked deleted but not immediately removed.
 * PerstUserManager handles cleanup of deleted PerstUsers from cache.
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

    private AActor actor;  // Persistent reference to the owning AActor
    
    private String verificationToken;
    private long verificationExpiresAt;
    
    private String preferredLanguage = "en";
    
    public PerstUser() {
        this.createdDate = System.currentTimeMillis();
    }
    
    /**
     * Create a PerstUser with username, password, and linked AActor.
     */
    public PerstUser(String username, String password, AActor actor) {
        this();
        this.username = username;
        this.passwordHash = PasswordSecurity.hashPassword(password);
        this.actor = actor;
    }
    
    /**
     * @deprecated Use {@link #getActor()} instead
     */
    @Deprecated
    public AActor getAActor() {
        return actor;
    }
    
    /**
     * @deprecated Use {@link #setActor(AActor)} instead
     */
    @Deprecated
    public void setAActor(AActor AActor) {
        this.actor = AActor;
    }
    
    public boolean checkPassword(String password) {
        if (passwordHash == null || password == null) {
            return false;
        }
        return PasswordSecurity.verifyPassword(password, passwordHash);
    }
    
    public void setPassword(String password) {
        this.passwordHash = PasswordSecurity.hashPassword(password);
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
        return active && emailVerified && !isDeleted();
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