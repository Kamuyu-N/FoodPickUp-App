package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.RestaurantDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Restaurant;
import com.example.foodpickupapp.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Main screen for the FoodPickupApp.
 * Shows database status and restaurant locations after login.
 *
 * Requires an active session — redirects to LoginActivity if not logged in.
 *
 * TODO: In Sprint 4, add navigation to the menu UI (FOOD-11)
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in — redirect to login if not
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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

        // Set up logout button if it exists in the layout
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.clearSession();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
