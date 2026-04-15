package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.PersistentCollection;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import oodb.PerstStorageManager;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 * 
 * Extends Actor (NATURAL by default), so automatically has a PerstUser
 * created by the Actor constructor (deactivated by default).
 * 
 * Bidirectional relationships:
 * - Cleaner HAS schedules (Collection)
 * - Schedule HAS cleaner (single reference)
 */
@Getter @Setter
public class Cleaner extends Actor {
    
    private String phone;
    
    @FullTextSearchable
    @Indexable
    private String email;
    
    private String address;
    
    // Bidirectional: Cleaner → schedules (implicit filtering via schedule.cleaner)
    private Set<Schedule> schedules = new HashSet<>();
    
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
    
    // Use persisted collection - no more iteration!
    public Collection<Schedule> getSchedules() {
        return schedules;
    }
    
    public void addSchedule(Schedule schedule) {
        if (schedule != null) {
            schedules.add(schedule);
            schedule.setCleaner(this);
        }
    }
    
    public void removeSchedule(Schedule schedule) {
        if (schedule != null && schedules.contains(schedule)) {
            schedules.remove(schedule);
            schedule.setCleaner(null);
        }
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
                ", schedules=" + schedules.size() +
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
