package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.adapter.MenuAdapter;
import com.example.foodpickupapp.dao.FoodItemDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.FoodItem;
import com.example.foodpickupapp.util.CartManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;
import java.util.Locale;

/**
 * Displays a scrollable list of available food items with names,
 * descriptions, prices, and categories.
 *
 * The student can browse all available menu items across all restaurants,
 * add items to their cart, and navigate to the cart screen.
 *
 * Related to: FOOD-11 (student sees a list of available food with prices)
 *             FOOD-12 (navigate to cart)
 */
public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerMenu;
    private TextView textMenuEmpty;
    private MenuAdapter menuAdapter;
    private ExtendedFloatingActionButton fabCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Set up the toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbarMenu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Find views
        recyclerMenu = findViewById(R.id.recyclerMenu);
        textMenuEmpty = findViewById(R.id.textMenuEmpty);
        fabCart = findViewById(R.id.fabCart);

        // Set up RecyclerView and Adapter
        menuAdapter = new MenuAdapter();
        
        // Listen for "Add to Cart" clicks to update the cart badge
        menuAdapter.setOnCartChangedListener(new MenuAdapter.OnCartChangedListener() {
            @Override
            public void onCartChanged() {
                updateCartBadge();
            }
        });

        recyclerMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerMenu.setAdapter(menuAdapter);

        // Set up FAB click to go to CartActivity
        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CartManager.getInstance().getItemCount() > 0) {
                    startActivity(new Intent(MenuActivity.this, CartActivity.class));
                } else {
                    Toast.makeText(MenuActivity.this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load food items from the database
        loadMenuItems();
        
        // Update badge initially in case there are already items in the cart
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the cart badge when returning from the Cart screen (items may have been removed)
        updateCartBadge();
    }

    /**
     * Updates the FAB text to show the number of distinct items in the cart.
     */
    private void updateCartBadge() {
        int count = CartManager.getInstance().getItemCount();
        fabCart.setText(String.format(Locale.US, "Cart (%d)", count));
        if (count > 0) {
            fabCart.show();
        } else {
            // Optional: Hide or show it even when empty? Showing it empty is fine.
            fabCart.show();
        }
    }

    /**
     * Queries all available food items and updates the RecyclerView.
     * Shows an empty-state message if no items are found.
     */
    private void loadMenuItems() {
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        FoodItemDao foodItemDao = new FoodItemDao(dbHelper);

        List<FoodItem> items = foodItemDao.getAllAvailableItems();

        if (items.isEmpty()) {
            recyclerMenu.setVisibility(View.GONE);
            textMenuEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerMenu.setVisibility(View.VISIBLE);
            textMenuEmpty.setVisibility(View.GONE);
            menuAdapter.updateItems(items);
        }
    }
}
