package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.OrderDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.CartItem;
import com.example.foodpickupapp.model.Order;
import com.example.foodpickupapp.model.OrderItem;
import com.example.foodpickupapp.util.CartManager;
import com.example.foodpickupapp.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Checkout screen that shows a final order summary so the student
 * can review items and totals before confirming.
 *
 * On confirmation the order is saved to the database via OrderDao,
 * the cart is cleared, and the student is taken back to the main screen.
 *
 * Related to: FOOD-14 (proceed to a checkout screen to confirm order)
 */
public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Set up the toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbarCheckout);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // If the cart is empty, show a toast and finish
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, R.string.cart_empty_cannot_checkout, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate the order summary
        LinearLayout layoutOrderItems = findViewById(R.id.layoutOrderItems);
        TextView textCheckoutTotal = findViewById(R.id.textCheckoutTotal);

        for (CartItem cartItem : cartItems) {
            TextView lineView = new TextView(this);
            String line = String.format(Locale.US,
                    getString(R.string.checkout_item_line),
                    cartItem.getFoodItem().getName(),
                    cartItem.getQuantity(),
                    cartItem.getSubtotal());
            lineView.setText(line);
            lineView.setTextSize(16);
            lineView.setPadding(0, 8, 0, 8);
            layoutOrderItems.addView(lineView);
        }

        // Show the total
        double total = CartManager.getInstance().getTotal();
        textCheckoutTotal.setText(String.format(Locale.US,
                getString(R.string.checkout_total), total));

        // Set up "Confirm Order" button
        MaterialButton btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrder();
            }
        });
    }

    /**
     * Creates an Order and OrderItem list from the cart, saves them to the
     * database, clears the cart, and navigates back to MainActivity.
     */
    private void confirmOrder() {
        CartManager cartManager = CartManager.getInstance();
        List<CartItem> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            Toast.makeText(this, R.string.cart_empty_cannot_checkout, Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the Order object
        SessionManager sessionManager = new SessionManager(this);
        Order order = new Order();
        order.setUserId(sessionManager.getUserId());
        // Use the restaurant ID from the first item (simplified approach)
        order.setRestaurantId(cartItems.get(0).getFoodItem().getRestaurantId());
        order.setTotalAmount(cartManager.getTotal());
        order.setStatus("PLACED");

        // Build the OrderItem list
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setFoodItemId(cartItem.getFoodItem().getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getFoodItem().getPrice());
            orderItems.add(orderItem);
        }

        // Save to database
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        OrderDao orderDao = new OrderDao(dbHelper);
        long orderId = orderDao.insertOrder(order, orderItems);

        if (orderId != -1) {
            // Success — clear cart and go back to main
            cartManager.clearCart();
            Toast.makeText(this, R.string.order_confirmed, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Failed
            Toast.makeText(this, R.string.order_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
