package koo.core.database;

import koo.core.actor.user.PerstUser;
import lombok.Getter;
import lombok.Setter;
import org.garret.perst.PersistentCollection;
import org.garret.perst.IPersistentSet;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import java.util.ArrayList;
import java.util.List;

/**
 * CDatabaseRoot - Perst database root object.
 * 
 * Required by Perst as the entry point for the database.
 * Stores references to all indexed collections.
 */
public class CDatabaseRoot extends CVersion {
    
    @Setter
    @Getter
    @Indexable
    private String name = "KissOO Database Root";
    
    private IPersistentSet<PerstUser> users = StorageManager.getStorage().createScalableSet();

    public CDatabaseRoot() {
    }

}
