package mycompany.database;

import mycompany.actor.cleaner.Cleaner;
import mycompany.actor.cleaner.Schedule;
import mycompany.actor.owner.Owner;
import koo.oodb.core.user.PerstUser;
import mycompany.oov.house.Booking;
import mycompany.oov.house.CostProfile;
import mycompany.oov.house.House;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import java.util.ArrayList;
import java.util.List;

/**
 * CDatabaseRoot - Perst database root object.
 * 
 * Required by Perst as the entry point for the database.
 * Stores references to all indexed collections.
 */
public class CDatabaseRoot extends CVersion {
    
    @Indexable
    private String name = "KissOO Database Root";
    
    private List<Owner> owners = new ArrayList<>();
    private List<Cleaner> cleaners = new ArrayList<>();
    private List<House> houses = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private List<Schedule> schedules = new ArrayList<>();
    private List<PerstUser> users = new ArrayList<>();
    private List<CostProfile> costProfiles = new ArrayList<>();
    
    public CDatabaseRoot() {
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<Owner> getOwners() { return owners; }
    public void setOwners(List<Owner> owners) { this.owners = owners; }
    
    public List<Cleaner> getCleaners() { return cleaners; }
    public void setCleaners(List<Cleaner> cleaners) { this.cleaners = cleaners; }
    
    public List<House> getHouses() { return houses; }
    public void setHouses(List<House> houses) { this.houses = houses; }
    
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
    
    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
    
    public List<PerstUser> getUsers() { return users; }
    public void setUsers(List<PerstUser> users) { this.users = users; }
    
    public List<CostProfile> getCostProfiles() { return costProfiles; }
    public void setCostProfiles(List<CostProfile> costProfiles) { this.costProfiles = costProfiles; }
}
