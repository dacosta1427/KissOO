package example.domain;

import koo.framework.domain.FrameworkEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostProfile extends FrameworkEntity {
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