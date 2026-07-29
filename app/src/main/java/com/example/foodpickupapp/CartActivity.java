package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.adapter.CartAdapter;
import com.example.foodpickupapp.model.CartItem;
import com.example.foodpickupapp.util.CartManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Displays the contents of the shopping cart.
 * The student can adjust quantities (+/−), remove items,
 * and proceed to the checkout screen.
 *
 * Related to: FOOD-12 (view cart), FOOD-13 (change quantities),
 *             FOOD-14 (proceed to checkout)
 */
public class CartActivity extends AppCompatActivity
        implements CartAdapter.CartItemListener {

    private RecyclerView recyclerCart;
    private TextView textCartEmpty;
    private TextView textCartTotal;
    private LinearLayout layoutCartBottom;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Set up the toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbarCart);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Find views
        recyclerCart = findViewById(R.id.recyclerCart);
        textCartEmpty = findViewById(R.id.textCartEmpty);
        textCartTotal = findViewById(R.id.textCartTotal);
        layoutCartBottom = findViewById(R.id.layoutCartBottom);

        // Set up RecyclerView
        cartAdapter = new CartAdapter();
        cartAdapter.setCartItemListener(this);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);

        // Set up "Proceed to Checkout" button (FOOD-14)
        MaterialButton btnProceedCheckout = findViewById(R.id.btnProceedCheckout);
        btnProceedCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            }
        });

        // Load cart contents
        refreshCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning from checkout (cart may have been cleared)
        refreshCart();
    }

    /**
     * Called when the user changes the quantity of a cart item. (FOOD-13)
     * If the new quantity is 0 or less, the item is removed.
     */
    @Override
    public void onQuantityChanged(long foodItemId, int newQuantity) {
        CartManager.getInstance().updateQuantity(foodItemId, newQuantity);
        refreshCart();
    }

    /**
     * Called when the user removes an item from the cart.
     */
    @Override
    public void onItemRemoved(long foodItemId) {
        CartManager.getInstance().removeItem(foodItemId);
        refreshCart();
    }

    /**
     * Reloads the cart contents from CartManager and updates the UI.
     * Shows the empty state if the cart is empty, or the item list and total otherwise.
     */
    private void refreshCart() {
        List<CartItem> items = CartManager.getInstance().getCartItems();

        if (items.isEmpty()) {
            recyclerCart.setVisibility(View.GONE);
            layoutCartBottom.setVisibility(View.GONE);
            textCartEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerCart.setVisibility(View.VISIBLE);
            layoutCartBottom.setVisibility(View.VISIBLE);
            textCartEmpty.setVisibility(View.GONE);
            cartAdapter.updateItems(items);
            textCartTotal.setText(String.format(Locale.US,
                    getString(R.string.cart_total), CartManager.getInstance().getTotal()));
        }
    }
}
