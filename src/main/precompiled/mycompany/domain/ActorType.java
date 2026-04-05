package mycompany.domain;

/**
 * ActorType - Defines the type of Actor in the system.
 * 
 * NATURAL - Individual person, has a persisted PerstUser (deactivated by default).
 * CORPORATE - Company/organization, contains collection of Natural actors, no PU.
 * 
 * Default is NATURAL unless explicitly set to CORPORATE.
 */
public enum ActorType {
    NATURAL,
    CORPORATE
}