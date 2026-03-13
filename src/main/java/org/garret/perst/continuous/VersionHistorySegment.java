package org.garret.perst.continuous;

import org.garret.perst.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class VersionHistorySegment extends Persistent 
{ 
    private static final long serialVersionUID = 1L;

    CVersionHistory vh;
    int from;
    int till;

    boolean containsLastVersion() 
    { 
        return vh.getNumberOfVersions() == till;
    }

    CVersion getCurrentVersion(long transId) 
    { 
        CVersion v = vh.getCurrent(transId);
        return v != null && v.id >= from && v.id <= till ? v : null;
    }

    void increment() 
    {
        till += 1;
        store();
    }

    void decrement() 
    {
        from += 1;
        store();
    }

    public VersionHistorySegment() {
    }

    public VersionHistorySegment(CVersion v) 
    { 
        super(v.getStorage());
        vh = v.history;
        Assert.that(vh != null);
        from = till = v.id;
    }

    public VersionHistorySegment(CVersionHistory vh, int from, int till) 
    { 
        super(vh.getStorage());
        this.vh = vh;
        this.from = from;
        this.till = till;
    }
}