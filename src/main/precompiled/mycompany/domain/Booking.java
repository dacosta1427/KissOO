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
 */
@Getter @Setter
public class Booking extends CVersion {
    
    // Proper OO reference to House (was: int houseId)
    private House house;
    
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
    }
    
    // Convenience method for API serialization
    public long getHouseOid() {
        return house != null ? house.getOid() : 0;
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "house=" + (house != null ? house.getOid() : "null") +
                ", guestName='" + guestName + '\'' +
                ", dogsCount=" + dogsCount +
                ", status='" + status + '\'' +
                '}';
    }
}
