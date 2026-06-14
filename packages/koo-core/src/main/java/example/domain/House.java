package example.domain;

import org.garret.perst.continuous.CVersion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class House extends CVersion {
    private Owner owner;
    private String address;
    private String city;
    private String country;
    private double size;
    private int bedrooms;
    private int bathrooms;
    private String description;
    private double checkInTime;
    private double checkOutTime;
    private double surfaceM2;
    private int floors;
    private int bathroomCount;
    private String luxuryLevel;
    private CostProfile costProfile;
    
    public House() {}
    
    public House(Owner owner, String address) {
        this.owner = owner;
        this.address = address;
    }
    
    public example.proto.HouseProto toProto() {
        return example.proto.HouseProto.newBuilder()
            .setOid(getOid())
            .setOwnerOid(owner != null ? owner.getOid() : 0)
            .setAddress(getAddress())
            .setCity(getCity())
            .setCountry(getCountry())
            .setSize(getSize())
            .setBedrooms(getBedrooms())
            .setBathrooms(getBathrooms())
            .build();
    }
}