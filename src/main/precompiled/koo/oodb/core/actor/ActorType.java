package koo.oodb.core.actor;

/**
 * ActorType - Defines the type of AActor in the system.
 * 
 * NATURAL - Individual person, has a persisted PerstUser (deactivated by default).
 * CORPORATE - Company/organization, contains collection of Natural actors, no PU.
 */
public enum ActorType {
    NATURAL,
    CORPORATE
}