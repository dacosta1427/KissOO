package services;

import org.kissweb.json.JSONArray;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.MainServlet;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.continuous.TransactionContainer;
import koo.core.actor.Role;
import koo.core.user.PerstUser;
import domain.oov.house.House;
import domain.oov.house.HouseManager;
import domain.oov.house.CostProfile;
import domain.oov.house.CostProfileManager;
import domain.actor.owner.Owner;
import domain.actor.owner.OwnerManager;

import java.util.Collection;

public class HouseService {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }

    public void getHouses(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            boolean admin = isAdmin(servlet);
            Owner owner = getCurrentOwner(servlet);

            Collection<House> houses;
            if (admin) {
                houses = HouseManager.getAll();
            } else if (owner != null) {
                houses = owner.getHouses();
            } else {
                houses = new java.util.ArrayList<>();
            }

            JSONArray rows = new JSONArray();
            for (House house : houses) {
                JSONObject row = new JSONObject();
                row.put("oid", house.getOid());
                row.put("name", house.getName());
                row.put("address", house.getAddress());
                row.put("description", house.getDescription());
                row.put("ownerOid", house.getOwnerOid());
                row.put("costProfileOid", house.getCostProfileOid());
                row.put("active", house.isActive());
                row.put("check_in_time", house.getCheckInTime());
                row.put("check_out_time", house.getCheckOutTime());
                row.put("surface_m2", house.getSurfaceM2());
                row.put("floors", house.getFloors());
                row.put("bedrooms", house.getBedrooms());
                row.put("bathrooms", house.getBathrooms());
                row.put("luxury_level", house.getLuxuryLevel());

                Owner houseOwner = house.getOwner();
                if (houseOwner != null) {
                    row.put("ownerName", houseOwner.getName());
                }

                rows.put(row);
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            House house = HouseManager.getByOid(oid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }

            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu != null) {
                koo.core.actor.AActor actor = pu.getActor();
                if (actor != null) {
                    boolean isAdmin = actor.getAgreement() != null &&
                        (actor.getAgreement().getRole() == Role.ADMIN || actor.getAgreement().getRole() == Role.SUPER_ADMIN);
                    if (!isAdmin) {
                        if (actor instanceof Owner) {
                            if (!((Owner) actor).getHouses().contains(house)) {
                                outjson.put("_Success", false);
                                outjson.put("_ErrorMessage", "Not authorized");
                                outjson.put("_ErrorCode", 3);
                                return;
                            }
                        } else {
                            outjson.put("_Success", false);
                            outjson.put("_ErrorMessage", "Not authorized");
                            outjson.put("_ErrorCode", 3);
                            return;
                        }
                    }
                }
            }
            JSONObject data = houseToJson(house);
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getOwnerHouses(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long ownerOid = injson.has("ownerOid") ? injson.getLong("ownerOid") : 0;
            Owner owner = OwnerManager.getByOid(ownerOid);
            if (owner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }

            Collection<House> houses = owner.getHouses();
            JSONArray rows = new JSONArray();

            for (House house : houses) {
                JSONObject row = new JSONObject();
                row.put("oid", house.getOid());
                row.put("name", house.getName());
                row.put("address", house.getAddress());
                row.put("description", house.getDescription());
                row.put("ownerOid", house.getOwnerOid());
                row.put("active", house.isActive());
                row.put("check_in_time", house.getCheckInTime());
                row.put("check_out_time", house.getCheckOutTime());
                rows.put(row);
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void createHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            JSONObject data = injson.getJSONObject("data");
            String name = data.getString("name");
            String address = data.has("address") ? data.getString("address") : "";
            String description = data.has("description") ? data.getString("description") : "";
            boolean active = data.has("active") ? data.getBoolean("active") : true;
            String checkInTime = data.has("check_in_time") ? data.getString("check_in_time") : "16:00";
            String checkOutTime = data.has("check_out_time") ? data.getString("check_out_time") : "10:00";

            long ownerOid = data.has("ownerOid") ? data.getLong("ownerOid") : 0;
            Owner owner = null;
            if (ownerOid > 0) {
                owner = OwnerManager.getByOid(ownerOid);
            }
            if (owner == null) {
                owner = getCurrentOwner(servlet);
            }

            House house = new House(owner, name, address, description, active);
            house.setCheckInTime(checkInTime);
            house.setCheckOutTime(checkOutTime);

            if (data.has("costProfileOid")) {
                long costProfileOid = data.getLong("costProfileOid");
                if (costProfileOid > 0) {
                    CostProfile costProfile = CostProfileManager.getByOid(costProfileOid);
                    house.setCostProfile(costProfile);
                }
            }

            if (data.has("surface_m2")) house.setSurfaceM2((double) data.get("surface_m2"));
            if (data.has("floors")) house.setFloors(data.getInt("floors"));
            if (data.has("bedrooms")) house.setBedrooms(data.getInt("bedrooms"));
            if (data.has("bathrooms")) house.setBathrooms(data.getInt("bathrooms"));
            if (data.has("luxury_level")) house.setLuxuryLevel(data.getString("luxury_level"));

            TransactionContainer tc = udbm.createContainer();
            tc.addInsert(house);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to create house");
                return;
            }
            JSONObject result = houseToJson(house);
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void updateHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            JSONObject data = injson.getJSONObject("data");
            House house = HouseManager.getByOid(oid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }
            if (data.has("name")) house.setName(data.getString("name"));
            if (data.has("address")) house.setAddress(data.getString("address"));
            if (data.has("description")) house.setDescription(data.getString("description"));
            if (data.has("active")) house.setActive(data.getBoolean("active"));
            if (data.has("check_in_time")) house.setCheckInTime(data.getString("check_in_time"));
            if (data.has("check_out_time")) house.setCheckOutTime(data.getString("check_out_time"));

            if (data.has("ownerOid")) {
                long ownerOid = data.getLong("ownerOid");
                if (ownerOid > 0) {
                    Owner owner = OwnerManager.getByOid(ownerOid);
                    house.setOwner(owner);
                } else {
                    house.setOwner(null);
                }
            }

            if (data.has("costProfileOid")) {
                long costProfileOid = data.getLong("costProfileOid");
                if (costProfileOid > 0) {
                    CostProfile costProfile = CostProfileManager.getByOid(costProfileOid);
                    house.setCostProfile(costProfile);
                } else {
                    house.setCostProfile(null);
                }
            }

            if (data.has("surface_m2")) house.setSurfaceM2((double) data.get("surface_m2"));
            if (data.has("floors")) house.setFloors(data.getInt("floors"));
            if (data.has("bedrooms")) house.setBedrooms(data.getInt("bedrooms"));
            if (data.has("bathrooms")) house.setBathrooms(data.getInt("bathrooms"));
            if (data.has("luxury_level")) house.setLuxuryLevel(data.getString("luxury_level"));

            TransactionContainer tc = udbm.createContainer();
            tc.addUpdate(house);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update house");
                return;
            }
            JSONObject result = houseToJson(house);
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void deleteHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            House house = HouseManager.getByOid(oid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }
            TransactionContainer tc = udbm.createContainer();
            tc.addDelete(house);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to delete house");
                return;
            }
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    private JSONObject houseToJson(House house) {
        JSONObject json = new JSONObject();
        json.put("oid", house.getOid());
        json.put("name", house.getName());
        json.put("address", house.getAddress());
        json.put("description", house.getDescription());
        json.put("ownerOid", house.getOwnerOid());
        json.put("costProfileOid", house.getCostProfileOid());
        json.put("active", house.isActive());
        json.put("check_in_time", house.getCheckInTime());
        json.put("check_out_time", house.getCheckOutTime());
        json.put("surface_m2", house.getSurfaceM2());
        json.put("floors", house.getFloors());
        json.put("bedrooms", house.getBedrooms());
        json.put("bathrooms", house.getBathrooms());
        json.put("luxury_level", house.getLuxuryLevel());
        return json;
    }

    private boolean isAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return false;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor == null) return false;
            koo.core.actor.Role role = actor.getAgreement() != null ? actor.getAgreement().getRole() : null;
            return role == Role.ADMIN || role == Role.SUPER_ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    private Owner getCurrentOwner(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return null;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor instanceof Owner) {
                return (Owner) actor;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}