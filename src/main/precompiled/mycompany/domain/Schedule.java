package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

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
