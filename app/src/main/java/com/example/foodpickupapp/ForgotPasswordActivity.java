package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.UserDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * Password reset screen with a two-step flow.
 * Step 1: Verify that the email exists in the database.
 * Step 2: Set a new password.
 *
 * Related to: FOOD-7 (Password Reset)
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    // At least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    private TextInputLayout layoutEmail, layoutNewPassword, layoutConfirmNewPassword;
    private TextInputEditText editEmail, editNewPassword, editConfirmNewPassword;
    private MaterialButton btnVerifyEmail, btnResetPassword;
    private TextView textInstructions, textBackToLogin;
    private View layoutStep1, layoutStep2;

    private UserDao userDao;
    private long verifiedUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize database and DAO
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        userDao = new UserDao(dbHelper);

        // Find views — Step 1
        layoutEmail = findViewById(R.id.layoutEmail);
        editEmail = findViewById(R.id.editEmail);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        layoutStep1 = findViewById(R.id.layoutStep1);

        // Find views — Step 2
        layoutNewPassword = findViewById(R.id.layoutNewPassword);
        layoutConfirmNewPassword = findViewById(R.id.layoutConfirmNewPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmNewPassword = findViewById(R.id.editConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        layoutStep2 = findViewById(R.id.layoutStep2);

        textInstructions = findViewById(R.id.textInstructions);
        textBackToLogin = findViewById(R.id.textBackToLogin);

        // Step 1: Verify email
        btnVerifyEmail.setOnClickListener(v -> verifyEmail());

        // Step 2: Reset password
        btnResetPassword.setOnClickListener(v -> resetPassword());

        // Back to login
        textBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void verifyEmail() {
        layoutEmail.setError(null);

        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            layoutEmail.setError(getString(R.string.error_empty_email));
            editEmail.requestFocus();
            return;
        }

        User user = userDao.getUserByEmail(email);
        if (user == null) {
            layoutEmail.setError(getString(R.string.error_email_not_found));
            editEmail.requestFocus();
            return;
        }

        // Email verified — show step 2
        verifiedUserId = user.getId();
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        textInstructions.setText(getString(R.string.forgot_password_step2_instructions));
    }

    private void resetPassword() {
        layoutNewPassword.setError(null);
        layoutConfirmNewPassword.setError(null);

        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmNewPassword.getText().toString().trim();

        // Validate password strength
        if (newPassword.isEmpty()) {
            layoutNewPassword.setError(getString(R.string.error_empty_password));
            editNewPassword.requestFocus();
            return;
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            layoutNewPassword.setError(getString(R.string.error_password_too_weak));
            editNewPassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (!newPassword.equals(confirmPassword)) {
            layoutConfirmNewPassword.setError(getString(R.string.error_passwords_not_matching));
            editConfirmNewPassword.requestFocus();
            return;
        }

        // Update the password
        int rowsUpdated = userDao.updatePassword(verifiedUserId, newPassword);
        if (rowsUpdated > 0) {
            Toast.makeText(this, getString(R.string.success_password_reset), Toast.LENGTH_LONG).show();
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_password_reset_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
