package example.domain;

import koo.framework.domain.FrameworkEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends FrameworkEntity {
    private String username;
    private String email;
    private String passwordHash;
    private boolean active = true;
    private boolean emailVerified = false;
    
    public User() {}
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}