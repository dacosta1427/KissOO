package example.domain;

import org.garret.perst.continuous.CVersion;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class Invoice extends CVersion {
    private User user;
    private House house;
    private double amount;
    private Date issueDate;
    private Date dueDate;
    private String status;
    private String pdfPath;
    
    public Invoice() {}
    
    public Invoice(User user, House house, double amount) {
        this.user = user;
        this.house = house;
        this.amount = amount;
        this.issueDate = new Date();
    }
}