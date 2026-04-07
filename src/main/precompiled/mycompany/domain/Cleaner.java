package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.ArrayList;
import java.util.Collection;
import oodb.PerstStorageManager;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 * 
 * Extends Actor (NATURAL by default), so automatically has a PerstUser
 * created by the Actor constructor (deactivated by default).
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
        super(name, "Cleaner", new Agreement(), ActorType.NATURAL);
        this.phone = phone;
        this.email = email;
        setActive(active);
        
        // Actor constructor already created a deactivated PerstUser
        // Update PU username/email if provided
        if (email != null && !email.isEmpty()) {
            getPerstUser().setUsername(email);
            getPerstUser().setEmail(email);
        }
    }
    
    public Cleaner(String name, String phone, String email, String address, boolean active) {
        super(name, "Cleaner", new Agreement(), ActorType.NATURAL);
        this.phone = phone;
        this.email = email;
        this.address = address;
        setActive(active);
        
        // Actor constructor already created a deactivated PerstUser
        // Update PU username/email if provided
        if (email != null && !email.isEmpty()) {
            getPerstUser().setUsername(email);
            getPerstUser().setEmail(email);
        }
    }
    
    public Collection<Schedule> getSchedules() {
        Collection<Schedule> result = new ArrayList<>();
        Collection<Schedule> all = PerstStorageManager.getAll(Schedule.class);
        for (Schedule schedule : all) {
            if (schedule.getCleaner() != null && schedule.getCleaner().getOid() == this.getOid()) {
                result.add(schedule);
            }
        }
        return result;
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
