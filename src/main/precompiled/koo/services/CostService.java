package koo.services;

import mycompany.oov.house.Booking;
import mycompany.oov.house.CostProfile;
import mycompany.oov.house.CostProfileManager;
import mycompany.oov.house.House;

/**
 * CostService - Service for calculating cleaning costs.
 * 
 * Uses CostProfile to calculate costs based on house characteristics.
 */
public class CostService {
    
    /**
     * Cost calculation result with breakdown
     */
    public static class CostResult {
        private double baseCost;
        private double sizeCost;
        private double roomCost;
        private double luxuryMultiplier;
        private double dogSurcharge;
        private double total;
        private String[] breakdown;
        
        public CostResult(double baseCost, double sizeCost, double roomCost, 
                         double luxuryMultiplier, double dogSurcharge, double total, 
                         String[] breakdown) {
            this.baseCost = baseCost;
            this.sizeCost = sizeCost;
            this.roomCost = roomCost;
            this.luxuryMultiplier = luxuryMultiplier;
            this.dogSurcharge = dogSurcharge;
            this.total = total;
            this.breakdown = breakdown;
        }
        
        // Getters
        public double getBaseCost() { return baseCost; }
        public double getSizeCost() { return sizeCost; }
        public double getRoomCost() { return roomCost; }
        public double getLuxuryMultiplier() { return luxuryMultiplier; }
        public double getDogSurcharge() { return dogSurcharge; }
        public double getTotal() { return total; }
        public String[] getBreakdown() { return breakdown; }
    }
    
    /**
     * Calculate cleaning cost for a house and booking
     * 
     * @param house The house to clean
     * @param booking The booking (for dogs count)
     * @param profile The cost profile to use (if null, uses standard)
     * @return CostResult with breakdown
     */
    public static CostResult calculateCost(House house, Booking booking, CostProfile profile) {
        if (profile == null) {
            profile = CostProfileManager.getStandard();
        }
        
        if (house == null || profile == null) {
            return new CostResult(0, 0, 0, 1.0, 0, 0, new String[]{"Error: Invalid input"});
        }
        
        java.util.List<String> breakdownList = new java.util.ArrayList<>();
        
        // 1. Estimate hours based on house size
        double estimatedHours = estimateHours(house);
        breakdownList.add(String.format("Estimated hours: %.1fh", estimatedHours));
        
        // 2. Base cost (hourly rate × hours, or minimum)
        double baseCost = Math.max(
            profile.getMinimumCharge(),
            estimatedHours * profile.getBaseHourlyRate()
        );
        breakdownList.add(String.format("Base cost: €%.2f (min €%.2f)", 
            baseCost, profile.getMinimumCharge()));
        
        // 3. Size cost (m² × rate)
        double surfaceM2 = house.getSurfaceM2() != null ? house.getSurfaceM2() : 0;
        double sizeCost = surfaceM2 * profile.getRatePerM2();
        breakdownList.add(String.format("Surface cost: %.0fm² × €%.2f = €%.2f", 
            surfaceM2, profile.getRatePerM2(), sizeCost));
        
        // Floor surcharge (beyond ground floor)
        int floors = house.getFloors() != null ? house.getFloors() : 1;
        double floorCost = Math.max(0, floors - 1) * profile.getRatePerFloor();
        if (floorCost > 0) {
            sizeCost += floorCost;
            breakdownList.add(String.format("Floor surcharge: %d extra floor(s) × €%.2f = €%.2f", 
                floors - 1, profile.getRatePerFloor(), floorCost));
        }
        
        // 4. Room costs
        int bedrooms = house.getBedrooms() != null ? house.getBedrooms() : 0;
        int bathrooms = house.getBathrooms() != null ? house.getBathrooms() : 0;
        
        double bedroomCost = bedrooms * profile.getRatePerBedroom();
        double bathroomCost = bathrooms * profile.getRatePerBathroom();
        double roomCost = bedroomCost + bathroomCost;
        
        if (bedroomCost > 0) {
            breakdownList.add(String.format("Bedrooms: %d × €%.2f = €%.2f", 
                bedrooms, profile.getRatePerBedroom(), bedroomCost));
        }
        if (bathroomCost > 0) {
            breakdownList.add(String.format("Bathrooms: %d × €%.2f = €%.2f", 
                bathrooms, profile.getRatePerBathroom(), bathroomCost));
        }
        
        // 5. Calculate subtotal before multiplier
        double subtotal = baseCost + sizeCost + roomCost;
        
        // 6. Apply luxury multiplier
        String luxuryLevel = house.getLuxuryLevel() != null ? house.getLuxuryLevel() : "standard";
        double luxuryMultiplier = profile.getMultiplierForLevel(luxuryLevel);
        
        if (luxuryMultiplier != 1.0) {
            breakdownList.add(String.format("Luxury multiplier (%s): ×%.2f", 
                luxuryLevel, luxuryMultiplier));
        }
        subtotal *= luxuryMultiplier;
        
        // 7. Dog surcharge
        double dogSurcharge = 0;
        int dogsCount = booking != null ? booking.getDogsCount() : 0;
        if (dogsCount > 0) {
            dogSurcharge = profile.getDogSurcharge();
            breakdownList.add(String.format("Dog surcharge: %d dog(s) = €%.2f", 
                dogsCount, dogSurcharge));
        }
        
        // 8. Total
        double total = subtotal + dogSurcharge;
        total = Math.round(total * 100) / 100.0;  // Round to cents
        
        breakdownList.add(String.format("TOTAL: €%.2f", total));
        
        String[] breakdown = breakdownList.toArray(new String[0]);
        
        return new CostResult(baseCost, sizeCost, roomCost, luxuryMultiplier, 
                             dogSurcharge, total, breakdown);
    }
    
    /**
     * Calculate cost with just house info (no booking needed for basic estimate)
     */
    public static CostResult calculateCost(House house, CostProfile profile) {
        return calculateCost(house, null, profile);
    }
    
    /**
     * Calculate cost using standard profile
     */
    public static CostResult calculateCost(House house) {
        return calculateCost(house, null, null);
    }
    
    /**
     * Estimate cleaning hours based on house characteristics
     */
    public static double estimateHours(House house) {
        double hours = 1.0;  // Base 1 hour
        
        // Add time based on surface (1h per 50m²)
        double surfaceM2 = house.getSurfaceM2() != null ? house.getSurfaceM2() : 0;
        hours += surfaceM2 / 50.0;
        
        // Add time for bedrooms (0.5h each)
        int bedrooms = house.getBedrooms() != null ? house.getBedrooms() : 0;
        hours += bedrooms * 0.5;
        
        // Add time for bathrooms (0.5h each - more intensive)
        int bathrooms = house.getBathrooms() != null ? house.getBathrooms() : 0;
        hours += bathrooms * 0.5;
        
        // Add time for extra floors (0.25h per extra floor)
        int floors = house.getFloors() != null ? house.getFloors() : 1;
        hours += Math.max(0, floors - 1) * 0.25;
        
        return Math.round(hours * 10) / 10.0;  // Round to 1 decimal
    }
    
    /**
     * Get the standard/default cost profile
     */
    public static CostProfile getDefaultProfile() {
        return CostProfileManager.getStandard();
    }
}
