package com.example.foodpickupapp.util;

import com.example.foodpickupapp.model.CartItem;
import com.example.foodpickupapp.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton that manages the in-memory shopping cart.
 * Stores CartItem objects (FoodItem + quantity) in a simple list.
 * The cart lives in memory only — it is cleared when the app process dies.
 *
 * Related to: FOOD-12 (add items to cart), FOOD-13 (change quantities)
 */
public class CartManager {

    private static CartManager instance;

    private final List<CartItem> cartItems = new ArrayList<>();

    /** Private constructor — use getInstance(). */
    private CartManager() {}

    /** Returns the single CartManager instance. */
    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /**
     * Adds a food item to the cart. If the item is already in the cart,
     * its quantity is incremented by 1.
     *
     * @param foodItem the food item to add
     */
    public void addItem(FoodItem foodItem) {
        // Check if this item is already in the cart
        for (CartItem cartItem : cartItems) {
            if (cartItem.getFoodItem().getId() == foodItem.getId()) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                return;
            }
        }
        // Not found — add as a new entry with quantity 1
        cartItems.add(new CartItem(foodItem, 1));
    }

    /**
     * Removes an item from the cart entirely.
     *
     * @param foodItemId the ID of the food item to remove
     */
    public void removeItem(long foodItemId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getFoodItem().getId() == foodItemId) {
                cartItems.remove(i);
                return;
            }
        }
    }

    /**
     * Updates the quantity of an item in the cart.
     * If the new quantity is 0 or less, the item is removed.
     *
     * @param foodItemId  the ID of the food item
     * @param newQuantity the new quantity
     */
    public void updateQuantity(long foodItemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(foodItemId);
            return;
        }
        for (CartItem cartItem : cartItems) {
            if (cartItem.getFoodItem().getId() == foodItemId) {
                cartItem.setQuantity(newQuantity);
                return;
            }
        }
    }

    /** Returns the list of items currently in the cart. */
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    /** Returns the total price of all items in the cart. */
    public double getTotal() {
        double total = 0;
        for (CartItem cartItem : cartItems) {
            total += cartItem.getSubtotal();
        }
        return total;
    }

    /** Returns the number of distinct items in the cart. */
    public int getItemCount() {
        return cartItems.size();
    }

    /** Clears all items from the cart. */
    public void clearCart() {
        cartItems.clear();
    }
}
