package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 * Extends Actor to leverage the "Manager at the Gate" pattern with PerstUser.
 * 
 * Automatically creates a deactivated PerstUser on construction.
 */
@Getter @Setter
public class Cleaner extends Actor {
    
    private String phone;
    
    @FullTextSearchable
    private String email;
    
    private String address;
    
    public Cleaner() {
        super("Cleaner", "Cleaner", new Agreement());
    }
    
    public Cleaner(String name, String phone, String email, boolean active) {
        super(name, "Cleaner", new Agreement());
        this.phone = phone;
        this.email = email;
        setActive(active);
        replacePerstUserWithCleanerUser();
    }
    
    public Cleaner(String name, String phone, String email, String address, boolean active) {
        super(name, "Cleaner", new Agreement());
        this.phone = phone;
        this.email = email;
        this.address = address;
        setActive(active);
        replacePerstUserWithCleanerUser();
    }
    
    private void replacePerstUserWithCleanerUser() {
        String username = email != null && !email.isEmpty() ? email : getName().toLowerCase().replaceAll("\\s+", "_") + "_" + getUuid().substring(0, 8);
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 16);
        PerstUser user = new PerstUser(username, tempPassword, this);
        user.setEmail(email);
        user.setActive(false);
        user.setEmailVerified(false);
        setPerstUser(user);
    }
    
    // Convenience delegate
    public PerstUser getUser() { return getPerstUser(); }
    public void setUser(PerstUser user) { setPerstUser(user); }
    
    @Override
    public String toString() {
        return "Cleaner{" +
                "name='" + getName() + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", active=" + isActive() +
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
