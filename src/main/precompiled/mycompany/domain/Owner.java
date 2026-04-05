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
 * Extends Actor (NATURAL by default), so automatically has a PerstUser
 * created by the Actor constructor (deactivated by default).
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
        super(name, "Owner", new Agreement(), ActorType.NATURAL);
        this.email = email;
        this.phone = phone;
        this.address = address;
        
        // Actor constructor already created a deactivated PerstUser
        // Update the PU username to use email if provided
        if (email != null && !email.isEmpty()) {
            getPerstUser().setUsername(email);
            getPerstUser().setEmail(email);
        }
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
