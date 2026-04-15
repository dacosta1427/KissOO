package koo.oodb.core.actor;

/**
 * ActorType - Defines the type of AActor in the system.
 * 
 * NATURAL - Individual person, has a persisted PerstUser (deactivated by default).
 * CORPORATE - Company/organization, contains collection of Natural actors, no PU.
 * 
 * Default is NATURAL unless explicitly set to CORPORATE.
 */
public enum ActorType {
    ADMIN, // Admin for normal content mgt
    SYSADMIN, // Super system admin, has got the God power
    NATURAL,
    CORPORATE
}