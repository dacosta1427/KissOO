package services;

import org.kissweb.json.JSONArray;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import domain.oov.house.CostProfile;
import domain.oov.house.CostProfileManager;
import domain.oov.house.Booking;
import domain.actor.owner.Owner;
import domain.actor.owner.OwnerManager;

import java.util.Collection;

/**
 * CostProfileService - REST endpoints for CostProfile CRUD operations.
 */
public class CostProfileService {

    public void getCostProfiles(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<CostProfile> profiles = CostProfileManager.getAll();
            JSONArray rows = new JSONArray();

            for (CostProfile profile : profiles) {
                rows.put(costProfileToJson(profile));
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            CostProfile profile = CostProfileManager.getByOid(oid);
            if (profile == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cost profile not found");
                return;
            }
            outjson.put("data", costProfileToJson(profile));
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getStandardCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            CostProfile profile = CostProfileManager.getStandard();
            if (profile == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "No standard cost profile found");
                return;
            }
            outjson.put("data", costProfileToJson(profile));
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void createCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            JSONObject data = injson.getJSONObject("data");

            CostProfile profile = new CostProfile();
            profile.setName(data.getString("name"));
            profile.setStandard(data.has("is_standard") ? data.getBoolean("is_standard") : false);
            profile.setBaseHourlyRate(data.has("base_hourly_rate") ? data.getDouble("base_hourly_rate") : 25.0);
            profile.setMinimumCharge(data.has("minimum_charge") ? data.getDouble("minimum_charge") : 75.0);
            profile.setRatePerM2(data.has("rate_per_m2") ? data.getDouble("rate_per_m2") : 0.15);
            profile.setRatePerFloor(data.has("rate_per_floor") ? data.getDouble("rate_per_floor") : 15.0);
            profile.setRatePerBedroom(data.has("rate_per_bedroom") ? data.getDouble("rate_per_bedroom") : 10.0);
            profile.setRatePerBathroom(data.has("rate_per_bathroom") ? data.getDouble("rate_per_bathroom") : 15.0);
            profile.setDogSurcharge(data.has("dog_surcharge") ? data.getDouble("dog_surcharge") : 20.0);
            profile.setBasicMultiplier(data.has("basic_multiplier") ? data.getDouble("basic_multiplier") : 1.0);
            profile.setStandardMultiplier(data.has("standard_multiplier") ? data.getDouble("standard_multiplier") : 1.0);
            profile.setPremiumMultiplier(data.has("premium_multiplier") ? data.getDouble("premium_multiplier") : 1.25);
            profile.setLuxuryMultiplier(data.has("luxury_multiplier") ? data.getDouble("luxury_multiplier") : 1.5);
            profile.setActive(data.has("active") ? data.getBoolean("active") : true);

            if (data.has("ownerOid")) {
                long ownerOid = data.getLong("ownerOid");
                if (ownerOid > 0) {
                    Owner owner = OwnerManager.getByOid(ownerOid);
                    profile.setOwner(owner);
                }
            }

            if (CostProfileManager.create(profile) == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to create cost profile");
                return;
            }

            outjson.put("data", costProfileToJson(profile));
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void updateCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            JSONObject data = injson.getJSONObject("data");

            CostProfile profile = CostProfileManager.getByOid(oid);
            if (profile == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cost profile not found");
                return;
            }

            if (data.has("name")) profile.setName(data.getString("name"));
            if (data.has("is_standard")) profile.setStandard(data.getBoolean("is_standard"));
            if (data.has("base_hourly_rate")) profile.setBaseHourlyRate(data.getDouble("base_hourly_rate"));
            if (data.has("minimum_charge")) profile.setMinimumCharge(data.getDouble("minimum_charge"));
            if (data.has("rate_per_m2")) profile.setRatePerM2(data.getDouble("rate_per_m2"));
            if (data.has("rate_per_floor")) profile.setRatePerFloor(data.getDouble("rate_per_floor"));
            if (data.has("rate_per_bedroom")) profile.setRatePerBedroom(data.getDouble("rate_per_bedroom"));
            if (data.has("rate_per_bathroom")) profile.setRatePerBathroom(data.getDouble("rate_per_bathroom"));
            if (data.has("dog_surcharge")) profile.setDogSurcharge(data.getDouble("dog_surcharge"));
            if (data.has("basic_multiplier")) profile.setBasicMultiplier(data.getDouble("basic_multiplier"));
            if (data.has("standard_multiplier")) profile.setStandardMultiplier(data.getDouble("standard_multiplier"));
            if (data.has("premium_multiplier")) profile.setPremiumMultiplier(data.getDouble("premium_multiplier"));
            if (data.has("luxury_multiplier")) profile.setLuxuryMultiplier(data.getDouble("luxury_multiplier"));
            if (data.has("active")) profile.setActive(data.getBoolean("active"));

            if (data.has("ownerOid")) {
                long ownerOid = data.getLong("ownerOid");
                if (ownerOid > 0) {
                    Owner owner = OwnerManager.getByOid(ownerOid);
                    profile.setOwner(owner);
                } else {
                    profile.setOwner(null);
                }
            }

            if (!CostProfileManager.update(profile)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update cost profile");
                return;
            }

            outjson.put("data", costProfileToJson(profile));
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void deleteCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            CostProfile profile = CostProfileManager.getByOid(oid);
            if (profile == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cost profile not found");
                return;
            }
            if (!CostProfileManager.delete(profile)) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to delete cost profile");
                return;
            }
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void copyCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long sourceOid = injson.getLong("sourceOid");
            String newName = injson.getString("name");
            long ownerOid = injson.has("ownerOid") ? injson.getLong("ownerOid") : 0;

            CostProfile source = CostProfileManager.getByOid(sourceOid);
            if (source == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Source cost profile not found");
                return;
            }

            Owner owner = ownerOid > 0 ? OwnerManager.getByOid(ownerOid) : null;
            CostProfile copy = CostProfileManager.copyFrom(source, newName, owner);

            if (copy == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to copy cost profile");
                return;
            }

            outjson.put("data", costProfileToJson(copy));
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    private JSONObject costProfileToJson(CostProfile profile) {
        JSONObject json = new JSONObject();
        json.put("oid", profile.getOid());
        json.put("name", profile.getName());
        json.put("is_standard", profile.isStandard());
        json.put("ownerOid", profile.getOwner() != null ? profile.getOwner().getOid() : 0);
        json.put("base_hourly_rate", profile.getBaseHourlyRate());
        json.put("minimum_charge", profile.getMinimumCharge());
        json.put("rate_per_m2", profile.getRatePerM2());
        json.put("rate_per_floor", profile.getRatePerFloor());
        json.put("rate_per_bedroom", profile.getRatePerBedroom());
        json.put("rate_per_bathroom", profile.getRatePerBathroom());
        json.put("dog_surcharge", profile.getDogSurcharge());
        json.put("basic_multiplier", profile.getBasicMultiplier());
        json.put("standard_multiplier", profile.getStandardMultiplier());
        json.put("premium_multiplier", profile.getPremiumMultiplier());
        json.put("luxury_multiplier", profile.getLuxuryMultiplier());
        json.put("active", profile.isActive());
        return json;
    }

    public void calculateCost(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long houseOid = injson.has("houseOid") ? injson.getLong("houseOid") : 0;
            long bookingOid = injson.has("bookingOid") ? injson.getLong("bookingOid") : 0;
            long profileOid = injson.has("costProfileOid") ? injson.getLong("costProfileOid") : 0;

            domain.oov.house.House house = domain.oov.house.HouseManager.getByOid(houseOid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }

            CostProfile profile = profileOid > 0 ? CostProfileManager.getByOid(profileOid) : CostProfileManager.getStandard();
            if (profile == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cost profile not found");
                return;
            }

            Booking booking = bookingOid > 0 ? domain.oov.house.BookingManager.getByOid(bookingOid) : null;
            CostService.CostResult result = CostService.calculateCost(house, booking, profile);

            JSONObject data = new JSONObject();
            data.put("base_cost", result.getBaseCost());
            data.put("size_cost", result.getSizeCost());
            data.put("room_cost", result.getRoomCost());
            data.put("luxury_multiplier", result.getLuxuryMultiplier());
            data.put("dog_surcharge", result.getDogSurcharge());
            data.put("total", result.getTotal());
            JSONArray breakdown = new JSONArray();
            for (String line : result.getBreakdown()) {
                breakdown.put(line);
            }
            data.put("breakdown", breakdown);
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void estimateHours(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
long houseOid = injson.getLong("houseOid");
             domain.oov.house.House house = domain.oov.house.HouseManager.getByOid(houseOid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }
            double hours = CostService.estimateHours(house);
            outjson.put("estimated_hours", hours);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }
}