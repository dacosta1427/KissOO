package example.domain;

import org.garret.perst.continuous.CVersion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostProfile extends CVersion {
    private String name;
    private double dailyRate;
    private double weeklyRate;
    private double monthlyRate;
    private String description;
    
    public CostProfile() {}
    
    public CostProfile(String name, double dailyRate) {
        this.name = name;
        this.dailyRate = dailyRate;
    }
}