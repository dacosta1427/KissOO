package org.garret.perst.continuous;

/**
 * Base class for all exceptions thrown by this package
 */
public class ContinuousException extends RuntimeException 
{
    private static final long serialVersionUID = 1L;

    public ContinuousException(Throwable cause) {
        super(cause);
    }

    public ContinuousException(String message) { 
        super(message);
    }
}