package com.example.foodpickupapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foodpickupapp.dao.OrderDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Displays the live status of a student's order using a vertical stepper.
 * The screen auto-refreshes every 5 seconds to reflect status changes
 * made by staff (e.g., PLACED → PREPARING → READY).
 *
 * The student can see exactly when to walk to the café to collect their food.
 *
 * Related to: FOOD-21 (see order status change on screen)
 */
public class OrderStatusActivity extends AppCompatActivity {

    /** Key for the order ID passed via Intent extras. */
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    /** Interval in milliseconds between automatic status refreshes. */
    private static final long REFRESH_INTERVAL_MS = 5000;

    // All possible order statuses in progression order
    private static final String[] STATUS_KEYS = {
            "PLACED", "PAID", "PREPARING", "READY", "PICKED_UP"
    };

    private long orderId;
    private OrderDao orderDao;

    // UI references
    private TextView textOrderId;
    private TextView textOrderDate;
    private TextView textOrderTotal;
    private TextView textStatusMessage;
    private LinearLayout layoutStatusStepper;
    private TextView textAutoRefresh;

    // Auto-refresh handler
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        // Set up toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbarOrderStatus);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get order ID from intent
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize DAO
        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        orderDao = new OrderDao(dbHelper);

        // Find views
        textOrderId = findViewById(R.id.textOrderId);
        textOrderDate = findViewById(R.id.textOrderDate);
        textOrderTotal = findViewById(R.id.textOrderTotal);
        textStatusMessage = findViewById(R.id.textStatusMessage);
        layoutStatusStepper = findViewById(R.id.layoutStatusStepper);
        textAutoRefresh = findViewById(R.id.textAutoRefresh);

        // Set up Refresh button
        MaterialButton btnRefresh = findViewById(R.id.btnRefreshStatus);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadOrderAndUpdateUI();
            }
        });

        // Set up Back to Menu button
        MaterialButton btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderStatusActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Load and display the order
        loadOrderAndUpdateUI();

        // Set up auto-refresh
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadOrderAndUpdateUI();
                if (isRefreshing) {
                    refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    /**
     * Starts the periodic auto-refresh polling.
     */
    private void startAutoRefresh() {
        isRefreshing = true;
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
        textAutoRefresh.setVisibility(View.VISIBLE);
    }

    /**
     * Stops the periodic auto-refresh polling.
     */
    private void stopAutoRefresh() {
        isRefreshing = false;
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    /**
     * Loads the order from the database and updates the entire UI.
     */
    private void loadOrderAndUpdateUI() {
        Order order = orderDao.getOrderById(orderId);

        if (order == null) {
            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // Update order header
        textOrderId.setText(String.format(Locale.US,
                getString(R.string.order_id_label), order.getId()));

        String createdAt = order.getCreatedAt();
        if (createdAt != null && !createdAt.isEmpty()) {
            textOrderDate.setText(String.format(
                    getString(R.string.order_placed_at), createdAt));
            textOrderDate.setVisibility(View.VISIBLE);
        } else {
            textOrderDate.setVisibility(View.GONE);
        }

        textOrderTotal.setText(String.format(Locale.US,
                getString(R.string.order_total_label), order.getTotalAmount()));

        // Update the status message
        updateStatusMessage(order.getStatus());

        // Rebuild the stepper
        buildStatusStepper(order.getStatus());

        // If order is picked up, stop auto-refreshing
        if ("PICKED_UP".equals(order.getStatus())) {
            stopAutoRefresh();
            textAutoRefresh.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the friendly status message banner based on the current status.
     */
    private void updateStatusMessage(String currentStatus) {
        int messageResId;
        switch (currentStatus) {
            case "PAID":
                messageResId = R.string.order_status_msg_paid;
                break;
            case "PREPARING":
                messageResId = R.string.order_status_msg_preparing;
                break;
            case "READY":
                messageResId = R.string.order_status_msg_ready;
                break;
            case "PICKED_UP":
                messageResId = R.string.order_status_msg_picked_up;
                break;
            default:
                messageResId = R.string.order_status_msg_placed;
                break;
        }
        textStatusMessage.setText(messageResId);
    }

    /**
     * Builds the vertical status stepper by inflating a step view for each
     * status in the order lifecycle and styling it appropriately.
     *
     * @param currentStatus the order's current status
     */
    private void buildStatusStepper(String currentStatus) {
        layoutStatusStepper.removeAllViews();

        int currentIndex = getStatusIndex(currentStatus);

        for (int i = 0; i < STATUS_KEYS.length; i++) {
            View stepView = LayoutInflater.from(this)
                    .inflate(R.layout.item_status_step, layoutStatusStepper, false);

            View circle = stepView.findViewById(R.id.viewStatusCircle);
            View connector = stepView.findViewById(R.id.viewConnectorLine);
            TextView labelText = stepView.findViewById(R.id.textStatusLabel);
            TextView descText = stepView.findViewById(R.id.textStatusDescription);

            // Set the label and description text
            labelText.setText(getStatusLabel(STATUS_KEYS[i]));
            descText.setText(getStatusDescription(STATUS_KEYS[i]));

            if (i < currentIndex) {
                // Completed step
                circle.setBackgroundResource(R.drawable.bg_status_completed);
                labelText.setTextColor(ContextCompat.getColor(this, R.color.status_completed));
                descText.setTextColor(ContextCompat.getColor(this, R.color.status_completed));
            } else if (i == currentIndex) {
                // Current step
                circle.setBackgroundResource(R.drawable.bg_status_current);
                labelText.setTextColor(ContextCompat.getColor(this, R.color.status_current));
                descText.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
            } else {
                // Pending/future step
                circle.setBackgroundResource(R.drawable.bg_status_pending);
                labelText.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
                descText.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
            }

            // Style the connector line
            if (i == STATUS_KEYS.length - 1) {
                // Last step — hide connector
                connector.setVisibility(View.GONE);
            } else if (i < currentIndex) {
                // Completed connector
                connector.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.status_connector_done));
            } else {
                // Pending connector
                connector.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.status_connector_pending));
            }

            layoutStatusStepper.addView(stepView);
        }
    }

    /**
     * Returns the index (0-based) of the given status in the STATUS_KEYS array.
     * Returns 0 if the status is not recognized.
     */
    private int getStatusIndex(String status) {
        for (int i = 0; i < STATUS_KEYS.length; i++) {
            if (STATUS_KEYS[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Returns the user-friendly label for a given status key.
     */
    private int getStatusLabel(String statusKey) {
        switch (statusKey) {
            case "PLACED":
                return R.string.order_status_placed;
            case "PAID":
                return R.string.order_status_paid;
            case "PREPARING":
                return R.string.order_status_preparing;
            case "READY":
                return R.string.order_status_ready;
            case "PICKED_UP":
                return R.string.order_status_picked_up;
            default:
                return R.string.order_status_placed;
        }
    }

    /**
     * Returns the description text for a given status key.
     */
    private int getStatusDescription(String statusKey) {
        switch (statusKey) {
            case "PLACED":
                return R.string.order_status_desc_placed;
            case "PAID":
                return R.string.order_status_desc_paid;
            case "PREPARING":
                return R.string.order_status_desc_preparing;
            case "READY":
                return R.string.order_status_desc_ready;
            case "PICKED_UP":
                return R.string.order_status_desc_picked_up;
            default:
                return R.string.order_status_desc_placed;
        }
    }
}
