package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.RestaurantDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Restaurant;
import com.example.foodpickupapp.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Main dashboard screen for the FoodPickupApp.
 * Displays a branded header with user greeting, restaurant location chips,
 * and role-aware quick-action cards (Browse Menu, Kitchen Dashboard, Admin Panel).
 *
 * Requires an active session — redirects to LoginActivity if not logged in.
 *
 * Ticket refs: FOOD-11 (View Menu navigation), FOOD-18 (Kitchen Dashboard link)
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

        // ------------------------------------------------------------------
        // Header: populate welcome greeting with user email
        // ------------------------------------------------------------------
        TextView textUserEmail = findViewById(R.id.textUserEmail);
        textUserEmail.setText(sessionManager.getUserEmail());

        // ------------------------------------------------------------------
        // Restaurant Locations: dynamically build chips from the database
        // ------------------------------------------------------------------
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        RestaurantDao restaurantDao = new RestaurantDao(dbHelper);
        List<Restaurant> restaurants = restaurantDao.getAllRestaurants();

        LinearLayout containerRestaurants = findViewById(R.id.containerRestaurants);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Restaurant restaurant : restaurants) {
            View chip = inflater.inflate(R.layout.item_restaurant_card, containerRestaurants, false);
            TextView textName = chip.findViewById(R.id.textRestaurantName);
            textName.setText(restaurant.getName());
            containerRestaurants.addView(chip);
        }

        // ------------------------------------------------------------------
        // Quick Actions: Browse Menu (visible to all logged-in users)
        // ------------------------------------------------------------------
        MaterialCardView cardBrowseMenu = findViewById(R.id.cardBrowseMenu);
        cardBrowseMenu.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MenuActivity.class))
        );

        // ------------------------------------------------------------------
        // Quick Actions: Kitchen Dashboard (visible to STAFF only) (FOOD-18)
        // ------------------------------------------------------------------
        MaterialCardView cardKitchenDashboard = findViewById(R.id.cardKitchenDashboard);
        String role = sessionManager.getUserRole();

        if ("STAFF".equals(role)) {
            cardKitchenDashboard.setVisibility(View.VISIBLE);
            cardKitchenDashboard.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, KitchenDashboardActivity.class))
            );
        }

        // ------------------------------------------------------------------
        // Quick Actions: Admin Panel (visible to ADMIN only) (FOOD-8)
        // ------------------------------------------------------------------
        MaterialCardView cardAdminPanel = findViewById(R.id.cardAdminPanel);
        if ("ADMIN".equals(role)) {
            cardAdminPanel.setVisibility(View.VISIBLE);
            cardAdminPanel.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, AdminCreateStaffActivity.class))
            );
        }

        // ------------------------------------------------------------------
        // Logout
        // ------------------------------------------------------------------
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
