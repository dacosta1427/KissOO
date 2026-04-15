package mycompany.actor.cleaner;

import lombok.Getter;
import lombok.Setter;
import mycompany.oov.house.Booking;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;

/**
 * Schedule entity for cleaning scheduler.
 * Represents a scheduled cleaning task assigned to a cleaner for a booking.
 * 
 * Uses proper OO references to Cleaner and Booking (not IDs).
 */
@Getter @Setter
public class Schedule extends CVersion {
    
    // Proper OO references (was: int cleanerId, int bookingId)
    private Cleaner cleaner;
    private Booking booking;
    
    private String scheduleDate;
    private String startTime;
    private String endTime;
    private String notes;
    
    @Indexable
    private String status = "scheduled";
    
    public Schedule() {
    }
    
    // Convenience methods for API serialization
    public long getCleanerOid() {
        return cleaner != null ? cleaner.getOid() : 0;
    }
    
    public long getBookingOid() {
        return booking != null ? booking.getOid() : 0;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    @Override
    public String toString() {
        return "Schedule{" +
                "cleaner=" + (cleaner != null ? cleaner.getOid() : "null") +
                ", booking=" + (booking != null ? booking.getOid() : "null") +
                ", date='" + scheduleDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
