package mycompany.actor.cleaner;

import koo.oodb.core.actor.ANaturalActor;
import lombok.Getter;
import lombok.Setter;
import koo.oodb.core.actor.ActorType;
import koo.oodb.core.actor.Agreement;
import koo.oodb.core.user.PerstUser;
import org.garret.perst.continuous.FullTextSearchable;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 * 
 * Extends AActor (NATURAL by default), so automatically has a PerstUser
 * created by the AActor constructor (deactivated by default).
 * 
 * Uses stored collections for Pure OO navigation.
 */
@Getter @Setter
public class Cleaner extends ANaturalActor {
    
    private String phone;
    
    @FullTextSearchable
    private String email;
    
    private String address;
    
    private Set<Schedule> schedules = new HashSet<>();
    
    public Cleaner(String name, String phone, String email, boolean active) {
        super(name, new Agreement());
        this.phone = phone;
        this.email = email;
        setActive(active);
        
        // AActor constructor already created a deactivated PerstUser
        // Update PU username/email if provided
        if (email != null && !email.isEmpty()) {
            getPerstUser().setUsername(email);
            getPerstUser().setEmail(email);
        }
    }
    
    public Collection<Schedule> getSchedules() {
        return schedules;
    }
    
    public void addSchedule(Schedule schedule) {
        if (schedule != null) {
            schedule.setCleaner(this);
            schedules.add(schedule);
        }
    }
    
    public void removeSchedule(Schedule schedule) {
        if (schedule != null && schedules.contains(schedule)) {
            schedule.setCleaner(null);
            schedules.remove(schedule);
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
