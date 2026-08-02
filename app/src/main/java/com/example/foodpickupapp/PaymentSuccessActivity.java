package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodpickupapp.util.CartManager;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Displays a payment success confirmation after the student completes payment.
 *
 * Shows the order number, transaction reference, and amount paid so the student
 * has a clear record of their purchase. The student can then navigate to the
 * order status screen to track their order, or return to the menu.
 *
 * This activity also clears the cart since the purchase is now finalized.
 *
 * Related to: FOOD-16 (see a success message and order number after paying)
 */
public class PaymentSuccessActivity extends AppCompatActivity {

    /** Key for the order ID passed via Intent extras. */
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    /** Key for the total amount passed via Intent extras. */
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";

    /** Key for the transaction reference passed via Intent extras. */
    public static final String EXTRA_TRANSACTION_REF = "extra_transaction_ref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Get extras from intent
        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        double totalAmount = getIntent().getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0);
        String transactionRef = getIntent().getStringExtra(EXTRA_TRANSACTION_REF);

        // Clear the cart now that payment is complete
        CartManager.getInstance().clearCart();

        // Find views
        TextView textOrderNumber = findViewById(R.id.textSuccessOrderNumber);
        TextView textTransactionRef = findViewById(R.id.textSuccessTransactionRef);
        TextView textAmount = findViewById(R.id.textSuccessAmount);
        MaterialButton btnViewOrderStatus = findViewById(R.id.btnViewOrderStatus);
        MaterialButton btnBackToMenu = findViewById(R.id.btnBackToMenuFromSuccess);

        // Populate the success details
        textOrderNumber.setText(String.format(Locale.US,
                getString(R.string.payment_success_order_value), orderId));

        if (transactionRef != null) {
            textTransactionRef.setText(transactionRef);
        }

        textAmount.setText(String.format(Locale.US,
                getString(R.string.payment_success_amount_value), totalAmount));

        // Set up "View Order Status" button
        btnViewOrderStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentSuccessActivity.this,
                        OrderStatusActivity.class);
                intent.putExtra(OrderStatusActivity.EXTRA_ORDER_ID, orderId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Set up "Back to Menu" button
        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentSuccessActivity.this,
                        MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Override back press to go to menu instead of back to payment.
     * The payment is already complete, so going back doesn't make sense.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
