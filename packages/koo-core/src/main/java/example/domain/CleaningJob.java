package example.domain;

import org.garret.perst.continuous.CVersion;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class CleaningJob extends CVersion {
    private Booking booking;
    private Cleaner cleaner;
    private Date scheduledDate;
    private String status;
    private String notes;
    
    public CleaningJob() {}
    
    public CleaningJob(Booking booking, Cleaner cleaner, Date scheduledDate) {
        this.booking = booking;
        this.cleaner = cleaner;
        this.scheduledDate = scheduledDate;
    }
}