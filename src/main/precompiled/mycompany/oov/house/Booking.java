package mycompany.oov.house;

import lombok.Getter;
import lombok.Setter;
import mycompany.actor.owner.Owner;
import mycompany.actor.cleaner.Schedule;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import org.garret.perst.Link;
import org.garret.perst.Storage;

/**
 * Booking entity for cleaning scheduler.
 * Pure OO: house and owner are required, schedule is optional (1:1).
 */
@Getter @Setter
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