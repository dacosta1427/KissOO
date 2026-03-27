package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;

/**
 * CostProfile entity for cleaning scheduler.
 * Defines pricing rules for cleaning services.
 * 
 * A CostProfile can be:
 * - Global (isStandard=true, owner=null) - the default profile
 * - Owner-specific (owner != null) - custom profile for an owner
 */
public class CostProfile extends CVersion {
    
    @Indexable
    private String name;                    // "Standard", "Custom Beach House"
    
    @Indexable
    private boolean isStandard;             // Only one standard profile allowed
    
    private Owner owner;                    // null = global, else owner-specific (OO ref)
    
    // Base rates
    private double baseHourlyRate = 25.0;   // €/hour for basic cleaning
    private double minimumCharge = 75.0;    // € minimum per cleaning
    
    // Size factors
    private double ratePerM2 = 0.15;        // € per m² of cleaning surface
    private double ratePerFloor = 15.0;     // € per additional floor (beyond ground)
    private double ratePerBedroom = 10.0;   // € per bedroom
    private double ratePerBathroom = 15.0;  // € per bathroom (more intensive)
    
    // Special surcharges
    private double dogSurcharge = 20.0;     // € flat fee for dogs
    
    // Luxury level multipliers
    private double basicMultiplier = 1.0;
    private double standardMultiplier = 1.0;
    private double premiumMultiplier = 1.25;
    private double luxuryMultiplier = 1.5;
    
    private boolean active = true;
    
    // Constructors
    public CostProfile() {
    }
    
    public CostProfile(String name, boolean isStandard) {
        this.name = name;
        this.isStandard = isStandard;
        this.active = true;
    }
    
    public CostProfile(String name, Owner owner) {
        this.name = name;
        this.isStandard = false;
        this.owner = owner;
        this.active = true;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public boolean isStandard() { return isStandard; }
    public void setStandard(boolean standard) { isStandard = standard; }
    
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    
    public double getBaseHourlyRate() { return baseHourlyRate; }
    public void setBaseHourlyRate(double baseHourlyRate) { this.baseHourlyRate = baseHourlyRate; }
    
    public double getMinimumCharge() { return minimumCharge; }
    public void setMinimumCharge(double minimumCharge) { this.minimumCharge = minimumCharge; }
    
    public double getRatePerM2() { return ratePerM2; }
    public void setRatePerM2(double ratePerM2) { this.ratePerM2 = ratePerM2; }
    
    public double getRatePerFloor() { return ratePerFloor; }
    public void setRatePerFloor(double ratePerFloor) { this.ratePerFloor = ratePerFloor; }
    
    public double getRatePerBedroom() { return ratePerBedroom; }
    public void setRatePerBedroom(double ratePerBedroom) { this.ratePerBedroom = ratePerBedroom; }
    
    public double getRatePerBathroom() { return ratePerBathroom; }
    public void setRatePerBathroom(double ratePerBathroom) { this.ratePerBathroom = ratePerBathroom; }
    
    public double getDogSurcharge() { return dogSurcharge; }
    public void setDogSurcharge(double dogSurcharge) { this.dogSurcharge = dogSurcharge; }
    
    public double getBasicMultiplier() { return basicMultiplier; }
    public void setBasicMultiplier(double basicMultiplier) { this.basicMultiplier = basicMultiplier; }
    
    public double getStandardMultiplier() { return standardMultiplier; }
    public void setStandardMultiplier(double standardMultiplier) { this.standardMultiplier = standardMultiplier; }
    
    public double getPremiumMultiplier() { return premiumMultiplier; }
    public void setPremiumMultiplier(double premiumMultiplier) { this.premiumMultiplier = premiumMultiplier; }
    
    public double getLuxuryMultiplier() { return luxuryMultiplier; }
    public void setLuxuryMultiplier(double luxuryMultiplier) { this.luxuryMultiplier = luxuryMultiplier; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    /**
     * Get multiplier for a given luxury level
     */
    public double getMultiplierForLevel(String luxuryLevel) {
        if (luxuryLevel == null) return standardMultiplier;
        return switch (luxuryLevel.toLowerCase()) {
            case "basic" -> basicMultiplier;
            case "premium" -> premiumMultiplier;
            case "luxury" -> luxuryMultiplier;
            default -> standardMultiplier;
        };
    }
    
    @Override
    public String toString() {
        return "CostProfile{" +
                "name='" + name + '\'' +
                ", isStandard=" + isStandard +
                ", owner=" + (owner != null ? owner.getOid() : "null") +
                ", baseHourlyRate=" + baseHourlyRate +
                ", active=" + active +
                '}';
    }
}
