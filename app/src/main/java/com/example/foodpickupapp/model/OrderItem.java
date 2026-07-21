package com.example.foodpickupapp.model;

/**
 * Model class representing a single item within an order.
 * Stores the quantity and the price at the time of purchase
 * (so the order total stays correct even if menu prices change later).
 *
 * Related to: FOOD-17 (save completed order details)
 */
public class OrderItem {

    private long id;
    private long orderId;
    private long foodItemId;
    private int quantity;
    private double priceAtPurchase;

    /** Default constructor */
    public OrderItem() {}

    /** Full constructor */
    public OrderItem(long id, long orderId, long foodItemId, int quantity, double priceAtPurchase) {
        this.id = id;
        this.orderId = orderId;
        this.foodItemId = foodItemId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public long getFoodItemId() { return foodItemId; }
    public void setFoodItemId(long foodItemId) { this.foodItemId = foodItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }

    @Override
    public String toString() {
        return "OrderItem{id=" + id + ", orderId=" + orderId + ", foodItemId=" + foodItemId
                + ", qty=" + quantity + ", price=" + priceAtPurchase + "}";
    }
}
