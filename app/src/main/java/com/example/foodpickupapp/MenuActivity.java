package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.adapter.MenuAdapter;
import com.example.foodpickupapp.dao.FoodItemDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.FoodItem;
import com.example.foodpickupapp.util.CartManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

/**
 * Displays a scrollable list of available food items with names,
 * descriptions, prices, and categories. Each item has an "Add to Cart"
 * button so the student can build up their order.
 *
 * A floating "View Cart" button appears at the bottom once items are added,
 * showing the current item count.
 *
 * Related to: FOOD-11 (student sees a list of available food with prices)
 *             FOOD-12 (student can add items to a digital shopping cart)
 */
public class MenuActivity extends AppCompatActivity
        implements MenuAdapter.OnItemAddedToCartListener {

    private RecyclerView recyclerMenu;
    private TextView textMenuEmpty;
    private MenuAdapter menuAdapter;
    private MaterialButton btnViewCart;

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
        btnViewCart = findViewById(R.id.btnViewCart);

        // Set up RecyclerView
        menuAdapter = new MenuAdapter();
        menuAdapter.setOnItemAddedToCartListener(this);
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerMenu.setAdapter(menuAdapter);

        // Set up "View Cart" button — navigates to CartActivity
        btnViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, CartActivity.class));
            }
        });

        // Load food items from the database
        loadMenuItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the cart button whenever we return (e.g. after removing items in CartActivity)
        updateCartButton();
    }

    /**
     * Called when a student taps "Add to Cart" on a menu item. (FOOD-12)
     * Adds the item to the in-memory cart and shows a confirmation Snackbar.
     */
    @Override
    public void onAddToCart(FoodItem item) {
        CartManager.getInstance().addItem(item);
        updateCartButton();

        Snackbar.make(recyclerMenu, getString(R.string.added_to_cart), Snackbar.LENGTH_SHORT)
                .show();
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

    /**
     * Shows or hides the "View Cart" button based on the current cart count.
     * Updates the button text to show the number of items.
     */
    private void updateCartButton() {
        int count = CartManager.getInstance().getItemCount();
        if (count > 0) {
            btnViewCart.setText(String.format(Locale.US,
                    getString(R.string.btn_view_cart), count));
            btnViewCart.setVisibility(View.VISIBLE);
        } else {
            btnViewCart.setVisibility(View.GONE);
        }
    }
}
