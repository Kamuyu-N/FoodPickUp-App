package com.example.foodpickupapp;

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
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * Displays a scrollable list of available food items with names,
 * descriptions, prices, and categories.
 *
 * The student can browse all available menu items across all restaurants.
 *
 * Related to: FOOD-11 (student sees a list of available food with prices)
 */
public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerMenu;
    private TextView textMenuEmpty;
    private MenuAdapter menuAdapter;

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

        // Set up RecyclerView
        menuAdapter = new MenuAdapter();
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerMenu.setAdapter(menuAdapter);

        // Load food items from the database
        loadMenuItems();
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
