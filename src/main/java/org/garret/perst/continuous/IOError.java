package org.garret.perst.continuous;

import java.io.IOException;

/**
 * Wrapper for IOException
 */
public class IOError extends ContinuousException { 
    private static final long serialVersionUID = 1L;

    public IOError(IOException x) { 
        super(x);
    }
}