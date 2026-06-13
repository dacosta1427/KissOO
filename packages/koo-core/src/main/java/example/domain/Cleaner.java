package example.domain;

import koo.framework.domain.FrameworkEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cleaner extends FrameworkEntity {
    private String name;
    private String email;
    private String phone;
    private String specialties;
    
    public Cleaner() {}
    
    public Cleaner(String name, String email) {
        this.name = name;
        this.email = email;
    }
}