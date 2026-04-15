package koo.oodb.core.actor;

import koo.oodb.core.user.PerstUser;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract  class ANaturalActor extends AActor {
    private PerstUser perstUser;  // persisted - NATURAL actors have PU, CORPORATE don't

    public ANaturalActor(String name, String cleaner1, Agreement agreement, ActorType actortype) {

        super(name, "Cleaner", new Agreement());
        actortype = ActorType.NATURAL;
    }

    private void createPerstUser() {
        String username = getName().toLowerCase().replaceAll("\\s+", "_") + "_" + getUuid().substring(0, 8);
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 16);
        ((ANaturalActor) this).setPerstUser(new PerstUser(username, tempPassword, this));
        ((ANaturalActor) this).getPerstUser().setActive(false);
        ((ANaturalActor) this).getPerstUser().setEmailVerified(false);
    }

    public boolean isNatural() {
        return true;
    }

    @Override
    public boolean isCorporate() { return false; }
}
