package koo.core.actor;

import koo.core.user.PerstUser;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class ANaturalActor extends AActor {
    private PerstUser perstUser;  // persisted - NATURAL actors have PU, CORPORATE don't
    
    public ANaturalActor(String name, Agreement agreement) {
        this(name, agreement, null);
    }
    
    public ANaturalActor(String name, Agreement agreement, String email) {
        super(name, agreement);
        createPerstUser(email);
    }
    
    private void createPerstUser(String email) {
        String username;
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 16);
        
        if (email != null && !email.isEmpty()) {
            username = email;
            this.perstUser = new PerstUser(username, tempPassword, this);
            this.perstUser.setEmail(email);
        } else {
            username = getName().toLowerCase().replaceAll("\\s+", "_") + "_" + getUuid().substring(0, 8);
            this.perstUser = new PerstUser(username, tempPassword, this);
        }
        
        this.perstUser.setActive(false);
        this.perstUser.setEmailVerified(false);
    }
    
    public boolean isNatural() {
        return true;
    }

    @Override
    public boolean isCorporate() { return false; }
}