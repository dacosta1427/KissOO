package domain.oov.house;

import domain.actor.owner.Owner;
import domain.actor.cleaner.Schedule;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Booking entity for cleaning scheduler.
 * Pure OO: house and owner are required, schedule is optional (1:1).
 */
public class Booking extends CVersion {
      
    private House house;
    private Owner owner;
    private Schedule schedule;  // 1:1 relationship
    
    private String checkInDate;
    private String checkOutDate;
    
    @FullTextSearchable
    private String guestName;
    
    private String guestEmail;
    private String guestPhone;
    private String notes;
    private int dogsCount = 0;
    
    @Indexable
    private String status = "pending";
    
    public Booking() {
    }
    
    public Booking(House house, Owner owner, String checkInDate, String checkOutDate,
                    String guestName, String guestEmail, String guestPhone, String notes) {
        this();
        this.house = house;
        this.owner = owner;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.notes = notes;
    }
    
    public House getHouse() { return house; }
    public void setHouse(House house) { this.house = house; }
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getDogsCount() { return dogsCount; }
    public void setDogsCount(int dogsCount) { this.dogsCount = dogsCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public long getHouseOid() {
        return house != null ? house.getOid() : 0;
    }
    
    public long getOwnerOid() {
        return owner != null ? owner.getOid() : 0;
    }
    
    public long getScheduleOid() {
        return schedule != null ? schedule.getOid() : 0;
    }
    
    @Override
    public String toString() {
        return "Booking{house=" + (house != null ? house.getOid() : "null") +
                ", owner=" + (owner != null ? owner.getOid() : "null") +
                ", schedule=" + (schedule != null ? schedule.getOid() : "null") +
                ", guestName=" + guestName + "}";
    }
}
