package org.garret.perst.continuous;

import org.garret.perst.*;


@SuppressWarnings({"rawtypes", "unchecked"})
class RootObject extends PersistentResource 
{ 
    private static final long serialVersionUID = 1L;

    FieldIndex<TableDescriptor> tables;
    long transId;
    IPersistent userData;

    synchronized long getLastTransactionId() 
    { 
        return transId;
    }

    synchronized long newTransactionId() 
    { 
        long id = ++transId;
        store();
        return id;
    }

    public RootObject(Storage storage) 
    { 
        super(storage);
        transId = 0;
        tables = storage.<TableDescriptor>createFieldIndex(TableDescriptor.class, "className", true);
    }

    public RootObject() {}
}
    