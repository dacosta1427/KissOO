package example.domain;

import koo.framework.domain.FrameworkEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Owner extends FrameworkEntity {
    private User user;
    private String companyName;
    private String contactPerson;
    
    public Owner() {}
    
    public Owner(User user) {
        this.user = user;
    }
}