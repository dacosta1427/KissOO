package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Booking entity for cleaning scheduler.
 * Represents a booking for a house cleaning service.
 * 
 * Uses proper OO reference to House (not houseId).
 * 
 * Bidirectional relationships:
 * - Booking HAS house (single reference)
 * - Booking HAS schedule (single reference - one cleaning per booking)
 * - Schedule HAS booking (single reference)
 */
@Getter @Setter
public class Booking extends CVersion {
    
    // Proper OO reference to House (was: int houseId)
    private House house;
    
    // One schedule per booking (the cleaning for this stay)
    private Schedule schedule;
    
    private String checkInDate;
    private String checkOutDate;
    
    @Indexable
    private String guestName;

    @Indexable
    private String guestEmail;
    private String guestPhone;
    @FullTextSearchable
    private String notes;
    private int dogsCount = 0;
    
    @Indexable
    private String status = "pending";
    
    public Booking() {
    }
    
    public Booking(House house, String checkInDate, String checkOutDate,
                   String guestName, String guestEmail, String guestPhone, String notes) {
        this();
        this.house = house;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.notes = notes;
        // Bidirectional: add this booking to house's collection
        if (house != null) {
            house.getBookings().add(this);
        }
    }
    
    // Convenience method for API serialization
    public long getHouseOid() {
        return house != null ? house.getOid() : 0;
    }
    
    public long getScheduleOid() {
        return schedule != null ? schedule.getOid() : 0;
    }
    
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        if (schedule != null && schedule.getBooking() != this) {
            schedule.setBooking(this);
        }
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "house=" + (house != null ? house.getOid() : "null") +
                ", guestName='" + guestName + '\'' +
                ", dogsCount=" + dogsCount +
                ", status='" + status + '\'' +
                ", schedule=" + (schedule != null ? schedule.getOid() : "null") +
                '}';
    }
}
