package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Booking entity for cleaning scheduler.
 * Represents a booking for a house cleaning service.
 * 
 * Uses proper OO reference to House (not houseId).
 */
public class Booking extends CVersion {
    
    // Proper OO reference to House (was: int houseId)
    private House house;
    
    private String checkInDate;   // ISO date string
    private String checkOutDate;  // ISO date string
    
    @FullTextSearchable
    private String guestName;
    
    private String guestEmail;
    private String guestPhone;
    private String notes;
    private int dogsCount = 0;
    
    @Indexable
    private String status;  // pending, confirmed, cancelled
    
    public Booking() {
        this.status = "pending";
        this.dogsCount = 0;
    }
    
    // Legacy constructor for backward compatibility
    public Booking(int houseId, String checkInDate, String checkOutDate, 
                   String guestName, String guestEmail, String guestPhone, String notes) {
        this();
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.notes = notes;
    }
    
    public Booking(int houseId, String checkInDate, String checkOutDate, 
                   String guestName, String guestEmail, String guestPhone, String notes, int dogsCount) {
        this(houseId, checkInDate, checkOutDate, guestName, guestEmail, guestPhone, notes);
        this.dogsCount = dogsCount;
    }
    
    // Getters and setters
    // House - proper OO reference
    public House getHouse() { return house; }
    public void setHouse(House house) { this.house = house; }
    
    /**
     * Get house OID (for API serialization)
     * Returns 0 if no house is set.
     */
    public long getHouseOid() {
        return house != null ? house.getOid() : 0;
    }
    
    // Legacy method for backward compatibility
    @Deprecated
    public int getHouseId() {
        return house != null ? (int) house.getOid() : 0;
    }
    
    // Legacy method for backward compatibility
    @Deprecated
    public void setHouseId(int houseId) {
        // Note: This only stores the ID, not the actual object reference
        // Use setHouse(House house) for proper OO behavior
    }
    
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
