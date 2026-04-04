package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 * 
 * Automatically creates a deactivated PerstUser on construction.
 */
@Getter @Setter
public class Owner extends Actor {
    
    private String email;
    private String phone;
    private String address;
    
    public Owner() {
        super("Owner", "Owner", new Agreement());
    }
    
    public Owner(String name, String email, String phone, String address) {
        super(name, "Owner", new Agreement());
        this.email = email;
        this.phone = phone;
        this.address = address;
        
        // Replace the auto-created PerstUser with one using email as username
        String username = email != null && !email.isEmpty() ? email : name.toLowerCase().replaceAll("\\s+", "_") + "_" + getUuid().substring(0, 8);
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
        return "Owner{" +
                "name='" + getName() + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", active=" + isActive() +
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
