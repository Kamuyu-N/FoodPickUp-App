package com.example.foodpickupapp.model;

/**
 * Model class representing a user in the system.
 * Supports three roles: STUDENT, STAFF, and ADMIN.
 * Staff members are optionally linked to a specific restaurant.
 *
 * Related to: FOOD-9 (user credentials table)
 */
public class User {

    private long id;
    private String email;
    private String passwordHash;
    private String salt;
    private String role;          // "STUDENT", "STAFF", or "ADMIN"
    private long restaurantId;    // Only applicable for STAFF; -1 if not assigned
    private String createdAt;

    /** Default constructor */
    public User() {
        this.restaurantId = -1;
    }

    /** Full constructor */
    public User(long id, String email, String passwordHash, String salt,
                String role, long restaurantId, String createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.restaurantId = restaurantId;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(long restaurantId) { this.restaurantId = restaurantId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role='" + role + "'}";
    }
}
