package org.garret.perst.continuous;

/**
 * Exception thrown when application tries to insert in the database object which is already part of some other version history
 */
public class ObjectAlreadyInsertedException extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    /**
     * Version which insertion attempt was rejected
     */
    public CVersion getVersion() { 
        return v;
    }
 
    public ObjectAlreadyInsertedException(CVersion v) { 
        super("Transaction already started exception");
        this.v = v;
    }

    CVersion v;
}
