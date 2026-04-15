package koo.oodb.core.actor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.garret.perst.PersistentCollection;
import org.garret.perst.impl.KDTree;

@Getter @Setter
@NoArgsConstructor
public class ACorporateActor extends AActor {
    
    private java.util.Set<ANaturalActor> naturalActors;
    
    public ACorporateActor(String name, Agreement agreement) {
        super(name, agreement);
        this.naturalActors = new java.util.HashSet<>();
    }
    
    public boolean addNaturalActor(ANaturalActor naturalActor) {
        if (naturalActors == null) {
            naturalActors = new java.util.HashSet<>();
        }
        return naturalActors.add(naturalActor);
    }
    
    public ANaturalActor getNaturalActor(String email) {
        if (naturalActors == null) return null;
        return naturalActors.stream()
            .filter(a -> a.getPerstUser() != null && email.equals(a.getPerstUser().getEmail()))
            .findFirst()
            .orElse(null);
    }
    
    public boolean removeNaturalActor(ANaturalActor naturalActor) {
        if (naturalActors == null) return false;
        return naturalActors.remove(naturalActor);
    }

    public boolean isNatural() {
        return false;
    }

    @Override
    public boolean isCorporate() { return true; }
}
