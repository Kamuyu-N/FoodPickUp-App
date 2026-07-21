package com.example.foodpickupapp.model;

/**
 * Model class representing a food item on a restaurant's menu.
 * Each food item belongs to a specific restaurant via restaurantId.
 *
 * Related to: FOOD-10 (add, edit, or remove food items)
 */
public class FoodItem {

    private long id;
    private long restaurantId;
    private String name;
    private String description;
    private double price;
    private String category;
    private boolean isAvailable;
    private String createdAt;
    private String updatedAt;

    /** Default constructor */
    public FoodItem() {
        this.isAvailable = true;
    }

    /** Full constructor */
    public FoodItem(long id, long restaurantId, String name, String description,
                    double price, String category, boolean isAvailable,
                    String createdAt, String updatedAt) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(long restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "FoodItem{id=" + id + ", name='" + name + "', price=" + price
                + ", restaurant=" + restaurantId + "}";
    }
}
