package org.garret.perst;
import org.garret.perst.impl.StorageImpl;

/**
 * Base class for all persistent capable objects
 * It includes the finalize override method, which is deprecated
 */
public class Persistent extends PinnedPersistent
{ 
    public Persistent() {}

    public Persistent(Storage storage) { 
        super(storage);
    }

    @Deprecated
    protected void finalize() { 
        if ((state & DIRTY) != 0 && oid != 0) { 
            storage.storeFinalizedObject(this);
        }
        state = DELETED;
    }
}





