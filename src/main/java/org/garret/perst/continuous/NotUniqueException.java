package org.garret.perst.continuous;

/**
 * Exception thrown when unique constraint violation is detected during transaction commit
 */
public class NotUniqueException extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    /**
     * Get version which field contains not unique value
     */
    public CVersion getVersion() { 
        return v;
    }

    public NotUniqueException(CVersion v) { 
        super("Unique constraint violated");
        this.v = v;
    }

    private CVersion v;
}
