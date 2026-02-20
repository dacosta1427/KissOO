package org.garret.perst.continuous;

/**
 * Exception thrown when application tries to delete non-current version 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class NotCurrentVersionException extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    /**
     * Get version which delete attempt was rejected
     */
    public CVersion getVersion() { 
        return v;
    }

    public NotCurrentVersionException(CVersion v) { 
        super("Attempt to delete non-current version");
        this.v = v;
    }

    private CVersion v;
}
