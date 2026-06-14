package example.domain;

import org.garret.perst.continuous.CVersion;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class Booking extends CVersion {
    private House house;
    private User guest;
    private Date startDate;
    private Date endDate;
    private double totalPrice;
    private String status;
    private String guestName;
    private String guestEmail;
    
    public Booking() {}
    
    public Booking(House house, User guest, Date startDate, Date endDate) {
        this.house = house;
        this.guest = guest;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public example.proto.BookingProto toProto() {
        return example.proto.BookingProto.newBuilder()
            .setOid(getOid())
            .setHouseOid(house != null ? house.getOid() : 0)
            .setGuestOid(guest != null ? guest.getOid() : 0)
            .setStartDate(getStartDate() != null ? getStartDate().getTime() : 0)
            .setEndDate(getEndDate() != null ? getEndDate().getTime() : 0)
            .setTotalPrice(getTotalPrice())
            .setStatus(getStatus() != null ? getStatus() : "confirmed")
            .build();
    }
}