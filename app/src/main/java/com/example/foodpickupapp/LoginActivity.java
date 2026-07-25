package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.UserDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.User;
import com.example.foodpickupapp.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Login screen — the app's main entry point.
 * Authenticates students, staff, and admins using email and password.
 *
 * Related to: FOOD-6 (Login)
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText editEmail, editPassword;
    private MaterialButton btnLogin;
    private TextView textForgotPassword, textRegister;

    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already logged in, skip to the appropriate screen
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateByRole(sessionManager.getUserRole());
            return;
        }

        setContentView(R.layout.activity_login);

        // Initialize database and DAO
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        userDao = new UserDao(dbHelper);

        // Find views
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        textRegister = findViewById(R.id.textRegister);

        // Login button click
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Forgot password link
        textForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Register link
        textRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        // Clear previous errors
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_empty_email));
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            editPassword.requestFocus();
            return;
        }

        // Verify credentials against database
        if (userDao.verifyCredentials(email, password)) {
            User user = userDao.getUserByEmail(email);
            sessionManager.saveSession(user.getId(), user.getEmail(), user.getRole());
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            navigateByRole(user.getRole());
        } else {
            layoutPassword.setError(getString(R.string.error_invalid_credentials));
        }
    }

    /**
     * Routes the user to the correct screen based on their role.
     */
    private void navigateByRole(String role) {
        Intent intent;
        if ("ADMIN".equals(role)) {
            intent = new Intent(this, AdminCreateStaffActivity.class);
        } else {
            // STUDENT and STAFF both go to MainActivity for now
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
