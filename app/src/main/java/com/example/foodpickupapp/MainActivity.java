package com.example.foodpickupapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.RestaurantDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Restaurant;

import java.util.List;

/**
 * Main entry point for the FoodPickupApp.
 * Initializes the database and shows a confirmation screen.
 *
 * This is a placeholder activity for Sprint 2 (Database Setup).
 *
 * TODO: In Sprint 3, this will be replaced with a login/registration screen (FOOD-5, FOOD-6)
 * TODO: In Sprint 4, add navigation to the menu UI (FOOD-11)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        TextView textDbStatus = findViewById(R.id.textDbStatus);
        TextView textRestaurants = findViewById(R.id.textRestaurants);

        // Initialize the database (triggers table creation and seeding on first launch)
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);

        // Read the seeded restaurants to verify the database is working
        RestaurantDao restaurantDao = new RestaurantDao(dbHelper);
        List<Restaurant> restaurants = restaurantDao.getAllRestaurants();

        // Show the result on screen
        if (!restaurants.isEmpty()) {
            textDbStatus.setText(R.string.db_success);

            // Build a simple list of restaurant names
            StringBuilder sb = new StringBuilder("Registered Locations:\n\n");
            for (Restaurant restaurant : restaurants) {
                sb.append("• ").append(restaurant.getName()).append("\n");
            }
            textRestaurants.setText(sb.toString());
        } else {
            textDbStatus.setText(R.string.db_error);
        }
    }
}
