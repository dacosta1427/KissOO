package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import mycompany.domain.PerstUser;
import mycompany.domain.Agreement;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 */
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
    }
    
    // Getters and setters for Owner-specific fields
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    // Delegating methods to Actor's perstUser field
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
