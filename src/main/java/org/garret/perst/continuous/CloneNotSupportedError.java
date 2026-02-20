package org.garret.perst.continuous;

/**
 * Exception rethrown when CloneNotSupportedException is catched
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CloneNotSupportedError extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    public CloneNotSupportedError() { 
        super("Clone is not supported");
    }
}
