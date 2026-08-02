package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.dao.OrderDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.util.PaymentGateway;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Payment screen where the student enters card details to pay for their order.
 *
 * This activity receives the order ID and total amount from CheckoutActivity,
 * collects card information, and processes the payment through PaymentGateway.
 * On success, the order status is updated to "PAID" and the student is taken
 * to the PaymentSuccessActivity.
 *
 * Related to: FOOD-15 (integrate a payment API like Stripe, PayPal, or Mobile Money)
 */
public class PaymentActivity extends AppCompatActivity {

    /** Key for the order ID passed via Intent extras. */
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    /** Key for the total amount passed via Intent extras. */
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";

    private long orderId;
    private double totalAmount;

    // UI references
    private TextInputLayout layoutCardNumber;
    private TextInputLayout layoutExpiry;
    private TextInputLayout layoutCvv;
    private TextInputEditText editCardNumber;
    private TextInputEditText editExpiry;
    private TextInputEditText editCvv;
    private MaterialButton btnPayNow;
    private LinearLayout layoutPayButton;
    private LinearLayout layoutProcessing;

    // Background thread for payment processing
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get extras from intent
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        totalAmount = getIntent().getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0);

        if (orderId == -1 || totalAmount <= 0) {
            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbarPayment);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Find views
        TextView textPaymentAmount = findViewById(R.id.textPaymentAmount);
        layoutCardNumber = findViewById(R.id.layoutCardNumber);
        layoutExpiry = findViewById(R.id.layoutExpiry);
        layoutCvv = findViewById(R.id.layoutCvv);
        editCardNumber = findViewById(R.id.editCardNumber);
        editExpiry = findViewById(R.id.editExpiry);
        editCvv = findViewById(R.id.editCvv);
        btnPayNow = findViewById(R.id.btnPayNow);
        layoutPayButton = findViewById(R.id.layoutPayButton);
        layoutProcessing = findViewById(R.id.layoutProcessing);

        // Display the total amount
        textPaymentAmount.setText(String.format(Locale.US,
                getString(R.string.payment_amount_value), totalAmount));

        // Set the button text with the amount
        btnPayNow.setText(String.format(Locale.US,
                getString(R.string.btn_pay_now), totalAmount));

        // Set up Pay Now button
        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPayment();
            }
        });
    }

    /**
     * Validates the card input fields and initiates the payment process.
     * Shows error messages on invalid fields and switches to processing
     * state while the payment is being processed.
     */
    private void attemptPayment() {
        // Clear previous errors
        layoutCardNumber.setError(null);
        layoutExpiry.setError(null);
        layoutCvv.setError(null);

        // Get input values
        String cardNumber = editCardNumber.getText() != null
                ? editCardNumber.getText().toString().trim() : "";
        String expiry = editExpiry.getText() != null
                ? editExpiry.getText().toString().trim() : "";
        String cvv = editCvv.getText() != null
                ? editCvv.getText().toString().trim() : "";

        // Validate card number
        boolean hasError = false;
        if (cardNumber.isEmpty() || !cardNumber.matches("\\d{16}")) {
            layoutCardNumber.setError(getString(R.string.error_invalid_card_number));
            hasError = true;
        }

        // Validate expiry date
        if (expiry.isEmpty() || !expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            layoutExpiry.setError(getString(R.string.error_invalid_expiry));
            hasError = true;
        }

        // Validate CVV
        if (cvv.isEmpty() || !cvv.matches("\\d{3}")) {
            layoutCvv.setError(getString(R.string.error_invalid_cvv));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Show processing state
        showProcessing(true);

        // Process payment on background thread
        final String finalCardNumber = cardNumber;
        final String finalExpiry = expiry;
        final String finalCvv = cvv;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Call the payment gateway (simulates API call with delay)
                final PaymentGateway.PaymentResult result =
                        PaymentGateway.processPayment(finalCardNumber, finalExpiry,
                                finalCvv, totalAmount);

                // Handle result on main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onPaymentComplete(result);
                    }
                });
            }
        });
    }

    /**
     * Handles the payment result after the gateway responds.
     * On success: updates the order status to PAID and navigates to success screen.
     * On failure: shows an error toast and re-enables the form.
     */
    private void onPaymentComplete(PaymentGateway.PaymentResult result) {
        if (result.isSuccess()) {
            // Update order status and payment reference in the database
            FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
            OrderDao orderDao = new OrderDao(dbHelper);
            orderDao.updateOrderPayment(orderId, "PAID", result.getTransactionReference());

            // Navigate to success screen
            Intent intent = new Intent(this, PaymentSuccessActivity.class);
            intent.putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, orderId);
            intent.putExtra(PaymentSuccessActivity.EXTRA_TOTAL_AMOUNT, totalAmount);
            intent.putExtra(PaymentSuccessActivity.EXTRA_TRANSACTION_REF,
                    result.getTransactionReference());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Payment failed — show error and re-enable form
            showProcessing(false);
            String errorMessage = result.getErrorMessage() != null
                    ? result.getErrorMessage()
                    : getString(R.string.payment_error_generic);
            Toast.makeText(this,
                    String.format(getString(R.string.payment_failed), errorMessage),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Toggles between the normal pay button and the processing spinner.
     *
     * @param processing true to show processing state, false to show pay button
     */
    private void showProcessing(boolean processing) {
        if (processing) {
            layoutPayButton.setVisibility(View.GONE);
            layoutProcessing.setVisibility(View.VISIBLE);
            // Disable form inputs
            editCardNumber.setEnabled(false);
            editExpiry.setEnabled(false);
            editCvv.setEnabled(false);
        } else {
            layoutPayButton.setVisibility(View.VISIBLE);
            layoutProcessing.setVisibility(View.GONE);
            // Re-enable form inputs
            editCardNumber.setEnabled(true);
            editExpiry.setEnabled(true);
            editCvv.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
