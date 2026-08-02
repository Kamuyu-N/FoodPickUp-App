package com.example.foodpickupapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.adapter.KitchenOrderAdapter;
import com.example.foodpickupapp.dao.OrderDao;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Order;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class KitchenDashboardActivity extends AppCompatActivity implements KitchenOrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerViewOrders;
    private TextView textEmptyOrders;
    private KitchenOrderAdapter adapter;
    private OrderDao orderDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbarKitchen);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textEmptyOrders = findViewById(R.id.textEmptyOrders);

        FoodPickupDbHelper dbHelper = FoodPickupDbHelper.getInstance(this);
        orderDao = new OrderDao(dbHelper);

        adapter = new KitchenOrderAdapter(this);
        recyclerViewOrders.setAdapter(adapter);

        loadActiveOrders();
    }

    private void loadActiveOrders() {
        List<Order> activeOrders = orderDao.getActiveOrders();
        if (activeOrders.isEmpty()) {
            recyclerViewOrders.setVisibility(View.GONE);
            textEmptyOrders.setVisibility(View.VISIBLE);
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            textEmptyOrders.setVisibility(View.GONE);
            adapter.setOrders(activeOrders);
        }
    }

    @Override
    public void onMarkAsPreparing(Order order) {
        int updated = orderDao.updateOrderStatus(order.getId(), "PREPARING");
        if (updated > 0) {
            Toast.makeText(this, "Order marked as preparing", Toast.LENGTH_SHORT).show();
            loadActiveOrders();
        } else {
            Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMarkAsReady(Order order) {
        int updated = orderDao.updateOrderStatus(order.getId(), "READY");
        if (updated > 0) {
            Toast.makeText(this, "Order marked as ready", Toast.LENGTH_SHORT).show();
            loadActiveOrders();
        } else {
            Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
        }
    }
}
