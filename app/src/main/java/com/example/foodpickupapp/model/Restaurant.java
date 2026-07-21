package com.example.foodpickupapp.model;

/**
 * Model class representing a restaurant/cafe location.
 * The system supports 3 locations: Paul's Cafe, Sironi (Freida), Sironi (SSHS).
 *
 * Related to: System Adaptation (multi-location architecture)
 */
public class Restaurant {

    private long id;
    private String name;
    private String locationCode; // e.g., "PAULS_CAFE", "SIRONI_FREIDA", "SIRONI_SSHS"

    /** Default constructor */
    public Restaurant() {}

    /** Full constructor */
    public Restaurant(long id, String name, String locationCode) {
        this.id = id;
        this.name = name;
        this.locationCode = locationCode;
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    @Override
    public String toString() {
        return "Restaurant{id=" + id + ", name='" + name + "', code='" + locationCode + "'}";
    }
}
