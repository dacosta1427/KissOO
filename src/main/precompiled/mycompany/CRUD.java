package mycompany;

/**
 * CRUD - Type-safe constants for CRUD actions.
 * Use these with Agreement.grant() to avoid string typos.
 */
public final class CRUD {
    
    private CRUD() {}  // Prevent instantiation
    
    public static final String CREATE = "create";
    public static final String READ = "read";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String EXECUTE = "execute";
    
    public static final String ALL = "*";
}
