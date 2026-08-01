package com.example.foodpickupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodpickupapp.R;
import com.example.foodpickupapp.model.FoodItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter that displays a list of available food items.
 * Each row shows the item name, description, price, and category.
 *
 * Related to: FOOD-11 (student sees a list of available food with prices)
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<FoodItem> items = new ArrayList<>();

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        FoodItem item = items.get(position);

        holder.textName.setText(item.getName());
        holder.textDescription.setText(item.getDescription());
        holder.textPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice()));

        // Show category if available, hide if null/empty
        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            holder.textCategory.setText(item.getCategory());
            holder.textCategory.setVisibility(View.VISIBLE);
        } else {
            holder.textCategory.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Replaces the current list of items and refreshes the RecyclerView.
     *
     * @param newItems the new list of food items to display
     */
    public void updateItems(List<FoodItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder that caches references to the views in each menu item row.
     */
    static class MenuViewHolder extends RecyclerView.ViewHolder {

        final TextView textName;
        final TextView textDescription;
        final TextView textPrice;
        final TextView textCategory;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textDescription = itemView.findViewById(R.id.textItemDescription);
            textPrice = itemView.findViewById(R.id.textItemPrice);
            textCategory = itemView.findViewById(R.id.textItemCategory);
        }
    }
}
