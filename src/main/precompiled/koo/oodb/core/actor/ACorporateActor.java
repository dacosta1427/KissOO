package koo.oodb.core.actor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.garret.perst.PersistentCollection;
import org.garret.perst.impl.KDTree;

@Getter @Setter
@NoArgsConstructor
public class ACorporateActor extends AActor {
    // TODO Add a multikey to the Corporate actor, create an Annotation?
//    String[] flds = new String[2];//
//    flds[0]="name"; flds[1]="name";
//    MultidimensionalComparator<ACorporateActor> comparator = new ReflectionMultidimensionalComparator<>(db, ACorporateActor.class, flds, true);
    static KDTree<ACorporateActor> corporations;
    private PersistentCollection<ANaturalActor> naturalActors;

    public boolean addNaturalActor(ANaturalActor naturalActor) {
        return naturalActors.add(naturalActor);
    }

    public ANaturalActor getNaturalActor(String email) {
        // TODO Complete method
        return null;
    }

    public boolean removeNaturalActor(ANaturalActor naturalActor) {
        return naturalActors.remove(naturalActor);
    }

    private void createCorporateUsers() {
        // TODO to be implemented
    }

    public boolean isNatural() {
        return false;
    }

    @Override
    public boolean isCorporate() { return true; }

}
