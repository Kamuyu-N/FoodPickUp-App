package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.UserDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * Registration screen for student self-registration.
 * Validates university email format and password strength.
 *
 * Related to: FOOD-5 (Registration)
 */
public class RegisterActivity extends AppCompatActivity {

    // Accepts emails ending in .edu, .ac.xx (e.g. .ac.uk, .ac.za), or university.xx
    private static final Pattern UNIVERSITY_EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@.+\\.(edu|ac\\.[a-z]{2,}|university\\.[a-z]{2,})$",
            Pattern.CASE_INSENSITIVE
    );

    // At least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    private TextInputLayout layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText editEmail, editPassword, editConfirmPassword;
    private MaterialButton btnRegister;
    private TextView textLogin;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database and DAO
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        userDao = new UserDao(dbHelper);

        // Find views
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        textLogin = findViewById(R.id.textLogin);

        // Register button click
        btnRegister.setOnClickListener(v -> attemptRegistration());

        // Login link
        textLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegistration() {
        // Clear previous errors
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // Validate university email
        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_empty_email));
            editEmail.requestFocus();
            return;
        }
        if (!UNIVERSITY_EMAIL_PATTERN.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_invalid_university_email));
            editEmail.requestFocus();
            return;
        }

        // Check if email is already registered
        if (userDao.getUserByEmail(email) != null) {
            layoutEmail.setError(getString(R.string.error_email_already_registered));
            editEmail.requestFocus();
            return;
        }

        // Validate password strength
        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            editPassword.requestFocus();
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            layoutPassword.setError(getString(R.string.error_password_too_weak));
            editPassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError(getString(R.string.error_passwords_not_matching));
            editConfirmPassword.requestFocus();
            return;
        }

        // Insert user as STUDENT
        long userId = userDao.insertUser(email, password, "STUDENT", -1);
        if (userId > 0) {
            Toast.makeText(this, getString(R.string.success_registration), Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_registration_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
