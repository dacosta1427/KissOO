package mycompany.actor.cleaner;

import koo.core.actor.ANaturalActor;
import koo.core.database.StorageManager;
import lombok.Getter;
import lombok.Setter;
import koo.core.actor.Agreement;
import koo.core.user.PerstUser;
import org.garret.perst.continuous.FullTextSearchable;
import org.garret.perst.Link;

import java.util.Arrays;
import java.util.List;

/**
 * Cleaner entity for cleaning scheduler.
 * Uses Perst Link for schedules collection.
 */
@Getter @Setter
public class Cleaner extends ANaturalActor {
    
    private String phone;
    
    @FullTextSearchable
    private String email;
    
    private String address;
    
    private Link schedules;  // Perst Link - initialized in constructor
    
    public Cleaner(String name, String phone, String email, boolean active) {
        super(name, new Agreement(), email);
        this.phone = phone;
        this.email = email;
        setActive(active);
        this.schedules = StorageManager.getStorage().createLink();
    }
    
    public List<Schedule> getSchedules() {
        if (schedules == null || schedules.isEmpty()) return List.of();
        return Arrays.asList((Schedule[])schedules.toArray(new Schedule[0]));
    }
    
    public Link getSchedulesLink() {
        return schedules;
    }
    
    public void addSchedule(Schedule schedule) {
        if (schedule != null && schedules != null) {
            schedule.setCleaner(this);
            schedules.add(schedule);
        }
    }
    
    public void removeSchedule(Schedule schedule) {
        if (schedule != null && schedules != null) {
            schedule.setCleaner(null);
            int idx = schedules.indexOf(schedule);
            if (idx >= 0) {
                schedules.remove(idx);
            }
        }
    }
    
    public PerstUser getUser() { return getPerstUser(); }
    public void setUser(PerstUser user) { setPerstUser(user); }
    
    @Override
    public String toString() {
        return "Cleaner{name=" + getName() + ", schedules=" + (schedules != null ? schedules.size() : 0) + "}";
    }
}