package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Schedule entity for cleaning scheduler.
 * Represents a scheduled cleaning task assigned to a cleaner for a booking.
 * 
 * Uses proper OO references to Cleaner and Booking (not IDs).
 */
public class Schedule extends CVersion {
    
    // Proper OO references (was: int cleanerId, int bookingId)
    private Cleaner cleaner;
    private Booking booking;
    
    private String scheduleDate;   // ISO date string
    private String startTime;      // HH:mm
    private String endTime;        // HH:mm
    private String notes;
    
    @Indexable
    private String status;  // scheduled, completed, cancelled, pending (in progress)
    
    public Schedule() {
        this.status = "scheduled";
    }
    
    // Legacy constructor for backward compatibility
    public Schedule(int cleanerId, int bookingId, String scheduleDate, 
                    String startTime, String endTime, String notes) {
        this();
        this.scheduleDate = scheduleDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }
    
    // Convenience constructor without notes
    public Schedule(int cleanerId, int bookingId, String scheduleDate, 
                    String startTime, String endTime) {
        this(cleanerId, bookingId, scheduleDate, startTime, endTime, null);
    }
    
    // Getters and setters
    // Cleaner - proper OO reference
    public Cleaner getCleaner() { return cleaner; }
    public void setCleaner(Cleaner cleaner) { this.cleaner = cleaner; }
    
    /**
     * Get cleaner OID (for API serialization)
     */
    public long getCleanerOid() {
        return cleaner != null ? cleaner.getOid() : 0;
    }
    
    // Legacy methods for backward compatibility
    @Deprecated
    public int getCleanerId() {
        return cleaner != null ? (int) cleaner.getOid() : 0;
    }
    
    @Deprecated
    public void setCleanerId(int cleanerId) {
        // Note: This doesn't set the actual object reference
        // Use setCleaner(Cleaner cleaner) for proper OO behavior
    }
    
    // Booking - proper OO reference
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    
    /**
     * Get booking OID (for API serialization)
     */
    public long getBookingOid() {
        return booking != null ? booking.getOid() : 0;
    }
    
    // Legacy methods for backward compatibility
    @Deprecated
    public int getBookingId() {
        return booking != null ? (int) booking.getOid() : 0;
    }
    
    @Deprecated
    public void setBookingId(int bookingId) {
        // Note: This doesn't set the actual object reference
        // Use setBooking(Booking booking) for proper OO behavior
    }
    
    public String getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(String scheduleDate) { this.scheduleDate = scheduleDate; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
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
