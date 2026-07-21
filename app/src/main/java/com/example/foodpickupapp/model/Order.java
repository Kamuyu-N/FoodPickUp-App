package com.example.foodpickupapp.model;

/**
 * Model class representing an order placed by a student.
 * Each order is linked to a user and a specific restaurant.
 *
 * Statuses: PLACED → PAID → PREPARING → READY → PICKED_UP
 *
 * Related to: FOOD-17 (save completed order details)
 */
public class Order {

    private long id;
    private long userId;
    private long restaurantId;
    private double totalAmount;
    private String status;            // PLACED, PAID, PREPARING, READY, PICKED_UP
    private String paymentReference;
    private String createdAt;
    private String updatedAt;

    /** Default constructor */
    public Order() {
        this.status = "PLACED";
    }

    /** Full constructor */
    public Order(long id, long userId, long restaurantId, double totalAmount,
                 String status, String paymentReference, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentReference = paymentReference;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(long restaurantId) { this.restaurantId = restaurantId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", userId=" + userId + ", status='" + status
                + "', total=" + totalAmount + "}";
    }
}
