package mycompany.database;

import org.kissweb.database.Connection;

/**
 * A Connection wrapper for Perst OODBMS.
 * Used when no SQL database is configured but Perst is enabled.
 * 
 * This allows Login.groovy to receive a non-null Connection parameter
 * when using Perst-only mode, solving the Java reflection method
 * lookup issue with null parameters.
 */
public class PerstConnection extends Connection {
    
    public PerstConnection() {
        super(null);  // No underlying SQL connection
    }
}
