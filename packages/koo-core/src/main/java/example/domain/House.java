package example.domain;

import koo.framework.domain.FrameworkEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class House extends FrameworkEntity {
    private Owner owner;
    private String address;
    private String city;
    private String country;
    private double size;
    private int bedrooms;
    private int bathrooms;
    private String description;
    
    public House() {}
    
    public House(Owner owner, String address) {
        this.owner = owner;
        this.address = address;
    }
}