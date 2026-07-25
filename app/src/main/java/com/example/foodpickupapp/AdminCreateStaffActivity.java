package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.RestaurantDao;
import com.example.foodpickupapp.dao.UserDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Restaurant;
import com.example.foodpickupapp.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Admin-only screen for creating staff (cafeteria worker) accounts.
 * The admin assigns each staff member to a specific restaurant location.
 *
 * Related to: FOOD-8 (Admin - Staff Creation)
 */
public class AdminCreateStaffActivity extends AppCompatActivity {

    // Basic email format validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    // At least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    private TextInputLayout layoutStaffEmail, layoutStaffPassword;
    private TextInputEditText editStaffEmail, editStaffPassword;
    private Spinner spinnerRestaurant;
    private MaterialButton btnCreateStaff, btnLogout;
    private TextView textResult;

    private UserDao userDao;
    private RestaurantDao restaurantDao;
    private SessionManager sessionManager;
    private List<Restaurant> restaurantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check admin access — redirect if not an admin
        sessionManager = new SessionManager(this);
        if (!"ADMIN".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, getString(R.string.error_admin_only), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_create_staff);

        // Initialize database and DAOs
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        userDao = new UserDao(dbHelper);
        restaurantDao = new RestaurantDao(dbHelper);

        // Find views
        layoutStaffEmail = findViewById(R.id.layoutStaffEmail);
        layoutStaffPassword = findViewById(R.id.layoutStaffPassword);
        editStaffEmail = findViewById(R.id.editStaffEmail);
        editStaffPassword = findViewById(R.id.editStaffPassword);
        spinnerRestaurant = findViewById(R.id.spinnerRestaurant);
        btnCreateStaff = findViewById(R.id.btnCreateStaff);
        btnLogout = findViewById(R.id.btnLogout);
        textResult = findViewById(R.id.textResult);

        // Populate restaurant spinner from database
        loadRestaurants();

        // Create staff button
        btnCreateStaff.setOnClickListener(v -> createStaffAccount());

        // Logout button
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(AdminCreateStaffActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Loads all restaurants from the database and populates the spinner.
     */
    private void loadRestaurants() {
        restaurantList = restaurantDao.getAllRestaurants();
        List<String> restaurantNames = new ArrayList<>();
        restaurantNames.add(getString(R.string.spinner_select_prompt));
        for (Restaurant restaurant : restaurantList) {
            restaurantNames.add(restaurant.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, restaurantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRestaurant.setAdapter(adapter);
    }

    private void createStaffAccount() {
        // Clear previous errors and result
        layoutStaffEmail.setError(null);
        layoutStaffPassword.setError(null);
        textResult.setVisibility(View.GONE);

        String email = editStaffEmail.getText().toString().trim();
        String password = editStaffPassword.getText().toString().trim();
        int selectedPosition = spinnerRestaurant.getSelectedItemPosition();

        // Validate email
        if (email.isEmpty()) {
            layoutStaffEmail.setError(getString(R.string.error_empty_email));
            editStaffEmail.requestFocus();
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            layoutStaffEmail.setError(getString(R.string.error_invalid_email));
            editStaffEmail.requestFocus();
            return;
        }

        // Check duplicate email
        if (userDao.getUserByEmail(email) != null) {
            layoutStaffEmail.setError(getString(R.string.error_email_already_registered));
            editStaffEmail.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            layoutStaffPassword.setError(getString(R.string.error_empty_password));
            editStaffPassword.requestFocus();
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            layoutStaffPassword.setError(getString(R.string.error_password_too_weak));
            editStaffPassword.requestFocus();
            return;
        }

        // Validate restaurant selection (position 0 is the prompt)
        if (selectedPosition == 0) {
            Toast.makeText(this, getString(R.string.error_select_restaurant), Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected restaurant (offset by 1 because of the prompt item)
        Restaurant selectedRestaurant = restaurantList.get(selectedPosition - 1);

        // Insert staff user
        long userId = userDao.insertUser(email, password, "STAFF", selectedRestaurant.getId());
        if (userId > 0) {
            textResult.setText(getString(R.string.success_staff_created_details,
                    email, selectedRestaurant.getName()));
            textResult.setTextColor(getResources().getColor(R.color.teal_700));
            textResult.setVisibility(View.VISIBLE);

            // Clear the form for the next entry
            editStaffEmail.setText("");
            editStaffPassword.setText("");
            spinnerRestaurant.setSelection(0);
        } else {
            textResult.setText(getString(R.string.error_staff_creation_failed));
            textResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            textResult.setVisibility(View.VISIBLE);
        }
    }
}
