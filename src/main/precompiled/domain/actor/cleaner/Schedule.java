package domain.actor.cleaner;

import domain.oov.house.Booking;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;

/**
 * Schedule entity for cleaning scheduler.
 * Represents a scheduled cleaning task assigned to a cleaner for a booking.
 */
public class Schedule extends CVersion {
    
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
    
    public Cleaner getCleaner() { return cleaner; }
    public void setCleaner(Cleaner cleaner) { this.cleaner = cleaner; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
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
