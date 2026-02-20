package org.garret.perst.continuous;

/**
 * Exception thrown when conflict is detected during transaction commit
 */
public class ConflictException extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    CVersion v;
    
    /**
     * Get version which is conflicted with version previously committed by another transaction
     */
    public CVersion getVersion() { 
        return v;
    }
    
    public ConflictException(CVersion v) { 
        super("Version conflict detected");
        this.v = v;
    }
}