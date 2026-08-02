package com.example.foodpickupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.R;
import com.example.foodpickupapp.model.Order;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class KitchenOrderAdapter extends RecyclerView.Adapter<KitchenOrderAdapter.OrderViewHolder> {

    private List<Order> orderList = new ArrayList<>();
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onMarkAsPreparing(Order order);
        void onMarkAsReady(Order order);
    }

    public KitchenOrderAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kitchen_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView textOrderId;
        private final TextView textOrderStatus;
        private final TextView textOrderTotal;
        private final MaterialButton btnPreparing;
        private final MaterialButton btnReady;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
            btnPreparing = itemView.findViewById(R.id.btnPreparing);
            btnReady = itemView.findViewById(R.id.btnReady);

            btnPreparing.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMarkAsPreparing(orderList.get(position));
                }
            });

            btnReady.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMarkAsReady(orderList.get(position));
                }
            });
        }

        public void bind(Order order) {
            textOrderId.setText("Order #" + order.getId());
            textOrderStatus.setText(order.getStatus());
            textOrderTotal.setText(String.format("Total: $%.2f", order.getTotalAmount()));

            if ("PAID".equals(order.getStatus())) {
                btnPreparing.setVisibility(View.VISIBLE);
                btnReady.setVisibility(View.GONE);
                textOrderStatus.setTextColor(itemView.getContext().getColor(R.color.teal_700));
            } else if ("PREPARING".equals(order.getStatus())) {
                btnPreparing.setVisibility(View.GONE);
                btnReady.setVisibility(View.VISIBLE);
                textOrderStatus.setTextColor(itemView.getContext().getColor(R.color.purple_500));
            } else {
                btnPreparing.setVisibility(View.GONE);
                btnReady.setVisibility(View.GONE);
            }
        }
    }
}
