package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 */
public class Owner extends CVersion {
    
    @FullTextSearchable
    @Indexable(unique = true)
    private String name;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    @Indexable
    private boolean active = true;
    
    @Indexable
    private long userId = 0;  // Reference to PerstUser entity (0 = no user)
    
    public Owner() {
        // default constructor
    }
    
    public Owner(String name, String email, String phone, String address, boolean active) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.active = active;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    @Override
    public String toString() {
        return "Owner{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", active=" + active +
                ", userId=" + userId +
                '}';
    }
}
