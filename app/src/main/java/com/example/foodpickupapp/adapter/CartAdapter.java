package com.example.foodpickupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.R;
import com.example.foodpickupapp.model.CartItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the cart screen.
 * Each row shows item name, unit price, quantity controls (+/−),
 * subtotal, and a remove button.
 *
 * Related to: FOOD-12 (view cart items), FOOD-13 (change quantities)
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private CartItemListener listener;

    /**
     * Listener interface for cart item actions.
     */
    public interface CartItemListener {
        /** Called when the user taps + or − to change the quantity. */
        void onQuantityChanged(long foodItemId, int newQuantity);

        /** Called when the user taps the Remove button. */
        void onItemRemoved(long foodItemId);
    }

    /**
     * Sets the listener for cart item actions.
     *
     * @param listener the callback listener
     */
    public void setCartItemListener(CartItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        final long foodItemId = cartItem.getFoodItem().getId();
        final int quantity = cartItem.getQuantity();

        holder.textName.setText(cartItem.getFoodItem().getName());
        holder.textPrice.setText(
                String.format(Locale.US, "$%.2f", cartItem.getFoodItem().getPrice()));
        holder.textQuantity.setText(String.valueOf(quantity));
        holder.textSubtotal.setText(
                String.format(Locale.US, "$%.2f", cartItem.getSubtotal()));

        // Decrease quantity button (FOOD-13)
        holder.btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onQuantityChanged(foodItemId, quantity - 1);
                }
            }
        });

        // Increase quantity button (FOOD-13)
        holder.btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onQuantityChanged(foodItemId, quantity + 1);
                }
            }
        });

        // Remove item button
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemRemoved(foodItemId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Replaces the current list of cart items and refreshes the RecyclerView.
     *
     * @param newItems the updated list of cart items
     */
    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder that caches references to the views in each cart item row.
     */
    static class CartViewHolder extends RecyclerView.ViewHolder {

        final TextView textName;
        final TextView textPrice;
        final TextView textQuantity;
        final TextView textSubtotal;
        final MaterialButton btnDecrease;
        final MaterialButton btnIncrease;
        final MaterialButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textCartItemName);
            textPrice = itemView.findViewById(R.id.textCartItemPrice);
            textQuantity = itemView.findViewById(R.id.textCartQuantity);
            textSubtotal = itemView.findViewById(R.id.textCartSubtotal);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}
