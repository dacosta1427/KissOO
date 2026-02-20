package org.garret.perst.continuous;

/**
 * Exception thrown when application tries to update more than one version from the same version history within one transaction
 */
public class AmbiguousVersionException extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    /**
     * Get version which update attempt was rejected
     */
    public CVersion getVersion() { 
        return v;
    }

    public AmbiguousVersionException(CVersion v) { 
        super("Attempt to update more than one version from the same version history in one transaction");
        this.v = v;
    }

    private CVersion v;
}
