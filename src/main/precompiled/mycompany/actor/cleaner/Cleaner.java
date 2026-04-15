package mycompany.actor.cleaner;

import koo.oodb.core.actor.ANaturalActor;
import lombok.Getter;
import lombok.Setter;
import koo.oodb.core.actor.AActor;
import koo.oodb.core.actor.ActorType;
import koo.oodb.core.actor.Agreement;
import koo.oodb.core.user.PerstUser;
import koo.oodb.core.StorageManager;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 * 
 * Extends AActor (NATURAL by default), so automatically has a PerstUser
 * created by the AActor constructor (deactivated by default).
 */
@Getter @Setter
public class Cleaner extends ANaturalActor {
    
    private String phone;
    
    @FullTextSearchable
    private String email;
    
    private String address;
    
    public Cleaner(String name, String phone, String email, boolean active) {
        super(name, "Cleaner", new Agreement(), ActorType.NATURAL);
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
        Collection<Schedule> result = new ArrayList<>();
        Collection<Schedule> all = StorageManager.getAll(Schedule.class);
        for (Schedule schedule : all) {
            if (schedule.getCleaner() != null && schedule.getCleaner().getOid() == this.getOid()) {
                result.add(schedule);
            }
        }
        return result;
    }
    
    public void addSchedule(Schedule schedule) {
        if (schedule != null) {
            schedule.setCleaner(this);
        }
    }
    
    public void removeSchedule(Schedule schedule) {
        if (schedule != null && schedule.getCleaner() != null && schedule.getCleaner().getOid() == this.getOid()) {
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
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
