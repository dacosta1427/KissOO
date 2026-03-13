//-< CompileError.java >---------------------------------------------*--------*
// JSQL                       Version 1.04       (c) 1999  GARRET    *     ?  *
// (Java SQL)                                                        *   /\|  *
//                                                                   *  /  \  *
//                          Created:      5-Mar-99    K.A. Knizhnik  * / [] \ *
//                          Last update:  6-Mar-99    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Exception thrown by compiler
//-------------------------------------------------------------------*--------*

package org.garret.perst;

/**
 * Exception thrown by compiler
 */
public class CompileError extends RuntimeException { 
    private static final long serialVersionUID = 1L;

    public CompileError(String msg, int  pos) { 
	super(msg + " in position " + pos);
    }
}
