package com.example.foodpickupapp.model;

/**
 * Model class representing an item in the shopping cart.
 * Pairs a FoodItem with a quantity so the cart can track
 * how many of each item the student wants to order.
 *
 * Related to: FOOD-12 (add items to a digital shopping cart)
 */
public class CartItem {

    private FoodItem foodItem;
    private int quantity;

    /** Creates a cart item with the given food item and quantity. */
    public CartItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    // --- Getters and Setters ---

    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * Returns the subtotal for this cart line: price × quantity.
     */
    public double getSubtotal() {
        return foodItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return "CartItem{food='" + foodItem.getName() + "', qty=" + quantity
                + ", subtotal=" + getSubtotal() + "}";
    }
}
