package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Booking entity for cleaning scheduler.
 * Represents a booking for a house cleaning service.
 */
public class Booking extends CVersion {
    
    @Indexable
    private int houseId;
    
    private String checkInDate;   // ISO date string
    private String checkOutDate;  // ISO date string
    
    @FullTextSearchable
    private String guestName;
    
    private String guestEmail;
    private String guestPhone;
    private String notes;
    
    @Indexable
    private String status;  // pending, confirmed, cancelled
    
    public Booking() {
        this.status = "pending";
    }
    
    public Booking(int houseId, String checkInDate, String checkOutDate, 
                   String guestName, String guestEmail, String guestPhone, String notes) {
        this.houseId = houseId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.notes = notes;
        this.status = "pending";
    }
    
    // Getters and setters
    public int getHouseId() { return houseId; }
    public void setHouseId(int houseId) { this.houseId = houseId; }
    
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Booking{" +
                "houseId=" + houseId +
                ", guestName='" + guestName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}